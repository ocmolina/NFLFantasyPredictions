/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.model.players;

import com.ovidiomolina.db.Database;

/**
 *
 * @author omolina
 */
public class TightEnd extends ReceiverPlayer {

    public TightEnd(Database db, String position, String playerId, String playerName, String playerTeam) {
        super(db, position, playerId, playerName, playerTeam);
    }

    @Override
    public String getFromQueryView() {
        return "te_fantasy_features_table";
    }
}
