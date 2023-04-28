package com.example.demo.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

/**
 * @author douwenjie
 * @create  2023-04-06
 */
public class AnalysisUtils {
    private static final List<String> BUSINESS_LICENSE = Arrays.asList("统一社会信用代码", "名称", "类型", "住所",
            "法定代表人", "注册资本", "成立日期", "营业期限", "经营范围", "登记日期", "二维码");

    private static final String TABLE_WIDE = "                    ";

    private static final List<String> ID_CARD = Arrays.asList("姓名", "性别", "民族", "出生", "住址");

    /**
     * 解析PDF-JSON数据
     * @param jsonStr jsonStr
     * @return String String
     * @throws JSONException JSONException
     */
    public static String pdfJsonAnalysis(String jsonStr) throws JSONException {
        JSONObject dataJson = JSONObject.parseObject(jsonStr).getJSONObject("data");
        JSONObject messageJson = dataJson.getJSONObject("message");
        if (messageJson == null) {
            throw new RuntimeException("识别失败");
        }
        if (Integer.parseInt(messageJson.get("status").toString()) < 0) {
            throw new RuntimeException(messageJson.get("value").toString());
        }
        JSONArray rowItemJson =
                dataJson.getJSONObject("cardsinfo")
                        .getJSONObject("card")
                        .getJSONArray("rowitem");
        JSONObject jsonObject = new JSONObject(true);
        String key = "";
        String value = "";
        List<String> idCardKey = new ArrayList<>();
        for (int i = 0; i < rowItemJson.size() - 1; i++) {
            String rowItem = getRowItem (rowItemJson, i);
            if (rowItem.contains("性别") && rowItem.length() > 2) {
                jsonObject.put("性别", rowItem.substring(2, rowItem.length()));
                idCardKey.add("性别");
                continue;
            }
            if (rowItem.contains("民族") && rowItem.length() > 2) {
                jsonObject.put("民族", rowItem.substring(2, rowItem.length()));
                idCardKey.add("民族");
                continue;
            }
            if (Strings.isEmpty(key)) {
                key = rowItem;
                idCardKey.add(key);
                continue;
            }
            if (!Strings.isEmpty(key) && Strings.isEmpty(value)) {
                value = rowItem;
                if (idCardKey.equals(ID_CARD) && i < rowItemJson.size() - 1) {
                    rowItem = getRowItem (rowItemJson, ++i);
                    while (!"公民身份号码".equals(rowItem)) {
                        value += rowItem;
                        rowItem = getRowItem (rowItemJson, ++i);
                    }
                    --i;
                }
                jsonObject.put(key, value);
                key = "";
                value = "";
            }
        }
        return jsonObject.toString();
    }

    /**
     * 解析图片-证书数据
     * @param jsonStr jsonStr
     * @return String String
     * @throws JSONException JSONException
     */
    public static String licenseJsonAnalysis(String jsonStr) throws JSONException {
        checkReturnCode(jsonStr);
        JSONObject infoJson = JSONObject.parseObject(jsonStr).getJSONObject("info");
        JSONObject jsonObject = new JSONObject(true);
        for (String str : BUSINESS_LICENSE) {
            jsonObject.put(str, infoJson.getString(str));
        }
        return jsonObject.toString();
    }

    /**
     * 解析图片-报表数据
     * @param jsonStr jsonStr
     * @return String String
     * @throws JSONException JSONException
     */
    public static String financialJsonAnalysis(String jsonStr) throws JSONException {
        JSONObject dataJson = JSONObject.parseObject(jsonStr).getJSONObject("data");
        checkReturnCode(dataJson.toString());
        JSONObject fsResultJson = dataJson
                .getJSONObject("fsocrinfo")
                .getJSONArray("fsResult")
                .getJSONObject(0);

        StringBuffer stringBuffer = new StringBuffer();
        // 获取表格名
        stringBuffer.append(fsResultJson.getJSONObject("commonInfo").getString("statementName"))
                .append(System.getProperty("line.separator"));
        // 获取表头
        JSONArray mainHeader = fsResultJson.getJSONObject("formHeaderInfo")
                .getJSONObject("mainHeaderResultInfo")
                .getJSONArray("mainHeader");
        for (int i = 0; i < mainHeader.size(); i++) {
            String name = mainHeader.getJSONObject(i).getString("name");
            stringBuffer.append(name)
                    .append(getSpace(name));
        }
        stringBuffer.append(System.getProperty("line.separator"));
        // 获取表格内容
        JSONArray formContentInfo = fsResultJson.getJSONObject("formContentInfo")
                .getJSONArray("cellInfo");
        HashMap<String, String> rowColumnValue = new HashMap<>(100);
        int rowMax = 0;
        int columnMax = 0;
        for (int i = 0; i < formContentInfo.size(); i++) {
            JSONObject cellInfo = formContentInfo.getJSONObject(i);
            JSONArray subCellInfos = cellInfo.getJSONArray("subCellInfo");
            for (int j = 0; j < subCellInfos.size(); j++) {
                JSONObject subCellInfo = subCellInfos.getJSONObject(j);
                int row = subCellInfo.getInteger("nRowIndex");
                int column = subCellInfo.getInteger("nColumnIndex");
                String rowColumn = row + ":" + column;
                rowColumnValue.put(rowColumn, subCellInfo.getString("result"));
                if (rowMax < row) {
                    rowMax = row;
                }
                if (columnMax < column) {
                    columnMax = column;
                }
            }
        }
        int row = 1;
        int column = 0;
        for (int i = 0; i < rowMax * (columnMax + 1); i ++) {
            if (rowColumnValue.containsKey(row + ":" + column)) {
                stringBuffer.append(rowColumnValue.get(row + ":" + column))
                        .append(getSpace(rowColumnValue.get(row + ":" + column)));

            } else {
                stringBuffer.append(getSpace(""));
            }
            if (column == columnMax) {
                stringBuffer.append(System.getProperty("line.separator"));
                row ++;
                column = 0;
            } else {
                column++;
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 解析图片-条形码数据
     * @param jsonStr jsonStr
     * @return String String
     * @throws JSONException JSONException
     */
    public static String barCodeJsonAnalysis(String jsonStr) throws JSONException {
        JSONObject dataJson = JSONObject.parseObject(jsonStr).getJSONObject("data");
        checkReturnCode(dataJson.toString());
        JSONObject itemJson = dataJson.getJSONObject("info").getJSONArray("item").getJSONObject(0);
        JSONObject result = new JSONObject(true);
        result.put("识别结果", itemJson.getString("barcode"));
        return result.toString();
    }


    private static void checkReturnCode(String jsonStr) throws JSONException {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr).getJSONObject("message");
        if (jsonObject == null) {
            throw new RuntimeException("识别失败");
        }
        if (Integer.parseInt(jsonObject.get("status").toString()) < 0) {
            throw new RuntimeException(jsonObject.get("value").toString());
        }
    }

    private static String getSpace(String str) {
        if (Strings.isEmpty(str)) {
            return TABLE_WIDE;
        }
        return TABLE_WIDE.substring(str.length());
    }

    public String serialNumberAnalysis(String ocrStr) {
        String serialNumber = "";
        try {
            JSONObject jsonObject = JSONObject.parseObject(ocrStr);
//            Map.Entry<String, String> entry = (Map.Entry<String, String>) ;
            List<String> candidates = new ArrayList<>();
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                String value = (String) entry.getValue();
                if (key.length() >= 19 && key.contains("32")) {
                    String candidate = key.substring(key.indexOf("32"));
                    if (candidate.length() >= 19) {
                        candidates.add(candidate);
                    }
                }
                if (value.length() >= 19 && value.contains("32")) {
                    String candidate = value.substring(value.indexOf("32"));
                    if (candidate.length() >= 19) {
                        candidates.add(candidate);
                    }
                }
            }
            for (String candidate : candidates) {
                for (int i = 2; i < candidate.length(); i++) {
                    char ch = candidate.charAt(i);
                    if (i < 19 && !Character.isLetterOrDigit(ch)) {
                        break;
                    }
                    if (i == 18 && candidate.length() == 19) {
                        serialNumber = candidate;
                        break;
                    }
                    if (i == 19 && !Character.isLetterOrDigit(ch)) {
                        serialNumber = candidate.substring(0, 19);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException();
        }
        return serialNumber;
    }

    public String qrBarAnalysis(String barCodeData) {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("二维码", JSONObject.parseObject(barCodeData).getString("二维码"));
        return jsonObject.toString();
    }

    private static String getRowItem (JSONArray rowItemJson, int i) throws JSONException{
        return  rowItemJson
                .getJSONObject(i)
                .getJSONObject("rowContext")
                .getJSONArray("charitem")
                .getJSONObject(0)
                .getString("charValue");
    }
}