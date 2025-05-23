package wanted.commerce.query.document;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagDocumentRepository extends MongoRepository<TagDocument, Long> {
}
