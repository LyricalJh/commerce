package wanted.commerce.query.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentModelSyncer {

    private final ObjectMapper objectMapper;
    //private final List<ProductDocumentModelEventHandler> documentEventHandlers;


}
