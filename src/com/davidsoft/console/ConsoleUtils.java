package com.davidsoft.console;

import java.io.File;
import java.util.Scanner;

public final class ConsoleUtils {

    public static Integer inputOptions(String[] options, Scanner scanner) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d - %s", i + 1, options[i]);
            System.out.println();
        }
        while (true) {
            System.out.print("请输入选择序号：");
            String input = scanner.nextLine();
            if (input.length() == 0) {
                return null;
            }
            int inputNumber;
            try {
                inputNumber = Integer.parseInt(input);
            }
            catch (NumberFormatException ignored) {
                continue;
            }
            if (inputNumber < 1 || inputNumber > options.length) {
                continue;
            }
            return inputNumber - 1;
        }
    }

    public static boolean inputYesNo(String tip, Scanner scanner) {
        System.out.print(tip);
        return scanner.nextLine().toLowerCase().equals("y");
    }

    //返回null说明执行返回操作
    public static String inputDirectory(String tip, Scanner scanner) {
        String input;
        while (true) {
            System.out.print(tip);
            input = scanner.nextLine();
            if (input.length() == 0) {
                return null;
            }
            File file = new File(input);
            if (!file.exists()) {
                if (inputYesNo(input + "不存在，要创建它吗(Y/N)？", scanner)) {
                    if (!file.mkdirs()) {
                        System.out.println("无法创建此路径。");
                        System.out.println();
                        continue;
                    }
                }
                else {
                    System.out.println();
                    continue;
                }
            }
            else if (!file.isDirectory()) {
                System.out.println("此路径不是文件夹。");
                System.out.println();
                continue;
            }
            return file.getAbsolutePath();
        }
    }

    //返回null说明执行返回操作
    public static String inputFileForSave(String tip, Scanner scanner) {
        String input;
        while (true) {
            System.out.print(tip);
            input = scanner.nextLine();
            if (input.length() == 0) {
                return null;
            }
            if (input.endsWith(File.separator)) {
                System.out.println(input + "不是一个有效的文件路径。");
                continue;
            }
            int findPos = input.lastIndexOf(File.separator);
            String fileName = input.substring(findPos + 1);
            if (fileName.contains("\\") || fileName.contains("/") || fileName.contains(":") || fileName.contains("*") || fileName.contains("?") || fileName.contains("\"") || fileName.contains("<") || fileName.contains(">") || fileName.contains("|")) {
                System.out.println("文件名不能包含 \\ / : * ? \" < > | 。");
                continue;
            }
            File wholeFile = new File(input).getAbsoluteFile();
            File directoryFile = new File(findPos < 0 ? "" : input.substring(0, findPos)).getAbsoluteFile();
            if (wholeFile.exists()) {
                if (wholeFile.isFile()) {
                    if (inputYesNo(wholeFile.getAbsolutePath() + " 已存在，要替换它吗(Y/N)？", scanner)) {
                        return wholeFile.getAbsolutePath();
                    }
                }
                else {
                    System.out.println(wholeFile.getAbsolutePath() + " 已存在，且不可被覆盖。");
                }
            }
            else {
                if (directoryFile.exists()) {
                    if (directoryFile.isDirectory()) {
                        return wholeFile.getAbsolutePath();
                    }
                    else {
                        System.out.println("无法在 " + directoryFile.getAbsolutePath() + " 中创建文件。");
                    }
                }
                else {
                    if (inputYesNo(directoryFile.getAbsolutePath() + " 不存在，要创建它吗(Y/N)？", scanner)) {
                        if (!directoryFile.mkdirs()) {
                            System.out.println("无法创建此路径。");
                        }
                        else {
                            return wholeFile.getAbsolutePath();
                        }
                    }
                }
            }
        }
    }

    //返回null说明执行返回操作
    public static String inputFileForOpen(String tip, Scanner scanner) {
        String input;
        while (true) {
            System.out.print(tip);
            input = scanner.nextLine();
            if (input.length() == 0) {
                return null;
            }
            if (input.endsWith(File.separator)) {
                System.out.println(input + "不是一个有效的文件路径。");
                continue;
            }
            File file = new File(input).getAbsoluteFile();
            if (file.exists() || file.isFile()) {
                return file.getAbsolutePath();
            }
            else {
                System.out.println(file.getAbsolutePath() + "不可用。");
            }
        }
    }

    //返回null说明执行返回操作
    public static Integer inputInteger(String tip, Scanner scanner) {
        String input;
        while (true) {
            System.out.print(tip);
            input = scanner.nextLine();
            if (input.length() == 0) {
                return null;
            }
            try {
                return Integer.parseInt(input);
            }
            catch (NumberFormatException ignored) { }
        }
    }

    public static String makePath(String directory) {
        if (directory.endsWith(File.separator)) {
            return directory;
        }
        return directory + File.separator;
    }
}