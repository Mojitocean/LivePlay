package com.mg.service;

import java.util.Map;

/**
 * packageName com.mg.service
 *
 * @author mj
 * @className SimpleJson
 * @date 2025/11/19
 * @description TODO
 */
public class SimpleJson {
    public static Map<String, String> parse(String text) {
        // 这里用一个非常简单的解析，仅支持 flat 的 k/v，生产环境请换成稳健的解析器
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        String t = text.trim();
        t = t.replaceAll("^[{]", "").replaceAll("[}]$", "");
        String[] parts = t.split(",");
        for (String p : parts) {
            String[] kv = p.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replaceAll("^\"|\"$", "");
                String v = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(k, v);
            }
        }
        return map;
    }
}