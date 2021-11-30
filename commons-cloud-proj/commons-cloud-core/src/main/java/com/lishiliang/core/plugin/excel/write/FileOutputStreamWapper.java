package com.lishiliang.core.plugin.excel.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-11-21 18:09
 * @desc :
 */
public class FileOutputStreamWapper extends FileOutputStream {

    private static final Logger logger = LoggerFactory.getLogger(FileOutputStreamWapper.class);

    private OutputStream out;

    public FileOutputStreamWapper(String name) throws FileNotFoundException {
        super(name);
    }

    public FileOutputStreamWapper(String name, boolean append, OutputStream out) throws FileNotFoundException {
        super(name, append);
        this.out = out;
    }

    public FileOutputStreamWapper(File file, OutputStream out) throws FileNotFoundException {
        super(file);
        this.out = out;
    }

    public FileOutputStreamWapper(File file, boolean append, OutputStream out) throws FileNotFoundException {
        super(file, append);
        this.out = out;
    }

    public FileOutputStreamWapper(FileDescriptor fdObj, OutputStream out) {
        super(fdObj);
        this.out = out;
    }


    @Override
    public void close() {
        logger.warn(" do nothing ");
    }

    public void relClose()  {
        try {
            super.flush();
            super.close();
        } catch (IOException e) {
            logger.error(" close error msg :{}", e.getMessage());
        }
    }



}
