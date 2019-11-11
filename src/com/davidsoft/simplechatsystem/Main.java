package com.davidsoft.simplechatsystem;

import com.davidsoft.console.ConsoleUtils;
import com.davidsoft.natural.chinese.WordBankManager;

import java.util.Scanner;

/**
 * 主类。运行此类可以以目录交互的形式进入每一个子程序。
 */
public final class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Integer inputOption = null;
            while (inputOption == null) {
                inputOption = ConsoleUtils.inputOptions(new String[] {
                        "打开词典管理器",
                        "打开同义词编译器",
                        "打开对话样本编译器",
                        "打开随机回应编译器",
                        "进入对话系统",
                        "退出"
                }, scanner);
            }
            switch (inputOption) {
                case 0:
                    WordBankManager.main(args);
                    System.out.println();
                    System.out.println("词典管理器已退出");
                    System.out.println();
                    break;
                case 1:
                    SynonymBankCompiler.main(args);
                    System.out.println();
                    System.out.println("同义词编译器已退出");
                    System.out.println();
                    break;
                case 2:
                    ChatBankCompiler.main(args);
                    System.out.println();
                    System.out.println("对话样本编译器已退出");
                    System.out.println();
                    break;
                case 3:
                    RandomResponseBankCompiler.main(args);
                    System.out.println();
                    System.out.println("随机回应编译器已退出");
                    System.out.println();
                    break;
                case 4:
                    ChartingSystemMain.main(args);
                    System.out.println();
                    System.out.println("对话系统已退出");
                    System.out.println();
                    break;
                case 5:
                    System.out.println();
                    return;
            }
        }
    }
}
