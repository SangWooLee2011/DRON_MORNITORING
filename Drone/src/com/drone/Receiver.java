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
	 * Receiver ������ - (Gateway -> Drone) �޽��� ������ ���� InputStream ����
	 * @param is InputStream
	 * @param client_id client id
	 */	
	public Receiver(InputStream is, int client_id) {
		in = new BufferedReader(new InputStreamReader(is));
		this.client_id = client_id;
		up = new BufferedInputStream(is);
	}

	/**
	 * (Gateway -> Drone) �޽��� ������ ���� InputStream ����
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
	 * Thread�� ������ ���� run �Լ� - (Gateway -> Drone) �޽��� ����
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
				
				//(Gateway -> Drone) �޽��� ����
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
				if(tempData[0].equals("/sendFile")){ //(Gateway -> Drone) /sendFile �޽��� ����
					System.out.println("Sensors or Actuators to Drone");
					//tempData[1] -> ���+����
					for(int i=1; i<tempData.length; i++){
						//System.out.println("file: "+tempData[i]);
						if(tempData[i].matches(".*/filePath.*")){
							filePath = tempData[i].split(" ")[1];
							System.out.println(filePath+" ���� ����...");
							//���� �ʱ�ȭ
							fileOut = new PrintWriter(new FileWriter(filePath));
							fileOut.close();
							//
							fileOut = new PrintWriter(new FileWriter(filePath,true));

						}else{
							if(tempData[i].equals("EOF")){
								fileOut.flush();
								fileOut.close();
								System.out.println(filePath+" ���� �Ϸ�...");
								ServerList.serverList[client_id].sendMessage("NEXT"); //(Drone -> Sensor) �޽��� ����

							}else{
								fileOut.println(tempData[i]); //(Gateway -> Drone) ���� ����
							}
						}
					}
				}else if(tempData[0].equals("/getFile")){ //(Gateway -> Drone) /getFile �޽��� ����
					System.out.println("Drone to Gateway");
					String root = "/home/pi/data/";
					File dir = new File(root);
					File[] fileList = dir.listFiles();
					System.out.println("���۽õ�");

					try{						
						for(int i=0; i<fileList.length; i++){
							File file = fileList[i];
							filePath = root+file.getName();
							BufferedReader in = new BufferedReader(new FileReader(filePath));
							String read = "";
							String sendMsg = "/sendFile\n";
							String sendPath = "/filePath "+filePath;
							sendMsg += sendPath +"\n";
							System.out.println(filePath+" ���۽õ�..");
							while( (read = in.readLine()) != null ){
								System.out.println("���� ������...");
								sendMsg += read +"\n";
							}
							sendMsg += "EOF";
							ServerList.serverList[client_id].sendMessage(sendMsg);
							System.out.println(filePath+" ���ۿϷ�...");

							if(i == fileList.length-1){
								System.out.println("��� ���� ���ۿϷ�...");
								ServerList.serverList[client_id].sendMessage("/sendAllFile\nEOF"); //(Drone -> Sensor) �޽��� ����
							}
						}
					}catch(IOException e){
						e.printStackTrace();
					}					
				} else if (tempData[0].equals("/setPeriod")) { //(Gateway -> Drone) /setPeriod �޽��� ����
					System.out.println("Client�κ��� ��� Ž�� Ÿ�̸� ���� �Ϸ�");
					try {
						//���� �ֱ� peirod.txt ���� ���
						PrintWriter tempFile = new PrintWriter(new FileWriter("/home/pi/data/period.txt"));						
						tempFile.write(tempData[1]);
						tempFile.close();
						System.out.println("��� Ž�� Ÿ�̸� timer.txt ���� ��� �Ϸ�");
					} catch (IOException e) {						
						e.printStackTrace();
					}				
					
					//(Drone -> Sensor) �޽��� ����
					sendingMsg = tempData[1];
					ServerList.serverList[client_id].sendMsgToClient("/setPeriod\t" + tempData[1]);
					ServerList.serverList[client_id].sendMsgToClient("EOF");					
				} else if (tempData[0].equals("/setWiFiPeriod")) { //(Gateway -> Drone) /setWiFiPeriod �޽��� ����
					System.out.println("Client�κ��� ��� Ž�� �ֱ� ���� �Ϸ�");										
					try {
						//AP ��Ž�� �ֱ� wifi_peirod.txt ���� ���
						PrintWriter tempFile = new PrintWriter(new FileWriter("/home/pi/data/wifi_period.txt"));						
						tempFile.write(tempData[1]);
						tempFile.close();						
						System.out.println("��� Ž�� �ֱ� wifi_period.txt ���� ��� �Ϸ�");
					} catch (IOException e) {						
						e.printStackTrace();
					}				
					
					//(Drone -> Sensor) �޽��� ����
					sendingMsg = tempData[1];
					ServerList.serverList[client_id].sendMsgToClient("/setWiFiPeriod\t" + tempData[1]);
					ServerList.serverList[client_id].sendMsgToClient("EOF");					
				}
			}catch(IOException e) {				
				break;
			}
		}
		
		//(Drone -> Sensor) �޽��� ����
		ServerList.serverList[client_id].sendMsgToClient("/setPeriod\t" + sendingMsg);
		ServerList.serverList[client_id].sendMsgToClient("EOF");				
		System.out.println("" + client_id + " ���� �����Ͽ����ϴ�.");	
		close();
	}
}