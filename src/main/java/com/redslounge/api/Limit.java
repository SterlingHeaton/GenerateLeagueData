package com.redslounge.api;

public class Limit
{
    private int secondCurrent;
    private int secondLimit;
    private int secondTime;
    private int minuteCurrent;
    private int minuteLimit;
    private int minuteTime;

    public Limit(int secondCurrent, int secondLimit, int secondTime, int minuteCurrent, int minuteLimit, int minuteTime)
    {
        this.secondCurrent = secondCurrent;
        this.secondLimit = secondLimit;
        this.secondTime = secondTime;
        this.minuteCurrent = minuteCurrent;
        this.minuteLimit = minuteLimit;
        this.minuteTime = minuteTime;
    }

    public void update(int secondCurrent, int minuteCurrent)
    {
        this.secondCurrent = secondCurrent;
        this.minuteCurrent = minuteCurrent;
    }

    public boolean isSecondReached()
    {
        return (secondCurrent < secondLimit);
    }

    public boolean isMinuteReached()
    {
        return (minuteCurrent < minuteLimit);
    }

    public int getSecondTime()
    {
        return secondTime;
    }

    public int getMinuteTime()
    {
        return minuteTime;
    }
}
