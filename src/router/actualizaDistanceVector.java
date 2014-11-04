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
public class actualizaDistanceVector implements Runnable {
    int costo;  
    String idRouter;
    
    public actualizaDistanceVector(int costo, String router) throws Exception {
        this.costo = costo;
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

        // busca el router por la IP que esta enviando.//
        ResultSet rs = st.executeQuery ("select * from `devices` where `devices_nombreRouter`= '" + this.idRouter + "'");

        if (rs.next()){ 
            if (rs.getByte(4) == 0){
                int cantidad = st.executeUpdate("update dvlocal set `dvLocal_distance`='"+this.costo+"', `dvLocal_cambio`='1' where `devices_nombreRouter`='" + this.idRouter + "'");
                if (cantidad==1) {
                  System.out.println ("dvActualizado");
                } else {
                    cantidad = st.executeUpdate("INSERT INTO dvlocal VALUES ('"+rs.getString(1)+"', '"+rs.getString(3)+"', '"+ this.costo +"', '1'");
                    if (cantidad==1) {
                      System.out.println ("dvActualizado");
                    } else {
                      System.out.println ("error en reactivacion de conexion");
                    }
                }
            }else{
                System.out.println ("conexion ya iniciada");
            }
        }else{
            System.out.println ("error en nombre de router");
        }
    }
}
