package com.davidsoft.natural.chinese;

import java.io.IOException;

/**
 * 中文分句器
 */

public class SentenceSegmenter {

    /**
     * 句子停顿类型枚举
     */
    public enum ContextType {
        HALF_STOP,      //以逗号结束
        FULL_STOP,      //以句号结束
        SERIES_STOP,    //以顿号结束
        QUESTION,       //以问号结束
        EXCLAMATION,    //以叹号结束
        ABBREVIATION,   //以省略号结束
        COLON,          //以冒号结束
        SEMICOLON,      //以分号结束
        PARAGRAPH,      //一段结束(换行符)
        IN_BRACKET,     //括号中的内容。注：括号中的内容不予处理，原封输出，如也需要处理，则需递归调用分句。
        IN_QUOTE        //引号中的内容。注：引号中的内容不予处理，原封输出，如也需要处理，则需递归调用分句。
    }

    private FormattedReader reader;
    private ContextType lastContextType;
    private ContextType previousAnalyse;

    /**
     * 创建一个分句器。
     *
     * @param reader 一个正规化输入装饰流
     */
    public SentenceSegmenter(FormattedReader reader) {
        this.reader = reader;
        lastContextType = null;
        previousAnalyse = null;
    }

    /**
     * 获得下一个句子。
     *
     * @return 下一个句子，{@code null}表示已继续无法读取。
     */
    public String nextSentence() {
        try {
            return nextSentenceInner();
        }
        catch (IOException ignored) {
            return null;
        }
    }

    private String nextSentenceInner() throws IOException {
        int character;
        Utils.CharacterType characterType;
        StringBuilder builder;

        //判断上一状态
        if (previousAnalyse != null) {
            builder = new StringBuilder();
            switch (previousAnalyse) {
                case IN_BRACKET:
                    while ((character = reader.read()) >= 0) {
                        if (character == ')') {
                            break;
                        }
                        builder.append((char)character);
                    }
                    lastContextType = ContextType.IN_BRACKET;
                    previousAnalyse = null;
                    return builder.toString();
                case IN_QUOTE:
                    while ((character = reader.read()) >= 0) {
                        if (character == '\"') {
                            break;
                        }
                        builder.append((char)character);
                    }
                    lastContextType = ContextType.IN_QUOTE;
                    previousAnalyse = null;
                    return builder.toString();
            }
        }

        //吃掉空白符、控制字符和特殊符号
        do {
            character = reader.read();
            characterType = reader.getLastCharacterType();
        } while ((characterType == Utils.CharacterType.SPLITTER || characterType == Utils.CharacterType.CONTROL || characterType == Utils.CharacterType.SPECIAL_SYMBOL));
        if (characterType == null) {
            lastContextType = null;
            return null;
        }
        /*
         * 复制字符
         * 1.遇见控制字符、分隔符或达到流末尾则断全句
         * 2.遇见符号则根据符号判断断句
         * 3.遇见特殊符号则吃掉
         * 4.其他情况复制字符
         */
        builder = new StringBuilder();
        while (true) {
            if (characterType == null) {
                lastContextType = ContextType.FULL_STOP;
            }
            else {
                switch (characterType) {
                    case CONTROL:
                        lastContextType = ContextType.FULL_STOP;
                        break;
                    case SPLITTER:
                        switch (character) {
                            case ' ':
                                builder.append((char)character);
                                character = reader.read();
                                characterType = reader.getLastCharacterType();
                                continue;
                            case '\r':
                                character = reader.read();
                                characterType = reader.getLastCharacterType();
                                continue;
                            case '\n':
                                lastContextType = ContextType.PARAGRAPH;
                                break;
                            default:
                                lastContextType = ContextType.FULL_STOP;
                                break;
                        }
                        break;
                    case SYMBOL:
                        switch (character) {
                            case ',':
                                lastContextType = ContextType.HALF_STOP;
                                break;
                            case '\\':
                                lastContextType = ContextType.SERIES_STOP;
                                break;
                            case '.':
                                lastContextType = ContextType.FULL_STOP;
                                break;
                            case '?':
                                lastContextType = ContextType.QUESTION;
                                break;
                            case '!':
                                lastContextType = ContextType.EXCLAMATION;
                                break;
                            case '^':
                                lastContextType = ContextType.ABBREVIATION;
                                break;
                            case ':':
                                lastContextType = ContextType.COLON;
                                break;
                            case ';':
                                lastContextType = ContextType.SEMICOLON;
                                break;
                            case '(':
                                if (builder.length() == 0) {
                                    while ((character = reader.read()) >= 0) {
                                        if (character == ')') {
                                            break;
                                        }
                                        builder.append((char)character);
                                    }
                                    lastContextType = ContextType.IN_BRACKET;
                                }
                                else {
                                    lastContextType = ContextType.HALF_STOP;
                                    previousAnalyse = ContextType.IN_BRACKET;
                                }
                                break;
                            case '{':
                                builder.append((char)character);
                                while ((character = reader.read()) >= 0) {
                                    builder.append((char)character);
                                    if (character == '}') {
                                        break;
                                    }
                                }
                                character = reader.read();
                                characterType = reader.getLastCharacterType();
                                continue;
                            case '\"':
                                if (builder.length() == 0) {
                                    while ((character = reader.read()) >= 0) {
                                        if (character == '\"') {
                                            break;
                                        }
                                        builder.append((char)character);
                                    }
                                    lastContextType = ContextType.IN_QUOTE;
                                }
                                else {
                                    lastContextType = ContextType.HALF_STOP;
                                    previousAnalyse = ContextType.IN_QUOTE;
                                }
                                break;
                            default:
                                builder.append((char)character);
                                character = reader.read();
                                characterType = reader.getLastCharacterType();
                                continue;
                        }
                        break;
                    case SPECIAL_SYMBOL:
                        character = reader.read();
                        characterType = reader.getLastCharacterType();
                        continue;
                    default:
                        builder.append((char)character);
                        character = reader.read();
                        characterType = reader.getLastCharacterType();
                        continue;
                }
            }
            break;
        }
        return builder.toString();
    }

    /**
     * 获得上一次读取到的句子的停顿类型
     *
     * @return 停顿类型，{@link ContextType}中的值之一。
     */
    public ContextType getLastContextType() {
        return lastContextType;
    }
}
