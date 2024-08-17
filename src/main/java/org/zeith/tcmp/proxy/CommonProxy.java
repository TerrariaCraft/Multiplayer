package org.zeith.tcmp.proxy;

import net.minecraftforge.fml.common.event.*;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameProxy;
import org.zeith.tcmp.MCGameSession;

import java.io.File;
import java.util.Optional;

public interface CommonProxy
		extends IGameProxy
{
	void tryCreateApi();
	
	FriendsProxy friendProxy();
	
	default void preInit(FMLPreInitializationEvent e)
	{
		tryCreateApi();
	}
	
	default void loadComplete(FMLLoadCompleteEvent e) {}
	
	default void serverStarting(FMLServerStartingEvent e) {}
	
	void serverStarted(FMLServerAboutToStartEvent e);
	
	void serverStop(FMLServerStoppingEvent e);
	
	void startSession(MCGameSession session);
	
	Optional<CloudflaredAPI> getApi();
	
	default File getLatestLogFile()
	{
		return new File("logs");
	}
	
	default void construct() {}
}