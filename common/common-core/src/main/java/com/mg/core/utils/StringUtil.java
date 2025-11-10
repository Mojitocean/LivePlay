package com.mg.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * packageName com.mg.core.utils
 *
 * @author mj
 * @className StringUtil
 * @date 2025/5/27
 * @description TODO
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil extends StringUtils {
    /**
     * 特殊截取字符串
     *
     * @param input 原始字符串
     * @return 返回符号前的子串，不包含符号，如果未找到特殊符号则返回原字符串
     */
    public static String substringFirst(String input, String symbol) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int index = input.indexOf(symbol);
        if (index == -1) {
            return "";
        }
        return input.substring(0, index);
    }

    /**
     * 特殊截取字符串
     *
     * @param input 原始字符串
     * @return 返回符号后的子串，包含符号，如果未找到特殊符号则返回空字符串
     */
    public static String substringLast(String input, String symbol) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int index = input.lastIndexOf(symbol);
        if (index == -1 || index == 0 || index == input.length() - 1) {
            return "";
        }
        return input.substring(index, input.length());
    }

    /**
     * 特殊截取字符串
     *
     * @param input 原始字符串
     * @return 返回符号后的子串，包含符号，如果未找到特殊符号则返回空字符串
     */
    public static String substringLast2(String input, String symbol) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int index = input.lastIndexOf(symbol);
        if (index == -1 || index == 0 || index == input.length() - 1) {
            return "";
        }
        return input.substring(index + 1, input.length());
    }


}