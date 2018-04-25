package com.redslounge.database;

import java.sql.ResultSet;
import java.sql.Statement;

public class Query
{
    private Database database;
    private Statement statement;
    private ResultSet result;

    public Query(Database database)
    {
        this.database = database;

        try
        {
            statement = this.database.getConnection().createStatement();
        }
        catch(Exception e)
        {
            System.out.println("Failed to create instance of Query: " + e.toString());
        }
    }

    public void queryDatabase(String query)
    {
        try
        {
            result = statement.executeQuery(query);
        }
        catch(Exception e)
        {
            System.out.println("Failed to execute query: " + e.toString());
        }
    }

    public void executeIntoDatabase(String sql)
    {
        try
        {
            statement.executeUpdate(sql);
        }
        catch(Exception e)
        {
            System.out.println("Failed to insurt data into database: " + e.toString());
        }
    }
}
