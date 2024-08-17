package org.zeith.tcmp.friendship.net;

import org.zeith.tcmp.crypto.KeyIO;

import java.io.*;
import java.security.*;

public class Challenge
{
	public static boolean runBidiChallenge(boolean client, SecureRandom rng, PublicKey theirPubKey, PrivateKey outPrivKey, DataInputStream in, DataOutputStream out)
			throws IOException
	{
		if(client)
		{
			boolean v = outgoingChallenge(rng, theirPubKey, in, out);
			incomingChallenge(outPrivKey, in, out);
			return v;
		} else
		{
			incomingChallenge(outPrivKey, in, out);
			return outgoingChallenge(rng, theirPubKey, in, out);
		}
	}
	
	public static boolean outgoingChallenge(SecureRandom rng, PublicKey key, DataInputStream in, DataOutputStream out)
			throws IOException
	{
		byte[] randomBytes = new byte[512 + rng.nextInt(1024)];
		rng.nextBytes(randomBytes);
		out.writeShort(randomBytes.length);
		out.write(randomBytes);
		
		try
		{
			Signature sig = Signature.getInstance(KeyIO.SIGNATURE);
			sig.initVerify(key);
			sig.update(randomBytes);
			byte[] recSig = new byte[in.readShort()];
			in.readFully(recSig);
			return sig.verify(recSig);
		} catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e)
		{
			throw new IOException(e);
		}
	}
	
	public static void incomingChallenge(PrivateKey key, DataInputStream in, DataOutputStream out)
			throws IOException
	{
		byte[] randomBytes = new byte[in.readShort()];
		in.readFully(randomBytes);
		
		try
		{
			Signature sig = Signature.getInstance(KeyIO.SIGNATURE);
			sig.initSign(key);
			sig.update(randomBytes);
			randomBytes = sig.sign();
			out.writeShort(randomBytes.length);
			out.write(randomBytes);
		} catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e)
		{
			throw new IOException(e);
		}
	}
}