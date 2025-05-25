package wanted.commerce.service.query;

import wanted.commerce.service.query.entity.ProductDocument;

import java.util.List;

public interface ProductDocumentOperations {

    ProductDocument findProductDocumentWithReferences(Long productId);

    List<ProductDocument> findProductDocumentsWithReferences(List<Long> productIds);
}
