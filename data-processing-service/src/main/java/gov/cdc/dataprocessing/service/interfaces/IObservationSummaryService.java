package gov.cdc.dataprocessing.service.interfaces;

import gov.cdc.dataprocessing.exception.DataProcessingException;
import gov.cdc.dataprocessing.model.container.LabReportSummaryContainer;
import gov.cdc.dataprocessing.model.container.ProviderDataForPrintContainer;
import gov.cdc.dataprocessing.model.container.UidSummaryContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface IObservationSummaryService {
    Collection<UidSummaryContainer> findAllActiveLabReportUidListForManage(Long investigationUid, String whereClause) throws DataProcessingException;
    Map<Object,Object> getLabParticipations(Long observationUID) throws DataProcessingException;
    ArrayList<Object> getPatientPersonInfo(Long observationUID) throws DataProcessingException;
    ArrayList<Object>  getProviderInfo(Long observationUID,String partTypeCd) throws DataProcessingException;
    ArrayList<Object>  getActIdDetails(Long observationUID) throws DataProcessingException;
    String getReportingFacilityName(Long organizationUid) throws DataProcessingException;
    String getSpecimanSource(Long materialUid) throws DataProcessingException;
    ProviderDataForPrintContainer getOrderingFacilityAddress(ProviderDataForPrintContainer providerDataForPrintVO, Long organizationUid) throws DataProcessingException;
    ProviderDataForPrintContainer getOrderingFacilityPhone(ProviderDataForPrintContainer providerDataForPrintVO, Long organizationUid) throws DataProcessingException;
    ProviderDataForPrintContainer getOrderingPersonAddress(ProviderDataForPrintContainer providerDataForPrintVO, Long organizationUid) throws  DataProcessingException;
    ProviderDataForPrintContainer getOrderingPersonPhone(ProviderDataForPrintContainer providerDataForPrintVO, Long organizationUid) throws DataProcessingException;
    Long getProviderInformation (ArrayList<Object>  providerDetails, LabReportSummaryContainer labRep);
    void getTestAndSusceptibilities(String typeCode, Long observationUid, LabReportSummaryContainer labRepEvent, LabReportSummaryContainer labRepSumm);
    Map<Object,Object>  getAssociatedInvList(Long uid,String sourceClassCd) throws DataProcessingException;
}
