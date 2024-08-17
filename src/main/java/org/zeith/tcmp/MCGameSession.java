package org.zeith.tcmp;

import com.zeitheron.hammercore.utils.base.Cast;
import lombok.val;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.zeith.cloudflared.core.process.CFDTunnel;
import org.zeith.tcmp.proxy.ClientProxy;

import java.util.UUID;

public class MCGameSession
		extends org.zeith.cloudflared.core.api.MCGameSession
{
	public static final String SESSION_PREFIX = "tcmp://";
	protected final ICommandSender owner;
	
	public MCGameSession(int serverPort, UUID host, ICommandSender owner)
	{
		super(serverPort, host);
		this.owner = owner;
	}
	
	@Override
	public void onTunnelOpen(CFDTunnel tunnel)
	{
		String hostnameSTR = Cast.or(tunnel.getGeneratedHostname(), tunnel.getApi().getConfigs().getHostname().get());
		if(hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
		
		if(hostnameSTR == null)
		{
			ITextComponent txt = new TextComponentTranslation("chat.cloudflared:game_logs")
					.setStyle(new Style()
							.setColor(TextFormatting.BLUE)
							.setUnderlined(true)
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.cloudflared:click_to_open")))
							.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, TCMultiplayer.PROXY.getLatestLogFile().getAbsolutePath()))
					);
			owner.sendMessage(new TextComponentTranslation("chat.cloudflared:tunnel_open_unknown", txt));
			return;
		}
		
		if(hostnameSTR.contains("://"))
		{
			hostnameSTR = SESSION_PREFIX + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
		}
		
		val fss = ClientProxy.getFriendshipServer();
		if(fss != null) fss.gameAddress = hostnameSTR;
		
		ITextComponent hostname = new TextComponentString(hostnameSTR)
				.setStyle(new Style()
						.setColor(TextFormatting.BLUE)
						.setUnderlined(true)
						.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, hostnameSTR))
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.cloudflared:click_to_suggest")))
				);
		
		owner.sendMessage(new TextComponentTranslation("chat.cloudflared:tunnel_open", hostname));
		TCMultiplayer.LOG.warn("Game tunnel open: {}", hostnameSTR);
	}
	
	@Override
	public void onTunnelClosed(CFDTunnel tunnel)
	{
		String hostnameSTR = Cast.or(tunnel.getGeneratedHostname(), tunnel.getApi().getConfigs().getHostname().get());
		if(hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
		
		if(hostnameSTR != null && hostnameSTR.contains("://"))
			hostnameSTR = SESSION_PREFIX + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
		
		TCMultiplayer.LOG.warn("Game tunnel closed: {}", hostnameSTR);
		
		val fss = ClientProxy.getFriendshipServer();
		if(fss != null) fss.gameAddress = "";
	}
}