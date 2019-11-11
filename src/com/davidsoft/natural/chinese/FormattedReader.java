package com.davidsoft.natural.chinese;

import java.io.IOException;
import java.io.Reader;

/**
 * 字符正规化输入装饰流。
 * 负责：1. 全角字符转半角字符，2. 返回刚刚读入的字符的分类。
 */
public class FormattedReader extends Reader {

    private Reader reader;
    private Utils.CharacterType lastCharacterType;

    /**
     * 构造一个字符正规化输入装饰流。
     *
     * @param reader 待装饰的输入流。
     */
    public FormattedReader(Reader reader) {
        this.reader = reader;
        lastCharacterType = null;
    }

    /**
     * 获得刚刚读入的字符的分类。
     *
     * @return 字符分类。
     *
     * @see Utils.CharacterType
     */
    public Utils.CharacterType getLastCharacterType() {
        return lastCharacterType;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int ret = reader.read(cbuf, off, len);
        if (ret > 0) {
            for (int i = 0; i < ret; ++i) {
                switch (cbuf[i + off]) {
                    case '　':
                        cbuf[i + off] = ' ';
                        break;
                    case '‘':
                    case '’':
                        cbuf[i + off] = '\'';
                    case '“':
                    case '”':
                        cbuf[i + off] = '\"';
                        break;
                    case '、':
                    case '﹑':
                    case '﹨':
                        cbuf[i + off] = '\\';
                        break;
                    case '。':
                    case '﹒':
                        cbuf[i + off] = '.';
                        break;
                    case '…':
                        cbuf[i + off] = '^';
                        break;
                    case '—':
                        cbuf[i + off] = '-';
                        break;
                    case '﹦':
                        cbuf[i + off] = '=';
                        break;
                    case '﹟':
                        cbuf[i + off] = '#';
                        break;
                    case '﹠':
                        cbuf[i + off] = '&';
                        break;
                    case '﹡':
                        cbuf[i + off] = '*';
                        break;
                    case '﹢':
                        cbuf[i + off] = '+';
                        break;
                    case '﹣':
                        cbuf[i + off] = '-';
                        break;
                    case '﹐':
                        cbuf[i + off] = ',';
                        break;
                    case '﹔':
                        cbuf[i + off] = ';';
                        break;
                    case '﹕':
                        cbuf[i + off] = ':';
                        break;
                    case '﹖':
                        cbuf[i + off] = '?';
                        break;
                    case '﹗':
                        cbuf[i + off] = '!';
                        break;
                    case '﹪':
                        cbuf[i + off] = '%';
                        break;
                    case '﹩':
                        cbuf[i + off] = '$';
                        break;
                    case '﹫':
                        cbuf[i + off] = '@';
                        break;
                    case '〈':
                    case '《':
                    case '﹤':
                        cbuf[i + off] = '<';
                        break;
                    case '〉':
                    case '》':
                    case '﹥':
                        cbuf[i + off] = '>';
                        break;
                    case '「':
                    case '『':
                    case '【':
                    case '〔':
                    case '〖':
                    case '﹝':
                        cbuf[i + off] = '[';
                        break;
                    case '」':
                    case '』':
                    case '】':
                    case '〕':
                    case '〗':
                    case '﹞':
                        cbuf[i + off] = ']';
                        break;
                    case '﹛':
                        cbuf[i + off] = '{';
                        break;
                    case '﹜':
                        cbuf[i + off] = '}';
                        break;
                    case '﹙':
                        cbuf[i + off] = '(';
                        break;
                    case '﹚':
                        cbuf[i + off] = ')';
                        break;
                    default:
                        if (cbuf[i + off] >= 65281 && cbuf[i + off] <= 65374) {
                            cbuf[i + off] = (char) (cbuf[i + off] - 65248);
                        }
                }
            }
            //CharacterType
            lastCharacterType = Utils.getCharacterType(cbuf[off + ret - 1]);
        }
        else {
            lastCharacterType = null;
        }
        return ret;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
