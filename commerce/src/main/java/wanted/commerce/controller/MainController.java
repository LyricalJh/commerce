package wanted.commerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wanted.commerce.service.query.MainPageQueryHandler;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainPageQueryHandler mainPageQueryHandler;

    @GetMapping
    public ResponseEntity<?> getMainPageContents() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        mainPageQueryHandler.getMainPageContents(),
                        "메인 페이지 상품 목록을 성공적으로 조회했습니다."
                )
        );
    }
}