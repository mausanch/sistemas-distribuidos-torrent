/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author J Guadalupe Canales
 */
public class TorrentClient {
	static final int PEDAZO_TAM =524288-16;//bytes
	static final int MAX_PETICION = 10;//Numero de conexiones concurrentes
        static final String HASH_ALGORITHM = "MD5";//Identificador
        final int PIECE_LENGTH=524288-16; //
	String id;
	String tracker;
	int puertoTracker;
	int pedazos;
	int ultimo_pedazo;
	String nombre;
	String archivo;
	Boolean[] obtenidos;
	String[] checksum;
        private int DefaultState=3;
        // int CHUNK_SIZE=1;

	//Parsear el .torrent (constructor)
    public JSONObject LecturaTorrent (String torrentPath) throws IOException{ //JSONException 
                System.out.printf("Leyendo .Torrent");
		File file = new File(torrentPath);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
                String array1[]=new String[3];
		if(line != null){		
                    JSONObject obj = new JSONObject(line);
                    nombre= obj.getString("name");
                    pedazos= obj.getInt("pieces");
                    id = obj.getString("id");
                    tracker = obj.getString("tracker");
                    puertoTracker = obj.getInt("puertoTracker");
                    pedazos = obj.getInt("pieces");
                    ultimo_pedazo = obj.getInt("lastPiece");
                    nombre = obj.getString("name");
                    archivo = obj.getString("filepath");
                    System.out.print("Id:\n"+id+"tracker:\n"+tracker+"PuertoTracker:\n"+puertoTracker+"Pedazos:\n"+pedazos+"Nombre:\n"+nombre+"Ruta:\n"+archivo);
                    JSONArray checksumJSON = obj.getJSONArray("checksum");
                    checksum = new String[checksumJSON.length()];
                    for(int i = 0, l = checksumJSON.length(); i < l; i++){
                    	checksum[i] = checksumJSON.getString(i);
                    }
                    return obj;
		}
		br.close();
		fr.close();       
                return null;
	}

	//Para verificar que el fragmento llego completo
	public Boolean isPieceValid(byte[] piece, int index){
		try{
			MessageDigest m = MessageDigest.getInstance(HASH_ALGORITHM);
			return hash(m, piece).equals(checksum[index]);
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
			return false;
		}
	}
        //Asignar un identificador
	public static String hash(MessageDigest messageDigest, byte[] data) throws NoSuchAlgorithmException {
		messageDigest.reset();
		messageDigest.update(data);
		byte[] digest = messageDigest.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashtext = bigInt.toString(16);
		while(hashtext.length() < 32){
			hashtext = "0" + hashtext;
		}
                
		return hashtext;
	}   
        
        public String[] CrearTorrent (String[] arg) throws NoSuchAlgorithmException {
        String[] Torrent =new String[5];            //Arg[0]=Ruta de archivo a compartir
         try{                                       //Arg[1]=Ip del tracker
                                                    //Arg[2]=Puerto del tracker
				MessageDigest m = MessageDigest.getInstance(HASH_ALGORITHM);
				String file_path=arg[0];
                                File fileObj = new File(file_path);
				//pregunto si existe el archivo y este disponible
				if(fileObj.exists()){
					//Obtener el nombre del archivo\
					String fileName = fileObj.getName();
					int pos = fileName.lastIndexOf(".");
					if (pos > 0) {
						fileName = fileName.substring(0,pos);
					}
                                        
					//Calcular la cantidad de fragmentos y el tamaño del ultimo fragmento
					double fileSize = fileObj.length();
					int piecesQty = (int)Math.ceil(fileSize/TorrentClient.PEDAZO_TAM);
					int lastPiece = 0;
					if(piecesQty*TorrentClient.PEDAZO_TAM > fileSize){
						lastPiece = (int)(fileSize - (piecesQty-1)*TorrentClient.PEDAZO_TAM);
					}
					//Obtener el checksum de cada pedazo
					JSONArray checksum = new JSONArray();
					InputStream is = new FileInputStream(file_path);
					for(int i = 0; i < piecesQty; i++){
						byte[] data = new byte[TorrentClient.PEDAZO_TAM];
						if (i < piecesQty-1) {
							is.read(data, 0, TorrentClient.PEDAZO_TAM);
						}else if(i == piecesQty - 1){
							data = new byte[lastPiece];
							is.read(data, 0, lastPiece);
						}
						//Hash
						checksum.put(hash(m, data));
					}
					is.close();
                                        //Crear el archivo de salida
                                        //String basePath = new File("").getAbsolutePath();
                                        System.out.println("Escribiendo Torrent en:"+fileName+".torrent");
					FileOutputStream fos = new FileOutputStream(fileName+".torrent");
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
					//Crear el torrent de este archivo como un JSON
					JSONObject torrentObj = new JSONObject();
					try{
						torrentObj.put("tracker",arg[1]);
						torrentObj.put("pieces", piecesQty);
						torrentObj.put("lastPiece", lastPiece);
						torrentObj.put("filepath", file_path);
						torrentObj.put("name", fileObj.getName());
						torrentObj.put("puertoTracker", arg[2]);
						torrentObj.put("checksum", checksum);
						//Generar ID unico
						torrentObj.put("id", hash(m, fileName.getBytes()));
					}catch(JSONException e){
						System.out.println("Error al escribir el Json"+e.getMessage());
					}
					//Guardar en el archivo
					bw.write(torrentObj.toString());
					bw.close();
					fos.close();
					System.out.println("Torrent creado!");
                                        Fragmentacion frg=new Fragmentacion();    
                                        frg.fragmentar(fileObj.getName(),file_path,PIECE_LENGTH);
					
                                        
                                        Torrent[0]=String.valueOf(fileObj.getName());
                                        Torrent[1]=file_path;
                                        Torrent[2]=String.valueOf(piecesQty);
                                        Torrent[3]=String.valueOf(piecesQty*(1024*512));
                                        return Torrent;
				}
                                else{
					System.out.println("Archivo no encontrado!");
				}
			}catch(FileNotFoundException fe){fe.printStackTrace();}
			 catch(IOException ie){ie.printStackTrace();}
                return null;
        }
        
   public static void main(String[] args) throws IOException {
                
		String IP_Tracker="localhost";
                Integer Puerto_Tracker=4445;
                System.out.println("Ingrese el la IP del tracker");
                Scanner ScanIPTracker= new Scanner(System.in);
                IP_Tracker= ScanIPTracker.nextLine();
                System.out.println("El puerto del tracker por defecto es el 4445");
                //Scanner ScanPuerto_Tracker=new Scanner(System.in);
                //Puerto_Tracker= ScanPuerto_Tracker.nextInt(1);
                InetAddress inetAddress;
                String MyAddress=null;
                String reading = null;
                Integer Puerto_Peer;
                Scanner MessagePort = new Scanner(System.in);//Puerto
                Scanner scan = new Scanner(System.in);//sirve para leer la opcion
                //reading=scan.nextLine();
                //Puerto_Peer = Integer.getInteger(reading);     
                try {
                    inetAddress = InetAddress.getLocalHost();
                    MyAddress=inetAddress.getHostAddress();
                    
                } catch (UnknownHostException ex) {
                    Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
                Socket Peer = new Socket(IP_Tracker, Puerto_Tracker);
                int choice=0;//Opción que se Castea
		String s;//Selector
                String FileBase=null;   
                FileBase=new File("").getAbsolutePath();
                FileBase=FileBase+"\\";
                System.out.println("Ubicado en: "+FileBase+"Joya.mp4");
                String PTorrent=FileBase+ "\\" + "Joya.Torrent";
                String[] filename =new String[3];
                //System.out.println("Puerto local: "+Peer.getLocalPort());
                JSONObject obj=new JSONObject();
                JSONObject Json_Message=new JSONObject(); 
                //Json_Message;
                JSONObject ToPeer=new JSONObject();
                ToPeer=null;
                Boolean Loop=true;

                
                //Para esuchar 
                System.out.println("Ingrese su puerto local");
                String Port=scan.nextLine();
                int PuertoMessages = Integer.parseInt(Port.trim());
                ServerSocket MensajesP2P=new ServerSocket(PuertoMessages);
                
                PeerMultiHilos Hilo1 = new PeerMultiHilos(MensajesP2P,ToPeer);

		//boolean loop = true	
                while (Loop!=false){
                    
                    try {
                        System.out.println("Puerto Conectado (Tracker): "+Peer.getPort());
                        System.out.println("Puerto para mensajes: "+PuertoMessages);
                        System.out.println("Opciones:");
                        System.out.println("1 - Leer torrent (Solicitar archivo)");
                        System.out.println("2 - Crear Torrent(Registrar Archivo)");
                        System.out.println("3 - Recuperar Progreso");
                        System.out.println("4 - Exit");
                        System.out.print("\n\n>");
                        
                        s = scan.nextLine();
                        try { choice = Integer.parseInt(s.trim()); }
                        catch(NumberFormatException e) {
                            System.out.println("\nPlease enter an integer\n");
                        }
                        switch (choice) {
                            case 1:      
                                TorrentClient Torrent = new TorrentClient();
                                obj = Torrent.LecturaTorrent(PTorrent);
                                //Estructuramos el JSON para este tipo de mensaje
                                            try{
                                                    Json_Message.put("Tipo",1);
                                                    Json_Message.put("IP",MyAddress);
                                                    Json_Message.put("PIn",PuertoMessages);
                                                    Json_Message.put("Archivo",obj.getString("name"));
                                                    Integer Peso=obj.getInt("pieces")*(1024*512);
                                                    Json_Message.put("Peso",Peso);
                                                    Json_Message.put("Ruta",obj.getString("filepath"));
                                                    Json_Message.put("Piezas",obj.getInt("pieces"));
                                                    Json_Message.put("Inicio",0);
                                                    Json_Message.put("Final",0);
                                            }catch(JSONException e){
                                                    System.out.println("Error al escribir el Json"+e.getMessage());
                                            }
/*-------------------------------Nos conectamos con el tracker para solicitar Peers con fragmentos------------------------------------*/
                                ((HiloPeerConnection) new HiloPeerConnection(Peer,obj,Json_Message)).start();
                            break;
                            case 2:
                                TorrentClient tt=new TorrentClient();
                                String[] Arreglo=new String[4];
                                Arreglo[0]=FileBase+"Joya.mp4";
                                System.out.println(Arreglo[0]);
                                Arreglo[1]=IP_Tracker;
                                Arreglo[2]=Integer.toString(Puerto_Tracker);
                                /*System.out.println("Ingrese la ruta");
                                Arreglo[0] = myObj.nextLine();  // Read user input
                                System.out.println("La ruta es: " + Arreglo[0]);  // Output user input        
                                */
                                try {
                                Arreglo=tt.CrearTorrent(Arreglo);
                                }
                                catch(NoSuchAlgorithmException e){
                                    System.out.println(e.getMessage());
                                }
/*-----------------------------------------------Almacenamos nuestros datos---------------------------------------------------------*/
                                try{
                                    Json_Message.put("Tipo",2);
                                    try {
                                        inetAddress = InetAddress.getLocalHost();
                                        MyAddress=inetAddress.getHostAddress();
                                        Json_Message.put("IP",MyAddress);
                                    } catch (UnknownHostException ex) {
                                        Logger.getLogger(HiloPeerConnection.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    Json_Message.put("PIn",PuertoMessages);
                                    Json_Message.put("Archivo",Arreglo[0]);
                                    Json_Message.put("Peso",Arreglo[3]);
                                    Json_Message.put("Ruta",Arreglo[1]);
                                    Json_Message.put("Piezas",Arreglo[2]);
                                    Json_Message.put("Inicio",0);
                                    Json_Message.put("Final",0);
                                    }catch(JSONException e){
                                     System.out.println("Error al escribir el Json"+e.getMessage());
                                    }
                                ((HiloPeerConnection) new HiloPeerConnection(Peer, obj,Json_Message)).start();
                                break;

                            case 3:
/*-----------------------------------------------Opcion para recuperar información---------------------------------------------------------*/
                                DataBase cc= new DataBase();
                                cc.RecuperarFragmentos(MyAddress);
                                                
                            break;
                            case 4:
                                Loop =false;
                            break;
                            case 5:
              
                            break;
                            default:
                                System.out.println("\nIngrese una opción valida\n");
                            break;
                        }
                    }
                    catch(IOException ex) {
                        System.out.println("\nPlease enter an integer\n");
                    }
            }
        }       
    }
        
    

