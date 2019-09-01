package org.bihe.client.bin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.SerializationUtils;

public class Encryption {
	private static Cipher cipher;
	private static KeyPairGenerator keyGen;
	private static PrivateKey privateKey;
	private static PublicKey publicKey;
	private static File fileName = new File("data");
	static {
		try {
			if (fileName.exists() == false)
				fileName.createNewFile();
			cipher = Cipher.getInstance("RSA");
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair kp = keyGen.generateKeyPair();
			privateKey = kp.getPrivate();
			publicKey = kp.getPublic();
		} catch (NoSuchPaddingException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static PublicKey getPublicKey() {
		return publicKey;
	}
	
	public static byte[][] encrypt(Object o)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException {
		//encrypt with own public key
		return doFinal(o,null);
	}

	public static byte[][] encrypt(Object o, PublicKey publicKey)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException {
		//encrypt with user's public key
		return doFinal(o,publicKey);
	}

	public static Object decrypt(byte[][] o)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ClassNotFoundException {
		// decrypt data
		// decrypt byte[1] to get AES encrypted key
		// decrypt byte[0] to access data by AES decrypted key
		try {
			cipher.init(Cipher.PRIVATE_KEY, privateKey);
			byte[] decryptedKey = cipher.doFinal(o[1]);
			SecretKey originalKey = new SecretKeySpec(decryptedKey , 0, decryptedKey.length, "AES");
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.DECRYPT_MODE, originalKey);
			byte[] bytePlain = aesCipher.doFinal(o[0]);
			return SerializationUtils.deserialize(bytePlain);
		} catch (NoSuchPaddingException | NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException();
		}
		
	}

	private static byte[][] doFinal(Object o,PublicKey publicKey)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,BadPaddingException, IllegalBlockSizeException, ClassNotFoundException {
		//decrypting process
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
		}catch (IOException ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException();
		}
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128);
		SecretKey secKey = generator.generateKey();
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
		byte[] byteCipher = aesCipher.doFinal(baos.toByteArray());
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		if(publicKey!=null)
			cipher.init(Cipher.PUBLIC_KEY, publicKey);
		else
			cipher.init(Cipher.PUBLIC_KEY, Encryption.publicKey);
		byte[] encryptedKey = cipher.doFinal(secKey.getEncoded());
		
		byte[][] data = new byte[][] {byteCipher,encryptedKey};
		return data;
	}
}















