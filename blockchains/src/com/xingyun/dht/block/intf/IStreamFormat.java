package com.xingyun.dht.block.intf;

import java.io.DataOutputStream;
import java.io.IOException;

public interface IStreamFormat {
	void format(DataOutputStream dos) throws IOException;
}
