package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "DiaChi")
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDC;

    @ManyToOne
    @JoinColumn(name = "MaKH")
    private KhachHang khachHang;

    private String tenNN;
    private Boolean macDinh;
    private String sdt;
    private String diemGiao;
}
