package wanted.commerce.query.sync.handler;

import wanted.commerce.query.sync.CdcEvent;

public interface CdcEventHandler {

    boolean canHandle(CdcEvent event);

    void handle(CdcEvent event);
}
