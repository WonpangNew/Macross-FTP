package com.jlu.ftp.service;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by niuwanpeng on 17/4/2.
 *
 * ftp初始化配置
 */
@Service
public class FTPConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPConfig.class);
    private static FTPClient FTP_CLIENT = null;
    private static final FTPConfig FTP_CONFIG = new FTPConfig();
    public static final String SUB_DIR_NAME_FORMAT = "%0" + String.valueOf(1000).length() + "d";

    private static final String ftpHost = "139.129.35.200";

    private static final String ftpPassword = "cihome";

    private static final String ftpUserName = "work";

    private static final int ftpPort = 21;

    private FTPConfig() {
        FTP_CLIENT = this.getFtpClient();
    }

    public static FTPClient getFTPClient() {
        return FTP_CONFIG.getFtpClient();
    }

    public static FTPConfig getInstance() {
        return FTP_CONFIG;
    }

    private FTPClient getFtpClient() {
        try {
            FTP_CLIENT = new FTPClient();
            FTP_CLIENT.connect(ftpHost, ftpPort);
            FTP_CLIENT.login(ftpUserName, ftpPassword);
            if (!FTPReply.isPositiveCompletion(FTP_CLIENT.getReplyCode())) {
                LOGGER.info("未连接到FTP，用户名或密码错误。user:{}, passwd:{}", ftpUserName, ftpPassword);
                FTP_CLIENT.disconnect();
            } else {
                LOGGER.info("FTP连接成功。");
            }
        } catch (SocketException e) {
            LOGGER.info("FTP的IP地址可能错误，请正确配置。host:{}", ftpHost, e);
        } catch (IOException e) {
            LOGGER.info("FTP的端口错误,请正确配置。port:{}", ftpPort, e);
        }
        FTP_CLIENT.setControlEncoding("UTF-8");
        return FTP_CLIENT;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public String getFtpUserName() {
        return ftpUserName;
    }

    public int getFtpPort() {
        return ftpPort;
    }
}
