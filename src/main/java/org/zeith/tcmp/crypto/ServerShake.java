package org.zeith.tcmp.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ServerShake
{
	private final KeyPair pair;
	
	public ServerShake(String algorithm) throws NoSuchAlgorithmException
	{
		this(KeyPairGenerator.getInstance(algorithm));
	}
	
	public ServerShake(KeyPairGenerator algorithm) throws NoSuchAlgorithmException
	{
		this.pair = algorithm.generateKeyPair();
	}
	
	public PublicKey getPublicKey()
	{
		return pair.getPublic();
	}
	
	public byte[] generateServerShake()
	{
		return getPublicKey().getEncoded();
	}
	
	public ServerCipher generateCipher(byte[] clientShake) throws GeneralSecurityException
	{
		Cipher c = Cipher.getInstance(pair.getPublic().getAlgorithm());
		c.init(Cipher.DECRYPT_MODE, pair.getPrivate());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(c.doFinal(clientShake));
		
		byte[] algo = new byte[bais.read()];
		byte[] key = new byte[bais.read()];
		try
		{
			bais.read(algo);
			bais.read(key);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return new ServerCipher(new SecretKeySpec(key, new String(algo, StandardCharsets.UTF_8)));
	}
}