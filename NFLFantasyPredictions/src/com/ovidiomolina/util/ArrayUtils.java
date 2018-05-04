/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.util;

/**
 *
 * @author omolina
 */
public class ArrayUtils {

    public static double[] createPrimitiveDoubleArray(Double[] sourceDoubleArray) {
        double[] array = new double[sourceDoubleArray.length];
        for (int i = 0; i < sourceDoubleArray.length; i++) {
            array[i] = sourceDoubleArray[i];
        }
        return array;
    }
    
    public static double calculateArrayAvgValue(double array[]) {
        double sum = 0.0;
        for(int i = 0; i<array.length; i++) {
            sum += array[i];
        }
        return sum/array.length;
    }
    
    public static double calculateArrayStddevValue(double array[]) {
        double avg = calculateArrayAvgValue(array);
        double sum = 0.0;
        for(int i = 0; i<array.length; i++) {
            sum += Math.pow(array[i] - avg, 2);
        }
        return Math.sqrt(sum/array.length);
    }
    
}
