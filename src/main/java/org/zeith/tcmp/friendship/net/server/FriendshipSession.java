package org.zeith.tcmp.friendship.net.server;

import org.zeith.cloudflared.core.api.IGameSession;
import org.zeith.cloudflared.core.process.CFDTunnel;

import java.util.UUID;
import java.util.function.Consumer;

public class FriendshipSession
		implements IGameSession
{
	protected final UUID session = UUID.randomUUID();
	public int port;
	protected final Consumer<String> addrReporter;
	
	public FriendshipSession(Consumer<String> addrReporter)
	{
		this.addrReporter = addrReporter;
	}
	
	@Override
	public UUID getSessionID()
	{
		return session;
	}
	
	@Override
	public int getPort()
	{
		return port;
	}
	
	@Override
	public void onTunnelOpen(CFDTunnel tunnel)
	{
		addrReporter.accept(tunnel.getGeneratedHostname());
	}
	
	@Override
	public void onTunnelClosed(CFDTunnel tunnel)
	{
	}
}