package poly.edu.dao;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;

public interface NhanVienDAO extends JpaRepository<NhanVien, Integer> {
	NhanVien findByUser_UserID(Integer userID);
	Optional<NhanVien> findByUser(Users user);
}
