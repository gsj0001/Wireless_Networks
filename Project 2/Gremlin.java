import java.net.DatagramPacket;
import java.util.Random;
import java.util.concurrent.TimeUnit;



public class Gremlin {
	
	double packetLossProb;
	double corruptedPacketProb;
	double delayedPacketProb;
	double successProb;
	double delayTime; //in ms

	int packetsLost = 0;
	int packetsCorrupt = 0;
	int packetsDelayed = 0;
	int successPackets = 0;

	Random randomNum;
	DatagramPacket packet;

	
	public Gremlin(double lossProbability, double corruptProbability, double delayProbability, double delay){
		randomNum = new Random();
		delayTime = delay;
		
		packetLossProb = lossProbability;
		corruptedPacketProb = corruptProbability;
		delayedPacketProb = delayProbability;
		successProb = 1 - (lossProbability + corruptProbability + delayProbability);
	}

	
	private void loadDatagramPacket(DatagramPacket datagram){
		packet = datagram;
	}
	
	public DatagramPacket getDatagramPacket(){
		return packet;
	}

	
	private DatagramPacket corruptDatagramPacket(){
		double sample = randomNum.nextDouble();
		byte[] data = getDatagramPacket().getData();


		
		if(sample <=.5){
			//randomly selects a byte from the data and then from that byte selects a bit and flips it
			//does this once
				int randomIndex = randomNum.nextInt(getDatagramPacket().getData().length);
				byte byteVar = data[randomIndex];
				int byteToInt = byteVar >= 0?byteVar:256 + byteVar;
				//Selects a random bit from the  randomly selected byte from the data to flip
				int x = randomNum.nextInt(8-(1-1)) % 8;
				int flippedInt = byteToInt ^ 1 << x;
				//converts the int back into a byte
				data[randomIndex]=(byte)flippedInt;
			
		}else if(sample <= (.3 + .5)){
			//randomly selects a byte from the data and then from that byte selects a bit and flips it
			//does this twice
			for(int i = 0; i<2; i++){

				int randomIndex = randomNum.nextInt(getDatagramPacket().getData().length);
				byte byteVar = data[randomIndex];
				int byteToInt = byteVar >= 0?byteVar:256 + byteVar;
				int x = randomNum.nextInt(8-(1-1)) % 8;
				int flippedInt = byteToInt ^ 1 << x;
				//converts the int back into a byte
				data[randomIndex]=(byte)flippedInt;
			}
			
		} else{
			//randomly selects a byte from the data and then from that byte selects a bit and flips it
			//does this three times
			for(int i = 0; i<3; i++){

				int randomIndex = randomNum.nextInt(getDatagramPacket().getData().length);
				byte byteVar = data[randomIndex];
				int byteToInt = byteVar >= 0?byteVar:256 + byteVar;
				int x = randomNum.nextInt(8-(1-1)) % 8;
				int flippedInt = byteToInt ^ 1 << x;
				//converts the int back into a byte
				data[randomIndex]=(byte)flippedInt;
			}
		}

		getDatagramPacket().setData(data);
		return getDatagramPacket();
	}

	
	public DatagramPacket looseDatagramPacket(){
		return null;
	}
	
	public DatagramPacket delayDatagramPacket(){
		try {
			TimeUnit.MILLISECONDS.wait((long)delayTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packet;
	}

	
	public DatagramPacket passDatagramPacket(){
		return packet;
	}

	
	public DatagramPacket filter(DatagramPacket datagram){
		loadDatagramPacket(datagram);
		double sample = randomNum.nextDouble();
		if(sample <= getSuccessProb()){
			successPackets++;

			return passDatagramPacket();
		} else if( sample <= getSuccessProb() + getCorruptProb()){
			packetsCorrupt++;

			return corruptDatagramPacket();
			
		} else if( sample <= getSuccessProb() + getCorruptProb() + getDelayProb()){
			packetsDelayed++;
			
			return delayDatagramPacket();
		} else{
			packetsLost++;

			return looseDatagramPacket();
		}
	}

	
	public double getLossProb() {
		return packetLossProb;
	}

	
	public void setLossProb(double packetLossProb) {
		this.packetLossProb = packetLossProb;
	}

	
	public double getCorruptProb() {
				
		return corruptedPacketProb;
	}

	
	public void setCorruptProb(double corruptedPacketProb) {
		this.corruptedPacketProb = corruptedPacketProb;
	}
	
	public double getDelayProb() {
		
		return delayedPacketProb;
	}

	
	public void setDelayProb(double delayedPacketProb) {
		this.delayedPacketProb = delayedPacketProb;
	}

	
	public double getSuccessProb() {
		return successProb;
	}

	
	public void setSuccessProb(double successProb) {
		this.successProb = successProb;
	}
	
	public double getDelayTime() {
		
		return delayTime;
	}

	
	public void setDelayTime(double time) {
		this.delayTime = time;
	}


}