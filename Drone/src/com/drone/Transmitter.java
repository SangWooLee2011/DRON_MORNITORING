package com.drone;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

//Drone Transmitter class
public class Transmitter {
	private PrintWriter out = null;
	
	/**
	 * Transmitter 생성자
	 * @param os OutputStream 
	 */
	public Transmitter(OutputStream os) {
		out = new PrintWriter(new OutputStreamWriter(os));
	}
	
	/**
	 * 연결 종료
	 */
	public void close() {
		if(out != null)
			out.close();
	}
	
	/**
	 * (Drone -> Sensor) 메시지 전송
	 * @param msg 메시지 
	 */
	public void sendMessage(String msg) {
		if(out != null) {
			out.println(msg);
			out.flush();
		}
	}
}