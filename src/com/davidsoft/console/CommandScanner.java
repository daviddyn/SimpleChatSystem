package com.davidsoft.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * 命令行解析器
 * Created by David on 2017/1/8.
 */
public final class CommandScanner {

    private BufferedReader reader;
    private String command;
    private String[] args;

    public CommandScanner(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    public boolean scanCommand() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            command = null;
            return true;
        }
        int i;
        for (i = 0; i < line.length(); ++i) {
            if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
                break;
            }
        }
        command = line.substring(0, i);
        ArrayList<String> argList = new ArrayList<>();
        int j;
        while (i < line.length()) {
            //吃空格
            for (++i; i < line.length(); ++i) {
                if (line.charAt(i) != ' ' && line.charAt(i) != '\t') {
                    break;
                }
            }
            if (i == line.length()) {
                break;
            }
            //处理参数
            if (line.charAt(i) == '\"') {
                j = i + 1;
                for (i = j; i < line.length(); ++i) {
                    if (line.charAt(i) == '\"') {
                        break;
                    }
                }
                if (i == line.length()) {
                    return false;
                }
            }
            else {
                j = i;
                for (++i; i < line.length(); ++i) {
                    if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
                        break;
                    }
                }
            }
            argList.add(line.substring(j, i));
        }
        args = new String[argList.size()];
        argList.toArray(args);
        return true;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

}