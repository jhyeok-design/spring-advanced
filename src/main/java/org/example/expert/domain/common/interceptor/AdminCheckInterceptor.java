package org.example.expert.domain.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AdminCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Object admin = request.getAttribute("Admin");

        if (admin == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한없습니다.");
            return false;
        }

        String url = request.getRequestURI();
        log.info("url {} time {}", url, LocalDateTime.now());
        return true;
    }
}
