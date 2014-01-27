/**
* @file UDPInterface.java
* @author Sebastian Hopp
* @date 2012/07/26
*
* Einfache Klasse zur Komunikation mit einem Partner
* via UDP.
*
*/
package de.h_da.nfc_inventur_app;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;

public class UDPInterface {
    
        static DatagramSocket SOCKET;   //Socket
        String m_remoteIP;              //Remote Adresse
        int m_remotePort;               //Remote Port 
        int m_localPort;                //Lokaler Port
        String SendText;
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
                
        
        
    }
    
        /** Legt den Lokalen Endpunkt fest und 
*           bindet das Socket.
* 
*/
    
 public void setLocalEndPoint( int LocalPort){
            
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
                
        public void setRemoteEndPoint(String RemoteIP, int RemotePort){
            
            m_remoteIP = RemoteIP;
            m_remotePort = RemotePort;
            
        }

/** Sendet eine Nachricht an den vorher festgelegten Partner (RemoteEndPoint)
*
*/

        public void SendDataToNetwork(String cmd) { 
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
* @return liefert die Info �ber einen Tag oder eine OK bei einer Transaktion zur�ck.
*         
*
*/
        public String recive(){
            
                byte[] message = new byte[1500];
                DatagramPacket p = new DatagramPacket(message, message.length);
                String text= "";
                try{
                    
                    SOCKET.setSoTimeout(5000);
                   
                    SOCKET.receive(p);
                    
                    text = new String(message, 0, p.getLength());
                    text = "RECIVED;;" + text;
                    
                }catch(java.net.SocketTimeoutException e){
                    
                    text = "TIMEOUT;;" + e.toString();
                    
                }catch(Exception e){
                           
                    text = "EXCEPTION;;" + e.toString();
                    
                }
                
             return text;
        }
        
        /** Fragt die Datenbankanwendung nach einem Tag
*   
* @return Liefert die Infos zu einem Tag aus der Datenbank oder ein
*         Timeout  bzw Exception zur�ck.
*
*/
        public String getTagInfo(String TagID){
            
            SendDataToNetwork("GETINFO;;" + TagID);
            return recive();
            
        }
        
        //List<Object> list1 = new ArrayList<Object>();
        
        /** Funktion die eine Reihe von Tags ann die Datenbank sendet und diese dann abwickeln l�sst.
*   
* @return Liefert Timeout  bzw Exception zur�ck.
*
*/
        public String sendTransaction(List<String> TagIDList){
            
            int i=0;
            SendText = "TRANSACT;;";
            
            
            while (i != TagIDList.size()){
                
                SendText = SendText + "," + TagIDList.get(i);
                i++;
            }
            
            SendDataToNetwork(SendText);
            return recive();
            
        }
        
        public String bla(){
        
        return SendText;
        
        }
        
                //List<Object> list1 = new ArrayList<Object>();

            /** Schlie�t das Socket wieder.
* 
*/

        public void close(){
            
            SOCKET.close();
            
        }
       
}
