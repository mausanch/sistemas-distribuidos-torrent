/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentserver;

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
            Integer i=0; 
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
                Tablas.put(Ip,String.valueOf(PIn));
                i++;
                System.out.format("%s, %s\n", Ip, PIn);
            }
            try{
            Json2Peer.put("Peers",Tablas);
            Json2Peer.put("Qty",i);
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
            Integer i=0; 
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
                i++;
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

public void RegistrarSeeder(String arg[]){ //arg[0]=IP, arg[1]=Id_Archivo,arg[2]=Peso
    
        try{
            String Procedure="call RegistrarSeeder(?,?,?)"; 
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
            System.out.println("\nSeeder Registrado");  
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

public void RegistrarLeacher (String IP, String Id_Archivo, int Peso){ //arg[0]=IP, arg[1]=Id_Archivo,arg[2]=Peso
        System.out.println("Registrando Leacher");
        try{
            String Procedure="call RegistrarArchivoVacio(?,?,?)"; 
            Integer i=1;
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            CallableStatement stmt;
            stmt=cnn.prepareCall(Procedure);
            stmt.setString(1,IP);//Correspo
            stmt.setString(2,Id_Archivo);
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

public void PesoPieza(String Peer, String Pieza, String Archivo) {
        try {
            int peso = 1;
            String Query = ("UPDATE PeerPieza SET Peso = " + peso + " WHERE Id_Peer = '" + Peer + "' and Id_Pieza = '" + Pieza + "'and Id_Archivo = '" + Archivo + "';");
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
        System.out.println("..........................Buscando Archivo...........................");
        try{
            
            String Table="Archivos"; 
            String Query="select Id_Archivo from "+Table+" where Id_Archivo='"+arg+"';"; 
            System.out.println(Query);
            DataBase cnn1= new DataBase();  
            cnn=cnn1.conexion(cnn);
            Statement stmt = cnn.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            System.out.println("Los resultados son:");
            String Archivo="N/A";
            while (rs.next()){ //Leemos los valores de la consulta
                Archivo=rs.getString("Id_Archivo");
                i++;            
            }
            System.out.println("Se encontró el archivo: "+Archivo);
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



}
