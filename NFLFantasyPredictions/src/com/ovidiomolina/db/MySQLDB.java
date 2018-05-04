/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.db;

/**
 *
 * @author omolina
 */
public class MySQLDB extends AbstractDB{

    private static final String driverName = "com.mysql.jdbc.Driver";
    private static final String connectionURL = "jdbc:mysql://localhost:3306/nfl_2000_2017";
    
    public MySQLDB() throws Exception{
        super(driverName,connectionURL,"root","root");
    }
}
