package com.lishiliang.core.utils;

import java.io.*;

/**
 * 多线程复制文件
 * @version 1.0
 */
public class MultiWriteFile {
    public static void main(String[] args) throws Exception {
        File file = new File("E:\\home\\app\\files\\in\\BI.xlsx");
//        FileInputStream inputStream = new FileInputStream(file);
        int ni = '\n';
        int ri = '\r';
        System.out.println(ni);
        System.out.println(ri);
        System.out.println("==================");

//        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

//        for (String line =  bufferedReader.readLine(); line != null ;) {
//            System.out.println(line);
//        }
//        while ((i = inputStream.read()) != -1) {
////            char[] chars = Character.toChars();
////            System.out.println(chars.length);
//            System.out.println(i);
////            System.out.println(i == '\n');
//        }
        startThread(1, file.length(), "E:\\home\\app\\files\\in\\新建文本文档.txt", "E:\\home\\app\\files\\out\\新建文本文档.txt");
    }

    /**
     * 开启多线程下载
     * 
     * @param threadnum 线程数
     * @param fileLength 文件大小（用于确认每个线程下载多少东西）
     * @param sourseFilePath 源文件目录
     * @param targerFilePath 目标文件目录
     */
    public static void startThread(int threadnum, long fileLength, String sourseFilePath, String targerFilePath) throws FileNotFoundException {

//        String fileParent = targerFilePath.substring(0 ,targerFilePath.lastIndexOf(File.separator));
//        String fileName = targerFilePath.substring(targerFilePath.lastIndexOf(File.separator) + 1);
        long targetLength = fileLength / threadnum;
        for (int i = 0; i < threadnum; i++) {
            new FileWriteThread((targetLength * i), (targetLength * (i + 1)), sourseFilePath, targerFilePath).start();
//            new FileWriteThread((targetLength * i), (targetLength * (i + 1)), sourseFilePath, String.format("%s%s%s_%s", fileParent, File.separator, i , fileName)).start();
        }
    }


    /**
     * 写线程：指定文件开始位置、结束位置、源文件、目标文件，
     */
    static class FileWriteThread extends Thread {
        //模式 1-复制模式,2-分割模式,3-自动分割模式(优化换行符)
        private int mode = 1;
        private long begin;
        private long end;
        private RandomAccessFile soursefile;
        private RandomAccessFile targerFile;

        public FileWriteThread(long begin, long end, String sourseFilePath, String targerFilePath) throws FileNotFoundException {
            this.begin = begin;
            this.end = end;
            this.soursefile = new RandomAccessFile(sourseFilePath, "rw");
            this.targerFile = new RandomAccessFile(targerFilePath, "rw");
        }

        public FileWriteThread(long begin, long end, String sourseFilePath, String targerFilePath, int mode) throws FileNotFoundException {
            this.begin = begin;
            this.end = end;
            this.soursefile = new RandomAccessFile(sourseFilePath, "rw");
            this.targerFile = new RandomAccessFile(targerFilePath, "rw");
            this.mode = mode;
        }

        public void run() {
            try {
                soursefile.seek(begin);
                targerFile.seek(begin);
                int hasRead = 0;

                int min = (int)Math.min(1024, (end - begin));
                byte[] buffer = new byte[min];
                while (begin < end && -1 != (hasRead = soursefile.read(buffer))) {
                    begin += hasRead;
                    targerFile.write(buffer, 0, hasRead);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    soursefile.close();
                    targerFile.close();
                } catch (Exception e) {
                }
            }
        }
    }
}