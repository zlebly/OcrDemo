package com.example.demo.Controller;

import com.example.demo.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author douwenjie
 * @create 2023-04-11
 */
@RequestMapping("/ocr")
@RestController
public class OcrController {
    @Autowired
    OcrService ocrService;

    @PostMapping("/barCode")
    public String barCode (MultipartFile file) {
        checkParam(file);
        return ocrService.barCode(file);
    }

    @PostMapping("/businessLicense")
    public String businessLicense (MultipartFile file) {
        checkParam(file);
        return ocrService.businessLicenseRecognition(file);
    }

    @PostMapping("/financial")
    public String financial (MultipartFile file) {
        checkParam(file);
        return ocrService.financialStatements(file);
    }

    @PostMapping("/common")
    public String common (MultipartFile file) {
        checkParam(file);
        return ocrService.commonOcr(file);
    }


    private void checkParam (MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件数据为空");
        }
    }
}
