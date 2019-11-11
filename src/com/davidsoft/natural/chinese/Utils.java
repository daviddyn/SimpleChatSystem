package com.davidsoft.natural.chinese;

import java.util.ArrayList;

public final class Utils {

    /**
     * 字符分类
     */
    public enum CharacterType {
        CONTROL,        //控制字符(如\0等)
        SPLITTER,       //空白分隔符(如空格、\t、\n、\r等)
        SYMBOL,         //常见的符号(如逗号、句号、各种括号引号等)
        NUMBER,         //数字0-9
        UPPER_CASE,     //大写字母A-Z
        LOWER_CASE,     //小写字母a-z
        CHINESE,        //中文汉字
        SPECIAL_SYMBOL  //除上述之外的字符(统称为“特殊符号”)
    }

    /**
     * 获得字符的类型。
     *
     * @param c 字符
     * @return 字符类型。为枚举{@link CharacterType}中的值之一。
     */
    public static CharacterType getCharacterType(char c) {
        if (c <= 8 || 14 <= c && c <= 31 || c == 127) {
            return CharacterType.CONTROL;
        }
        else if (9 <= c && c <= 13 || c == 32) {
            return CharacterType.SPLITTER;
        }
        else if (33 <= c && c <= 47 || 58 <= c && c <= 64 || 91 <= c && c <= 96 || 123 <= c && c <= 126) {
            return CharacterType.SYMBOL;
        }
        else if (48 <= c && c <= 57) {
            return CharacterType.NUMBER;
        }
        else if (65 <= c && c <= 90) {
            return CharacterType.UPPER_CASE;
        }
        else if (97 <= c && c <= 122) {
            return CharacterType.LOWER_CASE;
        }
        else if (13312 <= c && c <= 40917) {
            return CharacterType.CHINESE;
        }
        else {
            return CharacterType.SPECIAL_SYMBOL;
        }
    }

    /**
     * 将一个字符串切分成若干单元以便于分词。规则：
     * 1. 一个中文汉字字符自成一个单元；
     * 2. 连续的空白分隔符是拆分的标志；
     * 3. 连续的除中文汉字、空白分隔符外的字符成一个单元。
     * 分词算法将在单元上进行。
     *
     * @param sentence 原始字符串
     * @return 切分后的字符串组。
     */
    public static String[] partDivide(String sentence) {
        int i = 0, j = 0;
        ArrayList<String> partsBuilder = new ArrayList<>();
        boolean englishState = false;
        while (i < sentence.length()) {
            if (englishState) {
                switch (getCharacterType(sentence.charAt(i))) {
                    case CHINESE:
                    case SPLITTER:
                        partsBuilder.add(sentence.substring(j, i));
                        englishState = false;
                        break;
                    default:
                        //continue scan
                        ++i;
                        break;
                }
            }
            else {
                switch (getCharacterType(sentence.charAt(i))) {
                    case CHINESE:
                        partsBuilder.add(sentence.substring(i, i + 1));
                        ++i;
                        break;
                    case SPLITTER:
                        //eat it
                        ++i;
                        break;
                    default:
                        englishState = true;
                        j = i++;
                        break;
                }
            }
        }
        if (englishState) {
            partsBuilder.add(sentence.substring(j, i));
        }
        String[] parts = new String[partsBuilder.size()];
        partsBuilder.toArray(parts);
        return parts;
    }

    /**
     * 将若干个单元组重新拼接成句子。基本拼接原则：
     * 1. 若单元的内容是中文汉字，则直接拼接
     * 2. 否则，拼接时中间添加空格
     *
     * @param parts 单元数组
     * @param begin 从数组的指定起始位置处理
     * @param end   一直处理到数组的(指定位置-1)结束。
     * @return 拼接后的句子。
     */
    public static String combineParts(String[] parts, int begin, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = begin; i < end; ++i) {
            if (getCharacterType(parts[i].charAt(0)) == CharacterType.CHINESE) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ' ') {
                    builder.deleteCharAt(builder.length() - 1);
                }
                builder.append(parts[i]);
            }
            else {
                builder.append(parts[i]).append(" ");
            }
        }
        return builder.toString().trim();
    }

    /**
     * 判断一个字符串中是否全部由数字组成。
     *
     * @param word 待检测字符串。
     * @return {@code true} if 所有字符都是数字，{@code false} otherwise.
     */
    public static boolean isNumeric(String word) {
        for (int i = 0; i < word.length(); ++i) {
            if (word.charAt(i) < 48 || word.charAt(i) > 57) {
                return false;
            }
        }
        return true;
    }
}
