package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SanPham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maSP;

    @ManyToOne
    @JoinColumn(name = "MaDM")
    private DanhMuc danhMuc;

    private String hinh;
    private String tenSP;
    private Integer soLuong;
    private Double donGia;
    private String phanLoai;
    
    @Column(columnDefinition = "nvarchar(max)")
    private String moTa;
}
