/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author omolina
 */
public class Teams {
    private static Teams _instance;
    private static HashMap<String,Integer> teamKeys;
    public static final double[] TEAM_ID_ARRAY = 
        {
            1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,
            9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,
            17.0,18.0,19.0,20.0,21.0,22.0,23.0,24.0,
            25.0,26.0,27.0,28.0,29.0,30.0,31.0,32.0
        };
    private Teams(){
        teamKeys = new HashMap<String,Integer>();
        teamKeys.put("ARI",1);
        teamKeys.put("ATL",2);
        teamKeys.put("BAL",3);
        teamKeys.put("BUF",4);
        teamKeys.put("CAR",5);
        teamKeys.put("CHI",6);
        teamKeys.put("CIN",7);
        teamKeys.put("CLE",8);
        teamKeys.put("DAL",9);
        teamKeys.put("DEN",10);
        teamKeys.put("DET",11);
        teamKeys.put("GB",12);
        teamKeys.put("HOU",13);
        teamKeys.put("IND",14);
        teamKeys.put("JAC",15);
        teamKeys.put("KC",16);
        teamKeys.put("MIA",17);
        teamKeys.put("MIN",18);
        teamKeys.put("NE",19);
        teamKeys.put("NO",20);
        teamKeys.put("NYG",21);
        teamKeys.put("NYJ",22);
        teamKeys.put("OAK",23);
        teamKeys.put("PHI",24);
        teamKeys.put("PIT",25);
        teamKeys.put("LAC",26);
        teamKeys.put("SEA",27);
        teamKeys.put("SF",28);
        teamKeys.put("LA",29);
        teamKeys.put("TB",30);
        teamKeys.put("TEN",31);
        teamKeys.put("WAS",32);
    }
    
    synchronized public static Teams getInstance() {
        if(_instance == null) {
            _instance = new Teams();
        }
        return _instance;
    }
    
    synchronized public  int getTeamId(String team) {
        if(team.equals("STL")) return 29; //ugly patch for new team
        if(team.equals("SD")) return 26;
        return teamKeys.get(team);
    }
    
    synchronized public  String getTeamValueForId(int teamId) {
        for(Map.Entry<String,Integer> item : teamKeys.entrySet()) {
            if(item.getValue() == teamId) {
                return item.getKey();
            }
        }
        return null;
    }
    
    synchronized public Set<String> getAllTeamKeys() {
        return teamKeys.keySet();
    }
    
    synchronized public int getRandomTeamExcluding(String team) {
        Random r = new Random(System.currentTimeMillis());
        int excludeTeam = teamKeys.get(team);
        int randomTeam = (int) TEAM_ID_ARRAY[r.nextInt(32)];
        while(excludeTeam == randomTeam) {
            randomTeam = (int) TEAM_ID_ARRAY[r.nextInt(32)];
        }
        return randomTeam;
    }
}
