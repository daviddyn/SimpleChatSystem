package com.davidsoft.natural.chinese;

/**
 * 多个词频向量的集合。
 * 因为存在一些基于多个词频向量的算法。
 */
public final class WordFrequencyVectors {

    private WordFrequencyVector[] vectors;
    private int originalSentenceLength;

    /**
     * 创建一个词频向量集合。
     *
     * @param vectors 词频向量组
     * @param originalSentenceLength 原始自然语言句子的长度
     */
    public WordFrequencyVectors(WordFrequencyVector[] vectors, int originalSentenceLength) {
        this.vectors = vectors;
        this.originalSentenceLength = originalSentenceLength;
    }

    /**
     * 获得向量组。
     *
     * @return 向量组
     */
    public WordFrequencyVector[] getVectors() {
        return vectors;
    }

    /**
     * 获得原始自然语言句子的长度。
     *
     * @return 原始自然语言句子的长度
     */
    public int getOriginalSentenceLength() {
        return originalSentenceLength;
    }

    //计算最大组合
    private static double combineMax(double[] frequencies, int rowCount, int elemPos, double sum, double max, boolean[] selected) {
        if (elemPos < frequencies.length) {
            for (int i = 0; i < rowCount; ++i) {
                if (!selected[i]) {
                    selected[i] = true;
                    max = combineMax(frequencies, rowCount, elemPos + rowCount, sum + frequencies[elemPos + i], max, selected);
                    selected[i] = false;
                }
            }
            return max;
        }
        else {
            return sum > max ? sum : max;
        }
    }

    /**
     * 和另一个词频向量组计算夹角余弦。
     *
     * @param another 另一个词频向量组
     * @return 计算的夹角余弦
     */
    public double calculateCosine(WordFrequencyVectors another) {
        double[] frequencies = new double[vectors.length * another.vectors.length];
        int i, j;
        for (i = 0; i < vectors.length; ++i) {
            for (j = 0; j < another.vectors.length; ++j) {
                frequencies[i * another.vectors.length + j] = vectors[i].calculateCosine(another.vectors[j])
                        * (vectors[i].getOriginalSentence().length() + another.vectors[j].getOriginalSentence().length())
                        / (originalSentenceLength + another.originalSentenceLength);
            }
        }
        return combineMax(frequencies, another.vectors.length, 0, 0, 0, new boolean[another.vectors.length]);
    }

    /**
     * 和另一个词频向量组计算夹角余弦，结果取log10对数。
     *
     * @param another 另一个词频向量组
     * @return 取了log10对数的夹角余弦
     */
    public double calculateCosineLog(WordFrequencyVectors another) {
        double[] frequencies = new double[vectors.length * another.vectors.length];
        int i, j;
        for (i = 0; i < vectors.length; ++i) {
            for (j = 0; j < another.vectors.length; ++j) {
                frequencies[i * another.vectors.length + j] = vectors[i].calculateCosine(another.vectors[j])
                        + Math.log10(vectors[i].getOriginalSentence().length() + another.vectors[j].getOriginalSentence().length())
                        - Math.log10(originalSentenceLength + another.originalSentenceLength);
            }
        }
        return combineMax(frequencies, another.vectors.length, 0, 0, 0, new boolean[another.vectors.length]);
    }
}