package org.zeith.tcmp.friendship;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.utils.java.Hashers;
import lombok.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import org.zeith.tcmp.ConfigsTCMP;
import org.zeith.tcmp.TCMultiplayer;
import org.zeith.tcmp.crypto.KeyIO;
import org.zeith.tcmp.friendship.net.*;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Getter
public class FriendshipDatabase
{
	public static final String MAGIC = "TCMPFRDBREQ";
	private final List<FriendEntry> friends;
	private final KeyPair keypair;
	private GameProfile whoAmI;
	
	public FriendshipDatabase(GameProfile whoAmI, NBTTagCompound tag)
	{
		this.whoAmI = whoAmI;
		val lst = tag.getTagList("Friends", Constants.NBT.TAG_COMPOUND);
		List<FriendEntry> friends = new ArrayList<>(lst.tagCount());
		for(int i = 0; i < lst.tagCount(); i++) friends.add(new FriendEntry(lst.getCompoundTagAt(i)));
		this.friends = friends;
		this.keypair = KeyIO.readKeyPair(tag.getByteArray("Keypair"));
	}
	
	public FriendshipDatabase(GameProfile whoAmI)
	{
		this.whoAmI = whoAmI;
		this.friends = new ArrayList<>();
		this.keypair = KeyIO.createRSA();
	}
	
	public void storeApproval(FriendshipApproval approval)
	{
		friends.removeIf(e -> Objects.equals(e.getProfile().getId(), approval.profile.getId()));
		friends.add(new FriendEntry(approval.profile, approval.pub, approval.key));
		trySave();
	}
	
	public FriendshipApproval acceptRequest(FriendshipRequestWithKey request)
	{
		val key = KeyIO.createAES();
		FriendEntry e = new FriendEntry(request.getRequest().profile, request.getKey(), key);
		this.friends.add(e);
		trySave();
		return new FriendshipApproval(key, keypair.getPublic(), whoAmI);
	}
	
	public void trySave()
	{
		CompletableFuture.runAsync(() ->
		{
			try
			{
				save();
			} catch(IOException ex)
			{
				throw new RuntimeException(ex);
			}
		});
	}
	
	public NBTTagCompound serialize()
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		NBTTagList friends = new NBTTagList();
		for(FriendEntry friend : this.friends) friends.appendTag(friend.serialize());
		tag.setTag("Friends", friends);
		tag.setByteArray("Keypair", KeyIO.writeKeyPair(this.keypair));
		
		return tag;
	}
	
	@SneakyThrows
	public byte[] decryptMessage(byte[] data)
	{
		Cipher c = Cipher.getInstance(KeyIO.RSA_CIPHER);
		c.init(Cipher.DECRYPT_MODE, keypair.getPrivate());
		return c.doFinal(data);
	}
	
	private final Supplier<String> publicKeyHash = Suppliers.memoize(this::computeKeyHash);
	
	protected String computeKeyHash()
	{
		return Hashers.SHA1.encrypt(keypair.getPublic().getEncoded());
	}
	
	public String getMyKey()
	{
		return publicKeyHash.get();
	}
	
	public void save()
			throws IOException
	{
		val f = ConfigsTCMP.friendshipsDatabase;
		TCMultiplayer.LOG.info("Saving friendship database to {}", f);
		f.getParent().toFile().mkdirs();
		try(val out = Files.newOutputStream(f))
		{
			CompressedStreamTools.writeCompressed(serialize(), out);
		}
	}
	
	public static FriendshipDatabase load(GameProfile whoAmI)
			throws IOException
	{
		val f = ConfigsTCMP.friendshipsDatabase;
		TCMultiplayer.LOG.info("Loading friendship database from {}", f);
		if(Files.isRegularFile(f) && Files.isReadable(f))
			try(val in = Files.newInputStream(f))
			{
				return new FriendshipDatabase(whoAmI, CompressedStreamTools.readCompressed(in));
			}
		return null;
	}
	
	public static FriendshipDatabase getOrCreate(GameProfile whoAmI)
			throws IOException
	{
		FriendshipDatabase db = FriendshipDatabase.load(whoAmI);
		if(db == null)
		{
			db = new FriendshipDatabase(whoAmI);
			db.save();
		}
		return db;
	}
	
	public FriendEntry findFriend(PublicKey key)
	{
		return friends.stream()
				.filter(f -> f.is(key))
				.findFirst()
				.orElse(null);
	}
	
	@SneakyThrows
	public String authSignature()
	{
		Signature sig = Signature.getInstance(KeyIO.SIGNATURE);
		sig.initSign(getKeypair().getPrivate());
		sig.update(getKeypair().getPublic().getEncoded());
		return Base64.getUrlEncoder().encodeToString(sig.sign());
	}
}