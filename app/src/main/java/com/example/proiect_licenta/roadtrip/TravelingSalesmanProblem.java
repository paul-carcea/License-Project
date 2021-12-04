package com.example.proiect_licenta.roadtrip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static com.example.proiect_licenta.roadtrip.MatrixHelper.*;

/**
 * Traveling salesman problem rezolvat cu branch and bound
 */
public class TravelingSalesmanProblem {

    private double[][] distanceMatrix;

    private int[] shortestPath;

    private int startingPoint = 0;

    private double distance = 0.0;

    public TravelingSalesmanProblem(double[][] distanceMatrix){
        this.distanceMatrix = MatrixHelper.deepCopy(distanceMatrix);
    }

    public void solve(){
        infiniteFirstDiag(this.distanceMatrix);

        double upper = Double.POSITIVE_INFINITY;
        double cost = reduceMatrix(this.distanceMatrix);

        ArrayList<PartialPath> visited = new ArrayList<>();
        visited.add(new PartialPath(new int[]{this.startingPoint}, this.distanceMatrix, cost));

        PartialPath shortestCandidate = null;

        // Cât timp mai sunt noduri de vizitat
        while (!visited.isEmpty()){
            PartialPath currentRoute = visited.get(0);
            visited.remove(currentRoute);
            // Dacă noul nod care a fost explorat are costul mai mare decât upper bound
            // Îl ignorăm
            if(currentRoute.cost >= upper){
                continue;
            }

            // ultimul nod vizitat in ruta curenta
            int lastNode = currentRoute.exploredNodes[currentRoute.exploredNodes.length - 1];
            int[] nextToVisit = currentRoute.getAllVisitableNodes();

            if(nextToVisit.length == 0){
                // Nod terminal, nu mai sunt noduri de vizitat
                upper = currentRoute.cost;
                shortestCandidate = currentRoute;
                continue;
            }

            // De la nodul curent vizităm toate nodurile rămase
            for(int nextNode: nextToVisit){
                double[][] nextMatrix = MatrixHelper.deepCopy(currentRoute.distanceMatrix);
                infiniteRow(nextMatrix, lastNode);
                infiniteCol(nextMatrix, nextNode);

                // Nu mă pot întoarce de la acest nod la un nod vizitat anterior
                // Punem distanța în matrice +infinit
                for(int explored: currentRoute.exploredNodes){
                    nextMatrix[nextNode][explored] = Double.POSITIVE_INFINITY;
                }

                // Calcul cost pentru nodul curent
                double currentCost = currentRoute.cost;
                currentCost += reduceMatrix(nextMatrix);
                currentCost += currentRoute.distanceMatrix[lastNode][nextNode];

                // Update la lista de noduri explorate
                // Luăm lista cu drumul anterior și adăugam nodul explorat la lista
                int[] exploredNodes = new int[currentRoute.exploredNodes.length + 1];
                System.arraycopy(currentRoute.exploredNodes, 0, exploredNodes, 0, currentRoute.exploredNodes.length);
                // Adaug ultimul element explorat
                exploredNodes[exploredNodes.length - 1] = nextNode;

                // Salvăm drumul în coadă
                PartialPath nextPath = new PartialPath(exploredNodes, nextMatrix, currentCost);
                visited.add(nextPath);
            }

            // Sortează calea pentru a explora LC (cel mai mic cost) în continuare
            visited.sort(Comparator.comparingDouble(path -> path.cost));
        }

        assert shortestCandidate != null;
        this.distance = shortestCandidate.cost;
        this.shortestPath = shortestCandidate.exploredNodes;
    }

    public void setStartingPoint(int i){
        this.startingPoint = i;
    }

    public int[] getShortestPath() {
        if(this.shortestPath == null){
            throw new RuntimeException("Traveling Salesman Problem not solved! Please call solve() first!");
        }
        return this.shortestPath;
    }

    public double getDistance() {
        if(this.shortestPath == null){
            throw new RuntimeException("Traveling Salesman Problem not solved! Please call solve() first!");
        }
        return this.distance;
    }

}
