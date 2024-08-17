import com.mojang.authlib.GameProfile;
import org.zeith.tcmp.ConfigsTCMP;
import org.zeith.tcmp.friendship.FriendshipDatabase;
import org.zeith.tcmp.friendship.FriendEntry;
import org.zeith.tcmp.friendship.net.FriendshipConstants;
import org.zeith.tcmp.friendship.net.OnlinePerson;
import org.zeith.tcmp.friendship.net.client.FriendshipPromise;

import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TestFriendshipClientSCAN
{
	public static void main(String[] args)
	{
		ConfigsTCMP.friendshipsDatabase = FileSystemView.getFileSystemView().getDefaultDirectory()
				.toPath()
				.resolve("My Games")
				.resolve("TerrariaCraft")
				.resolve("TCMultiplayer_2")
				.resolve("friends.db");
		
		GameProfile wai = new GameProfile(new UUID(230962476L, 2436347437358738L), "Zeitheron");
		FriendshipDatabase db;
		try
		{
			db = FriendshipDatabase.getOrCreate(wai);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		Map<String, OnlinePerson> people = FriendshipConstants.list();
		if(people.isEmpty())
		{
			System.out.println("Nobody is online :(");
			return;
		}
		for(FriendEntry friend : db.getFriends())
		{
			OnlinePerson fren = people.get(friend.getKey());
			System.out.println("Found " + fren.getUsername() + " online!");
			FriendshipPromise.obtainAddress(TestCommon.api, db, friend, fren).thenAccept(ga ->
			{
				System.out.println("Game address of " + fren.getUsername() + ": " + ga);
			});
		}
	}
}