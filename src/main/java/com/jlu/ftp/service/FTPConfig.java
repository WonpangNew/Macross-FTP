package com.jlu.ftp.service;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by niuwanpeng on 17/4/2.
 *
 * ftp初始化配置
 */
public class FTPConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPConfig.class);
    private static FTPClient FTP_CLIENT = null;
    private static final FTPConfig FTP_CONFIG = new FTPConfig();
    public static final String SUB_DIR_NAME_FORMAT = "%0" + String.valueOf(1000).length() + "d";

    @Value("${ftp.host}")
    private String ftpHost;
    @Value("${ftp.password}")
    private String ftpPassword;
    @Value("${ftp.username}")
    private String ftpUserName;
    @Value("${ftp.port}")
    private int ftpPort;

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
        if (FTP_CLIENT != null && FTPReply.isPositiveCompletion(FTP_CLIENT.getReplyCode())) {
            return FTP_CLIENT;
        }
        try {
            FTP_CLIENT = new FTPClient();
            FTP_CLIENT.connect(ftpHost, ftpPort);
            FTP_CLIENT.login(ftpUserName, ftpPassword);
            if (!FTPReply.isPositiveCompletion(FTP_CLIENT.getReplyCode())) {
                LOGGER.info("未连接到FTP，用户名或密码错误。");
                FTP_CLIENT.disconnect();
            } else {
                LOGGER.info("FTP连接成功。");
            }
        } catch (SocketException e) {
            LOGGER.info("FTP的IP地址可能错误，请正确配置。", e);
        } catch (IOException e) {
            LOGGER.info("FTP的端口错误,请正确配置。", e);
        }
        FTP_CLIENT.setControlEncoding("UTF-8");
        return FTP_CLIENT;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpUserName() {
        return ftpUserName;
    }

    public void setFtpUserName(String ftpUserName) {
        this.ftpUserName = ftpUserName;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }
}
