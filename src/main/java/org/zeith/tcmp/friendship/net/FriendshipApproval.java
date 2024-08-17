package org.zeith.tcmp.friendship.net;

import com.mojang.authlib.GameProfile;
import lombok.val;
import net.minecraft.nbt.*;
import org.zeith.tcmp.crypto.KeyIO;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.PublicKey;

public class FriendshipApproval
{
	public final GameProfile profile;
	public final SecretKey key;
	public final PublicKey pub;
	
	public FriendshipApproval(SecretKey key, PublicKey pub, GameProfile profile)
	{
		this.key = key;
		this.pub = pub;
		this.profile = profile;
	}
	
	public void write(DataOutputStream output)
			throws IOException
	{
		CompressedStreamTools.write(NBTUtil.writeGameProfile(new NBTTagCompound(), profile), output);
		byte[] pk = KeyIO.writeSymKey(key);
		output.writeShort(pk.length);
		output.write(pk);
		pk = KeyIO.writePublicKey(pub);
		output.writeShort(pk.length);
		output.write(pk);
	}
	
	public static FriendshipApproval read(DataInputStream input)
			throws IOException
	{
		val profile = NBTUtil.readGameProfileFromNBT(CompressedStreamTools.read(input));
		byte[] pk = new byte[input.readShort()];
		input.readFully(pk);
		val key = KeyIO.readSymKey(pk);
		pk = new byte[input.readShort()];
		input.readFully(pk);
		val pub = KeyIO.readPublicKey(pk);
		return new FriendshipApproval(key, pub, profile);
	}
}