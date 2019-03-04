package com.drone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Main class�� Thread ������ ���� class
public class AcceptServer extends Thread {
	private ServerSocket serverSocket = null;
	private static final int SERVER_PORT = 8000;
	private boolean serverFlag = true;
	
	/**
	 * ���� ���� ����
	 * @exception IOException
	 */
	public void closeServer() {
		serverFlag = false;		
		try {
			serverSocket.close();
		}catch(IOException e) {
			e.printStackTrace();
		}		
		System.out.println("������ ����Ǿ����ϴ�.");
		System.exit(0);
	}
	
	/**
	 * ���� ���� ����
	 * @exception IOException
	 */
	public boolean startServer() {
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		}catch(IOException e) {
			return false;
		}		
		super.start();
		return true;
	}
	
	/**
	 * RuntimeException ���� ó��
	 * @param args
	 */
	@Override
	public void start() {
		throw new RuntimeException("Don't call the start()");
	}
	
	/**
	 * Thread�� ������ ���� run �Լ� - (Sensor -> Drone) �޽��� ����
	 * @exception IOException
	 */	
	public void run() {
		while(true) {
			try {
				System.out.println("���ο� Ŭ���̾�Ʈ�� ������ ��ٸ��ϴ�.");
				Socket socket = serverSocket.accept();				
				int ran = -1;				
				
				do{
					ran = (int)(Math.random() * ServerList.SERVER_MAX);
				}while(!ServerList.serverList[ran].isIdle());				
				System.out.println("Ŭ���̾�Ʈ (" + socket.getInetAddress().getHostAddress() + ") �� �����Ͽ����ϴ�.");

				//���� �ֱ� ���� ������ ����
				BufferedReader in = new BufferedReader(new FileReader("/home/pi/data/period.txt"));				
				String read = "";				
				while ((read = in.readLine()) != null) { 
					break;
				}			
				
				//AP ��Ž�� �ֱ� ���� ������ ����
				BufferedReader in2 = new BufferedReader(new FileReader("/home/pi/data/wifi_period.txt"));				
				String read2 = "";				
				while ((read2 = in2.readLine()) != null) {
					break;
				}		
				
				ServerList.serverList[ran].setServer(ran, socket, read + "\t" + read2);
				ServerList.serverList[ran].startServer();
			}catch(IOException e) {
				break;
			}
		}
	}
}