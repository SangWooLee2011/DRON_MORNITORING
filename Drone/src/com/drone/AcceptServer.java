package com.drone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Main class의 Thread 동작을 위한 class
public class AcceptServer extends Thread {
	private ServerSocket serverSocket = null;
	private static final int SERVER_PORT = 8000;
	private boolean serverFlag = true;
	
	/**
	 * 서버 연결 종료
	 * @exception IOException
	 */
	public void closeServer() {
		serverFlag = false;		
		try {
			serverSocket.close();
		}catch(IOException e) {
			e.printStackTrace();
		}		
		System.out.println("서버가 종료되었습니다.");
		System.exit(0);
	}
	
	/**
	 * 서버 연결 시작
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
	 * RuntimeException 예외 처리
	 * @param args
	 */
	@Override
	public void start() {
		throw new RuntimeException("Don't call the start()");
	}
	
	/**
	 * Thread의 동작을 위한 run 함수 - (Sensor -> Drone) 메시지 수신
	 * @exception IOException
	 */	
	public void run() {
		while(true) {
			try {
				System.out.println("새로운 클라이언트의 접속을 기다립니다.");
				Socket socket = serverSocket.accept();				
				int ran = -1;				
				
				do{
					ran = (int)(Math.random() * ServerList.SERVER_MAX);
				}while(!ServerList.serverList[ran].isIdle());				
				System.out.println("클라이언트 (" + socket.getInetAddress().getHostAddress() + ") 가 접속하였습니다.");

				//전송 주기 설정 파일을 읽음
				BufferedReader in = new BufferedReader(new FileReader("/home/pi/data/period.txt"));				
				String read = "";				
				while ((read = in.readLine()) != null) { 
					break;
				}			
				
				//AP 재탐색 주기 설정 파일을 읽음
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