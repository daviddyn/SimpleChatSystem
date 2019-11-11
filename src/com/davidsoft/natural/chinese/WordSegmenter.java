package com.davidsoft.natural.chinese;

import java.util.LinkedList;

/**
 * 中文分词器
 */
public final class WordSegmenter {

    private WordBank wordBank;

    public WordSegmenter(WordBank wordBank) {
        this.wordBank = wordBank;
    }

    public String[] segment(String sentence) {
        String[] parts = Utils.partDivide(sentence);
        double[] cps = new double[parts.length + 1];
        int [] chooses = new int[parts.length + 1];
        double[] frequencyTrace;
        double tmp;
        double preEffectiveValue = 0;
        int n, i;
        for (n = 1; n <= parts.length; ++n) {
            chooses[n] = n - 1;
            frequencyTrace = wordBank.getFrequencyTrace(parts, 0, n);
            for (i = n - 1; i >= 0; --i) {
                if (frequencyTrace[i] == -1) {
                    if (i == n - 1) {
                        frequencyTrace[i] = 0.0001;
                    }
                    else {
                        if (i == 0 || frequencyTrace[i - 1] >= 0) {
                            if (preEffectiveValue == 0) {
                                preEffectiveValue = 0.0001;
                            }
                            frequencyTrace[i] = preEffectiveValue * 2;
                        }
                        else {
                            frequencyTrace[i] = 0;
                        }
                    }
                }
                if (frequencyTrace[i] > 0) {
                    preEffectiveValue = frequencyTrace[i];
                    tmp = Math.log10(frequencyTrace[i]) + cps[i];
                    if (tmp >= cps[n] || cps[n] == 0) {
                        cps[n] = tmp;
                        chooses[n] = i;
                    }
                }
            }
        }
        LinkedList<String> segmentsBuilder = new LinkedList<>();
        i = parts.length;
        do {
            n = i;
            i = chooses[i];
            segmentsBuilder.addFirst(Utils.combineParts(parts, i, n));
        } while (i > 0);
        String[] segments = new String[segmentsBuilder.size()];
        segmentsBuilder.toArray(segments);
        return segments;
    }
}
