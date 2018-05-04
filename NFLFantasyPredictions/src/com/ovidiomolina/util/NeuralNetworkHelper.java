/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ovidiomolina.util;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.ConvolutionalBackpropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.nnet.learning.ResilientPropagation;
import org.neuroph.util.TransferFunctionType;

/**
 *
 * @author omolina
 */
public class NeuralNetworkHelper {
    public static final String BACKPROPAGATION = "BACKPROPAGATION";
    public static final String RESILIENT_BACKPROPAGATION ="RESILIENT BACKPROPAGATION";
    public static final String MOMENTUM_BACKPROPAGATION= "MOMENTUM BACKPROPAGATION";
    public static final String CONVOLUTIONAL_BACKPROPAGATION = "CONVOLUTIONAL BACK PROPAGATION";
    public static final String[] NEURAL_NETWORK_TRAINING_ALGORITHMS = 
        {
            BACKPROPAGATION,RESILIENT_BACKPROPAGATION, 
            MOMENTUM_BACKPROPAGATION, CONVOLUTIONAL_BACKPROPAGATION 
        };
    
    public static NeuralNetwork train(double[][] trainingSet, double[] resultValues, int maxEpochs, double targetError, double learningRate, int hiddenNodes, String trainingAlgorithm) {

        final NeuralNetwork neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,trainingSet[0].length ,hiddenNodes,1);
        DataSet trainingDataSet = new DataSet(trainingSet[0].length,1);
        for(int i = 0; i<trainingSet.length; i++) {
            trainingDataSet.addRow(new DataSetRow(trainingSet[i],new double[]{resultValues[i]}));
        }
        
        SupervisedLearning backprop = getLearningAlgorithm(trainingAlgorithm);
        backprop.setMaxError(targetError);
        backprop.setMaxIterations(maxEpochs);
        backprop.setLearningRate(learningRate);
        neuralNetwork.setLearningRule(backprop);
        backprop.addListener(new LearningEventListener() {
            @Override
            public void handleLearningEvent(LearningEvent le) {
                SupervisedLearning bp = (SupervisedLearning)le.getSource();
                int iteration = bp.getCurrentIteration();
                if(iteration % 1000 == 0) {
                    System.out.println(String.format("THREAD: %s Epoch: %s Error: %s, LR: %s",Thread.currentThread().getName(), bp.getCurrentIteration(), bp.getTotalNetworkError(),bp.getLearningRate()));
                }
            }
        });
        neuralNetwork.learn(trainingDataSet);
        return neuralNetwork;
    }

    private static SupervisedLearning getLearningAlgorithm(String algorithm) {
        if(algorithm.equalsIgnoreCase(NeuralNetworkHelper.RESILIENT_BACKPROPAGATION)){
            return new ResilientPropagation();
        }
        if(algorithm.equalsIgnoreCase(NeuralNetworkHelper.MOMENTUM_BACKPROPAGATION)) {
            return new MomentumBackpropagation();
        }
        if(algorithm.equalsIgnoreCase(NeuralNetworkHelper.CONVOLUTIONAL_BACKPROPAGATION)) {
            return new ConvolutionalBackpropagation();
        }
        return new BackPropagation();
    }
}
