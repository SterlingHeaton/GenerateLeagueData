package com.redslounge.main;

import com.redslounge.api.Information;
import com.redslounge.api.Limit;
import com.redslounge.api.Worker;
import com.redslounge.database.Database;
import com.redslounge.database.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class main
{
    private static String key = "";
    public static Limit limits = new Limit(0, 20, 1, 0, 100, 120);
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private static Database database;
    private static Query query;

    public static void main(String[] args)
    {
        String startingSummoner = "johnstyles";

        String urlBuilder = "https://na1.api.riotgames.com/lol/summoner/v3/summoners/by-name/" + startingSummoner + "?api_key=" + key;
        JSONObject parentSummoner = getJsonObject(urlBuilder);

        System.out.println("Starting to gather information!");

        Information.getInstance().getToBeSearched().add(parentSummoner);

        database = new Database("jdbc:mysql://localhost:3306/league", "admin", "password");
        database.connect();
        query = new Query(database);

        while(!Information.getInstance().getToBeSearched().isEmpty())
        {
            System.out.println("\nStarting while loop! Begining to search " + Information.getInstance().getToBeSearched().size() + " players!");
            for(int count = 0; count < Information.getInstance().getToBeSearched().size(); count++)
            {

                String gameId = getLastRankedGame(Information.getInstance().getToBeSearched().get(count).getLong("accountId"));

                if(gameId == null)
                {
                    continue;
                }
                Information.getInstance().getToBeAdded().addAll(getPlayers(gameId));
            }

            Information.getInstance().getToBeSearched().clear();

            // Search database if players already exist

            System.out.println("Collecting new players to search! Searching through " + Information.getInstance().getToBeAdded().size() + " games!");
            for(int count = 0; count < Information.getInstance().getToBeAdded().size(); count++)
            {
                String gameId = getLastRankedGame(Information.getInstance().getToBeAdded().get(count).getLong("accountId"));

                if(gameId == null)
                {
                    continue;
                }

                Information.getInstance().getToBeSearched().addAll(getPlayers(gameId));
            }

            System.out.println("Adding " + Information.getInstance().getToBeAdded().size() + " players to the database!");
            addPlayersToDatabase();

            Information.getInstance().getToBeAdded().clear();
        }

    }

    // https://na1.api.riotgames.com/lol/match/v3/matchlists/by-account/217885567?beginIndex=0&endIndex=1&queue=420&api_key=RGAPI-1fc91303-328a-4798-8912-36cfb62084c1
    private static String getLastRankedGame(long accountID)
    {
        String urlBuilder = "https://na1.api.riotgames.com/lol/match/v3/matchlists/by-account/" + accountID + "?beginIndex=0&endIndex=1&queue=420&api_key=" + key;

        try
        {
            JSONObject matchHistory = getJsonObject(urlBuilder);
            JSONArray matches = matchHistory.getJSONArray("matches");

            return String.valueOf(matches.getJSONObject(0).getLong("gameId"));
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
            players.add(jsonPlayers.getJSONObject(count).getJSONObject("player"));
        }

        return players;
    }

    public static void addPlayersToDatabase()
    {
        for(int count = 0; count < Information.getInstance().getToBeAdded().size(); count++)
        {
            JSONObject player = Information.getInstance().getToBeAdded().get(count);

            String sql = "INSERT IGNORE INTO summoner (accountId, summonerId, name) VALUES (" +
                    player.getInt("accountId") + ", " +
                    player.getInt("summonerId") + ", \"" +
                    player.getString("summonerName") + "\")";

            query.executeIntoDatabase(sql);
        }
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
                System.out.println("Something went wrong with the executor?!? " + e.toString());

                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception e2)
                {

                }
            }
        }

    }
}