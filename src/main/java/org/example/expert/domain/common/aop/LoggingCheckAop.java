package org.example.expert.domain.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingCheckAop {

    private final ObjectMapper objectMapper;


    @Around("execution(* org.example.expert.domain..controller..*(..))")

    public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = requestAttributes.getRequest();

        ContentCachingRequestWrapper requestWrapper = null;

        if (request instanceof ContentCachingRequestWrapper) {
            requestWrapper = (ContentCachingRequestWrapper) request;
        }


        String requestBody;
        if (requestWrapper != null) {
            byte[] content = requestWrapper.getContentAsByteArray();
            requestBody = (content.length > 0) ? new String(content, StandardCharsets.UTF_8) : "값이 없습니다.";
        } else {
            requestBody = "값이 없습니다.";
        }

        Object userIdByJwt = request.getAttribute("userId");
        Long userId = (Long) userIdByJwt;

        LocalDateTime requestTime = LocalDateTime.now();

        String url = request.getRequestURI();

        log.info("user: {} time: {} url: {} requestBody: {}",
                userId, requestTime, url, requestBody);

        Object result = joinPoint.proceed();

        return result;
    }
}
