package org.zeith.tcmp.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyGenerator;

public class ClientShake
{
	private final PublicKey key;
	
	public ClientShake(String algorithm, byte[] serverShake) throws GeneralSecurityException
	{
		this(KeyFactory.getInstance(algorithm), serverShake);
	}
	
	public ClientShake(KeyFactory algorithm, byte[] serverShake) throws GeneralSecurityException
	{
		this.key = algorithm.generatePublic(new X509EncodedKeySpec(serverShake));
	}
	
	public ClientCipher generateCipher(String algorithm) throws GeneralSecurityException
	{
		return generateCipher(KeyGenerator.getInstance(algorithm));
	}
	
	public ClientCipher generateCipher(KeyGenerator algorithm) throws GeneralSecurityException
	{
		return new ClientCipher(key, algorithm.generateKey());
	}
}