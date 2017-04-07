package com.jlu.ftp.web;

import com.jlu.ftp.service.FTPUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by niuwanpeng on 17/4/2.
 *
 * 开放接口 controller
 */
@Controller
@RequestMapping(("/ftp/api"))
public class ApiController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(@RequestParam("remoteDir") String remoteDir,
                             @RequestParam("remoteFileName") String remoteFileName,
                             @RequestParam("file") MultipartFile file) {
        return FTPUtils.uploadFileByInputStream(remoteDir, remoteFileName, file);
    }

    /**
     * 返回下载产出的url
     * @param remoteDir
     * @param remoteFileName
     * @return
     */
    @RequestMapping(value = "/downloadUrl", method = RequestMethod.GET)
    @ResponseBody
    public String downloadFile(@RequestParam("remoteDir") String remoteDir,
                             @RequestParam("remoteFileName") String remoteFileName) {
        return FTPUtils.getFileHTTPUrlByFileName(remoteDir, remoteFileName);
    }
}
