package com.blog.utils;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;

public class LogRecordUtils {

    private LogRecordUtils() {
    }

    public static String getBrowser(String userAgentValue) {
        UserAgent userAgent = UserAgentUtil.parse(userAgentValue);
        if (userAgent == null || userAgent.getBrowser() == null) {
            return "未知浏览器";
        }
        return userAgent.getBrowser().getName();
    }

    public static String getOs(String userAgentValue) {
        UserAgent userAgent = UserAgentUtil.parse(userAgentValue);
        if (userAgent == null || userAgent.getOs() == null) {
            return "未知系统";
        }
        return userAgent.getOs().getName();
    }

    public static String getIpSource(String ip) {
        if (ip == null || ip.isBlank()) {
            return "未知来源";
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "本机地址";
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || isPrivate172Ip(ip)) {
            return "局域网IP";
        }
        return "公网IP";
    }

    private static boolean isPrivate172Ip(String ip) {
        if (!ip.startsWith("172.")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
