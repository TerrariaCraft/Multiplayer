package org.zeith.tcmp.proxy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.tcmp.*;
import org.zeith.tcmp.MCGameSession;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class ServerProxy
		implements CommonProxy
{
	private CloudflaredAPI api;
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	public IGameSession startedSession;
	protected MinecraftServer server;
	
	@Override
	public void tryCreateApi()
	{
		try
		{
			api = CloudflaredAPIFactory.builder()
					.gameProxy(this)
					.build()
					.createApi();
		} catch(CloudflaredNotFoundException ex)
		{
			TCMultiplayer.LOG.fatal("Unable to create communicate with cloudflared. Are you sure you have cloudflared installed?", ex);
		}
	}
	
	@Override
	public FriendsProxy friendProxy()
	{
		return null;
	}
	
	@Override
	public void startSession(MCGameSession session)
	{
		startedSession = session;
		for(IGameListener listener : listeners)
			listener.onHostingStart(session);
	}
	
	@Override
	public List<IGameListener> getListeners()
	{
		return listeners;
	}
	
	@Override
	public Optional<CloudflaredAPI> getApi()
	{
		return Optional.ofNullable(api);
	}
	
	@Override
	public void serverStarted(FMLServerAboutToStartEvent e)
	{
		server = e.getServer();
		if(api != null) api.closeAllAccesses();
		if(ConfigsTCMP.startTunnel)
		{
			server.sendMessage(new TextComponentTranslation("chat.cloudflared:starting_tunnel"));
			startSession(new MCGameSession(server.getServerPort(), UUID.randomUUID(), server));
		}
	}
	
	@Override
	public void serverStop(FMLServerStoppingEvent e)
	{
		server = null;
		if(startedSession != null)
		{
			for(IGameListener listener : listeners)
				listener.onHostingEnd(startedSession);
			startedSession = null;
		}
	}
	
	@Override
	public ExecutorService getBackgroundExecutor()
	{
		return HttpUtil.DOWNLOADER_EXECUTOR;
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
		server.sendMessage(new TextComponentTranslation(message));
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		server.sendMessage(new TextComponentTranslation(title));
	}
}