package wanted.commerce.service;

import wanted.commerce.controller.dto.ProductListResponse;
import wanted.commerce.service.dto.ProductDto;

public interface ProductService {

    // 상품 관리
    ProductDto.Product createProduct(ProductDto.CreateRequest request);

    ProductDto.Product updateProduct(Long productId, ProductDto.UpdateRequest request);

    void deleteProduct(Long productId);

    // 옵션 관리
    ProductDto.Option addProductOption(Long productId, ProductDto.Option option);

    ProductDto.Option updateProductOption(Long productId, ProductDto.Option option);

    void deleteProductOption(Long productId, Long optionId);

    // 이미지 관리
    ProductDto.Image addProductImage(Long productId, ProductDto.Image image);

    // 상품조회
    ProductDto.Product getProductById(Long productId);

    ProductListResponse getProducts(ProductDto.ListRequest request);

}
