package com.davidsoft.natural.chinese;

import java.util.HashMap;
import java.util.Map;

public class WordFrequencyVectorDouble {

    private HashMap<Integer, double[]> vector;
    private double module2;

    public WordFrequencyVectorDouble() {
        vector = new HashMap<>();
        module2 = 0;
    }

    public void merge(int word, double frequency) {
        double[] aDouble = vector.get(word);
        if (aDouble == null) {
            aDouble = new double[1];
            vector.put(word, aDouble);
        }
        aDouble[0] += frequency;
        module2 = -1;
    }

    public void mergeAll(HashMap<Integer, double[]> another) {
        for (Map.Entry<Integer, double[]> element : another.entrySet()) {
            merge(element.getKey(), element.getValue()[0]);
        }
    }

    public void mergeAllInt(HashMap<Integer, Integer> another) {
        for (Map.Entry<Integer, Integer> element : another.entrySet()) {
            merge(element.getKey(), element.getValue());
        }
    }

    public void mergeAll(WordFrequencyVector another) {
        mergeAllInt(another.getVector());
    }

    public void mergeAll(WordFrequencyVectors another) {
        for (WordFrequencyVector vector : another.getVectors()) {
            mergeAll(vector);
        }
    }

    public HashMap<Integer, double[]> getVector() {
        return vector;
    }

    public double getFrequency(int word) {
        if (vector.containsKey(word)) {
            return vector.get(word)[0];
        }
        else {
            return 0;
        }
    }

    private void calculateModule2() {
        if (module2 >= 0) {
            return;
        }
        module2 = 0;
        for (double[] frequency : vector.values()) {
            module2 += (frequency[0] * frequency[0]);
        }
    }

    public double calculateCosine(WordFrequencyVectorDouble another) {
        calculateModule2();
        another.calculateModule2();
        double sum = 0;
        for (Map.Entry<Integer, double[]> element : vector.entrySet()) {
            sum += element.getValue()[0] * another.getFrequency(element.getKey());
        }
        return sum / Math.sqrt(module2 * another.module2);
    }

    public double calculateCosineLog(WordFrequencyVectorDouble another) {
        calculateModule2();
        another.calculateModule2();
        double sum = 0;
        for (Map.Entry<Integer, double[]> element : vector.entrySet()) {
            sum += element.getValue()[0] * another.getFrequency(element.getKey());
        }
        return Math.log10(sum) - Math.log10(Math.sqrt(module2 * another.module2));
    }

    public void clear() {
        vector.clear();
        module2 = 0;
    }
}