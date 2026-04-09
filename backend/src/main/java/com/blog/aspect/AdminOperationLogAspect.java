package com.blog.aspect;

import cn.hutool.json.JSONUtil;
import com.blog.common.result.Result;
import com.blog.entity.OperationLog;
import com.blog.security.SecurityUser;
import com.blog.service.LogService;
import com.blog.utils.IpUtils;
import com.blog.utils.LogRecordUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminOperationLogAspect {

    private final LogService logService;

    @Around("within(com.blog.controller.admin..*)")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest();
        if (request == null || shouldSkip(request)) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        OperationLog operationLog = buildOperationLog(joinPoint, request);

        try {
            Object result = joinPoint.proceed();
            operationLog.setStatus(resolveStatus(result));
            operationLog.setResponseData(truncate(serializeObject(result), 2000));
            return result;
        } catch (Throwable e) {
            operationLog.setStatus(0);
            operationLog.setResponseData(truncate(e.getMessage(), 2000));
            throw e;
        } finally {
            operationLog.setCostTime(System.currentTimeMillis() - startTime);
            logService.saveOperationLog(operationLog);
        }
    }

    private OperationLog buildOperationLog(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(getCurrentUserId(request));
        operationLog.setModule(resolveModule(targetClass));
        operationLog.setDescription(resolveDescription(method));
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setRequestParams(truncate(serializeArgs(joinPoint.getArgs()), 2000));

        String ip = IpUtils.getIpAddress(request);
        operationLog.setIp(ip);
        operationLog.setIpSource(LogRecordUtils.getIpSource(ip));
        return operationLog;
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/admin/")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return uri.startsWith("/api/admin/logs")
                || "/api/admin/auth/login".equals(uri)
                || "/api/admin/auth/info".equals(uri)
                || "/api/admin/auth/menus".equals(uri);
    }

    private String resolveModule(Class<?> targetClass) {
        Tag tag = targetClass.getAnnotation(Tag.class);
        if (tag != null && tag.name() != null && !tag.name().isBlank()) {
            return tag.name();
        }
        return targetClass.getSimpleName();
    }

    private String resolveDescription(Method method) {
        Operation operation = method.getAnnotation(Operation.class);
        if (operation != null && operation.summary() != null && !operation.summary().isBlank()) {
            return operation.summary();
        }
        return method.getName();
    }

    private Integer resolveStatus(Object result) {
        if (result instanceof Result<?> response) {
            return response.getCode() != null && response.getCode() == 200 ? 1 : 0;
        }
        return 1;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object currentUser = request.getUserPrincipal();
        if (currentUser instanceof SecurityUser securityUser) {
            return securityUser.getUser().getId();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null
                ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUser().getId();
        }
        return null;
    }

    private String serializeArgs(Object[] args) {
        List<Object> payload = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse) {
                continue;
            }
            if (arg instanceof MultipartFile file) {
                payload.add(file.getOriginalFilename());
                continue;
            }
            if (arg instanceof MultipartFile[] files) {
                List<String> fileNames = new ArrayList<>();
                for (MultipartFile file : files) {
                    fileNames.add(file.getOriginalFilename());
                }
                payload.add(fileNames);
                continue;
            }
            payload.add(arg);
        }
        return serializeObject(payload);
    }

    private String serializeObject(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return JSONUtil.toJsonStr(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
