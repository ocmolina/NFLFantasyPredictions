/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players.util;

import com.ovidiomolina.nflfantasy.Teams;
import com.ovidiomolina.nflfantasy.model.players.Player;
import com.ovidiomolina.nflfantasy.model.players.PlayerConstants;
import com.ovidiomolina.util.ArrayUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate a training features set limited to what is read from the database
 * @author omolina
 */
public class DBReadTrainingFeaturesSet implements TrainingFeaturesSetGenerator {

    private final Player player;
    
    public DBReadTrainingFeaturesSet(Player player) {
        this.player = player;
    }
    
    protected List<List<Double>> createTrainingSetLists(String feature, String last3GameFeatureKey, String featureKey, List<Double> trainingRealValuesList) throws SQLException {
        Teams teams = Teams.getInstance();
        String sql = 
          String.format("select wk,team,%s,%s,team_avg_pass_yds_to_wk,team_avg_rush_yds_to_wk,team_avg_pts_to_wk,defense,defense_accepted_avg_pass_yds_to_wk,defense_accepted_rush_avg_yds_to_wk,total_sacks_by_defense_to_wk,total_fumbles_by_defense_to_wk,total_ints_by_defense_to_wk, defense_accepted_avg_pts_to_wk, team_avg_pos_time_to_wk,defense_avg_pos_time_to_wk from %s where player = '%s' and season >= %s and season <= %s and wk>1 and wk < 18",
                  feature,last3GameFeatureKey,player.getFromQueryView(),player.getPlayerId(),player.getInitialTrainingSeason(),player.getFinalTrainingSeason());
        System.out.println(sql);
        List<List<Double>> trainingSetLists = new ArrayList<>();
        ResultSet rs = player.getDatabase().executeQuery(sql);
        while(rs.next()) {
            player.setHasTrainData(true);
            trainingRealValuesList.add(player.softMaxNormalizeValue(featureKey,rs.getDouble(3)));
            List<Double> currentEntry = new ArrayList<>();
            currentEntry.add(1.0); //bias
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.WK,rs.getDouble(1)));//wk
            currentEntry.add(player.softMaxNormalizeValue(last3GameFeatureKey,rs.getDouble(4)));
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(5)));//team_avg_pass_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(6)));//team_avg_rush_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(7)));//team_avg_pts_to_wk
            int defenseId = teams.getTeamId(rs.getString(8));//defense
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAMS,(double)defenseId));
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK, rs.getDouble(9)));//defense_accepted_avg_pass_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(10)));//defense_accepted_rush_avg_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK, rs.getDouble(11)));//total_sacks_by_defense_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK, rs.getDouble(12)));//total_fumbles_by_defense_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK, rs.getDouble(13)));//total_ints_by_defense_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK, rs.getDouble(14)));//defense_accepted_avg_pts_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(15)));//team_avg_pos_time_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(16)));//defense_avg_pos_time_to_wk
            trainingSetLists.add(currentEntry);
        }
        
        return trainingSetLists;
    }
    
    @Override
    public double[][] initTrainingSet(String feature, String last3GameFeatureKey, String featureKey, List<Double> trainingRealValuesList) throws SQLException {
        List<List<Double>> trainingSetLists = createTrainingSetLists(feature, last3GameFeatureKey, featureKey, trainingRealValuesList);
        System.out.println("TRAINING ENTRIES: " + trainingSetLists.size());
        return generateTrainingSetMatrix(trainingSetLists);
    }
    
    @Override
    public double[][] initTrainingSet(String playerFeatures, String last3GameFeatureKeys, List<Double> trainingFPRealValuesList) throws SQLException {
        List<List<Double>> trainingSetLists = createTrainingSetLists(playerFeatures,last3GameFeatureKeys,trainingFPRealValuesList);
        System.out.println("TRAINING ENTRIES: " + trainingSetLists.size());
        return generateTrainingSetMatrix(trainingSetLists);
    }
    
    protected List<List<Double>> createTrainingSetLists(String playerFeatures, String last3GameFeatureKeys, List<Double> trainingFPRealValuesList) throws SQLException {
        Teams teams = Teams.getInstance();
        String sql = 
          String.format("select wk,team,fp,%s,%s,team_avg_pass_yds_to_wk,team_avg_rush_yds_to_wk,team_avg_pts_to_wk,defense,defense_accepted_avg_pass_yds_to_wk,defense_accepted_rush_avg_yds_to_wk,total_sacks_by_defense_to_wk,total_fumbles_by_defense_to_wk,total_ints_by_defense_to_wk, defense_accepted_avg_pts_to_wk, team_avg_pos_time_to_wk,defense_avg_pos_time_to_wk from %s where player = '%s' and season >= %s and season <= %s and wk>1 and wk < 18",
                  playerFeatures,last3GameFeatureKeys,player.getFromQueryView(),player.getPlayerId(),player.getInitialTrainingSeason(),player.getFinalTrainingSeason());
        System.out.println(sql);
        List<List<Double>> trainingSetLists = new ArrayList<>();
        ResultSet rs = player.getDatabase().executeQuery(sql);
        while(rs.next()) {
            player.setHasTrainData(true);
            trainingFPRealValuesList.add(player.softMaxNormalizeValue(PlayerConstants.FANTASY_POINTS,rs.getDouble(3)));
            List<Double> currentEntry = new ArrayList<>();
            currentEntry.add(1.0); //bias
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.WK,rs.getDouble(1)));//wk
            int nextFeature = player.addTrainingFeatureData(currentEntry,rs);
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(nextFeature++)));//team_avg_pass_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(nextFeature++)));//team_avg_rush_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(nextFeature++)));//team_avg_pts_to_wk
            int defenseId = teams.getTeamId(rs.getString(nextFeature++));//defense
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAMS,(double)defenseId));
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK, rs.getDouble(nextFeature++)));//defense_accepted_avg_pass_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(nextFeature++)));//defense_accepted_rush_avg_yds_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK, rs.getDouble(nextFeature++)));//total_sacks_by_defense_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK, rs.getDouble(nextFeature++)));//total_fumbles_by_defense_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK, rs.getDouble(nextFeature++)));//total_ints_by_defense_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK, rs.getDouble(nextFeature++)));//defense_accepted_avg_pts_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(nextFeature++)));//team_avg_pos_time_to_wk
            currentEntry.add(player.softMaxNormalizeValue(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(nextFeature++)));//defense_avg_pos_time_to_wk
            trainingSetLists.add(currentEntry);
        }
        return trainingSetLists;
    }
    
    protected double [][] generateTrainingSetMatrix(List<List<Double>> trainingSetLists) {
        if(player.hasTrainData()) {
            double trainingSet[][] = new double[trainingSetLists.size()][trainingSetLists.get(0).size()];
            for(int i = 0; i< trainingSetLists.size(); i++) {
                List<Double> current = trainingSetLists.get(i);
                trainingSet[i] = ArrayUtils.createPrimitiveDoubleArray(current.toArray(new Double[current.size()]));
            }
            return trainingSet;
        }
        return null;
    }
    
    protected Player getPlayer() {
        return player;
    }

    
}
