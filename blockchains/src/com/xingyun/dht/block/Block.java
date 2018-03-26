package com.xingyun.dht.block;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.xingyun.dht.block.intf.IBlockSteam;

public class Block implements IBlockSteam{
	//魔法数字，用来作为区块间的分隔符(4byte)
	private static final int MAGIC=0x780CE9DA;
	private int HEAD_LENGTH=80;
	//区块大小(4byte)
	private int length;
	//区块头(80byte)
	private final BlockHead blockHead;
	//区块体(无固定大小)
	private final BlockBody blockBody;
	
	/**
	 * 创世区块
	 * @param blockBody
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@Deprecated
	public Block(BlockBody blockBody) throws NoSuchAlgorithmException, IOException{
		this.blockHead=new BlockHead(blockBody);
		this.blockBody=blockBody;
	}
	
	/**
	 * 新区块
	 * @param lastBlock
	 * @param blockBody
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public Block(Block lastBlock,BlockBody blockBody) throws IOException {
		this.blockHead=new BlockHead(lastBlock.getBlockHead(),blockBody);
		this.blockBody=blockBody;
	}
	
	private Block(DataInputStream dis) throws IOException {
		int magic=dis.readInt();
		if(magic!=MAGIC){
			throw new IOException("magic_error");
		}
		int length=dis.readInt();
		int bodyLength=length-HEAD_LENGTH;
		if(bodyLength<0){
			throw new IOException("length_error");
		}
		this.length=length;
		this.blockHead=new BlockHead(dis);
		byte[] bodyBytes=new byte[bodyLength];
		dis.readFully(bodyBytes);
		this.blockBody=new BlockBody(bodyBytes);
		
		byte[] merkleRootHash1=this.blockHead.getMerkleRootHash();
		byte[] merkleRootHash2=this.blockBody.getMerkleRootHash();
		if(!Arrays.equals(merkleRootHash1, merkleRootHash2)){
			throw new RuntimeException("merkleRootHash_check_error");
		}
	}
	
	public static Block parserBlock(DataInputStream dis) throws IOException{
		if(dis.available()>0){
			return new Block(dis);
		}else {
			return null;
		}
	}
	
	@Override
	public void format(DataOutputStream dos) throws IOException{
		dos.writeInt(MAGIC);
		byte[] bodyBytes=this.blockBody.format();
		int length=bodyBytes.length+HEAD_LENGTH;
		dos.writeInt(length);
		this.blockHead.format(dos);
		dos.write(bodyBytes);
		dos.flush();
	}
	
	public byte[] formatBytes() throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		DataOutputStream dos=new DataOutputStream(baos);
		format(dos);
		return baos.toByteArray();
	}
	
	public BlockHead getBlockHead(){
		return this.blockHead;
	}
}
