import java.io.*;
import java.net.*;

public class UDPClient {
	public static void main(String args[]) throws Exception {

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		DatagramSocket clientSocket = new DatagramSocket(); 

		byte[] sendData = new byte[128];
		byte[] emptyData = new byte[128];
		byte[] receiveData = new byte[128];

		InetAddress IPAddress = null; 
		
		String hostAddress;
		int serverPortNumber;
		Scanner userInput = new Scanner(System.in);
		while(true){		
			System.out.print("Enter the server IP address: ");
			hostAddress = userInput.nextLine();
			try{
				IPAddress = InetAddress.getByName(hostAddress);
			} catch(UnknownHostException e){
				System.out.println("Invalid host name or the host name is unreachable.");
				continue;
			}
			break;
		}

		System.out.print("Enter the server port number: ");
		serverPortNumber = userInput.nextInt();
		String sentence;
		String[] request;
		do{
			System.out.println("Type your HTTP Request and press Enter:");
			sentence = inFromUser.readLine();
			request = sentence.split("[ ]");
		}while(request.length != 3);


		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		String sentence = inFromUser.readLine(); 
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence); 
		clientSocket.close();
		}
}
