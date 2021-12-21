package com.lishiliang.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.StringTokenizer;

public class FTPTools {
    
    private static final Logger logger = LoggerFactory.getLogger(FTPTools.class);
    
    private String user;
    
    private String pwd;
    
    private String hostname;
    
    /**
     * 初始化是否被动模式
     */
    private boolean initPassiveMode;
    
    private boolean binaryTransfer = true;
    
    private int port = 21;
    
    private String initPath;
    
    public FTPTools(String hostname, String user, String pwd) {
    
        this.user = user;
        this.pwd = pwd;
        this.hostname = hostname;
    }
    
    public FTPTools(String hostname, int port, String user, String pwd) {
    
        this.user = user;
        this.pwd = pwd;
        this.hostname = hostname;
        this.port = port;
    }
    
    /**
     * 创建FTP工具类
     *
     * @param hostname
     *            hostname
     * @param port
     *            port
     * @param user
     *            user
     * @param pwd
     *            pwd
     * @param initPassiveMode
     *            初始化是否使用被动模式
     */
    public FTPTools(String hostname, int port, String user, String pwd, boolean initPassiveMode) {
    
        this.user = user;
        this.pwd = pwd;
        this.hostname = hostname;
        this.port = port;
        this.initPassiveMode = initPassiveMode;
    }
    
    public boolean isBinaryTransfer() {
    
        return binaryTransfer;
    }
    
    public void setBinaryTransfer(boolean binaryTransfer) {
    
        this.binaryTransfer = binaryTransfer;
    }
    
    public int getPort() {
    
        return port;
    }
    
    public void setPort(int port) {
    
        this.port = port;
    }
    
    private FTPClient ftpClient = null;
    
    /**
     * 设置FTP客服端的配置,一般可以不设置
     *
     * @return FTPClientConfig
     */
    private FTPClientConfig getFtpConfig() {
    
        FTPClientConfig ftpConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpConfig.setServerLanguageCode(FTP.DEFAULT_CONTROL_ENCODING);
        return ftpConfig;
    }
    
    /**
     * 连接到服务器
     */
    public void openConnect() {
    
        int reply;
        try {
            ftpClient = new FTPClient();
            ftpClient.setDefaultPort(port);
            ftpClient.configure(getFtpConfig());
            ftpClient.connect(hostname);
            ftpClient.setDataTimeout(60000);
            ftpClient.setConnectTimeout(60000);
            ftpClient.login(user, pwd);
            ftpClient.setControlEncoding("GBK");
            
            // 是否初始化被动模式
            if (initPassiveMode) {
                ftpClient.enterLocalPassiveMode();
            }
            
            reply = ftpClient.getReplyCode();
            
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                writeLog("FTP server refused connection.");
            } else {
                ftpClient.login(user, pwd);
                writeLog("登录FTP服务器[" + hostname + "]成功,当前目录为：" + ftpClient.printWorkingDirectory());
            }
        } catch (Exception e) {
            writeLog("登录FTP服务器[" + hostname + "]失败", e);
        }
    }
    
    /**
     * 关闭连接
     */
    public void closeConnect() {
    
        try {
            if (ftpClient != null) {
                ftpClient.logout();
                writeLog("退出登录服务器[" + hostname + "],服务器返回状态：" + ftpClient.getReplyString());
                ftpClient.disconnect();
                writeLog("已经断开FTP服务器[" + hostname + "]成功");
            } else {
                writeLog("已经断开FTP服务器[" + hostname + "]");
            }
            
        } catch (Exception e) {
            writeLog("断开FTP服务器[" + hostname + "]失败", e);
        }
    }
    
    /**
     * 进入到服务器的某个目录下
     *
     * @param directory
     *            directory
     * @throws IOException
     *             IOException
     */
    public boolean changeWorkingDirectory(String directory) throws IOException {
    
        if (ftpClient == null) {
            openConnect();
        }
        writeLog("更改工作目录开始，更改前工作目录：" + ftpClient.printWorkingDirectory());
        boolean b = ftpClient.changeWorkingDirectory(directory);
        if (b) {
            writeLog("更改完成，更改后工作目录：" + ftpClient.printWorkingDirectory());
        } else {
            writeLog("更改完成，未能更改工作目录");
        }
        return b;
    }
    
    /**
     * 设置传输文件的类型[文本文件或者二进制文件]
     *
     * @param fileType
     *            --BINARY_FILE_TYPE、ASCII_FILE_TYPE
     */
    public void setFileType(int fileType) {
    
        try {
            if (ftpClient == null) {
                openConnect();
            }
            ftpClient.setFileType(fileType);
        } catch (Exception e) {
            writeLog("异常", e);
        }
    }
    
    /**
     * 获取远程文件上传路径
     *
     * @param realBankType
     *            realBankType
     * @return path
     */
    
    public String genRemotePath(int realBankType) {
    
        if (!this.initPath.endsWith("/")) {
            this.initPath += "/";
        }
        
        return this.initPath + realBankType;
    }
    
    /**
     * 上传文件
     *
     * @param remoteFilePath
     *            --远程文件路径
     * @param remoteFileName
     *            --新的文件名
     */
    public boolean uploadFile(byte[] bytes, String remoteFilePath, String remoteFileName) {
    
        checkConnect(ftpClient);
        transferType(binaryTransfer);
        
        // 上传文件
        OutputStream f = null;
        try {
            if (!this.changeWorkingDirectory(remoteFilePath)) {
                this.makeRemoteDir(remoteFilePath);
                this.changeWorkingDirectory(remoteFilePath);
            }
            writeLog("FTP当前工作目录：" + ftpClient.printWorkingDirectory());
            
            f = ftpClient.storeFileStream(remoteFileName);
            f.write(bytes);
            f.flush();
            writeLog("上传文件服务器返回状态：" + ftpClient.getReplyString());
            return true;
        } catch (Exception e) {
            writeLog("上传文件失败!", e);
            return false;
        } finally {
            try {
                if (f != null) {
                    f.close();
                }
            } catch (IOException e) {
                writeLog("输出流关闭失败!", e);
            }
        }
    }
    
    /**
     * 上传文件
     *
     * @param localFilePath
     *            --本地文件路径
     * @param localFileName
     *            --本地文件名
     * @param remoteFilePath
     *            --远程文件路径
     * @param remoteFileName
     *            --远程文件名
     */
    public boolean uploadFile(String localFilePath, String localFileName, String remoteFilePath, String remoteFileName) {
    
        checkConnect(ftpClient);
        if (!localFilePath.endsWith("/")) {
            localFilePath += "/";
        }
        transferType(binaryTransfer);
        
        // 上传文件
        BufferedInputStream bis = null;
        try {
            if (!this.changeWorkingDirectory(remoteFilePath)) {
                this.makeRemoteDir(remoteFilePath);
                this.changeWorkingDirectory(remoteFilePath);
            }
            writeLog("FTP当前工作目录：" + ftpClient.printWorkingDirectory());
            bis = new BufferedInputStream(new FileInputStream(localFilePath + localFileName));
            
            boolean complete = ftpClient.storeFile(remoteFileName, bis);
            writeLog("上传文件" + localFilePath + localFileName + "服务器返回状态：" + ftpClient.getReplyString());
            
            return complete;
            
        } catch (Exception e) {
            writeLog("上传文件" + localFilePath + localFileName + "失败!", e);
            return false;
            
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {
                writeLog("输入流关闭失败!", e);
            }
        }
    }
    
    /**
     * 下载文件
     *
     * @param remoteFileName
     *            --服务器上的文件名
     * @param localFileName
     *            --本地文件名
     */
    public boolean downloadFile(String remoteFilePath, String remoteFileName, String localFilePath, String localFileName) {
    
        return downloadFile(remoteFilePath, remoteFileName, localFilePath, localFileName, null);
    }
    
    public boolean downloadFile(String remoteFilePath, String remoteFileName, String localFilePath,
        String localFileName, String charset) {
    
        checkConnect(ftpClient);
        
        if (!remoteFilePath.endsWith("/")) {
            remoteFilePath += "/";
        }
        if (!localFilePath.endsWith("/")) {
            localFilePath += "/";
        }
        transferType(binaryTransfer);
        if (localFileName == null || "".equals(localFileName)) {
            localFileName = remoteFileName;
        }
        // 下载文件
        BufferedOutputStream bos = null;
        try {
            if (StringUtils.isNotBlank(remoteFilePath)) {
                changeWorkingDirectory(remoteFilePath);
            }
            writeLog("开始下载文件到" + localFilePath + localFileName);
            if (isExist(remoteFileName)) {
                bos = new BufferedOutputStream(new FileOutputStream(localFilePath + localFileName));
                ftpClient.retrieveFile(charset == null ? remoteFileName : new String(remoteFileName.getBytes(charset),
                    "ISO-8859-1"), bos);
                writeLog(ftpClient.getReplyString());
                writeLog("下载文件" + remoteFilePath + remoteFileName + "成功!");
                return true;
            } else {
                writeLog("文件" + remoteFilePath + remoteFileName + "不存在!");
                writeLog("下载文件" + remoteFilePath + remoteFileName + "失败!");
                return false;
            }
        } catch (Exception e) {
            writeLog(ftpClient.getReplyString() + "[FTPTools]:下载文件" + remoteFilePath + remoteFileName + "失败!", e);
            return false;
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                writeLog("异常", e);
            }
        }
    }
    
    /**
     * 设置文件传输类型
     *
     * @param binaryTransfer
     *            binaryTransfer
     */
    public void transferType(boolean binaryTransfer) {
    
        try {
            if (binaryTransfer) {
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            }
        } catch (IOException e) {
            writeLog("异常", e);  
        }
    }
    
    /**
     * 检测文件或文件夹是否存在
     *
     * @param fileName
     *            --文件或文件夹名称
     * @return true/false
     */
    public boolean isExist(String fileName) {
    
        checkConnect(ftpClient);
        boolean tmp = false;
        try {
            writeLog("当前目录为" + ftpClient.printWorkingDirectory());
            
            String[] str = ftpClient.listNames();
            for (int i = 0; i < str.length; i++) {
                if (str[i].equals(fileName)) {
                    tmp = true;
                }
            }
        } catch (IOException e) {
            writeLog("异常", e);
        }
        return tmp;
    }
    
    public void checkConnect() {
    
        checkConnect(this.ftpClient);
    }
    
    private void checkConnect(FTPClient ftpClient) {
    
        if (ftpClient == null) {
            openConnect();
        } else {
            try {
                ftpClient.stat();
            } catch (IOException e) {
                try {
                    ftpClient.setDefaultPort(port);
                    ftpClient.configure(getFtpConfig());
                    ftpClient.connect(hostname);
                    ftpClient.login(user, pwd);
                    ftpClient.setControlEncoding("GB2312");
                    writeLog(ftpClient.getReplyString());
                    int reply = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(reply)) {
                        ftpClient.disconnect();
                        writeLog("FTP server refused connection.");
                    } else {
                        ftpClient.login(user, pwd);
                        writeLog("登录FTP服务器[" + hostname + "]成功");
                        writeLog("当前目录为" + ftpClient.printWorkingDirectory());
                    }
                } catch (Exception e2) {
                    writeLog("登录FTP服务器[" + hostname + "]失败", e);
                }
            }
        }
    }
    
    public boolean createDirectory(String pathName) throws IOException {
    
        return ftpClient.makeDirectory(pathName);
    }
    
    public void makeRemoteDir(String dir) throws IOException {
    
        String workingDirectory = ftpClient.printWorkingDirectory();
        if (dir.indexOf("/") == 0) {
            ftpClient.changeWorkingDirectory("/");
        }
        String subDir = new String();
        StringTokenizer st = new StringTokenizer(dir, "/");
        while (st.hasMoreTokens()) {
            subDir = st.nextToken();
            if (!(ftpClient.changeWorkingDirectory(subDir))) {
                if (!(ftpClient.makeDirectory(subDir))) {
                    int rc = ftpClient.getReplyCode();
                    if (((rc != 550) && (rc != 553) && (rc != 521))) {
                        writeLog("不能创建新目录: " + ftpClient.getReplyString());
                    }
                } else {
                    ftpClient.changeWorkingDirectory(subDir);
                    writeLog("创建了新目录：" + ftpClient.printWorkingDirectory());
                }
            }
        }
        if (workingDirectory != null) {
            ftpClient.changeWorkingDirectory(workingDirectory);
        }
    }
    
    /**
     * 从FTP服务器读取字节流
     *
     * @param filePath
     *            filePath
     * @return byte[]
     */
    public byte[] retrieveFileStream(String filePath) {
    
        checkConnect(ftpClient);
        
        byte[] data = null;
        InputStream in = null;
        ByteArrayOutputStream swapStream = null;
        try {
            in = ftpClient.retrieveFileStream(filePath);
            
            if (in == null) {
                writeLog("获取ftp不到照片流filePath：" + filePath);
                throw new RuntimeException("获取ftp照片流出错");
            }
            
            swapStream = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = in.read(b)) != -1) {
                swapStream.write(b, 0, n);
            }
            data = swapStream.toByteArray();
            
        } catch (IOException e) {
            writeLog("获取ftp照片流出错：" + e);
            throw new RuntimeException("获取ftp照片流出错", e);
        } catch (Exception ex) {
            writeLog("获取ftp照片流出错：" + ex);
            throw new RuntimeException("获取ftp照片流出错", ex);
        } finally {
            try {
                if (swapStream != null) {
                    swapStream.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("close stream error", e);
            }
        }
        
        return data;
    }
    
    private void writeLog(String info) {
    
        logger.info("[FTPTools]" + info);
    }
    
    private void writeLog(String info, Throwable t) {
    
        logger.info("[FTPTools]" + t.getMessage(), t);
    }
    
    /**
     * 获取文件字节数组
     *
     * @author yang.hu
     *         2017年8月22日 上午11:08:47
     */
    public byte[] file2Bytes(String remoteFilePath, String remoteFileName) {
    
        checkConnect(ftpClient);
        
        if (!remoteFilePath.endsWith("/")) {
            remoteFilePath += "/";
        }
        
        byte[] buffer = null;
        InputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if (StringUtils.isNotBlank(remoteFilePath)) {
                changeWorkingDirectory(remoteFilePath);
            }
            
            if (isExist(remoteFileName)) {
                File file = new File(remoteFilePath + remoteFileName);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                String remoteAbsoluteFile = file.getName();
                remoteAbsoluteFile = new String(remoteAbsoluteFile.getBytes("UTF-8"), "ISO-8859-1");
                
                fis = ftpClient.retrieveFileStream(remoteAbsoluteFile);
                
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                buffer = bos.toByteArray();
            } else {
                writeLog("文件" + remoteFilePath + remoteFileName + "不存在!");
                writeLog("获取文件" + remoteFilePath + remoteFileName + "字节数组失败!");
            }
        } catch (IOException e) {
            writeLog(ftpClient.getReplyString() + "[FTPTools]:获取文件" + remoteFilePath + remoteFileName + "字节数组失败!");
            writeLog("获取文件字节数组失败!", e);
        } finally {
            try {
                if(fis != null) {
                    
                    fis.close();
                }
            } catch (IOException e) {
                writeLog("关闭输入流失败!", e);
            }
            try {
                bos.close();
            } catch (IOException e) {
                writeLog("关闭字节输出流失败!", e);
            }
        }
        
        return buffer;
    }
    
    /**
     * @desc: 获取指定路径下的文件名列表
     * @param receiptPath
     * @return
     */
    public String[] getFileNameList(String receiptPath) {
    
        checkConnect(ftpClient);
        if (!receiptPath.endsWith("/")) {
            receiptPath += "/";
        }
        
        try {
            if (StringUtils.isNotBlank(receiptPath)) {
                changeWorkingDirectory(receiptPath);
            }
            return ftpClient.listNames();
        } catch (Exception e) {
            return null;
        }
    }
    
    
    public static void main(String[] args) {
        FTPTools app = new FTPTools("192.168.70.154", 22,"app", "xdzf@2020");

        app.checkConnect();
        System.out.println("=================end=================");
    }
}
