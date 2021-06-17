/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentclient;

//package com.mycompany.torrent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import javax.swing.JOptionPane;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBase {
    Connection cnn=null;    
    /*Local
    private String NameDataBase="Torrent";
    private String Usuario="WebApplication";
    private String Password="123456";
    private String URL="jdbc:mysql://localhost:3306/"+NameDataBase+"?useUnicode=true&use"
                       +"JDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&"
                       +"serverTimezone=UTC";
    */
    /*NUBE*/
    private String NameDataBase="Torrent";
    private String Usuario="BDTorrent";
    private String Password="HermanosConvoy";
    private String URL="jdbc:mysql://database-bt.ctazi4dvrctb.us-east-1.rds.amazonaws.com:3306/"+NameDataBase+"?useUnicode=true&use"
                       +"JDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&"
                       +"serverTimezone=UTC";
    
    public Connection conexion(Connection cnn){
        try{   
            Class.forName("com.mysql.cj.jdbc.Driver");
            //cnn=DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql?useTimezone=true&serverTimezone=UTC", "WebApplication", "123456");
            cnn=DriverManager.getConnection(URL,Usuario,Password);            
            //JOptionPane.showMessageDialog(null, "Se ha conectado");
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "No se ha podido conectar"+" "+ e.getMessage());
        }
        return cnn;
    }  
       
    public String Consulta(String arg){ 
        try{
            String Table="PeersConFragmetos"; 
            String Query="select IP,PIn from " +Table+ " where Ip='"+arg+"';"; 
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            System.out.println("Los siguientes Peers tienen el archivo "+arg+"los fragmentos:");
            while (rs.next()){ //Leemos los valores de la consulta
                String Ip = rs.getString("IP");
                int PIn=rs.getInt("PIn");
            /*Introducir de acuerdo a la estructura de la tabla*/    
                //int Id_Peer = rs.getInt("id");
                //String firstName = rs.getString("first_name");
                //String lastName = rs.getString("last_name");
                //boolean isAdmin = rs.getBoolean("is_admin");
                //int numPoints = rs.getInt("num_points");
                System.out.format("%s, %d\n", Ip, PIn);              
            }
            stmt.close();
            
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }       
    return null;
    }

        
public String GetPeersConFragmetos(String arg){ 
        System.out.println("Buscando fragmentos......");
        Gson gson = new Gson(); 
        try{
            String Table="PeersConFragmentos"; 
            String Query="select Ip,PIn from " +Table+ " where Archivo='"+arg+"';" ; 
            Integer i=1; 
            HashMap<String, String> Tablas = new HashMap<>();  
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            System.out.println("Los siguientes Peers tienen fragmentos del archivo: "+arg);
            JSONObject Json2Peer = new JSONObject();
            while (rs.next()){ //Leemos los valores de la consulta
                String Ip = rs.getString("IP");
                int PIn=rs.getInt("PIn");
                //Tablas.put("IP",Ip);
                //Tablas.put("Pin",String.valueOf(PIn));
                Tablas.put(Ip,String.valueOf(PIn));
                System.out.format("%s, %s\n", Ip, PIn);
            }
            try{
            Json2Peer.put("Peers",Tablas);
            }catch(JSONException e){
                System.out.println("Error al escribir el Json"+e.getMessage());
            }
            
            String json = gson.toJson(Tablas); 
            String GsonSerializado=json.toString();
            System.out.println("El mensaje en formato Gson serializado es:"+GsonSerializado);
            stmt.close();
            
            return json; 
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }      
    return null;
    }
        
public Map<String, Integer> GetPeersSinFragmetos(String arg){ 
        try{
            String Table="PeersConFragmentosFaltantes"; 
            String Query="select Ip,Pin from" +Table+ "where Archivo='"+arg+"'" ; 
            Integer i=1; 
            Map<String, Integer> Tablas = new HashMap<>();  
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            System.out.println("Los siguientes Peers tienen el archivo"+arg+"los fragmentos:");
            while (rs.next()){ //Leemos los valores de la consulta
                String Ip = rs.getString("IP");
                int PIn=rs.getInt("PIn");
                Tablas.put(Ip,PIn);
                System.out.format("%s, %d\n", Ip, PIn);
            }
            stmt.close();
            return Tablas;
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/
        }       
    return null;
    }


/*Registrar SEEDER*/
public void RegistrarArchivo(String arg[]){ //arg[0]=IP, arg[1]=Id_Archivo,arg[2]=Peso
    
        try{
            String Procedure="call RegistrarArchivo(?,?,?)"; 
            Integer i=1;
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            CallableStatement stmt;
            stmt=cnn.prepareCall(Procedure);
            stmt.setString(1,arg[0]);//Correspo
            stmt.setString(2,arg[1]);
            Integer Peso=Integer.parseInt(arg[2]);
            stmt.setInt(3,Peso);
            stmt.execute();
            System.out.println("Archivo Registrado");  
            //stmt.close();
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }      
    }

public void RegistrarLeacher (String arg[]){ //arg[0]=IP, arg[1]=Id_Archivo,arg[2]=Peso
        System.out.println("Registrando Leacher");
        try{
            String Procedure="call RegistrarArchivoVacio(?,?,?)"; 
            Integer i=1;
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            CallableStatement stmt;
            stmt=cnn.prepareCall(Procedure);
            stmt.setString(1,arg[0]);//Correspo
            stmt.setString(2,arg[1]);
            Integer Peso=Integer.parseInt(arg[2]);
            stmt.setInt(3,Peso);
            stmt.execute();
            System.out.println("success");  
            //stmt.close();
        }
        catch(SQLException  ex) {            
            System.out.println("---SQLException: " + ex.getMessage());
            System.out.println("---SQLState: " + ex.getSQLState());
            System.out.println("---VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }      
    }

public void RegistrarPeer(String arg[]){ //arg[0]=IP, arg[1]=Puerto 
    
        System.out.println("Registrando Peer");
        System.out.println("Valores a registrar:"+arg[0]+arg[1]);
        try{
            String Query="insert into Peers values (null,?,?);"; 
            Integer i=1;
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            PreparedStatement stmt;
            stmt=cnn.prepareStatement(Query);
            stmt.setString(1,arg[0]);//Corresponde a la IP
            Integer Peso=Integer.parseInt(arg[1]);
            stmt.setInt(2,Peso);//Corresponde al Peso
            stmt.execute();
            System.out.println("Nuevo PEER Registrado");  
            stmt.close();
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }      
    }
public boolean ConsultarPeer(String arg){ //arg[0]=IP, arg[1]=Puerto 
      
        int i=0;
        System.out.println("..........................Buscando PEER............");
        try{
            
            String Table="Peers"; 
            String Query="select Ip,PuertoEntrada from "+Table+" where Ip='"+arg+"';"; 
            System.out.println(Query);
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            System.out.println("Los resultados son:");
            while (rs.next()){ //Leemos los valores de la consulta
                String IP=rs.getString("Ip");
                int Puerto=rs.getInt("PuertoEntrada");
                i++;            
            }
            System.out.println("Cantidad de PEERS:"+i);
            stmt.close();
            cnn.close();
            if(i>0){ return true;}
            else{return false;}
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } 
        finally{
       
        }
     return false;
    }

public void PesoPieza(int Peer, int Pieza, String Archivo) {
        try {
            int peso = 1;
            String Query = ("UPDATE PeerPieza SET Peso = " + peso + " WHERE Id_Peer = '" + Peer + "' and Id_Pieza = '" + Pieza + "' and Id_Archivo = '" + Archivo + "';");
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            PreparedStatement stmt;
            stmt=cnn.prepareStatement(Query);
            stmt.execute();
            stmt.close();
            cnn.close();
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } 
        finally{
       
        }
    }

public boolean ConsultarArchivo(String arg){ //arg[0]=IP, arg[1]=Puerto 
      
        int i=0;
        System.out.println("..........................Buscando Archivo............");
        try{
            
            String Table="Archivos"; 
            String Query="select Id_Archivo from "+Table+" where Id_Archivo='"+arg+"';"; 
            System.out.println(Query);
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            System.out.println("Los resultados son:");
            while (rs.next()){ //Leemos los valores de la consulta
                String IP=rs.getString("Id_Archivo:");
                i++;            
            }
            stmt.close();
            cnn.close();
            if(i>0){ return true;}
            else{return false;}
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } 
        finally{
       
        }
     return false;
    }

public int Progreso (int Id_Peer, String Id_Archivo){

    int Carga=0;
    try{
            String Table="PeerArchivo"; 
            String Query="select Carga from " +Table+ " where Id_Peer='"+Id_Peer +"' and Id_Archivo='"+Id_Archivo+"';"; 
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            while (rs.next()){ //Leemos los valores de la consulta
               Carga = rs.getInt("Carga");
            }
            stmt.close();
            
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }       
    return Carga;
}



public int getIdPeer (String IP){
    
    int ID=0;
try{
            String Table="Peers"; 
            String Query="select Id_Peer from " +Table+ " where Ip='"+IP+"';"; 
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            while (rs.next()){ //Leemos los valores de la consulta
                ID = rs.getInt("Id_Peer");
            }
            stmt.close();
        }
        catch(SQLException  ex) {            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }       
    return ID;    
}
    public JSONObject RecuperarFragmentos (String IP){
    
    JSONObject Json_To_Peer=new JSONObject();
    ResultSet rs;
    int IdArchivo=0;
    int QtyArchivo=0;
    int QtyPieza=0;
    String[] MisArchivos=new String[10];
    String MiIdPeer="N/A";
    String[][] PiezasFaltantes=new String[10] [100];
    String[] PortAConectar= new String[10];
    String[] IpsAConectar=new String[10];
    int Tipo=4;
    System.out.println("----------Recuperando progreso------------");
        try{
            String RecuperarArchivos="call RecuperarArchivos(?)"; 
            String RecuperarPiezas="select * RecuperarPiezas";
            String ConseguirIps="select *ConseguirIPsConFragmentos";
            Statement stmtConsulta;
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            CallableStatement stmt;
            stmt=cnn.prepareCall(RecuperarArchivos);
            stmt.setString(1,IP);//Correspo
            rs= stmt.executeQuery();
            System.out.println("Archivos encontrados");
            while (rs.next()) {
                MisArchivos[IdArchivo]=rs.getString("Id_Archivo");
                MiIdPeer=rs.getString("Id_Peer");
                System.out.println(MisArchivos[IdArchivo]+MiIdPeer);
                IdArchivo++;
            }
            

            
            while (QtyArchivo<=IdArchivo){
            RecuperarPiezas="select * from RecuperarPiezas where Id_Peer= "+MiIdPeer+" and Id_Archivo='"+MisArchivos[QtyArchivo]+"';";
            stmtConsulta = cnn.createStatement();
            rs=stmtConsulta.executeQuery(RecuperarPiezas);        
                while (rs.next()){
                    PiezasFaltantes[QtyArchivo][QtyPieza]=rs.getString("Id_Pieza");
                    QtyPieza++;
                }
            QtyArchivo++;
            }
            int i=0;
            int j=0;
            int QtyIps = 0;
            int Inicio=0;
            int Final=0;
            int solicitud=0;
            while (i<=QtyArchivo){
                while (j<=QtyPieza){
                    
                    ConseguirIps="select * from ConseguirIPsConFragmentos where Pieza="+PiezasFaltantes[i][j]+" and Archivo='"+MisArchivos[i]+"';";  
                    stmtConsulta=cnn.createStatement();
                    rs=stmtConsulta.executeQuery(ConseguirIps);
                    while (rs.next()){
                        if (QtyIps==0 || rs.getString("IP")!=IpsAConectar[QtyIps-1]){
                           if(QtyIps==0){
                            Inicio=Integer.getInteger(PiezasFaltantes[i][j]);
                        }       
                            IpsAConectar[QtyIps]=rs.getString("IP");
                            PortAConectar[QtyIps]=rs.getString("Puerto");
                            QtyIps++;
                        }    
                    }
                        
                    if (PiezasFaltantes[i][j+1]==PiezasFaltantes[i][j]+1){//Comprobamos la continuidad
                       Final=Integer.getInteger(PiezasFaltantes[i][j]+1);
                    }
                    else{//Aquí enviamos solicitud A PEER
                        
                        Final=Inicio;                       
                        try{
                        //Envíamos un mensaje de tipo 3 que corresponde a una solicitud
                        Tipo=4;
                        Json_To_Peer.put("Tipo",Tipo);
                        Json_To_Peer.put("IP",PortAConectar[QtyIps]);
                        Json_To_Peer.put("PIn",4400+solicitud);
                        Json_To_Peer.put("Archivo",MisArchivos[QtyArchivo]);
                        Json_To_Peer.put("Peso",0);
                        Json_To_Peer.put("Ruta",MisArchivos[QtyArchivo]);
                        Json_To_Peer.put("Piezas",0);
                        Json_To_Peer.put("Inicio",Inicio);
                        Json_To_Peer.put("Final",Final);
                        }catch(JSONException e){
                        System.out.println("Error al escribir el Json"+e.getMessage());
                         }                
                      ((HiloPeerConnection) new HiloPeerConnection(null,Json_To_Peer,Json_To_Peer)).start();
                        
                    solicitud=+5;
                    }
                    
                                                          
                    j++;
                }
                i++;
            }
            
            
            
            
            System.out.println("success");
        }
        catch(SQLException  ex) {            
            System.out.println("---SQLException: " + ex.getMessage());
            System.out.println("---SQLState: " + ex.getSQLState());
            System.out.println("---VendorError: " + ex.getErrorCode());
        }
        finally{
        /*Lo ideal es cerrar la conexión pero aquí omitimos ese paso porque 
         Ocuparemos las consutlas de forma recursiva*/   
        }   
        
    return Json_To_Peer;
}

}
