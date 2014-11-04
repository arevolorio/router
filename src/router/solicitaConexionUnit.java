/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Rafa Cuevas
 */
public class solicitaConexionUnit implements Runnable {
    int puerto;
    String routerIpDest;
    
    public solicitaConexionUnit(int puerto, String routerIpDest) throws Exception {
        this.puerto = puerto;
        this.routerIpDest = routerIpDest;
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
        try {    
            Socket socket = new Socket(this.routerIpDest, this.puerto);
            socket.setSoTimeout(router.keepAliveDie);

            PrintWriter sal = new PrintWriter(socket.getOutputStream(), true);

            String mensajeOut = "From:"+router.yoRouter+"\n";
            mensajeOut = mensajeOut + "Type:HELLO";

            sal.println(mensajeOut);
            System.out.println("Mensaje Enviado: ");
            System.out.println(mensajeOut);
            System.out.println ("se solicito HELLO de conexion: "+routerIpDest);

            BufferedReader ent;
            ent = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String mensajeIn = ent.readLine();

            String delims = "[From:]+";
            String[] tokens = mensajeIn.split(delims);

            System.out.println("Entro respuesta: ");
            System.out.println(mensajeIn);

            if (tokens.length > 0){
                mensajeIn = ent.readLine();

                System.out.println(mensajeIn);

                delims = "[Type:]+";
                tokens = mensajeIn.split(delims);

                if (tokens.length > 0){
                    delims = "[WELCOME]+";
                    tokens = mensajeIn.split(delims);

                    if (tokens.length > 0){
                        Date date = new Date();
                        SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd");
                        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                        Date today = Calendar.getInstance().getTime();  
                        String s = hourFormat.format(today);
                        delims = "[:]+";
                        String[] sTokens = s.split(delims);
                        String sHora = sTokens[0]+sTokens[1]+sTokens[2];
                        int hora = Integer.parseInt(sHora);
                        Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/routercc8", "root", "dbadmin");
                        Statement st = (Statement) conexion.createStatement();
                        String sqlUpdate = "UPDATE `devices` SET `devices_estatus`=1, `devices_lastConectionDate`='"+fecha.format(date)+"', `devices_lastConectionTime`="+hora+" WHERE `devices_ipRouter`='" + this.routerIpDest + "'";                                        
                        System.out.println(sqlUpdate);
                        int cantidad = st.executeUpdate(sqlUpdate);
                        new solicitaConexionKeepAlive(socket);
                        if (cantidad!=1) {
                            System.out.println ("error en reactivacion de conexion");
                        }
                        conexion.close();
                    }else{
                        System.out.println ("error en mensaje de reactivacion de conexion");
                    }
                }
            }
           // socket.close();
        } catch (UnknownHostException e) {
             System.out.println("Error al conectar con el servidor");
        } catch (IOException ex) {
            System.out.println("Error al conectar con el servidor: " + this.routerIpDest + " ,motivo: " + ex.getLocalizedMessage());
        }
    }
}
