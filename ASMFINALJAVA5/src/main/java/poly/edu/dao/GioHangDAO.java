package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;

import java.util.List;
public interface GioHangDAO extends JpaRepository<GioHang, Integer> {
	List<GioHang> findByKhachHang(KhachHang khachHang);
    GioHang findByKhachHangAndSanPham(KhachHang khachHang, SanPham sanPham);
}
