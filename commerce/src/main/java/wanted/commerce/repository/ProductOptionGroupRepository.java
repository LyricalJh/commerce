package wanted.commerce.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wanted.commerce.entity.ProductOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import wanted.commerce.repository.projection.OptionGroupWithProductProjection;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupRepository extends JpaRepository<ProductOptionGroup, Long> {

    @Query("SELECT og.id as id, og.name as name, og.displayOrder as displayOrder, p.id as productId " +
            "FROM ProductOptionGroup og " +
            "JOIN og.product p " +
            "WHERE og.id = :optionGroupId")
    Optional<OptionGroupWithProductProjection> findOptionGroupWithProductProjection(@Param("optionGroupId") Long optionGroupId);

    @Query("SELECT og.id as id, og.name as name, og.displayOrder as displayOrder, p.id as productId " +
            "FROM ProductOptionGroup og " +
            "JOIN og.product p " +
            "WHERE p.id = :productId")
    List<OptionGroupWithProductProjection> findOptionGroupsByProductId(@Param("productId") Long productId);
}