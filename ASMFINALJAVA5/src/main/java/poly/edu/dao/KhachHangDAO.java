package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;
import java.util.List;


public interface KhachHangDAO extends JpaRepository<KhachHang, Integer> {
	KhachHang findByUser_UserID(Integer userID);
}

