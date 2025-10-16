package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "redirect:/customer/index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/employee/index";
    }
}