package org.zeith.tcmp.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class ClientCipher implements ICipher
{
	private final PublicKey publicKey;
	private final SecretKey secretKey;
	private final Cipher cipher;
	
	public ClientCipher(PublicKey publicKey, SecretKey secretKey) throws GeneralSecurityException
	{
		this.publicKey = publicKey;
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
	
	public byte[] generateClientShake() throws GeneralSecurityException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try
		{
			byte[] arr = secretKey.getAlgorithm().getBytes(StandardCharsets.UTF_8);
			byte[] arr2 = secretKey.getEncoded();
			baos.write(arr.length);
			baos.write(arr2.length);
			baos.write(arr);
			baos.write(arr2);
		} catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		Cipher c = Cipher.getInstance(publicKey.getAlgorithm());
		c.init(Cipher.ENCRYPT_MODE, publicKey);
		
		return c.doFinal(baos.toByteArray());
	}
}