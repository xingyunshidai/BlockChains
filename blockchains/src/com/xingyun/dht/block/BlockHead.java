package com.xingyun.dht.block;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.xingyun.dht.block.intf.IBlockHead;
import com.xingyun.dht.block.wallet.WalletAddress;

/**
 * 区块头(80byte)
 * @author nibaogang
 *
 */
public class BlockHead implements IBlockHead{
	//版本(4byte)
	private int version;
	//块序号(8byte)
	private final long blockIndex;
	//前一块头哈希(32byte)
	private final byte[] prevBlockHash;
	//体的哈希(32byte)
	private final byte[] merkleRootHash;
	//时间戳(8byte)
	private final long time;
	
	protected BlockHead(BlockBody currentBlockBody) throws NoSuchAlgorithmException, IOException{
		this.blockIndex=0;
		this.prevBlockHash=new byte[32];
		this.merkleRootHash=currentBlockBody.getMerkleRootHash();
		this.time=System.currentTimeMillis();
	}
	
	protected BlockHead(BlockHead lastBlockHead,BlockBody currentBlockBody) throws IOException {
		this.blockIndex=lastBlockHead.getBlockIndex()+1;
		this.prevBlockHash=lastBlockHead.getBlockHash();
		this.merkleRootHash=currentBlockBody.getMerkleRootHash();
		this.time=System.currentTimeMillis();
	}
	
	public BlockHead(DataInputStream dis) throws IOException {
		this.version=dis.readInt();
		this.blockIndex=dis.readLong();
		this.prevBlockHash=new byte[32];
		dis.readFully(this.prevBlockHash);
		this.merkleRootHash=new byte[32];
		dis.readFully(this.merkleRootHash);
		this.time=dis.readLong();
	}
	
	@Override
	public void format(DataOutputStream dos) throws IOException {
		dos.writeInt(BLOCK_VERSION);
		dos.writeLong(blockIndex);
		dos.write(prevBlockHash);
		dos.write(merkleRootHash);
		dos.writeLong(time);
	}
	
	private byte[] getHeadBytes() throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		format(new DataOutputStream(baos));
		return baos.toByteArray();
	}
	
	@Override
	public byte[] getBlockHash() throws IOException{
		byte[] headBytes=getHeadBytes();
		return WalletAddress.getSHA256(headBytes, 0, headBytes.length);
	}
	
	public long getBlockIndex(){
		return this.blockIndex;
	}
	
	public byte[] getPrevBlockHash(){
		return this.prevBlockHash;
	}

	public byte[] getMerkleRootHash() {
		return this.merkleRootHash;
	}
}
