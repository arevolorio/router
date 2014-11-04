/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package router;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafa Cuevas
 */
public class solicitaConexion implements Runnable {
    int puerto;
    
    public solicitaConexion() throws Exception {
        this.puerto = 9080;
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
            while(router.running){
                Class.forName("com.mysql.jdbc.Driver");
                try (Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/routercc8", "root", "dbadmin")) {
                    Statement st = (Statement) conexion.createStatement();
                    ResultSet rs = st.executeQuery ("select * from devices");
                    while (rs.next() && router.running){
                        System.out.println ("enviando mensaje de db");
                        if (rs.getByte(5) == 0){
                            new solicitaConexionUnit(this.puerto,rs.getString(1));
                        }/*else{
                            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                            Date today = Calendar.getInstance().getTime();  
                            String s = hourFormat.format(today);
                            String delims = "[:]+";
                            String[] sTokens = s.split(delims);
                            String sHora = sTokens[0]+sTokens[1]+sTokens[2];
                            int hora = Integer.parseInt(sHora);
                            if (rs.getInt(7) <= hora+router.keepAlive && rs.getByte(5) == 1){
                                new solicitaConexionKeepAlive(this.puerto,rs.getString(1));
                            }
                        }*/
                    }
                    conexion.close();
                }
                Thread.sleep(router.hello);
            }
        } catch (UnknownHostException e) {
             System.out.println("Error al conectar con el servidor");
        } catch (IOException ex) {
            System.out.println("Error al conectar con el servidor");
            Logger.getLogger(aceptaConexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
