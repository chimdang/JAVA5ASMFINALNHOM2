package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;
import poly.edu.service.AuthService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();
        
        if (uri.startsWith("/auth/") || 
        	uri.equals("/customer/index")) {
        	return true;
        }

        // Kiểm tra nếu chưa đăng nhập, thử đăng nhập tự động từ cookie
        if (authService.getCurrentUser() == null) {
            boolean autoLoggedIn = authService.autoLoginFromCookie();
            if (!autoLoggedIn) {
                res.sendRedirect("/auth/login");
                return false;
            }
        }

        // Kiểm tra quyền truy cập employee routes
        if (uri.startsWith("/employee")) {
            if (!authService.isEmployee()) {
                res.sendRedirect("/auth/login");
                return false;
            }
            
            if (uri.equals("/employee/dashboard") && !authService.isAdmin()) {
                res.sendRedirect("/employee/products");
                return false;
            }
            return true; 
        }

        // Kiểm tra quyền truy cập customer routes (trừ trang chủ)
        if (uri.startsWith("/customer") && !uri.equals("/customer/index")) {
            if (!authService.isCustomer()) {
                res.sendRedirect("/auth/login");
                return false;
            }
        }
    return true;
    }
}