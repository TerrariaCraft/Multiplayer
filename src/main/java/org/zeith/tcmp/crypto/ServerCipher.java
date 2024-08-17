package org.zeith.tcmp.crypto;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class ServerCipher implements ICipher
{
	private final SecretKey secretKey;
	private final Cipher cipher;
	
	public ServerCipher(SecretKey secretKey) throws GeneralSecurityException
	{
		this.secretKey = secretKey;
		this.cipher = Cipher.getInstance(secretKey.getAlgorithm());
	}

	@Override
	public byte[] encrypt(byte[] data) throws GeneralSecurityException
	{
		return encrypt(data, 0, data.length);
	}

	@Override
	public byte[] decrypt(byte[] data) throws GeneralSecurityException
	{
		return decrypt(data, 0, data.length);
	}

	@Override
	public byte[] encrypt(byte[] data, int off, int len) throws GeneralSecurityException
	{
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(data, off, len);
	}
	
	@Override
	public byte[] decrypt(byte[] data, int off, int len) throws GeneralSecurityException
	{
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(data, off, len);
	}
}