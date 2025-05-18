package wanted.commerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wanted.commerce.entity.Product;
import wanted.commerce.entity.Review;
import wanted.commerce.entity.User;
import wanted.commerce.exception.AccessDeniedException;
import wanted.commerce.exception.ResourceNotFoundException;
import wanted.commerce.repository.ProductRepository;
import wanted.commerce.repository.ReviewRepository;
import wanted.commerce.repository.UserRepository;
import wanted.commerce.service.dto.PaginationDto;
import wanted.commerce.service.dto.ReviewDto;
import wanted.commerce.service.mapper.ReviewMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public ReviewDto.ReviewPage getProductReviews(
            Long productId,
            Integer rating,
            PaginationDto.PaginationRequest paginationRequest
    ) {

        // 상품 존재 확인
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // 모든 리뷰 조회 (summary 계산용)
        List<Review> allReviews = reviewRepository.findAllByProductId(productId);

        // 리뷰 요약 정보 계산
        ReviewDto.ReviewSummary summary = reviewMapper.toReviewSummary(allReviews);

        // 평점 필터 적용하여 페이지 조회
        Page<Review> reviewPage;
        if (rating != null ) {
            reviewPage = reviewRepository.findByProductIdAndRating(productId, rating, paginationRequest.toPageable());
        } else {
            reviewPage = reviewRepository.findByProductId(productId, paginationRequest.toPageable());
        }

        // 페이지 리뷰를 DTO로 변환
        List<ReviewDto.Review> reviewResponse = reviewPage.getContent().stream()
                .map(reviewMapper::toReviewDto)
                .toList();

        // 페이징 정보 생성
        PaginationDto.PaginationInfo paginationInfo = PaginationDto.PaginationInfo.builder()
                .totalItems((int) reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .currentPage(reviewPage.getNumber() + 1) // 0-based to 1-based
                .perPage(reviewPage.getSize())
                .build();

        // API 스펙에 맞게 응답 설정
        return ReviewDto.ReviewPage.builder()
                .items(reviewResponse)
                .summary(summary)
                .pagination(paginationInfo)
                .build();
    }

    @Override
    @Transactional
    public ReviewDto.Review createReview(Long productId, Long userId, ReviewDto.CreateRequest request) {
        // 상품 존재 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 리뷰 생성
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .verifiedPurchase(true)
                .helpfulVotes(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 저장
        review = reviewRepository.save(review);

        // 응답 변환
        return reviewMapper.toReviewDto(review);
    }

    @Override
    @Transactional
    public ReviewDto.Review updateReview(Long reviewId, Long userId, ReviewDto.UpdateRequest request) {
        // 리뷰 존재 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        // 권한 확인 (리뷰 작성자만 수정 가능)
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("다른 사용자의 리뷰를 수정할 권한이 없습니다.");
        }

        // 리뷰 수정
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        review.setUpdatedAt(LocalDateTime.now());

        // 저장
        review = reviewRepository.save(review);

        // 응답변환
        return reviewMapper.toReviewDto(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 리뷰 존재 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        // 권환 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 리뷰를 삭제할 권한이 없습니다.");
        }

        // 리뷰 삭제
        reviewRepository.delete(review);
    }
}
