package com.xingyun.dht.block;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AliTest {
	public static void main(String[] args) {
		for (int i = 1000; i < 65536; i++) {
			final int port=i;
			new Thread(){
				public void run() {
					Socket socket=new Socket();
					try {
						if(port%1000==0){
							System.out.print(port+"|");
						}
						socket.connect(new InetSocketAddress("110.75.138.10", port), 500);
						System.out.println();
						System.out.println("port:"+port);
					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
					}
			
				};
			}.start();

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
