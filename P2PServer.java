import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class P2PServer extends Thread 
{
	ServerSocket welcomeSocket;
	int socket;
	int ftpSock;
	Thread thread;
	DataInputStream inFromClient;
	DataOutputStream outToClient;
	BufferedOutputStream outTransfer;
	
	  public P2PServer(int socketNum) throws IOException
	  {
		ftpSock = socketNum;
		socket = socketNum;
		welcomeSocket = new ServerSocket(socketNum);

	  }
	  
	  public void fileTransfer() throws IOException
	  {
		  //Create new socket for file transfer
		  Socket connectionSocket = welcomeSocket.accept();
		  System.out.println("Opening streams . . .");
		  inFromClient =  new DataInputStream(connectionSocket.getInputStream());
		  outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		  outTransfer = new BufferedOutputStream(connectionSocket.getOutputStream());
		  
		  //Wait for file name
		  System.out.println("Waiting for file name from Client . . .");
		  String theFile = inFromClient.readUTF();
		  theFile = theFile+".jpg";
		  File myFile = new File( theFile);
		  byte[] mybytearray = new byte[(int) myFile.length()];
		  FileInputStream fis = null;
		  System.out.println("File name received");
		  
		  //Send the file to the client via new socket
		  System.out.println("Sending file . . .");
		  try {
              fis = new FileInputStream(myFile);
          } catch (FileNotFoundException ex) {
              // Do exception handling
          }
          BufferedInputStream bis = new BufferedInputStream(fis);
          try {
              bis.read(mybytearray, 0, mybytearray.length);
              outTransfer.write(mybytearray, 0, mybytearray.length);
              //Finished transferring, close TCP
              outTransfer.flush();
              outTransfer.close();
              connectionSocket.close();

          } catch (IOException ex) {
              // Do exception handling
          }
          
		  System.out.println("File sent successfully, closing transfer TCP");
		  System.out.println("(Still Listening for Connections) What to do? (1 = INIT, 2 = INFORM AND UPDATE, 3 = QUERY FOR CONTENT, 5 = EXIT)");
		  this.thread = null;
	  }
	  
	   public void start ()
	   {
	      System.out.println("Starting new thread " );
	      if (thread == null)
	      {
	         thread = new Thread (this);
	         thread.start ();
	      }
	   }
	   
	   
	  public void run() 
	  {
		  while(thread != null)
	   		{
		   		
			   	try 
			   	{
			   		//Connection received
			   		if(socket == 40232) // 40232 is listening TCP, create a new thread for requesting client
			   		{
			   			  ftpSock++; // Increment socket num by 1 so that it won't be a TCP used before, this is to avoid closed sockets, they might be in use
				  		  System.out.println("Waiting for connection . . .");
						  Socket connectionSocket = welcomeSocket.accept();
						  System.out.println("Connection received");
						  inFromClient =  new DataInputStream(connectionSocket.getInputStream());
						  outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						  String sendThis = ftpSock + "";
						  outToClient.writeUTF(sendThis);
						  outToClient.flush();
						  System.out.println("Sent the listening socket ("+sendThis+") to: "+connectionSocket.getInetAddress().getHostAddress());
						  
						  System.out.println("Transfering Files . . .");
						  (new P2PServer(ftpSock)).start(); // Start a new thread, but socket won't be 40232, therefore it is a file transfer TCP
			   		}
			   		else // Otherwise it's a file transfer
			   			fileTransfer();
				}
			   	catch (IOException e) 
			   	{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   		}
	    }
	   
}
