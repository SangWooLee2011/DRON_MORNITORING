package com.drone;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


//Drone Main class
public class Main {
	public static void main(String[] args) throws Exception {
		File data_folder = new File("./data");
		File period_folder = new File("./period");
		
		if(!data_folder.exists())
		{
			data_folder.mkdirs();
		}
		
		if(!period_folder.exists())
		{
			period_folder.mkdirs();
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		AcceptServer accept = new AcceptServer();		
		
		if (accept.startServer()) {			
		}
	}
}