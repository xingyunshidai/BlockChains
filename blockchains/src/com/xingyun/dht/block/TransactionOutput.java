package com.xingyun.dht.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.xingyun.dht.block.intf.ITransaction;
import com.xingyun.dht.block.intf.ITransactionOutput;

public class TransactionOutput implements ITransactionOutput{
	//交易金额(8byte)
	private final long amount;
	//地址类型
	private final byte addressType;
	//输出地址(20byte)
	private final byte[] address;
	
	protected TransactionOutput(DataInputStream dis) throws IOException{
		this.amount=dis.readLong();
		this.addressType=dis.readByte();
		byte[] address=new byte[20];
		dis.readFully(address);
		this.address=address;
	}
	
	public TransactionOutput(long amount,byte[] address) {
		if(address==null||address.length!=20){
			throw new RuntimeException("address_error");
		}
		if(amount<=0){
			throw new RuntimeException("amount_error");
		}
		this.amount=amount;
		this.addressType=ITransaction.ADDRESS_TYPE_NORMAL;
		this.address=address;
	}

	@Override
	public void format(DataOutputStream dos) throws IOException {
		dos.writeLong(amount);
		dos.writeByte(addressType);
		dos.write(address);
	}
}
