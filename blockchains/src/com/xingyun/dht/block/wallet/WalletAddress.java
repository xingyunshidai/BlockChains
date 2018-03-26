package com.xingyun.dht.block.wallet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

/**
 * 钱包地址
 * 
 * @author nibaogang
 *
 */
public class WalletAddress {
	private static final byte ADDRESS_VER=1;
	private static final byte[] ADDRESS_TAG="DHT".getBytes();
	private final byte[] pubHash;
	private WalletKey walletKey;
	private static String priPath="pri.der";
	
	public WalletAddress(byte[] address){
		this(address, null);
	}
	
	public WalletAddress(byte[] pubHash,WalletKey walletKey){
		if(pubHash==null||pubHash.length!=20){
			throw new RuntimeException("address_error");
		}
		this.pubHash=pubHash;
		this.walletKey=walletKey;
	}

	/**
	 * 创建一个钱包地址
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeySpecException 
	 * @throws IOException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws NoSuchProviderException 
	 */
	public static WalletAddress createAddress(String password) throws Exception {
		ECKey ecKey=ECKey.fromRandom();

		byte[] priData = ecKey.getPrivKeyBytes();
		printByte("create priData", priData);
		WalletKey walletKey=new WalletKey(priData);
		byte[] encryptData=walletKey.encrypt(password);
		printByte("write encrypt", encryptData);
		writeFile(priPath, encryptData);

		// 获取公钥
		byte[] pubData=ecKey.getPubKey().getEncoded(true);
		System.out.println(byte2Hex(pubData)+"  len:"+pubData.length);
        
		return loadAddressFromEncryptData(password,encryptData);
	}
	
	public static WalletAddress loadFromPrivFile(String password) throws Exception{
		byte[] encryptData=readFile(priPath);
		printByte("read encryptData", encryptData);
		return loadAddressFromEncryptData(password,encryptData);
	}
	
	public static WalletAddress loadAddressFromEncryptData(String password,byte[] priData) throws Exception{
		WalletKey walletKey=WalletKey.decrypt(password, priData);
		
		byte[] pubData=walletKey.getPubData();
		printByte("pubData", pubData);
		byte[] pub256 = getSHA256(pubData,0,pubData.length);
		printByte("pub256", pub256);
		byte[] pub160 = getRipeMD160(pub256);
		printByte("pub160", pub160);
		
		return new WalletAddress(pub160,walletKey);
	}
	
	public static WalletAddress loadAddressFromAddress(String address) throws NoSuchAlgorithmException{
		byte[] addressBytes=new BigInteger(address, 16).toByteArray();
		printByte("addressBytes", addressBytes);
		if(addressBytes.length!=28){
			throw new RuntimeException("address_length_error");
		}
		if(addressBytes[0]!=ADDRESS_VER){
			throw new RuntimeException("address_version_error");
		}
		if(addressBytes[21]!=ADDRESS_TAG[0]||addressBytes[22]!=ADDRESS_TAG[1]||addressBytes[23]!=ADDRESS_TAG[2]){
			throw new RuntimeException("address_tag_error");
		}
		byte[] sign=getSHA256(addressBytes, 0, 21);
		if(addressBytes[24]!=sign[0]||addressBytes[25]!=sign[1]||addressBytes[26]!=sign[2]||addressBytes[27]!=sign[3]){
			throw new RuntimeException("address_sign_error");
		}
	
		byte[] pubHash=Arrays.copyOfRange(addressBytes, 1, 21);
		return new WalletAddress(pubHash);
	}
	
	public String formatAddress(){
		byte[] addressBytes=new byte[28];
		addressBytes[0]=ADDRESS_VER;
		addressBytes[21]=ADDRESS_TAG[0];
		addressBytes[22]=ADDRESS_TAG[1];
		addressBytes[23]=ADDRESS_TAG[2];
		System.arraycopy(pubHash, 0, addressBytes, 1, pubHash.length);
		byte[] sign=getSHA256(addressBytes,0,21);
		System.arraycopy(sign, 0, addressBytes, 24, 4);
		return byte2Hex(addressBytes);
	}
	
	public byte[] getPubHash(){
		return this.pubHash;
	}
	
	public boolean hashWalletKey(){
		return this.walletKey!=null;
	}
	
	public WalletKey getWalletKey(){
		return this.walletKey;
	}
	
	private static byte[] readFile(String path) throws IOException{
		File file=new File(path);
		int length=(int)file.length();
		byte[] bs=new byte[length];
		DataInputStream dis=new DataInputStream(new FileInputStream(file));
		dis.readFully(bs);
		dis.close();
		return bs;
	}
	
	private static void writeFile(String path,byte[] data) throws IOException{
		FileOutputStream fos=new FileOutputStream(path);
		fos.write(data);
		fos.flush();
		fos.close();
	}

	public static byte[] getRipeMD160(byte[] input) throws NoSuchAlgorithmException {
		RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(input, 0, input.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
	}

	/**
	 * 利用java原生的摘要实现SHA256加密
	 * 
	 * @return
	 */
	public static byte[] getSHA256(byte[] input, int offset, int len){
		try{
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(input, offset, len);
		return messageDigest.digest();
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException("not_support_SHA-256");
		}
	}

	/**
	 * 将byte转为16进制
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byte2Hex(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length() == 1) {
				// 1得到一位的进行补0操作
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}
	
	public static void printByte(String tag,byte[] data){
		System.out.println(tag+":"+byte2Hex(data)+"  len:"+data.length);
	}
}
