package com.xingyun.dht.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.xingyun.dht.block.intf.ITransaction;

public class Transaction implements ITransaction{
	//锁定时间
	private final long lockTime;
	//序号
	private final int sequenceNumber;
	//输入
	private final TransactionInput[] transactionInputs;
	//输出
	private final TransactionOutput[] transactionOutputs;
	
	public Transaction(TransactionInput[] transactionInputs,TransactionOutput[] transactionOutputs,long lockTime) {
		this.transactionInputs=transactionInputs;
		this.transactionOutputs=transactionOutputs;
		this.lockTime=lockTime;
		this.sequenceNumber=Integer.MAX_VALUE;
	}
	
	public Transaction(DataInputStream dis) throws IOException {
		int version=dis.readInt();
		if(version!=TRANSACTION_VERSION){
			throw new RuntimeException("transaction_version_error");
		}
		
		this.lockTime=dis.readLong();
		this.sequenceNumber=dis.readInt();
		
		int inputCount=dis.readInt();
		transactionInputs=new TransactionInput[inputCount];
		for(int i=0;i<inputCount;i++){
			transactionInputs[i]=TransactionInput.parserTransactionInput(dis);
		}
		
		int outputCount=dis.readInt();
		transactionOutputs=new TransactionOutput[outputCount];
		for(int i=0;i<outputCount;i++){
			transactionOutputs[i]=new TransactionOutput(dis);
		}
	}

	@Override
	public void format(DataOutputStream dos) throws IOException {
		dos.writeInt(ITransaction.TRANSACTION_VERSION);
		dos.writeLong(this.lockTime);
		dos.writeInt(this.sequenceNumber);
		if(transactionInputs!=null){
			dos.writeInt(transactionInputs.length);
			for(int i=0;i<transactionInputs.length;i++){
				transactionInputs[i].format(dos);
			}
		}else {
			throw new RuntimeException("inputs_error");
		}
		if(transactionOutputs!=null){
			dos.writeInt(transactionOutputs.length);
			for(int i=0;i<transactionOutputs.length;i++){
				transactionOutputs[i].format(dos);
			}
		}else {
			throw new RuntimeException("outputs_error");
		}
	}

	public byte[] getTransactionHash() {
		return new byte[32];
	}
	
	
}
