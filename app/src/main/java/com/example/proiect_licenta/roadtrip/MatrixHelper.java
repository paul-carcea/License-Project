package com.example.proiect_licenta.roadtrip;

import java.util.Arrays;

public final class MatrixHelper {

    public static double[][] infiniteFirstDiag(double[][] mat){
        for(int i = 0; i < mat.length; ++i){
            mat[i][i] = Double.POSITIVE_INFINITY;
        }
        return mat;
    }

    ////Returneaza minimul de pe un rand
    public static double minRow(double[][] mat, int row){
        double min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < mat[row].length; ++i){
            if(mat[row][i] < min){
                min = mat[row][i];
            }
        }
        return min;
    }

    //Returneaza minimul de pe o coloana
    public static double minCol(double[][] mat, int col){
        double min = Double.POSITIVE_INFINITY;
        for (double[] doubles : mat) {
            if (doubles[col] < min) {
                min = doubles[col];
            }
        }
        return min;
    }

    public static double[][] infiniteRow(double[][] mat, int row){
        Arrays.fill(mat[row], Double.POSITIVE_INFINITY);
        return mat;
    }

    public static double[][] infiniteCol(double[][] mat, int col){
        for(int i = 0; i < mat.length; ++i){
            mat[i][col] = Double.POSITIVE_INFINITY;
        }
        return mat;
    }

    /**
     * Face o copie a matricei tuturor distantelor intre noduri.
     * @param original = matricea care va fi copiată.
     * @return returnează copia
     */
    public static double[][] deepCopy(double[][] original) {
        final double[][] result = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    /**
     * @return cost
     */
    public static double reduceRow(double[][] mat, int row){
        double minRow = minRow(mat, row);
        if(Double.isInfinite(minRow)){
            return 0.0;
        }
        for(int i = 0; i < mat[row].length; ++i){
            if(Double.isFinite(mat[row][i])){
                mat[row][i] -= minRow;
            }
        }
        return minRow;
    }

    /**
     * @return cost
     */
    public static double reduceCol(double[][] mat, int col){
        double minCol = minCol(mat, col);
        if(Double.isInfinite(minCol)){
            return 0.0;
        }
        for(int i = 0; i < mat.length; ++i){
            if(Double.isFinite(mat[i][col])){
                mat[i][col] -= minCol;
            }
        }
        return minCol;
    }

    /**
     * @return cost
     */
    public static double reduceRows(double[][] mat){
        double cost = 0.0;
        for(int i = 0; i < mat.length; ++i){
            cost += reduceRow(mat, i);
        }
        return cost;
    }

    /**
     * @return cost
     */
    public static double reduceCols(double[][] mat){
        double cost = 0.0;
        for(int i = 0; i < mat.length; ++i){
            cost += reduceCol(mat, i);
        }
        return cost;
    }

    /**
     * @return cost
     */
    public static double reduceMatrix(double[][] mat){
        double cost = 0.0;
        for(int i = 0; i < mat.length; ++i){
            cost += reduceRows(mat);
        }
        for(int i = 0; i < mat[0].length; ++i){
            cost += reduceCols(mat);
        }
        return cost;
    }
}
