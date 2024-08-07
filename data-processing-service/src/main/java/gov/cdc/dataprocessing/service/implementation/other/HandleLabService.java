package gov.cdc.dataprocessing.service.implementation.other;

import gov.cdc.dataprocessing.exception.DataProcessingConsumerException;
import gov.cdc.dataprocessing.service.interfaces.other.IHandleLabService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HandleLabService implements IHandleLabService {
    private static final Logger logger = LoggerFactory.getLogger(HandleLabService.class);
    public HandleLabService() {

    }

    public Object processingReviewedLab() throws DataProcessingConsumerException {
        try {
            return "processing reviewed lab";
        } catch (Exception e) {
            throw new DataProcessingConsumerException("ERROR", "Data");
        }

    }

    public Object processingNonReviewLabWithAct() throws DataProcessingConsumerException{
        try {
            return "processing non reviewed lab";
        } catch (Exception e) {
            throw new DataProcessingConsumerException("ERROR", "Data");
        }
    }

    public Object processingNonReviewLabWithoutAct() throws DataProcessingConsumerException{
        try {
            return "processing non reviewed lab without act";
        } catch (Exception e) {
            throw new DataProcessingConsumerException("ERROR", "Data");
        }
    }
}
