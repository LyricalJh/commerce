package wanted.commerce.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductUpdateRequest {
    private String name;
    private String slug;
    private String shortDescription;
    private String fullDescription;
    private Long sellerId;
    private Long brandId;
    private String status;

    private ProductCreateRequest.ProductDetailDto detail;
    private ProductCreateRequest.ProductPriceDto price;
    private List<ProductCreateRequest.ProductCategoryDto> categories;
    private List<Long> tagIds;
}