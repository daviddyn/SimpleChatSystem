package com.davidsoft.natural.chinese;

import java.util.HashMap;
import java.util.Map;

/**
 * 词频向量
 */
public class WordFrequencyVector {

    //原始自然语言的句子
    private String originalSentence;
    //(词→频度)集合，在中文词库中，每个词具有唯一的序号，因此此处使用序号代表词，不再使用字符串匹配
    private HashMap<Integer, Integer> vector;
    //此向量的模平方
    private int module2;

    /**
     * 以(词→频度)集合创建词频向量，自动计算模平方。
     *
     * @param originalSentence 原始自然语言的句子
     * @param vector           (词→频度)集合
     */
    public WordFrequencyVector(String originalSentence, HashMap<Integer, Integer> vector) {
        this.originalSentence = originalSentence;
        this.vector = vector;
        calculateModule2();
    }

    /**
     * 有时，在构造此对象之前就已经知道了此向量的模平方，因此提供直接指定模平方的构造函数，避免再次计算
     *
     * @param originalSentence 原始自然语言的句子
     * @param vector  (词→频度)集合
     * @param module2 已知的模平方
     */
    public WordFrequencyVector(String originalSentence, HashMap<Integer, Integer> vector, int module2) {
        this.originalSentence = originalSentence;
        this.vector = vector;
        this.module2 = module2;
    }

    //计算模平方
    private void calculateModule2() {
        module2 = 0;
        for (int frequency : vector.values()) {
            module2 += (frequency * frequency);
        }
    }

    /**
     * 获得此向量中出现指定词的次数。
     *
     * @param word 指定词
     * @return 该词出现的次数，从未出现则返回0。
     */
    public int getFrequency(int word) {
        Integer frequency = vector.get(word);
        if (frequency == null) {
            return 0;
        }
        else {
            return frequency;
        }
    }

    /**
     * 获得原始自然语言的句子。
     *
     * @return 原始自然语言的句子
     */
    public String getOriginalSentence() {
        return originalSentence;
    }

    /**
     * 和另一个词频向量计算夹角余弦。
     *
     * @param another 另一个词频向量
     * @return 计算的夹角余弦
     */
    public double calculateCosine(WordFrequencyVector another) {
        int sum = 0;
        for (Map.Entry<Integer, Integer> word : vector.entrySet()) {
            sum += word.getValue() * another.getFrequency(word.getKey());
        }
        return sum / Math.sqrt(module2 * another.module2);
    }

    /**
     * 和另一个词频向量计算夹角余弦，结果取log10对数。
     *
     * @param another 另一个词频向量
     * @return 取了log10对数的夹角余弦
     */
    public double calculateCosineLog(WordFrequencyVector another) {
        int sum = 0;
        for (Map.Entry<Integer, Integer> word : vector.entrySet()) {
            sum += word.getValue() * another.getFrequency(word.getKey());
        }
        return Math.log10(sum) - Math.log10(module2 * another.module2) / 2;
    }

    /**
     * 返回存储稀疏向量的集合。
     *
     * @return 构造此对象时传入的{@code vector}参数。
     */
    public HashMap<Integer, Integer> getVector() {
        return vector;
    }

    /**
     * 获得此向量的模平方。
     *
     * @return 模平方
     */
    public int getModule2() {
        return module2;
    }
}
