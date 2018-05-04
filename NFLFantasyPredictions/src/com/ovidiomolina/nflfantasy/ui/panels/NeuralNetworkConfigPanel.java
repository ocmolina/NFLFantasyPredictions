/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.ui.panels;

import com.ovidiomolina.nflfantasy.PlayerSelection;
import com.ovidiomolina.util.NeuralNetworkHelper;
import com.ovidiomolina.util.NeuralNetworkFeatureCalculator;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author omolina
 */
public class NeuralNetworkConfigPanel extends JPanel {
    
    private JComboBox trainingBeginSeason;
    private JComboBox trainingEndSeason;
    private JComboBox testSeason;
    private JComboBox neuralNetworkTrainingAlgorithm;
    private JTextField epochsTF;
    private JTextField targetErrorTF;
    private JTextField learningRateTF;
    private JTextField hiddenNodesTF;
    private JButton estimateFeaturesBtn;
    
    private final NeuralNetworkFeatureCalculator featureCalculator;
    
    public NeuralNetworkConfigPanel(NeuralNetworkFeatureCalculator featureCalculator) {
        this.featureCalculator = featureCalculator;
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        neuralNetworkTrainingAlgorithm = new JComboBox(NeuralNetworkHelper.NEURAL_NETWORK_TRAINING_ALGORITHMS);
        epochsTF = new JTextField("20000");
        targetErrorTF = new JTextField("0.00325");
        learningRateTF = new JTextField("0.000875");
        hiddenNodesTF = new JTextField("15");
        trainingBeginSeason = new JComboBox(PlayerSelection.SEASONS);
        trainingEndSeason = new JComboBox(PlayerSelection.SEASONS);
        testSeason = new JComboBox(PlayerSelection.SEASONS);
        estimateFeaturesBtn = new JButton("Train Network");
        estimateFeaturesBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                estimateFeaturesBtn.setEnabled(false);
                featureCalculator.estimateFeatures();
            }
        });
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                trainingBeginSeason.setSelectedItem("2000");
                trainingEndSeason.setSelectedItem("2014");
                testSeason.setSelectedItem("2015");
            }
        });
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        int y = 0;
        add(new JLabel("Max Epochs:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(epochsTF,
                new GridBagConstraints(1, y, 5, 1, 0.1, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("Target Error:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(targetErrorTF,
                new GridBagConstraints(1, y, 5, 1, 0.1, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("Learning Rate:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(learningRateTF,
                new GridBagConstraints(1, y, 5, 1, 0.1, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("Hidden Nodes:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(hiddenNodesTF,
                new GridBagConstraints(1, y, 5, 1, 0.1, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("Training Algorithm:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(neuralNetworkTrainingAlgorithm,
                new GridBagConstraints(1, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("Training from Season:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(trainingBeginSeason,
                new GridBagConstraints(1, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(new JLabel("To Season:"),
                new GridBagConstraints(2, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(trainingEndSeason,
                new GridBagConstraints(3, y, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        y++;
        add(new JLabel("Test Season:"),
                new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(testSeason,
                new GridBagConstraints(1, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(Box.createHorizontalGlue(),
                new GridBagConstraints(2, y, 2, 1, 0.1, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(estimateFeaturesBtn,
                new GridBagConstraints(5, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        
    }

    public String getTrainingBeginSeason() {
        return (String)trainingBeginSeason.getSelectedItem();
    }

    public String getTrainingEndSeason() {
        return (String)trainingEndSeason.getSelectedItem();
    }

    public String getTestSeason() {
        return (String)testSeason.getSelectedItem();
    }

    public String getNeuralNetworkTrainingAlgorithm() {
        return (String)neuralNetworkTrainingAlgorithm.getSelectedItem();
    }

    public String getEpochsTF() {
        return epochsTF.getText();
    }

    public String getLearningRateTF() {
        return learningRateTF.getText();
    }

    public String getTargetErrorTF() {
        return targetErrorTF.getText();
    }
    
    public String getHiddenNodesTF() {
        return hiddenNodesTF.getText();
    }
    
    public void enableEstimateFeaturesBtn(boolean enable) {
        estimateFeaturesBtn.setEnabled(enable);
    }
}
