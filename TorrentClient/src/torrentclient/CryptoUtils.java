/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentclient;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author J Guadalupe Canales
 */
public class CryptoUtils {
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";

	public synchronized static byte[] encrypt(String key, byte[] inputBytes)
			throws CryptoException {
		return doCrypto(Cipher.ENCRYPT_MODE, key, inputBytes);
	}

	public synchronized static byte[] decrypt(String key, byte[] inputBytes)
			throws CryptoException {
		return doCrypto(Cipher.DECRYPT_MODE, key, inputBytes);
	}

	private synchronized static byte[] doCrypto(int cipherMode, String key, byte[] inputBytes) throws CryptoException {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, secretKey);
			
			byte[] outputBytes = cipher.doFinal(inputBytes);
			return outputBytes;
			
		} catch (NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException  ex) {
			throw new CryptoException("Error encrypting/decrypting file", ex);
		}
	}
}
