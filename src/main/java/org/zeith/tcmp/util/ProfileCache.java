package org.zeith.tcmp.util;

import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.utils.java.tuples.Tuple2;
import lombok.val;
import net.minecraft.tileentity.TileEntitySkull;
import org.zeith.hammeranims.api.McUtil;
import org.zeith.tcmp.friendship.gui.McAuth;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ProfileCache
{
	private static final Map<UUID, Tuple2.Mutable2<GameProfile, Instant>> FILLED_PROFILE_CACHE = new HashMap<>();
	
	public static GameProfile fillWithCache(GameProfile profie)
	{
		val tup = FILLED_PROFILE_CACHE.computeIfAbsent(profie.getId(), i -> create(profie));
		update(tup, false);
		return tup.a();
	}
	
	public static Supplier<GameProfile> fillWithCacheRef(GameProfile profie)
	{
		val tup = FILLED_PROFILE_CACHE.computeIfAbsent(profie.getId(), i -> create(profie));
		update(tup, false);
		return tup::a;
	}
	
	private static Tuple2.Mutable2<GameProfile, Instant> create(GameProfile profile)
	{
		Tuple2.Mutable2<GameProfile, Instant> tup = new Tuple2.Mutable2<>(profile, Instant.now());
		update(tup, true);
		return tup;
	}
	
	private static boolean update(Tuple2.Mutable2<GameProfile, Instant> tup, boolean force)
	{
		if(!force && Duration.between(tup.b(), Instant.now()).getSeconds() < 15L)
			return false;
		
		// Avoid contant and unnecessary refreshes
		tup.setB(Instant.now().plusMillis(TimeUnit.DAYS.toMillis(1L)));
		
		McUtil.backgroundExecutor().submit(() ->
		{
			tup.setA(TileEntitySkull.updateGameProfile(tup.a()));
			tup.setB(Instant.now());
		});
		
		return true;
	}
}