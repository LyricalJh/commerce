package wanted.commerce.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductImageRequest {
    private String url;
    private String altText;
    private boolean isPrimary;
    private Integer displayOrder;
    private Long optionId;
}