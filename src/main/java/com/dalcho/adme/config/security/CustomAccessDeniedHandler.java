package com.dalcho.adme.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler { // 권한
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)throws IOException{
        //response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access is denied");
        response.sendRedirect("/error");
    }
}
