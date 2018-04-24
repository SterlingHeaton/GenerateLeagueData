package com.redslounge.main;

import com.redslounge.api.Information;
import com.redslounge.api.Limit;
import com.redslounge.api.Worker;
import com.redslounge.database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class main
{
    private static String key = "RGAPI-1fc91303-328a-4798-8912-36cfb62084c1";
    public static Limit limits;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void main(String[] args)
    {
        Database database = new Database("jdbc:mysql://localhost:3306/league", "admin", "password");

        Scanner scanner = new Scanner(System.in);
        String urlBuilder = "https://na1.api.riotgames.com/lol/summoner/v3/summoners/by-name/" + scanner.nextLine() + "?api_key=" + key;
        scanner.close();
        JSONObject parentSummoner = getJsonObject(urlBuilder);

        Information.getInstance().getToBeSearched().add(parentSummoner);

        while(!Information.getInstance().getToBeSearched().isEmpty())
        {
            for(int count = 0; count < Information.getInstance().getToBeSearched().size(); count++)
            {
                String gameId = getLastRankedGame(Information.getInstance().getToBeSearched().get(count).getInt("accountId"));

                Information.getInstance().getToBeAdded().addAll(getPlayers(gameId));
            }
            Information.getInstance().getToBeSearched().clear();

            // Search database if players already exist

            for(int count = 0; count < Information.getInstance().getToBeAdded().size(); count++)
            {
                String gameId = getLastRankedGame(Information.getInstance().getToBeAdded().get(count).getInt("accountId"));

                Information.getInstance().getToBeSearched().addAll(getPlayers(gameId));
            }

            // Add toBeAdded players to database

            Information.getInstance().getToBeAdded().clear();
        }

    }

    // https://na1.api.riotgames.com/lol/match/v3/matchlists/by-account/217885567?beginIndex=0&endIndex=1&queue=420&api_key=RGAPI-1fc91303-328a-4798-8912-36cfb62084c1
    private static String getLastRankedGame(int accountID)
    {
        String urlBuilder = "https://na1.api.riotgames.com/lol/match/v3/matchlists/by-account/" + accountID + "?beginIndex=0&endIndex=1&queue=420&api_key=" + key;

        try
        {
            JSONObject matchHistory = getJsonObject(urlBuilder);
            JSONArray matches = matchHistory.getJSONArray("matches");

            return String.valueOf(matches.getJSONObject(0).getInt("gameId"));
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }
    // https://na1.api.riotgames.com/lol/match/v3/matches/2747867438?api_key=RGAPI-1fc91303-328a-4798-8912-36cfb62084c1
    private static List<JSONObject> getPlayers(String gameId)
    {
        String urlBuilder = "https://na1.api.riotgames.com/lol/match/v3/matches/" + gameId + "?api_key=" + key;

        JSONObject game = getJsonObject(urlBuilder);
        JSONArray jsonPlayers = game.getJSONArray("participantIdentities");
        List<JSONObject> players = new ArrayList<JSONObject>();

        for(int count = 0; count < jsonPlayers.length(); count++)
        {
            players.add(jsonPlayers.getJSONObject(count));
        }

        return players;
    }

    public static JSONObject getJsonObject(String url)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(executeAPICall(url));
            return jsonObject;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public JSONArray getJsonArray(String url)
    {
        try
        {
            JSONArray jsonArray = new JSONArray(executeAPICall(url));
            return jsonArray;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static String executeAPICall(String url)
    {
        Callable<String> called = new Worker(limits, url);
        Future<String> future = executor.submit(called);

        while(true)
        {
            try
            {
                future.get();
                return future.get();
            }
            catch(Exception e)
            {

            }
        }

    }
}