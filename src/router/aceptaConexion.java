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


/**
 *
 * @author Rafa Cuevas
 */
public class aceptaConexion implements Runnable {
    Socket socket;  
    String idRouter;
    
    public aceptaConexion(Socket socket, String router) throws Exception {
        this.socket = socket;
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
        String nombreRoute= "select * from devices where devices_nombreRouter = '" + this.idRouter + "'";
        System.out.println (nombreRoute);
        if (rs.next()){

            PrintStream output_msg = new PrintStream(socket.getOutputStream(), true);
            String mensajeOut = "From:"+router.yoRouter+"\n";
            mensajeOut = mensajeOut + "Type:WELCOME";
            output_msg.println(mensajeOut);
            System.out.println ("MENSAJE ENVIADO:"+mensajeOut);

            if (rs.getByte(5) == 1){
                int cantidad = st.executeUpdate("UPDATE `devices` set `devices_estatus`=1 where `devices_nombreRouter` = '" + this.idRouter + "'");
                if (cantidad!=1) {
                  System.out.println ("error en reactivacion de conexion");
                }
            }
        }else{
            System.out.println ("error en nombre de router");
        }
        conexion.close();
        new dataReceiveConnected(this.socket);
    }
}