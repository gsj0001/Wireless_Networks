import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class GoBackNServer {
	ServerSegAndReassembly segFragments; //has the separated fragments

	int sucessfully_transmitted=0;
	int unsucessfully_transmitted=0;
	int send_base = 0;
	int window_base = 3;
	int currentSequenceNumber = 0;

	//constants
	final int BUFFER_AMT = 512;
	final int SERVER_PORT_NUMBER = 10077;
	final int windowSize = 32;

	//client information needed for sending
	int clientPortNumber;
	InetAddress clientIPAddr;

	boolean CLIENTRECIEVEDALLDATA = false;
	int lastFragmentInIndex = -1;

	Fragment[] fragmentWindow = new Fragment[windowSize];
	String[] AcknowledgmentBuffer = new String[windowSize];
	EventTime[] eventTimerArray = new EventTime[4];

	//Sending and RecivingSocket
	DatagramSocket serverSocket;

	Timer countDownTimer;

	boolean mTrace;

	//Constructor
	/**
	 * @param sar 
	 * @param client_port of the client
	 * @param ipAddress of the client
	 * @param trace will print some information
	 */
	public GoBackNServer(ServerSegAndReassembly sar, int client_port, InetAddress ipAddress, boolean trace){
		segFragments = sar;
		clientPortNumber = client_port;
		clientIPAddr = ipAddress;
		mTrace = trace; 

		try {
			serverSocket = new DatagramSocket(SERVER_PORT_NUMBER);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		for(int i = 0; i<AcknowledgmentBuffer.length; i++){
			AcknowledgmentBuffer[i]="N"+i;
		}

		fillFragmentWindow();
	}


	/**
	 * @param pos number correlates to the sequence number of the fragment
	 */
	public synchronized void sendFragment(int pos){
		if( fragmentWindow[pos] != null){

			byte[] fragmentData = fragmentWindow[pos].getFragmentBytes();
			DatagramPacket sendPacket = new DatagramPacket(fragmentData, fragmentData.length, clientIPAddr, clientPortNumber);

			try {
				serverSocket.send(sendPacket);
				eventTimerArray[pos%4] = new EventTime(pos, 30);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Recieves the acks and naks from the client and starts the process to send back the fragments
	 * to the client as well dequeues the naks to send those fragments to the client.
	 */
	public void beginTransmission(){

		countDownTimer = new Timer();
		countDownTimer.schedule(new PerTickBookKeeping(), 1,1);

		for(int i=0 ; i < 4 ; i++){
			sendFragment(i);
		}

		while(!CLIENTRECIEVEDALLDATA){
			byte[] receiveData = new byte[BUFFER_AMT];
			if(mTrace){
				System.out.println("Fragment: "+ sucessfully_transmitted +" "+"Transmitted -- intact" );
				System.out.println("Fragment: "+ unsucessfully_transmitted+" " +"Transmitted -- damaged" );
			}

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			String[] acknowledgments = recieveAcknowledgment(Arrays.copyOf(receivePacket.getData(), receivePacket.getLength()));
			updateServerAcknowledgmentArray(acknowledgments);
			
			if(AcknowledgmentBuffer[send_base].startsWith("A")){
				incrementWindowPosition();
			}
			for(int i = 0; i<4;i++){
				if(AcknowledgmentBuffer[(send_base+i)%64].startsWith("N")){
					sendFragment((send_base+i)%64);
					unsucessfully_transmitted++;
				}
			}

			//resetting the receive Data variable
			receiveData = null;
			if(CLIENTRECIEVEDALLDATA){
				System.out.println("Connection is Closing...");
			}
		}
		closeConnection();
	}


	/**
	 * This is used to update the server's acknowledgement array
	 * @param acknowledgmentArray the string array that is from the client
	 */
	public void updateServerAcknowledgmentArray(String[] acknowledgmentArray){
		int ackSequenceID;
		boolean isACK = false;
		boolean isLastFragmentAcknowledged = false;
		System.out.println(Arrays.toString(acknowledgmentArray) + " Received");

		for(int i = 0; i < 32; i++){
			ackSequenceID = Integer.parseInt(acknowledgmentArray[i].substring(1));
			isACK = acknowledgmentArray[i].startsWith("A");

			if(lastFragmentInIndex != -1){
			isLastFragmentAcknowledged = (fragmentWindow[lastFragmentInIndex].getmHeader().getmEndOfSequence() ==1)?true:false;
			}

			if(isACK){
				sucessfully_transmitted++;
				AcknowledgmentBuffer[ackSequenceID] = acknowledgmentArray[i];
				if(ackSequenceID >= send_base && ackSequenceID <=window_base){
					eventTimerArray[ackSequenceID%4]=null;
				}

				if(isLastFragmentAcknowledged){
					CLIENTRECIEVEDALLDATA = true;
				}
			}
		}

	}


	/**
	 * @param acknowledgment
	 * @return String filled with acknowledgments that are recieved from the reciever
	 */
	public String[] recieveAcknowledgment(byte[] acknowledgment){
		ByteArrayInputStream bytein = new ByteArrayInputStream(acknowledgment);
		ObjectInputStream objin=null;
		String[] ackArray = null;

		try {
			objin = new ObjectInputStream(bytein);
			ackArray = (String[])objin.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
		}

		try {
			objin.close();
			bytein.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ackArray;
	}

	/**
	 * A Run once Method to fill the window with Fragments
	 */
	public void fillFragmentWindow(){
		for(int i = 0; i < 32; i++){
			if(segFragments.hasNext()){
						
				fragmentWindow[i] = segFragments.next();

			} else{
				break;
			}
		}
	}

	/**
	 * Moves the Window Position over.
	 */
	public void incrementWindowPosition(){
		while(AcknowledgmentBuffer[send_base].startsWith("A")){

			send_base = (send_base + 1) % 64;
			window_base = (window_base + 1) % 64;
			AcknowledgmentBuffer[window_base] = "N"+window_base;

			if(segFragments.hasNext()){
				fragmentWindow[window_base] = segFragments.next();
				boolean testForLastFragment =(fragmentWindow[window_base].getmHeader().getmEndOfSequence() ==1)?true:false;
				if(testForLastFragment){
					lastFragmentInIndex = window_base;
				}
			}else{
				fragmentWindow[window_base] = null;
			}
		}

	}

	/**
	 * Closes the connections and thread that was used for this class
	 */
	public void closeConnection(){
		countDownTimer.cancel();
		countDownTimer.purge();
		serverSocket.close();
	}


	
	class  PerTickBookKeeping extends TimerTask{
		@Override
		synchronized public void run() {
			for(int i=0; i<eventTimerArray.length; i++){

				if(eventTimerArray[i] !=null){
					eventTimerArray[i].decrementExpirationTime();
					eventTimerArray[i].decrementExpirationTime();

					if(eventTimerArray[i].expirationTime <= 0){
						int tempID = eventTimerArray[i].eventID;
						eventTimerArray[i] = null;
						
						if(AcknowledgmentBuffer[tempID].startsWith("N")){
							sendFragment(tempID);
						}
					}
				}
			}//endfor
		}
	}
}