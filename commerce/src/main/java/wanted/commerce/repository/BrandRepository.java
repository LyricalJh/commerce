package wanted.commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wanted.commerce.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
