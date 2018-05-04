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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author omolina
 */
public class RunningBack extends Player {
    private double rushYdsTrainingSet[][];
    private double rushYdsReadTrainingValues[];
    
    private double recYdsTrainingSet[][];
    private double recYdsReadTrainingValues[];
    
    private double rushTDTrainingSet[][];
    private double rushTDReadTrainingValues[];
    
    private double recTDTrainingSet[][];
    private double recTDReadTrainingValues[];
    
    private double rushYdsTestData[][];
    private double recYdsTestData[][];
    private double rushTDTestData[][];
    private double recTDTestData[][];
    private final List<Double> rushYdsTestReadValues;
    private final List<Double> recYdsTestReadValues;
    private final List<Double> rushTDTestReadValues;
    private final List<Double> recTDTestReadValues;
    
    private static final String RUSH_YDS_NET = "RUSH_NET";
    private static final String REC_YDS_NET = "REC_YDS_NET";
    private static final String RUSH_TD_NET = "RUSH_TD_NET";
    private static final String REC_TD_NET = "REC_TD_NET";
    
    private static final int ESTIMATED_RUSH_YDS_COL = 5;
    private static final int ESTIMATED_REC_YDS_COL = 7;
    private static final int ESTIMATED_RUSH_TD_COL = 9;
    private static final int ESTIMATED_REC_TD_COL = 11;
    private static final int ESTIMATED_FANTASY_PTS_COL = 13;
    
    public RunningBack(Database db, String position, String playerId, String playerName, String playerTeam) {
        super(db, position, playerId, playerName, playerTeam);
        rushYdsTestReadValues = new ArrayList<>();
        recYdsTestReadValues = new ArrayList<>();
        rushTDTestReadValues = new ArrayList<>();
        recTDTestReadValues = new ArrayList<>();
    }

    @Override
    protected void initializeFeatureStddevAndAvgValues(Database db) throws SQLException {
        String query = String.format(
                "select " +
                    "stddev(ry), " +
                    "stddev(recy), " +
                    "stddev(tdr), " +
                    "stddev(tdrec), " +
                    "stddev(last_3_games_avg_player_ry), " +
                    "stddev(last_3_games_avg_player_recy),  " +
                    "stddev(last_3_games_avg_player_tdr), " +
                    "stddev(last_3_games_avg_player_tdrec),  " +
                    "stddev(team_avg_pass_yds_to_wk), " +
                    "stddev(team_avg_rush_yds_to_wk), " +
                    "stddev(team_avg_pts_to_wk), " +
                    "stddev(team_avg_pos_time_to_wk), " +
                    "stddev(defense_accepted_avg_pass_yds_to_wk), " +
                    "stddev(defense_accepted_rush_avg_yds_to_wk), " +
                    "stddev(defense_accepted_avg_pts_to_wk), " +
                    "stddev(defense_avg_pos_time_to_wk), " +
                    "stddev(total_sacks_by_defense_to_wk), " +
                    "stddev(total_fumbles_by_defense_to_wk), " +
                    "stddev(total_ints_by_defense_to_wk), " +
                    "stddev(fp), " +
                    "avg(ry), " +
                    "avg(recy), " +
                    "avg(tdr), " +
                    "avg(tdrec), " +
                    "avg(last_3_games_avg_player_ry), " +
                    "avg(last_3_games_avg_player_recy), " +
                    "avg(last_3_games_avg_player_tdr), " +
                    "avg(last_3_games_avg_player_tdrec), " +
                    "avg(team_avg_pass_yds_to_wk), " +
                    "avg(team_avg_rush_yds_to_wk), " +
                    "avg(team_avg_pts_to_wk), " +
                    "avg(team_avg_pos_time_to_wk), " +
                    "avg(defense_accepted_avg_pass_yds_to_wk), " +
                    "avg(defense_accepted_rush_avg_yds_to_wk), " +
                    "avg(defense_accepted_avg_pts_to_wk), " +
                    "avg(defense_avg_pos_time_to_wk), " +
                    "avg(total_sacks_by_defense_to_wk), " +
                    "avg(total_fumbles_by_defense_to_wk), " +
                    "avg(total_ints_by_defense_to_wk), " +
                    "avg(fp) " +
                "from rb_fantasy_features_table where player = '%s' ",getPlayerId());
        System.out.println("features stddev and mean values\n" + query);
        ResultSet rs = db.executeQuery(query);
        while(rs.next()) {
            getFeatureStddevValues().put(PlayerConstants.RUSH_YDS,rs.getDouble(1));
            getFeatureStddevValues().put(PlayerConstants.RECEIVED_YDS,rs.getDouble(2));
            getFeatureStddevValues().put(PlayerConstants.TD_RUSH,rs.getDouble(3));
            getFeatureStddevValues().put(PlayerConstants.TD_RECECPTION,rs.getDouble(4));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_RY,rs.getDouble(5));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,rs.getDouble(6));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR,rs.getDouble(7));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,rs.getDouble(8));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(9));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(10));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(11));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(12));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(13));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(14));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(15));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(16));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(17));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(18));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(19));
            getFeatureStddevValues().put(PlayerConstants.FANTASY_POINTS,rs.getDouble(20));
            getFeatureMeanValues().put(PlayerConstants.RUSH_YDS,rs.getDouble(21));
            getFeatureMeanValues().put(PlayerConstants.RECEIVED_YDS,rs.getDouble(22));
            getFeatureMeanValues().put(PlayerConstants.TD_RUSH,rs.getDouble(23));
            getFeatureMeanValues().put(PlayerConstants.TD_RECECPTION,rs.getDouble(24));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_RY,rs.getDouble(25));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,rs.getDouble(26));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR,rs.getDouble(27));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,rs.getDouble(28));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(29));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(30));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(31));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(32));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(33));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(34));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(35));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(36));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(37));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(38));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(39));
            getFeatureMeanValues().put(PlayerConstants.FANTASY_POINTS,rs.getDouble(40));
        }
    }

    @Override
    public String[] getFeatureLabels() {
        if(!isCalculateFeaturesIndividually()) {
            return super.getFeatureLabels();
        }
        String titles[] = {"Team","Player","Week","Defense","Read Rush Yds","Estimated Rush Yds",
                    "Read Received Yds","Estimated Received Yds","Read TD Rush","Estimated TD Rush",
                    "Read TD Recepction","Estimated TD Reception","Fantasy Pts", "Estimated Fantasy Pts"};
        return titles;
    }

    @Override
    public void initializeTrainingSet() throws SQLException {
        if(!isCalculateFeaturesIndividually()) {
            super.initializeTrainingSet();
        }
        else {
            Thread rushYdsThread = new Thread() {
                public void run() {
                    try {
                        List<Double> readRushYdsTrainingValues = new ArrayList<>();
                        rushYdsTrainingSet = initTrainingSet("ry",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_RY,PlayerConstants.RUSH_YDS, readRushYdsTrainingValues, isGenerateTrainingData());
                        rushYdsReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readRushYdsTrainingValues.toArray(readRushYdsTrainingValues.toArray(new Double[readRushYdsTrainingValues.size()])));
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread rushTDThread = new Thread() {
                public void run() {
                    try {
                        List<Double> readRushTDTrainingValues = new ArrayList<>();
                        rushTDTrainingSet = initTrainingSet("tdr",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR,PlayerConstants.TD_RUSH, readRushTDTrainingValues, isGenerateTrainingData());
                        rushTDReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readRushTDTrainingValues.toArray(readRushTDTrainingValues.toArray(new Double[readRushTDTrainingValues.size()])));
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
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
            rushYdsThread.start(); recTDThread.start();
            rushTDThread.start(); recYdsThread.start();
            try {
                rushYdsThread.join(); recTDThread.join();
                rushTDThread.join(); recYdsThread.join();
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
            rushYdsTestReadValues.clear();
            recYdsTestReadValues.clear();
            rushTDTestReadValues.clear();
            recTDTestReadValues.clear();
            Thread rushYdsThread = new Thread() {
                public void run() {
                    try {
                        rushYdsTestData = initFeatureTestData("ry",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_RY,PlayerConstants.RUSH_YDS, rushYdsTestReadValues, true);
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread recYdsThread = new Thread() {
                public void run() {
                    try {
                        recYdsTestData = initFeatureTestData("recy",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS,PlayerConstants.RECEIVED_YDS, recYdsTestReadValues);
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread rushTDThread = new Thread() {
                public void run() {
                    try {
                        rushTDTestData = initFeatureTestData("tdr",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR,PlayerConstants.TD_RUSH, rushTDTestReadValues);
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread recTDThread = new Thread() {
                public void run() {
                    try {
                        recTDTestData = initFeatureTestData("tdrec",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDREC,PlayerConstants.TD_RECECPTION, recTDTestReadValues);//initRecTDTestData(getDatabase());
                    } catch (SQLException ex) {
                        Logger.getLogger(RunningBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            rushYdsThread.start(); recYdsThread.start();
            rushTDThread.start(); recTDThread.start();
            try {
                rushYdsThread.join(); recYdsThread.join();
                rushTDThread.join(); recTDThread.join();
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
              @Override
              public void run() {
                NeuralNetwork net = NeuralNetworkHelper.train(rushYdsTrainingSet,
                    rushYdsReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(RUSH_YDS_NET,net);
              }  
            };
            net1.setName(RUSH_YDS_NET);
            Thread net2 = new Thread() {
                public void run() {
                    NeuralNetwork net = NeuralNetworkHelper.train(recYdsTrainingSet,
                    recYdsReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(REC_YDS_NET,net);
                }
            };
            net2.setName(REC_YDS_NET);
            Thread net3 = new Thread() {
              @Override
              public void run() {
                NeuralNetwork net = NeuralNetworkHelper.train(rushTDTrainingSet,
                    rushTDReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(RUSH_TD_NET,net);
              }  
            };
            net3.setName(RUSH_TD_NET);
            Thread net4 = new Thread() {
                public void run() {
                    NeuralNetwork net = NeuralNetworkHelper.train(recTDTrainingSet,
                    recTDReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(REC_TD_NET,net);
                }
            };
            net4.setName(REC_TD_NET);
            net1.start(); net2.start();
            net3.start(); net4.start();
            try {
                net2.join(); net3.join();
                net1.join(); net4.join();
            }
            catch(Exception e) {} 
        } 
    }

    @Override
    public List<Object[]> evaluate() {
        List<Object[]> evaluatedData = Collections.EMPTY_LIST;
        if(!isCalculateFeaturesIndividually()) {
            evaluatedData = super.evaluate();
            return evaluatedData;
        }
        evaluatedData = new ArrayList<>();
        for(int i =0; i<rushYdsTestData.length; i++) {
            if(rushYdsTestData[i] != null && recYdsTestData[i] != null && 
                    rushTDTestData[i] != null && recTDTestData[i] != null) {
                double team = softMaxDenormalizeValue(PlayerConstants.TEAMS,rushYdsTestData[i][6]);
                String defense = Teams.getInstance().getTeamValueForId((int)team);
                NeuralNetwork net = getNeuralNet(RUSH_YDS_NET);
                net.setInput(rushYdsTestData[i]);
                net.calculate();
                double estimatedRushYds = softMaxDenormalizeValue(PlayerConstants.RUSH_YDS, net.getOutput()[0]);
                net = getNeuralNet(REC_YDS_NET);
                net.setInput(recYdsTestData[i]);
                net.calculate();
                double estimatedRecYds = softMaxDenormalizeValue(PlayerConstants.RECEIVED_YDS, net.getOutput()[0]);
                net = getNeuralNet(RUSH_TD_NET);
                net.setInput(rushTDTestData[i]);
                net.calculate();
                double estimatedRushTD = Math.abs(softMaxDenormalizeValue(PlayerConstants.TD_RUSH, net.getOutput()[0]));
                net = getNeuralNet(REC_TD_NET);
                net.setInput(recTDTestData[i]);
                net.calculate();
                double estimatedRecTD =Math.abs(softMaxDenormalizeValue(PlayerConstants.TD_RECECPTION, net.getOutput()[0]));
                Object row[] = {getPlayerTeam(),getPlayerName(),
                    softMaxDenormalizeValue(PlayerConstants.WK,rushYdsTestData[i][1]),
                    defense,
                    softMaxDenormalizeValue(PlayerConstants.RUSH_YDS,rushYdsTestReadValues.get(i)),
                    estimatedRushYds,
                    softMaxDenormalizeValue(PlayerConstants.RECEIVED_YDS,recYdsTestReadValues.get(i)),
                    estimatedRecYds,
                    Math.floor(Math.abs(softMaxDenormalizeValue(PlayerConstants.TD_RUSH,rushTDTestReadValues.get(i)))),
                    Math.floor(estimatedRushTD),
                    Math.floor(Math.abs(softMaxDenormalizeValue(PlayerConstants.TD_RECECPTION,recTDTestReadValues.get(i)))),
                    Math.floor(estimatedRecTD),
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
            double rushYds = (double) model.getValueAt(i,ESTIMATED_RUSH_YDS_COL);
            double recYds = (double) model.getValueAt(i,ESTIMATED_REC_YDS_COL);
            double rushTD = (double) model.getValueAt(i,ESTIMATED_RUSH_TD_COL);
            double recTD = (double) model.getValueAt(i,ESTIMATED_REC_TD_COL);
            double fantasyPts = (rushYds + recYds)/10 + 6*(rushTD + recTD);
            model.setValueAt(fantasyPts,i,ESTIMATED_FANTASY_PTS_COL);
        }
    }

    @Override
    public String getFromQueryView() {
        return "rb_fantasy_features_table";
    }

    @Override
    public String getPlayerFeaturesForFP() {
        return "ry,tdr,recy,tdrec";
    }

    @Override
    public String getPlayer3GameAvgFeatures() {
       return "last_3_games_avg_player_ry,last_3_games_avg_player_tdr,last_3_games_avg_player_recy,last_3_games_avg_player_tdrec";
    }

    @Override
    public int addTrainingFeatureData(List<Double> entry, ResultSet rs) throws SQLException {
        //ry = 4, tdr = 5, recy = 6, tdrec = 7,last_3_games_avg_player_ry = 8,last_3_games_avg_player_tdr = 9,
        //last_3_games_avg_player_recy = 10, last_3_games_avg_player_tdrec = 11
        entry.add(softMaxNormalizeValue(PlayerConstants.RUSH_YDS, rs.getDouble(4)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_RUSH, rs.getDouble(5)));
        entry.add(softMaxNormalizeValue(PlayerConstants.RECEIVED_YDS, rs.getDouble(6)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_RECECPTION, rs.getDouble(7)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_RY, rs.getDouble(8)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR, rs.getDouble(9)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS, rs.getDouble(10)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR, rs.getDouble(11)));
        return 12;
    }

    @Override
    protected int addTestFeatures(List<Double> entry, ResultSet rs) throws SQLException {
        //ry = 3, tdr = 4, recy = 5, tdrec = 6,
        //last_3_games_avg_player_ry = 7,
        //last_3_games_avg_player_tdr = 8,
        //last_3_games_avg_player_recy = 9,
        //last_3_games_avg_player_tdrec = 10
        entry.add(softMaxNormalizeValue(PlayerConstants.RUSH_YDS, rs.getDouble(3)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_RUSH, rs.getDouble(4)));
        entry.add(softMaxNormalizeValue(PlayerConstants.RECEIVED_YDS, rs.getDouble(5)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_RECECPTION, rs.getDouble(6)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_RY, rs.getDouble(7)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR, rs.getDouble(8)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_REC_YDS, rs.getDouble(9)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDR, rs.getDouble(10)));
        return 11;
    }
    
    @Override
    protected int defenseIndex() {
        return 13;
    }
}
