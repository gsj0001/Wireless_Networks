import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.security.MessageDigest;

class UDPClient {
	public static void main(String args[]) throws Exception {

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = null;
		byte[] sendData = new byte[128];
		byte[] emptyData = new byte[128];
		byte[] receiveData = new byte[128];
		String ip_address;
		int portNum;
		Scanner scan = new Scanner(System.in);

		while(true){		
			System.out.print("Enter the IP address of the server: ");
			ip_address = scan.nextLine();

			try{

				IPAddress = InetAddress.getByName(ip_address);

			} catch(UnknownHostException e){

				System.out.println("Error: Invalid Host");
				continue;
			}
			break;
		}

		System.out.print("Enter the server port number: ");
		portNum = scan.nextInt();
		String sentence;
		String[] userInput;

		do{
			System.out.println("Enter the HTTP request: ");
			sentence = inFromUser.readLine();
			userInput = sentence.split("[ ]");

		}while(userInput.length != 3);
		
		String filename = userInput[1];
		String fileCheckSum = getMD5Checksum(filename);

		File file = new File(System.getProperty("user.dir"), userInput[1]);

		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNum);
		clientSocket.send(sendPacket); 
		scan.close();

		DatagramPacket receivePacket;

		receivePacket = new DatagramPacket(receiveData, receiveData.length); 
		clientSocket.setSoTimeout(2000);

		try{

		clientSocket.receive(receivePacket);

		} catch(SocketTimeoutException e){
			System.out.println("Connection timeout.");
			clientSocket.close();
		}

		if(!clientSocket.isClosed()){			

		String respnseFromServer = new String(receivePacket.getData());
		String[] usrIn = respnseFromServer.split("[ ]");
		if(usrIn.length > 3){
			System.out.println(respnseFromServer);

			int index = 0;
			for(int i = 0; i < 4; i++){
				index = respnseFromServer.indexOf('\n', index + 1);
			}

			String fileData = respnseFromServer.substring(index + 1);
			int x = -1;
			while(x == -1){

				receivePacket = new DatagramPacket(receiveData, receiveData.length); 
				clientSocket.receive(receivePacket);
				respnseFromServer = new String(receivePacket.getData());
				System.out.println(respnseFromServer);
				
				receiveData = emptyData.clone();				
				
				x = respnseFromServer.indexOf(0);
				fileData = fileData + respnseFromServer;
			}
			clientSocket.close();
			
			
			
			Writer fileWrite = null;
			try {
				fileData = fileData.trim();
				fileWrite = new BufferedWriter(new OutputStreamWriter(	new FileOutputStream(file), "UTF-8"));
				fileWrite.write(fileData);
				fileWrite.close();

			} 
			catch (IOException ex) {

			} 
			finally {

				try {
					fileWrite.close();
				} 

				catch (Exception ex) {
				}
			}
		} 
		else {

			file =null;
			System.out.println("Error: bad server response.");
		}

	    } 
		
	}
	
	public static byte[] createChecksum(String filename) throws
    Exception
	{
	  InputStream fis =  new FileInputStream(filename);
	
	  byte[] buffer = new byte[1024];
	  MessageDigest complete = MessageDigest.getInstance("MD5");
	  int numRead;
	  do {
	   numRead = fis.read(buffer);
	   if (numRead > 0) {
	     complete.update(buffer, 0, numRead);
	     }
	   } while (numRead != -1);
	  fis.close();
	  return complete.digest();
	}


	public static String getMD5Checksum(String filename) throws Exception {
	  byte[] b = createChecksum(filename);
	  String result = "";
	  for (int i=0; i < b.length; i++) {
	    result +=
	       Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	   }
	  return result;
	}
	
	public static boolean checksumValidated(String checksumBefore, String checksumAfter)
	{
		return checksumBefore == checksumAfter;
	}
} 
