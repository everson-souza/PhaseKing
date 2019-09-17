import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;
import org.json.*;
public class MulticastPeer{
    static int idProcesso, valor, phase = 0;
    static int mult, majority = -1;
    public static int getIdProcesso(){
        return MulticastPeer.idProcesso;
    }
    
    public static void setIdProcesso(int idProcesso) {
        MulticastPeer.idProcesso = idProcesso;
    }
    
    public static void setDatagrama(int mult, int majority) {
        MulticastPeer.mult = mult;
        MulticastPeer.majority = majority;
    }
    
    public static void main(String args[]){ 
        // args give message contents and destination multicast group (e.g. "228.5.6.7")
        MulticastSocket s =null;
        
        try {
                InetAddress group = InetAddress.getByName(args[1]);
                s = new MulticastSocket(6789);
                s.joinGroup(group); 		
                //Lê do teclado o id do processo e a mensagem                
                                  
                Scanner leID = new Scanner(System.in);               
                System.out.println("Digite o ID do processo:");
                
                setIdProcesso(leID.nextInt());
                
                Scanner leValor = new Scanner(System.in);
                System.out.println("Digite a mensagem:");
                valor = leValor.nextByte();
                
                //for (int i=0; i< 2; i++){
                    JSONObject message = new JSONObject();
                    message.put("i", getIdProcesso());
                    message.put("v", valor);                    

                    String msg = message.toString();

                    //Converte para um byte array e envia
                    byte [] m = msg.getBytes();                        
                    DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);               
                    
                    s.send(messageOut);	
                //\\\\\\\\\\\\\}		
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
   // Vetor de respostas
    int vetor[] = new int[5];
    int processos = 5;
    int zero, um, mult, majority;
    MulticastPeer multicastPeer = new MulticastPeer(); 
    int A = multicastPeer.getIdProcesso();

    public Connection (MulticastSocket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            byte[] buffer = new byte[1000];
            int i, v, p;
            for(int j=0; j< 6;j++) {		// get messages from others in group

                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

                clientSocket.receive(messageIn);
                JSONObject msg_recebida = new JSONObject(new String(messageIn.getData())); 				
                i = msg_recebida.getInt("i");
                v = msg_recebida.getInt("v");
                
                System.out.println("Id: " + i+ " Valor: "+v);                                
                vetor[j] = v; 

                //Verifica majority e maioria
                if (j == 4){
                    for(int k=0; k<vetor.length;k++) {
                        if (vetor[k] == 0) 
                            zero++;
                        if (vetor[k]==1)
                            um++;
                    }
                    if (zero>um){
                        mult = 0;
                        majority = zero;
                    }else if (zero < um){
                        mult = 1;
                        majority = um; 
                    }else{
                        mult = -1; //Empate
                        majority = -1;
                    }
                    System.out.println("Majority: "+majority+" Mult:"+mult+" ID: "+A );
                    multicastPeer.setDatagrama(mult, majority);
                }
            }                        
            this.start();
         } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }
}
class SegundoRound extends Thread {
    DataInputStream in;
    DataOutputStream out;
    MulticastSocket clientSocket;
   // Vetor de respostas
    int vetor[] = new int[5];
    int processos = 5;
    int zero, um, mult, majority;
    MulticastPeer multicastPeer = new MulticastPeer(); 
    int A = multicastPeer.getIdProcesso();

    public SegundoRound (MulticastSocket aClientSocket) {
        try {
            clientSocket = aClientSocket;
                                             
            this.start();
         } 
        catch(Exception e) {System.out.println("Connection:"+e.getMessage());}
    }
}