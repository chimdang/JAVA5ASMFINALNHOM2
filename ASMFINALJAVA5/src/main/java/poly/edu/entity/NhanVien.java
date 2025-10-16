package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "NhanVien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNV;

    private String tenNV;
    private String vaitro;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private Users user;
}
