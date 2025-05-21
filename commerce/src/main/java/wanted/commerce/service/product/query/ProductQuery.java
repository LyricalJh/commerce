package wanted.commerce.service.product.query;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import wanted.commerce.service.dto.PaginationDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductQuery {

    @Data
    @Builder
    public static class GetProduct {
        private Long productId;
    }

    @Data
    @Builder
    public static class ListProducts {
        private String status;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private List<Long> category;
        private Long seller;
        private Long brand;
        private Boolean inStock; // 단순 재고 파악은 불리언 이 쿼리 성능에 더 좋다
        private List<Long> tag;
        private String search;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate createdFrom;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate createdTo;

        private PaginationDto.PaginationRequest pagination;
    }
}
