package proj.AhmetRakap.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

import proj.AhmetRakap.db.Api;

public class Server
{
    public static void main(String[] args)
    {
        try(ServerSocket server = new ServerSocket(8080))
        {
            System.out.println("Server is up!");
            Api.dbInit();
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
            System.out.println("Unable to initiate the server.");
            err.printStackTrace();
        }
    }
}
