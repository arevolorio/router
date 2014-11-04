/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Rafa Cuevas
 */
public class dataReceive implements Runnable {
    
    public dataReceive() throws Exception {
        Thread t = new Thread( this );
        t.start();
    }

    
    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        int puerto = 9080;
        
        try {
            ServerSocket server = new ServerSocket(puerto);
            
            while(router.running){
                Socket socket = server.accept();
                
                BufferedReader input_msg;
                input_msg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String mensajeIn = input_msg.readLine();

                String delims = "[From:]+";
                String[] tokens = mensajeIn.split(delims);
                
                System.out.println("Entro mensaje: "+mensajeIn);
                
                if (tokens.length > 0){
                    int accion = 0;
                    String routerId = tokens[1];
                    System.out.println("routerId: "+tokens[0]+" : "+tokens[1]);
                    
                    mensajeIn = input_msg.readLine();
                    
                    System.out.println("Entro mensaje: "+mensajeIn);
                    
                    delims = "[Type:]+";
                    tokens = mensajeIn.split(delims);
                    
                    System.out.println("tokens: "+tokens.length);
                    
                    if (tokens.length > 0){
                        delims = "[HELLO]+";
                        tokens = mensajeIn.split(delims);

                        if (tokens.length > 0){
                            accion = 1;
                        }/*else{
                            delims = "[WELCOME]+";
                            tokens = mensajeIn.split(delims);

                            if (tokens.length > 0){
                                accion = 2;
                            }/*else{
                                delims = "[KeepAlive]+";
                                tokens = mensajeIn.split(delims);

                                if (tokens.length > 0){
                                    accion = 3;
                                }else{
                                    delims = "[DV]+";
                                    tokens = mensajeIn.split(delims);
                                    
                                    if (tokens.length > 0){
                                        accion = 4;
                                    }else{
                                        System.out.println("MENSAJE NO RECONOCIDO"+mensajeIn);
                                    }
                                }
                            }


                        }*/


                        System.out.println("Accion: "+ accion);
                        int dvTotal = 0;
                        switch (accion){
                            case 1:
                                new aceptaConexion(socket,routerId);
                                break;
                            case 0:
                                //new aceptaKeepAlive(socket,routerId);
                                //mantieneConexion(mensajeIn , ipOrigen, puerto);
                                break;
                            /*case 4:
                                //actualizaConexion(mensajeIn , ipOrigen, puerto);
                                mensajeIn = input_msg.readLine();
                                delims = "[Len:]+";
                                tokens = mensajeIn.split(delims);
                                if (tokens.length > 0){
                                    dvTotal = Integer.parseInt(tokens[1]);
                                    for (int dvCont = 0; dvCont < dvTotal; dvCont++ ){
                                        mensajeIn = input_msg.readLine();
                                        delims = "[:]+";
                                        tokens = mensajeIn.split(delims);
                                        int costo = Integer.parseInt(tokens[1]);
                                        new actualizaDistanceVector(costo,tokens[0]);
                                    }
                                }
                                break;
                            */
                        }
                    }
                }else{
                    socket.close();
                }  
            }
        }catch (IOException e) {
            System.out.println("Error en conexion del servidor");
        }        
                
    }
}