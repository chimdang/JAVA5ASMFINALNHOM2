package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class CookieService {
    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;

    public Cookie get(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public String getValue(String name) {
        Cookie cookie = get(name);
        return (cookie != null) ? cookie.getValue() : null;
    }

    public String getValue(String name, String defaultValue) {
        Cookie cookie = get(name);
        return (cookie != null) ? cookie.getValue() : defaultValue;
    }

    public Cookie add(String name, String value, int hours) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(hours * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
        return cookie;
    }

    public void remove(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/"); 
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);;
    }

    public void addRememberMe(String mail, String password, int days) {
        // Tạo cookie remember me với format: mail:password
        String credentials = mail + ":" + password;
        add("rememberMe", credentials, days * 24);
    }

    public String[] getRememberMeCredentials() {
        String credentials = getValue("rememberMe");
        if (credentials != null && credentials.contains(":")) {
            return credentials.split(":", 2);
        }
        return null;
    }

    public void removeRememberMe() {
        remove("rememberMe");
    }

    public boolean hasRememberMe() {
        return getValue("rememberMe") != null;
    }
}