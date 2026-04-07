package wanted.commerce.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import wanted.commerce.service.dto.PaginationDto;
import wanted.commerce.service.product.ProductDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {
    private List<ProductDto.ProductSummary> items;
    private PaginationDto.PaginationInfo pagination;
}
