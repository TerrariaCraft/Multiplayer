package org.zeith.tcmp.proxy;

import lombok.Getter;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.*;
import org.zeith.tcmp.TCMultiplayer;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class FriendsProxy
		implements IGameProxy
{
	@Getter
	CloudflaredAPI api;
	
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	protected final IGameProxy rootProxy;
	
	public FriendsProxy(IGameProxy rootProxy)
	{
		this.rootProxy = rootProxy;
	}
	
	@Override
	public ExecutorService getBackgroundExecutor()
	{
		return rootProxy.getBackgroundExecutor();
	}
	
	@Override
	public void addListener(IGameListener listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(IGameListener listener)
	{
		listeners.remove(listener);
	}
	
	@Override
	public void sendChatMessage(String message)
	{
		TCMultiplayer.LOG.info(message);
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		switch(level)
		{
			case INFO:
				TCMultiplayer.LOG.info("{}\t\t{}", title, subtitle);
				return;
			case WARNING:
				TCMultiplayer.LOG.warn("{}\t\t{}", title, subtitle);
				return;
			case CRITICAL:
				TCMultiplayer.LOG.error("{}\t\t{}", title, subtitle);
				return;
			default:
				return;
		}
	}
	
	@Override
	public List<IGameListener> getListeners()
	{
		return listeners;
	}
}