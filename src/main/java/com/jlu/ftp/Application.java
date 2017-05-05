package com.jlu.ftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.MultipartConfigElement;

/**
 * Run FTP Service !
 */
@RestController
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.jlu.ftp")
public class Application implements EmbeddedServletContainerCustomizer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);

    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {
        configurableEmbeddedServletContainer.setPort(8999);
    }

    @Bean
    public MultipartConfigElement createMultipartConfigElement()
    {
        MultipartConfigFactory mcf = new MultipartConfigFactory();
        /**
         * 设置最大上传文件的大小，默认是1000MB
         */
        mcf.setMaxFileSize("1000MB");
        return mcf.createMultipartConfig();
    }
}
