package com.jlu.ftp.bean;

/**
 * Created by niuwanpeng on 17/4/2.
 */
public enum FTPStatus {
    FAIL("FAIL"), SUCCESS("SUCCESS");

    private String value;

    private FTPStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
