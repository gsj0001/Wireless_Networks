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
		String serverIP;
		int serverPortNumber;
		Scanner scan = new Scanner(System.in);
		while(true){		
			System.out.print("Enter the IP Adress: ");
			serverIP = scan.nextLine();
			try{
				IPAddress = InetAddress.getByName(serverIP);
			} catch(UnknownHostException e){
				System.out.println("Invalid host..");
				continue;
			}
			break;
		}
		///NEW///
		System.out.print("Enter the server port number: ");
		serverPortNumber = scan.nextInt();
		String sentence;
		String[] request;
		do{
			System.out.println("Enter HTTP request: ");
			sentence = inFromUser.readLine();
			request = sentence.split("[ ]");
		}while(request.length != 3);
		
		String filename = request[1];

		File file = new File(System.getProperty("user.dir"), request[1]);
		
		System.out.print("Enter the probability of a corrupted packet: ");
		corruptProb = scan.nextDouble();
		//Set the crorrupt prob in the grem func*
		Gremlin gremlin = new Gremlin(corruptProb);
		
		//Send the request to the server//
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPortNumber);
		clientSocket.send(sendPacket); 
		scan.close();

		DatagramPacket receivePacket;
		//Receive packet from server
		receivePacket = new DatagramPacket(receiveData, receiveData.length); 
		clientSocket.setSoTimeout(2000);
		try{
		clientSocket.receive(receivePacket);
		} catch(SocketTimeoutException e){
			System.out.println("Connection timeout.");
			clientSocket.close();
		}
		////NEW////
		//set the corrupted packet(if neccessary)
		receivePacket = gremlin.filter(receivePacket);

		//Detect any errrors 

		//Error detect func here ***

		//////


		///NEW////
		//Reassemble the fragmented data that was received***
		ClientSegAndReassembly csar = new ClientSegAndReassembly();
		csar.setFragmentList(gbn.getFragList());
		csar.assembleData();
		byte[] csarDataIn = csar.getData();
		String csarData = new String(csarDataIn);
		//Print out the data//
		System.out.println(csarData);


		//if(!clientSocket.isClosed()){			
		
		// //String csarData = new String(receivePacket.getData());
		// String[] receivedCsarHeaderData = csarData.split("[ ]");
		// //String[] receivedPacketHeaderData = csarData.split("[ ]");
		// if(receivedPacketHeaderData.length > 3){
		// 	//System.out.println(csarData);
		// 	System.out.println(csarData);
			
		// 	int indexOfcrlf=0;
	
		// 	for(int i=0; i < 4; i++){

		// 		indexOfcrlf = csarData.indexOf('\n', indexOfcrlf+1);
		// 	}

		// 	String fileData = csarData.substring(indexOfcrlf+1);
		// 	int eos =- 1;
		// 	while(eos == -1){

		// 		receivePacket = new DatagramPacket(receiveData, receiveData.length); 
		// 		clientSocket.receive(receivePacket);
		// 		csarData = new String(receivePacket.getData());
		// 		System.out.println(csarData);
				
		// 		receiveData = emptyData.clone();				
				
		// 		eos = csarData.indexOf(0);
		// 		fileData = fileData + csarData;
		// 	}
		clientSocket.close();
			
			
			
			Writer writer = null;
			try {
				fileData = fileData.trim();
				writer = new BufferedWriter(new OutputStreamWriter(	new FileOutputStream(file), "UTF-8"));
				writer.write(fileData);
				writer.close();

			} catch (IOException ex) {
			} finally {
				try {
					writer.close();
				} catch (Exception ex) {
				}
			}
		} else {
			file =null;
			System.out.println("Error: Bad response");
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