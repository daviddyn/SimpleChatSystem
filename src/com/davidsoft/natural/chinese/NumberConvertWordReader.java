package com.davidsoft.natural.chinese;

import com.davidsoft.natural.WordReader;

import java.util.HashMap;
import java.util.Stack;

/**
 * 中文数字转阿拉伯数字的输入装饰流。
 */
public class NumberConvertWordReader implements WordReader {

    private static class NumberBuilder {

        private static long intPow(int a, int b) {
            long ret = 1;
            for (int i = 0; i < b; ++i) {
                ret *= a;
            }
            return ret;
        }

        long building;
        private Stack<Long> history;

        public NumberBuilder() {
            history = new Stack<>();
            history.push(0L);
        }

        public void addNumber(int number) {
            history.push(history.pop() + building);
            building = number;
        }

        public void addUnit(int unit) {
            switch (unit) {
                case 14:
                    history.push(history.pop() + building);
                    history.push(history.pop() * 100000000);
                    history.push(0L);
                    building = 0;
                    break;
                case 13:
                    history.push(history.pop() + building);
                    history.push(history.pop() * 10000);
                    history.push(0L);
                    building = 0;
                    break;
                default:
                    building *= intPow(10, unit - 9);
            }
        }

        private void sumUp() {
            long sum = building;
            for (long number : history) {
                sum += number;
            }
            history.clear();
            history.push(sum);
            building = 0;
        }

        public void roll() {
            sumUp();
            history.push(history.pop() * 10);
        }

        public long getNumber() {
            sumUp();
            return history.get(0);
        }
    }

    private static HashMap<String, Integer> TYPE_MAP;

    static {
        TYPE_MAP = new HashMap<>();
        TYPE_MAP.put("零", 0);
        TYPE_MAP.put("〇", 0);
        TYPE_MAP.put("一", 1);
        TYPE_MAP.put("壹", 1);
        TYPE_MAP.put("二", 2);
        TYPE_MAP.put("贰", 2);
        TYPE_MAP.put("三", 3);
        TYPE_MAP.put("叁", 3);
        TYPE_MAP.put("四", 4);
        TYPE_MAP.put("肆", 4);
        TYPE_MAP.put("五", 5);
        TYPE_MAP.put("伍", 5);
        TYPE_MAP.put("六", 6);
        TYPE_MAP.put("陆", 6);
        TYPE_MAP.put("七", 7);
        TYPE_MAP.put("柒", 7);
        TYPE_MAP.put("八", 8);
        TYPE_MAP.put("捌", 8);
        TYPE_MAP.put("九", 9);
        TYPE_MAP.put("玖", 9);
        TYPE_MAP.put("十", 10);
        TYPE_MAP.put("拾", 10);
        TYPE_MAP.put("百", 11);
        TYPE_MAP.put("佰", 11);
        TYPE_MAP.put("千", 12);
        TYPE_MAP.put("仟", 12);
        TYPE_MAP.put("万", 13);
        TYPE_MAP.put("亿", 14);
    }

    private static final int TYPE_NONE = 0;
    private static final int TYPE_ZERO = 1;
    private static final int TYPE_NUMBER = 2;
    private static final int TYPE_SUNIT = 3;
    private static final int TYPE_LUNIT = 4;

    private static int getWordCode(String word) {
        Integer code = TYPE_MAP.get(word);
        if (code == null) {
            return -1;
        }
        else {
            return code;
        }
    }

    private static int getWordType(int wordCode) {
        if (wordCode < 0) {
            return TYPE_NONE;
        }
        if (wordCode == 0) {
            return TYPE_ZERO;
        }
        if (wordCode < 10) {
            return TYPE_NUMBER;
        }
        if (wordCode < 13) {
            return TYPE_SUNIT;
        }
        return TYPE_LUNIT;
    }

    private WordReader sourceReader;

    /**
     * 构造一个中文数字转阿拉伯数字的输入装饰流。
     *
     * @param sourceReader 待装饰的输入流。
     */
    public NumberConvertWordReader(WordReader sourceReader) {
        this.sourceReader = sourceReader;
    }

    @Override
    public String nextWord() {
        int stateNumber = 0;
        String word;
        int wordCode;
        NumberBuilder builder = null;
        int currentLUnitLimit = 14;
        int currentSUnitLimit = 12;

        while (sourceReader.hasNext()) {
            switch (stateNumber) {
                case 0:
                    word = sourceReader.nextWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_NUMBER:
                            builder = new NumberBuilder();
                            builder.addNumber(wordCode);
                            stateNumber = 1;
                            break;
                        case TYPE_ZERO:
                            builder = new NumberBuilder();
                            stateNumber = 9;
                            break;
                        default:
                            if (word.equals("十")) {
                                builder = new NumberBuilder();
                                builder.addNumber(10);
                                stateNumber = 2;
                            }
                            else {
                                return word;
                            }
                    }
                    break;
                case 1:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_SUNIT:
                            sourceReader.nextWord();
                            builder.addUnit(wordCode);
                            currentSUnitLimit = wordCode - 1;
                            stateNumber = 2;
                            break;
                        case TYPE_LUNIT:
                            sourceReader.nextWord();
                            builder.addUnit(wordCode);
                            currentLUnitLimit = wordCode - 1;
                            stateNumber = 5;
                            break;
                        case TYPE_ZERO:
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.roll();
                            builder.addNumber(wordCode);
                            stateNumber = 7;
                            break;
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                case 2:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_ZERO:
                            sourceReader.nextWord();
                            --currentSUnitLimit;
                            stateNumber = 3;
                            break;
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.addNumber(wordCode);
                            stateNumber = 4;
                            break;
                        case TYPE_LUNIT:
                            if (wordCode <= currentLUnitLimit) {
                                sourceReader.nextWord();
                                builder.addUnit(wordCode);
                                currentSUnitLimit = 12;
                                currentLUnitLimit = wordCode - 1;
                                stateNumber = 5;
                                break;
                            }
                            else {
                                return String.valueOf(builder.getNumber());
                            }
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                case 3:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.addNumber(wordCode);
                            stateNumber = 4;
                            break;
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                case 4:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_SUNIT:
                            if (wordCode <= currentSUnitLimit) {
                                sourceReader.nextWord();
                                builder.addUnit(wordCode);
                                currentSUnitLimit = wordCode - 1;
                                stateNumber = 2;
                                break;
                            }
                            else {
                                return String.valueOf(builder.getNumber());
                            }
                        case TYPE_LUNIT:
                            if (wordCode <= currentLUnitLimit) {
                                sourceReader.nextWord();
                                builder.addUnit(wordCode);
                                currentLUnitLimit = wordCode - 1;
                                currentSUnitLimit = 12;
                                stateNumber = 5;
                                break;
                            }
                            else {
                                return String.valueOf(builder.getNumber());
                            }
                        default:
                            if (currentSUnitLimit >= 10) {
                                builder.addUnit(currentSUnitLimit);
                            }
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                case 5:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.addNumber(wordCode);
                            stateNumber = 4;
                            break;
                        case TYPE_ZERO:
                            sourceReader.nextWord();
                            stateNumber = 3;
                            break;
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                /*
                case 6:
                    break;
                */
                case 7:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_ZERO:
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.roll();
                            builder.addNumber(wordCode);
                            stateNumber = 8;
                            break;
                        default:
                            if (word.equals("年")) {
                                long number = builder.getNumber();
                                return (number < 10 ? "190" : "19") + number;
                            }
                            else {
                                return String.valueOf(builder.getNumber());
                            }
                    }
                    break;
                case 8:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_ZERO:
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.roll();
                            builder.addNumber(wordCode);
                            break;
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                case 9:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_ZERO:
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.roll();
                            builder.addNumber(wordCode);
                            stateNumber = 10;
                            break;
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
                case 10:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_ZERO:
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.roll();
                            builder.addNumber(wordCode);
                            stateNumber = 11;
                            break;
                        default:
                            if (word.equals("年")) {
                                long number = builder.getNumber();
                                return (number < 10 ? "200" : "20") + number;
                            }
                            else {
                                return String.valueOf(builder.getNumber());
                            }
                    }
                    break;
                case 11:
                    word = sourceReader.peekWord();
                    wordCode = getWordCode(word);
                    switch (getWordType(wordCode)) {
                        case TYPE_ZERO:
                        case TYPE_NUMBER:
                            sourceReader.nextWord();
                            builder.roll();
                            builder.addNumber(wordCode);
                            stateNumber = 11;
                            break;
                        default:
                            return String.valueOf(builder.getNumber());
                    }
                    break;
            }
        }
        if (builder != null) {
            if (stateNumber == 4 && currentSUnitLimit >= 10) {
                builder.addUnit(currentSUnitLimit);
            }
            return String.valueOf(builder.getNumber());
        }
        else {
            return null;
        }
    }

    @Override
    public String peekWord() {
        return null;
    }

    @Override
    public int skipWords(int wordCount) {
        int ret = 0;
        for (int i = 0; i < wordCount; ++i) {
            if (nextWord() != null) {
                ++ret;
            }
        }
        return ret;
    }

    @Override
    public boolean hasNext() {
        return sourceReader.hasNext();
    }
}