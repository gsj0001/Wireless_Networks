import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class ClientSegAndReassembly {

	ArrayList<Fragment> listOfFrags;
	ErrorDetector ed = new ErrorDetector();
	byte[] reassembledData;
	
	
	public ClientSegAndReassembly(){
		 listOfFrags = new ArrayList<Fragment>();
	}

	
	public byte[] getData() {
		return reassembledData;
	}

	public void setFragmentList(ArrayList<Fragment> fragList){
		listOfFrags = fragList;
	}

	
	public void addFragment(Fragment frag){
		listOfFrags.add(frag);
	}
	
	
	public void assembleData(){
		ByteArrayOutputStream completeData = new ByteArrayOutputStream();

		
		while(!listOfFrags.isEmpty()){
			Fragment fragsFromList = listOfFrags.remove(0);

			try {
				completeData.write(fragsFromList.getDataBytes());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		reassembledData = completeData.toByteArray();

		try {
			completeData.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}