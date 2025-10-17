package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.List;
import java.util.Date;
import jakarta.servlet.http.HttpSession;

import poly.edu.entity.*;
import poly.edu.dao.*;
import poly.edu.service.AuthService;
import poly.edu.service.ParamService;
import poly.edu.service.SessionService;


@Controller
@RequestMapping("/customer")
public class CustomerController {
    
    // Dependencies từ cả hai file
	 @Autowired KhachHangDAO khachHangDAO;
	 @Autowired DiaChiDAO diaChiDAO;
	 @Autowired UsersDAO usersDAO;
	 @Autowired SessionService sessionService;
	 @Autowired ParamService paramService;
	 @Autowired AuthService authService;
     @Autowired private GioHangDAO gioHangDAO;
     @Autowired private SanPhamDAO sanPhamDAO;
     @Autowired private HoaDonDAO hoaDonDAO;
     @Autowired private HoaDonCTDAO hoaDonCTDAO;
     @Autowired private HttpSession session;
	
    /**
     * Helper Method: Lấy thông tin Khách hàng đã đăng nhập
     */
    private KhachHang getCurrentCustomer() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return khachHangDAO.findByUser_UserID(currentUser.getUserID());
    }
    
    // -----------------------------------------------------------------------------------
    // I. VIEW CƠ BẢN
    // -----------------------------------------------------------------------------------
    
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

    // -----------------------------------------------------------------------------------
    // II. GIỎ HÀNG (CART)
    // -----------------------------------------------------------------------------------

    @GetMapping("/cart")
    public String cart(Model model) {
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) return "redirect:/auth/login";
        
        List<GioHang> cartItems = gioHangDAO.findByKhachHang(khachHang);
        double totalPrice = cartItems.stream()
            .mapToDouble(item -> item.getSoLuong() * item.getSanPham().getDonGia())
            .sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        
        model.addAttribute("title", "Giỏ hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_GioHang";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("maSP") Integer maSP, @RequestParam("soLuong") Integer soLuong) {
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) return "redirect:/auth/login";

        Optional<SanPham> sanPhamOpt = sanPhamDAO.findById(maSP);
        if (sanPhamOpt.isPresent()) {
            SanPham sanPham = sanPhamOpt.get();
            GioHang existingItem = gioHangDAO.findByKhachHangAndSanPham(khachHang, sanPham);
            if (existingItem != null) {
                existingItem.setSoLuong(existingItem.getSoLuong() + soLuong);
                gioHangDAO.save(existingItem);
            } else {
                GioHang newItem = new GioHang();
                newItem.setKhachHang(khachHang);
                newItem.setSanPham(sanPham);
                newItem.setSoLuong(soLuong);
                gioHangDAO.save(newItem);
            }
        }
        return "redirect:/customer/cart";
    }
    
    @PostMapping("/cart/update")
    @Transactional
    public String updateCart(@RequestParam("maGH") Integer maGH, @RequestParam("soLuong") Integer soLuong) {
        Optional<GioHang> cartItemOpt = gioHangDAO.findById(maGH);
        if (cartItemOpt.isPresent()) {
            GioHang cartItem = cartItemOpt.get();
            if (soLuong > 0) {
                cartItem.setSoLuong(soLuong);
                gioHangDAO.save(cartItem);
            } else {
                gioHangDAO.deleteById(maGH);
            }
        }
        return "redirect:/customer/cart";
    }

    @GetMapping("/cart/remove/{maGH}")
    public String removeFromCart(@PathVariable("maGH") Integer maGH) {
        gioHangDAO.deleteById(maGH);
        return "redirect:/customer/cart";
    }

    @PostMapping("/cart/delete-selected")
    public String deleteSelectedItems(@RequestParam("selectedIds") List<Integer> selectedIds) {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            gioHangDAO.deleteAllById(selectedIds);
        }
        return "redirect:/customer/cart";
    }

    // -----------------------------------------------------------------------------------
    // III. THANH TOÁN (CHECKOUT) VÀ ĐƠN HÀNG (ORDER)
    // -----------------------------------------------------------------------------------

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

    @PostMapping("/checkout")
    public String checkout(@RequestParam(value = "selectedItems", required = false) List<Integer> selectedItems, Model model) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return "redirect:/customer/cart?error=notselected";
        }
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) {
            return "redirect:/auth/login";
        }
        List<GioHang> cartItems = gioHangDAO.findAllById(selectedItems);
        session.setAttribute("selectedCartItemIds", selectedItems);
        List<DiaChi> addresses = diaChiDAO.findByKhachHang(khachHang);
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPham().getDonGia())
                .sum();
        model.addAttribute("addresses", addresses);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("newAddress", new DiaChi());
        model.addAttribute("title", "Đặt hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_DatHang";
    }

    /**
     * Xử lý hiển thị trang checkout khi bị redirect về
     */
    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        List<Integer> selectedItemsIds = (List<Integer>) session.getAttribute("selectedCartItemIds");
        if (selectedItemsIds == null || selectedItemsIds.isEmpty()) {
            return "redirect:/customer/cart";
        }
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) {
            return "redirect:/auth/login";
        }
        List<GioHang> cartItems = gioHangDAO.findAllById(selectedItemsIds);
        List<DiaChi> addresses = diaChiDAO.findByKhachHang(khachHang);
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPham().getDonGia())
                .sum();
        model.addAttribute("addresses", addresses);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("newAddress", new DiaChi());
        model.addAttribute("title", "Đặt hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_DatHang";
    }

    @PostMapping("/place-order")
    @Transactional
    public String placeOrder(@RequestParam("maDC") Integer maDC, RedirectAttributes redirectAttributes) {
        List<Integer> selectedItemsIds = (List<Integer>) session.getAttribute("selectedCartItemIds");
        if (selectedItemsIds == null || selectedItemsIds.isEmpty()) {
            return "redirect:/customer/cart";
        }
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) return "redirect:/auth/login";
        DiaChi diaChi = diaChiDAO.findById(maDC).orElse(null);
        if (diaChi == null) return "redirect:/customer/checkout?error=address_not_found";
        
        List<GioHang> cartItemsToOrder = gioHangDAO.findAllById(selectedItemsIds);
        HoaDon newOrder = new HoaDon();
        newOrder.setKhachHang(khachHang);
        newOrder.setDiaChi(diaChi);
        newOrder.setNgayMua(new Date());
        newOrder.setTrangThai("Chờ xác nhận");
        HoaDon savedOrder = hoaDonDAO.save(newOrder);
        
        for (GioHang item : cartItemsToOrder) {
            HoaDonCT orderDetail = new HoaDonCT();
            orderDetail.setHoaDon(savedOrder);
            orderDetail.setSanPham(item.getSanPham());
            orderDetail.setSoLuong(item.getSoLuong());
            orderDetail.setDonGia(item.getSanPham().getDonGia());
            hoaDonCTDAO.save(orderDetail);
            SanPham product = item.getSanPham();
            product.setSoLuong(product.getSoLuong() - item.getSoLuong());
            sanPhamDAO.save(product);
        }
        gioHangDAO.deleteAllById(selectedItemsIds);
        session.removeAttribute("selectedCartItemIds");
        redirectAttributes.addFlashAttribute("orderSuccess", "Đặt hàng thành công!");
        return "redirect:/customer/orders";
    }

    // -----------------------------------------------------------------------------------
    // IV. QUẢN LÝ TÀI KHOẢN (PROFILE)
    // -----------------------------------------------------------------------------------
    
    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/auth/login";
            }

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

    // -----------------------------------------------------------------------------------
    // V. QUẢN LÝ ĐỊA CHỈ (DÙNG CHO PROFILE - Dùng ParamService)
    // -----------------------------------------------------------------------------------
    
    @PostMapping("/add-address")
    public String addAddressProfile(RedirectAttributes redirectAttributes) {
        try {
            String tenNN = paramService.getString("tenNN", "");
            String sdt = paramService.getString("sdt", "");
            String diemGiao = paramService.getString("diemGiao", "");
            boolean macDinh = paramService.getBoolean("macDinh", false);

            KhachHang customer = getCurrentCustomer();
            if (customer == null) {
                return "redirect:/auth/login";
            }
            
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
    public String updateAddressProfile(RedirectAttributes redirectAttributes) {
        try {
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
            
            diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
            
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
    
    // -----------------------------------------------------------------------------------
    // VI. QUẢN LÝ ĐỊA CHỈ (DÙNG CHO CHECKOUT MODAL - Dùng @ModelAttribute)
    // -----------------------------------------------------------------------------------

    @GetMapping("/address/details/{maDC}")
    @ResponseBody
    public DiaChi getAddressDetails(@PathVariable("maDC") Integer maDC) {
        return diaChiDAO.findById(maDC).orElse(null);
    }

    @PostMapping("/address/add")
    @Transactional
    public String addAddressCheckout(@ModelAttribute DiaChi newAddress, RedirectAttributes redirectAttributes) {
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) return "redirect:/auth/login";
        
        newAddress.setKhachHang(khachHang);
        
        // SỬ DỤNG LOGIC CLEAR DEFAULT TỪ FILE 1
        if (newAddress.getMacDinh() != null && newAddress.getMacDinh()) {
            diaChiDAO.clearDefaultAddress(khachHang.getMaKH());
        }
        
        diaChiDAO.save(newAddress);
        redirectAttributes.addFlashAttribute("message", "Thêm địa chỉ mới thành công!");
        return "redirect:/customer/checkout";
    }

    @PostMapping("/address/update")
    @Transactional
    public String updateAddressCheckout(@ModelAttribute DiaChi updatedAddress, RedirectAttributes redirectAttributes) {
        KhachHang khachHang = getCurrentCustomer();
        if (khachHang == null) return "redirect:/auth/login";

        updatedAddress.setKhachHang(khachHang);
        
        // SỬ DỤNG LOGIC CLEAR DEFAULT TỪ FILE 1
        if (updatedAddress.getMacDinh() != null && updatedAddress.getMacDinh()) {
            // Hủy mặc định của các địa chỉ khác (chỉ địa chỉ của khách hàng này)
             diaChiDAO.clearDefaultAddress(khachHang.getMaKH());
        }
        
        diaChiDAO.save(updatedAddress);
        redirectAttributes.addFlashAttribute("message", "Cập nhật địa chỉ thành công!");
        return "redirect:/customer/checkout";
    }

    @GetMapping("/address/delete/{maDC}")
    public String deleteAddressCheckout(@PathVariable("maDC") Integer maDC, RedirectAttributes redirectAttributes) {
        // Kiểm tra xem địa chỉ có thuộc về khách hàng hiện tại không (Tùy chọn, thêm bảo mật)
        Optional<DiaChi> diaChiOpt = diaChiDAO.findById(maDC);
        if(diaChiOpt.isPresent() && diaChiOpt.get().getKhachHang().equals(getCurrentCustomer())) {
            diaChiDAO.deleteById(maDC);
            redirectAttributes.addFlashAttribute("message", "Xóa địa chỉ thành công!");
        } else {
             redirectAttributes.addFlashAttribute("error", "Không tìm thấy hoặc không có quyền xóa địa chỉ này.");
        }
        return "redirect:/customer/checkout";
    }
}