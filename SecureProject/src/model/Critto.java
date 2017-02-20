package model;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Critto {

	private MessageDigest hashCrittSic = null;
	private SecureRandom prng = null;

	public Critto() throws NoSuchAlgorithmException {
		this.hashCrittSic = MessageDigest.getInstance("SHA-1");
		this.prng = new SecureRandom();
	}

	public byte[] encrypt(final int inst, final byte[] keyBytes, final byte[] ivBytes, final byte[] messageBytes)
			throws Exception {

		if (inst == Launcher.ECB)
			return transformECB("AES/ECB/PKCS5Padding", Cipher.ENCRYPT_MODE, keyBytes, messageBytes);
		else if (inst == Launcher.CBC)
			return transformCBC("AES/CBC/PKCS5Padding", Cipher.ENCRYPT_MODE, keyBytes, ivBytes, messageBytes);
		else
			throw new InvalidAlgorithmParameterException("inst");
	}

	public byte[] decrypt(final int inst, final byte[] keyBytes, final byte[] ivBytes, final byte[] messageBytes)
			throws Exception {

		if (inst == Launcher.ECB)
			return transformECB("AES/ECB/PKCS5Padding", Cipher.DECRYPT_MODE, keyBytes, messageBytes);
		else if (inst == Launcher.CBC)
			return transformCBC("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, keyBytes, ivBytes, messageBytes);
		else
			throw new InvalidAlgorithmParameterException("inst");

	}

	private byte[] transformCBC(final String cipherInstance, final int mode, final byte[] keyBytes,
			final byte[] ivBytes, final byte[] messageBytes)
			throws GeneralSecurityException {

		final SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		final IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		byte[] transformedBytes = null;

		try {

			final Cipher cipher = Cipher.getInstance(cipherInstance);
			cipher.init(mode, keySpec, ivSpec);
			transformedBytes = cipher.doFinal(messageBytes);

		} catch (Exception e) {
			throw e;
		}
		return transformedBytes;
	}

	private byte[] transformECB(final String cipherInstance, final int mode, final byte[] keyBytes,
			final byte[] messageBytes) throws GeneralSecurityException {

		final SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		byte[] transformedBytes = null;

		try {

			final Cipher cipher = Cipher.getInstance(cipherInstance);
			cipher.init(mode, keySpec);
			transformedBytes = cipher.doFinal(messageBytes);

		} catch (Exception e) {
			throw e;
		}
		return transformedBytes;
	}

	public byte[] toBytes(char[] chars) {
		CharBuffer charBuffer = CharBuffer.wrap(chars);
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
		byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}

	public byte[] mySecureHash(byte[] m) throws NoSuchAlgorithmException {
		byte[] dig = hashCrittSic.digest(m);
		Arrays.fill(m, (byte) 0); // wiper
		return Arrays.copyOf(dig, 16); // 128 bit
	}

	public byte[] mySecureRandom(int dim) {
		byte[] rand = new byte[dim];
		prng.nextBytes(rand);
		return rand;
	}
}
