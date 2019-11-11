package com.davidsoft.natural.chinese;

import com.davidsoft.natural.SimpleWordReader;
import com.davidsoft.natural.WordReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * 对话样本库。
 *
 * 本类负责对话样本库的加载和查询，维持着对话样本库的数据结构。
 */
public final class ChatBank {

    //对象管理

    private static ChatBank instance = null;

    /**
     * ChatBank(本类)需要以单例模式创建对象。调用此函数以获取唯一的实例。
     *
     * @return 一个ChatBank对象的实例。
     */
    public static ChatBank getInstance() {
        if (instance == null) {
            try {
                FileInputStream fileIn = new FileInputStream("ChineseChats");
                instance = new ChatBank(fileIn);
                fileIn.close();
            }
            catch (IOException e) {
                instance = null;
            }
        }
        return instance;
    }

    //对话样本库的数据结构

    //样本库中用到的所有词。升序存储以供二分查找
    private String[] words;

    //一个问答对
    public final static class ChatPair {
        private WordFrequencyVectors sentences; //问句，词频向量组形式
        private String answer;                  //答句

        public WordFrequencyVectors getSentences() {
            return sentences;
        }

        public String getAnswer() {
            return answer;
        }
    }

    //多个问答对构成一个对话
    public final static class Chat {
        private ChatPair[] chatPairs;

        public ChatPair getChatPair(int position) {
            return chatPairs[position];
        }
    }

    //多个对话构成对话样本库
    private Chat[] chats;

    //对话样本库的索引

    //答句的索引。
    //wordAnswerIndex[i]是一个数组，是包含了词i的所有问句所对应的答句，数组元素类型是整数对，a代表对话编号，b代表对话内答句的位置。
    public PairInt[][] wordAnswerIndex;

    /**
     * 构造一个对话样本库。请使用{@link ChatBank#getInstance}静态方法获得对话样本库的实例。
     */
    private ChatBank(InputStream in) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(40960);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        int read;

        //read all word and index
        byteBuffer.rewind();
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.rewind();
        words = new String[byteBuffer.getInt()];
        wordAnswerIndex = new PairInt[words.length][];
        for (int i = 0; i < words.length; ++i) {
            byteBuffer.rewind();
            while ((read = in.read()) != 0) {
                byteBuffer.put((byte) read);
            }
            words[i] = new String(byteBuffer.array(), 0, byteBuffer.position(), "GBK");
            byteBuffer.rewind();
            byteBuffer.put((byte) in.read());
            byteBuffer.put((byte) in.read());
            byteBuffer.put((byte) in.read());
            byteBuffer.put((byte) in.read());
            byteBuffer.rewind();
            wordAnswerIndex[i] = new PairInt[byteBuffer.getInt()];
            for (int j = 0; j < wordAnswerIndex[i].length; ++j) {
                byteBuffer.rewind();
                byteBuffer.put((byte) in.read());
                byteBuffer.put((byte) in.read());
                byteBuffer.put((byte) in.read());
                byteBuffer.put((byte) in.read());
                byteBuffer.rewind();
                wordAnswerIndex[i][j] = new PairInt(byteBuffer.getInt(), in.read());
            }
        }

        //read chat data
        byteBuffer.rewind();
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.put((byte) in.read());
        byteBuffer.rewind();
        chats = new Chat[byteBuffer.getInt()];
        for (int i = 0; i < chats.length; ++i) {
            chats[i] = new Chat();
            chats[i].chatPairs = new ChatPair[in.read()];
            for (int j = 0; j < chats[i].chatPairs.length; ++j) {
                WordFrequencyVector[] wordFrequencyVectors = new WordFrequencyVector[in.read()];
                int sentenceLength = 0;
                for (int k = 0; k < wordFrequencyVectors.length; ++k) {
                    byteBuffer.rewind();
                    while ((read = in.read()) != 0) {
                        byteBuffer.put((byte) read);
                    }
                    String originalSentence = new String(byteBuffer.array(), 0, byteBuffer.position(), "GBK");
                    sentenceLength += originalSentence.length();
                    HashMap<Integer, Integer> vector = new HashMap<>();
                    while ((read = in.read()) != 0) {
                        byteBuffer.rewind();
                        byteBuffer.put((byte) in.read());
                        byteBuffer.put((byte) in.read());
                        byteBuffer.put((byte) in.read());
                        byteBuffer.put((byte) in.read());
                        byteBuffer.rewind();
                        vector.put(byteBuffer.getInt(), read);
                    }
                    byteBuffer.rewind();
                    byteBuffer.put((byte) in.read());
                    byteBuffer.put((byte) in.read());
                    byteBuffer.put((byte) in.read());
                    byteBuffer.put((byte) in.read());
                    byteBuffer.rewind();
                    int module2 = byteBuffer.getInt();

                    wordFrequencyVectors[k] = new WordFrequencyVector(originalSentence, vector, module2);
                }
                byteBuffer.rewind();
                while ((read = in.read()) != 0) {
                    byteBuffer.put((byte) read);
                }
                chats[i].chatPairs[j] = new ChatPair();
                chats[i].chatPairs[j].sentences = new WordFrequencyVectors(wordFrequencyVectors, sentenceLength);
                chats[i].chatPairs[j].answer = new String(byteBuffer.array(), 0, byteBuffer.position(), "GBK");
            }
        }
    }

    private ChatBank() {}

    /**
     * 通过给定词获得其编号。
     *
     * @param word 词
     * @return 词的编号。若不存在该次则返回-1。
     */
    public int getWordNumber(String word) {
        //二分法查找
        int left = 0;
        int right = words.length;
        int middle;
        int compare;
        while (left < right) {
            middle = (left + right) / 2;
            compare = word.compareTo(words[middle]);
            if (compare > 0) {
                left = middle + 1;
            }
            else {
                if (compare < 0) {
                    right = middle;
                }
                else {
                    return middle;
                }
            }
        }
        return Utils.isNumeric(word) ? -2 : -1;
    }

    /**
     * 通过词编号获得词。
     *
     * @param wordNumber 词编号
     * @return 词
     */
    public String getWord(int wordNumber) {
        return words[wordNumber];
    }

    /**
     * 获得包含给定词的所有问句所对应的答句。
     *
     * @param wordNumber 词编号
     * @return 一个PairInt数组，表示所有答句。PairInt.a代表对话编号，PairInt.b代表对话内答句的位置。
     */
    public PairInt[] getChatNumbersContainWord(int wordNumber) {
        return wordAnswerIndex[wordNumber];
    }

    /**
     * 通过对话编号获得对话。
     *
     * @param chatNumber 对话编号
     * @return 对话
     */
    public Chat getChat(int chatNumber) {
        return chats[chatNumber];
    }


    private static class WordFrequencyVectorString extends WordFrequencyVector {
        private HashMap<String, Integer> stringVector;
        private WordFrequencyVectorString(String originalSentence, HashMap<String, Integer> stringVector) {
            super(originalSentence, null, calcModule2(stringVector));
            this.stringVector = stringVector;
        }
        private static int calcModule2(HashMap<String, Integer> stringVector) {
            int module2 = 0;
            for (int value : stringVector.values()) {
                module2 += (value * value);
            }
            return module2;
        }
    }

    /**
     * 通过源文件编译对话样本库。
     *
     * @param sourceFiles 源文件列表
     * @param out 编译结果的输出流
     *
     * @throws IOException 当向输出流{@code out}写入数据发生IO异常时
     */
    public static void compileSourceFiles(File[] sourceFiles, WordBank wordBank, SynonymBank synonymBank, OutputStream out) throws IOException {
        FileInputStream fileIn;
        Scanner fileScanner;
        String string;
        SentenceSegmenter sentenceSegmenter;
        SentenceSegmenter.ContextType lastContextType;
        WordSegmenter wordSegmenter = new WordSegmenter(wordBank);
        WordReader wordReader;

        int chatPairPosition = 0;

        TreeMap<String, ArrayList<PairInt>> wordsIndexs = new TreeMap<>();
        ArrayList<PairInt> indexs;

        ArrayList<Chat> chats = new ArrayList<>();
        Chat chat;
        ArrayList<ChatPair> chatPairsBuilder = new ArrayList<>();
        ChatPair chatPair;
        ArrayList<WordFrequencyVectorString> sentenceVectorsBuilder = new ArrayList<>();

        //读取文件
        for (File file : sourceFiles) {
            try {
                System.out.println("正在处理 " + file.getPath());
                fileIn = new FileInputStream(file);
                fileScanner = new Scanner(fileIn, "GBK");

                //初始化新的段落
                chatPairsBuilder.clear();

                while (fileScanner.hasNext()) {
                    string = fileScanner.nextLine();
                    if (string.length() == 0) {
                        //本段已结束
                        if (chatPairPosition > 0) {
                            chat = new Chat();
                            chat.chatPairs = new ChatPair[chatPairsBuilder.size()];
                            chatPairsBuilder.toArray(chat.chatPairs);
                            chats.add(chat);
                        }
                        chatPairPosition = 0;
                        chatPairsBuilder.clear();
                        continue;
                    }

                    sentenceVectorsBuilder.clear();

                    //读取问句
                    sentenceSegmenter = new SentenceSegmenter(new FormattedReader(new StringReader(string)));

                    //构造问答对
                    chatPair = new ChatPair();

                    //构造问题内容
                    if (fileScanner.hasNext()) {
                        chatPair.answer = fileScanner.nextLine();
                    }
                    else {
                        chatPair.answer = "";
                    }

                    if (chatPair.answer.length() == 0) {
                        //本段已结束
                        if (chatPairPosition > 0) {
                            chat = new Chat();
                            chat.chatPairs = new ChatPair[chatPairsBuilder.size()];
                            chatPairsBuilder.toArray(chat.chatPairs);
                            chats.add(chat);
                        }
                        chatPairPosition = 0;
                        chatPairsBuilder.clear();
                        continue;
                    }

                    //分句
                    int sentenceLength = 0;
                    while ((string = sentenceSegmenter.nextSentence()) != null) {
                        lastContextType = sentenceSegmenter.getLastContextType();
                        //跳过引用、括号中的内容
                        if (lastContextType == SentenceSegmenter.ContextType.IN_BRACKET || lastContextType == SentenceSegmenter.ContextType.IN_QUOTE) {
                            continue;
                        }
                        //分词、数字转换和同义词转换
                        wordReader = new SynonymReplaceReader(new SimpleWordReader(wordSegmenter.segment(string)), synonymBank);
                        //构造子句
                        HashMap<String, Integer> vector = new HashMap<>();
                        sentenceLength += string.length();
                        String originalSentence = string;
                        while (wordReader.hasNext()) {
                            string = wordReader.nextWord();
                            //添加索引
                            indexs = wordsIndexs.get(string);
                            if (indexs == null) {
                                indexs = new ArrayList<>();
                                wordsIndexs.put(string, indexs);
                            }
                            indexs.add(new PairInt(chats.size(), chatPairPosition));
                            //构造向量
                            Integer frequency = vector.get(string);
                            if (frequency == null) {
                                frequency = 0;
                            }
                            vector.put(string, frequency + 1);
                        }

                        //计算模平方同时将该分句加入集合
                        sentenceVectorsBuilder.add(new WordFrequencyVectorString(originalSentence, vector));
                    }
                    WordFrequencyVector[] vectors = new WordFrequencyVector[sentenceVectorsBuilder.size()];
                    sentenceVectorsBuilder.toArray(vectors);
                    chatPair.sentences = new WordFrequencyVectors(vectors, sentenceLength);

                    //将该问答对加入集合
                    chatPairsBuilder.add(chatPair);

                    ++chatPairPosition;
                }
                //本段已结束
                if (chatPairPosition > 0) {
                    chat = new Chat();
                    chat.chatPairs = new ChatPair[chatPairsBuilder.size()];
                    chatPairsBuilder.toArray(chat.chatPairs);
                    chats.add(chat);
                }
                chatPairPosition = 0;
                chatPairsBuilder.clear();
                fileIn.close();
            }
            catch (Exception e) {
                System.out.println("Exception occurred when processing " + file.getPath());
                System.out.println("chartPosition = " + chats.size());
                System.out.println("chatPairPosition = " + chatPairPosition);
                e.printStackTrace();
                return;
            }
        }

        //词库变数组
        ChatBank chatBank = new ChatBank();
        chatBank.words = new String[wordsIndexs.size()];
        wordsIndexs.keySet().toArray(chatBank.words);

        System.out.print("正在写入文件...");

        //写入文件
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        //词的个数
        byteBuffer.rewind();
        byteBuffer.putInt(wordsIndexs.size());
        out.write(byteBuffer.array());
        for (Map.Entry<String, ArrayList<PairInt>> entry : wordsIndexs.entrySet()) {
            //词内容
            out.write(entry.getKey().getBytes("GBK"));
            out.write(0);
            //索引大小
            byteBuffer.rewind();
            byteBuffer.putInt(entry.getValue().size());
            out.write(byteBuffer.array());
            for (PairInt pairInt : entry.getValue()) {
                //段号
                byteBuffer.rewind();
                byteBuffer.putInt(pairInt.a);
                out.write(byteBuffer.array());
                //段内问答对号
                out.write(pairInt.b);
            }
        }
        //段数
        byteBuffer.rewind();
        byteBuffer.putInt(chats.size());
        out.write(byteBuffer.array());
        for (Chat chatI : chats) {
            //问答对数
            out.write(chatI.chatPairs.length);
            for (ChatPair chatPairI : chatI.chatPairs) {
                //问题内容的子句数
                out.write(chatPairI.sentences.getVectors().length);
                for (WordFrequencyVector vectorI : chatPairI.sentences.getVectors()) {
                    //子句内容
                    out.write(vectorI.getOriginalSentence().getBytes("GBK"));
                    out.write(0);
                    //子句向量
                    for (Map.Entry<String, Integer> entry : ((WordFrequencyVectorString)vectorI).stringVector.entrySet()) {
                        out.write(entry.getValue());
                        byteBuffer.rewind();
                        byteBuffer.putInt(chatBank.getWordNumber(entry.getKey()));
                        out.write(byteBuffer.array());
                    }
                    out.write(0);
                    //向量平方和
                    byteBuffer.rewind();
                    byteBuffer.putInt(vectorI.getModule2());
                    out.write(byteBuffer.array());
                }

                //回答内容
                out.write(chatPairI.answer.getBytes("GBK"));
                out.write(0);
            }
        }
        System.out.println("完成！");
    }
}