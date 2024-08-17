package org.zeith.tcmp.friendship.net.server;

import com.zeitheron.hammercore.utils.java.Hashers;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.process.CFDTunnel;
import org.zeith.cloudflared.core.process.ShutdownTunnels;
import org.zeith.tcmp.TCMultiplayer;
import org.zeith.tcmp.crypto.KeyIO;
import org.zeith.tcmp.friendship.FriendshipDatabase;
import org.zeith.tcmp.friendship.net.*;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;

public class FriendshipServer
		extends Thread
{
	private CFDTunnel tunnel;
	
	@Getter
	private final CloudflaredAPI api;
	
	private ServerSocket server;
	
	protected boolean interruptAsked;
	
	public String gameAddress;
	
	private String addr;
	public FriendshipDatabase database;
	
	private ScheduledExecutorService executor;
	
	protected ScheduledFuture<?> reporter;
	
	public IFriendshipConfirmer friendshipConfirmer = (request, timeout) -> CompletableFuture.completedFuture(Optional.empty());
	
	public FriendshipServer(CloudflaredAPI api)
	{
		this.api = api;
	}
	
	public FriendshipServer(@NotNull String name, CloudflaredAPI api)
	{
		super(name);
		this.api = api;
	}
	
	public FriendshipServer(@Nullable ThreadGroup group, @NotNull String name, CloudflaredAPI api)
	{
		super(group, name);
		this.api = api;
	}
	
	@Override
	@SneakyThrows
	public synchronized void start()
	{
		if(server != null)
		{
			server.close();
			server = null;
		}
		server = new ServerSocket(0);
		server.setSoTimeout(250);
		if(executor != null) executor.shutdown();
		executor = Executors.newScheduledThreadPool(8, r ->
		{
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		});
		super.start();
		if(tunnel != null)
		{
			tunnel.closeTunnel();
			tunnel = null;
		}
		ensureTunnelRunning();
	}
	
	private final FriendshipSession session = new FriendshipSession(a ->
	{
		addr = a;
		reportActivity();
	})
	{
		@Override
		public void onTunnelClosed(CFDTunnel tunnel)
		{
			super.onTunnelClosed(tunnel);
			if(Thread.currentThread() instanceof ShutdownTunnels)
			{
				interruptAsked = true;
				return;
			}
			if(!interruptAsked)
			{
				TCMultiplayer.LOG.info("Friend server closed while server is active. Rebooting tunnel.");
				if(FriendshipServer.this.tunnel != null)
				{
					FriendshipServer.this.tunnel.closeTunnel();
					FriendshipServer.this.tunnel = null;
				}
				ensureTunnelRunning();
			}
		}
	};
	
	public void ensureTunnelRunning()
	{
		if(tunnel != null && tunnel.isAlive()) return;
		session.port = server.getLocalPort();
		tunnel = api.createTunnel(session, server.getLocalPort(), null);
		tunnel.start();
	}
	
	public void reportActivity()
	{
		if(database != null && addr != null)
		{
			boolean ok = FriendshipConstants.notify(database.getWhoAmI().getName(), database, addr);
			if(ok) TCMultiplayer.LOG.info("Updated online presence. {}", Hashers.SHA1.encrypt(database.getKeypair().getPublic().getEncoded()));
			else TCMultiplayer.LOG.warn("Unable to update online presence.");
		}
	}
	
	@Override
	@SneakyThrows
	public void run()
	{
		reporter = executor.scheduleWithFixedDelay(this::reportActivity, 0L, 5L, TimeUnit.MINUTES);
		try(ServerSocket ss = server)
		{
			while(!interruptAsked)
			{
				try
				{
					Socket s = ss.accept();
					s.setSoTimeout(30000);
					executor.submit(() ->
					{
						try(Socket so = s; DataOutputStream out = new DataOutputStream(so.getOutputStream());
							DataInputStream in = new DataInputStream(so.getInputStream()))
						{
							if(!in.readUTF().equals(FriendshipDatabase.MAGIC))
								return null;
							
							byte action = in.readByte();
							
							if(action == 1) // friend request
							{
								byte[] pub = new byte[in.readUnsignedShort()];
								in.readFully(pub);
								val pk = KeyIO.readPublicKey(pub);
								
								byte[] ourPub = KeyIO.writePublicKey(database.getKeypair().getPublic());
								out.writeShort(ourPub.length);
								out.write(ourPub);
								out.flush();
								
								if(!in.readBoolean())
									return null;
								
								if(!Challenge.runBidiChallenge(false, new SecureRandom(), pk, database.getKeypair().getPrivate(), in, out))
									return null;
								
								val req = FriendshipRequest.read(in);
								
								Duration timeout = Duration.ofMillis(Math.min(TimeUnit.MINUTES.toMillis(30L), in.readLong()));
								
								Optional<FriendshipApproval> approval = Optional.empty();
								val cf = friendshipConfirmer.confirm(req.withKey(pk), timeout);
								try
								{
									approval = cf.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
								} catch(TimeoutException ignored)
								{
								}
								
								if(cf.isCancelled())
								{
									out.writeBoolean(false);
									return null;
								}
								
								out.writeBoolean(approval.isPresent());
								if(approval.isPresent()) approval.get().write(out);
								return null;
							} else if(action == 2)
							{
								int trials = in.readByte();
								
								byte[] pub = new byte[in.readShort()];
								in.readFully(pub);
								val pk = KeyIO.readPublicKey(pub);
								val fr = database.findFriend(pk);
								if(fr == null || !Challenge.runBidiChallenge(false, new SecureRandom(), pk, database.getKeypair().getPrivate(), in, out))
								{
									out.writeBoolean(false);
									return null;
								}
								
								out.writeBoolean(true);
								
								for(int i = 0; i < trials; i++)
								{
									byte[] enc = new byte[in.readShort()];
									in.readFully(enc);
									enc = fr.decryptFriendshipMessage(enc);
									out.writeShort(enc.length);
									out.write(enc);
								}
							} else if(action == 3)
							{
								byte[] pub = new byte[in.readShort()];
								in.readFully(pub);
								val pk = KeyIO.readPublicKey(pub);
								val fr = database.findFriend(pk);
								
								if(fr == null || !Challenge.runBidiChallenge(false, new SecureRandom(), pk, database.getKeypair().getPrivate(), in, out) || gameAddress == null)
								{
									out.writeBoolean(false);
									return null;
								}
								out.writeBoolean(true);
								
								out.writeUTF(gameAddress);
							}
						} catch(Exception e)
						{
							e.printStackTrace();
							throw e;
						}
						return null;
					});
				} catch(SocketTimeoutException ste)
				{
					continue;
				}
			}
		} finally
		{
			interruptAsked = true;
			if(database != null)
			{
				FriendshipConstants.logoff(database);
			}
			executor.shutdown();
			if(tunnel != null)
			{
				tunnel.closeTunnel();
				tunnel = null;
			}
		}
		server = null;
	}
	
	@Override
	public void interrupt()
	{
		interruptAsked = true;
		super.interrupt();
		if(tunnel != null)
		{
			tunnel.closeTunnel();
			tunnel = null;
		}
		if(server != null)
		{
			try
			{
				server.close();
				server = null;
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		if(reporter != null)
		{
			reporter.cancel(false);
			reporter = null;
		}
		if(executor != null)
		{
			executor.shutdown();
			executor = null;
		}
		if(database != null)
		{
			FriendshipConstants.logoff(database);
		}
	}
}