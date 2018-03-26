package com.xingyun.dht.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.xingyun.dht.block.intf.ITransaction;
import com.xingyun.dht.block.intf.ITransactionInput;

public abstract class TransactionInput implements ITransactionInput{
	//输入单类型
	private final byte transactionType;
	
	protected TransactionInput(byte transactionType){
		this.transactionType=transactionType;
	}
	
	public static TransactionInput parserTransactionInput(DataInputStream dis) throws IOException{
		byte transactionType=dis.readByte();
		if(transactionType==ITransaction.TRANSACTION_TYPE_REWARD){
			return new TransactionInputReward(dis);
		}else if(transactionType==ITransaction.TRANSACTION_TYPE_TRANSFER){
			return new TransactionInputTransfer(dis);
		}else {
			throw new RuntimeException("transaction_type_error");
		}
	}

	@Override
	public void format(DataOutputStream dos) throws IOException {
		dos.writeByte(this.transactionType);
		doFormat(dos);
	}
	
	protected abstract void doFormat(DataOutputStream dos) throws IOException;
}
