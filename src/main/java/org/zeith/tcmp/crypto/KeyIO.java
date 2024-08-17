package org.zeith.tcmp.crypto;

import lombok.SneakyThrows;
import lombok.val;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyIO
{
	public static final String ALGO = "RSA";
	public static final String AES_CIPHER = "AES";
	public static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
	public static final String SIGNATURE = "SHA1WithRSA";
	
	@SneakyThrows
	public static byte[] writeSymKey(SecretKey key)
	{
		val baos = new ByteArrayOutputStream();
		val dos = new DataOutputStream(baos);
		byte[] data = key.getEncoded();
		dos.writeUTF(key.getAlgorithm());
		dos.writeShort(data.length);
		dos.write(data);
		return baos.toByteArray();
	}
	
	@SneakyThrows
	public static byte[] writePublicKey(PublicKey key)
	{
		val baos = new ByteArrayOutputStream();
		val dos = new DataOutputStream(baos);
		byte[] data = key.getEncoded();
		dos.writeUTF(key.getAlgorithm());
		dos.writeShort(data.length);
		dos.write(data);
		return baos.toByteArray();
	}
	
	@SneakyThrows
	public static byte[] writePrivateKey(PrivateKey key)
	{
		val baos = new ByteArrayOutputStream();
		val dos = new DataOutputStream(baos);
		byte[] data = key.getEncoded();
		dos.writeUTF(key.getAlgorithm());
		dos.writeShort(data.length);
		dos.write(data);
		return baos.toByteArray();
	}
	
	@SneakyThrows
	public static SecretKey readSymKey(byte[] data)
	{
		val in = new DataInputStream(new ByteArrayInputStream(data));
		String algo = in.readUTF();
		byte[] key = new byte[in.readShort()];
		in.readFully(key);
		return new SecretKeySpec(key, algo);
	}
	
	@SneakyThrows
	public static PublicKey readPublicKey(byte[] data)
	{
		val in = new DataInputStream(new ByteArrayInputStream(data));
		String algo = in.readUTF();
		byte[] key = new byte[in.readShort()];
		in.readFully(key);
		
		return KeyFactory.getInstance(algo).generatePublic(new X509EncodedKeySpec(key));
	}
	
	@SneakyThrows
	public static PrivateKey readPrivateKey(byte[] data)
	{
		val in = new DataInputStream(new ByteArrayInputStream(data));
		String algo = in.readUTF();
		byte[] key = new byte[in.readShort()];
		in.readFully(key);
		return KeyFactory.getInstance(algo).generatePrivate(new PKCS8EncodedKeySpec(key));
	}
	
	@SneakyThrows
	public static byte[] writeKeyPair(KeyPair pair)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		byte[] pub = writePublicKey(pair.getPublic());
		byte[] priv = writePrivateKey(pair.getPrivate());
		dos.writeShort(pub.length);
		dos.write(pub);
		dos.writeShort(priv.length);
		dos.write(priv);
		return baos.toByteArray();
	}
	
	@SneakyThrows
	public static KeyPair readKeyPair(byte[] data)
	{
		val in = new DataInputStream(new ByteArrayInputStream(data));
		byte[] buf = new byte[in.readShort()];
		in.readFully(buf);
		val pub = readPublicKey(buf);
		buf = new byte[in.readShort()];
		in.readFully(buf);
		val priv = readPrivateKey(buf);
		return new KeyPair(pub, priv);
	}
	
	@SneakyThrows
	public static KeyPair createRSA()
	{
		return KeyPairGenerator.getInstance(ALGO).generateKeyPair();
	}
	
	@SneakyThrows
	public static SecretKey createAES()
	{
		return KeyGenerator.getInstance("AES").generateKey();
	}
}