/**
* @file UDPDiscovery.java
* @author Sebastian Hopp
* @date 2012/07/26
*
* Einfache Klasse zum finden eines Komunikationspartner
* via UDP und Braodcast.
*
*/
package de.h_da.nfc_inventur_app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;


public class UDPDiscovery {
        static DatagramSocket SOCKET;   //Socket
        String m_remoteIP;              
        int m_remotePort;               // Service Port
        int m_localPort;                // Service Port
        String Ich = "";
        String PacketContent;
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
                
    }
    
    /** Legt den Lokalen Endpunkt fest.
*
* 
*/
    private void setLocalEndPoint( int LocalPort){
            
            m_localPort = LocalPort;
            try{
                  SOCKET = new DatagramSocket(m_localPort);
                  SOCKET.setBroadcast(true);
                  
            }catch (Exception e) {

            }
          
        }
                
           /** Legt den Endpunkt des Partner fest.
*
* 
*/         
                
        private void setRemoteEndPoint(String RemoteIP, int RemotePort){
            
            m_remoteIP = RemoteIP;
            m_remotePort = RemotePort;
            
        }

/** Sendet eine Nachricht an die Broadcast Adresse (255.255.255.255)
*
*/
        private void SendDataToNetwork(String cmd) { 
            try {
                
                String messageStr= cmd;

                int server_port = m_remotePort;

                InetAddress remoteIP = InetAddress.getByName(m_remoteIP);

                int msg_length = messageStr.length();

                byte[] message = messageStr.getBytes();

                DatagramPacket p = new DatagramPacket(message, msg_length,remoteIP,server_port);

                SOCKET.send(p);
                
            } catch (Exception e) {
                
                

            }
        }
        
        
/** Funktion die auf ein Datenpaket wartet.
*   
* @return Liefert die IP Addresse der Datenbank oder ein
*         Timeout  bzw Exception zur�ck.
*
*/
        
        public String recive(){
            
                byte[] message = new byte[1500];
                DatagramPacket p = new DatagramPacket(message, message.length);
                String text= "";
                try{
                    
                    SOCKET.setSoTimeout(5000);
                    SOCKET.receive(p);
                    PacketContent = new String(message, 0, p.getLength());
                    if (PacketContent.contains("ICH"))
                            {
                                
                                  
                    SOCKET.setSoTimeout(5000);
                    SOCKET.receive(p);
                        
                            }
                            
                    int s = 1;
                    text = p.getAddress().toString();
                    text = text.substring(s);
                    
                }catch(java.net.SocketTimeoutException e){
                    
                    text = "TIMEOUT;;" + e.toString();
                    
                }catch(Exception e){
                           
                    text = "EXCEPTION;;" + e.toString();
                    
                }
                
             return text;
        }
        
            /** Sendet eine Broadcast auf dem angegebenen Port
*mit dem inhal "ICH"
* 
*/
        
        public String Bootstrap(int ServicePort){
                        
            setLocalEndPoint(ServicePort);
            setRemoteEndPoint("255.255.255.255", ServicePort);
            SendDataToNetwork("ICH");
            String ret = recive();
            close();
            return ret;
            
        }
        
        //List<Object> list1 = new ArrayList<Object>();

            /** Schlie�t das Socket wieder.
* 
*/

        public void close(){
            
            SOCKET.close();
            
        }
   
}
