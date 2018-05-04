/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players.util;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author omolina
 */
public interface TrainingFeaturesSetGenerator {
    
    double[][] initTrainingSet(String feature,String last3GameFeatureKey,String featureKey, List<Double> trainingRealValuesList) throws SQLException;
    
    double[][] initTrainingSet(String playerFeatures,String last3GameFeatureKeys, List<Double> trainingFPRealValuesList) throws SQLException;
    
}
