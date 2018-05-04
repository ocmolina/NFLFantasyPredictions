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
public class QuarterBack extends Player{
    
    private double passYdsTrainingSet[][];
    private double passYdsReadTrainingValues[];
    private double tdpTrainingSet[][];
    private double tdpReadTrainingValues[];
    private double passYdsTestData[][];
    private double tdpTestData[][];
    private final List<Double> passYdsTestReadValues;
    private final List<Double> tdpTestReadValues;
    private static final String PASS_YDS_NET = "PASS_YDS";
    private static final String TDP_NET = "TDP";
    
    
    private static final int READ_PASS_YDS_COL = 4;
    private static final int ESTIMATED_PASS_YDS_COL = 5;
    private static final int READ_TDP_COL = 6;
    private static final int ESTIMATED_TDP_COL = 7;
    private static final int READ_FANTASY_PTS_COL = 8;
    private static final int ESTIMATED_FANTASY_PTS_COL = 9;
    public QuarterBack(Database db,String position, String playerId, String playerName, String playerTeam) {
        super(db,position, playerId, playerName, playerTeam);
        passYdsTestReadValues = new ArrayList<>();
        tdpTestReadValues =  new ArrayList<>();
        readFantasyPts = new ArrayList<>();
    }

    @Override
    public String[] getFeatureLabels() {
        if(!isCalculateFeaturesIndividually()) {
            return super.getFeatureLabels();
        }
        String titles[] = {"Team","Player","Week","Defense","Read Pass Yds","Estimated Pass Yds",
                    "Read Touchdown Passes","Estimated Touchdown Passes","Fantasy Pts", "Estimated Fantasy Pts"};
        return titles;
    }

    @Override
    protected void initializeFeatureStddevAndAvgValues(Database db) throws SQLException{
        String query = String.format(
                "select " +
                    "stddev(py), " +
                    "stddev(last_3_games_avg_player_py), " +
                    "stddev(defense_accepted_avg_pass_yds_to_wk), "+
                    "stddev(defense_accepted_rush_avg_yds_to_wk)," +
                    "stddev(defense_accepted_avg_pts_to_wk), " +
                    "stddev(defense_avg_pos_time_to_wk), " +
                    "stddev(total_sacks_by_defense_to_wk), " +
                    "stddev(total_fumbles_by_defense_to_wk), " +
                    "stddev(total_ints_by_defense_to_wk), " +
                    "stddev(tdp), " +
                    "stddev(last_3_games_avg_player_tdp), "+
                    "stddev(fp), " +
                    "stddev(team_avg_pass_yds_to_wk), " +
                    "stddev(team_avg_rush_yds_to_wk), " +
                    "stddev(team_avg_pts_to_wk), " +
                    "stddev(team_avg_pos_time_to_wk), " +
                    "avg(py), " +
                    "avg(last_3_games_avg_player_py), " +
                    "avg(defense_accepted_avg_pass_yds_to_wk), " +
                    "avg(defense_accepted_rush_avg_yds_to_wk), " +
                    "avg(defense_accepted_avg_pts_to_wk), " +
                    "avg(defense_avg_pos_time_to_wk), " +
                    "avg(total_sacks_by_defense_to_wk), " +
                    "avg(total_fumbles_by_defense_to_wk), " +
                    "avg(total_ints_by_defense_to_wk), " +
                    "avg(tdp), " +
                    "avg(last_3_games_avg_player_tdp), " +
                    "avg(fp), " +
                    "avg(team_avg_pass_yds_to_wk), " +
                    "avg(team_avg_rush_yds_to_wk), " +
                    "avg(team_avg_pts_to_wk), " +  
                    "avg(team_avg_pos_time_to_wk) " +
                    "from qb_fantasy_features_table where %s ",getWhereClause());
        System.out.println("features stddev and mean values\n" + query);
        ResultSet rs = db.executeQuery(query);
        while(rs.next()) {
            getFeatureStddevValues().put(PlayerConstants.PASS_YDS,rs.getDouble(1));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_PY,rs.getDouble(2));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(3));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(4));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(5));
            getFeatureStddevValues().put(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(6));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(7));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(8));
            getFeatureStddevValues().put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(9));
            getFeatureStddevValues().put(PlayerConstants.TD_PASS,rs.getDouble(10));
            getFeatureStddevValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDP,rs.getDouble(11));
            getFeatureStddevValues().put(PlayerConstants.FANTASY_POINTS,rs.getDouble(12));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(13));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(14));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(15));
            getFeatureStddevValues().put(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(16));
            getFeatureMeanValues().put(PlayerConstants.PASS_YDS,rs.getDouble(17));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_PY,rs.getDouble(18));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PASS_YDS_TO_WK,rs.getDouble(19));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_RUSH_YDS_TO_WK,rs.getDouble(20));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_ACCEPTED_AVG_PTS_TO_WK,rs.getDouble(21));
            getFeatureMeanValues().put(PlayerConstants.DEFENSE_AVG_POS_TIME,rs.getDouble(22));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_SACKS_BY_DEFENSE_TO_WK,rs.getDouble(23));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_FUMBLES_BY_DEFENSE_TO_WK,rs.getDouble(24));
            getFeatureMeanValues().put(PlayerConstants.TOTAL_INTS_BY_DEFENSE_TO_WK,rs.getDouble(25));
            getFeatureMeanValues().put(PlayerConstants.TD_PASS,rs.getDouble(26));
            getFeatureMeanValues().put(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDP,rs.getDouble(27));
            getFeatureMeanValues().put(PlayerConstants.FANTASY_POINTS,rs.getDouble(28));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_PASS_YDS,rs.getDouble(29));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_RUSH_YDS,rs.getDouble(30));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_PTS,rs.getDouble(31));
            getFeatureMeanValues().put(PlayerConstants.TEAM_AVG_POS_TIME,rs.getDouble(32));
        }
    }
    protected String getWhereClause() {
        return String.format("player = '%s'",getPlayerId());
    }
    
    @Override
    public void initializeTrainingSet() throws SQLException {
        if(!isCalculateFeaturesIndividually()) {
            super.initializeTrainingSet();
        }
        else { 
            Thread initPassYdsThread = new Thread(){
                public void run() {
                    try {
                        List<Double> readTrainingValues = new ArrayList<>();
                        passYdsTrainingSet = initTrainingSet("py", PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_PY, PlayerConstants.PASS_YDS, readTrainingValues, isGenerateTrainingData());
                        passYdsReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readTrainingValues.toArray(readTrainingValues.toArray(new Double[readTrainingValues.size()])));
                    } catch (SQLException ex) {
                        Logger.getLogger(QuarterBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread initTdpThread = new Thread() {
                public void run() {
                    try {
                        List<Double> readTrainingValues = new ArrayList<>();
                        tdpTrainingSet = initTrainingSet("tdp", PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDP,PlayerConstants.TD_PASS,readTrainingValues,isGenerateTrainingData());
                        tdpReadTrainingValues = ArrayUtils.createPrimitiveDoubleArray(readTrainingValues.toArray(readTrainingValues.toArray(new Double[readTrainingValues.size()])));
                    } catch (SQLException ex) {
                        Logger.getLogger(QuarterBack.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            initPassYdsThread.start();
            initTdpThread.start();
            try {
                initPassYdsThread.join();
                initTdpThread.join();
            }
            catch(Exception e) {

            }
        }
    }
    
    @Override
    public  void initializeTestData() throws SQLException {
        if(!isCalculateFeaturesIndividually()) {
            super.initializeTestData();
        }
        else {
            passYdsTestReadValues.clear();
            tdpTestReadValues.clear();

            Thread passYdsThread = new Thread() {
              public void run() {
                  try {
                      passYdsTestData = initFeatureTestData("py",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_PY,PlayerConstants.PASS_YDS, passYdsTestReadValues,true);
                  } catch (SQLException ex) {
                      Logger.getLogger(QuarterBack.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }  
            };
            Thread tdpTestThread = new Thread() {
              public void run() {
                  try {
                      tdpTestData = initFeatureTestData("tdp",PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDP,PlayerConstants.TD_PASS, tdpTestReadValues);//initTdpTestData(getDatabase());
                  } catch (SQLException ex) {
                      Logger.getLogger(QuarterBack.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }  
            };
            passYdsThread.start();
            tdpTestThread.start();
            try {
                tdpTestThread.join();
                passYdsThread.join();
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
        for(int i =0; i<passYdsTestData.length; i++) {
            if(passYdsTestData[i] != null && tdpTestData[i] != null) {
                double team = softMaxDenormalizeValue(PlayerConstants.TEAMS,passYdsTestData[i][6]);
                String defense = Teams.getInstance().getTeamValueForId((int)team);
                NeuralNetwork net = getNeuralNet(PASS_YDS_NET);
                net.setInput(passYdsTestData[i]);
                net.calculate();
                double estimatedPassYds = softMaxDenormalizeValue(PlayerConstants.PASS_YDS, net.getOutput()[0]);
                net = getNeuralNet(TDP_NET);
                net.setInput(tdpTestData[i]);
                net.calculate();
                double estimatedTdp = Math.abs(softMaxDenormalizeValue(PlayerConstants.TD_PASS, net.getOutput()[0]));
                Object row[] = {getPlayerTeam(),getPlayerName(),
                    softMaxDenormalizeValue(PlayerConstants.WK,passYdsTestData[i][1]),
                    defense,
                    softMaxDenormalizeValue(PlayerConstants.PASS_YDS,passYdsTestReadValues.get(i)),
                    estimatedPassYds,
                    Math.floor(softMaxDenormalizeValue(PlayerConstants.TD_PASS,tdpTestReadValues.get(i))),
                    Math.floor(estimatedTdp),
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
            double fantasyPts = 
                (double)(model.getValueAt(i, ESTIMATED_PASS_YDS_COL))/25 + 4*(double)(model.getValueAt(i,ESTIMATED_TDP_COL));
            model.setValueAt(fantasyPts, i, ESTIMATED_FANTASY_PTS_COL);   
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
                System.out.println("TRAINING " + PASS_YDS_NET);
                NeuralNetwork net = NeuralNetworkHelper.train(passYdsTrainingSet,
                    passYdsReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(PASS_YDS_NET,net);
              }  
            };
            net1.setName("pass yds thread");
            Thread net2 = new Thread() {
                public void run() {
                    System.out.println("TRAINING " + TDP_NET);
                    NeuralNetwork net = NeuralNetworkHelper.train(tdpTrainingSet,
                    tdpReadTrainingValues, getEpochs(), getTargetError(),
                    getLearningRate(), getHiddenNodes(), getNeuralNetworkTrainingAlgorithm());
                    addNeuralNet(TDP_NET,net);
                }
            };
            net2.setName("tdp thread");
            net1.start();
            net2.start();
            try {
                net2.join();
                net1.join();
            }
            catch(Exception e) {} 
        }
    }

    @Override
    public String getFromQueryView() {
        return "qb_fantasy_features_table";
    }

    @Override
    public String getPlayerFeaturesForFP() {
        return "py,tdp";
    }

    @Override
    public String getPlayer3GameAvgFeatures() {
        return "last_3_games_avg_player_py,last_3_games_avg_player_tdp";
    }

    @Override
    public int addTrainingFeatureData(List<Double> entry, ResultSet rs) throws SQLException{
        //py = 4, tdp = 5, last_3_games_avg_player_py = 6, last_3_games_avg_player_tdp = 7
        entry.add(softMaxNormalizeValue(PlayerConstants.PASS_YDS, rs.getDouble(4)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_PASS, rs.getDouble(5)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_PY, rs.getDouble(6)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDP, rs.getDouble(7)));
        return 8;
    }

    @Override
    protected int addTestFeatures(List<Double> entry, ResultSet rs) throws SQLException {
        //py = 3, tdp = 4, last_3_games_avg_player_py = 5, last_3_games_avg_player_tdp = 6
        entry.add(softMaxNormalizeValue(PlayerConstants.PASS_YDS, rs.getDouble(3)));
        entry.add(softMaxNormalizeValue(PlayerConstants.TD_PASS, rs.getDouble(4)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_PY, rs.getDouble(5)));
        entry.add(softMaxNormalizeValue(PlayerConstants.LAST_THREE_GAMES_AVG_PLAYER_TDP, rs.getDouble(6)));
        return 7;
    }
}
