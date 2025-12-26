package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "NhapKho")
public class NhapKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNK;

    @ManyToOne
    @JoinColumn(name = "MaSP")
    private SanPham sanPham;

    private Integer soLuong;

    @Temporal(TemporalType.DATE)
    private Date ngayNK;
}
