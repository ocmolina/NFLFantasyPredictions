/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.ui;

import java.util.List;

/**
 *
 * @author omolina
 */
public class ChartItem {
    
    private final String label;
    private final List<Double> values;

    public ChartItem(String label, List<Double> values) {
        this.label = label;
        this.values = values;
    }

    public String getLabel() {
        return label;
    }

    public List<Double> getValues() {
        return values;
    }
    
    
}
