package com.example.demo.service.impl;

import com.example.demo.constant.HttpConstants;
import com.example.demo.service.OcrService;
import com.example.demo.utils.AnalysisUtils;
import com.example.demo.utils.FileUtil;
import com.example.demo.utils.HttpUtils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author douwenjie
 * @create 2023-04-13
 */
@Service
public class OcrServiceImpl implements OcrService {
    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);

    @Value("${ocr.url}")
    private String ocrUrl;

    @Value("${cache.file}")
    private String tmpFilePath;

    @Override
    public String barCode(MultipartFile file) {
        String result = "";
        try {
            JSONObject json = new JSONObject(true);
            json.put("username", "test");
            json.put("imgbase64", new BASE64Encoder().encode(file.getBytes()));
            json.put("typeid","2006");
            json.put("imgtype","jpg");
            json.put("left", 0);
            json.put("right", 0);
            json.put("top", 0);
            json.put("bottom", 0);
            result = AnalysisUtils.barCodeJsonAnalysis(
                    HttpUtils.doPost(ocrUrl + HttpConstants.DO_BARCODE_RECON,
                            json, MediaType.APPLICATION_JSON_VALUE));
            logger.debug("条形码解析如下:[{}]", result);
        } catch (JSONException | IOException e) {
            throw new RuntimeException("识别条形码失败:[{}]", e);
        }
        return result;
    }

    @Override
    public String businessLicenseRecognition(MultipartFile file) {
        String result;
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("username", "test");
            map.put("LGId", "5");
            map.put("LGReserve", "");
            result = AnalysisUtils.licenseJsonAnalysis(
                    HttpUtils.doPost(ocrUrl + HttpConstants.DOLG_STATEMENT_RECON, map, file));
            logger.debug("营业执照解析如下:[{}]", result);
        } catch (JSONException e) {
            throw new RuntimeException("识别营业执照失败:[{}]", e);
        }
        return result;
    }

    @Override
    public String financialStatements(MultipartFile file) {
        String result;
        try {
            String[] filename = file.getOriginalFilename().split("\\.");
            String id =  UUID.randomUUID().toString();
            String filePath = tmpFilePath + System.getProperty("path.separator") + id + "." + filename[1];
            File tmpFile = new File(filePath);
            FileUtils.copyInputStreamToFile(file.getInputStream(), tmpFile);
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("username", "test")
                    .addFormDataPart("files", filePath,
                            RequestBody.create(okhttp3.MediaType.parse("application/octet-stream"),
                                    tmpFile))
                    .addFormDataPart("typeid", "2005")
                    .build();
            String jsonStr = HttpUtils.doPost(ocrUrl + HttpConstants.DO_FIN_RECON, body);

            result = AnalysisUtils.financialJsonAnalysis(jsonStr);
            logger.debug("财务报表解析如下:[{}]", result);
            tmpFile.deleteOnExit();

        } catch (JSONException | IOException e) {
            throw new RuntimeException("识别财务报表失败:[{}]", e);
        }
        return result;
    }

    @Override
    public String commonOcr(MultipartFile file) {
        String typeid = "8999";
        JSONObject json = new JSONObject(true);
        String username = "test";
        String result;
        try {
            String strsrc = FileUtil.setRecgnPlainParam(file.getBytes(), typeid, "", null);
            String imgtype = file.getOriginalFilename().split("\\.")[1];
            json.put("username", username);
            json.put("paramdata", strsrc);
            json.put("signdata", "NULL");
            json.put("imgtype", imgtype);
            result = AnalysisUtils.pdfJsonAnalysis(
                    HttpUtils.doPost(ocrUrl + HttpConstants.DO_ALL_CARD_RECON, json, MediaType.APPLICATION_JSON_VALUE));
        } catch (JSONException | IOException e) {
            throw new RuntimeException("识别文件失败:[{}]", e);
        }
        return result;
    }
}
