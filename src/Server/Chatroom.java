/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 *
 * @author OckhamsRazor
 */
public class Chatroom {
    private int roomID;
    Vector<Client> roomCli;
    private String roomname;

    public Chatroom( int id ) {
        roomID = id;
        roomCli = new Vector<Client>();
        roomname = "Room #"+id;
    }
    public Chatroom( int id, String name ) {
        roomID = id;
        roomCli = new Vector<Client>();
        roomname = name;
    }

    public int getID() { return roomID; }
    
    public void adduser( Client c ) {
        roomCli.add(c);
        c.getRooms().add(this);
    }
    
    public void rmuser( Client c ) {
        roomCli.remove(c);
        c.getRooms().remove(this);
    }
    
    public boolean isEmpty() {
        return roomCli.isEmpty();
    }
    
    public boolean hasUser( Client c ) {
        return roomCli.contains(c);
    }
    
    public boolean hasUser( String username ) {
        for( Client c: roomCli ) {
            if( username.equals(c.getName()) ) return true;
        }
        return false;
    }
    
    public void sendRoom( Client sender, HashSet<String> noSend, String msg ) {
        for( Client c: roomCli ) {
            if (!noSend.contains(c.getName())) {
                c.send("\001MSG_GET\000"+sender.getName()+"\000" + Integer.toString(roomID) + "\000" + msg + "\000\004");
            }
            else {
                System.out.println("no send to " + c.getName());
            }
        }
    }
    
    /*
    public void sendRoomPrivate(Client sender, Client receiver, String msg) {
        
    }
    */
    
}
