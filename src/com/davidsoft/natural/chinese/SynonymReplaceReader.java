package com.davidsoft.natural.chinese;

import com.davidsoft.natural.WordReader;

/**
 * 同义词替换机输入装饰流。
 */
public class SynonymReplaceReader implements WordReader {

    private SynonymBank synonymBank;
    private WordReader sourceReader;

    /**
     * 同义词替换机输入装饰流。
     *
     * @param sourceReader 待装饰的输入流
     * @param synonymBank  同义词库
     */
    public SynonymReplaceReader(WordReader sourceReader, SynonymBank synonymBank) {
        this.synonymBank = synonymBank;
        this.sourceReader = sourceReader;
    }

    private String replace(String word) {
        if  (synonymBank.isSpecialWord(word)) {
            return word;
        }
        else {
            int position = synonymBank.getEscapedWordPosition(word);
            if (position >= 0) {
                return synonymBank.getEscapedWord(position);
            }
            else {
                return word;
            }
        }
    }

    @Override
    public String nextWord() {
        return replace(sourceReader.nextWord());
    }

    @Override
    public String peekWord() {
        return replace(sourceReader.peekWord());
    }

    @Override
    public int skipWords(int wordCount) {
        return sourceReader.skipWords(wordCount);
    }

    @Override
    public boolean hasNext() {
        return sourceReader.hasNext();
    }
}
