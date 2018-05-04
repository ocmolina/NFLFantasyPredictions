/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.ui;

import com.ovidiomolina.nflfantasy.ui.panels.PlayerStatsEstimator;
import com.ovidiomolina.db.Database;
import com.ovidiomolina.nflfantasy.PlayerSelection;
import com.ovidiomolina.nflfantasy.ui.panels.EstimateWeekTopPerformers;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 *
 * @author omolina
 */
public class NFLFantasyStatsCalculator extends JFrame {
    private final PlayerStatsEstimator playerStats;
    private final EstimateWeekTopPerformers weekTopPerformers;
    public NFLFantasyStatsCalculator(PlayerSelection players, Database db) {
        super("NFL Player Stats Estimator");
        playerStats = new PlayerStatsEstimator(this, players, db);
        weekTopPerformers = new EstimateWeekTopPerformers(this, players, db);
        layoutComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024,768));
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Player Fantasy Prediction", playerStats);
        tabs.add("Week Top Performers Prediction", weekTopPerformers);
        add(tabs,new GridBagConstraints(0,0,1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,0));
    }
}
