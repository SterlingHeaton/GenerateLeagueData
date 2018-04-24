package com.redslounge.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database
{
    private Connection connection;
    private String serverUrl;
    private String username;
    private String password;

    public Database(String serverUrl, String username, String password)
    {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
    }

    public void connect()
    {
        try
        {
            connection = DriverManager.getConnection(serverUrl, username, password);
        }
        catch(Exception e)
        {
            System.out.println("Failed to connect to the database: " + e.toString());
        }
    }

    public Connection getConnection()
    {
        return connection;
    }
}
