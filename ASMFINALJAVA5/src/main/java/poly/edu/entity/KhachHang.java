package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "KhachHang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKH;

    private String tenKH;
    private String sdt;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private Users user;
}
