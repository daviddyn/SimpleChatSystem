package com.davidsoft.natural;

/**
 * Stream-like reader
 * Created by David on 2017/3/8.
 */
public class SimpleWordReader implements WordReader {

    private String[] words;
    private int offset;

    public SimpleWordReader(String[] words) {
        this.words = words;
    }

    @Override
    public String nextWord() {
        if (offset >= words.length) {
            return null;
        }
        return words[offset++];
    }

    @Override
    public String peekWord() {
        if (offset >= words.length) {
            return null;
        }
        return words[offset];
    }

    @Override
    public int skipWords(int wordCount) {
        int ret;
        if (offset + wordCount < words.length) {
            offset += wordCount;
            ret = wordCount;
        }
        else {
            ret = words.length - offset;
            offset = words.length;
        }
        return ret;
    }

    @Override
    public boolean hasNext() {
        return offset < words.length;
    }

}
