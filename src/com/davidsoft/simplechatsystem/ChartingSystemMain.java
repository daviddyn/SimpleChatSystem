package com.davidsoft.simplechatsystem;

import com.davidsoft.natural.chinese.ChattingSystem;

import java.io.StringReader;
import java.util.Scanner;

/**
 * 对话系统主类。运行此类直接进入对话系统。
 */
public final class ChartingSystemMain {

    public static void main(String[] args) {
        System.out.println();
        ChattingSystem chattingSystem = new ChattingSystem();
        Scanner scanner = new Scanner(System.in);
        System.out.println("对话系统启动成功！现在可以扯淡了。");
        while (true) {
            String line = scanner.nextLine();
            if ("再见".equals(line)) {
                break;
            }
            System.out.println(chattingSystem.getAnswer(new StringReader(line), true));
        }
    }
}