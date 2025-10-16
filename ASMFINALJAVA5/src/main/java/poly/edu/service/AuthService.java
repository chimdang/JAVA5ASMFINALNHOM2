package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import poly.edu.dao.*;
import poly.edu.entity.*;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired UsersDAO usersDAO;
    @Autowired NhanVienDAO nhanVienDAO;
    @Autowired KhachHangDAO khachHangDAO;
    @Autowired HttpSession session;
    @Autowired CookieService cookieService;
    @Autowired SessionService sessionService;
    @Autowired
    private EmailService emailService;

    // Kiểm tra đăng nhập
    public String login(String mail, String pass, boolean remember) {
        Users user = usersDAO.findByMail(mail);
        if (user == null || !user.getPass().equals(pass)) {
            return "Sai tài khoản hoặc mật khẩu";
        }

        NhanVien nv = nhanVienDAO.findByUser_UserID(user.getUserID());
        KhachHang kh = khachHangDAO.findByUser_UserID(user.getUserID());

        
        if (nv != null) {
            sessionService.set("userRole", nv.getVaitro());
            sessionService.set("userName", nv.getTenNV());
        } else if (kh != null) {
            sessionService.set("userRole", "CUSTOMER");
            sessionService.set("userName", kh.getTenKH());
        }
        sessionService.set("user", user);
        sessionService.set("userMail", user.getMail());

        // Lưu thông tin đăng nhập vào cookie nếu chọn "Ghi nhớ"
        if (remember) {            
            String credentials = user.getMail() + ":" + user.getPass();
            cookieService.add("rememberMe", credentials, 24 * 7);
        } else {           
            cookieService.remove("rememberMe");
        }

        return "OK";
    }

    public void logout() {
        
        // Xóa tất cả session attributes
        sessionService.remove("userRole");
        sessionService.remove("userName");
        sessionService.remove("user");
        sessionService.remove("userMail");
        
        // Xóa tất cả session
        session.invalidate();
    }

    // Tự động đăng nhập từ cookie
    public boolean autoLoginFromCookie() {
        // Nếu đã có user trong session thì không cần auto login
        if (sessionService.get("user") != null) {
            return true;
        }
        
        String credentials = cookieService.getValue("rememberMe");
        
        if (credentials != null && !credentials.isEmpty()) {
            try {
                String[] parts = credentials.split(":");
                if (parts.length == 2) {
                    String mail = parts[0];
                    String pass = parts[1];
                    
                    Users user = usersDAO.findByMail(mail);
                    
                    if (user != null && user.getPass().equals(pass)) {                        
                        return performAutoLogin(user);
                    }
                }
            } catch (Exception e) {
                cookieService.remove("rememberMe");
            }
        }
        return false;
    }

    private boolean performAutoLogin(Users user) {
        try {
            NhanVien nv = nhanVienDAO.findByUser_UserID(user.getUserID());
            KhachHang kh = khachHangDAO.findByUser_UserID(user.getUserID());

            // Lưu thông tin vào session
            if (nv != null) {
                sessionService.set("userRole", nv.getVaitro());
                sessionService.set("userName", nv.getTenNV());
            } else if (kh != null) {
                sessionService.set("userRole", "CUSTOMER");
                sessionService.set("userName", kh.getTenKH());
            }
            sessionService.set("user", user);
            sessionService.set("userMail", user.getMail());

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmployee() {
        String role = sessionService.get("userRole");
        boolean result = role != null && (role.equals("ADMIN") || role.equals("EMPLOYEE") || role.equals("Nhân viên") || role.equals("ADMIN") || role.equals("Admin"));
        return result;
    }

    public boolean isAdmin() {
        String role = sessionService.get("userRole");
        boolean result = role != null && (role.equals("ADMIN") || role.equals("Admin"));
        return result;
    }

    public boolean isCustomer() {
        String role = sessionService.get("userRole");
        boolean result = role != null && role.equals("CUSTOMER");
        return result;
    }
    
    public String getCurrentUserRole() {
        return sessionService.get("userRole");
    }
    
    public String getCurrentUserMail() {
        return sessionService.get("userMail");
    }

    public Users getCurrentUser() {
        return sessionService.get("user");
    }
   
    public String forgotPassword(String email) {
        try {
            Users user = usersDAO.findByMail(email);
            if (user == null) {
                return "Email không tồn tại trong hệ thống";
            }

            // Tạo mật khẩu mới 6 ký tự (chữ và số viết hoa)
            String newPassword = generateRandomPassword();
            
            // Cập nhật mật khẩu mới cho user
            user.setPass(newPassword);
            usersDAO.save(user);

            // Lấy thông tin khách hàng để gửi email
            KhachHang khachHang = khachHangDAO.findByUser_UserID(user.getUserID());
            String fullname = (khachHang != null) ? khachHang.getTenKH() : "Quý khách";

            // Gửi email chứa mật khẩu mới
            sendPasswordResetEmail(email, fullname, newPassword);

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void sendPasswordResetEmail(String email, String fullname, String newPassword) {
        try {
        	String subject = "PINE SHOP - Khôi phục mật khẩu";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".password-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px; }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>PINE SHOP - Khôi phục mật khẩu</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>"
                    + "<p>Mật khẩu mới của bạn là:</p>"
                    + "<div class='password-box'>"
                    + newPassword
                    + "</div>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý quan trọng:</strong></p>"
                    + "<p>• Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi truy cập hệ thống</p>"
                    + "<p>• Không chia sẻ mật khẩu này với bất kỳ ai</p>"
                    + "</div>"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống PINE SHOP.</p>"
                    + "<p>© 2024 PINE SHOP. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }
    
    public String changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        try {
            // Kiểm tra email tồn tại
            Users user = usersDAO.findByMail(email);
            if (user == null) {
                return "Email không tồn tại trong hệ thống";
            }

            // Kiểm tra mật khẩu hiện tại
            if (!user.getPass().equals(currentPassword)) {
                return "Mật khẩu hiện tại không đúng";
            }

            // Kiểm tra mật khẩu mới và xác nhận mật khẩu
            if (!newPassword.equals(confirmPassword)) {
                return "Mật khẩu mới và xác nhận mật khẩu không khớp";
            }

            user.setPass(newPassword);
            usersDAO.save(user);

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }
}