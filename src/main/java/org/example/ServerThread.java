package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.db.Api;
import org.example.db.Message;
import org.example.db.User;

public class ServerThread extends Thread
{
    private Socket client;
    private String[] config;
    private Boolean isAdmin=false;
    private String usrname= "";
    private static int count = 0;

    public ServerThread(Socket socket)
    {
        client = socket;
    }

    @Override
    public void run()
    {
        Api backend = new Api();
        try
        {
            BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            while(true)
            {
                out.println("Enter your command: ");
                String input = in.readLine();
                if(input!=null)
                {
                    config = input.split("\\|");
                    switch (config[0]) {
                        case ("LOGIN") -> {
                            if (config.length != 3)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            if(!usrname.isEmpty())
                            {
                                out.println("Already logged in as "+usrname);
                                break;
                            }
                            User usr = new User(config[1], config[2]);
                            if (backend.login(usr))
                            {
                                isAdmin = backend.admin(usr);
                                usrname = config[1];
                                out.println("Successfully logged in as " + usr.getUsername() + ".");
                            }
                            else
                                out.println("Invalid login attempt.");
                        }
                        case ("LOGOUT") -> {
                            if(config.length != 1)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            if(usrname.isEmpty())
                            {
                                out.println("Already logged out.");
                                break;
                            }
                            isAdmin=false;
                            usrname="";
                            out.println("Successfully logged out.");
                        }
                        case ("EXIT") -> {
                            out.println("Exiting UMMT.");
                            System.out.println("Connection terminated. IP = "+((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress()+" Port = "+((InetSocketAddress) client.getRemoteSocketAddress()).getPort());
                            client.close();
                            return;
                        }
                        case ("INBOX") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(config.length != 1 && config.length != 2)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            User usr = new User(usrname);
                            try
                            {
                                ResultSet res;
                                if(config.length == 1)
                                    res = backend.inbox(usr);
                                else
                                    res = backend.inbox(usr, config[1]);
                                while(res.next())
                                    out.println(res.getString(1)+"|"+res.getString(2)+"|"+res.getString(3)+"|"+res.getString(4));
                            }
                            catch (SQLException err)
                            {
                                err.printStackTrace();
                            }
                            catch (IllegalArgumentException err)
                            {
                                out.println("Target does not exist.");
                            }
                        }
                        case ("OUTBOX") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(config.length != 1 && config.length != 2)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            User usr = new User(usrname);
                            try
                            {
                                ResultSet res;
                                if(config.length == 1)
                                    res = backend.outbox(usr);
                                else
                                    res = backend.outbox(usr, config[1]);
                                while(res.next())
                                    out.println(res.getString(1)+"|"+res.getString(2)+"|"+res.getString(3)+"|"+res.getString(4));
                            }
                            catch (SQLException err)
                            {
                                err.printStackTrace();
                            }
                            catch (IllegalArgumentException err)
                            {
                                out.println("Target does not exist.");
                            }
                        }
                        case ("SENDMSG") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(config.length != 4)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            Message msg = new Message(config[1], usrname, config[2], config[3]);
                            if(backend.sendMessage(msg))
                                out.println("Message sent.");
                            else
                                out.println("Unable to send message.");
                        }
                        case ("ADDUSR") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("Admin access needed to perform ADDUSR operation.");
                                break;
                            }
                            if (config.length != 9)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            if (!config[3].equals("true") && !config[3].equals("false"))
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            if (!config[7].equals("M") && !config[7].equals("F")) {
                                out.println("Invalid input.");
                                break;
                            }
                            User usr = new User(config[1], config[2], config[3].equals("true"), config[4], config[5], config[6], config[7].equals("M") ? 'M' : 'F', config[8]);
                            if (backend.addUser(usr))
                                out.println("Successfully added " + usr.getUsername() + ".");
                            else
                                out.println("Couldn't add "+usr.getUsername()+".");
                        }
                        case ("EXISTSUSR") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("Admin access needed to perform EXISTSUSR operation.");
                                break;
                            }
                            if (config.length != 2)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            User usr = new User(config[1]);
                            if (backend.existsUser(usr))
                                out.println("User " + usr.getUsername() + " exists.");
                            else
                                out.println("User " + usr.getUsername() + " does not exist.");
                        }
                        case ("REMOVEUSR") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("Admin access needed to perform REMOVEUSR operation.");
                                break;
                            }
                            if(config.length != 2)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            User usr = new User(config[1]);
                            if(backend.removeUser(usr))
                                out.println("Successfully removed "+usr.getUsername()+".");
                            else
                                out.println("Couldn't remove "+usr.getUsername()+".");
                        }
                        case ("LISTUSRS") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("Please login to perform operations.");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("Admin access needed to perform LISTUSRS operation.");
                                break;
                            }
                            if(config.length != 1)
                            {
                                out.println("Invalid input.");
                                break;
                            }
                            try
                            {
                                ResultSet res = backend.listUsers();
                                while(res.next())
                                    out.println(res.getString(1)+"|"+res.getString(2)+"|"+res.getBoolean(3)+"|"+res.getString(4)+"|"+res.getString(5)+"|"+res.getString(6)+"|"+res.getString(7)+"|"+res.getString(8));
                            }
                            catch (SQLException err)
                            {
                                err.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException err)
        {
            err.printStackTrace();
        }
    }
}
