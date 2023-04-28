package com.example.demo.Controller;

import com.example.demo.service.OcrService;
import com.example.demo.utils.AnalysisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
    @CrossOrigin
    public String barCode (MultipartFile file) {
        checkParam(file);
        return ocrService.barCode(file);
    }

    @PostMapping("/businessLicense")
    @CrossOrigin
    public String businessLicense (MultipartFile file) {
        checkParam(file);
        return ocrService.businessLicenseRecognition(file);
    }

    @PostMapping("/financial")
    @CrossOrigin
    public String financial (MultipartFile file) {
        checkParam(file);
        return ocrService.financialStatements(file);
    }

    @PostMapping("/common")
    @CrossOrigin
    public String common (MultipartFile file) {
        checkParam(file);
        return ocrService.commonOcr(file);
    }

    @PostMapping("/serialNumber")
    @CrossOrigin
    public String serialNumberOcr (MultipartFile file) {
        String str = common(file);
        AnalysisUtils analysisUtils = new AnalysisUtils();
        String serialNumber = analysisUtils.serialNumberAnalysis(str);
        return serialNumber;
    }

    @PostMapping("/serialNumbers")
    @CrossOrigin
    public void serialNumberOcr (MultipartFile[] files) {
        for (MultipartFile multipartFile : files) {
            String str = common(multipartFile);
            AnalysisUtils analysisUtils = new AnalysisUtils();
            String serialNumber = analysisUtils.serialNumberAnalysis(str);
            System.out.println(multipartFile.getOriginalFilename() + " : " + serialNumber);
        }
    }

    private void checkParam (MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件数据为空");
        }
    }
}
