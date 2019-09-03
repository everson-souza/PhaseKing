import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;
public class MulticastPeer{
    public static void main(String args[]){ 
        // args give message contents and destination multicast group (e.g. "228.5.6.7")
        MulticastSocket s =null;
        try {
                InetAddress group = InetAddress.getByName(args[1]);
                s = new MulticastSocket(6789);
                s.joinGroup(group); 		
                //Lê do teclado o id do processo e a mensagem
                String msg;
                int idProcesso;
                
                Scanner leMsg = new Scanner(System.in);               
                System.out.println("Digite o ID do processo:");
                idProcesso = leMsg.nextInt();
                
                Scanner leId = new Scanner(System.in);
                System.out.println("Digite a mensagem:");
                msg = leId.nextLine();
                                
                //Converte para um byte array e envia
                byte [] m = msg.getBytes();                        
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
                s.send(messageOut);			
                //Cria a thread da classe Connection e passa o socket por parâmetro
                Connection c = new Connection(s);			

        }catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }finally {
            if(s != null) s.close();
        }
    }		      	
}

class Connection extends Thread {
	DataInputStream in;
	DataOutputStream out;
	MulticastSocket clientSocket;
	public Connection (MulticastSocket aClientSocket) {
		try {
			clientSocket = aClientSocket;
			byte[] buffer = new byte[1000];
 			for(int i=0; i< 6;i++) {		// get messages from others in group
 				DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
 				clientSocket.receive(messageIn);
 				System.out.println("Received:" + new String(messageIn.getData()));
  			}
			this.start();
                 } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
	}
//	public void run(){
//		try {			                 // an echo server
//			String data = in.readUTF();	
//                        // read a line of data from the stream
//			out.writeUTF(data);
//		}catch (EOFException e){System.out.println("EOF:"+e.getMessage());
//		} catch(IOException e) {System.out.println("readline:"+e.getMessage());
//		} finally {if(clientSocket != null) clientSocket.close();}
//		
//
//	}
}
