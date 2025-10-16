package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.dao.*;
import poly.edu.entity.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private GioHangDAO gioHangDAO;
    @Autowired
    private SanPhamDAO sanPhamDAO;
    @Autowired
    private KhachHangDAO khachHangDAO;
    @Autowired
    private DiaChiDAO diaChiDAO;
    @Autowired
    private HoaDonDAO hoaDonDAO;
    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;
    @Autowired
    private HttpSession session;

    // ... (Các hàm không thay đổi như index, detailProduct, cart...)
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
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null); 
        if (khachHang != null) {
            List<GioHang> cartItems = gioHangDAO.findByKhachHang(khachHang);
            double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPham().getDonGia())
                .sum();
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalPrice", totalPrice);
        }
        model.addAttribute("title", "Giỏ hàng");
        model.addAttribute("role", "customer");
        return "customer/KH_GioHang";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("maSP") Integer maSP, @RequestParam("soLuong") Integer soLuong) {
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null);
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
    // ---- PHẦN SỬA LỖI BẮT ĐẦU TỪ ĐÂY ----

    @PostMapping("/checkout")
    public String checkout(@RequestParam(value = "selectedItems", required = false) List<Integer> selectedItems, Model model) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return "redirect:/customer/cart?error=notselected";
        }
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null);
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
     * [THÊM MỚI] - Xử lý hiển thị trang checkout khi bị redirect về
     */
    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        List<Integer> selectedItemsIds = (List<Integer>) session.getAttribute("selectedCartItemIds");
        if (selectedItemsIds == null || selectedItemsIds.isEmpty()) {
            return "redirect:/customer/cart";
        }
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null);
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
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null);
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

    @GetMapping("/address/details/{maDC}")
    @ResponseBody
    public DiaChi getAddressDetails(@PathVariable("maDC") Integer maDC) {
        return diaChiDAO.findById(maDC).orElse(null);
    }

    @PostMapping("/address/add")
    public String addAddress(@ModelAttribute DiaChi newAddress, RedirectAttributes redirectAttributes) {
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null);
        if (khachHang == null) return "redirect:/auth/login";
        
        newAddress.setKhachHang(khachHang);
        if (newAddress.getMacDinh() != null && newAddress.getMacDinh()) {
            diaChiDAO.findByKhachHang(khachHang).forEach(addr -> {
                addr.setMacDinh(false);
                diaChiDAO.save(addr);
            });
        }
        
        diaChiDAO.save(newAddress);
        redirectAttributes.addFlashAttribute("message", "Thêm địa chỉ mới thành công!");
        return "redirect:/customer/checkout";
    }

    @PostMapping("/address/update")
    public String updateAddress(@ModelAttribute DiaChi updatedAddress, RedirectAttributes redirectAttributes) {
        // [SỬA LỖI] - Đổi ID từ 5 thành 3 để nhất quán
        KhachHang khachHang = khachHangDAO.findById(3).orElse(null);
        if (khachHang == null) return "redirect:/auth/login";

        updatedAddress.setKhachHang(khachHang);
        if (updatedAddress.getMacDinh() != null && updatedAddress.getMacDinh()) {
            diaChiDAO.findByKhachHang(khachHang).forEach(addr -> {
                addr.setMacDinh(false);
                diaChiDAO.save(addr);
            });
        }
        
        diaChiDAO.save(updatedAddress);
        redirectAttributes.addFlashAttribute("message", "Cập nhật địa chỉ thành công!");
        return "redirect:/customer/checkout";
    }

    @GetMapping("/address/delete/{maDC}")
    public String deleteAddress(@PathVariable("maDC") Integer maDC, RedirectAttributes redirectAttributes) {
        diaChiDAO.deleteById(maDC);
        redirectAttributes.addFlashAttribute("message", "Xóa địa chỉ thành công!");
        return "redirect:/customer/checkout";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("title", "Quản lý tài khoản");
        model.addAttribute("role", "customer");
        return "customer/KH_QLuser";
    }
}