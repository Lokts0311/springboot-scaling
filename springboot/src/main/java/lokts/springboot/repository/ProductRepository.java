package lokts.springboot.repository;

import lokts.springboot.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);

    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select * from products p where p.id = :id for update",nativeQuery = true)
    Optional<Product> findByIdForUpdate(Long id);
}
