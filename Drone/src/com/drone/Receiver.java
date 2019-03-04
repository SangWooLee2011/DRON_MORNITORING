package com.drone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

//Drone Server class
public class Receiver extends Thread {
	private boolean threadFlag = true;
	private int client_id = -1;
	private BufferedReader in = null;
	private BufferedInputStream up;
	private PrintWriter fileOut = null;
	private String sendingMsg;

	/**
	 * Receiver 생성자 - (Gateway -> Drone) 메시지 수신을 위한 InputStream 생성
	 * @param is InputStream
	 * @param client_id client id
	 */	
	public Receiver(InputStream is, int client_id) {
		in = new BufferedReader(new InputStreamReader(is));
		this.client_id = client_id;
		up = new BufferedInputStream(is);
	}

	/**
	 * (Gateway -> Drone) 메시지 수신을 위한 InputStream 종료
	 * @exception IOException
	 */
	public void close() {
		try {
			if(in != null)
				in.close();
			in = null;
		}catch(IOException e) {
		}
		threadFlag = false;
		client_id = -1;
	}

	/**
	 * Thread의 동작을 위한 run 함수 - (Gateway -> Drone) 메시지 수신
	 * @exception IOException
	 */
	public void run()
	{
		while(threadFlag)
		{
			try
			{
				//String msg = in.readLine();
				String msg = "";
				String inputData = "";
				
				//(Gateway -> Drone) 메시지 수신
				while( (msg = in.readLine()) != null ){
					inputData += msg+"\t";
					//System.out.println(msg);
					if(msg.equals("EOF")){
						break;
					}
				}
				if(msg == null){
					break;
				}
				inputData = inputData.trim();
				//System.out.println("input: "+inputData);
				String tempData[] = inputData.split("\t");
				String filePath = "";
				if(tempData[0].equals("/sendFile")){ //(Gateway -> Drone) /sendFile 메시지 수신
					System.out.println("Sensors or Actuators to Drone");
					//tempData[1] -> 경로+파일
					for(int i=1; i<tempData.length; i++){
						//System.out.println("file: "+tempData[i]);
						if(tempData[i].matches(".*/filePath.*")){
							filePath = tempData[i].split(" ")[1];
							System.out.println(filePath+" 수신 시작...");
							//파일 초기화
							fileOut = new PrintWriter(new FileWriter(filePath));
							fileOut.close();
							//
							fileOut = new PrintWriter(new FileWriter(filePath,true));

						}else{
							if(tempData[i].equals("EOF")){
								fileOut.flush();
								fileOut.close();
								System.out.println(filePath+" 수신 완료...");
								ServerList.serverList[client_id].sendMessage("NEXT"); //(Drone -> Sensor) 메시지 전송

							}else{
								fileOut.println(tempData[i]); //(Gateway -> Drone) 파일 전송
							}
						}
					}
				}else if(tempData[0].equals("/getFile")){ //(Gateway -> Drone) /getFile 메시지 수신
					System.out.println("Drone to Gateway");
					String root = "/home/pi/data/";
					File dir = new File(root);
					File[] fileList = dir.listFiles();
					System.out.println("전송시도");

					try{						
						for(int i=0; i<fileList.length; i++){
							File file = fileList[i];
							filePath = root+file.getName();
							BufferedReader in = new BufferedReader(new FileReader(filePath));
							String read = "";
							String sendMsg = "/sendFile\n";
							String sendPath = "/filePath "+filePath;
							sendMsg += sendPath +"\n";
							System.out.println(filePath+" 전송시도..");
							while( (read = in.readLine()) != null ){
								System.out.println("파일 전송중...");
								sendMsg += read +"\n";
							}
							sendMsg += "EOF";
							ServerList.serverList[client_id].sendMessage(sendMsg);
							System.out.println(filePath+" 전송완료...");

							if(i == fileList.length-1){
								System.out.println("모든 파일 전송완료...");
								ServerList.serverList[client_id].sendMessage("/sendAllFile\nEOF"); //(Drone -> Sensor) 메시지 전송
							}
						}
					}catch(IOException e){
						e.printStackTrace();
					}					
				} else if (tempData[0].equals("/setPeriod")) { //(Gateway -> Drone) /setPeriod 메시지 수신
					System.out.println("Client로부터 드론 탐색 타이머 수신 완료");
					try {
						//전송 주기 peirod.txt 파일 출력
						PrintWriter tempFile = new PrintWriter(new FileWriter("/home/pi/data/period.txt"));						
						tempFile.write(tempData[1]);
						tempFile.close();
						System.out.println("드론 탐색 타이머 timer.txt 파일 출력 완료");
					} catch (IOException e) {						
						e.printStackTrace();
					}				
					
					//(Drone -> Sensor) 메시지 전송
					sendingMsg = tempData[1];
					ServerList.serverList[client_id].sendMsgToClient("/setPeriod\t" + tempData[1]);
					ServerList.serverList[client_id].sendMsgToClient("EOF");					
				} else if (tempData[0].equals("/setWiFiPeriod")) { //(Gateway -> Drone) /setWiFiPeriod 메시지 수신
					System.out.println("Client로부터 드론 탐색 주기 수신 완료");										
					try {
						//AP 재탐색 주기 wifi_peirod.txt 파일 출력
						PrintWriter tempFile = new PrintWriter(new FileWriter("/home/pi/data/wifi_period.txt"));						
						tempFile.write(tempData[1]);
						tempFile.close();						
						System.out.println("드론 탐색 주기 wifi_period.txt 파일 출력 완료");
					} catch (IOException e) {						
						e.printStackTrace();
					}				
					
					//(Drone -> Sensor) 메시지 전송
					sendingMsg = tempData[1];
					ServerList.serverList[client_id].sendMsgToClient("/setWiFiPeriod\t" + tempData[1]);
					ServerList.serverList[client_id].sendMsgToClient("EOF");					
				}
			}catch(IOException e) {				
				break;
			}
		}
		
		//(Drone -> Sensor) 메시지 전송
		ServerList.serverList[client_id].sendMsgToClient("/setPeriod\t" + sendingMsg);
		ServerList.serverList[client_id].sendMsgToClient("EOF");				
		System.out.println("" + client_id + " 님이 종료하였습니다.");	
		close();
	}
}