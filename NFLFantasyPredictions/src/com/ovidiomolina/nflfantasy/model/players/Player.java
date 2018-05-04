/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players;

import com.ovidiomolina.db.Database;
import com.ovidiomolina.nflfantasy.Teams;
import com.ovidiomolina.nflfantasy.model.players.util.TrainingFeaturesSetGenerator;
import com.ovidiomolina.nflfantasy.ui.ChartItem;
import com.ovidiomolina.util.ArrayUtils;
import com.ovidiomolina.util.NeuralNetworkHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author omolina
 */
public abstract class Player {
    
    private String position;
    private String playerId;
    private String playerName;
    private String playerTeam;
    private final Database db;
    private String initialTrainingSeason;
    private String finalTrainingSeason;
    private String testingSeason;
    private String testingWk;
    private HashMap<String,Double> featureStddevValues;
    private HashMap<String,Double> featureMeanValues;
    protected List<Double> readFantasyPts;
    protected double[][] fantasyPtsTestData;
    private HashMap<String,NeuralNetwork> neuralNets;
    private int epochs;
    private double targetError;
    private double learningRate;
    private int hiddenNodes;
    private String neuralNetworkTrainingAlgorithm;
    private boolean generateTrainingData;
    private int simulatedSeasons;
    private TrainingFeaturesSetGenerator trainingSetGenerator;
    private boolean hasTestData; //player contains test data
    private boolean hasTrainData; //player contains training data
    private boolean calculateFeaturesIndividually;
    protected double fantasyPtsTrainingSet[][];
    protected double fantasyPtsReadTrainingValues[];
    
    private static final String FANTASY_PTS_NET = "FANTASY_PTS";
    
    protected Player(Database db,String position, String playerId, 
            String playerName, String playerTeam) {
        this.db = db;
        this.position = position;
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerTeam = playerTeam;
        featureStddevValues = new HashMap<>();
        featureStddevValues.put(PlayerConstants.TEAMS,
                ArrayUtils.calculateArrayStddevValue(Teams.TEAM_ID_ARRAY));
        featureStddevValues.put(PlayerConstants.WK,
                ArrayUtils.calculateArrayStddevValue(PlayerConstants.WEEKS));
        featureMeanValues = new HashMap<>();
        featureMeanValues.put(PlayerConstants.TEAMS,
                ArrayUtils.calculateArrayAvgValue(Teams.TEAM_ID_ARRAY));
        featureMeanValues.put(PlayerConstants.WK,
                ArrayUtils.calculateArrayAvgValue(PlayerConstants.WEEKS));
        neuralNets = new HashMap<>();
        readFantasyPts = new ArrayList<>();
        try {
            initializeFeatureStddevAndAvgValues(db);
        } catch (SQLException ex) {
           ex.printStackTrace();
        }
    }
    
    protected void addNeuralNet(String key, NeuralNetwork n) {
        neuralNets.put(key, n);
    }
    
    protected NeuralNetwork getNeuralNet(String key) {
        return neuralNets.get(key);
    }

    public String getNeuralNetworkTrainingAlgorithm() {
        return neuralNetworkTrainingAlgorithm;
    }

    public void setNeuralNetworkTrainingAlgorithm(String neuralNetworkTrainingAlgorithm) {
        this.neuralNetworkTrainingAlgorithm = neuralNetworkTrainingAlgorithm;
    }
    
    
    
    public static Player createPlayer(Database db, String position, 
            String playerId, String playerName, String playerTeam) {
        if(position.equalsIgnoreCase(PlayerConstants.QB)) {
            return new QuarterBack(db, position, playerId, playerName, playerTeam);
        }
        
        if(position.equalsIgnoreCase(PlayerConstants.RB)) {
            return new RunningBack(db, position, playerId, playerName, playerTeam);
        }
        
        if(position.equalsIgnoreCase(PlayerConstants.WR)) {
            return new WideReceiver(db, position, playerId, playerName, playerTeam);
        }
        
        if(position.equalsIgnoreCase(PlayerConstants.TE)) {
            return new TightEnd(db, position, playerId, playerName, playerTeam);
        }
        return null;
    }

    public void setTestingWk(String testingWk) {
        this.testingWk = testingWk;
    }

    public void setTrainingSetGenerator(TrainingFeaturesSetGenerator trainingSetGenerator) {
        this.trainingSetGenerator = trainingSetGenerator;
    }
    
    protected abstract void initializeFeatureStddevAndAvgValues(Database db) throws SQLException;

    protected HashMap<String, Double> getFeatureStddevValues() {
        return featureStddevValues;
    }
    
    protected HashMap<String, Double> getFeatureMeanValues() {
        return featureMeanValues;
    }
    
    public String getPosition() {
        return position;
    }

    public String getInitialTrainingSeason() {
        return initialTrainingSeason;
    }

    public void setInitialTrainingSeason(String initialTrainingSeason) {
        this.initialTrainingSeason = initialTrainingSeason;
    }

    public String getFinalTrainingSeason() {
        return finalTrainingSeason;
    }

    public void setFinalTrainingSeason(String finalTrainingSeason) {
        this.finalTrainingSeason = finalTrainingSeason;
    }

    public String getTestingSeason() {
        return testingSeason;
    }

    public void setTestingSeason(String testingSeason) {
        this.testingSeason = testingSeason;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerTeam() {
        return playerTeam;
    }

    public void setPlayerTeam(String playerTeam) {
        this.playerTeam = playerTeam;
    }

    public void setGenerateTrainingData(boolean generateTrainingData) {
        this.generateTrainingData = generateTrainingData;
    }
    
    protected boolean isGenerateTrainingData() {
        return generateTrainingData;
    }
    
    public Database getDatabase() {
        return db;
    }
    
    public String[] getFeatureLabels() {
        String titles[] = {"Team","Player","Week","Defense","Fantasy Pts", "Estimated Fantasy Pts"};
        return titles;
    }
    
    //called by child implementations if !isCalculateFeaturesIndividually()
    public void initializeTrainingSet() throws SQLException {
        Thread fpThread = new Thread() {
            public void run() {
                try {
                    List<Double> readTrainingValues = new ArrayList<>();
                    fantasyPtsTrainingSet = initTrainingSet(getPlayerFeaturesForFP(),getPlayer3GameAvgFeatures(), readTrainingValues);
                    fantasyPtsReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readTrainingValues.toArray(readTrainingValues.toArray(new Double[readTrainingValues.size()])));
                }
                catch(SQLException ex) {}
            }
        };
        fpThread.start();
        try {
            fpThread.join();
        }
        catch(Exception e) {}
        
    }
    //called by child implementations if !isCalculateFeaturesIndividually()
    public void initializeTestData() throws SQLException {
        Thread fantasyPtsThread = new Thread() {
                public void run() {
                    try {
                        fantasyPtsTestData = initFantasyPtsTestData(getPlayerFeaturesForFP(), getPlayer3GameAvgFeatures());
                    }
                    catch(Exception e) {
                        System.out.println("something failed awfully");
                        e.printStackTrace();
                    }
                }  
            };
        fantasyPtsThread.start();
        try {
            fantasyPtsThread.join();
        }
        catch(Exception e){}
    }
    
    public void train() {
        Thread fantasyPtsTrainThread = new Thread() {
                public void run() {
                    System.out.println("TRAINING FANTASY PTS");
                    NeuralNetwork net = NeuralNetworkHelper.train(fantasyPtsTrainingSet,
                    fantasyPtsReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(FANTASY_PTS_NET,net);
                }
            };
            fantasyPtsTrainThread.start();
            try {
                fantasyPtsTrainThread.join();
            }
            catch(Exception e) { 
                e.printStackTrace();
            }
    }
    
    public List<Object[]> evaluate() {
        List<Object[]> evaluatedData = new ArrayList<>();
        NeuralNetwork net = getNeuralNet(FANTASY_PTS_NET);
        for(int i = 0; i<fantasyPtsTestData.length; i++) {
            if(fantasyPtsTestData[i] != null) {
                net.setInput(fantasyPtsTestData[i]);
                net.calculate();
                double calculatedFpts = softMaxDenormalizeValue(PlayerConstants.FANTASY_POINTS, net.getOutput()[0]);
                double team = softMaxDenormalizeValue(PlayerConstants.TEAMS,fantasyPtsTestData[i][defenseIndex()]);
                    String defense = Teams.getInstance().getTeamValueForId((int)team);
                Object row[] ={getPlayerTeam(),getPlayerName(),
                    softMaxDenormalizeValue(PlayerConstants.WK,fantasyPtsTestData[i][1]),
                     defense,
                     softMaxDenormalizeValue(PlayerConstants.FANTASY_POINTS,readFantasyPts.get(i)),
                     calculatedFpts};
                evaluatedData.add(row);
            }
        }
        return evaluatedData;
    }
    
    protected int defenseIndex() {
        return 9;
    }
    
    public abstract void calculateFantasyPoints(DefaultTableModel model);
    
    public double softMaxDenormalizeValue(String feature, double value) {
        double stddev = getFeatureStddevValues().get(feature);
        double mean = getFeatureMeanValues().get(feature);
        double x = -1*stddev*Math.log(1/value - 1) + mean;
        if(feature.equals(PlayerConstants.WK) || feature.equals(PlayerConstants.TEAMS)) {
            return Math.round(x);
        }
        return x;
    }
    
    public double softMaxNormalizeValue(String feature, double value) {
        double stddev = getFeatureStddevValues().get(feature);
        double mean = getFeatureMeanValues().get(feature);
        double z = (value - mean)/stddev;
        double softMaxNorm = 1/(1+Math.exp(-1*z));
        return softMaxNorm;
        
    }

    protected int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    protected double getTargetError() {
        return targetError;
    }

    public void setTargetError(double targetError) {
        this.targetError = targetError;
    }

    protected double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    protected int getHiddenNodes() {
        return hiddenNodes;
    }

    public void setHiddenNodes(int hiddenNodes) {
        this.hiddenNodes = hiddenNodes;
    }
    
    public List<Double> getXLabelValues(DefaultTableModel model) {
        List<Double> xLabelValues = new ArrayList<>();
        for(int i = 0; i<model.getRowCount(); i++) {
            xLabelValues.add(Double.parseDouble(
                    String.valueOf(
                            model.getValueAt(i, PlayerConstants.WEEK_COL))
            )
            );
        }
        return xLabelValues;
    }
    
    public List<ChartItem> getChartItems(DefaultTableModel model, int[] columns) {
        List<ChartItem> chartItems = new ArrayList<>();
        List<List<Double>> plots = new ArrayList<>();
        for(int i = 0; i<columns.length; i++) {
            plots.add(new ArrayList<Double>());
        }
        for(int row = 0; row<model.getRowCount(); row++) {
            for(int j = 0; j<columns.length; j++) {
                plots.get(j).add(Double.parseDouble(String.valueOf(model.getValueAt(row, columns[j]))));
            }
        }
        for(int i = 0; i<columns.length; i++) {
            chartItems.add(new ChartItem(
                    model.getColumnName(columns[i]),plots.get(i))
            );
        }
        return chartItems;
    }
    
    public abstract String getFromQueryView();
    
    public abstract int addTrainingFeatureData(List<Double> entry, ResultSet rs) throws SQLException;
        
    protected double[][] initTrainingSet(String feature,String last3GameFeatureKey,String featureKey, List<Double> trainingRealValuesList, boolean generateRandomTrainingVectors) throws SQLException {
        return trainingSetGenerator.initTrainingSet(feature, last3GameFeatureKey, featureKey, trainingRealValuesList);
    }
    
    protected double[][] initTrainingSet(String playerFeatures,String last3GameFeatureKeys,List<Double> trainingRealValuesList) throws SQLException {
        return trainingSetGenerator.initTrainingSet(playerFeatures, last3GameFeatureKeys, trainingRealValuesList);
    }
    
    /**
     * initialize test data for fantasy points
     * @param playerFeatures
     * @param last3GameFeatureKeys
     * @return 
     */
    protected double[][] initFantasyPtsTestData(String playerFeatures, String last3GameFeatureKeys) throws SQLException {
        readFantasyPts.clear();
        String weekRange = " wk >= 1 and wk < 18 ";
        if(testingWk != null)
        {
            weekRange = String.format(" wk = %s ", testingWk);
        }
        Teams teams = Teams.getInstance();
        String query = String.format("select fp,wk,%s,%s, team_avg_pass_yds_to_wk,team_avg_rush_yds_to_wk,team_avg_pts_to_wk, " +
                        "defense,defense_accepted_avg_pass_yds_to_wk, " +
                        "defense_accepted_rush_avg_yds_to_wk,total_sacks_by_defense_to_wk,total_fumbles_by_defense_to_wk,total_ints_by_defense_to_wk, defense_accepted_avg_pts_to_wk, " +
                        "team_avg_pos_time_to_wk, defense_avg_pos_time_to_wk " +
                        " from %s where player = '%s' and season = %s and %s ",playerFeatures,last3GameFeatureKeys,getFromQueryView(),getPlayerId(),getTestingSeason(),weekRange);
        System.out.println(query);
        ResultSet rs = db.executeQuery(query);
        double testData[][] = new double[16][];
        int i = 0;
        while(rs.next()) {
            hasTestData = true;
            List<Double> currentEntry = new ArrayList<>();
            readFantasyPts.add(softMaxNormalizeValue(PlayerConstants.FANTASY_POINTS,rs.getDouble(1)));
            currentEntry.add(1.0); //bias
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.WK,rs.getDouble(2)));//wk
            int nextFeature = addTestFeatures(currentEntry,rs);
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(nextFeature++)));
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(nextFeature++)));
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(nextFeature++)));
            int defenseId = teams.getTeamId(rs.getString(nextFeature++));//defense
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAMS,(double)defenseId));
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(nextFeature++)));//defense_accepted_avg_pass_yds_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(nextFeature++)));//defense_accepted_rush_avg_yds_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(nextFeature++)));//total_sacks_by_defense_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(nextFeature++)));//total_fumbles_by_defense_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(nextFeature++)));//total_ints_by_defense_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(nextFeature++)));;//defense_accepted_avg_pts_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(nextFeature++)));//team_avg_pos_time_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(nextFeature++)));//defense_avg_pos_time_to_wk
            testData[i++] = ArrayUtils.createPrimitiveDoubleArray(currentEntry.toArray(new Double[currentEntry.size()]));
        }
       return testData;
    }
    
    protected abstract int addTestFeatures(List<Double> currentEntry, ResultSet rs) throws SQLException;
    
    /**
     * initialize test data for each given feature
     * @param feature
     * @param last3GameFeatreKey
     * @param featureKey
     * @param featureTestReadValues
     * @return
     * @throws SQLException 
     */
    protected double[][] initFeatureTestData(String feature, String last3GameFeatreKey,String featureKey, List<Double> featureTestReadValues) throws SQLException {
        return initFeatureTestData(feature, last3GameFeatreKey, featureKey, featureTestReadValues, false);
    }
    
    /**
     * initialize test data for each feature and include fantasy points if param fantasyPts is true
     * @param feature
     * @param last3GameFeatureKey
     * @param featureKey
     * @param featureTestReadValues
     * @param fantasyPts
     * @return
     * @throws SQLException 
     */
    protected double[][] initFeatureTestData(String feature, String last3GameFeatureKey,String featureKey, List<Double> featureTestReadValues, boolean fantasyPts) throws SQLException {
        readFantasyPts.clear();
        String fPts = "";
        if(fantasyPts) {
            fPts = ",fp ";
        }
        String weekRange = " wk >= 1 and wk < 18 ";
        if(testingWk != null)
        {
            weekRange = String.format(" wk = %s ", testingWk);
        }
        Teams teams = Teams.getInstance();
        String query = String.format("select %s,wk,%s, team_avg_pass_yds_to_wk,team_avg_rush_yds_to_wk,team_avg_pts_to_wk, " +
                        "defense,defense_accepted_avg_pass_yds_to_wk, " +
                        "defense_accepted_rush_avg_yds_to_wk,total_sacks_by_defense_to_wk,total_fumbles_by_defense_to_wk,total_ints_by_defense_to_wk, defense_accepted_avg_pts_to_wk, " +
                        "team_avg_pos_time_to_wk, defense_avg_pos_time_to_wk " +
                        "%s from %s where player = '%s' and season = %s and %s ",feature,last3GameFeatureKey,fPts,getFromQueryView(),getPlayerId(),getTestingSeason(),weekRange);
        System.out.println(query);
        ResultSet rs = db.executeQuery(query);
        double testData[][] = new double[16][];
        int i = 0;
        while(rs.next()) {
            hasTestData = true;
            List<Double> currentEntry = new ArrayList<>();
            featureTestReadValues.add(softMaxNormalizeValue(featureKey,rs.getDouble(1)));
            if(fantasyPts) {
                readFantasyPts.add(softMaxNormalizeValue(PlayerConstants.FANTASY_POINTS, rs.getDouble(16)));//fp
            }
            currentEntry.add(1.0); //bias
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.WK,rs.getDouble(2)));//wk
            currentEntry.add(softMaxNormalizeValue(last3GameFeatureKey,rs.getDouble(3)));//last_3_games_avg_player_py
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(4)));
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(5)));
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(6)));
            int defenseId = teams.getTeamId(rs.getString(7));//defense
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAMS,(double)defenseId));
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(8)));//defense_accepted_avg_pass_yds_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(9)));//defense_accepted_rush_avg_yds_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(10)));//total_sacks_by_defense_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(11)));//total_fumbles_by_defense_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(12)));//total_ints_by_defense_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(13)));;//defense_accepted_avg_pts_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(14)));//team_avg_pos_time_to_wk
            currentEntry.add(softMaxNormalizeValue(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(15)));//defense_avg_pos_time_to_wk
            testData[i++] = ArrayUtils.createPrimitiveDoubleArray(currentEntry.toArray(new Double[currentEntry.size()]));
        }
       return testData; 
    }
    
    public double generateRandomValueFromGaussianDistribution(
            String featureKey, double probability)
    {
        double mean = getFeatureMeanValues().get(featureKey);
        double stddev = getFeatureStddevValues().get(featureKey);
        NormalDistribution nd = new NormalDistribution(mean, stddev);
        double value = nd.inverseCumulativeProbability(1-probability);
        return value;
    }

    public int getSimulatedSeasons() {
        return simulatedSeasons;
    }

    public void setSimulatedSeasons(int simulatedSeasons) {
        this.simulatedSeasons = simulatedSeasons;
    }

    public boolean hasTestData() {
        return hasTestData;
    }
    
    public boolean hasTrainData() {
        return hasTrainData;
    }
    
    public void setHasTrainData(boolean hasData) {
        hasTrainData = hasData;
    }
    
    /**
     * Get a the features required to calculate fantasy points for each player
     * depending on their position.
     * @return 
     */
    public abstract String getPlayerFeaturesForFP();
    
    public abstract String getPlayer3GameAvgFeatures();
    
    /**
     * 
     * @return true if player has training and testing data
     */
    public boolean isValid() {
        return hasTrainData && hasTestData;
    }

    public boolean isCalculateFeaturesIndividually() {
        return calculateFeaturesIndividually;
    }

    public void setCalculateFeaturesIndividually(boolean calculateFeaturesIndividually) {
        this.calculateFeaturesIndividually = calculateFeaturesIndividually;
    }
    
    
}
