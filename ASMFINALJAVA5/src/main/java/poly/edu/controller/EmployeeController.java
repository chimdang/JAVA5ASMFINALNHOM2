package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.dao.KhachHangDAO;
import poly.edu.dao.NhanVienDAO;
import poly.edu.dao.UsersDAO;
import poly.edu.entity.KhachHang;
import poly.edu.entity.NhanVien;
import poly.edu.entity.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Controller
@RequestMapping("/employee")
public class EmployeeController {
	@Autowired
	private NhanVienDAO nhanVienDAO;

	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private KhachHangDAO khachHangDAO;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
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
//USER
    @GetMapping("/users")
    public String users(Model model, 
                        @RequestParam(value = "roleFilter", required = false, defaultValue = "") String roleFilter,
                        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                        @RequestParam(value = "editId", required = false) Integer editId) {
        
        List<Users> userList;
        // Lọc theo keyword nếu có
        if (keyword != null && !keyword.isEmpty()) {
            userList = usersDAO.findByMailContaining(keyword);
        } else {
            userList = usersDAO.findAll();
        }
        List<Users> finalUserList = new ArrayList<>();
        if (roleFilter != null && !roleFilter.isEmpty()) {
            for (Users user : userList) {
                // Nếu lọc theo Nhân viên và user này là nhân viên -> thêm vào danh sách
                if ("NV".equals(roleFilter) && nhanVienDAO.findByUser(user).isPresent()) {
                    finalUserList.add(user);
                } 
                // Nếu lọc theo Khách hàng và user này là khách hàng -> thêm vào danh sách
                else if ("KH".equals(roleFilter) && khachHangDAO.findByUser(user).isPresent()) {
                    finalUserList.add(user);
                }
            }
        } else {
            // Nếu không có filter, danh sách cuối cùng là danh sách ban đầu
            finalUserList = userList;
        }
        // KẾT THÚC LOGIC LỌC
        
        // THAY THẾ DÒNG CŨ BẰNG DÒNG NÀY:
        model.addAttribute("userList", finalUserList);
        
        model.addAttribute("nhanVienDAO", nhanVienDAO);
        model.addAttribute("khachHangDAO", khachHangDAO);
        
        model.addAttribute("roleFilter", roleFilter); // Giữ lại giá trị filter
        model.addAttribute("keyword", keyword); // Giữ lại giá trị keyword

        // Chuẩn bị các đối tượng rỗng cho tab "Chỉnh sửa" (trường hợp Thêm mới)
        model.addAttribute("userEdit", new Users());
        model.addAttribute("nhanVienEdit", new NhanVien());
        model.addAttribute("khachHangEdit", new KhachHang());
        
        // Nếu có yêu cầu chỉnh sửa (click nút Edit từ danh sách)
        if (editId != null) {
            usersDAO.findById(editId).ifPresent(user -> {
                model.addAttribute("userEdit", user);
                nhanVienDAO.findByUser(user).ifPresent(nv -> model.addAttribute("nhanVienEdit", nv));
                khachHangDAO.findByUser(user).ifPresent(kh -> model.addAttribute("khachHangEdit", kh));
                model.addAttribute("activeTab", "edit"); // Tự động mở tab chỉnh sửa
            });
        }

        model.addAttribute("title", "Quản lý người dùng");
        model.addAttribute("role", "employee");
        return "employee/NV_QLuser";
    }

    /**
     * XỬ LÝ TẠO MỚI (CREATE)
     */
    @PostMapping("/users/create")
    @Transactional
    public String createUser(@RequestParam("mail") String mail,
                             @RequestParam("pass") String pass,
                             @RequestParam("role") String role,
                             @RequestParam(value = "fullname", required = false) String fullname,
                             @RequestParam(value = "sdt", required = false) String sdt,
                             RedirectAttributes redirectAttributes) {
        // 1. Tạo và lưu Users
        Users user = new Users();
        user.setMail(mail);
        user.setPass(pass); // Cần mã hóa mật khẩu trong thực tế
        Users savedUser = usersDAO.save(user);

        // 2. Dựa vào vai trò để tạo NhanVien hoặc KhachHang
        if ("NV".equals(role)) {
            NhanVien nv = new NhanVien();
            nv.setTenNV(fullname);
            nv.setVaitro("Nhân viên");
            nv.setUser(savedUser);
            nhanVienDAO.save(nv);
        } else if ("KH".equals(role)) {
            KhachHang kh = new KhachHang();
            kh.setTenKH(fullname);
            kh.setSdt(sdt);
            kh.setUser(savedUser);
            khachHangDAO.save(kh);
        }
        
        redirectAttributes.addFlashAttribute("message", "Tạo người dùng thành công!");
        return "redirect:/employee/users";
    }

    /**
     * XỬ LÝ CẬP NHẬT (UPDATE)
     */
    @PostMapping("/users/update")
    @Transactional
    public String updateUser(@RequestParam("userID") Integer userID,
                             @RequestParam("mail") String mail,
                             @RequestParam(value="pass", required=false) String pass,
                             @RequestParam(value = "fullname", required = false) String fullname,
                             @RequestParam(value = "sdt", required = false) String sdt,
                             RedirectAttributes redirectAttributes) {
        
        usersDAO.findById(userID).ifPresent(user -> {
            user.setMail(mail);
            if (pass != null && !pass.isEmpty()) {
                user.setPass(pass); // Chỉ cập nhật pass nếu người dùng nhập pass mới
            }
            usersDAO.save(user);

            // Cập nhật thông tin ở bảng NhanVien hoặc KhachHang
            nhanVienDAO.findByUser(user).ifPresent(nv -> {
                nv.setTenNV(fullname);
                nhanVienDAO.save(nv);
            });
            khachHangDAO.findByUser(user).ifPresent(kh -> {
                kh.setTenKH(fullname);
                kh.setSdt(sdt);
                khachHangDAO.save(kh);
            });
        });

        redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/employee/users?editId=" + userID; // Quay lại tab edit sau khi update
    }

    /**
     * XỬ LÝ XÓA (DELETE)
     */
    @PostMapping("/users/delete")
    @Transactional
    public String deleteUser(@RequestParam("userID") Integer userID, RedirectAttributes redirectAttributes) {
        usersDAO.findById(userID).ifPresent(user -> {
            // Phải xóa ở bảng phụ (NhanVien/KhachHang) trước
            nhanVienDAO.findByUser(user).ifPresent(nv -> nhanVienDAO.delete(nv));
            khachHangDAO.findByUser(user).ifPresent(kh -> khachHangDAO.delete(kh));
            // Sau đó mới xóa ở bảng chính (Users)
            usersDAO.delete(user);
        });
        redirectAttributes.addFlashAttribute("message", "Xóa người dùng thành công!");
        return "redirect:/employee/users";
    }
    //KETTHUC USER

    @GetMapping("/import")
    public String importStock(Model model) {
        model.addAttribute("title", "Nhập kho");
        model.addAttribute("role", "employee");
        return "employee/NV_NhapKho";
    }
}