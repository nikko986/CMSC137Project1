import java.net.* ;
import java.util.HashMap;
import java.util.Random;

public class Server{
    
     private final static int SERVER_PORT = 1112;
	 private final static int CLIENT_PORT = 1113;
     private final static int PACKETSIZE = 100 ;
	 private final static int WINDOWSIZE = 8;
	 private final static String hostAdd="127.0.0.1";
	 private static int startSeq=0;
	 static boolean continued = true;
	 
	 private static HashMap<String,Integer> dataToBePassed = new HashMap<String,Integer>();
	 private static HashMap<String,String> dataToBePassed2 = new HashMap<String,String>();
	 private static HashMap<Integer,String> message = new HashMap<Integer,String>();
	 
	 
	 public static void main(String args[]){
	   	
	      try{
	      	
	    	 InetAddress host = InetAddress.getByName(hostAdd) ;
	         DatagramSocket socket = new DatagramSocket(SERVER_PORT); 

	         System.out.println("Server ready.\n") ;

	         for( ;; ){ // 3 way handshake
	         
	        	
	            DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;
	            socket.receive( packet ) ;
	            //create then receive packet for blocking
	            
	            String dataReceived = new String(packet.getData());
	            
	            System.out.println(dataReceived);
	            
	            dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
	            String[] num = dataReceived.split(", ");
	            for(int y=0;y<num.length;y++){
	            	String[] temp = num[y].split("=");
	            	dataToBePassed.put(temp[0],Integer.parseInt(temp[1]));
	            }
	           
	           int syn =dataToBePassed.get("SYN");
	           int ack = dataToBePassed.get("ACK");
	           dataToBePassed.put("ACK",ack+1);
	  
	           int isn =dataToBePassed.get("ISN");
	           dataToBePassed.put("ACK NO",isn+1);
	           dataToBePassed.put("ISN",5000);
	            
	           if(syn==0 && ack==1){
	        	   startSeq =(dataToBePassed.get("SEQ NO")+1);
	        	   System.out.println("Connection established to "+packet.getPort());
	        	   break;
	           }
	            
	           // Return the packet to the sender
	           byte [] data = dataToBePassed.toString().getBytes() ;
		       DatagramPacket response = new DatagramPacket( data, data.length, packet.getAddress(), packet.getPort() ) ;

		       socket.send( response ) ; 
		         
	        } //end of 3 way handshake 
	         
	        //Start receiving data from the client
	         DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;
	         for(;;){//sending data
	        	 	
	        	 	socket.receive( packet ) ;
		            String dataReceived = new String(packet.getData());
		            dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
		            String[] num = dataReceived.split(", ");
		            
		            for(int y=0;y<num.length;y++){
		            	String[] temp = num[y].split("=");
		            	dataToBePassed2.put(temp[0],temp[1]);
		            }
		            
		            int fin = Integer.parseInt(dataToBePassed2.get("FIN"));
		            if(fin == 1){
		            	continued=false;
		            	break;
		            }
		            message.put(Integer.parseInt(dataToBePassed2.get("SYN")), dataToBePassed2.get("DATA"));
		            dataToBePassed2.put("ACKF", Integer.toString(1));
		            byte [] data = dataToBePassed2.toString().getBytes() ;
			        DatagramPacket response = new DatagramPacket( data, data.length, packet.getAddress(), packet.getPort() ) ;

			         // Send it			 
			         socket.send( response ) ;
			         
			         //Print out the message from the client
			         Thread timerThread = new Thread(new Runnable() {
			        	  int cnt=0;
			        	  public void run() {
			        		  while(continued==true){
			        			  cnt++;
			        			  if(cnt%2==0){
			        				 try{
			        				  for(int key : message.keySet()) {
			        					   System.out.print(message.get(key));
			        					}
			        				 }catch(Exception m){}
			        				 System.out.println("");
			        			  }
			        			  try {
									Thread.sleep(10);
								} catch (InterruptedException e) {					
								}
			        		  }
			        	  }
			          });
			          timerThread.start();
	         }
	         
	         //Printout the last message formed
	         try{
				  for(int key : message.keySet()) {
					   System.out.print(message.get(key));
					}
				 }catch(Exception m){}
				 System.out.println("");
				 
	         
	         //End Connection
	         	int currentseq = Integer.parseInt(dataToBePassed2.get("SYN"));
	         	currentseq++;
	            dataToBePassed2.put("ACKF", Integer.toString(1));
	            byte [] data = dataToBePassed2.toString().getBytes() ;
		        DatagramPacket response = new DatagramPacket( data, data.length, packet.getAddress(), packet.getPort()) ;
		         // Send it
		        socket.send( response ) ;
		       
		      //2nd sending of finish bit
		        dataToBePassed2.clear();
		        dataToBePassed2.put("SRC",Integer.toString(CLIENT_PORT));
		        dataToBePassed2.put("DST",Integer.toString(SERVER_PORT));
		        dataToBePassed2.put("SYN",Integer.toString(0));
		        dataToBePassed2.put("ACK",Integer.toString(0));
		        dataToBePassed2.put("SYNF",Integer.toString(1));
		        dataToBePassed2.put("ACKF",Integer.toString(0));
		        dataToBePassed2.put("FIN",Integer.toString(1));
		        dataToBePassed2.put("CS",Integer.toString(0));
		        dataToBePassed2.put("WS",Integer.toString(WINDOWSIZE));
		        dataToBePassed2.put("DATA",null);		         
		    
		        data = dataToBePassed2.toString().getBytes() ;
		        packet = new DatagramPacket( data, data.length, host, CLIENT_PORT ) ;
		        
		         socket.send( packet ) ;
		         
		         while(true){
		         socket.receive( packet ) ;		         
		         String dataReceived = new String(packet.getData());
		         System.out.println(dataReceived);
		         dataReceived=dataReceived.substring(1, dataReceived.length()-(PACKETSIZE-dataReceived.lastIndexOf('}')));
		         String[] temporary = dataReceived.split(", ");
		            
		         for(int y=0;y<temporary.length;y++){
		            	String[] temp = temporary[y].split("=");
		            	dataToBePassed2.put(temp[0],temp[1]);
		            }
		          if(Integer.parseInt(dataToBePassed2.get("ACKF"))==1){
		        	  break;
		          }
		         }
		         
		         
		         System.out.print("Closing in 10 secs...");
		          Thread timerThread = new Thread(new Runnable() {
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
		          timerThread.start();
	     }
	     catch( Exception e )
	     {
	        System.out.println( e ) ;
	     }
	   }
}
