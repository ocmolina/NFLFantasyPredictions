/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author omolina
 */
public abstract class AbstractDB implements Database{
    
    private Connection conn;
    
    public AbstractDB(String driverName, String connectionURL) throws Exception {
        this(driverName,connectionURL,"","");
    }
    
    public AbstractDB(String driverName, String connectionURL, String user, String password) throws Exception{
        Class.forName(driverName);
        conn = DriverManager.getConnection(connectionURL,user,password);
    }
    @Override
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
    
}
