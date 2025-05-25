package wanted.commerce.service.query.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import wanted.commerce.service.query.entity.TagDocument;

@Repository
public interface TagDocumentRepository extends MongoRepository<TagDocument, Long> {
}
