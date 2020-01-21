package eon.hg.fileserver.util.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.StringTokenizer;

@Slf4j
public class FtpHandler {

    private static FtpHandler ftpUtils;

    public static synchronized FtpHandler getInstance() throws Exception {
        if (ftpUtils == null) {
            ftpUtils = new FtpHandler();
        }
        return ftpUtils;
    }


    //ftp参数设置
    @SuppressWarnings("unused")
    private int timeOut, port;
    private String host, userName, passWord, encoding, baseDir;

    /***
     * 私有的构造方法
     */
    private FtpHandler() {

    }

    public FtpHandler(String userName, String passWord, String host, int port, String baseDir) {
        this.userName = userName;
        this.passWord = passWord;
        this.baseDir = baseDir;
        this.host = host;
        this.port = port;
        this.encoding = "GBK";
    }


    /**
     * ftp上传文件
     *
     * @param filePath
     * @param fileName
     * @param ins
     * @throws Exception
     */
    public void uploadFile(String filePath, String fileName, InputStream ins) throws Exception {

        FTPClient ftpClient = null;
        OutputStream os2ftp = null;

        try {
            ftpClient = new FTPClient();

            ftpClient.connect(host, port);

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            }

            boolean result = ftpClient.login(userName, passWord);
            if (!result) {
                throw new Exception("ftpClient登陆失败! ");
            }

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(1024 * 1024);
            ftpClient.setControlEncoding(encoding);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(baseDir);

            StringTokenizer dirs = new StringTokenizer(filePath, File.separator);
            String tempDir = "";
            while (dirs.hasMoreElements()) {
                tempDir = dirs.nextElement().toString();
                if (!ftpClient.changeWorkingDirectory(tempDir)) {
                    if (!ftpClient.makeDirectory(tempDir)) {
                        throw new Exception("创建目录[" + tempDir + "]失败！");
                    }
                    ;

                    ftpClient.changeWorkingDirectory(tempDir);
                }
                ;
            }
            ftpClient.storeFile(fileName, ins);
            ftpClient.logout();
        } catch (IOException e) {
            log.error("上传文件[" + fileName + "]失败");
            throw e;
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    // 20.断开ftp的连接
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }


    /**
     * 下载文件
     *
     * @param filePath
     * @param fileName
     * @return
     * @throws Exception
     */
    public byte[] downFile(String filePath, String fileName) throws Exception {

        long startTime = System.currentTimeMillis();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream is = null;
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            }

            boolean result = ftpClient.login(userName, passWord);
            if (!result) {
                throw new Exception("ftpClient登陆失败! ");
            }

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//	        ftpClient.setBufferSize(1024*10);
            ftpClient.setControlEncoding(encoding);
            ftpClient.enterLocalPassiveMode();

//	        ftpClient.changeWorkingDirectory("/Users/yueqh/Documents/");

            StringTokenizer dirs = new StringTokenizer(filePath, File.separator);
            String tempDir = "";
            while (dirs.hasMoreElements()) {
                tempDir = dirs.nextElement().toString();
                ftpClient.changeWorkingDirectory(tempDir);
            }

            ftpClient.retrieveFile(new String(fileName.getBytes("UTF-8"), "iso-8859-1"), output);
            if (output == null) {
                throw new Exception("当前附件不存在，请检查附件参数...");
            }
            System.out.println("downFile耗费时间...." + (System.currentTimeMillis() - startTime));
            return output.toByteArray();
        } catch (Exception e) {
            log.error("下载文件[" + fileName + "]失败");
            throw e;
        } finally {
            try {
                if (is != null)
                    is.close();
                if (output != null)
                    output.close();
                if (ftpClient != null && ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();

                }
            } catch (Exception e) {
                throw e;
            }
        }


    }

    public boolean downLoadDirectory(String localDirectoryPath, String remoteDirectory) {
        try {
            FTPClient ftpClient = new FTPClient();
//				ftpClient.setConnectTimeout(timeOut);

            ftpClient.connect(host, port);


            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            }

            boolean result = ftpClient.login(userName, passWord);
            if (!result) {
                throw new Exception("ftpClient登陆失败! ");
            }

            String fileName = new File(remoteDirectory).getName();
            localDirectoryPath = localDirectoryPath + fileName + "//";
            new File(localDirectoryPath).mkdirs();
            FTPFile[] allFile = ftpClient.listFiles(remoteDirectory);
            for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
                if (!allFile[currentFile].isDirectory()) {
                    FileOutputStream outputStream = null;
                    File file = new File(localDirectoryPath + File.separator + allFile[currentFile].getName());

                    outputStream = new FileOutputStream(file);
                    byte[] data = downFile(remoteDirectory, allFile[currentFile].getName());
                    outputStream.write(data);
                    outputStream.close();
                }
            }
            for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
                if (allFile[currentFile].isDirectory()) {
                    String strremoteDirectoryPath = remoteDirectory + "/" + allFile[currentFile].getName();
                    downLoadDirectory(localDirectoryPath, strremoteDirectoryPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.info("下载文件夹失败");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * description：删除文件
     * author：wsw
     * time: 2016-9-2 下午1:56:16
     */
    public void deleteFile(String filePath, String fileName) throws Exception {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(host, port);

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            }

            boolean result = ftpClient.login(userName, passWord);
            if (!result) {
                throw new Exception("ftpClient登陆失败! ");
            }

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(1024 * 10);
            ftpClient.setControlEncoding(encoding);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(baseDir);

            StringTokenizer dirs = new StringTokenizer(filePath, File.separator);
            String tempDir = "";
            while (dirs.hasMoreElements()) {
                tempDir = dirs.nextElement().toString();
                ftpClient.changeWorkingDirectory(tempDir);
            }
            ftpClient.deleteFile(fileName);
            ftpClient.logout();
        } catch (Exception e) {
            log.error("删除文件[" + fileName + "]失败");
            throw e;
        } finally {

            if (ftpClient != null) {
                ftpClient.disconnect();
            }
        }
    }

    /**
     * description：获取文件的字节流
     * author：wsw
     * time: 2016-9-2 下午12:56:54
     */
    public ByteArrayInputStream getFileInputStream(String filePath, String fileName) throws Exception {

        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();

            ftpClient.connect(host, port);

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            }

            boolean result = ftpClient.login(userName, passWord);
            if (!result) {
                throw new Exception("ftpClient登陆失败! ");
            }

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(1024 * 1024);
            ftpClient.setControlEncoding(encoding);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(baseDir);

            StringTokenizer dirs = new StringTokenizer(filePath, File.separator);
            String tempDir = "";
            while (dirs.hasMoreElements()) {
                tempDir = dirs.nextElement().toString();
                ftpClient.changeWorkingDirectory(tempDir);
            }
            if (ftpClient.retrieveFile(fileName, os)) {
                is = new ByteArrayInputStream(os.toByteArray());
            }


        } catch (Exception e) {
            log.error("下载文件[" + fileName + "]失败");
            throw e;
        } finally {

            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
            try {
                os.close();
            } catch (Exception e) {
                throw e;
            }
        }
        return is;
    }

}
