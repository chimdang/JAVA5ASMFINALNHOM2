package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.List;

import poly.edu.entity.*;
import poly.edu.dao.*;
import poly.edu.service.AuthService;
import poly.edu.service.ParamService;
import poly.edu.service.SessionService;


@Controller
@RequestMapping("/customer")
public class CustomerController {
	 @Autowired KhachHangDAO khachHangDAO;
	 @Autowired DiaChiDAO diaChiDAO;
	 @Autowired UsersDAO usersDAO;
	 @Autowired SessionService sessionService;
	 @Autowired ParamService paramService;
	 @Autowired AuthService authService;
	
    @GetMapping("/index")
    public String customerIndex(Model model) {
        model.addAttribute("title", "Pine Shop - Trang chủ");
        model.addAttribute("role", "customer");
        return "customer/KH_index";
    }
    
    @GetMapping("/detailProduct")
    public String detailProduct(Model model) {
        model.addAttribute("title", "Chi tiết đơn hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_detail-product";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("title", "Giỏ hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_GioHang";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("title", "Đơn hàng của bạn");
        model.addAttribute("role", "customer");
        return "customer/KH_QLDonHang";
    }

    @GetMapping("/order/detail")
    public String orderDetail(Model model) {
        model.addAttribute("title", "Chi tiết đơn hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_CTDonHang";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("title", "Đặt hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_DatHang";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/auth/login";
            }

            // Lấy thông tin khách hàng
            KhachHang customer = khachHangDAO.findByUser_UserID(currentUser.getUserID());
            if (customer == null) {
                model.addAttribute("error", "Không tìm thấy thông tin khách hàng");
                return "customer/KH_QLuser";
            }
            
            model.addAttribute("customer", customer);

            // Lấy danh sách địa chỉ
            List<DiaChi> addresses = diaChiDAO.findByMaKH(customer.getMaKH());
            model.addAttribute("addresses", addresses);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải thông tin: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "customer/KH_QLuser";
    }

    @PostMapping("/update-profile")
    public String updateProfile(RedirectAttributes redirectAttributes) {
    	String fullname = paramService.getString("fullname", "");
        String phone = paramService.getString("phone", "");
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/auth/login";
            }
            
            KhachHang customer = khachHangDAO.findByUser_UserID(currentUser.getUserID());
            if (customer == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
                return "redirect:/customer/profile";
            }
            
            customer.setTenKH(fullname);
            customer.setSdt(phone);
            khachHangDAO.save(customer);
            
            // Cập nhật session
            sessionService.set("userName", fullname);
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(RedirectAttributes redirectAttributes) {
    	String currentPassword = paramService.getString("currentPassword", "");
        String newPassword = paramService.getString("newPassword", "");
        String confirmPassword = paramService.getString("confirmPassword", "");
        try {
            String email = authService.getCurrentUserMail();
            if (email == null) {
                return "redirect:/auth/login";
            }
            
            String result = authService.changePassword(email, currentPassword, newPassword, confirmPassword);
            
            if (result.equals("OK")) {
                redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", result);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/customer/profile";
    }


    @PostMapping("/add-address")
    public String addAddress(RedirectAttributes redirectAttributes) {
        try {
            // SỬ DỤNG PARAMSERVICE THAY CHO @RequestParam
            String tenNN = paramService.getString("tenNN", "");
            String sdt = paramService.getString("sdt", "");
            String diemGiao = paramService.getString("diemGiao", "");
            boolean macDinh = paramService.getBoolean("macDinh", false);

            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/auth/login";
            }
            
            KhachHang customer = khachHangDAO.findByUser_UserID(currentUser.getUserID());
            if (customer == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
                return "redirect:/customer/profile";
            }
            
            // Nếu đặt làm mặc định, hủy mặc định của các địa chỉ khác
            if (macDinh) {
                diaChiDAO.clearDefaultAddress(customer.getMaKH());
            }
            
            DiaChi newAddress = new DiaChi();
            newAddress.setKhachHang(customer);
            newAddress.setTenNN(tenNN);
            newAddress.setSdt(sdt);
            newAddress.setDiemGiao(diemGiao);
            newAddress.setMacDinh(macDinh);
            
            diaChiDAO.save(newAddress);
            redirectAttributes.addFlashAttribute("message", "Thêm địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm địa chỉ thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/update-address")
    public String updateAddress(RedirectAttributes redirectAttributes) {
        try {
            // SỬ DỤNG PARAMSERVICE THAY CHO @RequestParam
            Integer maDC = paramService.getInt("maDC", -1);
            String tenNN = paramService.getString("tenNN", "");
            String sdt = paramService.getString("sdt", "");
            String diemGiao = paramService.getString("diemGiao", "");
            boolean macDinh = paramService.getBoolean("macDinh", false);

            if (maDC == -1) {
                redirectAttributes.addFlashAttribute("error", "Mã địa chỉ không hợp lệ");
                return "redirect:/customer/profile";
            }

            Optional<DiaChi> optionalAddress = diaChiDAO.findById(maDC);
            if (!optionalAddress.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy địa chỉ");
                return "redirect:/customer/profile";
            }
            
            DiaChi address = optionalAddress.get();
            
            // Nếu đặt làm mặc định, hủy mặc định của các địa chỉ khác
            if (macDinh) {
                diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
            }
            
            address.setTenNN(tenNN);
            address.setSdt(sdt);
            address.setDiemGiao(diemGiao);
            address.setMacDinh(macDinh);
            
            diaChiDAO.save(address);
            redirectAttributes.addFlashAttribute("message", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật địa chỉ thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/set-default-address")
    public String setDefaultAddress(RedirectAttributes redirectAttributes) {
        try {
            // SỬ DỤNG PARAMSERVICE THAY CHO @RequestParam
            Integer addressId = paramService.getInt("addressId", -1);

            if (addressId == -1) {
                redirectAttributes.addFlashAttribute("error", "Mã địa chỉ không hợp lệ");
                return "redirect:/customer/profile";
            }

            Optional<DiaChi> optionalAddress = diaChiDAO.findById(addressId);
            if (!optionalAddress.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy địa chỉ");
                return "redirect:/customer/profile";
            }
            
            DiaChi address = optionalAddress.get();
            
            // Hủy mặc định của tất cả địa chỉ
            diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
            
            // Đặt địa chỉ này làm mặc định
            address.setMacDinh(true);
            diaChiDAO.save(address);
            
            redirectAttributes.addFlashAttribute("message", "Đặt địa chỉ mặc định thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/delete-address")
    public String deleteAddress(RedirectAttributes redirectAttributes) {
        try {
            // SỬ DỤNG PARAMSERVICE THAY CHO @RequestParam
            Integer addressId = paramService.getInt("addressId", -1);

            if (addressId == -1) {
                redirectAttributes.addFlashAttribute("error", "Mã địa chỉ không hợp lệ");
                return "redirect:/customer/profile";
            }

            diaChiDAO.deleteById(addressId);
            redirectAttributes.addFlashAttribute("message", "Xóa địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa địa chỉ thất bại: " + e.getMessage());
        }
        return "redirect:/customer/profile";
    }
}