package com.xingyun.dht.block.wallet;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;

public class ECKey {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static ECDomainParameters CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
            CURVE_PARAMS.getH());
    private static final SecureRandom secureRandom = new SecureRandom();
    
    protected final BigInteger priv;
    protected final ECPoint pub;
    
    private ECKey(SecureRandom secureRandom) {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        priv = privParams.getD();
        pub = pubParams.getQ();
    }
    
    private ECKey(BigInteger privKey,ECPoint ecPoint){
    	this.priv=privKey;
    	this.pub=ecPoint;
    }
    
    public static ECKey fromPrivate(byte[] priData) {
    	BigInteger privKey=new BigInteger(1,priData);
        ECPoint point = publicPointFromPrivate(privKey);
        return new ECKey(privKey, point);
    }
    
    public static ECKey fromRandom(){
    	return new ECKey(ECKey.secureRandom);
    }
    
    public byte[] getPrivKeyBytes() {
        return bigIntegerToBytes(priv, 32);
    }
    
    public ECPoint getPubKey() {
        return pub;
    }
    
    private static ECPoint publicPointFromPrivate(BigInteger privKey) {
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }
    
    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }
    
	public static byte[] doSign(byte[] input, BigInteger privateKeyForSigning) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input);
        WalletAddress.printByte("sign0", components[0].toByteArray());
        WalletAddress.printByte("sign1", components[1].toByteArray());
        byte[] sign=new byte[64];
        byte[] r=bigIntegerToBytes(components[0], 32);
        System.arraycopy(r, 0, sign, 0, 32);
        byte[] s=bigIntegerToBytes(components[1], 32);
        System.arraycopy(s, 0, sign, 32, 32);
        return sign;
    }
	
	public static boolean verify(byte[] input, byte[] sign, byte[] pub) {
		if(sign==null||sign.length!=64){
			throw new RuntimeException("sign_error");
		}
		byte[] r=Arrays.copyOfRange(sign, 0, 32);
		byte[] s=Arrays.copyOfRange(sign, 32, 64);
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        
        return signer.verifySignature(input, new BigInteger(1, r), new BigInteger(1,s));
    }
}
