package com.davidsoft.natural.chinese;

import com.davidsoft.natural.ChatCommand;
import com.davidsoft.natural.SimpleWordReader;
import com.davidsoft.natural.WordReader;

import java.io.Reader;
import java.util.*;

/**
 * 对话系统。
 *
 * 一个本类的实例维护一个对话系统。包括输入问句输出答句，以及语境上下文的维持。
 * 可以创建多个本类的实例，这些实例可以互不干涉地同时为多个用户服务。
 */
public class ChattingSystem {

    //词汇上下文。将之前用户的输入的分词结果进行加权保存，保存的词汇将和用户新输入的分词结果一并参与对话库检索。
    //Integer：词编号
    //double[]：只有一个元素(为了可以改变其值)，代表该词的频度。初始值为词频，随后每进行一轮对话就降低其值。
    private WordFrequencyVectorDouble wordsContext;

    //对话上下文。保存上一次在库中检索成功的问句所在的对话编号，下一次检索时，属于同一个对话中的答句将具有更高的权重。
    private PairInt chatPairContext;

    //更新词汇上下文
    private void updateWordsContext() {
        Iterator<Map.Entry<Integer, double[]>> iterator = wordsContext.getVector().entrySet().iterator();
        Map.Entry<Integer, double[]> pair;
        while (iterator.hasNext()) {
            pair = iterator.next();
            pair.getValue()[0] /= 4.0;
            if (pair.getValue()[0] < 0.001) {
                iterator.remove();
            }
        }
    }

    //答句转义。答句中可能会包含需要使用程序算法即时生成的内容(如时间日期信息等)，此函数负责调用这些生成程序，拼接成完整的答句。
    private String escapeAnswer(String answer, Object extras) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < answer.length(); ++i) {
            if (answer.charAt(i) == '{') {
                int j = answer.indexOf(':', ++i);
                String className = answer.substring(i, j);
                i = answer.indexOf('}', ++j);
                String methodName = answer.substring(j, i);
                if (className.equals(".Random")) {
                    try {
                        builder.append(RandomResponseBank.getInstance().getResponse(methodName));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        builder.append("{此部分内容未能成功生成：").append(className).append(":").append(methodName).append("}");
                    }
                }
                else if (className.startsWith(".")) {
                    try {
                        ChatCommand chatCommand = (ChatCommand) Class.forName("com.davidsoft.natural.chinese.commands" + className).getConstructor().newInstance();
                        builder.append(chatCommand.execute(methodName, extras));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        builder.append("{此部分内容未能成功生成：").append(className).append(":").append(methodName).append("}");
                    }
                }
                else {
                    builder.append("{").append(className).append(":").append(methodName).append("}");
                }
            }
            else {
                builder.append(answer.charAt(i));
            }
        }
        return builder.toString();
    }

    /**
     * 构造一个对话系统。
     */
    public ChattingSystem() {
        wordsContext = new WordFrequencyVectorDouble();
        chatPairContext = new PairInt(-1 , 0);
    }

    /**
     * 清空语境上下文，开始一场全新的对话。
     */
    public void clearContext() {
        wordsContext.clear();
        chatPairContext.a = -1;
    }

    /**
     * 对话系统的“主函数”。输入问句，返回答句。
     *
     * @param question 提供问句内容的Reader。如此设计而不直接使用String的原因是为了更好地兼容流式的问句。
     * @param showStep 为{@code true}时，在返回答句的同时还向控制台输出计算过程。
     * @return 答句
     */
    public String getAnswer(Reader question, boolean showStep) {
        //准备分句器
        SentenceSegmenter sentenceSegmenter = new SentenceSegmenter(new FormattedReader(question));
        //准备分词器
        WordSegmenter wordSegmenter = new WordSegmenter(WordBank.getChineseInstance());
        //准备扯淡库
        ChatBank chatBank = ChatBank.getInstance();

        //答句候选集
        TreeSet<PairInt> alterSet = new TreeSet<>();

        //输入的信息
        String originalSentence;
        SentenceSegmenter.ContextType lastContextType;

        updateWordsContext();

        WordFrequencyVectorDouble inputVector = null;
        if (showStep) {
            System.out.print("存储的词汇上下文：");
            if (wordsContext.getVector().size() == 0) {
                System.out.println("Ø");
            }
            else {
                System.out.print("{");
                for (Map.Entry<Integer, double[]> entry : wordsContext.getVector().entrySet()) {
                    System.out.print("<");
                    System.out.print(chatBank.getWord(entry.getKey()));
                    System.out.print(",");
                    System.out.print(entry.getValue()[0]);
                    System.out.print(">");
                }
                System.out.println("}");
            }
            inputVector = new WordFrequencyVectorDouble();
        }

        while ((originalSentence = sentenceSegmenter.nextSentence()) != null) {
            if (showStep) {
                System.out.println();
                System.out.print("子句：");
                System.out.println(originalSentence);
                System.out.print("分词、同义替换：");
            }
            lastContextType = sentenceSegmenter.getLastContextType();
            //跳过引用、括号中的内容
            if (lastContextType == SentenceSegmenter.ContextType.IN_BRACKET || lastContextType == SentenceSegmenter.ContextType.IN_QUOTE) {
                continue;
            }
            //分词、数字转换和同义词转换
            WordReader wordReader = new SynonymReplaceReader(new NumberConvertWordReader(new SimpleWordReader(wordSegmenter.segment(originalSentence))), SynonymBank.getInstance());
            //获得子句向量
            while (wordReader.hasNext()) {
                String word = wordReader.nextWord();
                if (showStep) {
                    System.out.print(word);
                    System.out.print("/");
                }
                int wordPosition = chatBank.getWordNumber(word);
                if (wordPosition < 0) {
                    continue;
                }
                if (showStep) {
                    inputVector.merge(wordPosition, 1);
                }
                wordsContext.merge(wordPosition, 1);
            }
            if (showStep) {
                System.out.println();
                System.out.print("向量：{");
                for (Map.Entry<Integer, double[]> entry : inputVector.getVector().entrySet()) {
                    System.out.print("<");
                    System.out.print(chatBank.getWord(entry.getKey()));
                    System.out.print(",");
                    System.out.print(entry.getValue()[0]);
                    System.out.print(">");
                }
                System.out.println("}");
            }
        }

        if (showStep) {
            System.out.println();
            System.out.print("最终参与匹配的向量：{");
            for (Map.Entry<Integer, double[]> entry : wordsContext.getVector().entrySet()) {
                System.out.print("<");
                System.out.print(chatBank.getWord(entry.getKey()));
                System.out.print(",");
                System.out.print(entry.getValue()[0]);
                System.out.print(">");
            }
            System.out.println("}");
            System.out.println();
        }

        //准备候选集
        for (int word : wordsContext.getVector().keySet()) {
            Collections.addAll(alterSet, chatBank.wordAnswerIndex[word]);
        }

        if (showStep) {
            System.out.print("候选集大小：");
            System.out.println(alterSet.size());
        }

        //计算该向量与备选集中向量的夹角余弦最大值
        WordFrequencyVectorDouble bankVector = new WordFrequencyVectorDouble();
        double max = 0;
        PairInt maxPosition = null;
        for (PairInt pairInt : alterSet) {
            bankVector.clear();
            bankVector.mergeAll(chatBank.getChat(pairInt.a).getChatPair(pairInt.b).getSentences());
            double cosine = wordsContext.calculateCosine(bankVector);
            //与chatPairContext相同的句子将具有更高的权值：
            if (pairInt.a == chatPairContext.a && pairInt.b != chatPairContext.b) {
                cosine *= 5;
            }
            if (cosine > max) {
                maxPosition = pairInt;
                max = cosine;
            }
        }

        //获得答句
        String answer;
        if (maxPosition == null) {
            answer = "{.Random:chat_mismatch}";
        }
        else {
            chatPairContext.a = maxPosition.a;
            chatPairContext.b = maxPosition.b;
            answer = chatBank.getChat(maxPosition.a).getChatPair(maxPosition.b).getAnswer();
        }
        if (showStep) {
            System.out.print("检索到的答句：");
            System.out.println(answer);
            System.out.println();
        }

        //答句转义
        String escapedAnswer = answer;
        do {
            answer = escapedAnswer;
            escapedAnswer = escapeAnswer(answer, null);
        } while (!escapedAnswer.equals(answer));

        return answer;
    }
}