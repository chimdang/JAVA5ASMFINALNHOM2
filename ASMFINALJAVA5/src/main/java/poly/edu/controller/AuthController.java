package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;

import poly.edu.entity.Users;
import poly.edu.entity.KhachHang;
import poly.edu.dao.UsersDAO;
import poly.edu.dao.KhachHangDAO;

import poly.edu.service.AuthService;
import poly.edu.service.CookieService;
import poly.edu.service.ParamService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    
    @Autowired UsersDAO usersDAO;
    @Autowired KhachHangDAO khachHangDAO;
    @Autowired CookieService cookieService;
    @Autowired ParamService paramService;
    @Autowired AuthService authService;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {

        // Kiểm tra nếu đã đăng nhập thì redirect
        Users currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            return redirectByRole();
        }

        // Điền thông tin từ cookie vào form
        String credentials = cookieService.getValue("rememberMe");
        if (credentials != null && !credentials.isEmpty()) {
            try {
                String[] parts = credentials.split(":");
                if (parts.length == 2) {
                    model.addAttribute("savedMail", parts[0]);
                    model.addAttribute("savedPass", parts[1]);
                    model.addAttribute("rememberChecked", true);
                }
            } catch (Exception e) {
                // Không cần xử lý
            }
        }

        model.addAttribute("title", "Đăng nhập & Đăng ký - Pine Shop");
        model.addAttribute("role", "auth");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(Model model) {
    	
    	String mail = paramService.getString("mail", "");
        String pass = paramService.getString("pass", "");
        boolean remember = paramService.getBoolean("remember", false);

        String result = authService.login(mail, pass, remember);
        if (!result.equals("OK")) {
            model.addAttribute("error", result);
            model.addAttribute("savedMail", mail);
            return "auth/login";
        }

        // Kiểm tra và redirect dựa trên vai trò
        if (authService.isAdmin()) {
            return "redirect:/employee/dashboard";
        } else if (authService.isEmployee()) {
            return "redirect:/employee/products";
        } else if (authService.isCustomer()) {          
            return "redirect:/customer/index";
        }
        
        return "redirect:/customer/index";
    }
    
   
    @PostMapping("/register")
    public String register(Model model) {
    	String mail = paramService.getString("mail", "");
        String pass = paramService.getString("pass", "");
        String fullname = paramService.getString("fullname", "");
        String phone = paramService.getString("phone", "");
        boolean remember = paramService.getBoolean("remember", false);
    	
        // Kiểm tra email đã tồn tại chưa
        if (usersDAO.findByMail(mail) != null) {
            model.addAttribute("error", "Email đã tồn tại!");
            return "auth/login";
        }

        try {
            // 1. Tạo tài khoản Users
            Users user = new Users();
            user.setMail(mail);
            user.setPass(pass);
            Users savedUser = usersDAO.save(user);

            // 2. Tạo thông tin KhachHang
            KhachHang khachHang = new KhachHang();
            khachHang.setTenKH(fullname);
            khachHang.setSdt(phone);
            khachHang.setUser(savedUser);
            
            khachHangDAO.save(khachHang);
            // 3. Tự động đăng nhập sau khi đăng ký
            authService.login(mail, pass, remember);
            
            model.addAttribute("message", "Đăng ký thành công!");          
            return redirectByRole();
            
        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại! Vui lòng thử lại.");
        }

        return "auth/login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {

        // 1. Xóa session
        authService.logout();
        
        // 2. Xóa cookie rememberMe
        Cookie cookie = new Cookie("rememberMe", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/auth/login?logout=true";
    }

    private String redirectByRole() {
    	 if (authService.isAdmin()) {
             return "redirect:/employee/dashboard";
         } else if (authService.isEmployee()) {
             return "redirect:/employee/products";
         } else if (authService.isCustomer()) {          
             return "redirect:/customer/index";
         }
        return "redirect:/customer/index";
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        String result = authService.forgotPassword(email);
        
        if (result.equals("OK")) {
            model.addAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.");
        } else {
            model.addAttribute("error", result);
        }
        
        return "auth/login";
    }
}