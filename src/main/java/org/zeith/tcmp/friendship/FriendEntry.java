package org.zeith.tcmp.friendship;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.utils.java.Hashers;
import lombok.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import org.zeith.tcmp.crypto.KeyIO;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Supplier;

public class FriendEntry
{
	@Getter
	private GameProfile profile;
	
	@Getter
	private final PublicKey theirPublicKey;
	
	@Getter
	private final SecretKey friendship;
	
	private final Supplier<String> publicKeyHash;
	
	public FriendEntry(NBTTagCompound entry)
	{
		this.profile = NBTUtil.readGameProfileFromNBT(entry.getCompoundTag("User"));
		this.theirPublicKey = KeyIO.readPublicKey(entry.getByteArray("TheirKey"));
		this.friendship = KeyIO.readSymKey(entry.getByteArray("Friendship"));
		this.publicKeyHash = Suppliers.memoize(this::computeKeyHash);
	}
	
	public FriendEntry(GameProfile profile, PublicKey theirPublicKey, SecretKey friendship)
	{
		this.profile = profile;
		this.theirPublicKey = theirPublicKey;
		this.friendship = friendship;
		this.publicKeyHash = Suppliers.memoize(this::computeKeyHash);
	}
	
	protected String computeKeyHash()
	{
		return Hashers.SHA1.encrypt(theirPublicKey.getEncoded());
	}
	
	public boolean updateProfile()
	{
		if(profile == null) return false;
		val pm = profile.getProperties();
		val pn = profile.getName();
		profile = Minecraft.getMinecraft()
				.getSessionService()
				.fillProfileProperties(
						new GameProfile(profile.getId(), null),
						false
				);
		return !Objects.equals(pm, profile.getProperties()) || !Objects.equals(pn, profile.getName());
	}
	
	public NBTTagCompound serialize()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("User", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
		tag.setByteArray("TheirKey", KeyIO.writePublicKey(theirPublicKey));
		tag.setByteArray("Friendship", KeyIO.writeSymKey(friendship));
		return tag;
	}
	
	/**
	 * Encrypt message with a friendship key.
	 */
	@SneakyThrows
	public byte[] encryptFriendshipMessage(byte[] data)
	{
		Cipher c = Cipher.getInstance(KeyIO.AES_CIPHER);
		c.init(Cipher.ENCRYPT_MODE, friendship);
		return c.doFinal(data);
	}
	
	/**
	 * Decrypt message with a friendship key.
	 */
	@SneakyThrows
	public byte[] decryptFriendshipMessage(byte[] data)
	{
		Cipher c = Cipher.getInstance(KeyIO.AES_CIPHER);
		c.init(Cipher.DECRYPT_MODE, friendship);
		return c.doFinal(data);
	}
	
	/**
	 * This allows us to encode challenges for a remote friend to prove their ownership of the key.
	 * <p>
	 * Used to verify the remote connection is who they say they are.
	 */
	@SneakyThrows
	public byte[] encryptMessage(byte[] data)
	{
		Cipher c = Cipher.getInstance(KeyIO.RSA_CIPHER);
		c.init(Cipher.ENCRYPT_MODE, theirPublicKey);
		return c.doFinal(data);
	}
	
	public boolean is(PublicKey key)
	{
		return theirPublicKey.equals(key);
	}
	
	public String getKey()
	{
		return publicKeyHash.get();
	}
}