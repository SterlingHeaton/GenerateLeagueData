package com.redslounge.api;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Information
{
    private static Information ourInstance = new Information();

    private List<JSONObject> toBeAdded = new ArrayList<JSONObject>();
    private List<JSONObject> toBeSearched = new ArrayList<JSONObject>();

    public static Information getInstance()
    {
        return ourInstance;
    }

    private Information()
    {
    }

    public List<JSONObject> getToBeAdded()
    {
        return toBeAdded;
    }

    public List<JSONObject> getToBeSearched()
    {
        return toBeSearched;
    }
}
