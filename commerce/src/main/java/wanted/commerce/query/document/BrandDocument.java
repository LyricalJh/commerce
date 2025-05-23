package wanted.commerce.query.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "brands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDocument {

    @Id
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String website;
}
