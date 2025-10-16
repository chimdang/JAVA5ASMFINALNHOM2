package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;
import java.util.*;

public interface DiaChiDAO extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByKhachHang(KhachHang khachHang);
}
