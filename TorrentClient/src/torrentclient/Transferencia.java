/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import java.net.*;
/**
 *
 * @author J Guadalupe Canales
 */


public class Transferencia extends Thread{
    private int Tipo; //Si es de envío o recibido 
    private String Archivo; //Debe ser la ruta completa
    private String IP2Connect;
    private int PIN;
    private int PortDefault=4400;
    private int pieces;
    private int Inicio;
    private int Final;
    
    
public Transferencia (String IP2Connect,int Tipo,String Archivo, int Piezas, int PIN, int Inicio, int Final) {//Constructor
        this.Tipo=Tipo;
        this.Archivo=Archivo;
        this.IP2Connect=IP2Connect;
        //this.PIn2Connect=PIn2Connect;
        this.pieces=Piezas;
        this.PIN=PIN;
        this.Inicio=Inicio;
        this.Final=Final;
    }

    public void Enviar(String ip, int puerto, String Archivo,int Inicio, int Final){
       int iterador=0;
         int pieces=15;
         String FileName;
          try
          { 
                //Obtenemos la dirección a la que nos vamos a conectar
            InetAddress direccion = InetAddress.getByName(IP2Connect);    
            
            //while (iterador<pieces){
              for (iterador=Inicio;iterador<=Final;iterador++){
                Socket socket = new Socket(direccion,puerto);          
                socket.setSoTimeout( 2000 );
                socket.setKeepAlive( true );
        
                DataOutputStream dos = new DataOutputStream( socket.getOutputStream());

                System.out.println("Ciclo:"+iterador);
                FileName=Archivo+"."+iterador+".bin";
                // Creamos el archivo que vamos a enviar
                File archivo = new File(FileName);      
                // Obtenemos el tamaño del archivo
                int tamanioArchivo = ( int )archivo.length(); 
                System.out.println("Tamaño a enviar:"+tamanioArchivo);
                // Creamos el flujo de salida, este tipo de flujo nos permite 
                // hacer la escritura de diferentes tipos de datos tales como
                // Strings, boolean, caracteres y la familia de enteros, etc.

                dos.writeUTF(archivo.getName());

                // Enviamos el tamaño del archivo
                dos.writeInt( tamanioArchivo );

                // Creamos flujo de entrada para realizar la lectura del archivo en bytes
                FileInputStream fis = new FileInputStream( FileName );
                BufferedInputStream bis=new BufferedInputStream(fis);
                
                // Creamos el flujo de salida para enviar los datos del archivo en bytes
                BufferedOutputStream bos = new BufferedOutputStream( socket.getOutputStream());

                // Creamos un array de tipo byte con el tamaño del archivo 
                byte[] buffer = new byte[ tamanioArchivo ];

                // Leemos el archivo y lo introducimos en el array de bytes 
                bis.read( buffer ); 
                // Realizamos el envio de los bytes que conforman el archivo
                for( int i = 0; i < buffer.length; i++ )
                {
                    bos.write( buffer[ i ] ); 
                }           
                
                System.out.println( "Archivo Enviado: "+archivo.getName() );        
                //iterador++;
                if (iterador==pieces){
                    System.out.println("Transferencia terminada");
                    //dos.close();
                }
                bis.close();
                bos.close();
                socket.close();
                             
           }
            //dos.close();
            //socket.close(); 
           }catch( IOException e )
          {
            System.out.println(e.getMessage());
          }
    }
    
    public void Recibir(ServerSocket servidor,int pieces,String Archivo, int Inicio, int Final){
           int iterador=0;
           Socket cliente;
           String PathBase=null;
           pieces=15;
           Boolean Recibiendo=true;
           String MyAddress=null;
           try {
               InetAddress inetAddress = InetAddress.getLocalHost();
               MyAddress=inetAddress.getHostAddress();
            } catch (UnknownHostException ex) {
                Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
          System.out.println("Recibiendo en:"+MyAddress);
           DataBase cc=new DataBase();
           
          while( Recibiendo==true )
          {
                try {                 
                       for (iterador=Inicio;iterador<=Final;iterador++){
                            
                            System.out.println("Ciclo:"+iterador);                    
                            
                            cliente = servidor.accept();
                            
                            DataInputStream dis = new DataInputStream( cliente.getInputStream());
                            
                            String nombreArchivo = dis.readUTF().toString();
                            // Obtenemos el tamaño del archivo
                            int tam = dis.readInt();
                            System.out.println("Tamaño a recibir:"+tam);
                            System.out.println( "Recibiendo archivo "+nombreArchivo );
                            // Creamos flujo de salida, este flujo nos sirve para
                            // indicar donde guardaremos el archivo
                            PathBase=new File("").getAbsolutePath();
                            PathBase=PathBase+"\\Recibido\\";
                            System.out.println("Escribiendo:"+PathBase+nombreArchivo);
                            
                            FileOutputStream fos = new FileOutputStream(PathBase+nombreArchivo);
                            BufferedOutputStream out = new BufferedOutputStream( fos );
                            BufferedInputStream in = new BufferedInputStream( cliente.getInputStream());
                                // Creamos el array de bytes para leer los datos del archivo
                                byte[] buffer = new byte[ tam ];
                                // Obtenemos el archivo mediante la lectura de bytes enviados

                                for( int i = 0; i < buffer.length; i++ )
                                {
                                    buffer[ i ] = ( byte )in.read( );
                                }
                                // Escribimos el archivo
                                out.write( buffer );                        
                                // Cerramos flujos
                                out.flush();
                                in.close();
                                out.close();
                                //out.close();
                                cliente.close();                       

                            System.out.println( "Archivo Recibido "+nombreArchivo );
                            int Id=cc.getIdPeer(MyAddress);
                            cc.PesoPieza(Id,iterador,Archivo);
                            int Progreso=cc.Progreso(Id,Archivo);
                            System.out.println("----------Progreso:"+Progreso);
                            
                            if (iterador==pieces || iterador==Final){
                                System.out.println("Transferencia terminada");
                                Recibiendo=false;
                                cliente.isClosed();
                                
                            }
                            
                        }
                }catch (IOException ex) {
                    System.out.println(ex.getMessage());
                    Logger.getLogger(Transferencia.class.getName()).log(Level.SEVERE, null, ex);
                }                
       }    
        
            int ContadorPiezas=0;
            int i=0;
            for (i=0;i<=pieces;i++){
                File f = new File(PathBase+"Joya.mp4."+ContadorPiezas+".bin"); 
                        if (f.exists()){
                        System.out.println("Existe el archivo: "+PathBase+"Joya.mp4."+ContadorPiezas+".bin"); 
                        ContadorPiezas++;
                        }               
                        else{
                        System.out.println("Does not Exists");
                        }
                         
             } 
            if (ContadorPiezas==pieces){
                System.out.println("El archivo está completo");
                Fragmentacion frg= new Fragmentacion();
                frg.unirPartes(PathBase+"Joya.mp4", pieces);
            }
        }
    
 
    @Override
    public void run() {
    String IP;
    int PIn;
    System.out.println("---------------------------Modo transferencia-----------------------------------");
    
    switch(Tipo){
    case 1://Para recibir
        System.out.println("Recibiendo en:"+PIN); 
        {
            System.out.println("Inicio: "+Inicio+" Final: "+Final);
            try {
                ServerSocket servidor = new ServerSocket(PIN);
                Recibir(servidor,pieces,Archivo,Inicio,Final);
            } catch (IOException ex) {
                Logger.getLogger(Transferencia.class.getName()).log(Level.SEVERE, null, ex);
            }

        }        
    break;
    
    case 2://Para enviar
        System.out.println("Enviando en"+PIN);
        System.out.println("Inicio: "+Inicio+ "Final:"+Final);
        Enviar(IP2Connect,PIN,Archivo,Inicio,Final);
    break; 
    }  

    }   
    
    
}
