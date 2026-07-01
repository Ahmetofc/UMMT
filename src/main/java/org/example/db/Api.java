package org.example.db;

import java.sql.*;

public class Api
{
    private final static String dbURL = "jdbc:postgresql://localhost:5432/ummt";

    public Boolean login(User usr)
    {
        String query="SELECT * FROM users U WHERE U.username=? AND U.password=?;";
        try
        {
            Connection conn = DriverManager.getConnection(dbURL);
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
            Connection conn = DriverManager.getConnection(dbURL);
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

    public ResultSet inbox(User usr) throws SQLException
    {
        String query="SELECT * FROM registry R WHERE R.mto=?";
        Connection conn = DriverManager.getConnection(dbURL);
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, usr.getUsername());
        return stmt.executeQuery();
    }

    public ResultSet inbox(User usr, String _from) throws SQLException, IllegalArgumentException
    {
        if(!existsUser(new User(_from)))
            throw new IllegalArgumentException();
        String query="SELECT * FROM registry R WHERE R.mto=? AND R.mfrom=?";
        Connection conn = DriverManager.getConnection(dbURL);
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, usr.getUsername());
        stmt.setString(2, _from);
        return stmt.executeQuery();
    }

    public ResultSet outbox(User usr) throws SQLException
    {
        String query="SELECT * FROM registry R WHERE R.mfrom=?";
        Connection conn = DriverManager.getConnection(dbURL);
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, usr.getUsername());
        return stmt.executeQuery();
    }

    public ResultSet outbox(User usr, String _to) throws SQLException, IllegalArgumentException
    {
        if(!existsUser(new User(_to)))
            throw new IllegalArgumentException();
        String query="SELECT * FROM registry R WHERE R.mfrom=? AND R.mto=?";
        Connection conn = DriverManager.getConnection(dbURL);
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, usr.getUsername());
        stmt.setString(2, _to);
        return stmt.executeQuery();
    }

    public Boolean sendMessage(Message msg)
    {
        String query = "INSERT INTO registry (mto, mfrom, date, content) VALUES (?, ?, ?, ?)";
        try
        {
            if(!existsUser(new User(msg.getTo())) || !existsUser(new User(msg.getFrom())))
                return false;
            Connection conn = DriverManager.getConnection(dbURL);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, msg.getTo());
            stmt.setString(2, msg.getFrom());
            stmt.setString(3, msg.getDate());
            stmt.setString(4, msg.getContent());
            stmt.executeUpdate();
            return true;
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
            Connection conn = DriverManager.getConnection(dbURL);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.setString(2, usr.getPassword());
            stmt.setBoolean(3, usr.getAdmin());
            stmt.setString(4, usr.getFirstName());
            stmt.setString(5, usr.getLastName());
            stmt.setString(6, usr.getBirthday());
            stmt.setString(7, String.valueOf(usr.getGender()));
            stmt.setString(8, usr.getEmail());
            stmt.executeUpdate();
            return true;
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
            Connection conn = DriverManager.getConnection(dbURL);
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

    public Boolean removeUser(User usr)
    {
        String query = "DELETE FROM users U WHERE U.username=?;";
        try
        {
            if(!existsUser(new User(usr.getUsername())))
                return false;
            Connection conn = DriverManager.getConnection(dbURL);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, usr.getUsername());
            stmt.executeUpdate();
            return true;
        }
        catch(SQLException err)
        {
            err.printStackTrace();
            return false;
        }
    }

    public ResultSet listUsers() throws SQLException
    {
        String query = "SELECT * FROM users;";
        Connection conn = DriverManager.getConnection(dbURL);
        PreparedStatement stmt = conn.prepareStatement(query);
        return stmt.executeQuery();
    }
}

