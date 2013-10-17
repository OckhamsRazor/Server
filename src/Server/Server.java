/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.io.*;
import java.util.*;
import java.net.*;
//import gui.*;

/**
 *
 * @author OckhamsRazor
 */
public class Server {
    private ServerSocket _ss;
    private final int PORT = 5566;
    private ArrayList<Client> _clients;
    private ArrayList<Chatroom> _rooms;
    private HashSet<String> _users; // name, i.e., account
    private HashMap<String, Client> _name_user;
    
    private HashSet<String> _bannedUsers;
    private HashSet<String> _restrictedWords;
    private HashMap<String, String> _account_password;
//    private HashMap< String, HashSet<Chatroom> > _name_rooms;
    private int _id; // cumulated client id
    private int _roomId; // cumulated room id
    
    public Server() {
        _clients = new ArrayList<Client>();
        _rooms = new ArrayList<Chatroom>();
        _users = new HashSet<String>();
        _name_user = new HashMap<String, Client>();
    
        _bannedUsers = new HashSet<String>();
        _restrictedWords = new HashSet<String>();
        _account_password = new HashMap<String, String>();
//        _name_rooms = new HashMap< String, HashSet<Chatroom> >();
        
        _id = 0;
        _roomId = -1;
        makeRoom();
        
        createUser("YLC", "XDDD");
        createUser("jane", "810701");
        createUser("bosh", "810701");
        
//        System.out.println( _account_password.get("YLC") );
        
        try {
            _ss = new ServerSocket(PORT);
            while(true) {
                Client newClient;
                synchronized(this) {
                    System.out.println("init");
                    Socket s = _ss.accept();
                    System.out.println("accepted");
                    //dis.addText(sock.lastElement().getInetAddress().getHostAddress()+" connected. ID: "+id);
                    //dis.addText(s.getInetAddress().getHostAddress()+" connected. ID: "+id);
                    //cli.add( new Client( this, sock.lastElement(), id++) );
                    newClient = new Client( this, s, _id++ );
                    newClient.runAll();
                    
                    _rooms.get(0).adduser(newClient);
                    _clients.add( newClient );
                    //thd.add( new Thread(cli.lastElement()) );
                }

//                loginInform(newClient);
            }
        }
        catch(IOException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();           
        }
    }
    
    public HashSet<String> getUsers() {
        return _users;
    }
    
    public ArrayList<Client> getClients() {
        return _clients;
    }
    
    public ArrayList<Chatroom> getRooms() {
        return _rooms;
    }
    
    public HashMap<String, Client> getNameUsers() {
        return _name_user;
    }
    
    public Client nameToClient(String name) {
        return _name_user.get(name);
    }
    
    public String getPasswd(String name) {
        return _account_password.get(name);
    }
    
    public boolean createUser(String name, String passwd) {
        assert !_users.contains(name);
        
        _users.add(name);
        _account_password.put(name, passwd);
        return true;
    }
    
    public void makeRoom() {
        _roomId++;
        _rooms.add( new Chatroom(_roomId) );
    }
    
    public void makeRoom(Client creater, String clientRoomNO) {
        _roomId++;
        Chatroom newRoom = new Chatroom(_roomId);
        _rooms.add( newRoom );
        creater.send("\001NEWROOM\000" + clientRoomNO + "\000" + Integer.toString(_roomId) + "\000\004");
        //dis.addText("New room: #"+roomid+" made.");
        newRoom.adduser(creater);
//        return _roomId;
    }
    
    public boolean roomAdd( int roomID, Client c ) {
        //dis.addText("Room #"+room+" add "+c.getname());
        if (roomID < 0 || roomID > _rooms.size()-1) return false;
        Chatroom room = _rooms.get(roomID);
        if (room == null) return false;
        if (room.roomCli.contains(c)) return false;
        room.adduser(c);
        
        enterRoomInform(c, roomID);
        sendRoomUserlist(c, room);
        /*
        for( Client cli: (_rooms.get(room-1).roomCli) ) { // send the current userlist to the new user
            if (cli!=c) c.send("/r+ "+ room+" " + cli.getName()+" " + cli._userColor);
        }
        */
        //sendRoom( room, "/r+ "+room+" "+c.getName() + " "+c._userColor );
        return true;
    }
    
    public boolean roomRm(int roomID, Client c) {
        if (roomID < 0 || roomID > _rooms.size()-1) return false;
        Chatroom room = _rooms.get(roomID);
        if (room == null) return false;
        if (!room.roomCli.contains(c)) return false;
        room.rmuser(c);
        
        leaveRoomInform(c, roomID);
        return true;
    }
    
    /*
    public void roomadd( int room, String cname ) {
        Client c = _clients.get( _users.indexOf(cname) );
		roomlist.get(room-1).adduser(c);
		c.send("/a "+room);
                for( Client cli: (roomlist.get(room-1).roomCli) ) { // send the current userlist to the new user
                        if (cli!=c) c.send("/r+ "+ room+" " + cli.getname()+" " + cli.userColor);
                }
		sendRoom( room, "/r+ "+room+" "+c.getname() + " "+c.userColor );
	}
    */
        
    public boolean sendRoom( Client sender, int roomid, String msg ) {
        if( roomid>_rooms.size() ) return false;
        Chatroom r = _rooms.get(roomid);
        if (r == null) return false;
        
		/*if( r.hasUser(src) ) {
			r.sendRoom( "/r " + roomid + " " + src + " says: " + msg);
			return true;
		}
		else return false;*/
        
        HashSet<String> noSend = new HashSet<String>();
        noSend.add(sender.getName());
        r.sendRoom( sender, noSend, msg );
        return true;
    }
    
    public boolean sendPrivate( Client sender, String receiverID, int roomid, String msg ) {
        if( roomid>_rooms.size() ) return false;
        
        Chatroom r = _rooms.get(roomid);
        if (r == null) return false;
        
        Client receiver = _name_user.get(receiverID);
        if (receiver == null) return false;
                
        receiver.send("\001MSG_P_GET\000"+sender.getName()+"\000" + Integer.toString(roomid) + "\000" + msg + "\000\004");
        return true;
    }
    
    public void sendError(Client receiver, String err) {
        
    }
    
    public void sendUserlist(Client receiver) {
        String userlist = "\001USERLIST\000" + Integer.toString(getClients().size());
        for (Client c : getClients()) {
//            if (!c.getName().equals(receiver.getName())) {
                userlist += ("\000" + c.getName() + "\000" + Integer.toString(c.getStatus()));
//            }
        }
        userlist += "\000\004";
        receiver.send(userlist);
    }
    
    public void sendRoomUserlist(Client receiver, Chatroom room) {
        String userlist = "\001RM_USERLIST\000" + Integer.toString(room.getID()) + "\000" + Integer.toString(room.roomCli.size());
        for (Client c : room.roomCli) {
//            if (!c.getName().equals(receiver.getName())) {
                userlist += ("\000" + c.getName() + "\000" + Integer.toString(c.getStatus()));
//            }
        }
        userlist += "\000\004";
        receiver.send(userlist);
    }
    
       
    public void loginInform(Client login) {
        for (Client c : getClients()) {
            if (!c.getName().equals(login.getName())) {
                c.send("\001SB_LOGIN\000"+login.getName()+"\000\004");
            }
        }
    }
    
    public void enterRoomInform(Client enterRoom, int roomID) {
        Chatroom room = _rooms.get(roomID);
        assert room != null;
        
        for (Client c : room.roomCli) {
            if (!c.getName().equals(enterRoom.getName())) {
                c.send("\001SB_IN\000"+enterRoom.getName()+"\000"+Integer.toString(roomID)+"\000\004");
            }
        }
    }
 
    public void leaveRoomInform(Client leaveRoom, int roomID) {
        Chatroom room = _rooms.get(roomID);
        assert room != null;
        
        for (Client c : room.roomCli) {
            if (!c.getName().equals(leaveRoom.getName())) {
                c.send("\001SB_OUT\000"+leaveRoom.getName()+"\000"+Integer.toString(roomID)+"\000\004");
            }
        }
    }
    
    public boolean inviteToRoom(Client sender, int roomID, String recvName, String content) {
        /*
        for (Object key : _name_user.keySet()) {
            System.out.println(key + " " + _name_user.get(key));
        }
        */
        
        Client receiver = nameToClient(recvName);
        receiver.send("\001IN\000"+Integer.toString(roomID)+"\000\004");
        return roomAdd(roomID, receiver);
    }
    
    public void changeStat(String name, String newStat) {
        Client chStat = _name_user.get(name);
        for (Client c : _clients) {
            if (!c.getName().equals(name)) {
                c.send("\001SB_STAT\000"+name+"\000"+newStat+"\000\004");
            }
        }
    }
    
    public void logoutUser(Client rm) {
        rm.send("\001LOGOUT\000\004");
        rmUser(rm);
    }
    
    public void kickUser(Client rm) {
        rm.send("\001KICKED\000\004");
        rmUser(rm);
    }
    
    public void rmUser (Client quitter) {
        quitter.closeConnection();
        
        for (Chatroom room : quitter.getRooms()) {
            room.rmuser(quitter);
        }
        _name_user.remove(quitter.getName());
        _users.remove(quitter.getName());
        _clients.remove(quitter);
    }
    
    public boolean sendReq(Client sender, String receiverID, String filename, String filesize){
        Client receiver = _name_user.get(receiverID);
        if (receiver == null) return false;
        System.out.println("sendReq_Success");
        receiver.send("\001FS_REQ\000"+sender.getName() + "\000" + filename + "\000"+ filesize + "\000\004");
        return true;
    }
    
    public boolean sendReply(Client receiver, String senderID, String recvIP){
        Client sender = _name_user.get(senderID);
        if (sender == null) return false;
        System.out.println("sendReply_Y_Success");
        sender.send("\001FS_REP_Y\000"+receiver.getName() + "\000"+ recvIP + "\000\004");
        return true;
    }
    
    public boolean sendReply(Client receiver, String senderID){
        Client sender = _name_user.get(senderID);
        if (sender == null) return false;
        System.out.println("sendReply_N_Success");
        sender.send("\001FS_REP_N\000"+receiver.getName()+"\000" + "\000\004");
        return true;
    }
    
    public boolean sendSpeak(Client sender,  String receiverID, String senderIP, String portNo)
    {
        Client receiver = _name_user.get(receiverID);
        if(receiver==null) return false;
        receiver.send("\001SPEAK\000"+sender.getName()+"\000"+senderIP+"\000"+portNo+"\000\004");
        return true;
    }
    
    public boolean sendSpeakAck(Client sender,  String receiverID, String senderIP, String portNo)
    {
        Client receiver = _name_user.get(receiverID);
        if(receiver==null) return false;
        receiver.send("\001SPEAK_ACK\000"+senderIP+"\000"+portNo+"\000\004");
        return true;
    }
    public boolean sendVisual(Client sender,  String receiverID, String url)
    {
        Client receiver = _name_user.get(receiverID);
        if(receiver==null) return false;
        receiver.send("\001VISUAL\000"+sender.getName()+"\000"+url+"\000\004");
        return true;
    }
    
    public boolean sendVisualAck(Client sender,  String receiverID, String url)
    {
        Client receiver = _name_user.get(receiverID);
        if(receiver==null) return false;
        receiver.send("\001VISUAL_ACK\000"+url+"\000\004");
        return true;
    }
}
