package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class SessionService {
    @Autowired
    HttpSession session;

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) session.getAttribute(name);
    }

    public <T> T get(String name, T defaultValue) {
        T value = (T) session.getAttribute(name);
        return value != null ? value : defaultValue;
    }

    public void set(String name, Object value) {
        session.setAttribute(name, value);
    }

    public void remove(String name) {
        session.removeAttribute(name);
    }

    // Phương thức đặc biệt cho auth
    public String getCurrentUserMail() {
        return get("userMail");
    }

    public String getCurrentUserRole() {
        return get("userRole");
    }

    public String getCurrentUserName() {
        return get("userName");
    }

    public Object getCurrentUser() {
        return get("user");
    }

    public boolean isLoggedIn() {
        return get("user") != null;
    }

    public boolean isEmployee() {
        String role = getCurrentUserRole();
        return role != null && (role.equals("ADMIN") || role.equals("EMPLOYEE"));
    }

    public boolean isAdmin() {
        String role = getCurrentUserRole();
        return role != null && role.equals("ADMIN");
    }

    public boolean isCustomer() {
        String role = getCurrentUserRole();
        return role != null && role.equals("CUSTOMER");
    }

    public void clearAuth() {
        remove("user");
        remove("userMail");
        remove("userRole");
        remove("userName");
    }
}