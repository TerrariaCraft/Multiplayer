package org.zeith.tcmp.crypto;

import java.security.GeneralSecurityException;

public interface ICipher
{
	byte[] encrypt(byte[] data) throws GeneralSecurityException;
	
	byte[] decrypt(byte[] data) throws GeneralSecurityException;
	
	byte[] encrypt(byte[] data, int off, int len) throws GeneralSecurityException;
	
	byte[] decrypt(byte[] data, int off, int len) throws GeneralSecurityException;
}