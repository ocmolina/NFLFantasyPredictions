/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players.util;

import com.ovidiomolina.db.Database;
import com.ovidiomolina.nflfantasy.Teams;
import com.ovidiomolina.nflfantasy.model.players.PlayerConstants;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 *
 * @author omolina
 */
public class DefenseGeneratedFeatures {
    
    private Map<String,Map<String,List<Double>>> features;
    private Map<String,Map<String,TeamMeanAndStddevValues>> teamMeanAndStdDevMap;
    private final Database db;
    private String startSeason;
    private String endSeason;

    public DefenseGeneratedFeatures(final Database db, String initSeason, String finalSeason) {
        features = new HashMap<>();
        teamMeanAndStdDevMap = new HashMap<>();
        this.db = db;
        startSeason = initSeason;
        endSeason = finalSeason;
        initialize();
    }
    
    private void initialize() {
        Teams allTeams = Teams.getInstance();
        List<Thread> threads = new ArrayList<>();
        for(String key : allTeams.getAllTeamKeys()) {
            features.put(key,new HashMap<>());
            features.get(key).put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK, new ArrayList<>());
            features.get(key).put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK, new ArrayList<>());
            features.get(key).put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK, new ArrayList<>());
            features.get(key).put(PlayerConstants.DEFENSE_AVG_POS_TIME, new ArrayList<>());
            features.get(key).put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK, new ArrayList<>());
            features.get(key).put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK, new ArrayList<>());
            features.get(key).put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK, new ArrayList<>());
            Thread t = new Thread() {
              public void run() {
                  initMeanAndStddevForTeam(key);
              }  
            };
            threads.add(t);

        }
        for(Thread t : threads) {
            t.start();
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DefenseGeneratedFeatures.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void generateRandomValues() {
        for(String team : features.keySet()) {
            for(int i = 0; i < 18; i++) {
                synchronized(features) {
                    Map<String,List<Double>> currentTeamStats = features.get(team);
                    currentTeamStats.get(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK, Math.random()));
                    currentTeamStats.get(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK, Math.random()));
                    currentTeamStats.get(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK, Math.random()));
                    currentTeamStats.get(PlayerConstants.DEFENSE_AVG_POS_TIME).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.DEFENSE_AVG_POS_TIME, Math.random()));
                    currentTeamStats.get(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK, Math.random()));
                    currentTeamStats.get(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK, Math.random()));
                    currentTeamStats.get(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK).add(
                            generateRandomValueFromGaussianDistribution(team, PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK, Math.random()));
                }
            }
        }
    }
    public void clearValues() {
        for(String key : features.keySet()) {
            features.get(key).get(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK).clear();
            features.get(key).get(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK).clear();
            features.get(key).get(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK).clear();
            features.get(key).get(PlayerConstants.DEFENSE_AVG_POS_TIME).clear();
            features.get(key).get(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK).clear();
            features.get(key).get(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK).clear();
            features.get(key).get(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK).clear();
        }
    }
    private void initMeanAndStddevForTeam(String team) {
        //ugly patch for change in teams.
        String teamForQuery = team;
        if(team.equals("LA")) {
            teamForQuery = "STL";
        }
        try {
            String query = String.format(
                    "select "+
                            "stddev(rush_yds_against),avg(rush_yds_against), "+
                            "stddev(pass_yds_against),avg(pass_yds_against), "+
                            "stddev(points),avg(points), "+
                            "stddev(possession_time),avg(possession_time), "+
                            "stddev(sacks_by_defense),avg(sacks_by_defense), "+
                            "stddev(fumbles_by_defense),avg(fumbles_by_defense), "+
                            "stddev(ints_by_defense),avg(ints_by_defense) " +
                            "from features where team = '%s' and season >= %s and season <= %s ",
                        teamForQuery,startSeason,endSeason);
            ResultSet rs = db.executeQuery(query);
            while(rs.next()) {
                synchronized (teamMeanAndStdDevMap) {
                    Map<String,TeamMeanAndStddevValues> current = new HashMap<>();
                    current.put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK, new TeamMeanAndStddevValues(rs.getDouble(1),rs.getDouble(2)));
                    current.put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK, new TeamMeanAndStddevValues(rs.getDouble(3),rs.getDouble(4)));    
                    current.put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK, new TeamMeanAndStddevValues(rs.getDouble(5),rs.getDouble(6)));
                    current.put(PlayerConstants.DEFENSE_AVG_POS_TIME, new TeamMeanAndStddevValues(rs.getDouble(7),rs.getDouble(8)));
                    current.put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK, new TeamMeanAndStddevValues(rs.getDouble(9),rs.getDouble(10)));
                    current.put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK, new TeamMeanAndStddevValues(rs.getDouble(11),rs.getDouble(12)));
                    current.put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK, new TeamMeanAndStddevValues(rs.getDouble(13),rs.getDouble(14)));
                    teamMeanAndStdDevMap.put(team, current);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DefenseGeneratedFeatures.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<Double> getTeamGeneratedFeatureList(String team, String featureKey) {
        return features.get(team).get(featureKey);
    }
    
    private double generateRandomValueFromGaussianDistribution(String team,
            String featureKey, double probability)
    {
        double mean = teamMeanAndStdDevMap.get(team).get(featureKey).mean;
        double stddev = teamMeanAndStdDevMap.get(team).get(featureKey).stddev;
        NormalDistribution nd = new NormalDistribution(mean, stddev);
        double value = nd.inverseCumulativeProbability(1-probability);
        return value;
    }

    
    private class TeamMeanAndStddevValues {
        public double mean;
        public double stddev;
        
        public TeamMeanAndStddevValues(double mean, double stddev)
        {
            this.mean = mean;
            this.stddev = stddev;
        }
    }
}
