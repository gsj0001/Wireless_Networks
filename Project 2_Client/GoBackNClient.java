import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;



public class GoBackNClient {
	
	private final static boolean enableTestLogging = true;
	
	//list of acknowledgments
	String[] ackBuffer = new String[32];
	String currentAck = "A00";
	//ordered Fragments
	ArrayList<Fragment> fragList = new ArrayList<Fragment>();
	//unordered Fragments more or a buffer
	Fragment[] segWindow = new Fragment[32];


	int currentReceiveIndex = 0;
	int windowIndex = 3;

	int sucessfully_received = 0;
	int unsucessfully_received = 0; 
	//constants in the SR
	int localPortNumber;
	int serverPortNumber;
	InetAddress serverIP;

	boolean lastFragmentRecieved = false;

	DatagramSocket clientSocket;

	Gremlin gremlin;

	boolean trace = false;

	/**
	 * @param local_Port - Port on client Side
	 * @param server_IP - The Server IP
	 * @param remote_Port - Port on Server side
	 * @param grem is the Gremlin Used inside SR
	 */
	public GoBackNClient(int local_Port,InetAddress server_IP, int remote_Port, Gremlin grem){
		gremlin = grem;

		for(int i = 0; i < ackBuffer.length; i++){
			ackBuffer[i] = "N" + i;
		}

		localPortNumber = local_Port;
		serverPortNumber = remote_Port;
		serverIP = server_IP;

		try {
			clientSocket = new DatagramSocket(localPortNumber);

		} catch (SocketException e) {
			e.printStackTrace();
		}
		//trace = traceOPT;
	}

	/**
	 * @return
	 */
	public ArrayList<Fragment> getFragList(){
		return fragList;
	}

	/**
	 * The run function of the sr layer. Loops through until the last fragment is
	 * received from the server. Stores the fragments in the fragment list
	 */
	public void beginTransmission(){
		if(enableTestLogging)
		{
			System.out.println("Beginning transmission");
		}
		//loop until the last fragment is recieved
		while(!lastFragmentRecieved){
			if(enableTestLogging)
			{
				System.out.println("The last fragment has NOT been received.");
			}
			if(trace){
				System.out.println("Fragment: "+ sucessfully_received +" "+"Recieved -- intact" );
				System.out.println("Fragment: "+ unsucessfully_received +" "+"Recieved -- damaged" );
			}
			System.out.println(Arrays.toString(ackBuffer));

			byte[] receiveData = new byte[512];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			////			
			//Corrupting the recievedPacket Layer
			receivePacket = gremlin.filter(receivePacket);

			//Error Detection layer
			if(receivePacket!=null){
				Fragment temp = convertToFrag(receivePacket);
				int sequenceID = temp.getmHeader().getSequenceID();
				//Use Checksum to validate fragment
				System.out.println("Sequence # : " + sequenceID);
				boolean behindrcvbase =(sequenceID >= (currentReceiveIndex - 4) % 32 && sequenceID <= (currentReceiveIndex - 1) % 32); 
				boolean inwindow = (sequenceID >= currentReceiveIndex && sequenceID <= windowIndex);
				boolean inwindowmodulo = (sequenceID <= 31 && currentReceiveIndex > windowIndex);
				boolean validfragment = ErrorDetector.validateCheckSum(temp);
				System.out.println("Is Fragment Valid: "+ validfragment);
				
					if(validfragment){
						if(behindrcvbase || inwindow || inwindowmodulo){

						//Update acknowledgementBuffer and send ACK to Server
						ackBuffer[sequenceID]= "A" + sequenceID;
						
						currentAck = "A" + sequenceID
								
						segWindow[sequenceID] = temp;
//						sendAcknowledgements();
						sendAck();
						//If this is the first Sequence ID in the pane then we can move the window down one
						incrementWindowPosition();
						sucessfully_received++;
						if(enableTestLogging)
						{
							System.out.println("Packet of sequence ID " + sequenceID + " has been successfully transmitted.");
						}
					}else{
						//send acknowledgment if the fragment was corrupted
					//	sendAcknowledgements();
						currentAck = "N" + sequenceID;
						sendAck();
						unsucessfully_received++;
						if(enableTestLogging)
						{
							System.out.println("Packet of sequence ID " + sequenceID + "has NOT been succesfully transmitted. (corruption)");
						}
					}
						
				}
			}
		}
	}


	public static Fragment convertToFrag(DatagramPacket datagram){
		if(datagram != null){
		byte[] data = datagram.getData();
		Fragment tempFragment = new Fragment(Arrays.copyOfRange(data, 18, data.length));
		tempFragment.getmHeader().setCheckSum(Arrays.copyOfRange(data, 0, 16));
		tempFragment.getmHeader().setSequenceID(data[16]);
		//Set fragment's end of Sequence value
		tempFragment.getmHeader().setmEndOfSequence(data[17]);
		return tempFragment;
	}
		return null;
	}

	/**
	 * Transmits a vector of ACks and NAKs back to the server after each fragment is received
	 */
//	public void sendAcknowledgements(){
//		ArrayList<String> ackVector = new ArrayList<String>();
//		for (int i = 0; i < 32; i++) {
//			ackVector.add(ackBuffer[(currentReceiveIndex + i) % 32]);
//		}
//		if(enableTestLogging)
//		{
//			System.out.println("Sending Acknowledgment array: " + ackVector);
//		}
//
//		byte[] tes = null;
//
//		ByteArrayOutputStream objbytetest = new ByteArrayOutputStream();
//		ObjectOutputStream objout;
//
//		String[] tempAckVector = new String[ackVector.size()];
//		tempAckVector = ackVector.toArray(tempAckVector);
//		if(trace){
//			System.out.println(Arrays.toString(tempAckVector) + " Transmitted");
//		}
//		try {
//			objout = new ObjectOutputStream(objbytetest);
//			objout.writeObject(tempAckVector);
//			objout.flush();
//			objout.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		tes = objbytetest.toByteArray();
//		try {
//			objbytetest.flush();
//			objbytetest.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		//send Acknowledgment buffer to the server
//		DatagramPacket sendPacket = new DatagramPacket(tes, tes.length, serverIP, serverPortNumber);
//		try {
//			clientSocket.send(sendPacket);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	//sends an ACK or NAK, with the sequence #
	public void sendAck()
	{
		byte[] tes = null;
		
				ByteArrayOutputStream objbytetest = new ByteArrayOutputStream();
				ObjectOutputStream objout;
				if(trace){
					System.out.println(currentAck + " Transmitted");
				}
				try {
					objout = new ObjectOutputStream(objbytetest);
					objout.writeObject(currentAck);
					objout.flush();
					objout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				tes = objbytetest.toByteArray();
				try {
					objbytetest.flush();
					objbytetest.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//send Acknowledgment buffer to the server
				DatagramPacket sendPacket = new DatagramPacket(tes, tes.length, serverIP, serverPortNumber);
				try {
					clientSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
	}

	/**
	 * Moves the receive window over one position
	 */
	public void incrementWindowPosition() {
		if(enableTestLogging)
		{
			System.out.println("Incrementing window position from: " + windowIndex);
		}
		while(ackBuffer[currentReceiveIndex].startsWith("A")){
			//This will be used to decide when to close the connection
			lastFragmentRecieved = (segWindow[currentReceiveIndex].getmHeader().getmEndOfSequence() == 1)?true:false;

			if(lastFragmentRecieved){
				System.out.println("sf");
			}

			//Adds the Ordered Fragment into the FragementList
			fragList.add(segWindow[currentReceiveIndex]);
			currentReceiveIndex = (currentReceiveIndex + 1) % 32;
			windowIndex = (windowIndex + 1) % 32;

			//Always keeps the area outside of the pane ready
			ackBuffer[windowIndex] = "N" + windowIndex;
			segWindow[windowIndex] = null;
		}
		if(enableTestLogging)
		{
			System.out.println("Incremented to: " + windowIndex);
		}
	}


}