package com.blog.controller.web;

import com.blog.common.result.Result;
import com.blog.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "天气接口")
@Slf4j
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisService redisService;

    public WeatherController(RedisService redisService) {
        this.redisService = redisService;
        // 设置超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);  // 连接超时30秒
        factory.setReadTimeout(30000);     // 读取超时30秒
        this.restTemplate = new RestTemplate(factory);
    }

    @Operation(summary = "获取实时天气")
    @GetMapping
    @SuppressWarnings("unchecked")
    public Result<?> getWeather(HttpServletRequest request) {
        try {
            Map<String, Object> result = new HashMap<>();
            String cityName = "北京";  // 默认城市
            
            // 获取用户真实IP
            String clientIp = getClientIp(request);
            log.info("用户IP: {}", clientIp);
            
            // 1. 先通过IP获取中文城市名
            try {
                String ipUrl = "https://whois.pconline.com.cn/ipJson.jsp?ip=" + clientIp + "&json=true";
                String ipResponse = restTemplate.getForObject(ipUrl, String.class);
                if (ipResponse != null) {
                    Map ipData = objectMapper.readValue(ipResponse, Map.class);
                    String city = (String) ipData.get("city");
                    String province = (String) ipData.get("pro");
                    if (city != null && !city.isEmpty()) {
                        cityName = city;
                    } else if (province != null && !province.isEmpty()) {
                        cityName = province;
                    }
                }
            } catch (Exception e) {
                log.warn("获取IP定位失败，使用默认位置: {}", e.getMessage());
            }
            
            // 2. 先从缓存获取天气数据
            String cacheKey = RedisService.CACHE_WEATHER + cityName;
            Object cached = redisService.get(cacheKey);
            if (cached instanceof Map) {
                log.info("从缓存获取天气数据: {}", cityName);
                return Result.success(cached);
            }
            
            result.put("city", cityName);
            
            // 3. 获取天气数据 - 使用国内可访问的API
            String response = null;
            
            // 尝试使用 wttr.in
            try {
                String weatherUrl = "https://wttr.in/" + URLEncoder.encode(cityName, StandardCharsets.UTF_8) + "?format=j1&lang=zh";
                response = restTemplate.getForObject(weatherUrl, String.class);
            } catch (ResourceAccessException e) {
                log.warn("wttr.in 连接超时，返回默认天气数据");
                return getDefaultWeather(cityName);
            } catch (HttpClientErrorException e) {
                // wttr.in 有时返回404但body里有数据
                String body = e.getResponseBodyAsString();
                if (body != null && !body.isEmpty() && body.contains("current_condition")) {
                    response = body;
                    log.info("wttr.in返回{}但包含有效数据，继续处理", e.getStatusCode());
                } else {
                    log.warn("获取天气数据失败，状态码: {}，返回默认数据", e.getStatusCode());
                    return getDefaultWeather(cityName);
                }
            } catch (HttpServerErrorException e) {
                log.warn("天气服务器错误: {}，返回默认数据", e.getStatusCode());
                return getDefaultWeather(cityName);
            }
            
            if (response == null || response.isEmpty()) {
                return getDefaultWeather(cityName);
            }
            
            // 检查响应是否为有效JSON
            if (!response.trim().startsWith("{")) {
                log.warn("天气API返回非JSON数据，返回默认数据");
                return getDefaultWeather(cityName);
            }
            
            Map data = objectMapper.readValue(response, Map.class);
            
            // 解析天气数据
            List<Map> currentCondition = (List<Map>) data.get("current_condition");
            if (currentCondition == null || currentCondition.isEmpty()) {
                log.warn("天气数据中没有current_condition字段");
                return getDefaultWeather(cityName);
            }
            
            Map current = currentCondition.get(0);
            result.put("temp", current.get("temp_C"));
            result.put("feelsLike", current.get("FeelsLikeC"));
            result.put("humidity", current.get("humidity"));
            result.put("windSpeed", current.get("windspeedKmph"));
            result.put("windDir", current.get("winddir16Point"));
            result.put("weatherCode", current.get("weatherCode"));
            
            // 获取天气描述
            List<Map> weatherDesc = (List<Map>) current.get("lang_zh");
            if (weatherDesc != null && !weatherDesc.isEmpty()) {
                result.put("text", weatherDesc.get(0).get("value"));
            } else {
                List<Map> weatherDescEn = (List<Map>) current.get("weatherDesc");
                if (weatherDescEn != null && !weatherDescEn.isEmpty()) {
                    result.put("text", weatherDescEn.get(0).get("value"));
                } else {
                    result.put("text", "未知");
                }
            }
            
            // 缓存30分钟
            redisService.setWithMinutes(cacheKey, result, RedisService.EXPIRE_MEDIUM);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
            return getDefaultWeather("北京");
        }
    }
    
    /**
     * 返回默认天气数据（当API不可用时）
     */
    private Result<?> getDefaultWeather(String cityName) {
        Map<String, Object> result = new HashMap<>();
        result.put("city", cityName);
        result.put("temp", "--");
        result.put("feelsLike", "--");
        result.put("humidity", "--");
        result.put("windSpeed", "--");
        result.put("windDir", "--");
        result.put("weatherCode", "113");
        result.put("text", "暂无数据");
        return Result.success(result);
    }
    
    /**
     * 获取用户真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个IP，取第一个
                int index = ip.indexOf(',');
                if (index > 0) {
                    ip = ip.substring(0, index).trim();
                }
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
