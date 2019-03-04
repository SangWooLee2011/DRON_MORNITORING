package com.drone;

//ServerList class
public class ServerList {
	public static final int SERVER_MAX = 50;
	public static Server[] serverList = new Server[SERVER_MAX];
	static {
		for(int i = 0; i < serverList.length; i++)
			serverList[i] = new Server();
	}
}