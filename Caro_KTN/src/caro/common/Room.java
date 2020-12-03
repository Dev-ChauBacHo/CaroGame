/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package caro.common;

import caro.database.DataFunc;
import caro.server.ClientHandler;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Room implements Serializable{
    private static final long serialVersionUID = 1L;
    int id = 0;
    public ClientHandler client1 = null;
    public ClientHandler client2 = null;
    public ArrayList<ClientHandler> listClients = null;
    private static boolean someoneHasWin;
    public State[][] listStates;
    private int countRematch = 0;
    
    DataFunc dataFunc;
    
    static final int NOT5        = 0;
    static final int OK5         = 1;

    int Dx[];
    int Dy[];

    static final int D_UP        = 0;
    static final int D_UPRIGHT   = 1;
    static final int D_RIGHT     = 2;
    static final int D_DOWNRIGHT = 3;
    static final int D_DOWN      = 4;
    static final int D_DOWNLEFT  = 5;
    static final int D_LEFT      = 6;
    static final int D_UPLEFT    = 7;
    
    static final int BOARDSIZE = 25;
    
    private void newGame() {
        someoneHasWin = false;
        listStates = new State[BOARDSIZE][BOARDSIZE];
        for (int i = 0; i < BOARDSIZE; i++) {
            for (int j = 0; j < BOARDSIZE; j++) {
                listStates[i][j] = new State();
            }
        }
    }
    
    public Room(int _id) {
        id = _id;
        listClients = new ArrayList<ClientHandler>();
        someoneHasWin = false;
        newGame();
        
        dataFunc = new DataFunc();
        
        Dx = new int[8];
        Dy = new int[8];

        Dx[0] =  0;  Dy[0] = -1;
        Dx[1] =  1;  Dy[1] = -1;
        Dx[2] =  1;  Dy[2] =  0;
        Dx[3] =  1;  Dy[3] =  1;
        Dx[4] =  0;  Dy[4] =  1;
        Dx[5] = -1;  Dy[5] =  1;
        Dx[6] = -1;  Dy[6] =  0;
        Dx[7] = -1;  Dy[7] = -1;
    }

    public int getSequence(int color,int x,int y,int direction) {
        int num = 0;
        int dx = Dx[direction];
        int dy = Dy[direction];

        boolean Space = false;

        while(listStates[x][y].State == color) {
            num++;
            x += dx;
            y += dy;
            if( x < 0 || x >= BOARDSIZE || y < 0 || y >= BOARDSIZE ) break;
            if(listStates[x][y].State == State.EMPTY) {
                Space = true;
                break;
            }
        }
        return num;
    }
    
    
    public int find5Block(int color,int x,int y) {
        int max,a;

        max = getSequence(color,x,y,D_UP) + getSequence(color,x,y,D_DOWN) - 1 ;
        a = getSequence(color,x,y,D_LEFT) + getSequence(color,x,y,D_RIGHT) - 1 ;
        max = Math.max(max,a);
        a = getSequence(color,x,y,D_UPLEFT) + getSequence(color,x,y,D_DOWNRIGHT) -1 ;
        max = Math.max(max,a);
        a = getSequence(color,x,y,D_UPRIGHT) + getSequence(color,x,y,D_DOWNLEFT) - 1 ;
        max = Math.max(max,a);

        if( max >= 5)
            return OK5;

        return NOT5;
    }

    public void clientWinLose(ClientHandler client, Boolean isWin) {
        int sum = client.user.getScore();
        
        if (isWin) {
            sum += 1;
            client.user.setWin(client.user.getWin()+1);
        } else {
            sum -= 1;
            client.user.setLose(client.user.getLose()+1);
        }
        client.user.setScore(sum);
        
        try {
            dataFunc.updateUser(client.user);
        } catch (SQLException ex) {
            Logger.getLogger(Room.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int put(ClientHandler client, Coordinate coordinate) throws IOException {
        if (client == client1) {
            listStates[coordinate.x][coordinate.y].State = State.BLACK;
            if (find5Block(State.BLACK, coordinate.x, coordinate.y) == OK5) {
                someoneHasWin = true;
                System.out.printf("Black win");
                clientWinLose(client1, true);
                client1.sendMessage(35, "win");
                clientWinLose(client2, false);
                client2.sendMessage(35, "lose");
            } else {
                client2.sendMessage(31, null);
                client1.sendMessage(32, null);
            }
        }
        else {
            listStates[coordinate.x][coordinate.y].State = State.WHITE;
            if (find5Block(State.WHITE, coordinate.x, coordinate.y)==OK5) {
                someoneHasWin = true;
                System.out.printf("WHITE win");
                clientWinLose(client2, true);
                client2.sendMessage(35, "win");
                clientWinLose(client1, false);
                client1.sendMessage(35, "lose");
            } else {
                client1.sendMessage(31, null);
                client2.sendMessage(32, null);
            }
        }
        
        client1.sendMessage(30, listStates);
        client2.sendMessage(30, listStates);
        
        return 1;
    }
    
    public int countAvailable() {
        int n = 2;
        if (client1 != null)
            n--;
        if (client2 != null)
            n--;
        return n;
    }
    
    public boolean add(ClientHandler client) throws IOException {
        if (client1 == null) {
            client1 = client;
            return true;
        }
        
        if (client2 == null) {
            client2 = client;
            newGame();
            client1.sendMessage(31, null);
            client2.sendMessage(32, null);
            return true;
        }
        return false;
    }
    
    public void rematch(ClientHandler client) {
        ++countRematch;
        if (countRematch == 2) {//Both want to rematch
            countRematch = 0;
            try {
                newGame();
                client1.sendMessage(30, this.listStates);
                client2.sendMessage(30, this.listStates);
                client1.sendMessage(31, null);
                client2.sendMessage(32, null);
            } catch (IOException ex) {
                Logger.getLogger(Room.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (client == client1) {
                try {
                    client2.sendMessage(11, null);
                } catch (IOException ex) {
                    Logger.getLogger(Room.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    client1.sendMessage(11, null);
                } catch (IOException ex) {
                    Logger.getLogger(Room.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void refuseToRematch(ClientHandler client) throws IOException {
        countRematch = 0;
        if (client == client1) {
            client2.sendMessage(12, null);
        } else {
            client1.sendMessage(12, null);
        }
    }
    
    public void updateScore(ClientHandler client) {
        if (client == client1) {
            try {
                client2.sendMessage(13, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                client1.sendMessage(13, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void clientExit(ClientHandler clientHandler) throws IOException {
        if (countAvailable() == 1) {
            if (client1 != null)
                client1.room = null;
            client1 = null;
            if (client2 != null)
                client2.room = null;
            client2 = null;
        } else if (countAvailable() == 0) {
            if (client1 == clientHandler) {
                if (!someoneHasWin) {
                    clientWinLose(client1, false);
                    clientWinLose(client2, true);
                }
                    client2.sendMessage(35, "out");
            } else {
                if (!someoneHasWin) {
                    clientWinLose(client2, false);
                    clientWinLose(client1, true);
                }
                client1.sendMessage(35, "out");
            }
            if (client1 != null)
                client1.room = null;
            client1 = null;
            if (client2 != null)
                client2.room = null;
            client2 = null;
        }
    }
    
    @Override
    public String toString()
    {
        int n = 2;
        if (client1 != null)
            n--;
        if (client2 != null)
            n--;
        return "Room " + id + ": " + n + " available";
    }
}
