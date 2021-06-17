/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author J Guadalupe Canales
 */
public class PeerMultiHilos implements Runnable{
    Thread t;
    public ServerSocket MessageSocket;
    JSONObject ToPeer;   
    
    public PeerMultiHilos(){
    this.t = new Thread(this,"Hilo 1");
    t.start();   
    }
    public PeerMultiHilos(ServerSocket MessageSocket, JSONObject ToPeer){
    this.t = new Thread(this,"Hilo 2");
    this.MessageSocket=MessageSocket;
    this.ToPeer=ToPeer;
    t.start();    
    }
    
    public void run(){        
        
        Socket P2Peer;
        while (true){
            try {
                P2Peer=MessageSocket.accept();
                System.out.println("Nueva Conexión: "+P2Peer);
                ((HiloPeerConnection) new HiloPeerConnection(P2Peer)).start();        
            } catch (IOException ex) {
                Logger.getLogger(PeerMultiHilos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
}
