package gov.cdc.dataprocessing.service.implementation.manager;

import com.google.gson.Gson;
import gov.cdc.dataprocessing.cache.SrteCache;
import gov.cdc.dataprocessing.constant.DecisionSupportConstants;
import gov.cdc.dataprocessing.constant.elr.EdxELRConstant;
import gov.cdc.dataprocessing.constant.elr.NEDSSConstant;
import gov.cdc.dataprocessing.constant.enums.NbsInterfaceStatus;
import gov.cdc.dataprocessing.exception.DataProcessingConsumerException;
import gov.cdc.dataprocessing.exception.DataProcessingException;
import gov.cdc.dataprocessing.exception.EdxLogException;
import gov.cdc.dataprocessing.kafka.producer.KafkaManagerProducer;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.vo.PageActProxyVO;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.vo.PublicHealthCaseVO;
import gov.cdc.dataprocessing.model.container.LabResultProxyContainer;
import gov.cdc.dataprocessing.model.container.ObservationContainer;
import gov.cdc.dataprocessing.model.container.PamProxyContainer;
import gov.cdc.dataprocessing.model.dto.edx.EdxRuleAlgorothmManagerDto;
import gov.cdc.dataprocessing.model.dto.log.EDXActivityDetailLogDto;
import gov.cdc.dataprocessing.model.dto.observation.ObservationDto;
import gov.cdc.dataprocessing.model.dto.lab_result.EdxLabInformationDto;
import gov.cdc.dataprocessing.repository.nbs.msgoute.model.NbsInterfaceModel;
import gov.cdc.dataprocessing.repository.nbs.msgoute.repos.NbsInterfaceRepository;
import gov.cdc.dataprocessing.repository.nbs.srte.model.ConditionCode;
import gov.cdc.dataprocessing.repository.nbs.srte.model.ElrXref;
import gov.cdc.dataprocessing.service.implementation.other.CachingValueService;
import gov.cdc.dataprocessing.service.interfaces.*;
import gov.cdc.dataprocessing.service.interfaces.log.IEdxLogService;
import gov.cdc.dataprocessing.service.interfaces.manager.IManagerAggregationService;
import gov.cdc.dataprocessing.service.interfaces.manager.IManagerService;
import gov.cdc.dataprocessing.service.interfaces.observation.IObservationService;
import gov.cdc.dataprocessing.service.interfaces.other.ICatchingValueService;
import gov.cdc.dataprocessing.service.interfaces.other.IDataExtractionService;
import gov.cdc.dataprocessing.service.interfaces.other.IHandleLabService;
import gov.cdc.dataprocessing.service.model.PublicHealthCaseFlowContainer;
import gov.cdc.dataprocessing.service.model.WdsTrackerView;
import gov.cdc.dataprocessing.utilities.auth.AuthUtil;
import gov.cdc.dataprocessing.utilities.component.generic_helper.ManagerUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static gov.cdc.dataprocessing.constant.ManagerEvent.EVENT_ELR;

@Service
@Slf4j
public class ManagerService implements IManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerService.class);

    private final IObservationService observationService;

    private final IEdxLogService edxLogService;

    private final IHandleLabService handleLabService;

    private final IDataExtractionService dataExtractionService;

    private final NbsInterfaceRepository nbsInterfaceRepository;

    private final ICatchingValueService cachingValueService;

    private final CacheManager cacheManager;

    private final IAuthUserService authUserService;

    private final IDecisionSupportService decisionSupportService;

    private final ManagerUtil managerUtil;

    private final KafkaManagerProducer kafkaManagerProducer;

    private final IManagerAggregationService managerAggregationService;
    private final ILabReportProcessing labReportProcessing;
    private final IPageService pageService;
    private final IPamService pamService;
    private final IInvestigationNotificationService investigationNotificationService;

    @Autowired
    public ManagerService(IObservationService observationService,
                          IEdxLogService edxLogService,
                          IHandleLabService handleLabService,
                          IDataExtractionService dataExtractionService,
                          NbsInterfaceRepository nbsInterfaceRepository,
                          CachingValueService cachingValueService,
                          CacheManager cacheManager,
                          IAuthUserService authUserService, IDecisionSupportService decisionSupportService,
                          ManagerUtil managerUtil,
                          KafkaManagerProducer kafkaManagerProducer,
                          IManagerAggregationService managerAggregationService,
                          ILabReportProcessing labReportProcessing,
                          IPageService pageService,
                          IPamService pamService,
                          IInvestigationNotificationService investigationNotificationService) {
        this.observationService = observationService;
        this.edxLogService = edxLogService;
        this.handleLabService = handleLabService;
        this.dataExtractionService = dataExtractionService;
        this.nbsInterfaceRepository = nbsInterfaceRepository;
        this.cachingValueService = cachingValueService;
        this.cacheManager = cacheManager;
        this.authUserService = authUserService;
        this.decisionSupportService = decisionSupportService;
        this.managerUtil = managerUtil;
        this.kafkaManagerProducer = kafkaManagerProducer;
        this.managerAggregationService = managerAggregationService;
        this.labReportProcessing = labReportProcessing;
        this.pageService = pageService;
        this.pamService = pamService;
        this.investigationNotificationService = investigationNotificationService;
    }

    @Transactional
    public Object processDistribution(String eventType, String data) throws DataProcessingConsumerException {
        Object result = new Object();
        if (AuthUtil.authUser != null) {
            switch (eventType) {
                case EVENT_ELR:
                    result = processingELR(data);
                    break;
                default:
                    break;
            }
            return result;
        } else {
            throw new DataProcessingConsumerException("Invalid User");
        }

    }

    public void processingEdxLog(String data) throws EdxLogException {
        edxLogService.processingLog();
    }

    @Transactional
    public void initiatingInvestigationAndPublicHealthCase(String data) throws DataProcessingException {
        NbsInterfaceModel nbsInterfaceModel = null;
        try {
            Gson gson = new Gson();
            PublicHealthCaseFlowContainer publicHealthCaseFlowContainer = gson.fromJson(data, PublicHealthCaseFlowContainer.class);
            EdxLabInformationDto edxLabInformationDto = publicHealthCaseFlowContainer.getEdxLabInformationDto();
            ObservationDto observationDto = publicHealthCaseFlowContainer.getObservationDto();
            LabResultProxyContainer labResultProxyContainer = publicHealthCaseFlowContainer.getLabResultProxyContainer();
            var res = nbsInterfaceRepository.findByNbsInterfaceUid(publicHealthCaseFlowContainer.getNbsInterfaceId());
            if (res.isPresent()) {
                nbsInterfaceModel = res.get();
            } else {
                throw new DataProcessingException("NBS Interface Data Not Exist");
            }

            if (edxLabInformationDto.isLabIsUpdateDRRQ()) {
                edxLabInformationDto.setLabIsUpdateSuccess(true);
                edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_15);
            } else if (edxLabInformationDto.isLabIsUpdateDRSA()) {
                edxLabInformationDto.setLabIsUpdateSuccess(true);
                edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_22);
            }

            if (edxLabInformationDto.isLabIsCreate()) {
                if (observationDto.getJurisdictionCd() != null && observationDto.getProgAreaCd() != null) {

                    // This logic here determine whether logic is mark as review or not
                    decisionSupportService.validateProxyContainer(labResultProxyContainer, edxLabInformationDto);

                    WdsTrackerView trackerView = new WdsTrackerView();
                    trackerView.setWdsReport(edxLabInformationDto.getWdsReports());

                    Long patUid = -1L;
                    Long patParentUid = -1L;
                    String patFirstName = null;
                    String patLastName = null;
                    for(var item : publicHealthCaseFlowContainer.getLabResultProxyContainer().getThePersonContainerCollection()) {
                        if (item.getThePersonDto().getCd().equals("PAT")) {
                            patUid = item.getThePersonDto().getUid();
                            patParentUid = item.getThePersonDto().getPersonParentUid();
                            patFirstName = item.getThePersonDto().getFirstNm();
                            patLastName = item.getThePersonDto().getLastNm();
                            break;
                        }
                    }

                    trackerView.setPatientUid(patUid);
                    trackerView.setPatientParentUid(patParentUid);
                    trackerView.setPatientFirstName(patFirstName);
                    trackerView.setPatientLastName(patLastName);


                    nbsInterfaceModel.setRecordStatusCd("COMPLETED_V2_STEP_2");
                    nbsInterfaceRepository.save(nbsInterfaceModel);





                    PublicHealthCaseFlowContainer phcContainer = new PublicHealthCaseFlowContainer();
                    phcContainer.setNbsInterfaceId(nbsInterfaceModel.getNbsInterfaceUid());
                    phcContainer.setLabResultProxyContainer(labResultProxyContainer);
                    phcContainer.setEdxLabInformationDto(edxLabInformationDto);
                    phcContainer.setObservationDto(observationDto);
                    phcContainer.setWdsTrackerView(trackerView);

                    if (edxLabInformationDto.getPageActContainer() != null
                    || edxLabInformationDto.getPamContainer() != null) {
                        if (edxLabInformationDto.getPageActContainer() != null) {
                            var pageActProxyVO = (PageActProxyVO) edxLabInformationDto.getPageActContainer();
                            trackerView.setPublicHealthCase(pageActProxyVO.getPublicHealthCaseVO().getThePublicHealthCaseDT());
                        }
                        else
                        {
                            var pamProxyVO = (PamProxyContainer)edxLabInformationDto.getPamContainer();
                            trackerView.setPublicHealthCase(pamProxyVO.getPublicHealthCaseVO().getThePublicHealthCaseDT());
                        }
                    }
                    else
                    {
                        if (edxLabInformationDto.getAction() != null) {
                            //action 3 is REVIEW
                            System.out.println("TEST");
                        }
                    }



//                    gson = new Gson();
//                    String jsonString = gson.toJson(phcContainer);
        //            kafkaManagerProducer.sendDataLabHandling(jsonString);

                    gson = new Gson();
                    String trackerString = gson.toJson(trackerView);
                    kafkaManagerProducer.sendDataActionTracker(trackerString);

                    gson = new Gson();
                    String jsonString = gson.toJson(phcContainer);
                    kafkaManagerProducer.sendDataLabHandling(jsonString);


                }
            }
        } catch (Exception e) {
            if (nbsInterfaceModel != null) {
                nbsInterfaceModel.setRecordStatusCd("FAILED_V2_STEP_2");
                nbsInterfaceRepository.save(nbsInterfaceModel);
            }

        }

    }

    public void initiatingLabProcessing(String data) throws DataProcessingConsumerException {
        NbsInterfaceModel nbsInterfaceModel = null;
        try {
            Gson gson = new Gson();
            PublicHealthCaseFlowContainer publicHealthCaseFlowContainer = gson.fromJson(data, PublicHealthCaseFlowContainer.class);
            EdxLabInformationDto edxLabInformationDto = publicHealthCaseFlowContainer.getEdxLabInformationDto();
            ObservationDto observationDto = publicHealthCaseFlowContainer.getObservationDto();
            LabResultProxyContainer labResultProxyContainer = publicHealthCaseFlowContainer.getLabResultProxyContainer();
            var res = nbsInterfaceRepository.findByNbsInterfaceUid(publicHealthCaseFlowContainer.getNbsInterfaceId());
            if (res.isPresent()) {
                nbsInterfaceModel = res.get();
            } else {
                throw new DataProcessingException("NBS Interface Data Not Exist");
            }


            PageActProxyVO pageActProxyVO = null;
            PamProxyContainer pamProxyVO = null;
            PublicHealthCaseVO publicHealthCaseVO = null;
            Long phcUid = null;

            /**
             * REVIEW NOTE and what not implemented
             * - Basic reviewed implemented -- clean up needed
             * - Adv reviewed is not implemented
             * */
            if (edxLabInformationDto.getAction() != null && edxLabInformationDto.getAction().equalsIgnoreCase(DecisionSupportConstants.MARK_AS_REVIEWED)) {
                //Check for user security to mark as review lab
                //checkSecurity(nbsSecurityObj, edxLabInformationDto, NBSBOLookup.OBSERVATIONLABREPORT, NBSOperationLookup.MARKREVIEWED, programAreaCd, jurisdictionCd);


                //TODO: 3rd Flow
                labReportProcessing.markAsReviewedHandler(observationDto.getObservationUid(), edxLabInformationDto);
                if (edxLabInformationDto.getAssociatedPublicHealthCaseUid() != null && edxLabInformationDto.getAssociatedPublicHealthCaseUid().longValue() > 0) {
                    edxLabInformationDto.setPublicHealthCaseUid(edxLabInformationDto.getAssociatedPublicHealthCaseUid());
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_21);
                    edxLabInformationDto.setLabAssociatedToInv(true);
                } else {
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_11);
                }

            }
            else if (edxLabInformationDto.getPageActContainer() != null || edxLabInformationDto.getPamContainer() != null)
            {
                //Check for user security to create investigation
                //checkSecurity(nbsSecurityObj, edxLabInformationDto, NBSBOLookup.INVESTIGATION, NBSOperationLookup.ADD, programAreaCd, jurisdictionCd);

                /**
                 * Incoming payload should reach PageActProxyVO
                 * */
                if (edxLabInformationDto.getPageActContainer() != null) {
                    pageActProxyVO =  edxLabInformationDto.getPageActContainer();
                    publicHealthCaseVO = pageActProxyVO.getPublicHealthCaseVO();
                }
                else
                {
                    pamProxyVO = edxLabInformationDto.getPamContainer();
                    publicHealthCaseVO = pamProxyVO.getPublicHealthCaseVO();
                }

                if (publicHealthCaseVO.getErrorText() != null)
                {
                    //TODO: LOGGING
                    requiredFieldError(publicHealthCaseVO.getErrorText(), edxLabInformationDto);

                }


                if (pageActProxyVO != null && observationDto.getJurisdictionCd() != null && observationDto.getProgAreaCd() != null) {
                    //TODO: 3rd Flow
                    phcUid = pageService.setPageProxyWithAutoAssoc(NEDSSConstant.CASE, pageActProxyVO, edxLabInformationDto.getRootObserbationUid(), NEDSSConstant.LABRESULT_CODE, null);

                    pageActProxyVO.getPublicHealthCaseVO().getThePublicHealthCaseDT().setPublicHealthCaseUid(phcUid);

                    edxLabInformationDto.setInvestigationSuccessfullyCreated(true);
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_3);
                    edxLabInformationDto.setPublicHealthCaseUid(phcUid);
                    edxLabInformationDto.setLabAssociatedToInv(true);
                }
                else if (observationDto.getJurisdictionCd() != null && observationDto.getProgAreaCd() != null)
                {
                    //TODO: 3rd Flow
                    phcUid = pamService.setPamProxyWithAutoAssoc(pamProxyVO, edxLabInformationDto.getRootObserbationUid(), NEDSSConstant.LABRESULT_CODE);

                    pamProxyVO.getPublicHealthCaseVO().getThePublicHealthCaseDT().setPublicHealthCaseUid(phcUid);
                    edxLabInformationDto.setInvestigationSuccessfullyCreated(true);
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_3);
                    edxLabInformationDto.setPublicHealthCaseUid(phcUid);
                    edxLabInformationDto.setLabAssociatedToInv(true);
                }

                if(edxLabInformationDto.getAction().equalsIgnoreCase(DecisionSupportConstants.CREATE_INVESTIGATION_WITH_NND_VALUE)){
                    //TODO: 3rd Flow
                    //TODO: THIS SEEM TO GO TO LOG
                    EDXActivityDetailLogDto edxActivityDetailLogDT = investigationNotificationService.sendNotification(publicHealthCaseVO, edxLabInformationDto.getNndComment());
                    edxActivityDetailLogDT.setRecordType(EdxELRConstant.ELR_RECORD_TP);
                    edxActivityDetailLogDT.setRecordName(EdxELRConstant.ELR_RECORD_NM);
                    ArrayList<EDXActivityDetailLogDto> details = (ArrayList<EDXActivityDetailLogDto>)edxLabInformationDto.getEdxActivityLogDto().getEDXActivityLogDTWithVocabDetails();
                    if(details==null){
                        details = new ArrayList<>();
                    }
                    details.add(edxActivityDetailLogDT);
                    edxLabInformationDto.getEdxActivityLogDto().setEDXActivityLogDTWithVocabDetails(details);
                    if(edxActivityDetailLogDT.getLogType()!=null && edxActivityDetailLogDT.getLogType().equals(EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure.name())){
                        if(edxActivityDetailLogDT.getComment()!=null && edxActivityDetailLogDT.getComment().indexOf(EdxELRConstant.MISSING_NOTF_REQ_FIELDS)!=-1){
                            edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_8);
                            edxLabInformationDto.setNotificationMissingFields(true);
                        }
                        else{
                            edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_10);
                        }
                        throw new DataProcessingException("MISSING NOTI REQUIRED: "+edxActivityDetailLogDT.getComment());
                    }else{
                        //edxLabInformationDto.setNotificationSuccessfullyCreated(true);
                        edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_6);
                    }

                }

            }
        } catch (Exception e) {
            if (nbsInterfaceModel != null) {
                nbsInterfaceModel.setRecordStatusCd("FAILED_V2_STEP_3");
                nbsInterfaceRepository.save(nbsInterfaceModel);
            }
        }
    }

    private Object processingELR(String data) throws DataProcessingConsumerException {
        NbsInterfaceModel nbsInterfaceModel = null;
        Object result = new Object();
        EdxLabInformationDto edxLabInformationDto = new EdxLabInformationDto();
        String detailedMsg = "";
        Gson gson = new Gson();
        try {

            edxLabInformationDto.setStatus(NbsInterfaceStatus.Success);
            edxLabInformationDto.setUserName(AuthUtil.authUser.getUserId());


            //TODO: uncomment when deploy
            nbsInterfaceModel = gson.fromJson(data, NbsInterfaceModel.class);


            //TODO: uncomment when debug
//             nbsInterfaceModel = nbsInterfaceRepository.findById(Integer.valueOf(data)).get();
//             nbsInterfaceModel.setObservationUid(null);

            edxLabInformationDto.setNbsInterfaceUid(nbsInterfaceModel.getNbsInterfaceUid());
            //loadAndInitCachedValue();

            CompletableFuture<Void> cacheLoadingFuture = loadAndInitCachedValueAsync();
            cacheLoadingFuture.join();


            LabResultProxyContainer labResultProxyContainer = dataExtractionService.parsingDataToObject(nbsInterfaceModel, edxLabInformationDto);

            edxLabInformationDto.setLabResultProxyContainer(labResultProxyContainer);

            if (nbsInterfaceModel.getObservationUid() != null && nbsInterfaceModel.getObservationUid() > 0) {
                edxLabInformationDto.setRootObserbationUid(nbsInterfaceModel.getObservationUid());
            }
            Long aPersonUid = null;

            ObservationDto observationDto;

            // Checking for matching observation
            managerAggregationService.processingObservationMatching(edxLabInformationDto, labResultProxyContainer, aPersonUid);


            // This process patient, provider, nok, and organization. Then it will update both parsedData and edxLabInformationDto accordingly
            managerAggregationService.serviceAggregationAsync(labResultProxyContainer, edxLabInformationDto);


            // Hit when Obs is matched
            if (edxLabInformationDto.isLabIsUpdateDRRQ() || edxLabInformationDto.isLabIsUpdateDRSA()) {
                managerUtil.setPersonUIDOnUpdate(aPersonUid, labResultProxyContainer);
            }
            edxLabInformationDto.setLabResultProxyContainer(labResultProxyContainer);

            String nbsOperation = edxLabInformationDto.isLabIsCreate() ? "ADD" : "EDIT";

            ObservationContainer orderTest = managerUtil.getObservationWithOrderDomainCode(labResultProxyContainer);

            String programAreaCd = orderTest.getTheObservationDto().getProgAreaCd();
            String jurisdictionCd = orderTest.getTheObservationDto().getJurisdictionCd();


            observationDto = observationService.processingLabResultContainer(labResultProxyContainer);

            if (edxLabInformationDto.isLabIsCreate()) {
                edxLabInformationDto.setLabIsCreateSuccess(true);
                edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_2);
            }

            edxLabInformationDto.setLocalId(observationDto.getLocalId());
            edxLabInformationDto.getEdxActivityLogDto().setBusinessObjLocalId(observationDto.getLocalId());
            edxLabInformationDto.setRootObserbationUid(observationDto.getObservationUid());

            if (observationDto.getProgAreaCd() != null && SrteCache.programAreaCodesMap.containsKey(observationDto.getProgAreaCd())) {
                edxLabInformationDto.setProgramAreaName(SrteCache.programAreaCodesMap.get(observationDto.getProgAreaCd()));
            }

            if (observationDto.getJurisdictionCd() != null && SrteCache.jurisdictionCodeMap.containsKey(observationDto.getJurisdictionCd())) {
                String jurisdictionName = SrteCache.jurisdictionCodeMap.get(observationDto.getJurisdictionCd());
                edxLabInformationDto.setJurisdictionName(jurisdictionName);
            }


            if (edxLabInformationDto.isLabIsCreateSuccess() && (edxLabInformationDto.getProgramAreaName() == null
                    || edxLabInformationDto.getJurisdictionName() == null)) {
                edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_1);
            }


            nbsInterfaceModel.setObservationUid(observationDto.getObservationUid().intValue());
            nbsInterfaceModel.setRecordStatusCd("COMPLETED_V2");
            nbsInterfaceRepository.save(nbsInterfaceModel);


            PublicHealthCaseFlowContainer phcContainer = new PublicHealthCaseFlowContainer();
            phcContainer.setLabResultProxyContainer(labResultProxyContainer);
            phcContainer.setEdxLabInformationDto(edxLabInformationDto);
            phcContainer.setObservationDto(observationDto);
            phcContainer.setNbsInterfaceId(nbsInterfaceModel.getNbsInterfaceUid());
            gson = new Gson();
            String jsonString = gson.toJson(phcContainer);
            kafkaManagerProducer.sendDataPhc(jsonString);

            //return result;
        } catch (Exception e) {
            if (nbsInterfaceModel != null) {
                nbsInterfaceModel.setRecordStatusCd("FAILED_V2");
                nbsInterfaceRepository.save(nbsInterfaceModel);
                System.out.println("ERROR");
            }
            String accessionNumberToAppend = "Accession Number:" + edxLabInformationDto.getFillerNumber();
            edxLabInformationDto.setStatus(NbsInterfaceStatus.Failure);
            edxLabInformationDto.setSystemException(true);

            if (e.toString().indexOf("Invalid XML") != -1) {
                edxLabInformationDto.setInvalidXML(true);
                edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_13);
            }

            if ((edxLabInformationDto.getPageActContainer() != null || edxLabInformationDto.getPamContainer() != null) && !edxLabInformationDto.isInvestigationSuccessfullyCreated()) {
                if (edxLabInformationDto.isInvestigationMissingFields()) {
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_5);
                } else {
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_9);
                }
            }
            else if ((edxLabInformationDto.getPageActContainer() != null || edxLabInformationDto.getPamContainer() != null) && edxLabInformationDto.isInvestigationSuccessfullyCreated())
            {
                if (edxLabInformationDto.isNotificationMissingFields()) {
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_8);
                } else {
                    edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_10);
                }
            }


            // error check function in here to create the details message

                logger.error("Exception EdxLabHelper.getUnProcessedELR processing exception: " + e, e);

                if(edxLabInformationDto.getErrorText()==null){
                    //if error text is null, that means lab was not created due to unexpected error.
                    if(e!=null && (e.getMessage().contains(EdxELRConstant.SQL_FIELD_TRUNCATION_ERROR_MSG) || e.getMessage().contains(EdxELRConstant.ORACLE_FIELD_TRUNCATION_ERROR_MSG))){
                        edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_18);
                        edxLabInformationDto.setFieldTruncationError(true);
                        edxLabInformationDto.setSystemException(false);
                        try{
                            // Extract table name from Exception message, first find table name and ignore text after it.
                            StringWriter errors = new StringWriter();
                            e.printStackTrace(new PrintWriter(errors));
                            String exceptionMessage = errors.toString();
                            //Patient is not created so setting patient_parent_id to 0
                            edxLabInformationDto.setPersonParentUid(0);
                            //No need to create success message "The Ethnicity code provided in the message is not found in the SRT database. The code is saved to the NBS." in case of exception scenario
                            edxLabInformationDto.setEthnicityCodeTranslated(true);
                            String textToLookFor = "Table Name : ";
                            String tableName = exceptionMessage.substring(exceptionMessage.indexOf(textToLookFor)+textToLookFor.length());
                            tableName = tableName.substring(0, tableName.indexOf(" "));
                            detailedMsg = "SQLException while inserting into "+tableName+"\n "+accessionNumberToAppend+"\n "+exceptionMessage;
                            detailedMsg = detailedMsg.substring(0,Math.min(detailedMsg.length(), 2000));
                        }catch(Exception ex){
                            logger.error("Exception while formatting exception message for Activity Log: "+ex.getMessage(), ex);
                        }
                    } else if (e!=null && e.getMessage().contains(EdxELRConstant.DATE_VALIDATION)) {
                        edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_20);
                        edxLabInformationDto.setInvalidDateError(true);
                        edxLabInformationDto.setSystemException(false);

                        //Patient is not created so setting patient_parent_id to 0
                        edxLabInformationDto.setPersonParentUid(0);
                        //No need to create success message for Ethnic code
                        edxLabInformationDto.setEthnicityCodeTranslated(true);
                        try {
                            // Extract problem date from Exception message
                            String problemDateInfoSubstring = e.getMessage().substring(e.getMessage().indexOf(EdxELRConstant.DATE_VALIDATION));
                            problemDateInfoSubstring = problemDateInfoSubstring.substring(0,problemDateInfoSubstring.indexOf(EdxELRConstant.DATE_VALIDATION_END_DELIMITER1));
                            detailedMsg = problemDateInfoSubstring+"\n "+accessionNumberToAppend+"\n"+e.getMessage();
                            detailedMsg = detailedMsg.substring(0,Math.min(detailedMsg.length(), 2000));
                        }catch(Exception ex){
                            logger.error("Exception while formatting date exception message for Activity Log: "+ex.getMessage(), ex);
                        }
                    }else{
                        edxLabInformationDto.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_16);
                        try{
                            //Patient is not created so setting patient_parent_id to 0
                            edxLabInformationDto.setPersonParentUid(0);
                            //No need to create success message for Ethnicity code provided in the message is not found in the SRT database. The code is saved to the NBS." in case of exception scenario
                            edxLabInformationDto.setEthnicityCodeTranslated(true);
                            StringWriter errors = new StringWriter();
                            e.printStackTrace(new PrintWriter(errors));
                            String exceptionMessage = accessionNumberToAppend+"\n"+errors.toString();
                            detailedMsg = exceptionMessage.substring(0,Math.min(exceptionMessage.length(), 2000));
                        }catch(Exception ex){
                            logger.error("Exception while formatting exception message for Activity Log: "+ex.getMessage(), ex);
                        }
                    }
                }
                if( edxLabInformationDto.isInvestigationMissingFields() || edxLabInformationDto.isNotificationMissingFields() || (edxLabInformationDto.getErrorText()!=null && edxLabInformationDto.getErrorText().equals(EdxELRConstant.ELR_MASTER_LOG_ID_10))){
                    edxLabInformationDto.setSystemException(false);
                }

                if(edxLabInformationDto.isReflexResultedTestCdMissing() || edxLabInformationDto.isResultedTestNameMissing() || edxLabInformationDto.isOrderTestNameMissing() || edxLabInformationDto.isReasonforStudyCdMissing()){
                    try{
                        String exceptionMsg = e.getMessage();
                        String textToLookFor = "XMLElementName: ";
                        detailedMsg = "Blank identifiers in segments "+exceptionMsg.substring(exceptionMsg.indexOf(textToLookFor)+textToLookFor.length())+"\n\n"+accessionNumberToAppend;
                        detailedMsg = detailedMsg.substring(0,Math.min(detailedMsg.length(), 2000));
                    }catch(Exception ex){
                        logger.error("Exception while formatting exception message for Activity Log: "+ex.getMessage(), ex);
                    }
                }

            //throw new DataProcessingConsumerException(e.getMessage(), result);

        } finally {
            // do logging in here since we want it to be done within the first flow and not wait for the 2nd flow (health case flow)
            // and keep public health case stuff in the try
            //            if(result != null) {
            if(nbsInterfaceModel != null) {
                System.out.println("Source name: " + edxLabInformationDto.getSendingFacilityName());
                System.out.println("edxLabInformationDto.getErrorText():"+edxLabInformationDto.getErrorText());
                edxLogService.updateActivityLogDT(nbsInterfaceModel, edxLabInformationDto);
                edxLogService.addActivityDetailLogs(edxLabInformationDto, detailedMsg);
                gson = new Gson();
                String jsonString = gson.toJson(edxLabInformationDto.getEdxActivityLogDto());
                System.out.println("inside finally block jsonString: " + jsonString);
                kafkaManagerProducer.sendDataEdxActivityLog(jsonString);
            }
        }
        return result;
    }

    private CompletableFuture<Void> loadAndInitCachedValueAsync() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            if (SrteCache.loincCodesMap.isEmpty()) {
                try {
                    cachingValueService.getAOELOINCCodes();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.raceCodesMap.isEmpty()) {
                try {
                    cachingValueService.getRaceCodes();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.programAreaCodesMap.isEmpty()) {
                try {
                    cachingValueService.getAllProgramAreaCodes();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.jurisdictionCodeMap.isEmpty()) {
                try {
                    cachingValueService.getAllJurisdictionCode();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.jurisdictionCodeMapWithNbsUid.isEmpty()) {
                try {
                    cachingValueService.getAllJurisdictionCodeWithNbsUid();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.programAreaCodesMapWithNbsUid.isEmpty()) {
                try {
                    cachingValueService.getAllProgramAreaCodesWithNbsUid();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.elrXrefsList.isEmpty()) {
                try {
                    cachingValueService.getAllElrXref();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.coInfectionConditionCode.isEmpty()) {
                try {
                    cachingValueService.getAllOnInfectionConditionCode();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.conditionCodes.isEmpty() || SrteCache.investigationFormConditionCode.isEmpty()) {
                try {
                    cachingValueService.getAllConditionCode();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.labResultByDescMap.isEmpty()) {
                try {
                    cachingValueService.getLabResultDesc();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.snomedCodeByDescMap.isEmpty()) {
                try {
                    cachingValueService.getAllSnomedCode();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.labResultWithOrganismNameIndMap.isEmpty()) {
                try {
                    cachingValueService.getAllLabResultJoinWithLabCodingSystemWithOrganismNameInd();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            if (SrteCache.loinCodeWithComponentNameMap.isEmpty()) {
                try {
                    cachingValueService.getAllLoinCodeWithComponentName();
                } catch (DataProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).thenRun(() -> {
            // Retrieve cached values using Cache.ValueWrapper
            var cache = cacheManager.getCache("srte");
            if (cache != null) {
                Cache.ValueWrapper valueWrapper;
                valueWrapper = cache.get("loincCodes");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof TreeMap) {
                        SrteCache.loincCodesMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("raceCodes");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof TreeMap) {
                        SrteCache.raceCodesMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("programAreaCodes");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof TreeMap) {
                        SrteCache.programAreaCodesMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("jurisdictionCode");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof TreeMap) {
                        SrteCache.jurisdictionCodeMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("programAreaCodesWithNbsUid");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof TreeMap) {
                        SrteCache.programAreaCodesMapWithNbsUid = (TreeMap<String, Integer>) cachedObject;
                    }
                }

                valueWrapper = cache.get("jurisdictionCodeWithNbsUid");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof TreeMap) {
                        SrteCache.jurisdictionCodeMapWithNbsUid = (TreeMap<String, Integer>) cachedObject;
                    }
                }

                valueWrapper = cache.get("elrXref");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.elrXrefsList = (List<ElrXref>) cachedObject;
                    }
                }


                valueWrapper = cache.get("coInfectionConditionCode");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.coInfectionConditionCode = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("conditionCode");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.conditionCodes = (List<ConditionCode>) cachedObject;

                        // Populate Code for Investigation Form
                        for (ConditionCode obj : SrteCache.conditionCodes) {
                            SrteCache.investigationFormConditionCode.put(obj.getConditionCd(), obj.getInvestigationFormCd());
                        }

                    }
                }

                valueWrapper = cache.get("labResulDesc");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.labResultByDescMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("snomedCodeByDesc");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.snomedCodeByDescMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("labResulDescWithOrgnismName");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.labResultWithOrganismNameIndMap = (TreeMap<String, String>) cachedObject;
                    }
                }

                valueWrapper = cache.get("loinCodeWithComponentName");
                if (valueWrapper != null) {
                    Object cachedObject = valueWrapper.get();
                    if (cachedObject instanceof List) {
                        SrteCache.loinCodeWithComponentNameMap = (TreeMap<String, String>) cachedObject;
                    }
                }




            }
        });

        return future;
    }

    private void requiredFieldError(String errorTxt, EdxLabInformationDto edxLabInformationDT) throws DataProcessingException {
        if (errorTxt != null) {
            edxLabInformationDT	.setErrorText(EdxELRConstant.ELR_MASTER_LOG_ID_5);
            if (edxLabInformationDT.getEdxActivityLogDto().getEDXActivityLogDTWithVocabDetails() == null)
            {
                edxLabInformationDT.getEdxActivityLogDto().setEDXActivityLogDTWithVocabDetails(
                        new ArrayList<EDXActivityDetailLogDto>());
            }

            //TODO: LOGGING
//            setActivityDetailLog((ArrayList<Object>) edxLabInformationDT.getEdxActivityLogDto().getEDXActivityLogDTWithVocabDetails(),
//                    String.valueOf(edxLabInformationDT.getLocalId()),
//                    EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, errorTxt);

            edxLabInformationDT.setInvestigationMissingFields(true);
            throw new DataProcessingException("MISSING REQUIRED FIELDS: "+errorTxt);
        }
    }

}
