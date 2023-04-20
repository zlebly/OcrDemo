package com.example.demo.utils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author douwenjie
 * @create 2023-04-07
 */
public class FileUtil {
    public static FileItem createFileItem(String path){
        File file = new File(path);
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem("file", "text/plain", false,file.getName());
        File newfile = new File(path);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try (FileInputStream fis = new FileInputStream(newfile);
             OutputStream os = item.getOutputStream()) {
            while ((bytesRead = fis.read(buffer, 0, 8192))!= -1)
            {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    public static MultipartFile getMultipartFile (String path) {
        File file = new File(path);
        FileItem item = new DiskFileItemFactory().createItem("file",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                true,
                file.getName());
        FileInputStream input;
        OutputStream os;
        try{
            input = new FileInputStream(file);
            os = item.getOutputStream();
            IOUtils.copy(input, os);
            os.flush();
            os.close();
            input.close();
        } catch (IOException e) {
            throw new RuntimeException("Invalid file:" + e, e);
        }
        return new CommonsMultipartFile(item);
    }

    /**
     * 根据文件路径获取Base64编码
     * @param path
     * @return
     */
    public static String getFileBase64Encode (String path) {
        byte[] b;
        try {
            b = getMultipartFile(path).getBytes();
        } catch (IOException e) {
            throw new RuntimeException("获取文件字节失败");
        }
        if (b != null) {
            return new BASE64Encoder().encode(b);
        }
        return null;
    }

    public static String setRecgnPlainParam(byte[] bytes, String type, String option, String password) {
        String imgBase64Str = getBase64(bytes);
        return imgBase64Str + "==##" + type + "==##" + option + "==##" + password;
    }
    public static String getBase64(byte[] bytes) {
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        return encoder.encodeToString(bytes);
    }
}
