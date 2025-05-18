package wanted.commerce.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wanted.commerce.controller.dto.*;
import wanted.commerce.controller.mapper.ProductControllerMapper;
import wanted.commerce.service.ProductService;
import wanted.commerce.service.dto.ProductDto;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductControllerMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto.Product>> createProduct(
            @RequestBody ProductCreateRequest request
    ) {
        ProductDto.CreateRequest createRequest = mapper.toProductDtoCreateRequest(request);
        ProductDto.Product createdProduct = productService.createProduct(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdProduct, "상품이 성공적으로 등록되었습니다."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto.Product>> getProduct(@PathVariable Long id) {
        ProductDto.Product foundProduct = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(foundProduct, "상품 상세 정보를 성공적으로 조회했습니다."));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto.Product>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request
    ) {
        ProductDto.UpdateRequest updateRequest = mapper.toServiceUpdateDto(request);
        ProductDto.Product updatedProduct = productService.updateProduct(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "상품이 성공적으로 수정되었습니다"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "상품이 성공적으로 삭제되었습니다."));
    }

    @PostMapping("/{id}/options")
    public ResponseEntity<ApiResponse<ProductDto.Option>> addProductOption(
            @PathVariable Long id,
            @RequestParam Long optionGroupId,
            @RequestBody ProductOptionRequest request
    ) {
        ProductDto.Option createRequest = mapper.toProductDtoOptionWithOptionGroupId(optionGroupId, request);
        ProductDto.Option createdOption = productService.addProductOption(id, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdOption, "상품 옵션이 성공적으로 추가되었습니다."));
    }

    @PostMapping("/{id}/options/{optionId}")
    public ResponseEntity<ApiResponse<ProductDto.Option>> updateProductOption(
            @PathVariable Long id,
            @PathVariable Long optionId,
            @RequestBody ProductOptionRequest request
    ) {
        ProductDto.Option updateRequest = mapper.toProductDtoOptionOptionId(optionId, request);
        ProductDto.Option updatedOption = productService.updateProductOption(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedOption, "상품 옵션이 성공적으로 수정되었습니다."));
    }

    @DeleteMapping("/{id}/options/{optionId}")
    public ResponseEntity<ApiResponse<Void>> deleteProductOption(
            @PathVariable Long id,
            @PathVariable Long optionId
    ) {
        productService.deleteProductOption(id, optionId);
        return ResponseEntity.ok(ApiResponse.success(null, "상품 옵션이 성공적으로 삭제되었습니다."));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProductDto.Image>> addProductImage(
            @PathVariable Long id,
            @RequestBody ProductImageRequest request
    ) {
        ProductDto.Image createRequest = mapper.toProductDtoImage(request);
        ProductDto.Image createdImage = productService.addProductImage(id, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdImage, "상품 이미지가 성공적으로 추가되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getProducts(@ParameterObject ProductListRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducts(mapper.toProductDtoListRequest(request)),
                "상품 목록을 성공적으로 조회했습니다."
        ));
    }
}
