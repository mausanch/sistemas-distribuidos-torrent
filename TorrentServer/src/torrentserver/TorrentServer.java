/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentserver;

/**
 *
 * @author J Guadalupe Canales
 */
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.*;
import java.util.Scanner;
import java.util.logging.*;
public class TorrentServer {
    public static void main(String[] arg) throws IOException {

        int Conexiones=0;
        int DefaultPort=4445;        
        //Se declara un ServerSocket para recibir conexiones            
        ServerSocket Tracker = null;
        JSONObject json = new JSONObject();
        //Scanner input = new Scanner(System.in);
        try {   
                    Tracker = new ServerSocket(DefaultPort);//Puerto del Tracker
                    System.out.println("Tracker Listo, Esperando conexiones");
                    try {
                        InetAddress inetAddress = InetAddress.getLocalHost();
                        String MyAddress=inetAddress.getHostAddress();
                        System.out.println("Información del tracker: "+MyAddress+":"+DefaultPort);
                        } catch (UnknownHostException ex) {
                        Logger.getLogger(TorrentServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    
                    while (true){ 
                        Socket Peer;
                        if (Conexiones<10){
                            //Aquí también registraremos el nuevo peer en la base de datos                            
                            Peer=Tracker.accept();
                            System.out.println("Nueva Conexión: "+Peer);
                            Conexiones++;
                            ((TrackerHilo) new TrackerHilo(Peer, Conexiones)).start();         
                        }else{
                            System.out.println("No se permiten más conexiones :c");
                            break;
                        }
                    }
        } catch (IOException ex) {
            System.err.println("No puedo escuchar en el puerto:"+DefaultPort);
            Logger.getLogger(TorrentServer.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
}
