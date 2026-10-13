package com.canteengo.config;

import com.canteengo.entity.User;
import com.canteengo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Token check: /api/auth/** and GET /api/foods are public, /api/admin/** needs ADMIN, the rest needs login. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserRepository users;

    public AuthInterceptor(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        String path = req.getRequestURI();
        if (path.startsWith("/api/auth/")) return true;
        if ("GET".equalsIgnoreCase(req.getMethod()) && (path.equals("/api/foods") || path.equals("/api/health") || path.equals("/health"))) return true;

        String header = req.getHeader("Authorization");
        User user = null;
        if (header != null && header.startsWith("Bearer ")) {
            user = users.findByToken(header.substring(7)).orElse(null);
        }
        if (user == null) {
            error(res, 401, "Please log in");
            return false;
        }
        if (path.startsWith("/api/admin/") && !"ADMIN".equals(user.getRole())) {
            error(res, 403, "Admin access only");
            return false;
        }
        req.setAttribute("user", user);
        return true;
    }

    private void error(HttpServletResponse res, int status, String msg) throws Exception {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"" + msg + "\"}");
    }
}
