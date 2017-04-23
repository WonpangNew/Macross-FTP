package com.jlu.ftp.service;


import com.google.gson.Gson;
import com.jlu.ftp.bean.FTPStatus;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

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
     * 上传文件
     * @param remoteDir 远程目录
     * @param remoteFileName 文件名
     * @param file
     * @return
     */
    public static String uploadFileByInputStream(String remoteDir, String remoteFileName, MultipartFile file) {
        FTPClient ftpClient = null;
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(STATUS, FTPStatus.FAIL);
        try {
            InputStream inputStream = file.getInputStream();
            if (!StringUtils.isBlank(remoteFileName) && null != inputStream) {
                try {
                    ftpClient = FTPConfig.getFTPClient();
                    String subDir = getSubDirByFileName(remoteDir, remoteFileName);
                    mkAndcdDir(ftpClient, subDir);
                    LOGGER.info("Starting upload file:{}", remoteDir + "/" + remoteFileName);
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
                    LOGGER.info("Ending upload file:{}", remoteDir + "/" + remoteFileName);
                    logout(ftpClient, inputStream);
                }
            } else {
                response.put(ERR_MSG, "filename or inputStream must not be null or empty");
                LOGGER.error("filename or inputStream must not be null or empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GSON.toJson(response);
    }

    /**
     * 下载文件
     * @param remoteDir
     * @param remoteFileName
     * @param out
     */
    public static String downFile(String remoteDir, String remoteFileName, OutputStream out) {
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

    /**
     * 拷贝文件到指定目录
     * @param sourceDir 原路径及其文件名
     * @param targetDir 目标路径及其文件名
     * @return
     */
    public static String copyFile(String sourceDir, String targetDir, String sourceFileName) throws Exception{
        FTPClient ftpClient = null;
        ByteArrayInputStream in = null;
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        String result = "FAIL";
        try {
            ftpClient = FTPConfig.getFTPClient();
            sourceDir = getSubDirByFileName(sourceDir, sourceFileName);
            targetDir = getSubDirByFileName(targetDir, sourceFileName);
            ftpClient.setBufferSize(1024 * 2);
            ftpClient.changeWorkingDirectory(sourceDir);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(sourceFileName, fos);
            in = new ByteArrayInputStream(fos.toByteArray());
            if (in != null) {
                ftpClient.changeWorkingDirectory("/home/work");
                mkAndcdDir(ftpClient, targetDir);
                ftpClient.storeFile(sourceFileName, in);
                result = "SUCC";
                LOGGER.info("Copy file:{} is successful from sourceDir:{} to targetDir:{}", sourceFileName, sourceDir, targetDir);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return result;
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
    public static String getFileHTTPUrlByFileName(String remoteDir, String fileName) {
        if(StringUtils.isBlank(fileName)) {
            return "";
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
                    break;
                } else {
                    boolean isCD1 = ftpClient.changeWorkingDirectory(dir);
                    if(!isCD1) {
                        LOGGER.error("success to create dir, cd error." + dir);
                        break;
                    }
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

}
