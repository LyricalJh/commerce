package wanted.commerce.service.query.sync.handler;

import wanted.commerce.service.query.sync.CdcEvent;

public interface CdcEventHandler {

    boolean canHandle(CdcEvent event);

    void handle(CdcEvent event);
}
