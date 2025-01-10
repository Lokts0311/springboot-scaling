package lokts.springboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;

    @PrePersist
    protected void onCreate() {
        createAt = new Date();
        updateAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = new Date();
    }
}
