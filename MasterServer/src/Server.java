import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Server {
	public static void main(String args[]) {
		//synchronize();
		int port = 8888;
		ServerSocket ss = null;
		InputStream is = null;
		try {
			ss = new ServerSocket(port);
			System.out.println("Server 8888 Started...");

			while (true) {
				try {
					Socket s = ss.accept();
					is = s.getInputStream();
					System.out.println("------------New Response------------");
					GZIPInputStream gzis = new GZIPInputStream(is);
					ObjectInputStream ois = new ObjectInputStream(gzis);
					Message to = (Message) ois.readObject();
					if (to != null) {
						File f = new File("/Users/ankitkhani/Documents/workspace/"
								+ "MasterServer/MasterDir/"+to.fileName);
						if(to.action.equalsIgnoreCase("delete")){
							f.delete();
							System.out.println("File "+to.fileName+" deleted");
						}
						else if(to.action.equalsIgnoreCase("create")){
							System.out.println("action: " + to.action);
							System.out.println("fileData: " + to.fileData);
							System.out.println("fileName: " + to.fileName);
							//File f = new File(fileName);
							if (!f.exists()) {
								f.createNewFile();
							}
							PrintWriter pr = new PrintWriter(f);
							pr.println(to.fileData);
							pr.flush();
							pr.close();
						}
						else if(to.action.equalsIgnoreCase("modify")){
							f.getAbsolutePath();
						}
						is.close();
						s.close();
					}
				} catch (Exception e) {
					System.out.println("Exception --->" + e.getMessage());
				}
			}

			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private static List<String> fileToLines(String fileName){
		List<String> lines = new LinkedList<String>();
		String line = "";
		BufferedReader in;
		try{
			in = new BufferedReader(new FileReader(fileName));
			while((line = in.readLine())!=null){
				lines.add(line);
			}
		}  catch(IOException ie){
			ie.printStackTrace();
		}

		return lines;		
	}

}



