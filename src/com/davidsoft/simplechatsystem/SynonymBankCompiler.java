package com.davidsoft.simplechatsystem;

import com.davidsoft.console.ConsoleUtils;
import com.davidsoft.natural.chinese.SynonymBank;
import com.davidsoft.natural.chinese.WordBank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * 同义词库编译器主类。运行此类直接进入编译器。
 */
public final class SynonymBankCompiler {

    public static void main(String[] args) {
        System.out.println();
        String thesaurusSrcFile = null;
        String specialSrcFile = null;
        String wordBankPath = null;
        String outputFile = null;

        if (args.length >= 1) {
            thesaurusSrcFile = args[1];
        }
        if (args.length >= 2) {
            specialSrcFile = args[2];
        }
        if (args.length >= 3) {
            wordBankPath = args[3];
        }
        if (args.length >= 4) {
            outputFile = args[4];
        }

        Scanner scanner = new Scanner(System.in);

        while (thesaurusSrcFile == null) {
            thesaurusSrcFile = ConsoleUtils.inputFileForOpen("输入同义词库源文件：", scanner);
        }
        while (specialSrcFile == null) {
            specialSrcFile = ConsoleUtils.inputFileForOpen("输入特殊词库源文件：", scanner);
        }
        while (wordBankPath == null) {
            wordBankPath = ConsoleUtils.inputFileForOpen("输入编译所需的中文词库：", scanner);
        }
        while (outputFile == null) {
            outputFile = ConsoleUtils.inputFileForSave("输入目标文件名：", scanner);
        }

        //打开中文词库
        System.out.println();
        System.out.print("正在打开词库…");
        WordBank wordBank;
        try {
            wordBank = new WordBank(new File(wordBankPath));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误：无法读取中文词库文件。");
            return;
        }

        System.out.println("完成.");
        System.out.print("正在编译…");

        FileOutputStream out;
        boolean success = false;
        try {
            out = new FileOutputStream(outputFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("错误：无法写入目标文件。");
            return;
        }
        try {
            SynonymBank.compileSourceFile(new File(thesaurusSrcFile), new File(specialSrcFile), wordBank, out);
            success = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误：写入目标文件时出现错误。");
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (success) {
            System.out.println("完成.");
            System.out.println();
            System.out.println("全部完成！");
        }
    }

}