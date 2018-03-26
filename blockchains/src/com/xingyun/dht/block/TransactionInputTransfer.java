package com.xingyun.dht.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.xingyun.dht.block.intf.ITransaction;

public class TransactionInputTransfer extends TransactionInput{
	//引用输出交易单(32byte)
	private final byte[] previousTransactionHash;
	//引用输出索引号(4byte)
	private final int previousOutputIndex;
	//签名(64byte)
	private byte[] signature;
	//公钥(33byte)
	private byte[] pubKey;
	
	public TransactionInputTransfer(DataInputStream dis) throws IOException{
		super(ITransaction.TRANSACTION_TYPE_TRANSFER);
		byte[] previousTransactionHash=new byte[32];
		dis.readFully(previousTransactionHash);
		this.previousTransactionHash=previousTransactionHash;
		this.previousOutputIndex=dis.readInt();
		byte[] signature=new byte[64];
		dis.readFully(signature);
		this.signature=signature;
		byte[] pubKey=new byte[33];
		dis.readFully(pubKey);
		this.pubKey=pubKey;
	}
	
	/**
	 * 交易
	 * @param transaction
	 * @param outIndex
	 */
	public TransactionInputTransfer(Transaction transaction,int outIndex) {
		super(ITransaction.TRANSACTION_TYPE_TRANSFER);
		this.previousTransactionHash=transaction.getTransactionHash();
		this.previousOutputIndex=outIndex;
	}
	
	@Override
	protected void doFormat(DataOutputStream dos) throws IOException{
		dos.write(this.previousOutputIndex);
		dos.writeInt(this.previousOutputIndex);
		dos.write(this.signature);
		dos.write(this.pubKey);
	}
}
