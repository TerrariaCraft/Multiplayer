import com.mojang.authlib.GameProfile;
import org.zeith.tcmp.ConfigsTCMP;
import org.zeith.tcmp.friendship.FriendshipDatabase;
import org.zeith.tcmp.friendship.net.server.FriendshipServer;

import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TestFriendshipServer
{
	public static void main(String[] args)
	{
		ConfigsTCMP.friendshipsDatabase = FileSystemView.getFileSystemView().getDefaultDirectory()
				.toPath()
				.resolve("My Games")
				.resolve("TerrariaCraft")
				.resolve("TCMultiplayer_1")
				.resolve("friends.db");
		FriendshipDatabase db;
		try
		{
			GameProfile wai = new GameProfile(new UUID(2432763754357309676L, 636437358738L), "ZeithTheWolf");
			db = FriendshipDatabase.getOrCreate(wai);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		Scanner in = new Scanner(System.in);
		
		FriendshipServer fs = new FriendshipServer(TestCommon.api);
		fs.database = db;
		fs.gameAddress = "yolo";
		fs.friendshipConfirmer = (request, timeout) ->
		{
			System.out.println("Got request from " + request.getRequest().profile);
			System.out.println("Accept? (true/false)");
			return CompletableFuture.supplyAsync(() ->
			{
				if(Boolean.parseBoolean(in.nextLine()))
					return Optional.ofNullable(db.acceptRequest(request));
				return Optional.empty();
			});
		};
		fs.start();
	}
}