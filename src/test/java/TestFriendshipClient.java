import com.mojang.authlib.GameProfile;
import org.zeith.tcmp.ConfigsTCMP;
import org.zeith.tcmp.friendship.FriendshipDatabase;
import org.zeith.tcmp.friendship.net.*;
import org.zeith.tcmp.friendship.net.client.FriendshipPromise;

import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.UUID;

public class TestFriendshipClient
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
		
		FriendshipRequest req = new FriendshipRequest(
				wai,
				db.authSignature()
		);
		
		Scanner in = new Scanner(System.in);
		System.out.print("Provide friendship code: ");
		String code = in.nextLine();
		
		OnlinePerson op = FriendshipConstants.list().get(code);
		if(op == null)
		{
			System.out.println("Unable to find " + code);
			return;
		}
		
		FriendshipPromise.friendRequest(TestCommon.api, db, req, op, Duration.ofMinutes(5)).thenAccept(approval ->
		{
			if(approval != null)
			{
				System.out.println(approval.profile + " accepted friend request!");
				db.storeApproval(approval);
				return;
			}
			System.out.println("Remote declined friend request.");
		});
	}
}