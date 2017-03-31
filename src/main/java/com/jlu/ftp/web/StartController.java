package com.jlu.ftp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by niuwanpeng on 17/3/31.
 */
@Controller
public class StartController {

    @RequestMapping("/")
    @ResponseBody
    public String monitor() {
        return "OK";
    }
}
