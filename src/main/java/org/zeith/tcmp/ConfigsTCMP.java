package org.zeith.tcmp;

import com.zeitheron.hammercore.cfg.HCModConfigurations;
import com.zeitheron.hammercore.cfg.IConfigReloadListener;
import com.zeitheron.hammercore.cfg.fields.*;
import lombok.val;
import net.minecraftforge.common.config.Configuration;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@HCModConfigurations(modid = "tcmp")
public class ConfigsTCMP
		implements IConfigReloadListener
{
	public static ConfigsTCMP configInstance;
	public static Configuration cfg;
	
	@ModConfigPropertyInt(
			name = "Custom Port Override",
			category = "Hosting",
			defaultValue = 0,
			min = 0,
			max = 65534,
			comment = "Which port should be forced when opening world to LAN? Keep at 0 to retain Vanilla behavior."
	)
	public static int customPortOverride;
	
	@ModConfigPropertyBool(
			name = "Start Tunnel",
			category = "Hosting",
			defaultValue = true,
			comment = "Should Argo Tunnel be started whenever the hosting session starts?"
	)
	public static boolean startTunnel = true;
	
	@ModConfigPropertyString(
			name = "Friendship Database",
			category = "Friendship",
			defaultValue = "%global%/friends.db",
			comment = "Where would the friends database be stored? Please make sure to backup the file to avoid loss.\nUsually stored in 'Documents/My Games/TerrariaCraft/TCMultiplayer/friends.db'",
			allowedValues = {}
	)
	public static String friendshipDbPath;
	
	public static Path friendshipsDatabase;
	
	public ConfigsTCMP()
	{
		configInstance = this;
	}
	
	public static ConfigsTCMP get()
	{
		return configInstance;
	}
	
	private static long lastReload = System.currentTimeMillis();
	
	@Override
	public void reloadCustom(Configuration cfgs)
	{
		cfg = cfgs;
		
		val tcmprt = FileSystemView.getFileSystemView().getDefaultDirectory()
				.toPath()
				.resolve("My Games")
				.resolve("TerrariaCraft")
				.resolve("TCMultiplayer")
				.toFile()
				.getAbsolutePath();
		
		String dbp = friendshipDbPath;
		dbp = dbp.replace("%global%", tcmprt).replace('/', File.separatorChar);
		friendshipsDatabase = Paths.get(dbp);
		friendshipsDatabase.getParent().toFile().mkdirs();
		
		if(System.currentTimeMillis() - lastReload < 1000L) return;
		TCMultiplayer.LOG.info("Reloaded configs.");
		if(TCMultiplayer.PROXY.getApi().isPresent()) return;
		TCMultiplayer.PROXY.tryCreateApi();
		lastReload = System.currentTimeMillis();
	}
}