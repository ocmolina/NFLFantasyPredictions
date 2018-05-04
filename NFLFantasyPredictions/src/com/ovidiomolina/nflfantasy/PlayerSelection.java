/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy;

import com.ovidiomolina.db.Database;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author omolina
 */
public class PlayerSelection {
    
    public static final String[] POSITIONS = {"QB","WR","RB","TE"};
    public static final String[] TEAMS = 
        {"ARI","ATL","BAL","BUF","CAR","CHI","CIN","CLE",
         "DAL","DEN","DET","GB","HOU","IND","JAC","KC",
         "MIA","MIN","NE","NO","NYG","NYJ","OAK","PHI",
         "PIT","LAC","SEA","SF","LA","TB","TEN","WAS"};
    public static final String[] SEASONS = 
            {"2000","2001","2002","2003","2004","2005",
             "2006","2007","2008","2009","2010","2011",
             "2012","2013","2014","2015", "2016", "2017"};
    public static final String[] WEEK_SELECTION = {"2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17"};
    private HashMap<String,List<PlayerSelection.Player>> players;
    private final Database db;
    
    public PlayerSelection(Database db) throws Exception {
        this.db  = db;
        players = new HashMap<>();
        initPlayers();
    }
    
    private void initPlayers() throws Exception {
        
        BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/ovidiomolina/nflfantasy/allplayersmorethan17games.txt")));
        StringBuffer sqlBuffer = new StringBuffer();
        while(br.ready()) {
            sqlBuffer.append(String.format(" %s", br.readLine()));
        }
        ResultSet rs = db.executeQuery(sqlBuffer.toString());
        while(rs.next()) {
            String team = findPlayerCurrentTeam(rs.getString(2));
            String pos = rs.getString(1);
            String id = rs.getString(2);
            String name = rs.getString(3);
            PlayerSelection.Player player = new PlayerSelection.Player(id,name, pos, team);
            player.setGames(rs.getInt(4));
            if(players.containsKey(String.format("%s-%s",team,pos))) {
                List<PlayerSelection.Player> playersList = players.get(String.format("%s-%s",team,pos));
                playersList.add(player);
            }
            else {
                List<PlayerSelection.Player> playersList = new ArrayList<PlayerSelection.Player>();
                playersList.add(player);
                players.put(String.format("%s-%s",team,pos), playersList);
            }
        }
    }
    
    private String findPlayerCurrentTeam(String playerId) throws SQLException {
        String query = String.format("select cteam from player where player = '%s'",playerId);
        ResultSet rs = db.executeQuery(query);
        String team = "";
        while(rs.next()) {
            team = rs.getString(1);
        }
        return team;
    }
    
    public List<PlayerSelection.Player> getPlayers(String key) {
        return players.get(key);
    }
    
    public List<PlayerSelection.Player> getPlayersByPosition(String position) {
        List<PlayerSelection.Player> allPlayersForPosition = new ArrayList<>();
        for(String key : players.keySet()) {
            if(key.endsWith(position)) {
                allPlayersForPosition.addAll(players.get(key));
            }
        }
        return allPlayersForPosition;
    }
    
    
    public class Player implements Comparable<Player> {
        private final String id;
        private final String name;
        private final String pos;
        private final String team;
        private double fantasyPts;
        private int games;
        
        public Player(String id, String name, String pos, String team) {
            this.id = id;
            this.name = name;
            this.pos = pos;
            this.team = team;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPosition() {
            return pos;
        }

        public String getTeam() {
            return team;
        }

        public double getFantasyPts() {
            return fantasyPts;
        }

        public void setFantasyPts(double fantasyPts) {
            this.fantasyPts = fantasyPts;
        }

        public int getGames() {
            return games;
        }

        public void setGames(int games) {
            this.games = games;
        }
        
        

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(Player playerObj) {
            if(getFantasyPts() < playerObj.getFantasyPts()) {
                return -1;
            }
            if(getFantasyPts() > playerObj.getFantasyPts()) {
                return 1;
            }
            return 0;
        }
    }
}
