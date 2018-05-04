/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.ui.panels;

import com.ovidiomolina.db.Database;
import com.ovidiomolina.nflfantasy.PlayerSelection;
import com.ovidiomolina.nflfantasy.model.players.Player;
import com.ovidiomolina.nflfantasy.model.players.util.DBReadTrainingFeaturesSet;
import com.ovidiomolina.util.NeuralNetworkFeatureCalculator;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author omolina
 */
public final class EstimateWeekTopPerformers extends JPanel implements NeuralNetworkFeatureCalculator{

    private NeuralNetworkConfigPanel networkConfig;
    private JComboBox positionsCombo;
    private JComboBox weeksCombo;
    private JTable resultsTable;
    private DefaultTableModel topTenTableModel;
    private JProgressBar progressBar;
    private final JFrame parent;
    private final PlayerSelection allPlayers;
    private final Database db;

    public EstimateWeekTopPerformers(JFrame parent, PlayerSelection players, Database db) {
        this.parent = parent;
        allPlayers = players;
        this.db = db;
        initComponents();
        layoutComponents();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }
    
    public void initComponents() {
        positionsCombo = new JComboBox(PlayerSelection.POSITIONS);
        weeksCombo = new JComboBox(PlayerSelection.WEEK_SELECTION);
        networkConfig = new NeuralNetworkConfigPanel(this);
        resultsTable = new JTable();
        progressBar = new JProgressBar();
        progressBar.setVisible(true);
    }
    
    public void layoutComponents() {
        setLayout(new GridBagLayout());
        int y = 0 ;
        add(new JLabel("SELECT A PLAYER'S POSITION:"),
                new GridBagConstraints(0, y, 1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(5,5,5,5),0,0));
        add(positionsCombo,
                new GridBagConstraints(1, y, 1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(5,5,5,5),0,0));
        y++;
        add(new JLabel("SELECT A WEEK TO PREDICT TOP PERFORMERS:"),
                new GridBagConstraints(0, y, 1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(5,5,5,5),0,0));
        add(weeksCombo,
                new GridBagConstraints(1, y, 1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(5,5,5,5),0,0));
        y++;
        add(networkConfig,
                new GridBagConstraints(0, y, 2,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(5,5,5,5),0,0));
        y++;
        add(new JScrollPane(resultsTable),
                new GridBagConstraints(0, y, 2,1,0.5,0.5,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,0));
        y++;
        add(new JScrollPane(progressBar),
                new GridBagConstraints(0, y, 2,1,0.5,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
    }
    
    @Override
    public void estimateFeatures() {
        String position = (String) positionsCombo.getSelectedItem();
        final List<PlayerSelection.Player> positionPlayers = allPlayers.getPlayersByPosition(position);
        final String week = (String)weeksCombo.getSelectedItem();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    System.out.println(positionPlayers.size() + " " + position);
                    String initTraining = networkConfig.getTrainingBeginSeason();
                    String endTraining = networkConfig.getTrainingEndSeason();
                    String testingSeason = networkConfig.getTestSeason();
                    List<Thread> threads = new ArrayList<>();
                    int threadCount = 1;
                    for(final PlayerSelection.Player currentPlayer : positionPlayers) {
                        if(currentPlayer.getGames() < 17) {
                            continue;
                        }
                        Thread trainingThread = new Thread() {
                            public void run() {
                                final Player player = Player.createPlayer(db, currentPlayer.getPosition(), currentPlayer.getId(),currentPlayer.getName(),currentPlayer.getTeam());
                                player.setInitialTrainingSeason(initTraining);
                                player.setFinalTrainingSeason(endTraining);
                                player.setTestingSeason(testingSeason);
                                player.setTrainingSetGenerator(new DBReadTrainingFeaturesSet(player));
                                player.setTestingWk(week);
                                try {
                                    player.initializeTrainingSet();
                                    player.initializeTestData();
                                    player.setEpochs(Integer.parseInt(networkConfig.getEpochsTF()));
                                    player.setTargetError(Double.parseDouble(networkConfig.getTargetErrorTF()));
                                    player.setLearningRate(Double.parseDouble(networkConfig.getLearningRateTF()));
                                    player.setHiddenNodes(Integer.parseInt(networkConfig.getHiddenNodesTF()));
                                    player.setNeuralNetworkTrainingAlgorithm(networkConfig.getNeuralNetworkTrainingAlgorithm());
                                    final DefaultTableModel model = new DefaultTableModel();
                                    model.setColumnIdentifiers(player.getFeatureLabels());
                                    if(player.isValid()) {
                                        player.train();
                                        evaluate(player, model);
                                        double fantasyPts = (double) model.getValueAt(0,model.getColumnCount() - 1);
                                        if(!Double.isNaN(fantasyPts)) {
                                            currentPlayer.setFantasyPts(fantasyPts);
                                        }
                                        else {
                                            currentPlayer.setFantasyPts(0.0);
                                        }
                                    }
                                    else {
                                        currentPlayer.setFantasyPts(0.0);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        trainingThread.setName(String.format("Player %s thread %s",currentPlayer.getName(),threadCount++));
                        threads.add(trainingThread);
                    }
                    long time = System.currentTimeMillis();
                    for(Thread t : threads) {
                        System.out.println(String.format("--- start %s ---",t.getName()));
                        t.start();
                    }
                    for(Thread t : threads) {
                        System.out.println(String.format("--- wait %s ---",t.getName()));
                        t.join();
                    }
                    System.out.println(String.format("ALL DONE IN %s seconds", (System.currentTimeMillis() - time)/1000));
                    Collections.sort(positionPlayers);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                return "";
            }
            
            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                topTenTableModel = new DefaultTableModel();
                topTenTableModel.addColumn("Rank");
                topTenTableModel.addColumn("Week");
                topTenTableModel.addColumn("Team");
                topTenTableModel.addColumn("Position");
                topTenTableModel.addColumn("Player");
                topTenTableModel.addColumn("Expected Fantasy Points");
                int rank = 1;
                int topTenMark = positionPlayers.size() - 20;
                for(int i = positionPlayers.size() - 1; i >= topTenMark; i-- ) {
                    
                    Object data[] = {
                        rank,
                        week,
                        positionPlayers.get(i).getTeam(),
                        positionPlayers.get(i).getPosition(),
                        positionPlayers.get(i).getName(),
                        positionPlayers.get(i).getFantasyPts()
                    };
                    topTenTableModel.addRow(data);
                    rank++;
                }
                resultsTable.setModel(topTenTableModel); 
                networkConfig.enableEstimateFeaturesBtn(true);
            }
            
        };
        worker.execute();
        
    }
    
    private void evaluate(Player player, DefaultTableModel model) throws SQLException {
                List<Object[]> rows = player.evaluate();
                for (Object[] row : rows) {
                    model.addRow(row);
                }
                if(player.isCalculateFeaturesIndividually()) {
                    player.calculateFantasyPoints(model);
                }
            }
}
