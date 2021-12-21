package com.lishiliang.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @version 1.0
 */
public class MultiReadFile {
    
    public static void main(String[] args) {
        int NUMBER = 10;
        final String URL_DOWNLOAD = "https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/bd_logo1_31bdc765.png";
        final String PATH_TARGET = "F:/temp/download/";

        MultiReadFile loder = new MultiReadFile();
        File file = loder.createFile(PATH_TARGET, URL_DOWNLOAD);
        loder.startLoadThread(NUMBER, file, URL_DOWNLOAD);
    }
    /**
     * 创建目标文件(希望保存的文件和下载的同名)
     * 
     * @param targetPath
     *            目标路径
     * @param sourseURL
     *            根据源URL获取文件名
     * @return
     */
    public File createFile(String targetPath, String sourseURL) {
        return new File(targetPath
                + sourseURL.substring(sourseURL.lastIndexOf("/") + 1));
    }

    /**
     * 如果出现不整除的情况(如:11字节,4个线程,每个线程3字节,多出1字节)，但是实际上RandomAccessFile的read()
     * 读到文件尾会返回-1,因此不考虑余数问题
     * 
     * @param threadNum
     *            线程数量
     * @param targetFile
     *            目标文件
     * @param sourseURL
     *            源文件URL
     */
    public void startLoadThread(int threadNum, File targetFile, String sourseURL) {
        try {
            // 网络连接
            URLConnection connection = new URL(sourseURL).openConnection();
            long sourseSize = connection.getContentLengthLong();
            // 为目标文件分配空间
            this.openSpace(targetFile, sourseSize);
            // 分线程下载文件
            long avgSize = sourseSize / threadNum + 1;
            for (int i = 0; i < threadNum; i++) {
                System.out.println(avgSize * i + "------" + (avgSize * (i + 1)));
                new Thread(new DownloadsTask(avgSize * i, avgSize * (i + 1),
                        targetFile, sourseURL)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 为目标文件分配空间
     * 
     * @param targetfile
     *            目标文件
     * @param sourseSize
     *            源文件大小
     */
    private void openSpace(File targetfile, Long sourseSize) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(targetfile, "rw");
            randomAccessFile.setLength(sourseSize);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null)
                    randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *
     * @version 1.0
     */
    public class DownloadsTask implements Runnable {
        private long start;
        private long end;
        private File file;
        private String loadUrl;

        /**
         * 构造函数
         *
         * @param start
         *            开始位置
         * @param end
         *            结束位置
         * @param targetFile
         *            目标文件
         * @param loadUrl
         *            下载网址
         */
        public DownloadsTask(long start, long end, File targetFile, String loadUrl) {
            this.start = start;
            this.end = end;
            this.file = targetFile;
            this.loadUrl = loadUrl;
        }

        @Override
        public void run() {
            BufferedInputStream bufferedInputStream = null;
            RandomAccessFile randomAccessFile = null;
            try {
                URL url = new URL(loadUrl);
                URLConnection conn = url.openConnection();
                bufferedInputStream = new BufferedInputStream(conn.getInputStream());
                randomAccessFile = new RandomAccessFile(file, "rw");

                // 源文件和目标文件的指针指向同一个位置
                bufferedInputStream.skip(start);
                randomAccessFile.seek(start);

                long readLen = end - start;
                // 如果比默认长度小，就没必要按照默认长度读取文件了
                byte[] bs = new byte[(int) (2048 < readLen ? 2048 : readLen)];
                while (start < end
                        && (readLen = bufferedInputStream.read(bs)) != -1) {
                    start += readLen;
                    randomAccessFile.write(bs, 0, (int) readLen);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //关闭流
                try {
                    if (null != bufferedInputStream)
                        bufferedInputStream.close();
                    if (null != randomAccessFile)
                        randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}