package wanted.commerce.service.query.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import wanted.commerce.service.query.entity.ProductSearchDocument;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, Long> {
}
