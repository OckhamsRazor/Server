/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import gui.ServerFrame;

/**
 *
 * @author OckhamsRazor
 */
public class Main {
//    private static Server _server;
    private static ServerFrame _frameObject;
    
    public static void main(String[] args) throws Exception {
        _frameObject = new ServerFrame();
        //_frameObject.setVisible(true);
//        _server = new Server();
    }
}
