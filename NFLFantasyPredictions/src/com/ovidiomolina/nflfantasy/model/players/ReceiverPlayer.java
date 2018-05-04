/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players;

import com.ovidiomolina.db.Database;
import com.ovidiomolina.nflfantasy.Teams;
import com.ovidiomolina.util.ArrayUtils;
import com.ovidiomolina.util.NeuralNetworkHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author omolina
 */
public abstract class ReceiverPlayer extends Player {

    private double recYdsTrainingSet[][];
    private double recYdsReadTrainingValues[];
    
    private double recTDTrainingSet[][];
    private double recTDReadTrainingValues[];
    
    private double recYdsTestData[][];
    private double recTDTestData[][];
    
    private final List<Double> recYdsTestReadValues;
    private final List<Double> recTDTestReadValues;
    
    private static final String REC_YDS_NET = "REC_YDS_NET";
    private static final String REC_TD_NET = "REC_TD_NET";
    
    private static final int ESTIMATED_REC_YDS_COL = 5;
    private static final int ESTIMATED_REC_TDS_COL = 7;
    private static final int ESTIMATED_FANTASY_PTS_COL = 9;
    
    public ReceiverPlayer(Database db, String position, String playerId, String playerName, String playerTeam) {
        super(db, position, playerId, playerName, playerTeam);
        recYdsTestReadValues = new ArrayList<>();
        recTDTestReadValues = new ArrayList<>();
    }
    
    @Override
    protected void initializeFeatureStddevAndAvgValues(Database db) throws SQLException {
            String query = String.format(
                "select " +
                    "stddev(recy), " +
                    "stddev(last_3_games_avg_player_recy), " +
                    "stddev(defense_accepted_avg_pass_yds_to_wk), "+
                    "stddev(defense_accepted_rush_avg_yds_to_wk)," +
                    "stddev(defense_accepted_avg_pts_to_wk), " +
                    "stddev(defense_avg_pos_time_to_wk), " +
                    "stddev(total_sacks_by_defense_to_wk), " +
                    "stddev(total_fumbles_by_defense_to_wk), " +
                    "stddev(total_ints_by_defense_to_wk), " +
                    "stddev(tdrec), " +
                    "stddev(last_3_games_avg_player_tdrec), "+
                    "stddev(fp), " +
                    "stddev(team_avg_pass_yds_to_wk), " +
                    "stddev(team_avg_rush_yds_to_wk), " +
                    "stddev(team_avg_pts_to_wk), " +
                    "stddev(team_avg_pos_time_to_wk), " +
                    "avg(recy), " +
                    "avg(last_3_games_avg_player_recy), " +
                    "avg(defense_accepted_avg_pass_yds_to_wk), " +
                    "avg(defense_accepted_rush_avg_yds_to_wk), " +
                    "avg(defense_accepted_avg_pts_to_wk), " +
                    "avg(defense_avg_pos_time_to_wk), " +
                    "avg(total_sacks_by_defense_to_wk), " +
                    "avg(total_fumbles_by_defense_to_wk), " +
                    "avg(total_ints_by_defense_to_wk), " +
                    "avg(tdrec), " +
                    "avg(last_3_games_avg_player_tdrec), " +
                    "avg(fp), " +
                    "avg(team_avg_pass_yds_to_wk), " +
                    "avg(team_avg_rush_yds_to_wk), " +
                    "avg(team_avg_pts_to_wk), " +  
                    "avg(team_avg_pos_time_to_wk) " +
                    "from %s where player = '%s' ",getFromQueryView(), getPlayerId());
        System.out.println("features stddev and mean values\n" + query);
        ResultSet rs = db.executeQuery(query);
        while(rs.next()) {
            getFeatureStddevValues().put(PlayerConstants.RECEIVED_YDS,rs.getDouble(1));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,rs.getDouble(2));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(3));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(4));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(5));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(6));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(7));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(8));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(9));
            getFeatureStddevValues().put(PlayerConstants.TD_RECECPTION,rs.getDouble(10));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,rs.getDouble(11));
            getFeatureStddevValues().put(PlayerConstants.FANTASY_POINTS,rs.getDouble(12));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(13));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(14));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(15));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(16));
            getFeatureMeanValues().put(PlayerConstants.RECEIVED_YDS,rs.getDouble(17));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,rs.getDouble(18));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(19));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(20));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(21));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(22));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(23));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(24));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(25));
            getFeatureMeanValues().put(PlayerConstants.TD_RECECPTION,rs.getDouble(26));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,rs.getDouble(27));
            getFeatureMeanValues().put(PlayerConstants.FANTASY_POINTS,rs.getDouble(28));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(29));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(30));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(31));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(32));
        }
    }

    @Override
    public String[] getFeatureLabels() {
        if(!isCalculateFeaturesIndividually()) {
            return super.getFeatureLabels();
        }
        String titles[] = {"Team","Player","Week","Defense","Read Received Yds","Estimated Received Yds",
                    "Read Received Touchdown Passes","Estimated Received Touchdown Passes","Fantasy Pts", "Estimated Fantasy Pts"};
        return titles;
    }

    @Override
    public void initializeTrainingSet() throws SQLException {
        if(!isCalculateFeaturesIndividually()) {
            super.initializeTrainingSet();
        }
        else {
            Thread recYdsThread = new Thread() {
                public void run() {
                    try {
                        List<Double> readRecYdsTrainingValues = new ArrayList<>();
                        recYdsTrainingSet = initTrainingSet("recy",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,PlayerConstants.RECEIVED_YDS, readRecYdsTrainingValues, isGenerateTrainingData());
                        recYdsReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readRecYdsTrainingValues.toArray(readRecYdsTrainingValues.toArray(new Double[readRecYdsTrainingValues.size()])));
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread recTDThread =  new Thread() {
                public  void run() {
                    try {
                        List<Double> readRecTDTrainingValues = new ArrayList<>();
                        recTDTrainingSet = initTrainingSet("tdrec",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,PlayerConstants.TD_RECECPTION, readRecTDTrainingValues, isGenerateTrainingData());
                        recTDReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readRecTDTrainingValues.toArray(readRecTDTrainingValues.toArray(new Double[readRecTDTrainingValues.size()])));
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            recTDThread.start();
            recYdsThread.start();
            try {
                recTDThread.join();
                recYdsThread.join();
            }
            catch(Exception e) {

            } 
        }
    }

    @Override
    public void initializeTestData() throws SQLException {
        if(!isCalculateFeaturesIndividually()) {
            super.initializeTestData();
        }
        else {
            recYdsTestReadValues.clear();
            recTDTestReadValues.clear();
            Thread recYdsThread = new Thread() {
                public void run() {
                    try {
                        recYdsTestData = initFeatureTestData("recy",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,PlayerConstants.RECEIVED_YDS, recYdsTestReadValues, true);
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            Thread recTDThread = new Thread() {
                public void run() {
                    try {
                        recTDTestData = initFeatureTestData("tdrec",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,PlayerConstants.TD_RECECPTION, recTDTestReadValues);
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            recYdsThread.start();
            recTDThread.start();
            try {
                recYdsThread.join();
                recTDThread.join();
            }
            catch(Exception e) {}
        }
    }

    @Override
    public void train() {
        if(!isCalculateFeaturesIndividually()) {
            super.train();
        }
        else {
            Thread net1 = new Thread() {
                public void run() {
                    NeuralNetwork net = NeuralNetworkHelper.train(recYdsTrainingSet,
                    recYdsReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(REC_YDS_NET,net);
                }
            };
            net1.setName(REC_YDS_NET);
            Thread net2 = new Thread() {
                public void run() {
                    NeuralNetwork net = NeuralNetworkHelper.train(recTDTrainingSet,
                    recTDReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(REC_TD_NET,net);
                }
            };
            net2.setName(REC_TD_NET);
            net1.start(); net2.start();
            try {
                net1.join(); net2.join();
            }
            catch(Exception e) {} 
        } 
    }

    @Override
    public List<Object[]> evaluate() {
        if(!isCalculateFeaturesIndividually()) {
            return super.evaluate();
        }
        List<Object[]> evaluatedData = new ArrayList<>();
        for(int i =0; i<recYdsTestData.length; i++) {
            if(recYdsTestData[i] != null && recTDTestData[i] != null) {
                double team = softMaxDenormalizeValue(PlayerConstants.TEAMS,recYdsTestData[i][3]);
                String defense = Teams.getInstance().getTeamValueForId((int)team);
                NeuralNetwork eval = getNeuralNet(REC_YDS_NET);
                eval.setInput(recYdsTestData[i]);
                eval.calculate();
                double estimatedRecYds = softMaxDenormalizeValue(PlayerConstants.RECEIVED_YDS, eval.getOutput()[0]);
                eval = getNeuralNet(REC_TD_NET);
                eval.setInput(recTDTestData[i]);
                eval.calculate();
                double estimatedRecTD = Math.abs(softMaxDenormalizeValue(PlayerConstants.TD_RECECPTION, eval.getOutput()[0]));
                Object row[] = {getPlayerTeam(),getPlayerName(),
                    softMaxDenormalizeValue(PlayerConstants.WK,recYdsTestData[i][1]),
                    defense,
                    softMaxDenormalizeValue(PlayerConstants.RECEIVED_YDS,recYdsTestReadValues.get(i)),
                    estimatedRecYds,
                    softMaxDenormalizeValue(PlayerConstants.TD_RECECPTION,recTDTestReadValues.get(i)),
                    estimatedRecTD,
                    softMaxDenormalizeValue(PlayerConstants.FANTASY_POINTS,readFantasyPts.get(i))
                };
                evaluatedData.add(row);
            }
        } 
        return evaluatedData;
    }

    @Override
    public void calculateFantasyPoints(DefaultTableModel model) {
        for(int i = 0; i<model.getRowCount(); i++) {
            double recYds = (double)model.getValueAt(i, ESTIMATED_REC_YDS_COL);
            double recTds = (double)model.getValueAt(i, ESTIMATED_REC_TDS_COL);
            double fantasyPts = recYds/10 + 6*recTds;
            model.setValueAt(fantasyPts, i,ESTIMATED_FANTASY_PTS_COL);
        }
    }
    
    @Override
    public String getPlayerFeaturesForFP() {
        return "recy,tdrec";
    }

    @Override
    public String getPlayer3GameAvgFeatures() {
        return "last_3_games_avg_player_recy,last_3_games_avg_player_tdrec";
    }

    @Override
    public int addTrainingFeatureData(List<Double> entry, ResultSet rs) throws SQLException{
        //recy = 4, tdrec = 5, last_3_games_avg_player_recy = 6, last_3_games_avg_player_tdrec = 7
        entry.add(softMaxNormalizeValue(PlayerConstants.RECEIVED_YDS, rs.getDouble(4)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_RECECPTION, rs.getDouble(5)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS, rs.getDouble(6)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC, rs.getDouble(7)));
        return 8;
    }

    @Override
    protected int addTestFeatures(List<Double> entry, ResultSet rs) throws SQLException {
        //recy = 3, tdrec = 4, last_3_games_avg_player_recy = 5, last_3_games_avg_player_tdrec = 6
        entry.add(softMaxNormalizeValue(PlayerConstants.RECEIVED_YDS, rs.getDouble(3)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_RECECPTION, rs.getDouble(4)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS, rs.getDouble(5)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC, rs.getDouble(6)));
        return 7;
    }
}
