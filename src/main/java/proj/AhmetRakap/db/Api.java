package proj.AhmetRakap.db;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Api implements AutoCloseable
{
    private final String dbURL = "jdbc:postgresql://localhost:5432/ummt";
    private final Connection conn;

    public Api() throws SQLException
    {
        conn = DriverManager.getConnection(dbURL);
    }

    public Boolean login(User usr)
    {
        String query="SELECT * FROM users U WHERE U.username=? AND U.password=?;";
        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, usr.getPassword());
            return stmt.executeQuery().next();
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public Boolean admin(User usr)
    {
        String query="SELECT U.admin FROM users U WHERE U.username=? AND U.password=?";
        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, usr.getPassword());
            ResultSet res = stmt.executeQuery();
            res.next();
            return res.getBoolean(1);
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public ArrayList<Message> inbox(User usr)
    {
        try
        {
            String query = "SELECT * FROM registry R WHERE R.mto=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            ResultSet res = stmt.executeQuery();
            ArrayList<Message> msglist = new ArrayList<Message>();
            while(res.next())
                msglist.add(new Message(res.getString(1), res.getString(2), res.getObject(3, LocalDateTime.class), res.getString(4)));
            return msglist;
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return null;
        }
    }

    public ArrayList<Message> inbox(User usr, String _from) throws IllegalArgumentException
    {
        if(!existsUser(new User(_from)))
            throw new IllegalArgumentException();
        try
        {
            String query = "SELECT * FROM registry R WHERE R.mto=? AND R.mfrom=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, _from);
            ResultSet res = stmt.executeQuery();
            ArrayList<Message> msglist = new ArrayList<Message>();
            while(res.next())
                msglist.add(new Message(res.getString(1), res.getString(2), res.getObject(3, LocalDateTime.class), res.getString(4)));
            return msglist;
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return null;
        }
    }

    public ArrayList<Message> outbox(User usr)
    {
        try
        {
            String query = "SELECT * FROM registry R WHERE R.mfrom=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            ResultSet res = stmt.executeQuery();
            ArrayList<Message> msglist = new ArrayList<Message>();
            while (res.next())
                msglist.add(new Message(res.getString(1), res.getString(2), res.getObject(3, LocalDateTime.class), res.getString(4)));
            return msglist;
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return null;
        }
    }

    public ArrayList<Message> outbox(User usr, String _to) throws IllegalArgumentException
    {
        if(!existsUser(new User(_to)))
            throw new IllegalArgumentException();
        try
        {
            String query = "SELECT * FROM registry R WHERE R.mfrom=? AND R.mto=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, _to);
            ResultSet res = stmt.executeQuery();
            ArrayList<Message> msglist = new ArrayList<Message>();
            while(res.next())
                msglist.add(new Message(res.getString(1), res.getString(2), res.getObject(3, LocalDateTime.class), res.getString(4)));
            return msglist;
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return null;
        }
    }

    public Boolean sendMessage(Message msg)
    {
        String query = "INSERT INTO registry (mto, mfrom, date, content) VALUES (?, ?, ?, ?)";
        try
        {
            if(!existsUser(new User(msg.getTo())) || !existsUser(new User(msg.getFrom())))
                return false;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, msg.getTo());
            stmt.setString(2, msg.getFrom());
            stmt.setObject(3, msg.getDate());
            stmt.setString(4, msg.getContent());
            return stmt.executeUpdate()!=0;
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public Boolean addUser(User usr)
    {
        String query = "INSERT INTO users (username, password, admin, firstName, lastName, birthday, gender, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, usr.getPassword());
            stmt.setBoolean(3, usr.getAdmin());
            stmt.setString(4, usr.getFirstName());
            stmt.setString(5, usr.getLastName());
            stmt.setObject(6, usr.getBirthday());
            stmt.setString(7, String.valueOf(usr.getGender()));
            stmt.setString(8, usr.getEmail());
            return stmt.executeUpdate()!=0;
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public Boolean existsUser(User usr)
    {
        String query = "SELECT COUNT(*) FROM users U WHERE U.username=?;";
        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            ResultSet res = stmt.executeQuery();
            res.next();
            return res.getInt(1)!=0;
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public User makeUser(String usrname)
    {
        String query = "SELECT * FROM users U WHERE U.username=?;";
        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usrname);
            ResultSet res = stmt.executeQuery();
            res.next();
            return new User(res.getString(1), res.getString(2), res.getBoolean(3), res.getString(4), res.getString(5), res.getObject(6, LocalDate.class), res.getString(7).equals("M")?'M':'F', res.getString(8));
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return new User();
        }
    }

    public Boolean updateUser(User usr)
    {
        String query = "UPDATE users SET password=?, admin=?, firstname=?, lastname=?, birthday=?, gender=?, email=? WHERE username=?;";
        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getPassword());
            stmt.setBoolean(2, usr.getAdmin());
            stmt.setString(3, usr.getFirstName());
            stmt.setString(4, usr.getLastName());
            stmt.setObject(5, usr.getBirthday());
            stmt.setString(6, usr.getGender().toString());
            stmt.setString(7, usr.getEmail());
            stmt.setString(8, usr.getUsername());
            return stmt.executeUpdate()!=0;
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public Boolean removeUser(User usr)
    {
        String query = "DELETE FROM users U WHERE U.username=?;";
        try
        {
            if(!existsUser(new User(usr.getUsername())))
                return false;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            return stmt.executeUpdate()!=0;
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public ArrayList<User> listUsers()
    {
        try
        {
            String query = "SELECT * FROM users;";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            ArrayList<User> usrlist = new ArrayList<>();
            while (res.next())
                usrlist.add(new User(res.getString(1), res.getString(2), res.getBoolean(3), res.getString(4), res.getString(5), res.getObject(6, LocalDate.class), res.getString(7).equals("M") ? 'M' : 'F', res.getString(8)));
            return usrlist;
        }
        catch (SQLException err)
        {
            err.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws SQLException
    {
        conn.close();
    }
}

