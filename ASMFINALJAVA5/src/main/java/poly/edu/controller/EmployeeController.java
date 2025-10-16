package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import poly.edu.service.AuthService;
import poly.edu.service.ParamService;

@Controller
@RequestMapping("/employee")
public class EmployeeController {
	
	@Autowired
	private AuthService authService;
	@Autowired
	private ParamService paramService;
	
	@GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (!authService.isAdmin()) {
            return "redirect:/employee/products";
        }
        
        model.addAttribute("title", "Dashboard - Thống kê");
        model.addAttribute("role", "employee");
        return "employee/NV_index";
    }

    @GetMapping("/products")
    public String products(Model model) { 
        model.addAttribute("title", "Quản lý sản phẩm");
        model.addAttribute("role", "employee");
        return "employee/NV_QLsanpham";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("title", "Quản lý đơn hàng");
        model.addAttribute("role", "employee");
        return "employee/NV_QLdonhang";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("title", "Quản lý người dùng");
        model.addAttribute("role", "employee");
        return "employee/NV_QLuser";
    }

    @GetMapping("/import")
    public String importStock(Model model) {
        model.addAttribute("title", "Nhập kho");
        model.addAttribute("role", "employee");
        return "employee/NV_NhapKho";
    }
}