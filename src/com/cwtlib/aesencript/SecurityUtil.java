package com.cwtlib.aesencript;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class SecurityUtil {

	private static byte[] keyValue;

	private static byte[] iv;

	private static SecretKey key;
	private static AlgorithmParameterSpec paramSpec;
	private static Cipher ecipher;

	private static String str = "com.knifeapp.knifevpn";

	static {
		System.loadLibrary("cwtlib");
		keyValue = getKeyValue();
		iv = getIv();
		for (int i = 0; i < keyValue.length; i++) {
			str += keyValue[i];
		}

		if (null != keyValue && null != iv) {
			KeyGenerator kgen;
			try {
				kgen = KeyGenerator.getInstance("AES");
				kgen.init(128, new SecureRandom(keyValue));
				key = kgen.generateKey();
				paramSpec = new IvParameterSpec(iv);
				ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			} catch (NoSuchAlgorithmException e) {
			} catch (NoSuchPaddingException e) {
			}
		}
	}

	public static native byte[] getKeyValue();

	public static native byte[] getIv();

	/**
	 * 加密
	 */
	public static String encode(String msg) {
		String str = "";
		try {
			// 用密钥和一组算法参数初始化此 cipher
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			// 加密并转换成16进制字符串
			str = asHex(ecipher.doFinal(msg.getBytes()));
		} catch (BadPaddingException e) {
		} catch (InvalidKeyException e) {
		} catch (InvalidAlgorithmParameterException e) {
		} catch (IllegalBlockSizeException e) {
		}
		return str;
	}

	/**
	 * 解密
	 */
	public static String decode(String value) {
		try {
			ecipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			return new String(ecipher.doFinal(asBin(value)));
		} catch (BadPaddingException e) {
		} catch (InvalidKeyException e) {
		} catch (InvalidAlgorithmParameterException e) {
		} catch (IllegalBlockSizeException e) {
		}
		return "";
	}

	private static String asHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;
		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)// 小于十前面补零
				strbuf.append("0");
			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}
		return strbuf.toString();
	}

	private static byte[] asBin(String src) {
		if (src.length() < 1)
			return null;
		byte[] encrypted = new byte[src.length() / 2];
		for (int i = 0; i < src.length() / 2; i++) {
			int high = Integer.parseInt(src.substring(i * 2, i * 2 + 1), 16);// 取高位字节
			int low = Integer.parseInt(src.substring(i * 2 + 1, i * 2 + 2), 16);// 取低位字节
			encrypted[i] = (byte) (high * 16 + low);
		}
		return encrypted;
	}

	public static String enCrypto(String txt) {
		try {
			String key = str;
			StringBuffer sb = new StringBuffer();
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
			SecretKeyFactory skeyFactory = null;
			Cipher cipher = null;
			try {
				skeyFactory = SecretKeyFactory.getInstance("DES");
				cipher = Cipher.getInstance("DES");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			SecretKey deskey = skeyFactory.generateSecret(desKeySpec);
			cipher.init(Cipher.ENCRYPT_MODE, deskey);
			byte[] cipherText = cipher.doFinal(txt.getBytes());
			for (int n = 0; n < cipherText.length; n++) {
				String stmp = (java.lang.Integer
						.toHexString(cipherText[n] & 0XFF));

				if (stmp.length() == 1) {
					sb.append("0" + stmp);
				} else {
					sb.append(stmp);
				}
			}
			return sb.toString().toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage() + "";
		}
	}

	public static String deCrypto(String txt) {
		try {
			String key = str;
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
			SecretKeyFactory skeyFactory = null;
			Cipher cipher = null;
			try {
				skeyFactory = SecretKeyFactory.getInstance("DES");
				cipher = Cipher.getInstance("DES");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			SecretKey deskey = skeyFactory.generateSecret(desKeySpec);
			cipher.init(Cipher.DECRYPT_MODE, deskey);
			byte[] btxts = new byte[txt.length() / 2];
			for (int i = 0, count = txt.length(); i < count; i += 2) {
				btxts[i / 2] = (byte) Integer.parseInt(txt.substring(i, i + 2),
						16);
			}
			return (new String(cipher.doFinal(btxts)));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage() + "";
		}
	}

}
