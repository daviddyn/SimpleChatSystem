package com.davidsoft.natural.chinese;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 中文字典。
 * 建模的信息包括：词频、词性。可应用于概率语言模型。
 */
public final class WordBank {

    /**
     * 词性枚举。
     */
    public enum PartOfSpeechType {
        TYPE_A, TYPE_C, TYPE_D, TYPE_E, TYPE_F, TYPE_H,
        TYPE_I, TYPE_J, TYPE_K, TYPE_M, TYPE_MQ, TYPE_N,
        TYPE_ND, TYPE_NH, TYPE_NHF, TYPE_NHS, TYPE_NI, TYPE_NL,
        TYPE_NS, TYPE_NT, TYPE_O, TYPE_P, TYPE_Q, TYPE_R,
        TYPE_U, TYPE_V, TYPE_VD, TYPE_VL, TYPE_VU;
        public static PartOfSpeechType parseType(String tag) {
            switch (tag) {
                case "a":
                    return TYPE_A;
                case "c":
                    return TYPE_C;
                case "d":
                    return TYPE_D;
                case "e":
                    return TYPE_E;
                case "f":
                    return TYPE_F;
                case "h":
                    return TYPE_H;
                case "i":
                    return TYPE_I;
                case "j":
                    return TYPE_J;
                case "k":
                    return TYPE_K;
                case "m":
                    return TYPE_M;
                case "mq":
                    return TYPE_MQ;
                case "n":
                    return TYPE_N;
                case "nd":
                    return TYPE_ND;
                case "nh":
                    return TYPE_NH;
                case "nhf":
                    return TYPE_NHF;
                case "nhs":
                    return TYPE_NHS;
                case "ni":
                    return TYPE_NI;
                case "nl":
                    return TYPE_NL;
                case "ns":
                    return TYPE_NS;
                case "nt":
                    return TYPE_NT;
                case "o":
                    return TYPE_O;
                case "p":
                    return TYPE_P;
                case "q":
                    return TYPE_Q;
                case "r":
                    return TYPE_R;
                case "u":
                    return TYPE_U;
                case "v":
                    return TYPE_V;
                case "vd":
                    return TYPE_VD;
                case "vl":
                    return TYPE_VL;
                case "vu":
                    return TYPE_VU;
                default:
                    return null;
            }
        }
    }

    /**
     * Tire树中的词节点。
     */
    private class CharacterNode {
        private double frequency;    //词频
        private PartOfSpeechType partOfSpeech;   //词性
        private HashMap<String, CharacterNode> childHash;    //子节点
    }

    //单例模式
    private static WordBank chineseInstance = null;

    /**
     * 获得中文词库对象的实例。
     *
     * @return 中文词库对象的实例
     */
    public static WordBank getChineseInstance() {
        if (chineseInstance == null) {
            try {
                chineseInstance = new WordBank(new File("ChineseFreqDict"));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return chineseInstance;
    }

    private CharacterNode treeRootNode; //Tire树根
    private int wordCount;  //词典词总数

    private double lastFrequency;   //上一次匹配成功的词的频度
    private PartOfSpeechType lastPartOfSpeech;  //上一次匹配成功的词的词性

    /**
     * Create an empty word bank.
     */
    public WordBank() {
        treeRootNode = new CharacterNode();
    }

    private void LoadWordBank(InputStream in) throws IOException {
        treeRootNode = new CharacterNode();
        Queue<CharacterNode> queue = new LinkedList<>();
        queue.add(treeRootNode);
        CharacterNode treeNode;
        CharacterNode tmpNode;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int peakValue;
        while (!queue.isEmpty()) {
            peakValue = (byte) in.read();
            treeNode = queue.poll();
            if (peakValue == -2) {
                continue;
            }
            treeNode.childHash = new HashMap<>();
            do {
                tmpNode = new CharacterNode();
                if (peakValue != -1) {
                    tmpNode.partOfSpeech = PartOfSpeechType.values()[peakValue];
                }
                buffer.rewind();
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                buffer.rewind();
                tmpNode.frequency = buffer.getDouble();
                buffer.rewind();
                buffer.put((byte) in.read());
                buffer.put((byte) in.read());
                if (Utils.getCharacterType(buffer.getChar(0)) != Utils.CharacterType.CHINESE) {
                    int byte1, byte2;
                    while (true) {
                        byte1 = in.read();
                        byte2 = in.read();
                        if (byte1 == 0 && byte2 == 0) {
                            break;
                        }
                        buffer.put((byte) byte1);
                        buffer.put((byte) byte2);
                    }
                }
                treeNode.childHash.put(new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_16LE), tmpNode);
                if (tmpNode.frequency != 0) {
                    ++wordCount;
                }
                queue.offer(tmpNode);
                peakValue = (byte) in.read();
            } while (peakValue != -2);
        }
    }

    /**
     * Load an word bank form a file.
     */
    public WordBank(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        IOException exception = null;
        try {
            LoadWordBank(in);
        }
        catch (IOException e) {
            exception = e;
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Save the bank to a stream with compiled data.
     */
    public void save(OutputStream out) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        Queue<CharacterNode> queue = new LinkedList<>();
        queue.add(treeRootNode);
        CharacterNode treeNode;
        while (!queue.isEmpty()) {
            treeNode = queue.poll();
            if (treeNode.childHash != null) {
                for (Map.Entry<String, CharacterNode> entry : treeNode.childHash.entrySet()) {
                    if (entry.getValue().partOfSpeech == null) {
                        out.write(255);
                    }
                    else {
                        out.write(entry.getValue().partOfSpeech.ordinal());
                    }
                    buffer.rewind();
                    buffer.putDouble(entry.getValue().frequency);
                    out.write(buffer.array());
                    if (Utils.getCharacterType(entry.getKey().charAt(0)) == Utils.CharacterType.CHINESE) {
                        out.write(entry.getKey().getBytes(StandardCharsets.UTF_16LE), 0, 2);
                    }
                    else {
                        out.write(entry.getKey().getBytes(StandardCharsets.UTF_16LE));
                        out.write(0);
                        out.write(0);
                    }
                    queue.offer(entry.getValue());
                }
            }
            out.write(254);
        }
    }

    /**
     *  Add or update a word in bankNumber.
     *
     *  @return {@code true} if updated, {@code false} if added.
     */
    public boolean set(String[] parts, PartOfSpeechType partOfSpeech, double frequency) {
        int i = parts.length - 1;
        CharacterNode treeNode = treeRootNode;
        CharacterNode tmpNode;
        while (i >= 0 && treeNode.childHash != null) {
            tmpNode = treeNode.childHash.get(parts[i]);
            if (tmpNode == null) {
                break;
            }
            treeNode = tmpNode;
            --i;
        }
        if (i >= 0) {
            while (i >= 0) {
                if (treeNode.childHash == null) {
                    treeNode.childHash = new HashMap<>();
                }
                tmpNode = new CharacterNode();
                treeNode.childHash.put(parts[i], tmpNode);
                treeNode = tmpNode;
                --i;
            }
            treeNode.partOfSpeech = partOfSpeech;
            treeNode.frequency = frequency;
            if (frequency != 0) {
                ++wordCount;
            }
            return false;
        }
        else {
            treeNode.partOfSpeech = partOfSpeech;
            if (treeNode.frequency == 0) {
                if (frequency != 0) {
                    treeNode.frequency = frequency;
                    ++wordCount;
                    return false;
                }
                else {
                    return true;
                }
            }
            else {
                treeNode.frequency = frequency;
                if (frequency == 0) {
                    --wordCount;
                }
                return true;
            }
        }
    }

    /**
     *  To find a word if it is in the bank. If succeeded, use
     *  {@code getLastPartOfSpeech()} to get the part of speech of this word; use
     *  {@code getLastFrequency()} to get the frequency of this word.
     *
     *  @return {@code true} if succeeded, {@code false} otherwise.
     */
    public boolean find(String[] parts) {
        int i = parts.length - 1;
        CharacterNode treeNode = treeRootNode;
        CharacterNode tmpNode;
        while (i >= 0 && treeNode.childHash != null) {
            tmpNode = treeNode.childHash.get(parts[i]);
            if (tmpNode == null) {
                break;
            }
            treeNode = tmpNode;
            --i;
        }
        if (i >= 0 || treeNode.frequency == 0) {
            return false;
        }
        else {
            lastFrequency = treeNode.frequency;
            lastPartOfSpeech = treeNode.partOfSpeech;
            return true;
        }
    }

    /**
     *  Get the part of speech of the last found word.
     *
     *  @return The part of speech, {@code null} if the part of speech of this word is undefined.
     */
    public PartOfSpeechType getLastPartOfSpeech() {
        return lastPartOfSpeech;
    }

    /**
     *  Get the frequency of the last found word.
     *
     *  @return The frequency, {@code 0d} if the frequency of this word is undefined.
     */
    public double getLastFrequency() {
        return lastFrequency;
    }

    /**
     *  Get the frequency trace while finding a word.
     *
     *  The the frequency trace is an array that records the frequency of each tree nodes
     *  while finding a word in the trie tree.
     *
     *  @param parts An array of string units divided by {@link Utils#partDivide}.
     *  @param begin The begin index of the {@code parts} array to get frequency trace.
     *  @param end   The (last+1) index of the {@code parts} array to get frequency trace.
     *  @return The frequency trace. Note: the length of the returned array will ALWAYS
     *          equals to {@code parts.length} even if the parameter {@code begin} and
     *          {@code end} defined a sub-sequence of {@code parts}. The elements that are
     *          out of the range will be zero.
     */
    public double[] getFrequencyTrace(String[] parts, int begin, int end) {
        double[] trace = new double[parts.length];
        int i = end - 1;
        CharacterNode treeNode = treeRootNode;
        CharacterNode tmpNode;
        while (i >= begin && treeNode.childHash != null) {
            tmpNode = treeNode.childHash.get(parts[i]);
            if (tmpNode == null) {
                break;
            }
            trace[i] = tmpNode.frequency;
            treeNode = tmpNode;
            --i;
        }
        return trace;
    }

    /**
     *  Delete a word form the bank.
     *
     *  @return {@code true} if succeeded, {@code false} if no such word.
     */
    public boolean delete(String[] parts) {
        int i = parts.length - 1;
        CharacterNode lastTreeNode = treeRootNode;
        CharacterNode treeNode = treeRootNode;
        CharacterNode tmpNode;
        while (i >= 0 && treeNode.childHash != null) {
            lastTreeNode = treeNode;
            tmpNode = treeNode.childHash.get(parts[i]);
            if (tmpNode == null) {
                break;
            }
            treeNode = tmpNode;
            --i;
        }
        if (i >= 0) {
            return false;
        }
        else {
            if (treeNode.childHash == null) {
                lastTreeNode.childHash.remove(parts[0]);
                return true;
            }
            else {
                if (treeNode.frequency == 0) {
                    return false;
                }
                else {
                    treeNode.frequency = 0;
                    --wordCount;
                    return true;
                }
            }
        }
    }

    /**
     *  Get the number of word in the bank.
     */
    public int getWordCount() {
        return wordCount;
    }

}
