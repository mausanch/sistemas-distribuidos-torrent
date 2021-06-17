/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentclient;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.logging.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


public class HiloPeerConnection extends Thread {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private DataOutputStream PeticionesO;
    private DataInputStream PeticionesI;
    private int idSessio;
    private JSONObject Json;
    private JSONObject Json_Message;
    private int Mensaje_Tipo;
    int Tipo=-1;
    private int DefaultPort=4400;
    //protected DataOutputStream dos;
    //protected DataInputStream dis;   
    
    //Torrent Manual
    public HiloPeerConnection(Socket Peer,JSONObject Json, JSONObject Json_Message) {
        this.socket = Peer;
        this.Json=Json;
        this.Json_Message=Json_Message;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Torrent autoamtico
    public HiloPeerConnection(Socket Peer) throws IOException {
        this.socket = Peer;
        //this.Tipo=3;
        if(Json_Message==null){
            System.out.println("No tienes mensajes");
            //ServerSocket SocketEntrada=new ServerSocket(DefaultPort);
       try {
            PeticionesO = new DataOutputStream(Peer.getOutputStream());
            PeticionesI = new DataInputStream(Peer.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
            PeticionesI=new DataInputStream(Peer.getInputStream());          
            String line = PeticionesI.readUTF();
            System.out.println("\nEk mensaje recibido es:"+line);     
            Json_Message= new JSONObject(line);
            
        }
                
    }
    public void desconnectar() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
  public HashMap<String, String> jsonToMap(String t) throws JSONException {      
        Gson gson= new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        HashMap<String,String> mymap = new Gson().fromJson(t, HashMap.class);
        return mymap;
    }
    
    @Override
    public void run() {

        String IP=null;
        int PIn=-1;
        String Archivo=null;
        String [] args=new String[5];
        int Peso=-1;
        String Ruta;
        Integer Piezas;
        DataOutputStream Message;
        String Json_Message_Serializable;
        JSONObject RespuestaDeTracker= new JSONObject();
        JSONObject Json_To_Peer= new JSONObject();
        Integer MyPort;
        InetAddress inetAddress;
        int Solicitud=0;
        int Inicio = 0;
        int Final = 0;
        int Pz2Peer = 0;
                 
        HashMap<String, String> PeersConFragmentos= new HashMap<>(); 
        Boolean ExistsPeer=false;
           

        MyPort=socket.getLocalPort();
        System.out.println("\nEste puerto es:"+MyPort);
        System.out.println("\nRecibiendo paquete");
        if (Json_Message!=null){
        try {
            /*Existen 3 tipos de mensajes
            1.Registro de PEER (IP_Nuevo,PIn_Nuevo,archivo,Peso) - No responde
            2.Solicitud de archivo(IP_Destino,PIn_Destino,Archivo_Nombre) -Responde con IP Destino
            3.Respuesta de archivo (IP_Fuente,PIn_Fuente)
            JSON Tendrá 4 argumentos (Mensaje_Tipo,IP,PIn,Archivo,Peso)
             */
            System.out.println(Json_Message);
            Tipo=Json_Message.getInt("Tipo");
            IP=Json_Message.getString("IP");
            PIn=Json_Message.getInt("PIn");
            Archivo=Json_Message.getString("Archivo");
            Peso=Json_Message.getInt("Peso");
            Ruta=Json_Message.getString("Ruta");
            Piezas=Json_Message.getInt("Piezas");
            Inicio=Json_Message.getInt("Inicio");
            Final=Json_Message.getInt("Final");
            switch(Tipo) {
                case 1:
//---------------------------------------------Petición de archivo---------------------------------\\  
                Message= new DataOutputStream(socket.getOutputStream());
                Json_Message_Serializable = Json_Message.toString();
                System.out.println("\nMensaje enviado al Tracker:"+Json_Message.toString());
                
                Message.writeUTF(Json_Message_Serializable); //Mensaje a TRACKER

                dis=new DataInputStream(socket.getInputStream());
                
                String line = dis.readUTF();                 //Respuesta de TRACKER
                
                System.out.println("\nLa respuesta de tracker es:"+line);                             
                PeersConFragmentos=jsonToMap(line);
                int PeersQty=0;
                Iterator<Map.Entry<String, String>> Conteo = PeersConFragmentos.entrySet().iterator();//se usa un iterador para poder eliminar los peers de la lista
                 while(Conteo.hasNext()){
                 Map.Entry<String, String> entry = Conteo.next();
                 PeersQty++;
                 }                 
                System.out.println("La cantidad de peers son: "+PeersQty);
                Iterator<Map.Entry<String, String>> it = PeersConFragmentos.entrySet().iterator();//se usa un iterador para poder eliminar los peers de la lista
                 while(it.hasNext()) {
//-------------------------------------------Este WHILE permite crear una conexión a múltiples PEERS----------------------------------------\\\\                     
                    Map.Entry<String, String> entry = it.next();
                    String IP2Connect = entry.getKey();
                    String PIn2ConnectS = entry.getValue();
                    int PIn2Connect= Integer.parseInt(PIn2ConnectS);
                    System.out.println("Host a conectar:");
                    System.out.println(IP2Connect+":"+ PIn2Connect);
                                            
                    Pz2Peer = Piezas/PeersQty;
                    
                    if (Final >= Piezas){
                        int valor = (Piezas-1);
                        Final = valor;
                        System.out.println("Piezas sobrantes, El nuevo Final es:"+Final);
                     }
                    else{
                        if (Final==0 && PeersQty==1){
                        Final =+ Piezas-1;
                        }
                        else{
                        Final =+ Pz2Peer;
                        }      
                    }
                    try{
                        //Envíamos un mensaje de tipo 3 que corresponde a una solicitud
                        Tipo=3;
                        Json_To_Peer.put("Tipo",Tipo);
                        try {
                            inetAddress = InetAddress.getLocalHost();
                            String MyAddress=inetAddress.getHostAddress();
                            Json_To_Peer.put("IP",MyAddress);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Json_To_Peer.put("PIn",DefaultPort+Solicitud);
                        Json_To_Peer.put("Archivo",Archivo);

                        Json_To_Peer.put("Peso",Peso);
                        Json_To_Peer.put("Ruta",Ruta);
                        Json_To_Peer.put("Piezas",Piezas);
                        Json_To_Peer.put("Inicio",Inicio);
                        Json_To_Peer.put("Final",Final);
                        }catch(JSONException e){
                        System.out.println("Error al escribir el Json"+e.getMessage());
                         }
                  
//---------------------------Nos comunicamos con el PEER por medio de su puerto de mensajes------------------------\\
                        System.out.println("Conectandome a PEER: "+IP2Connect+":"+PIn2Connect);
                        Socket Peer = new Socket(IP2Connect,PIn2Connect);
                        Message= new DataOutputStream(Peer.getOutputStream());
                        Json_Message_Serializable = Json_To_Peer.toString(); 
                        System.out.println("\nEl mensaje a enviar es:"+Json_To_Peer.toString());
                        String StringDefault=null;

                        Message.writeUTF(Json_Message_Serializable);//Le mandamos el mensaje

                        ((Transferencia) new Transferencia(StringDefault,1,Archivo,0,DefaultPort+Solicitud,Inicio,Final)).start();                     
                        System.out.println("Solicitando en:"+DefaultPort+Solicitud);
                        Solicitud=+5;//Incrementamos en 5 para el nuevo puerto, si es que existe otro host
                        Inicio =+ Pz2Peer+1;
                        Final = Final+Pz2Peer+1;
                        System.out.println("Inicio:"+Inicio);
                        System.out.println("Final:"+Final);

                 }             
                break;

                case 2:
//---------------------------------------------Registrando un Archivo---------------------------------\\  
                Message= new DataOutputStream(socket.getOutputStream());
                Json_Message_Serializable = Json_Message.toString();
                System.out.println("\nEl mensaje enviado es:"+Json_Message.toString());
                Message.writeUTF(Json_Message_Serializable); 
                System.out.println("Archivo Registrado"); 
                break;              
                case 3:
//---------------------------------------------Creando conexión PEER TO PEER---------------------------------\\                          
                System.out.println("He recibido una petición de archivo;");
                try {
                        inetAddress = InetAddress.getLocalHost();
                        String MyAddress=inetAddress.getHostAddress();
                        System.out.println("Enviando respuesta desde desde: "+MyAddress+":"+MyPort);
                    } catch (UnknownHostException ex) {
                       Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
//---------------------------------------------Abrimos nuestro ServerSocket---------------------------------\\                          
//---------------------------------------------Para empezar la transferencia----------------------------------\\                          
                
                ((Transferencia) new Transferencia(IP,2,Archivo,Piezas,PIn,Inicio,Final)).start();                    
                                            //IP2Connect,Tipo,Archivo,Piezas,Solicitud )
                break;
                
                case 4:
//---------------------------------------------Recuperamos el progreso-----------------------------------------\\                          
                                  try{
                        //Envíamos un mensaje de tipo 3 que corresponde a una solicitud
                        Tipo=3;
                        Json_To_Peer.put("Tipo",Tipo);
                        try {
                            inetAddress = InetAddress.getLocalHost();
                            String MyAddress=inetAddress.getHostAddress();
                            Json_To_Peer.put("IP",MyAddress);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Json_To_Peer.put("PIn",DefaultPort+Solicitud);
                        Json_To_Peer.put("Archivo",Archivo);

                        Json_To_Peer.put("Peso",Peso);
                        Json_To_Peer.put("Ruta",Ruta);
                        Json_To_Peer.put("Piezas",Piezas);
                        Json_To_Peer.put("Inicio",Inicio);
                        Json_To_Peer.put("Final",Final);
                        }catch(JSONException e){
                        System.out.println("Error al escribir el Json"+e.getMessage());
                         }               
//---------------------------Nos comunicamos con el PEER por medio de su puerto de mensajes------------------------\\
                        System.out.println("Conectandome a PEER: "+IP+":"+PIn);
                        Socket Peer = new Socket(IP,PIn);
                        Message= new DataOutputStream(Peer.getOutputStream());
                        Json_Message_Serializable = Json_To_Peer.toString(); 
                        System.out.println("\nEl mensaje a enviar es:"+Json_To_Peer.toString());
                        String StringDefault=null;

                        Message.writeUTF(Json_Message_Serializable);//Le mandamos el mensaje

                        ((Transferencia) new Transferencia(StringDefault,1,Archivo,0,DefaultPort+Solicitud,Inicio,Final)).start();                     
                        //Solicitud=+5;//Incrementamos en 5 para el nuevo puerto, si es que existe otro host
  
                break;
                default:
                 System.out.println("Error en el tipo de mensaje");
            }                     
        } catch (IOException ex) {
            Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
           //desconnectar();
    }
}