package wanted.commerce.query.document;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDocumentRepository extends MongoRepository<ProductDocument, Long> {
}
