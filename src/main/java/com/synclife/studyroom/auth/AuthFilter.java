package com.synclife.studyroom.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AuthFilter implements Filter {
    public static final String ATTR = "AUTH_CTX";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;

        String uri = http.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") || uri.equals("/swagger-ui.html") || uri.startsWith("/docs")) {
            chain.doFilter(req, res);
            return;
        }

        AuthContext ctx = parse(http.getHeader("Authorization"));
        if (ctx != null) http.setAttribute(ATTR, ctx);
        else http.removeAttribute(ATTR);

        chain.doFilter(req, res);
    }

    private AuthContext parse(String header) {
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();
        if ("admin-token".equals(token)) return new AuthContext(Role.ADMIN, null);
        if (token.startsWith("user-token-")) {
            try {
                long uid = Long.parseLong(token.substring("user-token-".length()));
                return new AuthContext(Role.USER, uid);
            } catch (NumberFormatException ignore) {}
        }
        return null;
    }
}
