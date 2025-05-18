package wanted.commerce.controller.mapper;

import wanted.commerce.controller.dto.ReviewCreateRequest;
import wanted.commerce.controller.dto.ReviewUpdateRequest;
import wanted.commerce.service.dto.ReviewDto;
import org.springframework.stereotype.Component;

@Component
public class ReviewControllerMapper {

    public ReviewDto.CreateRequest toReviewDtoCreateRequest(ReviewCreateRequest request) {
        return ReviewDto.CreateRequest.builder()
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    public ReviewDto.UpdateRequest toReviewDtoUpdateRequest(ReviewUpdateRequest request) {
        return ReviewDto.UpdateRequest.builder()
                .rating(request.getRating())
                .content(request.getContent())
                .build();
    }

}