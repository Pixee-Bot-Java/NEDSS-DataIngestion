package gov.cdc.dataprocessing.service.interfaces.matching;

import gov.cdc.dataprocessing.exception.DataProcessingException;
import gov.cdc.dataprocessing.model.dto.matching.EdxPatientMatchDto;
import gov.cdc.dataprocessing.model.container.PersonContainer;

public interface IPatientMatchingService {
    EdxPatientMatchDto getMatchingPatient(PersonContainer personContainer) throws DataProcessingException;
    boolean getMultipleMatchFound();
}
