package com.maomao.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author starsea
 * @date 2022-02-05
 */
public interface FileService {

    /**
     * 上传文件到阿里云oss
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}
