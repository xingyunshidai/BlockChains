package com.xingyun.dht.block.intf;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface IBlockHead extends IStreamFormat{
	
	int BLOCK_VERSION=1;

	byte[] getBlockHash() throws NoSuchAlgorithmException, IOException;
}
