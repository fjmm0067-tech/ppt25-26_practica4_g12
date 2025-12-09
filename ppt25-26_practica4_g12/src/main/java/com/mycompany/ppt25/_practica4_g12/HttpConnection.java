package ppt.practica4;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*******************************************************************************
 * Protocolos de Transporte Grado en Ingeniería Telemática
 * Departamento de Ingeniería de Telecomunicación
 * Univerisdad de Jaén
 *
 *******************************************************************************
 * Práctica 4. Implementación de un servidor socket TCP para un protocolo
 *             de aplicación estándar en Java
 * Fichero: HttpConnection.java
 * Versión: 2.0
 * Fecha: 11/2025
 * Descripción: Clase sencilla de atención al protocolo HTTP/1.1
 * Autor: Juan Carlos Cuevas Martínez
 *
 *******************************************************************************
 * Alumno 1: Francisco
 * Alumno 2:
 *******************************************************************************
 */

/**
 * Clase que implementa la interfaz Runnable para ser ejecutada en una hebra que
 * gestiones una conexión entrante el un servidor HTTP/1.1
 * Esta clase solo está previto que implemente el comando GET
 * @author jccuevas
 */
public class HttpConnection implements Runnable {
    
    //Parámetros globales del servicio
    public static final String DOCUMENT_ROOT="./users/";
    public static final String DEFAULT_DOCUMENT="/index.html";
    
    public static final int HTTP_1_1_REQUEST_PARAMETERS = 0;//CAMBIAR
    public static final String CRLF = "\r\n";//Fin de línea

    Socket socket = null;
    
    

    public HttpConnection(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        String path=DEFAULT_DOCUMENT;
        DataOutputStream dos = null;
        try {
            System.out.println("Incoming HTTP connection with " + socket.getInetAddress().toString());
            dos = new DataOutputStream(socket.getOutputStream());

            BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line = bis.readLine();//Se obtiene la línea de petición/request line
            String request_line = line;//Se guarda para mostrarla más tarde
            String host_field="";//Para guardar la cabecera Host
            
            if(line!=null){
                System.out.println("Leido[" + line.length() + "]: " + line);
                
                //Se leen todas las cabeceras hasta la línea en blanco o fin
                //del buffer
                while (!(line = bis.readLine()).equals("") && line != null) {
                    System.out.println("Leido[" + line.length() + "]: " + line);
                    
                    //TAREA 2 Buscamos la cabecera HOST
                    
                }
                
                //TAREA 2. Analiazar la línea de petición, MÉTODO SP RUTA SP VERSIÓN
                //TAREA 2. Extraer el método
                //TAREA 2. Extraer la ruta y recurso
                
                // Esto se debe eliminar solo está para responder al cliente
                // con una respuesta básica para comprobar conectividad
                dos.write(("HTTP/1.1 200 OK"+CRLF).getBytes());
                dos.write(("Connection: close"+CRLF).getBytes());
                dos.write(("Content-Type: text/html"+CRLF).getBytes());
                dos.write(("Content-Length: 52"+CRLF).getBytes());
                dos.write((CRLF).getBytes());
                dos.write(("<html><meta charset='utf-8'><p>Práctica 4</p></html>"+CRLF).getBytes());
                dos.flush();
                //Eliminar hasta aquí
              
     
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                dos.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(HttpConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    
    

}
