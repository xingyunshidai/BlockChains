package com.xingyun.dht.block;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BlockChain {
	private final boolean readonly;
	private int currentIndex=-1;
	private File currentFile;
	private Block currentBlock;
	private long currentLength;
	
	private long currentBlockIndex=-1;
	
	private static BlockChain blockChain;

	private BlockChain(boolean readonly) {
		this.readonly=readonly;
	}
	
	public static synchronized BlockChain loadBlockChainFromPersist(boolean readonly){
		if(blockChain==null){
			synchronized (BlockChain.class) {				
				if(blockChain==null){
					blockChain=new BlockChain(readonly);
				}
			}
		}
		return BlockChain.blockChain;
	}
	
	public Block loadFromDir(String dir) throws IOException{
		if(this.currentIndex!=-1){
			throw new RuntimeException("current_index_error");
		}
		while (true) {
			int loadIndex=currentIndex+1;
			File dataFile=new File(dir+formatDataFileName(loadIndex));
			if(dataFile.exists()){
				Block lastBlock=loadDataFile(dataFile);
				System.out.println("last Block:"+lastBlock+" dataFile:"+dataFile+" length:"+dataFile.length());
				this.currentBlock=lastBlock;
				this.currentIndex=loadIndex;
				this.currentFile=dataFile;
				this.currentLength=dataFile.length();
				if(lastBlock!=null){
					System.out.println("load index:"+loadIndex+" blockIndex:"+lastBlock.getBlockHead().getBlockIndex());
				}
			}else {
				break;
			}
		}
		System.out.println("currentBlock:"+currentBlock);
		if(this.currentBlock==null){
			this.currentIndex=0;
			File dataFile=new File(dir+formatDataFileName(currentIndex));
			dataFile.getParentFile().mkdirs();
			this.currentFile=dataFile;
			this.currentLength=0;
		}else {
			System.out.println("loadFromDir currentIndex:"+currentIndex+"  blockIndex:"+currentBlock.getBlockHead().getBlockIndex());
		}
		return currentBlock;
	}
	
	private void checkNexFile(){
		if(currentLength>5*1024*1024){
			currentIndex++;
			currentLength=0;
			currentFile=new File(formatDataFileName(currentIndex));
		}
	}
	
	private Block loadDataFile(File dataFile) throws IOException{
		DataInputStream dis=new DataInputStream(new FileInputStream(dataFile));
		try{
			Block lastBlock=null;
			while(true){
				Block tempBlock=Block.parserBlock(dis);
				if(tempBlock!=null){
					long tempBlockIndex=tempBlock.getBlockHead().getBlockIndex();
					if(tempBlockIndex!=currentBlockIndex+1){
						throw new RuntimeException("blockIndex_error:"+tempBlockIndex);
					}
					this.currentBlockIndex++;
					lastBlock=tempBlock;
				}else {
					break;
				}
			}
			return lastBlock;
		}finally{
			dis.close();
		}
	}
	
	public synchronized boolean writeBlock(BlockBody blockBody){
		if(readonly){
			throw new RuntimeException("blockChain_readonly");
		}
		try{
			checkNexFile();
			Block writeBlock;
			if(currentBlock==null){
				writeBlock=new Block(blockBody);
			}else {
				writeBlock=new Block(currentBlock, blockBody);
			}
			System.out.println("write blockIndex:"+writeBlock.getBlockHead().getBlockIndex());
			byte[] blockData=writeBlock.formatBytes();
			checkBlock(blockData);
			
			System.out.println("current File:"+currentFile+" current Length:"+currentLength);
			if(this.currentFile.length()!=this.currentLength){
				throw new RuntimeException("file_length_error");
			}

			DataOutputStream currentOutStream=new DataOutputStream(new FileOutputStream(currentFile,true));
			currentOutStream.write(blockData);
			currentOutStream.flush();
			currentOutStream.close();
			
			this.currentLength+=blockData.length;
			this.currentBlock=writeBlock;
			
			if(this.currentFile.length()!=this.currentLength){
				throw new RuntimeException("file_length_error");
			}
				
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	private String formatDataFileName(int index){
		String a = String.format("%03d", index/1000000);
		String b = a+"-"+String.format("%03d", index/1000);
		String c = b+"-"+String.format("%03d", index);
		return "blocks/"+a+"/"+b+"/dht-"+c+".blk";
	}
	
	private void checkBlock(byte[] blockData) throws IOException, NoSuchAlgorithmException{
		DataInputStream dis=new DataInputStream(new ByteArrayInputStream(blockData));
		Block tempBlock=Block.parserBlock(dis);
		byte[] prevBlockHash=tempBlock.getBlockHead().getPrevBlockHash();
		if(currentBlock==null){
			if(tempBlock.getBlockHead().getBlockIndex()!=0){
				throw new RuntimeException("block_index_error");
			}
		}else {			
			byte[] currentBlockHash=currentBlock.getBlockHead().getBlockHash();
			if(!Arrays.equals(prevBlockHash, currentBlockHash)){
				throw new RuntimeException("prevBlockHash_error");
			}
		}
	}
	
	public Block getCurrentBlock(){
		return this.currentBlock;
	}
}
