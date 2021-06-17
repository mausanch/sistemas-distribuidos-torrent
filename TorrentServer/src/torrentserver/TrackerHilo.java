/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentserver;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.*;
import java.util.StringTokenizer; 
import org.json.JSONObject;

public class TrackerHilo extends Thread {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private int idSessio;
   
    public TrackerHilo(Socket Peer, int id) {
        this.socket = Peer;
        this.idSessio = id;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(TrackerHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void desconnectar() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TrackerHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
        int Tipo=-1;
        String IP=null;
        Integer PIn=-1;
        String Archivo=null;
        String Ruta;
        Integer Piezas;
        String [] args=new String[5];
        int Peso=-1;
        DataBase cc=new DataBase();
        Boolean ExistsPeer;
        Boolean ExistsArchivo;
        //Gson Respuesta2Peer;
        String Respuesta2Peer;
        DataOutputStream Message;
        String Json_Message_Serializable;
        try {                  
                            DataInputStream dis;
                            dis=new DataInputStream(socket.getInputStream());
                            String line = dis.readUTF();
                            System.out.println("\nNuevo Mensaje :"+line);
                           //String line = "asd";
                            //if(line != null){
                                /*Existen 3 tipos de mensajes
                                1.Registro de PEER (IP_Nuevo,PIn_Nuevo,archivo,Peso) - No responde
                                2.Solicitud de archivo(IP_Destino,PIn_Destino,Archivo_Nombre) -Responde con IP Destino
                                3.Respuesta de archivo (IP_Fuente,PIn_Fuente)
                                JSON Tendrá 4 argumentos (Mensaje_Tipo,IP,PIn,Archivo,Peso)
                                */
                                JSONObject obj = new JSONObject(line);
                                Tipo= obj.getInt("Tipo");
                                IP= obj.getString("IP");
                                PIn=obj.getInt("PIn");
                                Archivo=obj.getString("Archivo");
                                Peso=obj.getInt("Peso");
                                Ruta=obj.getString("Ruta");
                                Piezas=obj.getInt("Piezas");
                                System.out.println("Recibí una petición de tipo:"+Tipo);
                                System.out.println("El mensaje es:"+IP+PIn+Archivo+Peso);
                                
                                switch(Tipo) {
                                    case 1://Solicito un archivo 
                                        System.out.println("Llegó una solicitud de archivo");
                                        if (ExistsPeer=cc.ConsultarPeer(IP)==false){  
                                            System.out.println("Consultando");
                                            args[0]=IP;
                                            args[1]=Integer.toString(PIn);
                                            cc.RegistrarPeer(args);
                                            System.out.println("-----------------------------");                   
                                         }                        
                                         System.out.println("Solicitud de un Peer Existente");
                                        cc.RegistrarLeacher(IP,Archivo,Peso);
                                         Respuesta2Peer=cc.GetPeersConFragmetos(Archivo);
                                         //Vamos a retornar las IPS que tienen el archivo (si hay)
                                         Message= new DataOutputStream(socket.getOutputStream());
                                         Json_Message_Serializable = Respuesta2Peer.toString();
                                         System.out.println("\nLa respuesta es:"+Respuesta2Peer.toString());
                                         Message.writeUTF(Json_Message_Serializable);
                                         //Tipo=-1;                                 
                                     break;
                                case 2://Registro un archivo
                                    
                                    System.out.println("Llegó un nuevo registro");                               
                                    if (cc.ConsultarPeer(IP)==false){ 
                                        args[0]=IP;
                                        args[1]=Integer.toString(PIn);
                                        cc.RegistrarPeer(args);
                                   }
                                        args[0]=IP;
                                        args[1]=Archivo;
                                        args[2]=Integer.toString(Peso);
                                ///Agregar Condicional que verifiqué que el Archivo esté registrado, si no, registramos un SEEDER
                                    if (cc.ConsultarArchivo(args[1])==false){
                                    cc.RegistrarArchivo(args);
                                    }
                                    else{
                                        System.out.println("Ya existe un archivo llamado:"+args[1]);
                                        System.out.println("\nRegistrando un seeder");
                                        cc.RegistrarSeeder(args);
                                    }
                                                                           
                                   
                                break;
                                default:
                                    System.out.println("Error en el tipo de mensaje");
                              }                    
                            //}
        } catch (IOException ex) {
            Logger.getLogger(TrackerHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        //desconnectar();
    }
}