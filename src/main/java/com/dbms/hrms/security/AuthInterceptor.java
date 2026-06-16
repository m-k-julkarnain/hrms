package com.dbms.hrms.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    public static final String SESSION_HR_USER_ID = "hrUserId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Allow login page, static resources, webjars and error page
        if (path.startsWith("/login") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_HR_USER_ID) != null) {
            return true; // authenticated
        }

        response.sendRedirect("/login");
        return false;
    }
}
