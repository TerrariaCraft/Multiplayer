package org.zeith.tcmp.friendship.net;

import com.zeitheron.hammercore.lib.zlib.json.JSONObject;
import com.zeitheron.hammercore.lib.zlib.json.JSONTokener;
import com.zeitheron.hammercore.lib.zlib.web.HttpRequest;
import lombok.val;
import org.zeith.tcmp.TCMultiplayer;
import org.zeith.tcmp.friendship.FriendshipDatabase;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class FriendshipConstants
{
	public static final String USER_AGENT = "TCMultiplayer/" + TCMultiplayer.VERSION;
	public static String LISTING_SERVER = "https://multiplayer.terrariacraft.com/";
	
	public static boolean notify(String username, FriendshipDatabase db, String addr)
	{
		return HttpRequest.get(LISTING_SERVER + "notify")
				.userAgent(USER_AGENT)
				.header("TCMP-Authorization", db.authSignature())
				.header("TCMP-Public-Key", Base64.getUrlEncoder().encodeToString(db.getKeypair().getPublic().getEncoded()))
				.header("TCMP-Username", Base64.getUrlEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)))
				.header("TCMP-Addr", Base64.getUrlEncoder().encodeToString(addr.getBytes(StandardCharsets.UTF_8)))
				.ok();
	}
	
	public static boolean logoff(FriendshipDatabase db)
	{
		return HttpRequest.get(LISTING_SERVER + "logoff")
				.userAgent(USER_AGENT)
				.header("TCMP-Authorization", db.authSignature())
				.header("TCMP-Public-Key", Base64.getUrlEncoder().encodeToString(db.getKeypair().getPublic().getEncoded()))
				.ok();
	}
	
	public static Map<String, OnlinePerson> list()
	{
		JSONObject obj = new JSONTokener(
				HttpRequest.get(LISTING_SERVER + "list")
						.userAgent(USER_AGENT)
						.body()
		).nextValueOBJ().orElse(null);
		if(obj == null) return Collections.emptyMap();
		Map<String, OnlinePerson> people = new HashMap<>();
		for(String key : obj.keySet())
		{
			val o = obj.getJSONObject(key);
			people.put(key, new OnlinePerson(
					key,
					o.getString("name"),
					o.getString("addr")
			));
		}
		return people;
	}
}