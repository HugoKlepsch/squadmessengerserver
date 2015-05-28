/*
		 Title: ServerComms.java
		 Programmer: graham
		 Date of creation: May 26, 2015
		 Description: 
*/


package client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import sharedPackages.ActionRequest;
import sharedPackages.LoginDeets;
import sharedPackages.Message;
import sharedPackages.ActionTypes;

/**
 * @author graham
 *
 */
public class ServerOutComms extends Thread{
	private static int port = 6969;
	private Socket socket;
	public static int loopDelay = 333;
	private static ObjectOutputStream csStream;
	private LoginDeets userDeets;
	private String ipAddress;
	
	public ServerOutComms(String ipAddress, LoginDeets userDeets){
		this.ipAddress = ipAddress;
		this.userDeets = userDeets;
	}
	
	private void sendMsg(Message message) throws IOException{
		csStream.writeObject(new ActionRequest(ActionTypes.CSSENDMESSAGE, message));
		csStream.flush();
	}
	
	public void run(){
		try {
		socket = new Socket(this.ipAddress, port);
		csStream = new ObjectOutputStream(socket.getOutputStream());
		ActionRequest connectRequest = new ActionRequest(ActionTypes.CSCONNECT, new Message(userDeets, null));
		csStream.writeObject(connectRequest);
		csStream.flush();
		ActionRequest indexRequest = new ActionRequest(ActionTypes.CSGETCURRENTMESSAGEINDEX);
		while(ClientMain.StayAlive()){
			Thread.sleep(loopDelay);
			csStream.writeObject(indexRequest);
			csStream.flush();
			System.out.println("LocalIndex: " + ClientMain.getLocalIndex() + "\nRemoteIndex: " + ClientMain.getRemoteIndex());
			if(ClientMain.isUpToDate()){
				if(!ClientMain.messageQueue.isEmpty()){
					sendMsg(ClientMain.messageQueue.deQueue());
				}
			} else {
				System.out.println("ClientMain.isuptodate is false. ");
				for(int i = ClientMain.getLocalIndex() + 1; i<ClientMain.getRemoteIndex()+1;i++){
					ActionRequest getMsgRequest = new ActionRequest(ActionTypes.CSGETMESSAGE, i);
					csStream.writeObject(getMsgRequest);
					csStream.flush();
				}
			}
		}
		ActionRequest disconnectRequest = new ActionRequest(ActionTypes.CSDISCONNECT);
		csStream.writeObject(disconnectRequest);
		csStream.flush();
		Thread.sleep(400);
		csStream.close();
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
