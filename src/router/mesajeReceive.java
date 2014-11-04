/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafa Cuevas
 */
public class mesajeReceive implements Runnable {
    int puerto = 1981;
    int ttl = 1000;
    public mesajeReceive() throws Exception {
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
        //int puerto = 1981;
        
        try {
            ServerSocket server = new ServerSocket(this.puerto);
            
            while(router.running){
                Socket socket = server.accept();
                
                BufferedReader input_msg;
                input_msg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String mensajeIn = input_msg.readLine();

                String routerFrom = "";
                String routerTo = "";
                String mensaje = "";
                String routerIP = socket.getLocalAddress().getHostAddress();
                System.out.println("Entro mensaje de: "+routerIP);
                
                String delims = "[From:]+";
                String[] tokens = mensajeIn.split(delims);
                
                System.out.println("Entro mensaje: "+mensajeIn);
                
                if (tokens.length > 0){
                    int accion = 0;
                    routerFrom = tokens[1];
                    
                    mensajeIn = input_msg.readLine();
                    System.out.println("Entro mensaje: "+mensajeIn);
                    delims = "[To:]+";
                    tokens = mensajeIn.split(delims);
                    
                    System.out.println("tokens: "+tokens.length);
                    
                    if (tokens.length > 0){
                        routerTo = tokens[1];
                        
                        if (router.yoRouter.equals(routerTo)){
                            accion = 1;
                        }else{
                            accion = 2;
                        }
                        
                        mensajeIn = input_msg.readLine();
                        System.out.println("Entro mensaje: "+mensajeIn);
                        delims = "[Msg:]+";
                        tokens = mensajeIn.split(delims);
                        if (tokens.length > 0){
                            mensaje = tokens[1];
                            int finMensaje = 0;
                            while(finMensaje == 0){
                                mensajeIn = input_msg.readLine();
                                System.out.println("Entro mensaje: "+mensajeIn);
                                delims = "[EOF:]+";
                                tokens = mensajeIn.split(delims);
                                if (tokens.length > 0){
                                    finMensaje = 1;
                                }else{
                                    mensaje = mensaje + mensajeIn + '\n';
                                }
                            }
                        }
                        
                        System.out.println("Accion: "+ accion);
                        
                        PrintStream output_msg = new PrintStream(socket.getOutputStream(), true);
                        String mensajeOut = "recibido";
                        output_msg.println(mensajeOut);
                        System.out.println ("MENSAJE ENVIADO:"+mensajeOut);
                        
                        switch (accion){
                            case 1:
                                mensajeLocal(routerIP, routerFrom, routerTo, mensaje);
                                break;
                            case 2:
                                mensajeReenviado(routerIP, routerFrom, routerTo, mensaje);
                                break;
                            case 0:
                                System.out.println ("MENSAJE rechazado:"+mensajeIn);
                                break;
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
    
    public void mensajeLocal(String rIpOri, String rFrom, String rTo, String mInput){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/routercc8", "root", "dbadmin");
            Statement st = (Statement) conexion.createStatement();
            ResultSet rs;
            rs = st.executeQuery ("select * from devices where devices_ipRouter = '" + rIpOri + "'");
            if (rs.next()){ 
                int cantidad = st.executeUpdate("INSERT INTO `mensajes` (`mensajes_origen`, `mensajes_destino`, `mensajes_mensajes`, `mensajes_ttl`, `mensajes_inputInterface`, `mensajes_outputInterface`) VALUES ('"+rFrom+"', '"+rTo+"', '"+mInput+"', '0', '"+rs.getInt(3)+"', '0');");
                if (cantidad==1) {  
                  System.out.println ("Mensaje recibido exitosamente: "+mInput);
                } else {
                  System.out.println ("error en reactivacion de conexion");
                }
            }else{
                System.out.println ("Error mensaje recibido desde una direccion desconocida");
            }
            conexion.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(mesajeReceive.class.getName()).log(Level.SEVERE, null, ex);
        }catch (SQLException ex) {
            Logger.getLogger(mesajeReceive.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void mensajeReenviado(String rIpOri, String rFrom, String rTo, String mInput){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/routercc8", "root", "dbadmin");
            Statement st = (Statement) conexion.createStatement();
            ResultSet rs = st.executeQuery ("select * from `devices` where `devices_ipRouter`= '" + rIpOri + "'");
            if (rs.next()){
                int interfaceInput = rs.getInt(3);
                rs = st.executeQuery ("select * from `dvlocal` where `dvLocal_routerDestino` = '" + rTo + "'");
                if (rs.next()){
                    int interfaceOutput = rs.getInt(2);
                    rs = st.executeQuery ("select * from `devices` where `devices_interface`= '" + interfaceOutput + "'");
                    if (rs.next()){
                        int cantidad = st.executeUpdate("INSERT INTO `mensajes` (`mensajes_origen`, `mensajes_destino`, `mensajes_mensajes`, `mensajes_ttl`, `mensajes_inputInterface`, `mensajes_outputInterface`) VALUES ('"+rFrom+"', '"+rTo+"', '"+mInput+"', '"+this.ttl+"', '"+interfaceInput+"', '"+interfaceOutput+"');");
                        if (cantidad==1) {  
                          
                            Socket socketEnv;
                            try {
                                socketEnv = new Socket(rs.getString(1), this.puerto);
                                socketEnv.setSoTimeout(this.ttl);
                                PrintWriter sal = new PrintWriter(socketEnv.getOutputStream(), true);
                                String mensajeOut = "From:"+rFrom+"\n";
                                mensajeOut = mensajeOut + "To:"+rTo+"\n";
                                mensajeOut = mensajeOut + "Msg:"+mInput+"\n";
                                mensajeOut = mensajeOut + "EOF";
                                sal.println(mensajeOut);
                                System.out.println("Mensaje ReEnviado: ");
                                System.out.println(mensajeOut);
                                System.out.println ("se reenvio el mensajes por: "+rs.getString(1));

                                BufferedReader ent;
                                ent = new BufferedReader(new InputStreamReader(socketEnv.getInputStream()));
                                String mensajeIn = ent.readLine();
                                socketEnv.close();
                            } catch (IOException ex) {
                                Logger.getLogger(mesajeReceive.class.getName()).log(Level.SEVERE, null, ex);
                            }                                                        
                          System.out.println ("Mensaje recibido exitosamente: "+mInput);
                        } else {
                          System.out.println ("error en reactivacion de conexion");
                        }
                    }
                }
            }else{
                System.out.println ("Error mensaje recibido desde una direccion desconocida");
            }
            conexion.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(mesajeReceive.class.getName()).log(Level.SEVERE, null, ex);
        }catch (SQLException ex) {
            Logger.getLogger(mesajeReceive.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
