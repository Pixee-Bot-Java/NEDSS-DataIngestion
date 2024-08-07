package gov.cdc.dataprocessing.repository.nbs.odse.repos.observation;

import gov.cdc.dataprocessing.repository.nbs.odse.model.observation.*;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.Optional;

public interface Observation_SummaryRepository {
   Collection<Observation_Summary> findAllActiveLabReportUidListForManage(Long investigationUid, String whereClause);

   Optional<Collection<Observation_Lab_Summary_ForWorkUp_New>> findLabSummaryForWorkupNew(Long personParentUid, String whereClause);
}
