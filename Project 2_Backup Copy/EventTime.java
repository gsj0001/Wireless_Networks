
public class EventTime {
	
	double expirationTime;
	int eventID;

	public EventTime(int event_ID, double expiration_Time){
		expirationTime = expiration_Time;
		eventID = event_ID;
	}

	
	public double getExpirationTime() {
		return expirationTime;
	}

	
	public void setExpirationTime(double expiration_Time) {
		expirationTime = expiration_Time;
	}

	public void decrementExpirationTime(){
	expirationTime--;	
	}
	
	
	public int getEventID() {
		return eventID;
	}

	
	public void setEventID(int event_ID) {
		eventID = event_ID;
	}
	
	
	public String toString(){
		return "Event ID: "+eventID + "\t"+"Event has: " +expirationTime;
	}
}