package wanted.commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wanted.commerce.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 레벨별 카테고리 조회
    List<Category> findByLevel(int level);
}
