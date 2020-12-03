package caro.client;

import caro.common.KMessage;
import caro.common.Users;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

class ServerListener extends Thread {
    Socket socket;
    OutputStream outputStream;
    ObjectOutputStream objectOutputStream;
    InputStream inputStream;
    ObjectInputStream objectInputStream;

    public Users user;

    public IReceiveMessage receive;

    ServerListener(Socket socket) throws IOException {
        this.socket = socket;
        outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        inputStream = socket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
    }

    @Override
    public void run() {
        System.out.println("Start a new Server Listener");
        do {
            try {
                Object o = objectInputStream.readObject();
                System.out.println("Server Listener has received something");
                if (o != null && receive != null) {
                    receive.receiveMessage((KMessage) o);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        } while (true);

    }

    public void sendMessage(int ty, Object obj) throws IOException {
        System.out.print("Client sends: " + ty + " ");
        
        if (obj != null) System.out.println(obj.toString());
        else System.out.println("null");

        KMessage temp = new KMessage(ty, obj);
        sendMessage(temp);

    }

    public void sendMessage(KMessage msg) throws IOException {
        objectOutputStream.reset();
        objectOutputStream.writeObject(msg);
    }

    public void setReceive(IReceiveMessage receive) {
        this.receive = receive;
    }

}
    