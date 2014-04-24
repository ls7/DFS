import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.GZIPOutputStream;

import difflib.Delta;
import difflib.Patch;
//import java.util.zip.GZIPOutputStream;

public class SocketThread implements Runnable{
	
	//private String fileName;
	//private String action;
	//private Patch patch;
	private Message message;
	
	public SocketThread(Message message){
		this.message = message;
		//this.action = action;
		//this.fileName = fileName;
	}

	
	public void run() {
		try {
			System.out.println("name:" +message.fileName);
			System.out.println("action:" +message.action);
			System.out.println("data:" +message.fileData);
			
			if(message.patch!=null){
			for (Delta delta: message.patch.getDeltas()) {
                System.out.println(delta);
        }
			}
			 
			Socket sock = new Socket("localhost",8888);
			
			sendMessage(sock);
			/*String realFileName = fileName.substring(fileName.lastIndexOf("/")+1);
			Socket sock = new Socket("localhost",8888);
			GZIPOutputStream gz = new GZIPOutputStream(sock.getOutputStream(), true);
			ObjectOutputStream oos = new ObjectOutputStream(gz);
			if(action.equalsIgnoreCase("delete"))
			{
			Message messageObj = new Message(action, realFileName,null,null,null);
			oos.writeObject(messageObj);
			}
			else
			{
			String fileData = readFile();
			//String realFileName = fileName.substring(fileName.lastIndexOf("/")+1);
			
			Message messageObj = new Message(action, realFileName, fileData,null);
			
			Socket sock = new Socket("localhost",8888);
			GZIPOutputStream gz = new GZIPOutputStream(sock.getOutputStream(), true);
			ObjectOutputStream oos = new ObjectOutputStream(gz);
			oos.writeObject(messageObj);
			}
			oos.close();
			sock.close();*/
			
			
		} catch (Exception e) {
			System.err.println("Server 8888 down, trying server 9999...");
			try {
				Socket sock = new Socket("localhost",9999);
				sendMessage(sock);

			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}
	
	public void sendMessage(Socket sock){
		GZIPOutputStream gz;
		ObjectOutputStream oos;
		try {
			gz = new GZIPOutputStream(sock.getOutputStream(), true);
			oos = new ObjectOutputStream(gz);
			oos.writeObject(message);
			gz.close();
			oos.close();
			sock.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
	}
	
}
