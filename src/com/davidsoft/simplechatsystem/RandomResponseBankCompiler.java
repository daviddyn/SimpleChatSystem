package com.davidsoft.simplechatsystem;

import com.davidsoft.console.ConsoleUtils;
import com.davidsoft.natural.chinese.ChatBank;
import com.davidsoft.natural.chinese.RandomResponseBank;
import com.davidsoft.natural.chinese.SynonymBank;
import com.davidsoft.natural.chinese.WordBank;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * 随机回应库编译器主类。运行此类直接进入编译器。
 */
public final class RandomResponseBankCompiler {

    public static void main(String[] args) {
        System.out.println();
        File sourceDirectory = null;
        String outputFile = null;

        if (args.length >= 1) {
            sourceDirectory = new File(args[1]);
        }
        if (args.length >= 2) {
            outputFile = args[2];
        }

        Scanner scanner = new Scanner(System.in);

        //输入源文件路径
        while (sourceDirectory == null) {
            String input = ConsoleUtils.inputDirectory("输入源文件所在的文件夹：", scanner);
            if (input != null) {
                sourceDirectory = new File(input);
            }
        }

        //拿出所有源文件
        File[] sourceFiles = sourceDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (sourceFiles == null) {
            System.out.println("错误：无法访问 " + sourceDirectory.getAbsolutePath() + "。");
            return;
        }

        //确认源文件
        System.out.println("即将处理：");
        for (File sourceFile : sourceFiles) {
            System.out.println(sourceFile.getAbsolutePath());
        }
        if (!ConsoleUtils.inputYesNo("确定吗(Y/N)？", scanner)) {
            return;
        }

        while (outputFile == null) {
            outputFile = ConsoleUtils.inputFileForSave("输入目标文件名：", scanner);
        }

        //打开目标文件
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(outputFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误：无法写入 " + outputFile + "。");
            return;
        }

        System.out.println("正在处理...");

        //开始处理
        try {
            RandomResponseBank.compileSourceFiles(sourceFiles, fileOut);
            System.out.println();
            System.out.println("全部完成！");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误：无法写入 " + outputFile + "。");
        }

        try {
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
