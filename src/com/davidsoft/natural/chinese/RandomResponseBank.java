package com.davidsoft.natural.chinese;

import com.davidsoft.io.SimpleIO;

import java.io.*;
import java.util.*;

/**
 * 随机回应库
 */
public final class RandomResponseBank {

    //单例模式
    private static RandomResponseBank instance;

    /**
     * RandomResponseBank(本类)需要以单例模式创建对象。调用此函数以获取唯一的实例。
     *
     * @return 一个RandomResponseBank对象的实例。
     */
    public static RandomResponseBank getInstance() {
        if (instance == null) {
            try {
                instance = new RandomResponseBank(new File("ChineseRandomResponses"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private Random random;
    private HashMap<String, String[]> responses;

    private void LoadRandomResponseBank(InputStream in) throws IOException {
        responses = new HashMap<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int keyCount = SimpleIO.readInt(in);
        for (int i = 0; i < keyCount; i++) {
            String key = SimpleIO.readCString(in, buffer);
            String[] list = new String[SimpleIO.readInt(in)];
            for (int j = 0; j < list.length; j++) {
                list[j] = SimpleIO.readCString(in, buffer);
            }
            responses.put(key, list);
        }
        random = new Random();
    }

    private RandomResponseBank(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        IOException exception = null;
        try {
            LoadRandomResponseBank(in);
        } catch (IOException e) {
            exception = e;
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exception != null) {
            throw exception;
        }
    }

    public String getResponse(String key) {
        String[] array = responses.get(key);
        if (array == null) {
            return null;
        }
        return array[random.nextInt(array.length)];
    }

    /**
     * 通过源文件编译同义词库。
     *
     * @param sourceFiles 源文件集合。
     * @param out 输出。
     *
     * @throws IOException 当向输出流{@code out}写入数据发生IO异常时。
     */
    public static void compileSourceFiles(File[] sourceFiles, OutputStream out) throws IOException {
        HashMap<String, String[]> responses = new HashMap<>();
        ArrayList<String> stringArrayBuilder = new ArrayList<>();

        //读取
        for (File sourceFile : sourceFiles) {
            String methodName = sourceFile.getName();
            int findPos = methodName.lastIndexOf(".");
            if (findPos > 0) {
                methodName = methodName.substring(0, findPos);
            }
            Scanner scanner = new Scanner(new FileInputStream(sourceFile), "GBK");
            stringArrayBuilder.clear();
            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                if (line.length() > 0) {
                    stringArrayBuilder.add(line);
                }
            }
            if (stringArrayBuilder.size() > 0) {
                String[] stringArray = new String[stringArrayBuilder.size()];
                stringArrayBuilder.toArray(stringArray);
                responses.put(methodName, stringArray);
            }
            scanner.close();
        }

        //存储
        SimpleIO.writeInt(out, responses.size());
        for (Map.Entry<String, String[]> entry : responses.entrySet()) {
            SimpleIO.writeCString(out, entry.getKey());
            SimpleIO.writeInt(out, entry.getValue().length);
            for (String response : entry.getValue()) {
                SimpleIO.writeCString(out, response);
            }
        }
    }
}
