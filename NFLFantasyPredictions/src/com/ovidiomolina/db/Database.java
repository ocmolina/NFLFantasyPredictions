/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ovidiomolina.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author ovidio
 */
public interface Database {
    
    public ResultSet executeQuery(String query) throws SQLException;
    
    public void close() throws SQLException;
}
