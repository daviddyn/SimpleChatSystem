package com.davidsoft.natural.chinese;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 同义词库
 */
public final class SynonymBank {

    //单例模式
    private static SynonymBank instance = null;

    /**
     * 获得同义词库对象的实例。
     *
     * @return 同义词库对象的实例
     */
    public static SynonymBank getInstance() {
        if (instance == null) {
            try {
                instance = new SynonymBank(new File("ChineseThesaurus"));
            }
            catch (IOException e) {
                e.printStackTrace();
                instance = null;
            }
        }
        return instance;
    }

    //特殊词汇，特殊词汇将不发生替换
    private HashSet<String> specialWords;
    //所有替换词汇
    private String[] escapedWords;
    //对应法则
    private HashMap<String, Integer> escapes;


    private void LoadSynonymBank(InputStream in) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.rewind();
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.rewind();
        escapedWords = new String[byteBuffer.getShort()];

        byte byte1, byte2;
        for (int i = 0; i < escapedWords.length; ++i) {
            byteBuffer.rewind();
            while (true) {
                byte1 = (byte) in.read();
                byte2 = (byte) in.read();
                if (byte1 == 0 && byte2 == 0) {
                    break;
                }
                byteBuffer.put(byte1);
                byteBuffer.put(byte2);
            }
            escapedWords[i] = new String(byteBuffer.array(), 0, byteBuffer.position(), StandardCharsets.UTF_16LE);
        }

        byteBuffer.rewind();
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.rewind();
        int count = byteBuffer.getInt();
        escapes = new HashMap<>();

        for (int i = 0; i < count; ++i) {
            byteBuffer.rewind();
            while (true) {
                byte1 = (byte) in.read();
                byte2 = (byte) in.read();
                if (byte1 == 0 && byte2 == 0) {
                    break;
                }
                byteBuffer.put(byte1);
                byteBuffer.put(byte2);
            }
            byteBuffer.put((byte) in.read());
            byteBuffer.put((byte) in.read());
            byteBuffer.position(byteBuffer.position() - 2);
            escapes.put(new String(byteBuffer.array(), 0, byteBuffer.position(), StandardCharsets.UTF_16LE), (int) byteBuffer.getShort());
        }

        byteBuffer.rewind();
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.rewind();
        count = byteBuffer.getInt();
        specialWords = new HashSet<>();

        for (int i = 0; i < count; ++i) {
            byteBuffer.rewind();
            while (true) {
                byte1 = (byte) in.read();
                byte2 = (byte) in.read();
                if (byte1 == 0 && byte2 == 0) {
                    break;
                }
                byteBuffer.put(byte1);
                byteBuffer.put(byte2);
            }
            specialWords.add(new String(byteBuffer.array(), 0, byteBuffer.position(), StandardCharsets.UTF_16LE));
        }
    }

    /**
     * 读取同义词库文件构造同义词库对象。
     */
    public SynonymBank(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        IOException exception = null;
        try {
            LoadSynonymBank(in);
        } catch (IOException e) {
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
     * 判断一个词是否属于特殊词汇。
     *
     * @param word 词
     * @return {@code true} if the {@code word} is a sepcial word, {@code false} otherwise.
     */
    public boolean isSpecialWord(String word) {
        return specialWords.contains(word);
    }

    /**
     * 给定词，获得可以替换它的同义词编号。
     *
     * @param word 词
     * @return 可以替换它的同义词编号
     */
    public int getEscapedWordPosition(String word) {
        Integer position = escapes.get(word);
        if (position == null) {
            return -1;
        }
        else {
            return position;
        }
    }

    /**
     * 指定同义词编号获得词的内容
     *
     * @param position 同义词编号
     * @return 词的内容
     */
    public String getEscapedWord(int position) {
        return escapedWords[position];
    }

    /**
     * 通过源文件编译同义词库。
     *
     * @param thesaurusSourceFile 同义词源文件
     * @param specialWordsSourceFile 特殊词汇源文件
     * @param wordBank 编译所需的中文词典
     * @param out 输出
     *
     * @throws IOException 当向输出流{@code out}写入数据发生IO异常时。
     */
    public static void compileSourceFile(File thesaurusSourceFile, File specialWordsSourceFile, WordBank wordBank, OutputStream out) throws IOException {
        ArrayList<String> escapedWords = new ArrayList<>();
        HashMap<String, Integer> escapes = new HashMap<>();
        Scanner scanner = new Scanner(new FileInputStream(thesaurusSourceFile), "GBK");
        while (scanner.hasNext()) {
            String[] segments = scanner.nextLine().split(" ");
            int validCount = 0;
            for (int i = 1; i < segments.length; ++i) {
                if (wordBank.find(Utils.partDivide(segments[i]))) {
                    ++validCount;
                }
                else {
                    segments[i] = null;
                }
            }
            if (validCount >= 2) {
                //find the target word
                int max = 0, maxIndex = 0;
                for (int i = 2; i < segments.length; ++i) {
                    if (segments[i] != null && segments[i].length() > max) {
                        max = segments[i].length();
                        maxIndex = i;
                    }
                }
                if (segments[maxIndex].length() > 1) {
                    escapedWords.add(segments[maxIndex]);
                    for (int i = 1; i < segments.length; ++i) {
                        if (segments[i] != null && i != maxIndex) {
                            if (segments[i].length() > 1 && !escapes.containsKey(segments[i])) {
                                escapes.put(segments[i], escapedWords.size() - 1);
                            }
                        }
                    }
                }
            }
        }
        scanner.close();

        TreeSet<String> specialWords = new TreeSet<>();
        scanner = new Scanner(new FileInputStream(specialWordsSourceFile), "GBK");
        while (scanner.hasNext()) {
            specialWords.add(scanner.next());
        }
        scanner.close();

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.rewind();
        byteBuffer.putShort((short) escapedWords.size());
        out.write(byteBuffer.array(), 0, 2);
        for (String word : escapedWords) {
            out.write(word.getBytes(StandardCharsets.UTF_16LE));
            out.write(0);
            out.write(0);
        }

        byteBuffer.rewind();
        byteBuffer.putInt(escapes.size());
        out.write(byteBuffer.array());
        for (Map.Entry<String, Integer> entry : escapes.entrySet()) {
            out.write(entry.getKey().getBytes(StandardCharsets.UTF_16LE));
            out.write(0);
            out.write(0);
            byteBuffer.rewind();
            byteBuffer.putShort((short)((int)entry.getValue()));
            out.write(byteBuffer.array(), 0, 2);
        }

        byteBuffer.rewind();
        byteBuffer.putInt(specialWords.size());
        out.write(byteBuffer.array());
        for (String word : specialWords) {
            out.write(word.getBytes(StandardCharsets.UTF_16LE));
            out.write(0);
            out.write(0);
        }
    }
}
