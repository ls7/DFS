import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import difflib.DiffUtils;
import difflib.Patch;

public class MyDirectoryWatcher extends DaemonThread{


    private String directory;
    

    public Map<String,Long> currentFiles = new HashMap<String,Long>();


    public Map<String,Long> prevFiles = new HashMap<String,Long>();    

    
    public MyDirectoryWatcher(String directoryPath, int intervalSeconds)
            throws IllegalArgumentException {

        //Get the common thread interval stuff set up.
        super(intervalSeconds);

        //Check that it is indeed a directory.
        File theDirectory = new File(directoryPath);

        if (theDirectory != null && !theDirectory.isDirectory()) {

            //This is bad, so let the caller know
            String message = "The path " + directory +
                    " does not represent a valid directory.";
            throw new IllegalArgumentException(message);

        }

        //Else all is well so set this directory and the interval
        this.directory = directoryPath;

    }
    
    public static void main(String[] args) {
        // Monitor every 5 seconds
        MyDirectoryWatcher dw = new MyDirectoryWatcher("/Users/ankitkhani/Documents/test/", 5);
        dw.start();
    }
    
    public void start() {

        //Since we're going to start monitoring, we want to take a snapshot of the
        //current directory to we have something to refer to when stuff changes.
        takeSnapshot();

        //And start the thread on the given interval
        super.start();

    }
    
    private void takeSnapshot() {

        //Set the last recorded snap shot to be the current list
        prevFiles.clear();
        prevFiles.putAll(currentFiles);

        //And get a new current state with all the files and directories
        currentFiles.clear();

        File theDirectory = new File(directory);
        File[] children = theDirectory.listFiles();

        //Store all the current files and their timestamps
        for (int i = 0; i < children.length; i++) {

            File file = children[i];
            currentFiles.put(file.getAbsolutePath(),
                    new Long(file.lastModified()));

        }

    }

    /**
     * Check this directory for any changes and fire the proper events.
     */
    protected void doInterval() {

        //Take a snapshot of the current state of the dir for comparisons
        takeSnapshot();

        //Iterate through the map of current files and compare
        //them for differences etc...
        Iterator currentIt = currentFiles.keySet().iterator();

        while (currentIt.hasNext()) {

            String fileName = (String) currentIt.next();
            Long lastModified = (Long) currentFiles.get(fileName);
            

            //If this file did not exist before, but it does now, then
            //it's been added
            if (!prevFiles.containsKey(fileName)) {
                //DirectorySnapshot.addFile(fileName);
                resourceAdded(new File(fileName));
            }
            //If this file did exist before
            else if (prevFiles.containsKey(fileName)) {

                Long prevModified = (Long) prevFiles.get(fileName);

                //If this file existed before and has been modified
                if (prevModified.compareTo(lastModified) != 0) {
                    // 27 June 2006
                    // Need to check if the file are removed and added
                    // during the interval
                   /* if (!DirectorySnapshot.containsFile(fileName)) {
                        resourceAdded(new File(fileName));
                    } else {*/
                        resourceChanged(new File(fileName));
                    //}
                }
            }
        }

        //Now we need to iterate through the list of previous files and
        //see if any that existed before don't exist anymore
        Iterator prevIt = prevFiles.keySet().iterator();

        while (prevIt.hasNext()) {

            String fileName = (String) prevIt.next();

            //If this file did exist before, but it does not now, then
            //it's been deleted
            if (!currentFiles.containsKey(fileName)) {
               //DirectorySnapshot.removeFile(fileName);
                resourceDeleted(fileName);
            }
        }
    }

   
    protected void resourceAdded(File file) {
    	try {
			Files.copy( 
			        file.toPath(), 
			        new File("/Users/ankitkhani/Documents/testbackup/"+file.getName()).toPath(),
			        StandardCopyOption.REPLACE_EXISTING,
			        StandardCopyOption.COPY_ATTRIBUTES);
			
			String fileData = readFile(file.getAbsolutePath());
			
			Message messageObj = new Message("create", file.getName(), fileData, null, null);
			
	        new Thread(new SocketThread(messageObj)).start();

		} catch (IOException e) {
			System.out.println("IO Exception in resourceAdded method");
			e.printStackTrace();
		}

    }

    protected void resourceChanged(File file) {
    	try {
    		String dirPath = null, backupPath = null;
    		dirPath = "/Users/ankitkhani/Documents/test/"+file.getName();
    		backupPath = "/Users/ankitkhani/Documents/testbackup/"+file.getName();
    		
    		List<String> previous = fileToLines(backupPath);
    		List<String> newFile = fileToLines(dirPath);
    		
    		SerializedPatch patch = (SerializedPatch) DiffUtils.diff(previous, newFile);
    		    		
    		Message messageObj = new Message("modify", file.getName(), null, patch, null);
    		
    		new Thread(new SocketThread(messageObj)).start();;
    		
			Files.copy( 
			        file.toPath(), 
			        new File(backupPath).toPath(),
			        StandardCopyOption.REPLACE_EXISTING,
			        StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			System.out.println("IO Exception in resourceChanged method");
			e.printStackTrace();
		}
        //new Thread(new SocketThread1(file.getAbsolutePath(), "modify")).start();
        //new Thread(new SocketThread2(file.getAbsolutePath(), "modify")).start();

    }

    protected void resourceDeleted(String file) {
		try {
			String[] name = file.split("test/");
			System.out.println(file);
			Files.deleteIfExists(new File("/Users/ankitkhani/Documents/testbackup/"+name[1]).toPath());
			
			Message messageObj = new Message("delete", name[1], null, null, null);
			new Thread(new SocketThread(messageObj)).start();
		} catch (IOException e) {
			System.out.println("IO Exception in resourceDeleted method");
			e.printStackTrace();
		}

    }
    
    private String readFile(String fileName) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(fileName));
	    String line = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append(line);
	        stringBuilder.append(ls);
	    }
	    reader.close();
	    return stringBuilder.toString();
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