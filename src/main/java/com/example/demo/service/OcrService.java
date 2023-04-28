package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author douwenjie
 * @create 2023-04-13
 */
public interface OcrService {
    /**
     * 识别条形码
     * @param path 条形码文件
     * @return 识别结果
     */
    String barCode(MultipartFile path);

    /**
     * 识别营业执照
     * @param file 营业执照文件
     * @return 识别结果
     */
    String businessLicenseRecognition(MultipartFile file);

    /**
     * 识别财务报表
     * @param file 财务报表文件
     * @return 识别结果
     */
    String financialStatements(MultipartFile file);

    /**
     * 通用识别(PDF, 图片, 身份证)
     * @param file 文件
     * @return 识别结果
     */
    String commonOcr(MultipartFile file);
}
