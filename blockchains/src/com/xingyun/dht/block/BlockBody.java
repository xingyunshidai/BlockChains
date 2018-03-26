package com.xingyun.dht.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.xingyun.dht.block.intf.IBodySteam;
import com.xingyun.dht.block.wallet.WalletAddress;

public class BlockBody implements IBodySteam{
	private final Transaction[] transactions;
	
	protected byte[] getMerkleRootHash() throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		for(int i=0;i<transactions.length;i++){
			baos.write(transactions[0].getTransactionHash());
		}
		byte[] data=baos.toByteArray();
		if(data.length!=transactions.length*32){
			throw new RuntimeException("data_length_error");
		}
		return WalletAddress.getSHA256(data, 0, data.length);
	}
	
	public BlockBody(Transaction[] transactions){
		this.transactions=transactions;
	}
	
	public BlockBody(byte[] data) throws IOException {
		DataInputStream dis=new DataInputStream(new ByteArrayInputStream(data));
		int transactionsCounter=dis.readInt();
		transactions=new Transaction[transactionsCounter];
		for(int i=0;i<transactionsCounter;i++){
			transactions[i]=new Transaction(dis);
		}
	}

	@Override
	public byte[] format() throws IOException {
		if(transactions!=null){
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			DataOutputStream dos=new DataOutputStream(baos);
			dos.writeInt(transactions.length);
			for(int i=0;i<transactions.length;i++){
				transactions[i].format(dos);
			}
			return baos.toByteArray();
		}else {
			throw new RuntimeException("transactions_error");
		}
	}
}
