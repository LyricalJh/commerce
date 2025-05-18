package wanted.commerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wanted.commerce.entity.Category;
import wanted.commerce.entity.Product;
import wanted.commerce.exception.ResourceNotFoundException;
import wanted.commerce.repository.CategoryRepository;
import wanted.commerce.repository.ProductRepository;
import wanted.commerce.service.dto.CategoryDto;
import wanted.commerce.service.dto.PaginationDto;
import wanted.commerce.service.mapper.CategoryMapper;
import wanted.commerce.service.mapper.ProductMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryDto.Category> getAllCategories(Integer level) {
        if (level != null) {
            // 특정 레벨의 카테고리만 조회하되, 그. 하위 계층 구조도 포함
            List<Category> categories = categoryRepository.findByLevel(level);

            // 모든 카테고리 조회해서 계층 구조를 만들기 위한 준비
            List<Category> allCategories = categoryRepository.findAll();

            // 자식 카테고리 맵 구성 (부모 ID -> 자식 카테고리 리스트)
        }

        return List.of();
    }

    /**
     * 전체 카테고리의 계층 구조를 구성
     */
    private List<CategoryDto.Category> buildCategoryHierarchy() {
        // 1. 모든 카테고리 조회
        List<Category> allCategories = categoryRepository.findAll();

        // 2. 카테고리 ID -> 카테고리 맵 구성
        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        // 3. 자식 카테고리 맵 구성 (부모 ID -> 자식 카테고리 리스트)
        Map<Long, List<Category>> childrenMap = new HashMap<>();

        for (Category category : allCategories) {
            // 부모가 없다면 해당 객체가 부모
            if (category.getParent() != null) {
                Long parentId = category.getParent().getId();
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
            }
        }

        // 4. 최상위 카테고리 (level=1) 찾기
        List<Category> rootCategories = allCategories.stream()
                .filter(category -> category.getLevel() == 1)
                .toList();

        // 5. 계층 구조 구성 및 반환
        return rootCategories.stream()
                .map(root -> buildCategoryTree(root, childrenMap))
                .collect(Collectors.toList());
    }

    private CategoryDto.Category buildCategoryTree(Category category, Map<Long, List<Category>> childrenMap) {
        // 1. 현재 카테고리를 DTO로 반환
        CategoryDto.Category responseDto = categoryMapper.toCategoryResponse(category);

        // 2. 자식 카테고리가 있는 경우 재귀적으로 추가
        List<Category> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            List<CategoryDto.Category> childrenDto = children.stream()
                    .map(child -> buildCategoryTree(child, childrenMap))
                    .toList();
            responseDto.setChildren(childrenDto);
        }

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto.CategoryProducts getCategoryProducts(
            Long categoryId,
            Boolean includeSubcategories,
            PaginationDto.PaginationRequest paginationRequest
    ) {
        // 카테고리 존재 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        // 카테고리 정보 매핑
        CategoryDto.Detail categoryDetail = categoryMapper.toCategoryDetail(category);

        // 상품 조회
        Page<Product> productPage;
        if (Boolean.TRUE.equals(includeSubcategories)) {
            // 하위 카테고리 ID 수집
            List<Long> categoryIds = collectSubcategoryIds(categoryId);
            productPage = productRepository.findByCategoriesIdIn(categoryIds, paginationRequest.toPageable());
        } else {
            // 현재 카테고리만 조회
            productPage = productRepository.findByCategoriesId(categoryId, paginationRequest.toPageable());
        }

        // 응답 DTO 생성
        return CategoryDto.CategoryProducts.builder()
                .category(categoryDetail)
                .items(productPage.getContent().stream().map(productMapper::toProductSummaryDto).toList())
                .pagination(categoryMapper.toPaginationInfo(productPage))
                .build();
    }


    /**
     * 주어진 카테고리와 모든 하위 카테고리의 ID 목록 수집
     */
    private List<Long> collectSubcategoryIds(Long rootCategoryId) {
        List<Long> result = new ArrayList<>();
        result.add(rootCategoryId); // 루트 카테고리 포함

        // 전체 카테고리 조회
        List<Category> allCategories = categoryRepository.findAll();

        // 카테고리 ID -> 카테고리 매핑
        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(wanted.commerce.entity.Category::getId, c -> c));

        // 부모 ID -> 자식 카테고리 리스트 매핑
        Map<Long, List<Category>> childrenMap = new HashMap<>();
        for (Category category : allCategories) {
            if (category.getParent() != null) {
                Long parentId = category.getParent().getId();
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
            }
        }

        // 재귀적으로 하위 카테고리 ID 수집
        collectChildCategoryIds(rootCategoryId, childrenMap, result);

        return result;
    }

    /**
     * 재귀적으로 하위 카테고리 ID 수집
     */
    private void collectChildCategoryIds(Long rootCategoryId, Map<Long, List<Category>> childrenMap, List<Long> result) {
      List<Category> children = childrenMap.getOrDefault(rootCategoryId, new ArrayList<>());
      for (Category category : children) {
          result.add(category.getId());
          collectChildCategoryIds(category.getId(), childrenMap, result);
      }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto.Category getCategoryById(Long categoryId) {
        // 카테고리 존재 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        // 모든 카테고리 조회해서 계층 구조를 만들기 위한 준비
        List<Category> allCategories = categoryRepository.findAll();

        // 자식 카테고리 맵 구성 (부모 ID -> 자식 카테고리 리스트)
        Map<Long, List<Category>> childrenMap = new HashMap<>();
        for (Category each : allCategories) {
            if (each.getParent() != null) {
                Long parentId = category.getParent().getId();
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(each);
            }
        }

        // 요청된 카테고리의 계층 구조 구성
        return buildCategoryTree(category, childrenMap);
    }


}
