package wanted.commerce.service.query.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import wanted.commerce.service.query.entity.SellerDocument;

@Repository
public interface SellerDocumentRepository extends MongoRepository<SellerDocument, Long> {
}
