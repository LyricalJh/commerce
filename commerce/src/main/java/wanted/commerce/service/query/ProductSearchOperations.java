package wanted.commerce.service.query;

import org.springframework.data.elasticsearch.core.SearchHits;
import wanted.commerce.service.query.entity.ProductSearchDocument;

public interface ProductSearchOperations {

    SearchHits<ProductSearchDocument> searchProductsByConditions(ProductQuery.ListProducts query);
}
