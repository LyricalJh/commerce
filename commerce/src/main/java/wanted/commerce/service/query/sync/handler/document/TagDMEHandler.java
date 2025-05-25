package wanted.commerce.service.query.sync.handler.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wanted.commerce.service.query.entity.TagDocument;
import wanted.commerce.service.query.repository.TagDocumentRepository;
import wanted.commerce.service.query.sync.CdcEvent;

import java.util.Map;

@Component
@Slf4j
public class TagDMEHandler extends ProductDocumentModelEventHandler {

    private final TagDocumentRepository tagDocumentRepository;

    public TagDMEHandler(ObjectMapper objectMapper, TagDocumentRepository tagDocumentRepository) {
        super(objectMapper);
        this.tagDocumentRepository = tagDocumentRepository;
    }

    @Override
    protected String getSupportedTable() {
        return "tags";
    }

    @Override
    public void handle(CdcEvent event) {
        Map<String, Object> data;
        Long tagId;

        if (event.isDelete()) {
            // 삭제 이벤트 처리
            data = event.getBeforeData();
            if (data == null || !data.containsKey("id")) {
                return;
            }

            tagId = getLongValue(data, "id");
            tagDocumentRepository.deleteById(tagId);
            log.info("Deleted tag document with ID: {}", tagId);
            return;
        }

        // 생성 또는 업데이트 이벤트 처리
        data = event.getAfterData();
        if (data == null || !data.containsKey("id")) {
            return;
        }

        tagId = getLongValue(data, "id");

        // 태그 문서 생성 또는 업데이트
        TagDocument document = TagDocument.builder()
                .id(tagId)
                .name(getStringValue(data, "name"))
                .slug(getStringValue(data, "slug"))
                .build();

        tagDocumentRepository.save(document);
        log.info("Saved tag document with ID: {}", tagId);
    }
}