/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package router;

import java.io.PrintStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 *
 * @author Rafa Cuevas
 */
public class aceptaKeepAlive implements Runnable {
    String idRouter;
    
    public aceptaKeepAlive(String router) throws Exception {
        this.idRouter = router;
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
        Class.forName("com.mysql.jdbc.Driver");
        Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/routercc8", "root", "dbadmin");
        Statement st = (Statement) conexion.createStatement();

        ResultSet rs = st.executeQuery ("select * from devices where devices_nombreRouter = '" + this.idRouter + "'");
        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        Date today = Calendar.getInstance().getTime();  
        String s = hourFormat.format(today);
        String delims = "[:]+";
        String[] sTokens = s.split(delims);
        String sHora = sTokens[0]+sTokens[1]+sTokens[2];
        int hora = Integer.parseInt(sHora);
        
        if (rs.next()){ 
            int cantidad = st.executeUpdate("update `devices` set `devices_estatus`=1, `devices_lastConectionTime`="+hora+" WHERE devices_nombreRouter = '" + this.idRouter + "'");
            if (cantidad==1) {
              System.out.println ("KeepAlive recibido");
            } else {
              System.out.println ("error en reactivacion de conexion");
            }
        }else{
            System.out.println ("error en nombre de router");
        }
        conexion.close();
    }
}
