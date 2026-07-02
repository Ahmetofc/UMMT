package proj.AhmetRakap.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.ArrayList;

import proj.AhmetRakap.db.Api;
import proj.AhmetRakap.db.Message;
import proj.AhmetRakap.db.User;


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

    public static Boolean checkDate(String date) // returns whether the given string IS a valid date.
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try
        {
            LocalDate.parse(date, formatter);
            return true;
        }
        catch (DateTimeParseException err)
        {
            return false;
        }
    }

    public static Boolean checkEmail(String email) // returns whether the given string is NOT a valid email address.
    {
        return email.indexOf('@')<0 || email.lastIndexOf('.')<0 || email.indexOf('@')!=email.lastIndexOf('@') || email.indexOf('@')>email.lastIndexOf('.') || email.charAt(email.indexOf('@')+1)=='.' || email.charAt(email.length()-1)=='.';
    }

    @Override
    public void run()
    {
        try(Api backend = new Api(); BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream()));PrintWriter out = new PrintWriter(client.getOutputStream(), true))
        {
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
                                ArrayList<Message> res;
                                if(config.length == 1)
                                    res = backend.inbox(usr);
                                else
                                    res = backend.inbox(usr, config[1]);
                                if(res != null)
                                {
                                    out.println("OKAY|INBOXSTART");
                                    for (Message msg : res)
                                        out.println(msg.getFrom()+"|"+msg.getDate()+"|"+msg.getContent());
                                    out.println("OKAY|INBOXEND");
                                }
                                else
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
                                ArrayList<Message> res;
                                if(config.length == 1)
                                    res = backend.outbox(usr);
                                else
                                    res = backend.outbox(usr, config[1]);
                                if(res != null)
                                {
                                    out.println("OKAY|OUTBOXSTART");
                                    for (Message msg : res)
                                        out.println(msg.getTo()+"|"+msg.getDate()+"|"+msg.getContent());
                                    out.println("OKAY|OUTBOXEND");
                                }
                                else
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
                            Message msg = new Message(config[1], usrname, LocalDateTime.parse(config[2]), config[3]);
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
                            if(!checkDate(config[6]))
                            {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            if (!config[7].equals("M") && !config[7].equals("F")) {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            if(checkEmail(config[8]))
                            {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            User usr = new User(config[1], config[2], config[3].equals("true"), config[4], config[5], LocalDate.parse(config[6], DateTimeFormatter.ofPattern("dd-MM-yyyy")), config[7].equals("M") ? 'M' : 'F', config[8]);
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
                        case ("UPDATEUSR") -> {
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
                            if(config.length%2!=0)
                            {
                                out.println("ERROR|ARGS");
                                break;
                            }
                            for(String entry : config)
                                if (entry.isEmpty()) {
                                    out.println("ERROR|INVALID");
                                    break main;
                                }
                            if(!backend.existsUser(new User(config[1])))
                            {
                                out.println("ERROR|INVALID");
                                break;
                            }
                            User usr = backend.makeUser(config[1]);
                            for(int i=0; i<config.length/2-1; i++)
                            {
                                switch (config[2*i+2])
                                {
                                    case ("PASSWORD") -> usr.setPassword(config[2*i+3]);
                                    case ("ADMINSTATUS") -> {
                                        if(config[2*i+3].equals("true") || config[2*i+3].equals("false"))
                                            usr.setAdmin(config[2*i+3].equals("true"));
                                        else
                                        {
                                            out.println("ERROR|INVALID");
                                            break main;
                                        }
                                    }
                                    case ("FIRSTNAME") -> usr.setFirstName(config[2*i+3]);
                                    case ("LASTNAME") -> usr.setLastName(config[2*i+3]);
                                    case ("BIRTHDAY") -> {
                                        if(checkDate(config[2*i+3]))
                                            usr.setBirthday(LocalDate.parse(config[2*i+3], DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                                        else
                                        {
                                            out.println("ERROR|INVALID");
                                            break main;
                                        }
                                    }
                                    case ("GENDER") -> {
                                        if(config[2*i+3].equals("M") || config[2*i+3].equals("F"))
                                            usr.setGender(config[2*i+3].equals("M")?'M':'F');
                                        else
                                        {
                                            out.println("ERROR|INVALID");
                                            break main;
                                        }
                                    }
                                    case ("EMAIL") -> {
                                        if(checkEmail(config[2*i+3]))
                                        {
                                            out.println("ERROR|INVALID");
                                            break main;
                                        }
                                        usr.setEmail(config[2*i+3]);
                                    }
                                }
                            }
                            if(backend.updateUser(usr))
                                out.println("OKAY|UPDATEUSR");
                            else
                                out.println("ERROR|UPDATEUSR");
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
                            ArrayList<User> res = backend.listUsers();
                            if(res != null)
                            {
                                out.println("OKAY|LISTSTART");
                                for (User usr : res)
                                    out.println(usr.getUsername() + "|" + usr.getPassword() + "|" + usr.getAdmin() + "|" + usr.getFirstName() + "|" + usr.getLastName() + "|" + usr.getBirthday() + "|" + usr.getGender() + "|" + usr.getEmail());
                                out.println("OKAY|LISTEND");
                            }
                            else
                                out.println("ERROR|LISTUSRS");
                        }
                    }
                }
            }
        }
        catch(SQLException err)
        {
            System.out.println("Unable to connect to the database.");
            err.printStackTrace();
        }
        catch(IOException err)
        {
            System.out.println("Unable to generate a new thread.");
            err.printStackTrace();
        }
    }
}
