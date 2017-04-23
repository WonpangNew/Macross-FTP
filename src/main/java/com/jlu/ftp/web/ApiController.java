package com.jlu.ftp.web;

import com.jlu.ftp.service.FTPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by niuwanpeng on 17/4/2.
 *
 * 开放接口 controller
 */
@Controller
@RequestMapping(("/ftp/api"))
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    /**
     * 上传产出
     * @param remoteDir
     * @param remoteFileName
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(@RequestParam("remoteDir") String remoteDir,
                             @RequestParam("remoteFileName") String remoteFileName,
                             @RequestParam("file") MultipartFile file) {
        return FTPUtils.uploadFileByInputStream(remoteDir, remoteFileName, file);
    }

    /**
     * 以链接形式下载产出
     * @param remoteDir
     * @param remoteFileName
     * @return
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ResponseBody
    public String downloadFile(@RequestParam("remoteDir") String remoteDir,
                               @RequestParam("remoteFileName") String remoteFileName,
                               HttpServletResponse response) {
        String result = "";
        OutputStream outputStream = null;
        try {
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" + remoteFileName);
            outputStream = response.getOutputStream();
            LOGGER.info("Downloading file, remoteFileName:{}", remoteFileName);
            result = FTPUtils.downFile(remoteDir, remoteFileName, outputStream);
            response.flushBuffer();
            LOGGER.info("Downloaded file, remoteFileName:{}", remoteFileName);
        } catch (IOException e) {
            LOGGER.error("Download document error, file:{}, error:{}", remoteDir + "/" + remoteFileName, e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error("Closing outputStream error:{}", e);
            }
        }
        return result;
    }

    @RequestMapping(value = "/copy", method = RequestMethod.GET)
    @ResponseBody
    public String copyFileToDir(@RequestParam("sourceDir") String sourceDir,
                              @RequestParam("targetDir") String targetDir,
                              @RequestParam("sourceFileName") String sourceFileName) {
        String result = "FAIL";
        try {
            result = FTPUtils.copyFile(sourceDir, targetDir, sourceFileName);
        } catch (Exception e) {
            LOGGER.error("Copying file is fail! sourceDir:{}, targetDir:{}, sourceFileName:{}",
                    sourceDir, targetDir, sourceFileName, e);
        }
        return result;
    }
}
