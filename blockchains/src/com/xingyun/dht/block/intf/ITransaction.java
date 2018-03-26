package com.xingyun.dht.block.intf;

public interface ITransaction extends IStreamFormat{
	int TRANSACTION_VERSION=1;
	/**
	 * 地址类型
	 */
	byte ADDRESS_TYPE_NORMAL=1;
	/**
	 * 系统奖励
	 */
	byte TRANSACTION_TYPE_REWARD=1;
	/**
	 * 财富转移
	 */
	byte TRANSACTION_TYPE_TRANSFER=2;
}
