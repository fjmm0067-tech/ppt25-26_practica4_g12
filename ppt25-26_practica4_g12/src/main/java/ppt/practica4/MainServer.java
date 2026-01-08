package ppt.practica4;



import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*******************************************************************************
 * Protocolos de Transporte
 * Grado en Ingeniería Telemática
 * Dpto. Ingeniería de Telecomunicación
 * Univerisdad de Jaén
 *
 *******************************************************************************
 * Práctica 4. Implementación de un servidor socket TCP para un protocolo
 *             de aplicación estándar en Java
 * Fichero: MainServer.java
 * Versión: 1.0
 * Fecha: 11/2025
 * Descripción:
 *   Servidor sencillo multi-hebra Socket TCP para 
 *   atención al protocolo HTTP/1.1
 * Autor: Juan Carlos Cuevas Martínez
 *
 *******************************************************************************
 * Alumno 1:Francisco Javier Muñoz Madueño
 * Alumno 2:
 *
 ******************************************************************************/
public class MainServer {
    
    static ServerSocket server=null;
    
    //-------------------------------------------------------------------------
    //Configuracion del puerto TCP
    //
    //TCP_PORT define el puerto TCP en el que el servidor HTTP recibe las
    //conexiones entrantes.
    //El puerto 80 es el puerto estandar de HTTP
    //'final' se declara porque no debe cambiar durante su ejecución.
    //.
    static final short TCP_PORT = 90;
    /**
     * Función principal del servidor. Se encarga de crear el servidor y 
     * ponerlo a escuchar conexiones en el puerto 80 (TCP_PORT) 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            //-----------------------------------------------------------------
            //Configuración de la dirección ip del servidor
            //
            //Se obtiene la dirección IP local de la maquina mediante 
            //InetAddress.getLocalHost()
            //
            //Esto implica que el servidor escuchara únicamente en la interfaz
            //de red asociada al hostname local.
            //El servidor atendera peticiones enviadas a:
            // -localhost (127.0.0.1)
            // -Ip local de la máquina (192.168.x.x)
            //
            //Si se quiere escuchar en todas las interfaces, se debe usar:
            // InetAddress.getByName("0.0.0.0");
            //.
            InetAddress serveraddr = InetAddress.getByName("0.0.0.0");//Se optiene la dirección IP de la máquina
            
            //-----------------------------------------------------------------
            //Creación del socket servidor
            //
            //ServerSocket(TCP_PORT,5,serveraddr) crea un socket TCP pasivo
            //
            //Parametros:
            // -TCP_PORT: puerto en el que se escuchan las conexiones HTTP
            // -5: tamaño de la cola de conexiones pendientes (backlog)
            // -serveraddr: dirección IP concreta donde el servidor escucha
            //
            //Al quedar creado, el servidor entra en estado LISTEN, esperando
            //conexiones entrantes
            //.
            server = new ServerSocket(TCP_PORT,5,serveraddr);
            System.out.println("Simple HTTP/1.1. Server waiting in "+serveraddr+" port "+TCP_PORT);
            while(true){
                //-------------------------------------------------------------
                //Aceptación de una nueva conexión
                //
                //server.accept() bloquea la ejecución hasta que un cliente 
                //establece una conexión TCP. Devuelve un socket específico para
                // esa comunicación cliente-servidor
                //.
                Socket s = server.accept();// Se espera una conexión entrante, 
                                           // y cuando se recibe para el socket a
                                           // la clase HttpConnection
                
                //-------------------------------------------------------------
                //Gestión concurrente de la conexión
                //
                //Cada cliente es atendido en una hebra independiente.
                //HttpConection implementa Runnable, por lo que puede ser
                //ejecutable en un hilo separado
                //.
                HttpConnection conn = new HttpConnection(s);
                new Thread(conn).start();//Se inicia la hebra a través de HttpConnection
                                        // que implementa la interfaz Runnable
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }
    
}
