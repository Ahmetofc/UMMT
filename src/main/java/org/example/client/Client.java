package org.example.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client
{
    enum State
    {
        LOGGED_OUT,
        IDLE,
        INBOX,
        OUTBOX,
        SENDMSG,
        ADDUSR,
        EXISTSUSR,
        REMOVEUSR,
        LISTUSRS
    }

    public static void main(String[] args)
    {
        try
        {
            Socket client = new Socket("localhost", 8080);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);
            State state = State.LOGGED_OUT;
            Boolean adminStatus = false;

            while(true)
            {
                switch(state)
                {
                    case LOGGED_OUT -> {
                        System.out.println("Welcome to UMMT - User Messaging and Management Tool!");
                        System.out.print("Username:\n> ");
                        String usrname = sc.nextLine();
                        System.out.print("Password:\n> ");
                        String pswd = sc.nextLine();
                        out.println("LOGIN|"+usrname+"|"+pswd);
                        String[] response = in.readLine().split("\\|");
                        if(response[0].equals("OKAY"))
                        {
                            System.out.println("Successful login.");
                            TimeUnit.SECONDS.sleep(1);
                            state=State.IDLE;
                            adminStatus=in.readLine().split("\\|")[1].equals("ADMIN");
                        }
                        else switch(response[1])
                        {
                            case("ARGS") -> System.out.println("Invalid argument count.");
                            case("LOG") -> System.out.println("Already logged in.");
                            case("INVALID") -> System.out.println("Invalid entries.");
                            case("NOMATCH") -> System.out.println("Incorrect password or username.");
                        }
                    }
                    case IDLE -> {
                        System.out.println("Tell me what to do:");
                        System.out.println("1. logout - logs you out of the system.");
                        System.out.println("2. exit - terminates your connection to the server.");
                        System.out.println("3. inbox - displays your current inbox.");
                        System.out.println("4. outbox - displays your current outbox.");
                        System.out.println("5. message - send a message to a user.");
                        if(adminStatus)
                        {
                            System.out.println("6. add - adds a specified user.");
                            System.out.println("7. query - checks whether a specified user exists.");
                            System.out.println("8. remove - removes a specified user.");
                            System.out.println("9. list - lists all of the users.");
                        }
                        System.out.print("> ");
                        String action = sc.nextLine();
                        switch(action)
                        {
                            case ("logout") -> {
                                System.out.println("Logging you out...");
                                out.println("LOGOUT");
                                String[] response = in.readLine().split("\\|");
                                if(response[0].equals("OKAY"))
                                {
                                    TimeUnit.SECONDS.sleep(1);
                                    state=State.LOGGED_OUT;
                                }
                                else switch(response[1])
                                {
                                    case ("ARGS") -> System.out.println("Invalid argument count.");
                                    case ("NOTLOG") -> {
                                        System.out.println("You are not logged in.");
                                        TimeUnit.SECONDS.sleep(2);
                                        state=State.LOGGED_OUT;
                                    }
                                }
                            }
                            case ("exit") -> {
                                System.out.println("Terminating connection...");
                                out.println("EXIT");
                                if(in.readLine().split("\\|")[0].equals("OKAY"))
                                {
                                    client.close();
                                    return;
                                }
                                else
                                    System.out.println("Unable to terminate the connection.");
                            }
                            case ("inbox") -> state=State.INBOX;
                            case ("outbox") -> state=State.OUTBOX;
                            case ("message") -> state=State.SENDMSG;
                            case ("add") -> {
                                if (adminStatus)
                                    state = State.ADDUSR;
                            }
                            case ("query") -> {
                                if (adminStatus)
                                    state = State.EXISTSUSR;
                            }
                            case ("remove") -> {
                                if (adminStatus)
                                    state = State.REMOVEUSR;
                            }
                            case ("list") -> {
                                if (adminStatus)
                                    state = State.LISTUSRS;
                            }
                        }
                    }
                    case INBOX -> {
                        System.out.print("Do you want to filter by username? If yes write the username.\n> ");
                        String mode = sc.nextLine();
                        if(mode.equals("no"))
                            out.println("INBOX");
                        else
                            out.println("INBOX|"+mode);
                        String[] status = in.readLine().split("\\|");
                        if(status[0].equals("OKAY") && status[1].equals("INBOXSTART"))
                        {
                            if(mode.equals("no"))
                                System.out.println("Here is all of your inbox:\n");
                            else
                                System.out.println("Here is your inbox from "+mode+":\n");
                            String[] msg = in.readLine().split("\\|");
                            while(!msg[0].equals("OKAY") || !msg[1].equals("INBOXEND"))
                            {
                                System.out.println("Message from "+msg[0]+" at "+msg[1]+":");
                                System.out.println(msg[2]);
                                System.out.println();
                                msg=in.readLine().split("\\|");
                            }
                            TimeUnit.SECONDS.sleep(8);
                            state=State.IDLE;
                        }
                        else switch(status[1])
                        {
                            case ("NOTLOG") -> {
                                System.out.println("You are not logged in.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.LOGGED_OUT;
                            }
                            case("ARGS") -> System.out.println("Invalid argument count.");
                            case ("INVALID") -> System.out.println("Invalid entry.");
                            case("NOMATCH") -> {
                                System.out.println("No matching targets.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                            case("INBOX") -> {
                                System.out.println("Unable to load the inbox.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                        }
                    }
                    case OUTBOX -> {
                        System.out.print("Do you want to filter by username? If yes write the username.\n> ");
                        String mode = sc.nextLine();
                        if(mode.equals("no"))
                            out.println("OUTBOX");
                        else
                            out.println("OUTBOX|"+mode);
                        String[] status = in.readLine().split("\\|");
                        if(status[0].equals("OKAY") && status[1].equals("OUTBOXSTART"))
                        {
                            if(mode.equals("no"))
                                System.out.println("Here is all of your outbox:\n");
                            else
                                System.out.println("Here is your outbox to "+mode+":\n");
                            String[] msg = in.readLine().split("\\|");
                            while(!msg[0].equals("OKAY") || !msg[1].equals("OUTBOXEND"))
                            {
                                System.out.println("Message to "+msg[0]+" at "+msg[1]+":");
                                System.out.println(msg[2]);
                                System.out.println();
                                msg=in.readLine().split("\\|");
                            }
                            TimeUnit.SECONDS.sleep(8);
                            state=State.IDLE;
                        }
                        else switch(status[1])
                        {
                            case ("NOTLOG") -> {
                                System.out.println("You are not logged in.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.LOGGED_OUT;
                            }
                            case("ARGS") -> System.out.println("Invalid argument count.");
                            case ("INVALID") -> System.out.println("Invalid entry.");
                            case("NOMATCH") -> {
                                System.out.println("No matching targets.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                            case("OUTBOX") -> {
                                System.out.println("Unable to load the outbox.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                        }
                    }
                    case SENDMSG -> {
                        System.out.print("Who do you want to send message to? (type 'none' to exit)\n> ");
                        String target = sc.nextLine();
                        if(target.equals("none"))
                        {
                            TimeUnit.SECONDS.sleep(2);
                            state = State.IDLE;
                        }
                        else
                        {
                            System.out.print("Enter your message:\n> ");
                            String sendmsg = sc.nextLine();
                            LocalDateTime datetime = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                            out.println("SENDMSG|"+target+"|"+datetime.format(formatter)+"|"+sendmsg);
                            String[] response = in.readLine().split("\\|");
                            if(response[0].equals("OKAY"))
                            {
                                System.out.println("Successfully sent message to "+target+".");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                            else switch(response[1])
                            {
                                case ("NOTLOG") -> {
                                    System.out.println("You are not logged in.");
                                    TimeUnit.SECONDS.sleep(2);
                                    state=State.LOGGED_OUT;
                                }
                                case ("ARGS") -> System.out.println("Invalid argument count.");
                                case ("INVALID") -> System.out.println("Invalid entries.");
                                case ("SENDMSG") -> {
                                    System.out.println("Unable to send the message");
                                    TimeUnit.SECONDS.sleep(2);
                                    state=State.IDLE;
                                }
                            }
                        }
                    }
                    case ADDUSR -> {
                        System.out.println("You are adding a new user to the system.");
                        System.out.print("Enter username:\n> ");
                        String usrname = sc.nextLine();
                        System.out.print("Enter password:\n> ");
                        String pswd = sc.nextLine();
                        System.out.print("Enter admin status: (true/false)\n> ");
                        String admin = sc.nextLine();
                        System.out.print("Enter first name:\n> ");
                        String fname = sc.nextLine();
                        System.out.print("Enter last name:\n> ");
                        String lname = sc.nextLine();
                        System.out.print("Enter birthday: (DD-MM-YYYY)\n> ");
                        String bday = sc.nextLine();
                        System.out.print("Enter gender: (M/F)\n> ");
                        String gender = sc.nextLine();
                        System.out.print("Enter email:\n> ");
                        String email = sc.nextLine();
                        out.println("ADDUSR|"+usrname+"|"+pswd+"|"+admin+"|"+fname+"|"+lname+"|"+bday+"|"+gender+"|"+email);
                        String[] response = in.readLine().split("\\|");
                        if(response[0].equals("OKAY"))
                        {
                            System.out.println("Successfully created user "+usrname+".");
                            TimeUnit.SECONDS.sleep(2);
                            state=State.IDLE;
                        }
                        else switch(response[1])
                        {
                            case ("NOTLOG") -> {
                                System.out.println("You are not logged in.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.LOGGED_OUT;
                            }
                            case ("NOTADMIN") -> {
                                System.out.println("You are not an admin.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                            case ("ARGS") -> System.out.println("Invalid argument count.");
                            case ("INVALID") -> System.out.println("Invalid entries.");
                            case ("ADDUSR") ->{
                                System.out.println("Unable to create user.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                        }
                    }
                    case EXISTSUSR -> {
                        System.out.println("You are making a query.");
                        System.out.print("Enter username:\n> ");
                        String usrname = sc.nextLine();
                        out.println("EXISTSUSR|"+usrname);
                        String[] response = in.readLine().split("\\|");
                        if(response[0].equals("OKAY"))
                        {
                            System.out.println("User "+usrname+" is registered to the system.");
                            TimeUnit.SECONDS.sleep(2);
                            state=State.IDLE;
                        }
                        else switch(response[1])
                        {
                            case ("NOTLOG") -> {
                                System.out.println("You are not logged in.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.LOGGED_OUT;
                            }
                            case ("NOTADMIN") -> {
                                System.out.println("You are not an admin.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                            case ("ARGS") -> System.out.println("Invalid argument count.");
                            case ("INVALID") -> System.out.println("Invalid entry.");
                            case ("EXISTSUSR") -> {
                                System.out.println("Unable to locate the user "+usrname+".");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                        }
                    }
                    case REMOVEUSR -> {
                        System.out.println("You are removing a user from the system.");
                        System.out.print("Enter username:\n> ");
                        String usrname = sc.nextLine();
                        System.out.print("Are you sure you want to delete "+usrname+" from the system? (yes/no)\n> ");
                        if(sc.nextLine().equals("yes"))
                        {
                            out.println("REMOVEUSR|" + usrname);
                            String[] response = in.readLine().split("\\|");
                            if (response[0].equals("OKAY")) {
                                System.out.println("User " + usrname + " is removed from the system.");
                                TimeUnit.SECONDS.sleep(2);
                                state = State.IDLE;
                            }
                            else switch (response[1]) {
                                case ("NOTLOG") -> {
                                    System.out.println("You are not logged in.");
                                    TimeUnit.SECONDS.sleep(2);
                                    state = State.LOGGED_OUT;
                                }
                                case ("NOTADMIN") -> {
                                    System.out.println("You are not an admin.");
                                    TimeUnit.SECONDS.sleep(2);
                                    state = State.IDLE;
                                }
                                case ("ARGS") -> System.out.println("Invalid argument count.");
                                case ("INVALID") -> System.out.println("Invalid entry.");
                                case ("REMOVEUSR") -> {
                                    System.out.println("Unable to remove the user " + usrname + " from the system.");
                                    TimeUnit.SECONDS.sleep(2);
                                    state = State.IDLE;
                                }
                            }
                        }
                        else
                        {
                            TimeUnit.SECONDS.sleep(2);
                            state=State.IDLE;
                        }
                    }
                    case LISTUSRS -> {
                        out.println("LISTUSRS");
                        String[] response = in.readLine().split("\\|");
                        if(response[0].equals("OKAY") && response[1].equals("LISTSTART"))
                        {
                            System.out.println("Listing all of the users and their credentials registered to the system:\n");
                            String[] usr = in.readLine().split("\\|");
                            while(!usr[0].equals("OKAY") || !usr[1].equals("LISTEND"))
                            {
                                System.out.println("Username: "+usr[0]);
                                System.out.println("Password: "+usr[1]);
                                System.out.println("Admin Status: "+usr[2]);
                                System.out.println("First Name: "+usr[3]);
                                System.out.println("Last Name: "+usr[4]);
                                System.out.println("Birthday: "+usr[5]);
                                System.out.println("Gender: "+usr[6]);
                                System.out.println("Email: "+usr[7]);
                                System.out.println();
                                usr=in.readLine().split("\\|");
                            }
                            TimeUnit.SECONDS.sleep(8);
                            state=State.IDLE;
                        }
                        else switch (response[1])
                        {
                            case ("NOTLOG") -> {
                                System.out.println("You are not logged in.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.LOGGED_OUT;
                            }
                            case ("NOTADMIN") -> {
                                System.out.println("You are not an admin.");
                                TimeUnit.SECONDS.sleep(2);
                                state=State.IDLE;
                            }
                            case ("ARGS") -> System.out.println("Invalid argument count.");
                            case ("LISTUSRS") -> System.out.println("Unable to list the users.");
                        }
                    }
                }
            }
        }
        catch (Exception err)
        {
            System.out.println("Unable to connect to the server.");
            err.printStackTrace();
        }
    }
}
