/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players.util;

import com.ovidiomolina.nflfantasy.Teams;
import com.ovidiomolina.nflfantasy.model.players.Player;
import com.ovidiomolina.nflfantasy.model.players.PlayerConstants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generate a training features set from the db query and add generated data by using
 * DefenseGeneratedFeautres class
 * @author omolina
 */
public class TrainingFeaturesSetWithNaiveDataGeneration extends DBReadTrainingFeaturesSet{
    
    public TrainingFeaturesSetWithNaiveDataGeneration(Player player) {
        super(player);
    }
    
    @Override
    public double[][] initTrainingSet(String feature, String last3GameFeatureKey, String featureKey, List<Double> trainingRealValuesList) throws SQLException {
        List<List<Double>> trainingSetLists = createTrainingSetLists(feature, last3GameFeatureKey, featureKey, trainingRealValuesList);
        Teams teams = Teams.getInstance();
        Random random = new Random(System.currentTimeMillis());
        DefenseGeneratedFeatures randomFeatureValueGenerator = new DefenseGeneratedFeatures(getPlayer().getDatabase(), getPlayer().getInitialTrainingSeason(),getPlayer().getFinalTrainingSeason());
        int simulatedSeasons = (300 - trainingSetLists.size())/15;
        getPlayer().setSimulatedSeasons(simulatedSeasons);
        for(int i = 0; i<simulatedSeasons; i++) { 
            randomFeatureValueGenerator.clearValues();
            randomFeatureValueGenerator.generateRandomValues();
            for(int j = 2; j<=17; j++) { //same set of weeks for each entry for player's team
                double featureKeyValue = getPlayer().generateRandomValueFromGaussianDistribution(featureKey, random.nextDouble());
                int size =trainingRealValuesList.size();
                double last3GameAvgFeature = (getPlayer().softMaxDenormalizeValue(featureKey,trainingRealValuesList.get(size -1)) 
                        + getPlayer().softMaxDenormalizeValue(featureKey,trainingRealValuesList.get(size - 2)) + 
                        getPlayer().softMaxDenormalizeValue(featureKey,trainingRealValuesList.get(size -3)))/3;
                trainingRealValuesList.add(getPlayer().softMaxNormalizeValue(featureKey,featureKeyValue));
                List<Double> currentEntry = new ArrayList<>();
                currentEntry.add(1.0); //bias
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.WK,j));//wk
                currentEntry.add(getPlayer().softMaxNormalizeValue(last3GameFeatureKey,last3GameAvgFeature));
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PASS_YDS,
                    getPlayer().generateRandomValueFromGaussianDistribution(PlayerConstants.TEAM_AVG_PASS_YDS,random.nextDouble())));//team_avg_pass_yds_to_wk
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TEAM_AVG_RUSH_YDS,
                    getPlayer().generateRandomValueFromGaussianDistribution(PlayerConstants.TEAM_AVG_RUSH_YDS,random.nextDouble())));//team_avg_rush_yds_to_wk
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PTS,
                    getPlayer().generateRandomValueFromGaussianDistribution(PlayerConstants.TEAM_AVG_PTS,random.nextDouble())));//team_avg_pts_to_wk
                int currentDefenseInt = teams.getRandomTeamExcluding(getPlayer().getPlayerTeam());
                String currentDefense = teams.getTeamValueForId(currentDefenseInt);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TEAMS,currentDefenseInt));
                List<Double> list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,
                        getFeatureAvgToWkValue(list, j)));
                list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,
                        getFeatureAvgToWkValue(list, j)));
                list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,
                        getFeatureAvgToWkValue(list, j)));
                list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,
                        getFeatureAvgToWkValue(list, j)));
                list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,
                        getFeatureAvgToWkValue(list, j)));
                list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,
                        getFeatureAvgToWkValue(list, j)));
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.TEAM_AVG_POS_TIME,
                        getPlayer().generateRandomValueFromGaussianDistribution(PlayerConstants.TEAM_AVG_POS_TIME,random.nextDouble())));//team_avg_pos_time_to_wk
                list = randomFeatureValueGenerator.getTeamGeneratedFeatureList(currentDefense, PlayerConstants.DEFENSE_AVG_POS_TIME);
                currentEntry.add(getPlayer().softMaxNormalizeValue(PlayerConstants.DEFENSE_AVG_POS_TIME,
                        getFeatureAvgToWkValue(list, j)));
                trainingSetLists.add(currentEntry);
            }
        }
        return generateTrainingSetMatrix(trainingSetLists);
    }
    
    private double getFeatureAvgToWkValue(List<Double> featureList, int wk) {
        double count = 0;
        double sum = 0.0;
        for(int i = 0; i<wk -1; i ++) {
            sum += featureList.get(i);
            count++;
        }
        return sum/count;
    }
    
}
