package com.lishiliang.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

/**
 * 多线程分割大文件
 * @version 1.0
 */
public class MultiSplitLargeFile {

    public static void main(String[] args) throws Exception {
        String sourseFilePath = "E:\\home\\app\\files\\in\\新建文本文档.txt";
        FileChannel open = FileChannel.open(Paths.get(sourseFilePath));
        long size = open.size();
        System.out.println(size);
        System.out.println(open.position());

        startThread(20, new File(sourseFilePath).length(), sourseFilePath, "E:\\home\\app\\files\\out\\新建文本文档.txt");
    }

    /**
     * 开启多线程分割
     * 
     * @param threadnum 线程数
     * @param fileLength 文件大小（用于确认每个线程下载多少东西）
     * @param sourseFilePath 源文件目录
     * @param targerFilePath 目标文件目录
     */
    public static void startThread(int threadnum, long fileLength, String sourseFilePath, String targerFilePath) throws FileNotFoundException, InterruptedException {

        String fileParent = targerFilePath.substring(0 ,targerFilePath.lastIndexOf(File.separator));
        String fileName = targerFilePath.substring(targerFilePath.lastIndexOf(File.separator) + 1);
        long targetLength = fileLength / threadnum;
        for (int i = 0; i < threadnum; i++) {
            //最后一个线程 处理剩下的所有数据
            if (i == threadnum - 1) {
                new FileWriteThread((targetLength * i), fileLength, sourseFilePath, String.format("%s%s%s_%s", fileParent, File.separator, i , fileName)).start();
            } else {
                new FileWriteThread((targetLength * i), (targetLength * (i + 1)), sourseFilePath, String.format("%s%s%s_%s", fileParent, File.separator, i , fileName)).start();
            }

        }

    }


    /**
     * 写线程：分割文件
     */
    static class FileWriteThread extends Thread {

        private long begin;
        private long end;
        private RandomAccessFile sourseFile;
//        private FileChannel sourseFileChannel;
        private RandomAccessFile targerFile;
        private FileChannel targerFileChannel;

        public FileWriteThread(long begin, long end, String sourseFilePath, String targerFilePath) throws FileNotFoundException {
            this.begin = begin;
            this.end = end;
            this.sourseFile = new RandomAccessFile(sourseFilePath, "rw");
//            this.sourseFileChannel = sourseFile.getChannel();
            this.targerFile = new RandomAccessFile(targerFilePath, "rw");
//            this.targerFileChannel = targerFile.getChannel();
        }


        public void run() {
            try {

                if (begin != 0) {

                    //读文件前2个字节判断是否是行末尾
                    sourseFile.seek(begin - 2);
                    int pre = sourseFile.read();
                    int curr = sourseFile.read();
                    sourseFile.seek(begin);

                    beginSplit(pre, curr);
                }



                //清空文件
                targerFile.setLength(0);
                targerFile.seek(0);
                if (begin >= end) {
                    return;
                }

                sourseFile.seek(begin);
                int hasRead = -1;

                int min = (int)Math.min(1024, end - begin);
                byte[] buffer = new byte[min];

                int pre = -1;
                int curr = -1;
                while (begin < end && -1 != (hasRead = sourseFile.read(buffer))) {
                    //hasRead > 1 读取大于一个字节
                    if (hasRead > 1) {
                        pre = buffer[hasRead - 2];
                    } else if (hasRead == 1){
                        pre = curr;
                    }
                    curr = buffer[hasRead - 1];
                    begin += hasRead;
                    targerFile.write(buffer, 0, hasRead);
                }

                //如果不是读到文件末尾
                if (hasRead != -1) {
                   afterSpilt(pre, curr);
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    sourseFile.close();
                    targerFile.close();
                } catch (Exception e) {
                }
            }
        }

        //从下一行开始写
        private void beginSplit(int pre, int curr) throws IOException {
            if (isEndOfLine(pre, curr)) {
                return;
            } else {
                pre = curr;
            }
            while ((curr = sourseFile.read()) != -1 ) {
                begin++;
                if (isEndOfLine(pre, curr)) {
                    break;
                } else {
                    pre = curr;
                }
            }
        }

        //写到换行符才真正结束
        private void afterSpilt(int pre, int curr) throws IOException {
            if (isEndOfLine(pre, curr)) {
                return;
            } else {
                pre = curr;
            }

            while ((curr = sourseFile.read()) != -1) {
                if (isEndOfLine(pre, curr)) {
                    break;
                } else {
                    //todo
                    targerFile.write(curr);
                    pre = curr;
                }
            }
        }
    }

    private static boolean isEndOfLine(int pre, int curr) {
        return pre == '\r' && curr == '\n';
    }
}