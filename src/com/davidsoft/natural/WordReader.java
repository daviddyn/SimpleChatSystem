package com.davidsoft.natural;

/**
 * Abstract word reader.
 */
public interface WordReader {

    /**
     * 读入下一个单词，使指针向后移动。
     *
     * @return 接下来的单词。
     */
    String nextWord();

    /**
     * 预览下一个单词，指针不向后移动。
     *
     * @return 接下来的单词。
     */
    String peekWord();

    /**
     * 跳过指定数量的单词。
     *
     * @param wordCount 几个
     * @return 实际上跳过了几个单词。
     */
    int skipWords(int wordCount);

    /**
     * 是否存在下一个单词。
     *
     * @return {@code true} if there are words remain, {@code false} otherwise.
     */
    boolean hasNext();
}
