package org.zeith.tcmp.friendship.net;

import com.mojang.authlib.GameProfile;
import lombok.Builder;
import lombok.val;
import net.minecraft.nbt.*;

import java.io.*;
import java.security.PublicKey;

@Builder(toBuilder = true)
public class FriendshipRequest
{
	public final GameProfile profile;
	public final String signature;
	
	public FriendshipRequest(GameProfile profile, String signature)
	{
		this.profile = profile;
		this.signature = signature;
	}
	
	public FriendshipRequestWithKey withKey(PublicKey key)
	{
		return new FriendshipRequestWithKey(key, this);
	}
	
	public void write(DataOutputStream output)
			throws IOException
	{
		output.writeUTF(signature);
		CompressedStreamTools.write(NBTUtil.writeGameProfile(new NBTTagCompound(), profile), output);
	}
	
	public static FriendshipRequest read(DataInputStream input)
			throws IOException
	{
		String signature = input.readUTF();
		val profile = NBTUtil.readGameProfileFromNBT(CompressedStreamTools.read(input));
		return new FriendshipRequest(profile, signature);
	}
}