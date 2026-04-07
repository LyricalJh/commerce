package wanted.commerce.service.query.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import wanted.commerce.service.query.entity.BrandDocument;

@Repository
public interface BrandDocumentRepository extends MongoRepository<BrandDocument, Long> {
}
