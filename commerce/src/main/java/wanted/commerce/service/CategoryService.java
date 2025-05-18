package wanted.commerce.service;

import wanted.commerce.service.dto.CategoryDto;
import wanted.commerce.service.dto.PaginationDto;

import java.util.List;

public interface CategoryService {

    // 카테고리 목록 조회 (계층 구조 포함)
    List<CategoryDto.Category> getAllCategories(Integer level);

    // 특정 카테고리의 상품 목폭 조회
    CategoryDto.CategoryProducts getCategoryProducts(
            Long categoryId,
            Boolean includeSubcategories,
            PaginationDto.PaginationRequest paginationRequest
    );

    // 특정 카테고리 조회 (하위 카테고리 포함)
    CategoryDto.Category getCategoryById(Long categoryId);
}
