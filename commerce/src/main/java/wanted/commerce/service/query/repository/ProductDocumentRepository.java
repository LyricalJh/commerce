package wanted.commerce.service.query.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import wanted.commerce.service.query.entity.ProductDocument;

@Repository
public interface ProductDocumentRepository extends MongoRepository<ProductDocument, Long> {
}
