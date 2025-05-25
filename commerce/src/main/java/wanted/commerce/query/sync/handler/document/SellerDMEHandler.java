package wanted.commerce.query.sync.handler.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wanted.commerce.query.document.SellerDocument;
import wanted.commerce.query.document.SellerDocumentRepository;
import wanted.commerce.query.sync.CdcEvent;

import java.util.Map;

@Component
@Slf4j
public class SellerDMEHandler extends ProductDocumentModelEventHandler {

    private final SellerDocumentRepository sellerDocumentRepository;

    public SellerDMEHandler(ObjectMapper objectMapper, SellerDocumentRepository sellerDocumentRepository) {
        super(objectMapper);
        this.sellerDocumentRepository = sellerDocumentRepository;
    }

    @Override
    protected String getSupportedTable() {
        return "sellers";
    }

    @Override
    public void handle(CdcEvent event) {
        Map<String, Object> data;
        Long sellerId;

        if (event.isDelete()) {
            // 삭제 이벤트 처리
            data = event.getBeforeData();
            if (data == null || !data.containsKey("id")) {
                return;
            }

            sellerId = getLongValue(data, "id");
            sellerDocumentRepository.deleteById(sellerId);
            log.info("Deleted seller document with ID: {}", sellerId);
            return;
        }

        // 생성 또는 업데이트 이벤트 처리
        data = event.getAfterData();
        if (data == null || !data.containsKey("id")) {
            return;
        }

        sellerId = getLongValue(data, "id");

        // 판매자 문서 생성 또는 업데이트
        SellerDocument document = SellerDocument.builder()
                .id(sellerId)
                .name(getStringValue(data, "name"))
                .description(getStringValue(data, "description"))
                .logoUrl(getStringValue(data, "logo_url"))
                .rating(getBigDecimalValue(data, "rating"))
                .contactEmail(getStringValue(data, "contact_email"))
                .contactPhone(getStringValue(data, "contact_phone"))
                .createdAt(parseTimestampToLocalDateTime(data.get("created_at")))
                .build();

        sellerDocumentRepository.save(document);
        log.info("Saved seller document with ID: {}", sellerId);
    }
}