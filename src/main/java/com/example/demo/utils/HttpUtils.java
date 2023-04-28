package com.example.demo.utils;

import okhttp3.*;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author douwenjie
 * @create 2023-04-07
 */
public class HttpUtils {
    public HttpUtils() {
    }

    public static String doPost(String url, JSONObject json, String mediaType) {
        String result = "";
        // 1.创建httpclient对象
        CloseableHttpClient client = HttpClientBuilder.create().build();
        // 2.通过url创建post方法
        HttpPost post = new HttpPost(url);
        try {
            // 3.将传入的json封装成实体，并压入post方法
            StringEntity entity = new StringEntity(json.toString());
            entity.setContentEncoding("UTF-8");
            // 发送json数据需要设置contentType
            if (mediaType != null) {
                entity.setContentType(mediaType);
            } else {
                entity.setContentType(MediaType.APPLICATION_JSON_VALUE);
            }
            post.setEntity(entity);
            // 4.执行post方法，返回HttpResponse的对象
            CloseableHttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
                // 5.如果返回结果状态码为200，则读取响应实体response对象的实体内容，并封装成String对象返回
            }
            try {
                HttpEntity e = response.getEntity();
                // 6.关闭资源
                if (e != null) {
                    InputStream instream = e.getContent();
                    instream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 用于key-value参数请求,传输from-data数据
     *
     * @param url       url
     * @param params    params
     * @return String
     */
    public static <T> String doPost(String url, HashMap<String, String> params, T fileParam) {
        String result = "";
        CloseableHttpClient client =  HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(java.nio.charset.Charset.forName("UTF-8"));
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (fileParam instanceof MultipartFile[]){
                MultipartFile[] multipartFiles = (MultipartFile[]) fileParam;
                for (MultipartFile file: multipartFiles) {
                    builder.addBinaryBody("files", file.getBytes(), ContentType.MULTIPART_FORM_DATA, file.getName());
                }
            } else if (fileParam instanceof MultipartFile)  {
                MultipartFile file = (MultipartFile) fileParam;
                builder.addBinaryBody("file", file.getBytes(), ContentType.MULTIPART_FORM_DATA, file.getName());
            }

            //表单中参数
            for(Map.Entry<String, String> entry: params.entrySet()) {
                builder.addTextBody(
                        entry.getKey(),entry.getValue(), ContentType.create("text/plain", Consts.UTF_8));
            }
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            // 4.执行post方法，返回HttpResponse的对象
            CloseableHttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
                // 5.如果返回结果状态码为200，则读取响应实体response对象的实体内容，并封装成String对象返回
            }
            try {
                HttpEntity e = response.getEntity();
                // 6.关闭资源
                if (e != null) {
                    InputStream instream = e.getContent();
                    instream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 用于key-value参数请求,传输from-data数据
     *
     * @param url       url
     * @param body    params
     * @return String
     */
    public static String doPost(String url, RequestBody body) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Response response = null;
        String result;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .build();
            response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            throw new IOException();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return result;
    }
}
