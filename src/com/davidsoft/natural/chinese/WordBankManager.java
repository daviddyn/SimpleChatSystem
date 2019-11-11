package com.davidsoft.natural.chinese;

import com.davidsoft.console.CommandScanner;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 汉语词典管理器，可运行。
 */
public final class WordBankManager {

    /**
     * Commands
     * List: List all the loaded word banks.
     * LoadSource sourceFilePath: Load a word bank form a source file.
     * Load filePath: Load a word bank form a compiled file.
     * Create: Create a blank word bank.
     * Save bankNumber filePath: Save a loaded word bank to a compiled file.
     * SaveSource bankNumber: Save a loaded word bank to a source file.
     * Merge bankNumber1 bankNumber2: Merge words in bankNumber2 to bankNumber1.
     * Set bankNumber word [frequency] [partOfSpeech]: Add or update a word in bankNumber.
     * Add bankNumber word [frequency] [partOfSpeech]: Add a word in bankNumber if not exist.
     * Get bankNumber word: Get a word in bankNumber.
     * Trace bankNumber word: Get a word in bankNumber with trace.
     * Delete bankNumber word: Delete a word in bankNumber.
     * Exit: Exit.
     */

    private static ArrayList<WordBank> wordBanks;

    private static void bat(String[] args) {
        if (args.length < 1) {
            System.out.println("too few arguments.");
            return;
        }
        String encoding = "GBK";
        if (args.length > 1) {
            encoding = args[1];
        }
        CommandScanner scanner;
        try {
            scanner = new CommandScanner(new InputStreamReader(new FileInputStream(args[0]), encoding));
        }
        catch (FileNotFoundException e) {
            System.out.println("bad file path.");
            return;
        }
        catch (UnsupportedEncodingException e) {
            System.out.println("unknown encoding mark.");
            return;
        }
        commandLoop(scanner, false, false);
    }

    private static void list() {
        if (wordBanks.size() == 0) {
            System.out.println("no loaded bank.");
        }
        else {
            int i = 0;
            for (WordBank bank : wordBanks) {
                System.out.printf(" - bank %2d : %d word(s).", i++, bank.getWordCount());
                System.out.println();
            }
        }
    }

    private static void loadSource(String[] args) {
        if (args.length < 1) {
            System.out.println("too few arguments.");
            return;
        }
        Scanner scanner;
        try {
            scanner = new Scanner(new FileInputStream(args[0]), "GBK");
        }
        catch (FileNotFoundException e) {
            System.out.println("bad file path.");
            return;
        }
        String word;
        WordBank.PartOfSpeechType partOfSpeech;
        double frequency;
        WordBank bank = new WordBank();
        int i = 0;
        System.out.print("loading...");
        try {
            while (scanner.hasNext()) {
                word = scanner.next();
                ++i;
                partOfSpeech = WordBank.PartOfSpeechType.parseType(scanner.next());
                frequency = Double.parseDouble(scanner.next());
                bank.set(Utils.partDivide(word), partOfSpeech, frequency);
            }
        }
        catch (NumberFormatException e) {
            scanner.close();
            System.out.println();
            System.out.print("bad frequency argument at the record NO.");
            System.out.print(i);
            System.out.println(" .");
            return;
        }
        wordBanks.add(bank);
        scanner.close();
        System.out.println();
        System.out.print("bank loaded with the number ");
        System.out.print(wordBanks.size() - 1);
        System.out.println(" .");
    }

    private static void load(String[] args) {
        if (args.length < 1) {
            System.out.println("too few arguments.");
            return;
        }
        System.out.print("loading...");
        try {
            WordBank bank = new WordBank(new File(args[0]));
            wordBanks.add(bank);
            System.out.println();
            System.out.print("bank loaded with the number ");
            System.out.print(wordBanks.size() - 1);
            System.out.println(" .");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("open failed.");
        }
    }

    private static void create() {
        wordBanks.add(new WordBank());
        System.out.print("bank created with the number ");
        System.out.print(wordBanks.size() - 1);
        System.out.println(" .");
    }

    private static void save(String[] args) {
        if (args.length < 2) {
            System.out.println("too few arguments.");
            return;
        }
        int bankNumber;
        try {
            bankNumber = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        if (bankNumber >= wordBanks.size()) {
            System.out.println("bankNumber out of range.");
            return;
        }
        try (FileOutputStream fileOut = new FileOutputStream(args[1])) {
            System.out.print("saving...");
            wordBanks.get(bankNumber).save(fileOut);
            System.out.println();
            System.out.println("bank saved.");
        } catch (FileNotFoundException e) {
            System.out.println("bad file path.");
        } catch (IOException e) {
            System.out.println();
            System.out.println("write file error.");
        }
        //eat it.
    }

    private static void merge(String[] args) {
        int bankNumber1, bankNumber2;
        try {
            bankNumber1 = Integer.parseInt(args[0]);
            bankNumber2 = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("too few arguments.");
            return;
        }
        if (bankNumber1 >= wordBanks.size()) {
            System.out.println("bankNumber1 out of range.");
            return;
        }
        if (bankNumber2 >= wordBanks.size()) {
            System.out.println("bankNumber1 out of range.");
            return;
        }
        if (bankNumber1 == bankNumber2) {
            System.out.println("merging the same bank.");
        }
    }

    private static void set(String[] args) {
        int bankNumber;
        WordBank.PartOfSpeechType partOfSpeech = null;
        double frequency = -1;
        try {
            if (args.length < 2) {
                System.out.println("too few arguments.");
                return;
            }
            bankNumber = Integer.parseInt(args[0]);
            if (args.length > 2) {
                partOfSpeech = WordBank.PartOfSpeechType.parseType(args[2]);
                if (partOfSpeech == null) {
                    frequency = Double.parseDouble(args[2]);
                    if (args.length > 3) {
                        partOfSpeech = WordBank.PartOfSpeechType.parseType(args[3]);
                        if (partOfSpeech == null) {
                            System.out.println("bad argument(s).");
                            return;
                        }
                    }
                    else {
                        System.out.println("too few arguments.");
                        return;
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        if (bankNumber >= wordBanks.size()) {
            System.out.println("bankNumber out of range.");
            return;
        }
        if (wordBanks.get(bankNumber).set(Utils.partDivide(args[1]), partOfSpeech, frequency)) {
            System.out.println("word updated.");
        }
        else {
            System.out.println("word added.");
        }
    }

    private static void add(String[] args) {
        int bankNumber;
        WordBank.PartOfSpeechType partOfSpeech = null;
        double frequency = -1;
        try {
            if (args.length < 2) {
                System.out.println("too few arguments.");
                return;
            }
            bankNumber = Integer.parseInt(args[0]);
            if (args.length > 2) {
                partOfSpeech = WordBank.PartOfSpeechType.parseType(args[2]);
                if (partOfSpeech == null) {
                    frequency = Double.parseDouble(args[2]);
                    if (args.length > 3) {
                        partOfSpeech = WordBank.PartOfSpeechType.parseType(args[3]);
                        if (partOfSpeech == null) {
                            System.out.println("bad argument(s).");
                            return;
                        }
                    }
                    else {
                        System.out.println("too few arguments.");
                        return;
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        if (bankNumber >= wordBanks.size()) {
            System.out.println("bankNumber out of range.");
            return;
        }
        String[] parts = Utils.partDivide(args[1]);
        if (wordBanks.get(bankNumber).find(parts)) {
            System.out.println("word already exist.");
        }
        else {
            wordBanks.get(bankNumber).set(Utils.partDivide(args[1]), partOfSpeech, frequency);
            System.out.println("word added.");
        }
    }

    private static void get(String[] args) {
        int bankNumber;
        String word;
        try {
            bankNumber = Integer.parseInt(args[0]);
            word = (args[1]);
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("too few arguments.");
            return;
        }
        if (bankNumber >= wordBanks.size()) {
            System.out.println("bankNumber out of range.");
            return;
        }
        if (wordBanks.get(bankNumber).find(Utils.partDivide(word))) {
            System.out.print("word:\t\t\t");
            System.out.println(word);
            System.out.print("part of speech:\t");
            if (wordBanks.get(bankNumber).getLastPartOfSpeech() == null) {
                System.out.println("undefined");
            }
            else {
                System.out.println(wordBanks.get(bankNumber).getLastPartOfSpeech());
            }
            System.out.print("frequency:\t\t");
            if (wordBanks.get(bankNumber).getLastFrequency() == 0) {
                System.out.println("undefined");
            }
            else {
                System.out.println(wordBanks.get(bankNumber).getLastFrequency());
            }
        }
        else {
            System.out.println("word not found.");
        }
    }

    private static void trace(String[] args) {
        int bankNumber;
        String word;
        try {
            bankNumber = Integer.parseInt(args[0]);
            word = (args[1]);
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("too few arguments.");
            return;
        }
        if (bankNumber >= wordBanks.size()) {
            System.out.println("bankNumber out of range.");
            return;
        }
        String[] parts = Utils.partDivide(word);
        double[] trace = wordBanks.get(bankNumber).getFrequencyTrace(parts, 0, parts.length);
        for (bankNumber = 0; bankNumber < parts.length; ++bankNumber) {
            System.out.print(parts[bankNumber]);
            System.out.print('\t');
            System.out.println(trace[bankNumber]);
        }
    }

    private static void delete(String[] args) {
        int bankNumber;
        String word;
        try {
            bankNumber = Integer.parseInt(args[0]);
            word = (args[1]);
        }
        catch (NumberFormatException e) {
            System.out.println("bad argument(s).");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("too few arguments.");
            return;
        }
        if (bankNumber >= wordBanks.size()) {
            System.out.println("bankNumber out of range.");
            return;
        }
        String[] parts = Utils.partDivide(word);
        if (wordBanks.get(bankNumber).delete(parts)) {
            System.out.println("word deleted.");
        }
        else {
            System.out.println("word not found.");
        }
    }

    private static void commandLoop(CommandScanner scanner, boolean canExit, boolean echo) {
        while (true) {
            if (echo) {
                System.out.print(">");
            }
            try {
                if (!scanner.scanCommand()) {
                    System.out.println("bad command.");
                    System.out.println();
                    continue;
                }
            }
            catch (IOException e) {
                System.out.println("the file is no longer be accessible.");
                System.out.println();
                return;
            }
            if (scanner.getCommand() == null) {
                return;
            }
            if (scanner.getCommand().length() == 0) {
                continue;
            }
            switch (scanner.getCommand().toLowerCase()) {
                case "bat":
                    bat(scanner.getArgs());
                    break;
                case "list":
                    list();
                    break;
                case "loadsource":
                    loadSource(scanner.getArgs());
                    break;
                case "load":
                    load(scanner.getArgs());
                    break;
                case "create":
                    create();
                    break;
                case "save":
                    save(scanner.getArgs());
                    break;
                case "savesource":
                    break;
                case "merge":
                    merge(scanner.getArgs());
                    break;
                case "set":
                    set(scanner.getArgs());
                    break;
                case "add":
                    add(scanner.getArgs());
                    break;
                case "get":
                    get(scanner.getArgs());
                    break;
                case "trace":
                    trace(scanner.getArgs());
                    break;
                case "delete":
                    delete(scanner.getArgs());
                    break;
                case "exit":
                    if (canExit) {
                        return;
                    }
                default:
                    System.out.println("unknown command.");
                    break;
            }
            if (echo) {
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println();
        wordBanks = new ArrayList<>();
        System.out.println("welcome to word bank manager.");
        CommandScanner scanner = new CommandScanner(new InputStreamReader(System.in));
        commandLoop(scanner, true, true);
    }

}
