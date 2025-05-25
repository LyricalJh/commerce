package wanted.commerce.service.query.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDocument {

    @Id
    private Long id;
    private String name;
    private String slug;
}
