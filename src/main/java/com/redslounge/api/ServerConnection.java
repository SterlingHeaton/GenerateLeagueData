package com.redslounge.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerConnection
{
    private URL url;
    private HttpURLConnection connection;
    private BufferedReader reader;
    private Map<String, List<String>> headers;
    private int responseCode;
    private String jsonBody;

    public ServerConnection(String url)
    {
        try
        {
            this.url = new URL(url);
            connection = (HttpURLConnection) this.url.openConnection();
            responseCode = connection.getResponseCode();
            headers = connection.getHeaderFields();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonBody = reader.readLine();
        }
        catch(IOException e)
        {
            //Something goes here LUL
        }
    }

    public Map<String, List<String>> getHeaders()
    {
        return headers;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    public String getJsonBody()
    {
        return jsonBody;
    }
}
