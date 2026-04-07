package wanted.commerce.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wanted.commerce.service.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
