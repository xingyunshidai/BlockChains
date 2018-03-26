package com.xingyun.dht.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.xingyun.dht.block.intf.ITransaction;

public class TransactionInputReward extends TransactionInput{
	//metadata_hash(20byte)
	private final byte[] h1;
	//prices_hash(20byte)
	private final byte[] h2;
	
	public TransactionInputReward(DataInputStream dis) throws IOException{
		super(ITransaction.TRANSACTION_TYPE_REWARD);
		byte[] h1=new byte[20];
		dis.readFully(h1);
		this.h1=h1;
		byte[] h2=new byte[20];
		dis.readFully(h2);
		this.h2=h2;
	}
	
	/**
	 * 奖励收入
	 */
	public TransactionInputReward(byte[] h1,byte[] h2) {
		super(ITransaction.TRANSACTION_TYPE_REWARD);
		if(h1==null||h1.length!=20){
			throw new RuntimeException("h1_error");
		}
		if(h2==null||h2.length!=20){
			throw new RuntimeException("h2_error");
		}
		this.h1=h1;
		this.h2=h2;
	}

	@Override
	protected void doFormat(DataOutputStream dos) throws IOException {
		dos.write(h1);
		dos.write(h2);
	}
}
