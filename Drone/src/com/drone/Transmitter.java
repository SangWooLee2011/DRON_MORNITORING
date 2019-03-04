package com.drone;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

//Drone Transmitter class
public class Transmitter {
	private PrintWriter out = null;
	
	/**
	 * Transmitter ������
	 * @param os OutputStream 
	 */
	public Transmitter(OutputStream os) {
		out = new PrintWriter(new OutputStreamWriter(os));
	}
	
	/**
	 * ���� ����
	 */
	public void close() {
		if(out != null)
			out.close();
	}
	
	/**
	 * (Drone -> Sensor) �޽��� ����
	 * @param msg �޽��� 
	 */
	public void sendMessage(String msg) {
		if(out != null) {
			out.println(msg);
			out.flush();
		}
	}
}