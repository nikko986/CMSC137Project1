import java.net.* ;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Client {
	
	private final static int PACKETSIZE = 100 ;
	private final static int SERVER_PORT = 1112;
	private final static int CLIENT_PORT = 1113;
	
	private final static String hostAdd="127.0.0.1";
	private final static int WINDOWSIZE = 8;
	
	private static HashMap<String,Integer> dataToBePassed = new HashMap<String,Integer>();
	private static HashMap<String,String> dataToBePassed2 = new HashMap<String,String>();
	private static HashMap<String,String> dataThatReceived = new HashMap<String,String>();
	private static LinkedList<DatagramPacket> dropPack = new LinkedList<DatagramPacket>();
	
	private static String sentence ="This is a sample string.";
	//private static String sentence ="The quick brown fox jumps over the lazy dog";
	private static int[] dropPackets = {0, 25, 50, 75};
   static DatagramSocket socket = null ;
	   
	 public static void main( String args[] )
	   {
	   	
	      try{	
	         //	3way-hand shake connection
	         InetAddress host = InetAddress.getByName( hostAdd) ;

	         // Construct the socket
	         socket = new DatagramSocket(CLIENT_PORT) ;
	         dataToBePassed.put("SYN",1);
	         dataToBePassed.put("ACK",0);
	         dataToBePassed.put("ISN",2000);
	         
	         // Construct the datagram packet
	         byte [] data = dataToBePassed.toString().getBytes() ;
	         DatagramPacket packet = new DatagramPacket( data, data.length, host, SERVER_PORT ) ;

	         socket.send( packet ) ;

	         socket.setSoTimeout( 4000 ) ;

	         // Prepare the packet for receive
	         byte[] receivedData = new byte[PACKETSIZE];
	         final DatagramPacket receivePacket = new DatagramPacket(receivedData,receivedData.length);
	         
	         socket.receive( receivePacket ) ;
	         
	         String dataReceived = new String(receivePacket.getData());
	         System.out.println(dataReceived);
	
	         dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
	
	         String[] num = dataReceived.split(", ");
	
	            for(int y=0;y<num.length;y++){
	            	String[] temp = num[y].split("=");
	            	dataToBePassed.put(temp[0],Integer.parseInt(temp[1]));
	            }
	            
		     dataToBePassed.put("SYN",0);
		     int ack_no =dataToBePassed.get("ACK NO");
	
		     int isn =dataToBePassed.get("ISN");
		     dataToBePassed.put("ACK NO",isn+1);
		     dataToBePassed.put("SEQ NO",ack_no);
		     dataToBePassed.remove("ISN");
	
		     int seqNum = (dataToBePassed.get("SEQ NO")+1);
		     data = dataToBePassed.toString().getBytes() ;
	         packet = new DatagramPacket( data, data.length, host, SERVER_PORT ) ;
		
             socket.send( packet ) ;
	         socket.setSoTimeout( 4000 ) ;
	         
	         int sub=0;
	         for(;sub<sentence.length();seqNum++){
	        	 dataToBePassed2.put("SRC",Integer.toString(CLIENT_PORT));
		         dataToBePassed2.put("DST",Integer.toString(SERVER_PORT));
		         dataToBePassed2.put("SYN",Integer.toString(seqNum));
		         dataToBePassed2.put("ACK",Integer.toString(0));
		         dataToBePassed2.put("SYNF",Integer.toString(1));
		         dataToBePassed2.put("ACKF",Integer.toString(0));
		         dataToBePassed2.put("FIN",Integer.toString(0));
		         dataToBePassed2.put("CS",Integer.toString(0));
		         dataToBePassed2.put("WS",Integer.toString(WINDOWSIZE));
		         if(sub+WINDOWSIZE>sentence.length()){
		        	 dataToBePassed2.put("DATA",sentence.substring(sub,sentence.length()));
		         }
		         else{
		         dataToBePassed2.put("DATA",sentence.substring(sub,sub+WINDOWSIZE));
		         }
		         sub=sub+8;
	        	 data = dataToBePassed2.toString().getBytes() ;
		         packet = new DatagramPacket( data, data.length, host, SERVER_PORT ) ;

		         if(random()==true){
		        	 System.out.println(seqNum);
		        	 socket.send( packet ) ;
		        	 socket.receive( receivePacket ) ;
		        	 dataReceived = new String(receivePacket.getData());
		        	 System.out.println(dataReceived);
		        	 dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
		        	 String[] temporary = dataReceived.split(", ");
		        	 for(int y=0;y<temporary.length;y++){
		            	String[] temp = temporary[y].split("=");
		            	dataThatReceived.put(temp[0],temp[1]);
		            }
		           
		         }
		         else{
		        	 dropPack.add(packet);
		         }
	        }
	        	Thread timerThread = new Thread(new Runnable() {
		        	  int cnt=0;
		        	  public void run() {
		        		  while(!dropPack.isEmpty()){
		        			  cnt++;
		        			  if(cnt%4==0){
		        			  if(random()==true){
		      	        		
		        				  try{
		        				socket.send(dropPack.getFirst());
		      	        		dropPack.removeFirst();
		      	        		socket.receive( receivePacket ) ;
		      		        	 String dataReceived = new String(receivePacket.getData());
		      		        	 System.out.println(dataReceived);
		      		        	 dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
		      		        	 String[] temporary = dataReceived.split(", ");
		      		        	 for(int y=0;y<temporary.length;y++){
		      		            	String[] temp = temporary[y].split("=");
		      		            	dataThatReceived.put(temp[0],temp[1]);
		      		            }
		      		           
		        				  }catch(Exception o){}
		      	        	}
		        			  
		        			  try {
								Thread.sleep(10);
							} catch (InterruptedException e) {					
							}
		        			  }
		        		  }
		        	  }
		          });
		          timerThread.start();
	        
	        while(!dropPack.isEmpty()){
	        	
	        }

	        //End Connection
	        dataToBePassed2.clear();
	        dataToBePassed2.put("SRC",Integer.toString(CLIENT_PORT));
	        dataToBePassed2.put("DST",Integer.toString(SERVER_PORT));
	        dataToBePassed2.put("SYN",Integer.toString(seqNum));
	        dataToBePassed2.put("ACK",Integer.toString(0));
	        dataToBePassed2.put("SYNF",Integer.toString(1));
	        dataToBePassed2.put("ACKF",Integer.toString(0));
	        dataToBePassed2.put("FIN",Integer.toString(1));
	        dataToBePassed2.put("CS",Integer.toString(0));
	        dataToBePassed2.put("WS",Integer.toString(WINDOWSIZE));
	        dataToBePassed2.put("DATA",null);
	         
	         data = dataToBePassed2.toString().getBytes() ;
	         packet = new DatagramPacket( data, data.length, host, SERVER_PORT ) ;

	         socket.send( packet ) ;
	        
	         socket.receive( receivePacket ) ;		         
	         dataReceived = new String(receivePacket.getData());
	         System.out.println(dataReceived);
	         dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
	         String[] temporary = dataReceived.split(", ");
	            for(int y=0;y<temporary.length;y++){
	            	String[] temp = temporary[y].split("=");
	            	dataThatReceived.put(temp[0],temp[1]);
	            }
	          
	          if(Integer.parseInt(dataThatReceived.get("ACKF"))==1){
	        	  while(true){
	        		  socket.receive( receivePacket ) ;		         
	     	         dataReceived = new String(receivePacket.getData());
	     	         System.out.println(dataReceived);
	     	         dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
	     	         String[] tempor = dataReceived.split(", ");
	     	            for(int y=0;y<tempor.length;y++){
	     	            	String[] temp = tempor[y].split("=");
	     	            	dataThatReceived.put(temp[0],temp[1]);
	     	            }
	     	          if(Integer.parseInt(dataThatReceived.get("FIN"))==1){
	     	        	 dataThatReceived.put("ACKF",Integer.toString(1));
	     	        	 data = dataThatReceived.toString().getBytes() ;
	    		         packet = new DatagramPacket( data, data.length, host, SERVER_PORT ) ;
	    		         socket.send(packet ) ;
	    		         break;
	     	          }
	        	  }
	          }
	          System.out.print("10 seconds before closing");
	          Thread timerThread1 = new Thread(new Runnable() {
	        	  int cnt=0;
	        	  public void run() {
	        		  while(cnt<11){
	        			  cnt++;
	        			  System.out.print(".");
	        			  try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {					
						}
	        		  }
	        	  }
	          });
	          timerThread1.start();
	      }
	      catch( Exception e )
	      {
	         System.out.println( e ) ;
	      }
	      finally
	      {
	         if( socket != null )
	            socket.close() ;
	      }
	   }
	   
	   public static boolean random(){//randomizing 0%,25%,50%,75% in dropping packets
			 
			 Random ran = new Random();
			 int temp = ran.nextInt(4);
			 int var = dropPackets[temp];
			 int random = ran.nextInt(101);
			 if(random<=var){
				 return false;
			 }
			 else{
			 return true;
			 }
		 }
}

