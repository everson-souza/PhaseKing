import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;
import org.json.*;
public class MulticastPeer{
    static int idProcesso, valor, f = 0, phase = 0, tieBreaker = 0;
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
    public static void setMajority(Integer majority){
        MulticastPeer.majority = majority;
    }    
    public static void setDatagrama(Integer mult, Integer majority) {
        MulticastPeer.mult = mult;
        MulticastPeer.majority = majority;
    }
    
    public static MulticastSocket getSocket(){
        return MulticastPeer.s;
    }
    
    public static void setSocket(MulticastSocket s) {
        MulticastPeer.s = s;        
    }
    
    public static int getTieBreaker(){
        return MulticastPeer.tieBreaker;
    }
    
    public static void setTieBreaker(int tie) {
        MulticastPeer.tieBreaker = tie;
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
                setPhase(0);
                setTieBreaker(0);
                
                new Thread(t1).start();            
                new Thread(t2).start();
                new Thread(t3).start();
                
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
                while(true && getPhase() < 2){
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
                            p = msg_recebida.getInt("p");       
                            setMajority(m);                            
                            System.out.println("MAIORIA: "+m);
                            setPhase(p);
                            
                        }else if(msg_recebida.has("i") && msg_recebida.has("v")){
                            i = msg_recebida.getInt("i");
                            v = msg_recebida.getInt("v");
                            System.out.println("Id: " + i+ " Valor: "+v);                                
                            vetor[j] = v; 

                            //Verifica majority e maioria
                            if (j == 4){
                                int idProcesso = getIdProcesso();
                                int phase = getPhase();    
                                Integer majority = getMajority();     
                                System.out.println("ID: "+ idProcesso+" Phase: "+phase+" Maioria:"+majority);
                                if (idProcesso == phase){
                                    for(int k=0; k<vetor.length;k++) {
                                        if (vetor[k] == 0) 
                                            zero++;
                                        if (vetor[k]==1)
                                            um++;
                                    }   
                                    if (zero>um){                                          
                                        mult = zero;
                                    }else if (zero < um){                                            
                                        mult = um; 
                                    }
                                    if (mult> ((vetor.length)/2+f)){                                                                            
                                        if (zero>um){
                                            majority = 0;
                                        }else if (zero < um){
                                            majority = 1;
                                        }
                                    }
                                    else {
                                        majority = getTieBreaker(); //Empate
                                        mult = -1;
                                    }
                                    System.out.println("Majority: "+majority+" Mult:"+mult+" ID: "+A );
                                    setDatagrama(mult, majority);   
                                }
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
                while(true && getPhase() < 2){                    
                    int idProcesso = getIdProcesso();
                    int phase = getPhase();    
                    Integer majority = getMajority();     
                    System.out.println("ID: "+ idProcesso+" Phase: "+phase+" Maioria:"+majority);
                    if (idProcesso == phase && majority != null){
                        JSONObject message = new JSONObject();
                        message.put("m", getMajority());  
                        message.put("p", getPhase()+1);  

                        //Converte para um byte array e envia
                        byte [] m = message.toString().getBytes();                        
                        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);               

                        s.send(messageOut);
                    }
                }
            } catch (Exception e){}
 
        }
    };
    
    private static Runnable t3 = new Runnable() {
        
        public void run() {
            try{
                group = InetAddress.getByName("239.255.255.25");
                s = new MulticastSocket(6789);
                s.joinGroup(group);
                int phaseAtual = getPhase();
                while(true && getPhase() < 2){
                    JSONObject message = new JSONObject();
                    message.put("i", getIdProcesso());
                    message.put("v", valor);                    

                    //Converte para um byte array e envia
                    byte [] m = message.toString().getBytes();                        
                    DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);               

                    s.send(messageOut);	
                    while(getPhase()!=phaseAtual);
                    setDatagrama(null, null);
                    setPhase(phaseAtual);
                    wait(2000);
                    
                }
            } catch (Exception e){}
 
        }
    };
    
}