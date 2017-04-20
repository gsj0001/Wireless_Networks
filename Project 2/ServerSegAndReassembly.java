import java.util.ArrayList;
import java.util.Arrays;


public class ServerSegAndReassembly {

	ArrayList<Fragment> listOfFrags = new ArrayList<Fragment>();
	ErrorDetector detectError = new ErrorDetector();
	byte[] dataToSegment;

	public ServerSegAndReassembly(byte[] dataIn){
		dataToSegment = dataIn;
		if(dataToSegment !=null || dataToSegment.length != 0){
			segmentData();
		}
	}

	/**
	 * Using the Error Detector this separates the file into fragments with headers and stores them
	 * in the Servers Fragment List
	 */
	public void segmentData(){
		int start = 0;
		int end = 110;

		for(int i =0; i<(double)dataToSegment.length/110; i++){
			Fragment newFragment = new Fragment(Arrays.copyOfRange(dataToSegment, start, end));
			newFragment.getmHeader().setSequenceID((byte)(i%32));
			if((i+1)<(double)dataToSegment.length/110){
				newFragment.getmHeader().setmEndOfSequence((byte)0);
			} else{
				newFragment.getmHeader().setmEndOfSequence((byte)1);
			}
			addFragment(detectError.generateCheckSum(newFragment));
			start = end;
			end = end + 110;
		}
	}

	
	public boolean hasNext(){
		boolean isNext;
		if(listOfFrags.size() == 0){
			isNext = false;
		}
		else{
			isNext = true;
		}

		return isNext;
	}

	
	public Fragment next(){
		return listOfFrags.remove(0);
	}

	
	public void addFragment(Fragment fragment){
		listOfFrags.add(fragment);
	}
}