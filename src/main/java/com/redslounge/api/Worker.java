package com.redslounge.api;

import com.redslounge.main.main;

import java.util.concurrent.Callable;

public class Worker implements Callable<String>
{
    private Limit limits;
    private String url;
    private ServerConnection connection;

    public Worker(Limit limits, String url)
    {
        this.limits = limits;
        this.url = url;
    }

    public String call() throws Exception
    {
        checkLimits();

        if(isConnectionNull())
        {
            System.out.println("API connection resulted in no JSONBody checking response code!");

            if(connection.getResponseCode() == 429)
            {
                System.out.println("API response code is: 429. Retrying in " + connection.getHeaders().get("Retry-After").get(0) + " seconds!");
                sleepyTime(Integer.parseInt(connection.getHeaders().get("Retry-After").get(0)));

                if(isConnectionNull())
                {
                    System.out.println("API connection still resulted in no JSONBody. Response code: " + connection.getResponseCode());
                    System.out.println(url);
                    return null;
                }
            }
            else
            {
                System.out.println("Unhandled response code: " + connection.getResponseCode());
                System.out.println(url);
                return null;
            }
        }

        main.limits.update(getHeaderInformation("secondCurrent"), getHeaderInformation("minuteCurrent"));

        return connection.getJsonBody();
    }

    public void checkLimits()
    {
        if(limits.isSecondReached())
        {
            if(limits.isMinuteReached())
            {
                return;
            }
            System.out.println("API limit is being limited! " + limits.getMinuteTime() + " seconds!");
            sleepyTime(limits.getMinuteTime());
        }
        else
        {
            if(limits.isMinuteReached())
            {
                System.out.println("API limit is being limited! " + limits.getSecondTime() + " seconds!");
                sleepyTime(limits.getSecondTime());
                return;
            }
            System.out.println("API limit is being limited! " + limits.getMinuteTime() + " seconds!");
            sleepyTime(limits.getMinuteTime());
        }
    }

    private boolean isConnectionNull()
    {
        connection = new ServerConnection(url);

        if(connection.getJsonBody() == null)
        {
            return true;
        }
        return false;
    }

    public int getHeaderInformation(String mode)
    {
        String unparsed = connection.getHeaders().get("X-App-Rate-Limit-Count").get(0);

        if(mode == "secondCurrent")
        {
            String[] semiParsed = unparsed.split(",");
            String[] parced = semiParsed[0].split(":");
            return Integer.parseInt(parced[0]);
        }
        else if(mode == "minuteCurrent")
        {
            String[] semiParced = unparsed.split(",");
            String[] parced = semiParced[1].split(":");
            return Integer.parseInt(parced[0]);
        }
        else
        {
            return 3;
        }
    }

    private void sleepyTime(int time)
    {
        try
        {
            Thread.sleep(time*1000);
        }
        catch(InterruptedException e)
        {

        }
    }
}
