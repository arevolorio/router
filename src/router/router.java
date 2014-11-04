/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package router;

/**
 *
 * @author Rafa Cuevas
 */

public class router {
    
    public static boolean running = true;
    public static int hello = 30000;
    public static int helloDie = 1000;
    public static int keepAlive = 30000;
    public static int keepAliveDie = 90000;
    public static String yoRouter = "rafa";
    
    public static void main(String[] args) throws Exception{
        new dataReceive();
        new solicitaConexion();
        new mesajeReceive();
       // running = false;
    }
}