package com.xingyun.dht.block.wallet;


import java.math.BigInteger;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 钱包钥匙
 * @author nibaogang
 *
 */
public class WalletKey {
	private static final String SALT_1="zhongguogongchandangwansui";
	private static final String SALT_2="zhonghuarenmingongheguowansui";
	private static final String CBC_MODE="AES/CBC/NoPadding";
//	private static final String CBC_MODE="AES/CBC/PKCS5Padding";
	private final byte[] priData;
	private final byte[] pubData;
	
	public WalletKey(byte[] priData){
		this.priData=priData;
        ECKey ecKey=ECKey.fromPrivate(priData);
		// 获取公钥
		byte[] pubData=ecKey.getPubKey().getEncoded(true);
		this.pubData=pubData;
	}
	
	public byte[] encrypt(String password) throws Exception{
		byte[] pwdBytes=(SALT_1+password+SALT_2).getBytes();
		Cipher cipher = Cipher.getInstance(CBC_MODE);
        byte[] hash = WalletAddress.getSHA256(pwdBytes, 0, pwdBytes.length);
        byte[] key=Arrays.copyOfRange(hash, 0, 16);
        byte[] ivBytes=Arrays.copyOfRange(hash, 16, 32);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(priData);
        return encrypted;
	}
	
	public static WalletKey decrypt(String password,byte[] encryptData) throws Exception{
		byte[] pwdBytes=(SALT_1+password+SALT_2).getBytes();
		Cipher cipher = Cipher.getInstance(CBC_MODE);
        byte[] hash = WalletAddress.getSHA256(pwdBytes, 0, pwdBytes.length);
        byte[] key=Arrays.copyOfRange(hash, 0, 16);
        byte[] ivBytes=Arrays.copyOfRange(hash, 16, 32);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] priData = cipher.doFinal(encryptData);
        return new WalletKey(priData);
	}
	
	public byte[] getPubData(){
		return this.pubData;
	}
	
	public byte[] doSign(byte[] input) {
		return ECKey.doSign(input, new BigInteger(1,priData));
	}
	
	public boolean verify(byte[] input,byte[] sign){
		return ECKey.verify(input, sign, pubData);
	}
}
