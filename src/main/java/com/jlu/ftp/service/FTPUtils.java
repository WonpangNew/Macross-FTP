package com.jlu.ftp.service;


import com.google.gson.Gson;
import com.jlu.ftp.bean.FTPStatus;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by niuwanpeng on 17/4/2.
 *
 * ftp工具类
 */
public class FTPUtils {

    private static final String STATUS = "STATUS";
    private static final String ERR_MSG = "ERR_MSG";
    private static final String DOWN_URL = "DOWN_URL";
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPUtils.class);
    private static final Gson GSON = new Gson();

    /**
     * 以流的方式上传文件
     * @param remoteDir 远程目录
     * @param remoteFileName 文件名
     * @param inputStream 文件流
     * @return
     */
    public static String uploadFileByInputStream(String remoteDir, String remoteFileName, InputStream inputStream) {
        FTPClient ftpClient = null;
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(STATUS, FTPStatus.FAIL);
        if (!StringUtils.isBlank(remoteFileName) && null != inputStream) {
            try {
                ftpClient = FTPConfig.getFTPClient();
                String subDir = getSubDirByFileName(remoteDir, remoteFileName);
                mkAndcdDir(ftpClient, subDir);
                boolean result = ftpClient.storeFile(remoteFileName, inputStream);
                if(!result) {
                    response.put(ERR_MSG, "FTP fail to uploadFileFromInputStream.");
                    LOGGER.error("FTP fail to uploadFileFromInputStream.");
                } else {
                    response.put(STATUS, FTPStatus.SUCCESS);
                    response.put(DOWN_URL, getFileHTTPUrlByFileName(remoteDir, remoteFileName));
                }
            } catch (IOException e) {
                response.put(ERR_MSG, "ftp uploadFileFromInputStream error.");
                LOGGER.error("ftp uploadFileFromInputStream error.", e);
            } finally {
                logout(ftpClient, inputStream);
            }
        } else {
            response.put(ERR_MSG, "filename or inputStream must not be null or empty");
            LOGGER.error("filename or inputStream must not be null or empty");
        }
        return GSON.toJson(response);
    }

    /**
     * 直接以文件的方式上传
     * @param remoteDir 远程目录
     * @param remoteFileName 文件名
     * @param localFile 待上传文件
     * @return
     */
    public static String uploadLocalFile(String remoteDir, String remoteFileName, File localFile) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(localFile);
        } catch (FileNotFoundException e) {
            LOGGER.error("FTP not found.", e);
        }
        return uploadFileByInputStream(remoteDir, remoteFileName, in);
    }

    /**
     * 下载文件
     * @param remoteDir
     * @param remoteFileName
     * @param localFile
     */
    public static String downFile(String remoteDir, String remoteFileName, File localFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(localFile);
        } catch (FileNotFoundException e) {
            LOGGER.error("FTP not found.", e);
        }
        return retrieveFileOutputStream(remoteDir, remoteFileName, out);
    }

    /**
     * 拼接路径，并根据文件名计算hash值作为一级目录
     * @param path
     * @param fileName
     * @return
     */
    private static String getSubDirByFileName(String path, String fileName) {
        if(StringUtils.isNotBlank(path) && path.startsWith("/")) {
            LOGGER.error("your remote path must not start with \'/\'");
            return null;
        } else {
            int hash = fileName.hashCode();
            int mod = Math.abs(hash % 1000);
            String subDir = String.format(FTPConfig.SUB_DIR_NAME_FORMAT, new Object[]{Integer.valueOf(mod)});
            return StringUtils.isBlank(path) ? subDir:(path.endsWith("/") ? path + subDir:path + "/" + subDir);
        }
    }

    /**
     * 获得文件下载链接，HTTP方式
     * @param remoteDir
     * @param fileName
     * @return
     */
    private static String getFileHTTPUrlByFileName(String remoteDir, String fileName) {
        if(StringUtils.isBlank(fileName)) {
            return null;
        } else {
            String httpHost = FTPConfig.getInstance().getFtpHost();
            StringBuffer sb = new StringBuffer();
            if(!httpHost.startsWith("http://")) {
                sb.append("http://");
            }
            sb.append(httpHost);
            sb.append("/").append(FTPConfig.getInstance().getFtpUserName());
            String subDir = getSubDirByFileName(remoteDir, fileName);
            sb.append("/").append(subDir);
            sb.append("/").append(fileName);
            return sb.toString();
        }
    }

    /**
     * 关闭链接
     * @param ftpClient
     * @param is
     */
    private static void logout(FTPClient ftpClient, InputStream is) {
        if(null != is) {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.error("ftp close file error", e.getMessage());
            }
        }
        if(null != ftpClient) {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                LOGGER.error("ftp logout error", e.getMessage());
            } finally {
                if(ftpClient.isConnected()) {
                    try {
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        LOGGER.error("ftp disconnect error", e.getMessage());
                    }
                }

            }
        }
    }

    /**
     * 创建或切换到指定目录下
     * @param ftpClient
     * @param subDir
     * @throws IOException
     */
    private static void mkAndcdDir(FTPClient ftpClient, String subDir) throws IOException {
        String[] dirArray = subDir.split("/");
        String[] arr$ = dirArray;
        int len$ = dirArray.length;
        for(int i$ = 0; i$ < len$; ++i$) {
            String dir = arr$[i$];
            boolean isExist = existDirInCurrentDir(ftpClient, dir);
            boolean isCD;
            if(!isExist) {
                isCD = ftpClient.makeDirectory(dir);
                if(!isCD) {
                    LOGGER.error("fail to create dir:" + dir);
                }
                boolean isCD1 = ftpClient.changeWorkingDirectory(dir);
                if(!isCD1) {
                    LOGGER.error("success to create dir, cd error." + dir);
                }
            } else {
                isCD = ftpClient.changeWorkingDirectory(dir);
                if(!isCD) {
                    LOGGER.error("current dir is exist, cd error." + dir);
                }
            }
        }
    }

    /**
     * 判断当前目录是否存在
     * @param ftpClient
     * @param dir
     * @return
     * @throws IOException
     */
    private static boolean existDirInCurrentDir(FTPClient ftpClient, String dir) throws IOException {
        FTPFile[] files = ftpClient.listFiles();
        if(files == null) {
            return false;
        } else {
            FTPFile[] arr$ = files;
            int len$ = files.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                FTPFile file = arr$[i$];
                if(file.isDirectory() && dir.equals(file.getName())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 下载文件实际执行方法
     * @param remoteDir
     * @param remoteFileName
     * @param out
     * @return
     */
    public static String retrieveFileOutputStream(String remoteDir, String remoteFileName, OutputStream out) {
        FTPClient ftpClient = null;
        Map<String, Object> response = new HashedMap();
        response.put(STATUS, FTPStatus.FAIL);
        if(StringUtils.isBlank(remoteFileName)) {
            LOGGER.error("remoteFileName must not be null or empty");
            response.put(ERR_MSG, "remoteFileName must not be null or empty");
        } else {
            InputStream in = null;
            try {
                ftpClient = FTPConfig.getFTPClient();
                String subDir = getSubDirByFileName(remoteDir, remoteFileName);
                String filePath = subDir + "/" + remoteFileName;
                if(out != null) {
                    boolean result = ftpClient.retrieveFile(filePath, out);
                    if(!result) {
                        LOGGER.error("FTP fail to retrieveFileOutputStream.");
                        response.put(ERR_MSG, "FTP fail to retrieveFileOutputStream.");
                    } else {
                        response.put(STATUS, FTPStatus.SUCCESS);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("ftp retrieveFileStream error.", e);
                response.put(ERR_MSG, "ftp retrieveFileStream error.");
            } finally {
                logout(ftpClient, in);
            }
        }
        return GSON.toJson(response);
    }
}