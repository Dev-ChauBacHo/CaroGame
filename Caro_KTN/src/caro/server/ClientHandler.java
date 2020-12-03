/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package caro.server;

import caro.common.Coordinate;
import caro.common.KMessage;
import caro.common.Room;
import caro.common.Users;
import caro.database.DataFunc;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {
        public Room room = null;
        private Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        public Users user;
        Boolean execute = true;
        
        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            
            execute = true;
        }

        
        void Guilai() throws IOException, InterruptedException {
            //Users temp = new Users(1, "Server gui lai", "123456", 100);
            //outputStream.writeObject(temp);
            //Thread.sleep(1000);
        }

        void receiveMessage(KMessage msg) throws IOException {
            
            switch (msg.getType()) {
                case 0: {
                    Users temp = (Users)msg.getObject();
                    DataFunc df = new DataFunc();
                    user = df.checkLogin(temp.getUsername(), temp.getPassword());
                    if(user != null)
                    {
                        Boolean flag = true;
                        // Kiem tra coi da co ai dang nhap hay chua
                        for (ClientHandler cli : Main.listClients) {
                            if (cli!=this && cli.user!=null && cli.user.getUsername().equalsIgnoreCase(user.getUsername()))
                            {
                                user = null;
                                break;
                            }
                        }
                        if (user!=null)
                            System.out.println("Server: Xin chao " + user.getUsername());
                    }
                    ClientHandler.this.sendMessage(0, user);
                    break;
                }
                case 1: {
                    Users temp = (Users) msg.getObject();
                    DataFunc df = new DataFunc();
                    boolean succ;
                    succ = df.checkAva(df.getId(temp.getUsername()));
                    if (succ == true) {
                        ClientHandler.this.sendMessage(1, temp.getUsername() + " is Available");
                        return;
                    }

                    succ = df.register(temp.getUsername(), temp.getPassword());
                    if (succ == true) {
                        ClientHandler.this.sendMessage(1, "Register succesfull");
                    }
                    
                    break;
                }
                case 10: //chat
                {
                    System.out.println(msg.getObject().toString());
                    break;
                }
                
                case 11: { //someone wants to rematch
                    room.rematch(this);
                    break;
                }
                
                case 12: { //someone refuses to rematch
                    room.refuseToRematch(this);
                    break;
                }
                case 13: { //someone wins
                    room.updateScore(this);
                    break;
                }
                //Room
                case 20: {// Join room
                    room = Main.listRooms.get(Integer.parseInt(msg.getObject().toString()));
                    if (room.add(this) == false) {//full
                        int[] arrRoom = new int[Main.listRooms.size()];
                        for (int i = 0; i < Main.listRooms.size(); i++) {
                            arrRoom[i] = Main.listRooms.get(i).countAvailable();
                        }
                        ClientHandler.this.sendMessage(22, arrRoom);
                    }
                    else
                        ClientHandler.this.sendMessage(20, msg.getObject());
                    break;
                }
                
                case 21: {//Get all room
                    int[] arrRoom = new int[Main.listRooms.size()];
                    for (int i = 0; i < Main.listRooms.size(); i++)
                    {
                        arrRoom[i] = Main.listRooms.get(i).countAvailable();
                    }
                    ClientHandler.this.sendMessage(21, arrRoom);
                    break;
                }
                
                case 28: {
                    if (room.client1 != null && room.client2 != null) {
                        Users[] arrUser = new Users[2];
                        arrUser[0] = room.client1.user;
                        arrUser[1] = room.client2.user;
                        room.client1.sendMessage(34, arrUser);
                        room.client2.sendMessage(34, arrUser);
                        room.client2.sendMessage(32, null);
                    }
                    break;
                }
                
                case 30: {// Lay ban co
                    Coordinate coordinate = (Coordinate) msg.getObject();
                    if (coordinate != null)
                        room.put(this, coordinate);
                    
                    if (room != null) {
                        for (ClientHandler cli: room.listClients) {
                                cli.sendMessage(30, room.listStates);
                        }
                    }
        
                    break;
                }
                case 39: {//Exit room
                    if (room != null)
                    {
                        room.clientExit(this);
                    }
                    break;
                }
                
                case 40: {//Chat
                    if (room != null)
                    {
                        // Gui cho 2 client
                        if (room.client1 != this)
                            room.client1.sendMessage(msg);
                        if (room.client2 != this)
                            room.client2.sendMessage(msg);

                        for (ClientHandler cli : room.listClients) {
                            if (cli!=this)
                            {
                                cli.sendMessage(msg);
                            }
                        }
                    }
                    break;
                }
                case 41: {//View
                    room = Main.listRooms.get(Integer.parseInt(msg.getObject().toString()));
                    room.listClients.add(this);
                    ClientHandler.this.sendMessage(20, null);
                    break;
                }
            }
        }

        public void sendMessage(int ty, Object obj) throws IOException {
            System.out.println("Server: " + ty);
            KMessage temp = new KMessage(ty, obj);
            sendMessage(temp);
        }
                
        public void sendMessage(KMessage msg) throws IOException {
            outputStream.reset();
            outputStream.writeObject(msg);
        }
        
        public boolean closeClient() throws Throwable{
            if (room != null) // Thong bao thoat room
            {
                try {
                    room.listClients.remove(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                room.clientExit(this);
            }
            
            Main.listClients.remove(this);
            try {
                this.socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Client Exit");
            execute = false;
            
            
            return true;
        }
        
        @Override
        public void run() {
            
            while (execute) { 
                try {
                    Object o = inputStream.readObject();
                    if (o != null) {
                        receiveMessage((KMessage)o);
                    }
                    //Guilai();
                } catch (IOException e) {
                    try {
                        closeClient();
                    } catch (Throwable ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }


    }