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
import java.nio.charset.StandardCharsets;

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
 * Alumno 1:Francisco Javier Muñoz Madueño
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
    public static final String DOCUMENT_ROOT="./fjmm0067/";
    public static final String DEFAULT_DOCUMENT="/index.html";
    
    public static final int HTTP_1_1_REQUEST_PARAMETERS = 3;
    public static final String CRLF = "\r\n";//Fin de línea

    private final Socket socket;
   
    public HttpConnection(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        DataOutputStream dos = null;
        
        try{
            System.out.println("Incoming HTTP connection with " + socket.getInetAddress().toString());
            dos = new DataOutputStream(socket.getOutputStream());

            BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));

            String line = bis.readLine();//Se obtiene la línea de petición/request line
            String request_line = line;//Se guarda para mostrarla más tarde
            String host_field="";//Para guardar la cabecera Host
            
            if(request_line == null || request_line.isEmpty()){
                sendHtml(dos,"HTTP/1.1 400 Bad Request","<!DOCTYPE html><html><head><meta charset='utf-8'></head>" + "<body><h1>400 Bad Request</h1></body></html>");
                return;
            }
            
            System.out.println("Leido["+ request_line.length() + "]: "+ request_line);
            
                //Se leen todas las cabeceras hasta la línea en blanco o fin
                //del buffer
                while ((line = bis.readLine())!= null && !line.isEmpty()) {
                    System.out.println("Leido[" + line.length() + "]: " + line);
                    //TAREA 2 Buscamos la cabecera HOST
                    if(line.regionMatches(true,0,"Host:",0,5)){
                        host_field = line.substring(5).trim(); //guardamos el valor
                    }  
                }
                if(host_field == null || host_field.isEmpty()){
                    sendHtml(dos,"HTTP/1.1 400 Bad Request","<!DOCTYPE html><html><head><meta charset='utf-8'></head>"+"<body><h1>400 Bad Request</h1><p>Missing Host Header</p></body></html>");
                    return;
                }
                
                System.out.println("HOST:" + host_field);
                //TAREA 2. Analiazar la línea de petición, MÉTODO SP RUTA SP VERSIÓN
                String method = "";
                String requestedPath = "";
                String version = "";
                
                try{
                    String[] parts = request_line.trim().split("\\s+");
                    if (parts.length != HTTP_1_1_REQUEST_PARAMETERS){
                        throw new IllegalArgumentException("Formato incorrecto");
                    }
                    
                    method = parts[0];
                    requestedPath = parts[1];
                    version = parts[2];
                    
                }catch (Exception e){
                    sendHtml(dos, "HTTP/1.1 400 Bad Request","<!DOCTYPE html><html><head><meta charset='utf-8'></head><body><h1>400 Bad Request</h1></body></html>");
                return;
                }
                //TAREA 2. Extraer el método
                requestedPath = requestedPath.split("\\?")[0];
                if(requestedPath.equals("/")){
                    requestedPath = DEFAULT_DOCUMENT;
                }
                
                String hostName = host_field;
                int colon = hostName.indexOf(':');
                if(colon >= 0){
                    hostName = hostName.substring(0, colon);
                }
                
                String relativePath = requestedPath.startsWith("/") ? requestedPath.substring(1) : requestedPath;
               
                String finalPath = DOCUMENT_ROOT + relativePath;
                
                System.out.println("FINAL_PATH=" + finalPath);
                //TAREA 2. Extraer la ruta y recurso
                String route;
                String resource;
                int lastSlash = requestedPath.lastIndexOf('/');
                if (lastSlash < 0){
                    route = "/";
                    resource = requestedPath;
                }else{
                route = requestedPath.substring(0,lastSlash + 1);
                resource = requestedPath.substring(lastSlash + 1);
                }
                String extension = "";
                int dot = resource.lastIndexOf('.');
                if (dot >= 0 && dot < resource.length() - 1){
                    extension = resource.substring(dot + 1).toLowerCase();
                }
                    String mimeType = getMimeType(extension);
                    
                    System.out.println("ROUTE=" + route + " RESOURCE=" + resource + " EXT=" + extension + " MIME=" + mimeType);
                
                System.out.println("METHOD="+ method + "PATH=" + requestedPath + "ROUTE=" + route + "RESOURCE=" + resource + "VERSION=" + version);
                
                if(!"GET".equals(method)){
                    sendHtml(dos,"HTTP/1.1 405 Method Not Allowed","<!DOCTYPE html><html><head><meta charset='utf-8'></head>"+"<body><h1> 405 Method Not Allowed </h1></body></html>","Allow: GET");
                return;
                }
                
                try{
                byte[] body = readResourceBytes(finalPath);
                sendResource(dos, "HTTP/1.1 200 OK", mimeType, body);
        }catch(FileNotFoundException e){
                        sendHtml(dos, "HTTP/1.1 404 Not Found", "<!DOCTYPE html><html><head><meta charset='utf-8'></head>" + "<body><h1>404 Not Found</h1></body></html>");
                        return;
                        }
                
        }catch (IOException ex){
            Logger.getLogger(HttpConnection.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try{
                if(dos != null) dos.close();
                if(socket != null) socket.close();
            }catch (IOException ex){
                Logger.getLogger(HttpConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void sendResource(DataOutputStream dos, String statusLine, String contentType, byte[] body, String... extraHeaders)throws IOException{
   
    dos.write((statusLine + CRLF).getBytes(StandardCharsets.UTF_8));
    dos.write(("Connection: close" + CRLF).getBytes(StandardCharsets.UTF_8));
    dos.write(("Content-Type: " + contentType + CRLF).getBytes(StandardCharsets.UTF_8));
    
    if(extraHeaders != null){
        for(String h : extraHeaders){
            dos.write((h + CRLF).getBytes(StandardCharsets.UTF_8));
        }
    }
    dos.write(("Content-Length: " + body.length + CRLF).getBytes(StandardCharsets.UTF_8));
    dos.write((CRLF).getBytes(StandardCharsets.UTF_8));
    dos.write(body);
    dos.flush();
    }
    
    private void sendHtml(DataOutputStream dos, String statusLine, String html, String... extraHeaders)throws IOException{
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        
        dos.write((statusLine + CRLF).getBytes(StandardCharsets.UTF_8));
        dos.write(("Connection: close"+CRLF).getBytes(StandardCharsets.UTF_8));
        dos.write(("Content-Type: text/html; charset=utf-8"+CRLF).getBytes(StandardCharsets.UTF_8));
        
        if(extraHeaders != null){
            for(String h : extraHeaders){
                dos.write((h + CRLF).getBytes(StandardCharsets.UTF_8));
            }
        }        
        dos.write(("Content-Length: "+ body.length + CRLF).getBytes(StandardCharsets.UTF_8));
        dos.write((CRLF).getBytes(StandardCharsets.UTF_8));
                dos.write(body);
                dos.flush();
    }
    private void sendHtml(DataOutputStream dos, String statusLine, String html) throws IOException{
        sendHtml(dos, statusLine, html, (String[]) null);
    }
    private byte[] readResourceBytes(String resourcePath) throws IOException{
        File f = new File(resourcePath);
        
        if(!f.exists()|| !f.isFile()){
            throw new FileNotFoundException(f.getPath());
        }
        long len= f.length();
        if(len > Integer.MAX_VALUE){
            throw new IOException("File too large: " + len + "bytes");
        }
        byte[] data = new byte[(int) len];
        
        try(FileInputStream fis = new FileInputStream(f)){
            int offset = 0;
            while(offset < data.length){
                int r = fis.read(data, offset, data.length - offset);
                if(r == -1) break;
                offset += r;
            }
            if(offset < data.length){
                throw new IOException("Unexpected EOF: read "+ offset + " of " + data.length);
            }
        }
        return data;
    }
    
    private String getMimeType(String ext){
        switch(ext){
            case "html":
            case "htm":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "text/javascript";
            case "txt":
                return "text/plain";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "ico":
                return "image/x-icon";
            default:
                return "application/octet-stream";
        }
    }
    
}
                
              
     
       
    
    


