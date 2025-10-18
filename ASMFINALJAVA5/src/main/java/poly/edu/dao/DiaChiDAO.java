package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.DiaChi;
import java.util.List;

public interface DiaChiDAO extends JpaRepository<DiaChi, Integer> {
	List<DiaChi> findByKhachHang_MaKHAndTrangThaiXoaFalse(Integer maKH);
	
	@Modifying
	@Transactional
	@Query("UPDATE DiaChi d SET d.macDinh = false WHERE d.khachHang.maKH = :maKH AND d.trangThaiXoa = false")
	void clearDefaultAddress(@Param("maKH") Integer maKH);
	
	@Modifying
	@Transactional
	@Query("UPDATE DiaChi d SET d.trangThaiXoa = true WHERE d.maDC = :maDC")
	void softDelete(@Param("maDC") Integer maDC);
}	