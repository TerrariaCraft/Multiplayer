package org.zeith.tcmp.friendship.gui;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntitySkull;
import org.zeith.hammeranims.api.McUtil;

import java.io.File;
import java.util.UUID;

public class McAuth
{
	private static boolean created;
	
	public static GameProfile updateProfile(GameProfile profile)
	{
		if(profile == null) return null;
		
		if(!Minecraft.getMinecraft().isCallingFromMinecraftThread())
		{
			Minecraft.getMinecraft().addScheduledTask(() ->
			{
				TileEntitySkull.updateGameProfile(profile);
			});
			
			return profile;
		}
		
		return TileEntitySkull.updateGameProfile(profile);
	}
	
	public static synchronized void createServices()
	{
		if(created) return;
		created = true;
		McUtil.backgroundExecutor().submit(() ->
		{
			val mc = Minecraft.getMinecraft();
			AuthenticationService auth = new YggdrasilAuthenticationService(mc.getProxy(), UUID.randomUUID().toString());
			TileEntitySkull.setSessionService(auth.createMinecraftSessionService());
			TileEntitySkull.setProfileCache(new PlayerProfileCache(auth.createProfileRepository(), new File(mc.gameDir, MinecraftServer.USER_CACHE_FILE.getName())));
			PlayerProfileCache.setOnlineMode(false);
		});
	}
}