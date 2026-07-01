package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.example.db.Api;
import org.example.db.Message;
import org.example.db.User;

/*
 * STATUS CODES
 *
 * ERROR|NOTLOG := not logged in.
 * ERROR|ARGS := invalid argument count.
 * ERROR|LOG := already logged in/out.
 * ERROR|INBOX := unable to load the inbox.
 * ERROR|OUTBOX := unable to load the outbox.
 * ERROR|NOMATCH := target does not exist.
 * ERROR|SENDMSG := unable to send message.
 * ERROR|NOTADMIN := not an admin.
 * ERROR|INVALID := invalid input.
 * ERROR|ADDUSR := unable to add user.
 * ERROR|EXISTSUSR := unable to locate user.
 * ERROR|REMOVEUSR := unable to remove user.
 * ERROR|LISTUSRS := unable to list users.
 * OKAY|LOG := successful login/logout.
 * OKAY|ADMIN := user is admin.
 * OKAY|NOTADMIN := user is not an admin.
 * OKAY|NOTLOG := successfully logged out.
 * OKAY|EXIT := successful exit.
 * OKAY|INBOXSTART := sending inbox data.
 ** TO|FROM|DATE|CONTENT := inbox data format.
 * OKAY|INBOXEND := successfully sent inbox data.
 * OKAY|OUTBOXSTART := sending outbox data.
 ** TO|FROM|DATE|CONTENT := outbox data format.
 * OKAY|OUTBOXEND := successfully sent outbox data.
 * OKAY|SENDMSG := message successfully sent.
 * OKAY|ADDUSR := successfully added user.
 * OKAY|EXISTSUSR := user exists.
 * OKAY|REMOVEUSR := successfully removed user.
 * OKAY|LISTSTART := sending user data.
 ** USERNAME|PASSWORD|ADMINSTATUS|FIRSTNAME|LASTNAME|BIRTHDAY|GENDER|EMAIL := user list data format.
 * OKAY|LISTEND := successfully sent user data.
 *
 */

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
                String input = in.readLine();
                if(input!=null)
                {
                    config = input.split("\\|");
                    main:
                    switch (config[0]) {
                        case ("LOGIN") -> {
                            if (config.length != 3)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            if(!usrname.isEmpty())
                            {
                                out.println("ERROR|LOG");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(config[1], config[2]);
                            if (backend.login(usr))
                            {
                                isAdmin = backend.admin(usr);
                                usrname = config[1];
                                out.println("OKAY|LOG");
                                if(isAdmin)
                                    out.println("OKAY|ADMIN");
                                else
                                    out.println("OKAY|NOTADMIN");
                            }
                            else
                                out.println("ERROR|NOMATCH");
                        }
                        case ("LOGOUT") -> {
                            if(config.length != 1)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            isAdmin=false;
                            usrname="";
                            out.println("OKAY|NOTLOG");
                        }
                        case ("EXIT") -> {
                            if(config.length != 1)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            System.out.println("Connection terminated. IP = "+((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress()+" Port = "+((InetSocketAddress) client.getRemoteSocketAddress()).getPort());
                            out.println("OKAY|EXIT");
                            client.close();
                            return;
                        }
                        case ("INBOX") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(config.length != 1 && config.length != 2)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(usrname);
                            try
                            {
                                ResultSet res;
                                if(config.length == 1)
                                    res = backend.inbox(usr);
                                else
                                    res = backend.inbox(usr, config[1]);
                                out.println("OKAY|INBOXSTART");
                                while(res.next())
                                    out.println(res.getString(2)+"|"+res.getString(3)+"|"+res.getString(4));
                                out.println("OKAY|INBOXEND");
                            }
                            catch (SQLException err)
                            {
                                err.printStackTrace();
                                out.println("ERROR|INBOX");
                            }
                            catch (IllegalArgumentException err)
                            {
                                out.println("ERROR|NOMATCH");
                            }
                        }
                        case ("OUTBOX") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(config.length != 1 && config.length != 2)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(usrname);
                            try
                            {
                                ResultSet res;
                                if(config.length == 1)
                                    res = backend.outbox(usr);
                                else
                                    res = backend.outbox(usr, config[1]);
                                out.println("OKAY|OUTBOXSTART");
                                while(res.next())
                                    out.println(res.getString(1)+"|"+res.getString(3)+"|"+res.getString(4));
                                out.println("OKAY|OUTBOXEND");
                            }
                            catch (SQLException err)
                            {
                                err.printStackTrace();
                                out.println("ERROR|OUTBOX");
                            }
                            catch (IllegalArgumentException err)
                            {
                                out.println("ERROR|NOMATCH");
                            }
                        }
                        case ("SENDMSG") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(config.length != 4)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            Message msg = new Message(config[1], usrname, config[2], config[3]);
                            if(backend.sendMessage(msg))
                                out.println("OKAY|SENDMSG");
                            else
                                out.println("ERROR|SENDMSG");
                        }
                        case ("ADDUSR") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("ERROR|NOTADMIN");
                                break;
                            }
                            if (config.length != 9)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            if (!config[3].equals("true") && !config[3].equals("false"))
                            {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            try
                            {
                                LocalDate.parse(config[6], formatter);
                            }
                            catch (DateTimeParseException err)
                            {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            if (!config[7].equals("M") && !config[7].equals("F")) {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            if(config[8].indexOf('@')<0 || config[8].lastIndexOf('.')<0 || config[8].indexOf('@')!=config[8].lastIndexOf('@') || config[8].indexOf('@')>config[8].lastIndexOf('.'))
                            {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(config[1], config[2], config[3].equals("true"), config[4], config[5], config[6], config[7].equals("M") ? 'M' : 'F', config[8]);
                            if (backend.addUser(usr))
                                out.println("OKAY|ADDUSR");
                            else
                                out.println("ERROR|ADDUSR");
                        }
                        case ("EXISTSUSR") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("ERROR|NOTADMIN");
                                break;
                            }
                            if (config.length != 2)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(config[1]);
                            if (backend.existsUser(usr))
                                out.println("OKAY|EXISTSUSR");
                            else
                                out.println("ERROR|EXISTSUSR");
                        }
                        case ("REMOVEUSR") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("ERROR|NOTADMIN");
                                break;
                            }
                            if(config.length != 2)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(config[1]);
                            if(backend.removeUser(usr))
                                out.println("OKAY|REMOVEUSR");
                            else
                                out.println("ERROR|REMOVEUSR");
                        }
                        case ("LISTUSRS") -> {
                            if(usrname.isEmpty())
                            {
                                out.println("ERROR|NOTLOG");
                                break;
                            }
                            if(!isAdmin)
                            {
                                out.println("ERROR|NOTADMIN");
                                break;
                            }
                            if(config.length != 1)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            try
                            {
                                ResultSet res = backend.listUsers();
                                out.println("OKAY|LISTSTART");
                                while(res.next())
                                    out.println(res.getString(1)+"|"+res.getString(2)+"|"+res.getBoolean(3)+"|"+res.getString(4)+"|"+res.getString(5)+"|"+res.getString(6)+"|"+res.getString(7)+"|"+res.getString(8));
                                out.println("OKAY|LISTEND");
                            }
                            catch (SQLException err)
                            {
                                out.println("ERROR|LISTUSRS");
                                err.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException err)
        {
            System.out.println("Unable to generate a new thread.");
            err.printStackTrace();
        }
    }
}
