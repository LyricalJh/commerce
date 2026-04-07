package wanted.commerce.service.query;

import wanted.commerce.service.dto.MainPageDto;
import wanted.commerce.service.query.entity.ProductDocument;

import java.util.List;

public interface ProductDocumentOperations {

    ProductDocument findProductDocumentWithReferences(Long productId);

    List<ProductDocument> findProductDocumentsWithReferences(List<Long> productIds);

    List<ProductDocument> findNewProducts(int limit);

    List<ProductDocument> findPopularProducts(int limit);

    List<MainPageDto.FeaturedCategory> findFeaturedCategories(int limit);
}

