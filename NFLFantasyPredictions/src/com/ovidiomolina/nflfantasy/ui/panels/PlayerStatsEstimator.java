/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.ui.panels;

import com.ovidiomolina.db.Database;
import com.ovidiomolina.nflfantasy.PlayerSelection;
import com.ovidiomolina.nflfantasy.model.players.Player;
import com.ovidiomolina.nflfantasy.model.players.PlayerConstants;
import com.ovidiomolina.nflfantasy.model.players.util.DBReadTrainingFeaturesSet;
import com.ovidiomolina.nflfantasy.model.players.util.TrainingFeaturesSetWithEMDataGeneration;
import com.ovidiomolina.nflfantasy.model.players.util.TrainingFeaturesSetWithNaiveDataGeneration;
import com.ovidiomolina.nflfantasy.ui.ChartItem;
import com.ovidiomolina.nflfantasy.ui.ReadVsEstimateValueChart;
import com.ovidiomolina.util.NeuralNetworkFeatureCalculator;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author omolina
 */
public class PlayerStatsEstimator extends JPanel implements NeuralNetworkFeatureCalculator {
    private final PlayerSelection allPlayers;
    private final Database db;
    private JComboBox teamsCombo;
    private JComboBox positionsCombo;
    private JComboBox playersCombo;
    private JComboBox dataGenerationAlgorithmCombo;
    private JCheckBox generateRandomTrainingData;
    private NeuralNetworkConfigPanel neuralNetworkConfig;
    private JProgressBar progressBar;
    private final JFrame parent;
    private boolean estimatingValues;
    private JLabel elapsedTimeLabel;
    private JRadioButton calculateEqFeaturesOption;
    private JRadioButton calculateFpOption;
    private JTabbedPane tabs;
    private boolean calculateFPparametersIndependently;
    
    public PlayerStatsEstimator(JFrame parent,PlayerSelection players, Database db) {
        this.parent = parent;
        this.db = db;
        allPlayers = players;
        calculateFPparametersIndependently = true;
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        tabs = new JTabbedPane();
        tabs.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem closeTab = new JMenuItem("Close Tab");
                    closeTab.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int tab = tabs.getSelectedIndex();
                            tabs.remove(tab);
                        }
                    });
                    menu.add(closeTab);
                    menu.show(tabs,e.getX(),e.getY());
                }
            }
        });
        elapsedTimeLabel = new JLabel("");
        teamsCombo = new JComboBox(PlayerSelection.TEAMS);
        teamsCombo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionChanged();
            }
        });
        positionsCombo = new JComboBox(PlayerSelection.POSITIONS);
        positionsCombo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionChanged();
            }
        });
        dataGenerationAlgorithmCombo = new JComboBox(PlayerConstants.RANDOM_FEATURE_GENERATION_ALGORITHMS);
        dataGenerationAlgorithmCombo.setEnabled(false);
        generateRandomTrainingData = new JCheckBox("Generate Random Training Data");
        generateRandomTrainingData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataGenerationAlgorithmCombo.setEnabled(generateRandomTrainingData.isSelected());
            }
        });
        neuralNetworkConfig = new NeuralNetworkConfigPanel(this);
        calculateEqFeaturesOption = new JRadioButton("Use Neural Networks to calculate each Fantasy Points equation parameter");
        calculateEqFeaturesOption.setSelected(true);
        calculateFpOption = new JRadioButton("Calculate Fantasy Points on a single neural network");
        ButtonGroup calculationOptions = new ButtonGroup();
        calculationOptions.add(calculateEqFeaturesOption);
        calculationOptions.add(calculateFpOption);
        playersCombo = new JComboBox(new DefaultComboBoxModel());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        calculateEqFeaturesOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateFPparametersIndependently = 
                        calculateEqFeaturesOption.isSelected();
                generateRandomTrainingData.setEnabled(true);
            }
            
        });
        calculateFpOption.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateFPparametersIndependently = !calculateFpOption.isSelected();
                generateRandomTrainingData.setSelected(false);
                generateRandomTrainingData.setEnabled(false);
                dataGenerationAlgorithmCombo.setEnabled(false);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                teamsCombo.setSelectedItem("GB");
            }
        });
    }
    
    protected void processEstimates() {
        estimatingValues = true;
        String team = (String) teamsCombo.getSelectedItem();
        String position = (String) positionsCombo.getSelectedItem();
        PlayerSelection.Player selectedPlayer = (PlayerSelection.Player) playersCombo.getSelectedItem();
        String initTraining = neuralNetworkConfig.getTrainingBeginSeason();
        String endTraining = neuralNetworkConfig.getTrainingEndSeason();
        String testingSeason = neuralNetworkConfig.getTestSeason();
        final Player player = Player.createPlayer(db, position, selectedPlayer.getId(), selectedPlayer.getName(), team);
        player.setInitialTrainingSeason(initTraining);
        player.setFinalTrainingSeason(endTraining);
        player.setTestingSeason(testingSeason);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        final JTable resultsTable = new JTable();
        resultsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultsTable.setRowSelectionAllowed(false);
        resultsTable.setColumnSelectionAllowed(true);
        final DefaultTableModel model = new DefaultTableModel();        
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    player.setCalculateFeaturesIndividually(calculateFPparametersIndependently);
                    model.setColumnIdentifiers(player.getFeatureLabels());
                    if(!generateRandomTrainingData.isSelected()) {
                        player.setTrainingSetGenerator(new DBReadTrainingFeaturesSet(player));
                    }
                    else {
                        if(dataGenerationAlgorithmCombo.isEnabled()) {
                            String algorithm = (String)dataGenerationAlgorithmCombo.getSelectedItem();
                            if(algorithm.equalsIgnoreCase(PlayerConstants.RANDOM_FEATURE_GENERATION_ALGORITHMS[PlayerConstants.SINGLE_GAUSSIAN])) {
                                player.setTrainingSetGenerator(new TrainingFeaturesSetWithNaiveDataGeneration(player));
                            }
                            else if (algorithm.equalsIgnoreCase(PlayerConstants.RANDOM_FEATURE_GENERATION_ALGORITHMS[PlayerConstants.MIXED_GAUSSIANS])) {
                                player.setTrainingSetGenerator(new TrainingFeaturesSetWithEMDataGeneration(player));
                            }
                        }
                    }
                    player.initializeTrainingSet();
                    player.initializeTestData();
                    player.setEpochs(Integer.parseInt(neuralNetworkConfig.getEpochsTF()));
                    player.setTargetError(Double.parseDouble(neuralNetworkConfig.getTargetErrorTF()));
                    player.setLearningRate(Double.parseDouble(neuralNetworkConfig.getLearningRateTF()));
                    player.setHiddenNodes(Integer.parseInt(neuralNetworkConfig.getHiddenNodesTF()));
                    player.setNeuralNetworkTrainingAlgorithm(neuralNetworkConfig.getNeuralNetworkTrainingAlgorithm());
                    player.train();
                    evaluate(player, model);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void done() {
                estimatingValues = false;
                neuralNetworkConfig.enableEstimateFeaturesBtn(true);
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                resultsTable.setModel(model);
                JPanel panel = new JPanel(new GridBagLayout());
                String header = null;
                if(!generateRandomTrainingData.isSelected()){
                    header = String.format("%s TRAINED: %s - %s TEST %s",
                        player.getPlayerName(),
                        player.getInitialTrainingSeason(),
                        player.getFinalTrainingSeason(),
                        player.getTestingSeason());
                }
                else {
                    header = String.format("%s TRAINED: %s - %s and %s random generated seasons. TEST %s",
                        player.getPlayerName(),
                        player.getInitialTrainingSeason(),
                        player.getFinalTrainingSeason(),
                        player.getSimulatedSeasons(),
                        player.getTestingSeason());
                }
                final JLabel resultsHeader = new JLabel(header);
                JButton showGraphBtn = new JButton("Show Graph");
                showGraphBtn.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(resultsTable.getSelectedColumns() != null && resultsTable.getSelectedColumns().length > 0) {
                            showResultsChart(player, model, resultsTable.getSelectedColumns());
                        }
                    }
                });
                JButton evaluateTrainedNetBtn = new JButton("Evaluate");
                evaluateTrainedNetBtn.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        player.setTestingSeason(neuralNetworkConfig.getTestSeason());
                        try {
                            String header = "";
                            if (player.getSimulatedSeasons() == 0 ) {
                                header = String.format("%s TRAINED: %s - %s TEST %s",
                                        player.getPlayerName(),
                                        player.getInitialTrainingSeason(),
                                        player.getFinalTrainingSeason(),
                                        player.getTestingSeason());
                            } else {
                                header = String.format("%s TRAINED: %s - %s and %s random generated seasons. TEST %s",
                                        player.getPlayerName(),
                                        player.getInitialTrainingSeason(),
                                        player.getFinalTrainingSeason(),
                                        player.getSimulatedSeasons(),
                                        player.getTestingSeason());
                            }
                            player.initializeTestData();
                            model.setRowCount(0);
                            evaluate(player,model);
                            resultsHeader.setText(header);
                        } catch (SQLException ex) {
                            Logger.getLogger(PlayerStatsEstimator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                });
                
                panel.add(resultsHeader,
                        new GridBagConstraints(0,0,1,1,0.5,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
                panel.add(evaluateTrainedNetBtn,
                        new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
                panel.add(showGraphBtn,
                        new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
                panel.add(new JScrollPane(resultsTable),
                        new GridBagConstraints(0,1,3,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
                tabs.add(player.getPlayerName(),panel);
                tabs.setSelectedIndex(tabs.getTabCount() - 1);
            }

            private void evaluate(Player player, DefaultTableModel model) throws SQLException {
                List<Object[]> rows = player.evaluate();
                for (Object[] row : rows) {
                    model.addRow(row);
                }
                if(calculateFPparametersIndependently)
                    player.calculateFantasyPoints(model);
            }

            private void showResultsChart(Player player, DefaultTableModel model, int columns[]) {
                List<Double> xLabelValues = player.getXLabelValues(model);
                List<ChartItem> realAndEstimatedValues = player.getChartItems(model, columns);

                ReadVsEstimateValueChart chart
                        = new ReadVsEstimateValueChart(parent,
                                String.format("%s SEASON %s Estimated Fantasy Pts", player.getPlayerName(), player.getTestingSeason()),
                                "WEEKS", "Real vs Estimated Fantasy Pts", xLabelValues, realAndEstimatedValues);
                chart.pack();
                RefineryUtilities.centerFrameOnScreen(chart);
                chart.setVisible(true);
            }
        };
        worker.execute();
    }
    
    private void selectionChanged() {
        String team = (String)teamsCombo.getSelectedItem();
        String position = (String) positionsCombo.getSelectedItem();
        List<PlayerSelection.Player> players = allPlayers.getPlayers(String.format("%s-%s",team,position));
        DefaultComboBoxModel model = (DefaultComboBoxModel)playersCombo.getModel();
        model.removeAllElements();
        for(PlayerSelection.Player player: players) {
            model.addElement(player);
        }
    }
    
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        int playerPosComboWidth = 1;
        int y = 0;
        add(new JLabel("TEAM:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(teamsCombo,
                new GridBagConstraints(1, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(new JLabel("POSITION:"),
                new GridBagConstraints(2, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(positionsCombo,
                new GridBagConstraints(3, y, playerPosComboWidth, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(new JLabel("PLAYER:"),
                new GridBagConstraints(4, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(playersCombo,
                new GridBagConstraints(5, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(calculateEqFeaturesOption,
                new GridBagConstraints(0, y, 6,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE,new Insets(5,5,5,5), 0, 0));
        y++;
        add(calculateFpOption,
                new GridBagConstraints(0, y, 6,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE,new Insets(5,5,5,5), 0, 0));
        
        y++;
        add(generateRandomTrainingData,
                new GridBagConstraints(0, y, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("DATA GENERATION ALGORITHM:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(dataGenerationAlgorithmCombo,
                new GridBagConstraints(1, y, 5, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(neuralNetworkConfig,
                new GridBagConstraints(0, y, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(tabs,
                new GridBagConstraints(0, y, 6, 1, 0.5, 0.5, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 0, 0));
        y++;
        add(elapsedTimeLabel,
                new GridBagConstraints(0, y, 6, 1, 0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        y++;
        add(progressBar,
                new GridBagConstraints(0, y, 6, 1, 0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    }

    @Override
    public void estimateFeatures() {
        SwingWorker processEstimatesWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                processEstimates();
                return "";
            }
        };
        
        
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                int counter = 1;
                while(estimatingValues){
                    Thread.sleep(1000);
                    publish(counter);
                    counter++;
                }
                return "";
            }

            @Override
            protected void process(List chunks) {
                int seconds = (Integer)chunks.get(0);
                elapsedTimeLabel.setText(String.format("Elapsed time: %s seconds",seconds));
            }
            
        };
        processEstimatesWorker.execute();
        worker.execute();
        
    }
}
