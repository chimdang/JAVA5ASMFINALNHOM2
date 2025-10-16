package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Đăng nhập & Đăng ký - Pine Shop");
        model.addAttribute("role", "auth"); // Không có header cho auth
        return "auth/login";
    }
}