package com.drone;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

//Drone Server class
public class Server {
	private int client_id = -1;
	private Socket client_socket = null;
	private Receiver receiver = null;
	private Transmitter transmitter = null;
	private PrintWriter out = null;
	
	/**
	 * ������ idle ���� ��ȯ
	 */
	public boolean isIdle() {
		return (client_socket == null);
	}
	
	/**
	 * ���� ����
	 * @param client_id client id
	 * @param client_socket client socket
	 * @param period ���� �ֱ�
	 * @exception IOException
	 */
	public void setServer(int client_id, Socket client_socket, String period) {
		this.client_id = client_id;
		this.client_socket = client_socket;
		
		try {
			//(Drone -> Sensor) �޽��� ����
			out = new PrintWriter(new OutputStreamWriter(client_socket.getOutputStream())); 			
			out.println(period); 
			out.flush();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	/**
	 * ���� ����
	 * @exception IOException
	 */
	public void startServer() {
		if(client_id == -1 || isIdle()) {
			return;
		}
		
		try {
			transmitter = new Transmitter(client_socket.getOutputStream());
			receiver = new Receiver(client_socket.getInputStream(), client_id);
			receiver.start();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���� ����
	 */
	public void closeServer() {
		client_id = -1;
		client_socket = null;
	}
	
	/**
	 * (Drone -> Sensor) �޽��� ����
	 * @param msg �޽��� 
	 */
	public void sendMessage(String msg) {
		if(transmitter != null)
			transmitter.sendMessage(msg);
	}
	
	/**
	 * (Drone -> Sensor) �޽��� ����
	 * @param msg �޽��� 
	 */
	public void sendMsgToClient(String msg) {						
		out.println(msg);
		out.flush();
	}	
}
