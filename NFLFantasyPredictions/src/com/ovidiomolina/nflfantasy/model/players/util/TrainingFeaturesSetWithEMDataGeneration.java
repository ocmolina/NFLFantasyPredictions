/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players.util;

import com.ovidiomolina.nflfantasy.model.players.Player;
import com.ovidiomolina.nflfantasy.model.players.PlayerConstants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.distribution.MixtureMultivariateNormalDistribution;
import org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximization;

/**
 *
 * @author omolina
 */
public class TrainingFeaturesSetWithEMDataGeneration extends DBReadTrainingFeaturesSet{

    public TrainingFeaturesSetWithEMDataGeneration(Player player) {
        super(player);
    }
    
    @Override
    public double[][] initTrainingSet(String feature, String last3GameFeatureKey, String featureKey, List<Double> trainingRealValuesList) throws SQLException {
        List<List<Double>> trainingSetLists = createTrainingSetLists(feature, last3GameFeatureKey, featureKey, trainingRealValuesList);
        Random random = new Random(System.currentTimeMillis());
        double currentData[][] = new double[trainingSetLists.size()][];
        for(int i = 0; i<currentData.length; i++) {
            List<Double> currentFeatures = trainingSetLists.get(i);
            currentData[i] = new double[] {
                currentFeatures.get(3),currentFeatures.get(4),currentFeatures.get(5),
                currentFeatures.get(6),currentFeatures.get(7),currentFeatures.get(8),
                currentFeatures.get(9),currentFeatures.get(10),currentFeatures.get(11),
                currentFeatures.get(12),currentFeatures.get(13),currentFeatures.get(14)
            };
        }
        MultivariateNormalMixtureExpectationMaximization emImpl = new MultivariateNormalMixtureExpectationMaximization(currentData);
        int components = currentData[0].length;
        MixtureMultivariateNormalDistribution initialDist = MultivariateNormalMixtureExpectationMaximization.estimate(currentData, components);
        emImpl.fit(initialDist);
        MixtureMultivariateNormalDistribution fittedDist = emImpl.getFittedModel();
        if(fittedDist == null) {
            System.out.println("error fitting model");
            return null;
        }
        int simulatedSeasons = (300 - trainingSetLists.size())/15;
        getPlayer().setSimulatedSeasons(simulatedSeasons);
        for(int i = 0; i<simulatedSeasons; i++) { 
            for(int j = 2; j<=17; j++) { //same set of weeks for each entry for player's team
                double sample[] = fittedDist.sample();
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
                for(int k = 0; k<sample.length; k++) {
                    currentEntry.add(sample[k]);
                }
                trainingSetLists.add(currentEntry);
            }
        }
        return generateTrainingSetMatrix(trainingSetLists);
    }
    
}
