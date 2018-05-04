/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.nflfantasy.ui;

import java.awt.BasicStroke;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.StandardDialog;

/**
 *
 * @author omolina
 */
public class ReadVsEstimateValueChart extends StandardDialog{
    
    public ReadVsEstimateValueChart(JFrame owner,String title,String xLabel, String yLabel,List<Double> xLabelValues,List<ChartItem> values) {
        super(owner, title,false);
        super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
         title ,
         xLabel ,
         yLabel ,
         createDataset(xLabelValues,values) ,
         PlotOrientation.VERTICAL ,
         true , true , false);   
        
        ChartPanel chartPanel = new ChartPanel( xylineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        final XYPlot plot = xylineChart.getXYPlot( );
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        for(int i = 0; i<values.size(); i++) {
            renderer.setSeriesStroke( i , new BasicStroke( 2.0f ) );
        }
        chartPanel.addChartMouseListener(new ChartMouseListener(){
            @Override
            public void chartMouseClicked(ChartMouseEvent cme) {
                if(cme.getEntity() instanceof LegendItemEntity) {
                    LegendItemEntity itemEntity = (LegendItemEntity) cme.getEntity();
                    XYDataset dataset = (XYDataset) itemEntity.getDataset();
                    int index = dataset.indexOf(itemEntity.getSeriesKey());
                    XYPlot plot = (XYPlot) cme.getChart().getPlot();
                    //set the renderer to hide the series
                    XYItemRenderer renderer = plot.getRenderer();
                    renderer.setSeriesVisible(index, !renderer.isSeriesVisible(index), false);
                    renderer.setSeriesVisibleInLegend(index, true, false);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent cme) {
                
            }
        });
        plot.setRenderer( renderer ); 
        setContentPane( chartPanel );
    }
    
    private XYDataset createDataset(List<Double> xLabelValues, List<ChartItem> values) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for(ChartItem chartItem : values) {
            XYSeries series = new XYSeries(chartItem.getLabel());
            for(int i = 0; i<chartItem.getValues().size(); i++) {
                series.add(xLabelValues.get(i),
                        chartItem.getValues().get(i));
            }
            dataset.addSeries(series);
        }
        return dataset;
    }    
}
