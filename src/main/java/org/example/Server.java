package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class Server
{
    public static void main(String[] args)
    {
        try
        {
            ServerSocket server = new ServerSocket(8080);
            System.out.println("Server is up!");
            while(true)
            {
                Socket client = server.accept();
                System.out.println("Connection established. IP = "+((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress()+" Port = "+((InetSocketAddress) client.getRemoteSocketAddress()).getPort());
                ServerThread thread = new ServerThread(client);
                thread.start();
            }
        }
        catch (IOException err)
        {
            err.printStackTrace();
        }
    }
}
