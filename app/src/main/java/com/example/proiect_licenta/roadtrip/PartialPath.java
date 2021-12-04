package com.example.proiect_licenta.roadtrip;

import java.util.Arrays;

public class PartialPath {
    public int[] exploredNodes;
    public double[][] distanceMatrix;
    public double cost;

    public PartialPath(int[] exploredNodes, double[][] distanceMatrix, double cost){
        this.exploredNodes = exploredNodes;
        this.distanceMatrix = distanceMatrix;
        this.cost = cost;
    }

    /**
     * Returnează o listă a tuturor nodurilor care pot fi vizitate
     */
    public int[] getAllVisitableNodes(){
        // Numărul nodurilor care pot fi vizitate este totalul nr de noduri - nodurile vizitate
        int[] visitableNodes = new int[distanceMatrix.length - exploredNodes.length];
        int p = 0;
        for(int i = 0; i < distanceMatrix.length; ++i){
            int finalI = i;
            // Dacă nodul i nu a fost deja vizitat
            // None-match asigura ca nodul curent nu este in nodurile deja vizitate
            // salvate in exploredNodes
            if(Arrays.stream(exploredNodes).noneMatch(x -> x == finalI)){
                visitableNodes[p++] = i;
            }
        }
        return visitableNodes;
    }
}
