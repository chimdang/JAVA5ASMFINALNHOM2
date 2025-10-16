package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;
import java.util.*;

public interface KhachHangDAO extends JpaRepository<KhachHang, Integer> {
	Optional<KhachHang> findByUser(Users user);
}

