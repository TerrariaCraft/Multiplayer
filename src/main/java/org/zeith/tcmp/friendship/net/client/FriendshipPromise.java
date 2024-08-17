package org.zeith.tcmp.friendship.net.client;

import com.zeitheron.hammercore.utils.java.Hashers;
import lombok.val;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.tcmp.crypto.KeyIO;
import org.zeith.tcmp.friendship.FriendEntry;
import org.zeith.tcmp.friendship.FriendshipDatabase;
import org.zeith.tcmp.friendship.net.*;

import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FriendshipPromise
{
	public static CompletableFuture<FriendshipApproval> friendRequest(CloudflaredAPI api, FriendshipDatabase db, FriendshipRequest request, OnlinePerson person, Duration requestTimeout)
	{
		if(person == null) return CompletableFuture.completedFuture(null);
		val access = api.getOrOpenAccess(person.getAddr());
		val exe = api.getGame().getBackgroundExecutor();
		return access.getOpenFuture().thenApplyAsync(port ->
		{
			try(Socket so = new Socket("127.0.0.1", port);
				DataOutputStream out = new DataOutputStream(so.getOutputStream());
				DataInputStream in = new DataInputStream(so.getInputStream())
			)
			{
				int timeoutMs = (int) requestTimeout.toMillis();
				so.setSoTimeout(30000);
				
				byte[] pub = KeyIO.writePublicKey(db.getKeypair().getPublic());
				out.writeUTF(FriendshipDatabase.MAGIC);
				out.writeByte(1);
				out.writeShort(pub.length);
				out.write(pub);
				
				pub = new byte[in.readShort()];
				in.readFully(pub);
				val pk = KeyIO.readPublicKey(pub);
				
				out.writeBoolean(true);
				
				if(!Challenge.runBidiChallenge(true, new SecureRandom(), pk, db.getKeypair().getPrivate(), in, out))
					return null;
				
				request.write(out);
				out.writeLong(timeoutMs);
				
				so.setSoTimeout(timeoutMs);
				if(in.readBoolean())
				{
					return FriendshipApproval.read(in);
				}
			} catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return null;
		}, exe).thenApplyAsync(a ->
		{
			access.closeTunnel();
			return a;
		}, exe);
	}
	
	public static CompletableFuture<Boolean> verifyFriendship(CloudflaredAPI api, FriendshipDatabase db, FriendEntry fr, OnlinePerson person)
	{
		val access = api.getOrOpenAccess(person.getAddr());
		val exe = api.getGame().getBackgroundExecutor();
		return access.getOpenFuture().thenApplyAsync(port ->
		{
			try(Socket so = new Socket("127.0.0.1", port);
				DataOutputStream out = new DataOutputStream(so.getOutputStream());
				DataInputStream in = new DataInputStream(so.getInputStream())
			)
			{
				so.setSoTimeout(30000);
				
				int trials = 4;
				byte[] pub = KeyIO.writePublicKey(db.getKeypair().getPublic());
				out.writeUTF(FriendshipDatabase.MAGIC);
				out.writeByte(2);
				out.writeByte(trials);
				out.writeShort(pub.length);
				out.write(pub);
				
				if(!Challenge.runBidiChallenge(true, new SecureRandom(), fr.getTheirPublicKey(), db.getKeypair().getPrivate(), in, out))
				{
					return false;
				}
				
				out.flush();
				
				if(!in.readBoolean())
				{
					System.out.println("Server does not recognize us :(");
					return false;
				}
				
				SecureRandom sr = new SecureRandom();
				for(int i = 0; i < trials; i++)
				{
					byte[] randomBytes = new byte[512 + sr.nextInt(1024)];
					sr.nextBytes(randomBytes);
					byte[] encBytes = fr.encryptFriendshipMessage(randomBytes);
					out.writeShort(encBytes.length);
					out.write(encBytes);
					long start = System.currentTimeMillis();
					out.flush();
					
					System.out.println("Sent trial " + randomBytes.length);
					
					String sha256 = Hashers.SHA256.encrypt(randomBytes);
					encBytes = new byte[in.readShort()];
					in.readFully(encBytes);
					
					long ms = System.currentTimeMillis() - start;
					
					System.out.println("Receive trial " + encBytes.length + " within " + ms + " ms.");
					
					if(!Hashers.SHA256.encrypt(randomBytes).equals(sha256))
					{
						System.out.println("Mismatch");
						return false;
					}
					
					System.out.println("Match");
				}
				
				return true;
			} catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}, exe).thenApplyAsync(a ->
		{
			access.closeTunnel();
			return a;
		}, exe);
	}
	
	public static CompletableFuture<String> obtainAddress(CloudflaredAPI api, FriendshipDatabase db, FriendEntry fr, OnlinePerson person)
	{
		val access = api.getOrOpenAccess(person.getAddr());
		val exe = api.getGame().getBackgroundExecutor();
		return access.getOpenFuture().thenApplyAsync(port ->
		{
			try(Socket so = new Socket("127.0.0.1", port);
				DataOutputStream out = new DataOutputStream(so.getOutputStream());
				DataInputStream in = new DataInputStream(so.getInputStream())
			)
			{
				so.setSoTimeout(30000);
				
				byte[] pub = KeyIO.writePublicKey(db.getKeypair().getPublic());
				
				out.writeUTF(FriendshipDatabase.MAGIC);
				out.writeByte(3);
				out.writeShort(pub.length);
				out.write(pub);
				
				if(!Challenge.runBidiChallenge(true, new SecureRandom(), fr.getTheirPublicKey(), db.getKeypair().getPrivate(), in, out))
				{
					return null;
				}
				
				out.flush();
				
				if(!in.readBoolean())
				{
					System.out.println("Server does not recognize us :(");
					return null;
				}
				
				return in.readUTF();
			} catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}, exe).thenApplyAsync(a ->
		{
			access.closeTunnel();
			return a;
		}, exe);
	}
}