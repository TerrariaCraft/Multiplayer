package org.zeith.tcmp;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.tcmp.proxy.CommonProxy;
import org.zeith.terraria.api.mod.ITerrariaMod;

@Mod(
		modid = TCMultiplayer.MODID,
		name = TCMultiplayer.NAME,
		guiFactory = "org.zeith.tcmp.ConfigFactory",
		certificateFingerprint = "9f5e2a811a8332a842b34f6967b7db0ac4f24856",
		version = "@VERSION@", // This gets automatically replaced with actual version from build.txt when compiling the mod into jarfile.
		dependencies = "required-after:terraria",
		clientSideOnly = true
)
public class TCMultiplayer
		implements ITerrariaMod
{
	public static final Logger LOG = LogManager.getLogger("TerrariaCraftMultiplayer");
	
	public static final String MODID = "tcmp";
	public static final String NAME = "TerrariaCraft Multiplayer";
	
	@SidedProxy(
			clientSide = "org.zeith.tcmp.proxy.ClientProxy",
			serverSide = "org.zeith.tcmp.proxy.ServerProxy",
			modId = MODID
	)
	public static CommonProxy PROXY;
	
	public TCMultiplayer()
	{
		// Used to add custom recipes
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@EventHandler
	public void construction(FMLConstructionEvent event)
	{
		// This is very recommended for common addon setup.
		ITerrariaMod.super.constructionEvent(event);
		
		// Pass construction to proxies.
		PROXY.construct();
		
		LOG.info("Constructed.");
	}
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		PROXY.preInit(e);
	}
	
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent e)
	{
		PROXY.loadComplete(e);
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent e)
	{
		PROXY.serverStarting(e);
	}
	
	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent e)
	{
		PROXY.serverStarted(e);
	}
	
	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent e)
	{
		PROXY.serverStop(e);
	}
	
	// This is left like so intentionally.
	// When compiled, will get replaced by "12.5.1".contains("@VERSION") thus returning FALSE
	@SuppressWarnings("ConstantValue")
	public static final boolean IN_DEV = "@VERSION@".contains("@VERSION");
	public static final String VERSION = "@VERSION@";
}
