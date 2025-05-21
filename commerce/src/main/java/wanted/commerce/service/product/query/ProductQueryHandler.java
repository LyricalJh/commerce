package wanted.commerce.service.product.query;

import wanted.commerce.controller.dto.ProductListResponse;
import wanted.commerce.service.product.ProductDto;

public interface ProductQueryHandler {
    // 상품 조회
    ProductDto.Product getProduct(ProductQuery.GetProduct query);

    ProductListResponse getProducts(ProductQuery.ListProducts query);
}
