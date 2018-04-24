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
            if(connection.getResponseCode() == 429)
            {
                sleepyTime(Integer.parseInt(connection.getHeaders().get("Retry-After").get(0)));

                if(isConnectionNull())
                {
                    return null;
                }
            }
            else
            {
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
            sleepyTime(limits.getMinuteTime());
        }
        else
        {
            if(limits.isMinuteReached())
            {
                sleepyTime(limits.getSecondTime());
                return;
            }
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
