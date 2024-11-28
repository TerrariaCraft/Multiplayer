package org.zeith.tcmp.friendship.gui;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import org.zeith.hammeranims.api.McUtil;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class McAuth
{
	private static boolean created;
	
	public static GameProfile updateProfile(GameProfile profile)
	{
		if(profile == null) return null;
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
	
	public static ResourceLocation getSkinTexture(GameProfile profile)
	{
		ResourceLocation location = DefaultPlayerSkin.getDefaultSkinLegacy();
		if(profile != null)
		{
			Minecraft minecraft = Minecraft.getMinecraft();
			
			Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map;
			if(profile.getProperties().containsKey("textures") && (map = minecraft.getSkinManager().loadSkinFromCache(profile)).containsKey(MinecraftProfileTexture.Type.SKIN))
			{
				location = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			} else
			{
				UUID uuid = EntityPlayer.getUUID(profile);
				location = DefaultPlayerSkin.getDefaultSkin(uuid);
			}
		}
		return location;
	}
}