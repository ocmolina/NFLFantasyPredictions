/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy;

import com.ovidiomolina.db.Database;
import com.ovidiomolina.db.MySQLDB;
import com.ovidiomolina.nflfantasy.ui.NFLFantasyStatsCalculator;
import javax.swing.SwingUtilities;

/**
 *
 * @author omolina
 */
public class NFLFantasyPredictions {
    public static void main(String args[]) throws Exception {
        final Database db = new MySQLDB();
        final PlayerSelection allPlayers = new PlayerSelection(db);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                NFLFantasyStatsCalculator frame = new NFLFantasyStatsCalculator(allPlayers, db);
                frame.setVisible(true);
            }
        });
        
        
    }
}
