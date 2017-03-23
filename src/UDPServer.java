import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;



class UDPServer {

	
	private static String fileLen;
	private static String typeOfFile;
	private static int code;
	private final static String http = "HTTP/1.0";
	
	public static void main(String args[]) throws Exception {
		
		DatagramSocket serverSocket = new DatagramSocket(10078);
    
		byte[] receiveData = new byte[128];
		byte[] emptyDataSet = new byte[128];
		byte[] sendData  = new byte[128];
		
		
		while(true) {
            
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			
			String sentence = new String(receivePacket.getData());

			receiveData = null;
			receiveData = emptyDataSet.clone();
			String[] requestFromClient = sentence.split("[ ]");
			
			boolean fileExist;
			boolean methodTokenValid;
			String htmlDocumentBuffer = readFile(requestFromClient[1]);
			
			
			if(requestFromClient.length !=3){
				
				fileLen = "0";
				typeOfFile = "None";
				code = 400;
			}

			else {
				
				methodTokenValid = isMethodTokenValid(requestFromClient[0]);
				fileExist = checkFileExistence(requestFromClient[1]);

				if (methodTokenValid && fileExist){
					HeaderString(Integer.toString(FileLengthCalc(requestFromClient[1])), 200, GenerateFileType(requestFromClient[1]));
					htmlDocumentBuffer = readFile(requestFromClient[1]);
				} 
				if(methodTokenValid && !fileExist){
					fileLen = "0";
					typeOfFile = "None";
					code = 404;
				}
				if(!methodTokenValid){
					//HeaderString(Integer.toString(FileLengthCalc(ERRORFILE)), 400, GenerateFileType(ERRORFILE));
					fileLen = "0";
					typeOfFile = "None";
					code = 400;
				}
			} 

			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String headerInformation = http + " " + code + " "+statusString(code) + "\r\n" +
			"Content-Type: " +typeOfFile+"\r\n"+"Content-Length: " + fileLen+"\r\n";
			
			String headerAndData = new String(headerInformation + "\r\n"+ htmlDocumentBuffer);
			byte[] headerAndDataByteArray = headerAndData.getBytes();
			

			DatagramPacket sendPacket;
			int start = 0;
			int end = 128;
			for(int i = 0; i < (double)headerAndDataByteArray.length/128 ; i++){
				System.out.println(i);
				byte[] dataInformation = Arrays.copyOfRange(headerAndDataByteArray, start, end);
				System.out.println(new String(dataInformation));
				sendData = dataInformation;
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				start = end;
				end = end+128;
			}
			sendData = new byte[1];
			sendData[0]=0;
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

		}
	}
	

	
	private  static boolean checkFileExistence(String fileNameAndPath){
		if(fileNameAndPath == null){
			return false;
		}
		File file = new File(System.getProperty("user.dir") + "/"+fileNameAndPath);
		return file.exists();
	}


	private  static boolean isMethodTokenValid(String requestMethodToken){
		String[] MethodTokenList = {"GET"};
		for(String methodToken : MethodTokenList){
			if(requestMethodToken.toUpperCase().equalsIgnoreCase(methodToken)){
				return true;
			}	
		}
		return false;
	}

	
	private static int FileLengthCalc(String fileName){
		if(fileName == null){
			return 0;
		}
		byte[] fileContent = {0};
		int lengthOfByte;
		
		try{
			File file = new File(System.getProperty("user.dir") + "/"+fileName);
			FileInputStream fileInputStream = new FileInputStream(file);
			lengthOfByte = (int)file.length();
			fileContent = new byte[lengthOfByte];
			fileInputStream.read(fileContent, 0, lengthOfByte);
			
			return lengthOfByte;
			
		} catch(FileNotFoundException ex){
			return -1;
		} catch(IOException e){
			return -1;
		} 

	}
	

	private static String readFile(String fileName) throws IOException{
		if(fileName == null){
			return "";
		}
		byte[] fileContent = {0};
		int lengthOfByte;
		
		try{
			File file = new File(System.getProperty("user.dir") + "/"+fileName);
			FileInputStream fileInputStream = new FileInputStream(file);
			lengthOfByte = (int)file.length();
			fileContent = new byte[lengthOfByte];
			fileInputStream.read(fileContent, 0, lengthOfByte);
			
			try{
				return new String(fileContent,"UTF-8");
			} catch(UnsupportedEncodingException e){
				return "UTF IS NOT SUPPORTED";
			}
			
		} catch(FileNotFoundException ex){
			return "File not found.";
		} catch(IOException e){
			return "IOException.";
		}  
		
	}

	
	public static String GenerateFileType(String fileName){
		String fileType="text/plain";
		if (fileName.endsWith(".html") || fileName.endsWith(".htm"))
			fileType="text/html";
		else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
			fileType="image/jpeg";
		else if (fileName.endsWith(".gif"))
			fileType="image/gif";
		else if (fileName.endsWith(".class"))
			fileType="application/octet-stream";
		return fileType;
	}
	
	

	public static void HeaderString(String contentLength, int statusCode, String contentType){
		fileLen = contentLength;
		code = statusCode;
		typeOfFile = contentType;
	}

	public static String statusString(int codeIn){
		switch(codeIn){
		case 200:
			return "Document Follows";
		case 404:
			return "File Not Found.";
		case 400:
			return "Bad Request";
		default:
			return "Invalid Status Code";
		}
	}
	
	
	public int getcode() {
		return code;
	}

	public void setcode(int codeIn) {
		UDPServer.code = codeIn;
	}

}





