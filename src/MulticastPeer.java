import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;
import org.json.*;
public class MulticastPeer{
    static int idProcesso, valor, phase = 0;
    static Integer mult, majority;
    static MulticastSocket s =null;
    static InetAddress group;
    public static int getIdProcesso(){
        return MulticastPeer.idProcesso;
    }
    
    public static void setIdProcesso(int idProcesso) {
        MulticastPeer.idProcesso = idProcesso;
    }
    
    public static int getPhase(){
        return MulticastPeer.phase;
    }
    
    public static void setPhase(int phase) {
        MulticastPeer.phase = phase;
    }
    
    public static Integer getMult(){
        return MulticastPeer.mult;
    }
    public static Integer getMajority(){
        return MulticastPeer.majority;
    }
    
    public static void setDatagrama(int mult, int majority) {
        MulticastPeer.mult = mult;
        MulticastPeer.majority = majority;
    }
    
    public static MulticastSocket getSocket(){
        return MulticastPeer.s;
    }
    
    public static void setSocket(MulticastSocket s) {
        MulticastPeer.s = s;        
    }
    
    public static void main(String args[]){ 
        // args give message contents and destination multicast group (e.g. "228.5.6.7")   
        try {
                group = InetAddress.getByName("239.255.255.25");
                s = new MulticastSocket(6789);
                s.joinGroup(group); 		
                //Lê do teclado o id do processo e a mensagem                
                                  
                Scanner leID = new Scanner(System.in);               
                System.out.println("Digite o ID do processo:");
                
                setIdProcesso(leID.nextInt());
                
                Scanner leValor = new Scanner(System.in);
                System.out.println("Digite a mensagem:");
                valor = leValor.nextByte();
                
                new Thread(t1).start();            
                new Thread(t2).start();
                
                setPhase(0);
                JSONObject message = new JSONObject();
                message.put("i", getIdProcesso());
                message.put("v", valor);                    

                //Converte para um byte array e envia
                byte [] m = message.toString().getBytes();                        
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);               

                s.send(messageOut);	

        }catch (Exception e){
            System.out.println("Socket: " + e.getMessage());
        }finally {
            //if(s != null) s.close();
        }
    }
    
    private static Runnable t1 = new Runnable() {
        DataInputStream in;
        DataOutputStream out;
        MulticastSocket clientSocket;
       // Vetor de respostas
        int vetor[] = new int[5];
        int processos = 5;
        int zero, um, mult, majority;
        
        public void run() {
            try{
                while(true){
                    int A = getIdProcesso();
                    clientSocket = getSocket();
                    byte[] buffer = new byte[1000];
                    Integer i, v, p, m;
                    for(int j=0; j< 5;j++) {		// get messages from others in group

                        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

                        clientSocket.receive(messageIn);
                        JSONObject msg_recebida = null;
                        msg_recebida = new JSONObject(new String(messageIn.getData())); 				
                        System.out.println(msg_recebida);
                        if (msg_recebida.has("m")) {
                            m = msg_recebida.getInt("m");
                            System.out.println("MAIORIA: "+m);
                        }else if(msg_recebida.has("i") && msg_recebida.has("v")){
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
                                    majority = 0;
                                    mult = zero;
                                }else if (zero < um){
                                    majority = 1;
                                    mult = um; 
                                }else{
                                    majority = -1; //Empate
                                    mult = -1;
                                }
                                System.out.println("Majority: "+majority+" Mult:"+mult+" ID: "+A );
                                setDatagrama(mult, majority);                    
                            }                       
                        }                                               
                    }
                }
                
            } catch (Exception e){}
        }
    };
    
    private static Runnable t2 = new Runnable() {
        
        public void run() {
            try{
                group = InetAddress.getByName("239.255.255.25");
                s = new MulticastSocket(6789);
                s.joinGroup(group);
                while(true){                    
                    int idProcesso = getIdProcesso();
                    int phase = getPhase();    
                    Integer majority = getMajority();     
                    System.out.println("ID: "+ idProcesso+" Phase: "+phase+" Maioria:"+majority);
                    if (idProcesso == phase && majority != null){
                        JSONObject message = new JSONObject();
                        message.put("m", getMajority());                    

                        //Converte para um byte array e envia
                        byte [] m = message.toString().getBytes();                        
                        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);               

                        s.send(messageOut);
                    }
                }
            } catch (Exception e){}
 
        }
    };
    
}