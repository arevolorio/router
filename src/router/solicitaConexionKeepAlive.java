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
public class solicitaConexionKeepAlive implements Runnable {
    Socket socket;
    
    public solicitaConexionKeepAlive(Socket socket) throws Exception {
        this.socket = socket;
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
            
            PrintWriter sal = new PrintWriter(socket.getOutputStream(), true);
            String mensajeOut = "From:"+router.yoRouter+"\n";
            mensajeOut = mensajeOut + "Type:KeepAlive";

            sal.println(mensajeOut);
            System.out.println("Mensaje Enviado: ");
            System.out.println(mensajeOut);
            System.out.println ("se solicito KeepAlive de conexion: "+socket.getLocalAddress().getHostAddress());

            Date date = new Date();
            SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
            Date today = Calendar.getInstance().getTime();  
            String s = hourFormat.format(today);
            String delims = "[:]+";
            String[] sTokens = s.split(delims);
            String sHora = sTokens[0]+sTokens[1]+sTokens[2];
            int hora = Integer.parseInt(sHora);
            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/routercc8", "root", "dbadmin");
            Statement st = (Statement) conexion.createStatement();
            String sqlUpdate = "UPDATE `devices` SET `devices_estatus`=1, `devices_lastConectionDate`='"+fecha.format(date)+"', `devices_lastConectionTime`="+hora+" WHERE `devices_ipRouter`='" + socket.getLocalAddress().getHostAddress() + "'";                                        
            System.out.println(sqlUpdate);
            int cantidad = st.executeUpdate(sqlUpdate);

            if (cantidad!=1) {
                System.out.println ("error en reactivacion de conexion");
            }
            conexion.close();

        } catch (UnknownHostException e) {
             System.out.println("Error al conectar con el servidor :");
        } catch (IOException ex) {
            System.out.println("Error al conectar con el servidor: " + socket.getLocalAddress().getHostAddress() + " ,motivo: " + ex.getLocalizedMessage());
        }
    }
}
