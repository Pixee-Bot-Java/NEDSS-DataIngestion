package gov.cdc.dataprocessing.service.implementation.log;

import com.google.gson.Gson;
import gov.cdc.dataprocessing.constant.elr.EdxELRConstant;
import gov.cdc.dataprocessing.exception.EdxLogException;
import gov.cdc.dataprocessing.kafka.producer.KafkaManagerProducer;
import gov.cdc.dataprocessing.model.container.EdxActivityLogContainer;
import gov.cdc.dataprocessing.model.dto.edx.EdxRuleAlgorothmManagerDto;
import gov.cdc.dataprocessing.model.dto.lab_result.EdxLabInformationDto;
import gov.cdc.dataprocessing.model.dto.log.EDXActivityDetailLogDto;
import gov.cdc.dataprocessing.model.dto.log.EDXActivityLogDto;
import gov.cdc.dataprocessing.repository.nbs.msgoute.model.NbsInterfaceModel;
import gov.cdc.dataprocessing.repository.nbs.odse.model.log.EdxActivityDetailLog;
import gov.cdc.dataprocessing.repository.nbs.odse.model.log.EdxActivityLog;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.log.EdxActivityDetailLogRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.log.EdxActivityLogRepository;
import gov.cdc.dataprocessing.service.interfaces.log.IEdxLogService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static gov.cdc.dataprocessing.utilities.time.TimeStampUtil.getCurrentTimeStamp;

@Service
@Slf4j
public class EdxLogService implements IEdxLogService {

    private static final Logger logger = LoggerFactory.getLogger(EdxLogService.class);

    private final EdxActivityLogRepository edxActivityLogRepository;
    private final EdxActivityDetailLogRepository edxActivityDetailLogRepository;
    private final KafkaManagerProducer kafkaManagerProducer;


    public EdxLogService(EdxActivityLogRepository edxActivityLogRepository,
                         EdxActivityDetailLogRepository edxActivityDetailLogRepository,
                         KafkaManagerProducer kafkaManagerProducer) {
        this.edxActivityLogRepository = edxActivityLogRepository;
        this.edxActivityDetailLogRepository = edxActivityDetailLogRepository;
        this.kafkaManagerProducer = kafkaManagerProducer;
    }

    public Object processingLog() throws EdxLogException {
        try {
            return "processing log";
        } catch (Exception e) {
            throw new EdxLogException("ERROR", "Data");
        }
    }

    @Transactional
    @Override
    public EdxActivityLog saveEdxActivityLog(EDXActivityLogDto edxActivityLogDto) throws EdxLogException {
        EdxActivityLog edxActivityLog = new EdxActivityLog(edxActivityLogDto);
        EdxActivityLog edxActivityLogResult = edxActivityLogRepository.save(edxActivityLog);
        System.out.println("ActivityLog Id:" + edxActivityLogResult.getId());
        return edxActivityLogResult;
    }

    @Transactional
    @Override
    public EdxActivityDetailLog saveEdxActivityDetailLog(EDXActivityDetailLogDto detailLogDto) throws EdxLogException {
        EdxActivityDetailLog edxActivityDetailLog = new EdxActivityDetailLog(detailLogDto);
        EdxActivityDetailLog edxActivityDetailLogResult = edxActivityDetailLogRepository.save(edxActivityDetailLog);
        System.out.println("ActivityDetailLog Id:" + edxActivityDetailLogResult.getId());
        return edxActivityDetailLogResult;
    }
    @Transactional
    public void saveEdxActivityLogs(String logMessageJson) throws EdxLogException {
        Gson gson = new Gson();
        EDXActivityLogDto edxActivityLogDto = gson.fromJson(logMessageJson, EDXActivityLogDto.class);
        EdxActivityLog edxActivityLog = new EdxActivityLog(edxActivityLogDto);
        EdxActivityLog edxActivityLogResult = edxActivityLogRepository.save(edxActivityLog);
        System.out.println("ActivityLog Id:" + edxActivityLogResult.getId());

        Collection<EDXActivityDetailLogDto> edxActivityDetailLogsList= edxActivityLogDto.getEDXActivityLogDTWithVocabDetails();
        System.out.println("edxActivityDetailLogs size:" + edxActivityDetailLogsList.size());
        for (EDXActivityDetailLogDto eDXActivityDetailLogDto: edxActivityDetailLogsList) {
            eDXActivityDetailLogDto.setEdxActivityLogUid(edxActivityLogResult.getId());
            saveEdxActivityDetailLog(eDXActivityDetailLogDto);
        }
    }

    public void testKafkaproduceLogMessage() {
        EdxActivityLogContainer edxActivityLogContainer = new EdxActivityLogContainer();
        //Activity Log
        EDXActivityLogDto edxActivityLogDto = new EDXActivityLogDto();
        edxActivityLogDto.setSourceUid(12345678L);
        edxActivityLogDto.setTargetUid(12345L);
        edxActivityLogDto.setTargetUid(4567L);
        edxActivityLogDto.setDocType("test doc type1");
        edxActivityLogDto.setRecordStatusCd("Test status cd1");
        edxActivityLogDto.setRecordStatusTime(getCurrentTimeStamp());
        edxActivityLogDto.setExceptionTxt("test exception1");
        edxActivityLogDto.setImpExpIndCd("I");
        edxActivityLogDto.setSourceTypeCd("INT");
        edxActivityLogDto.setSourceUid(6789L);
        edxActivityLogDto.setTargetTypeCd("LAB");
        edxActivityLogDto.setBusinessObjLocalId("TESTBO1231");
        edxActivityLogDto.setDocName("DOC NAME1");
        edxActivityLogDto.setSrcName("TSTSRCNM1");
        edxActivityLogDto.setAlgorithmAction("TSTALGACT1");
        edxActivityLogDto.setAlgorithmName("TSTALGNM1");
        edxActivityLogDto.setMessageId("TSTMSGID1231");
        edxActivityLogDto.setEntityNm("TEST Entity name1");
        edxActivityLogDto.setAccessionNbr("TST ACC 501");

        //Activity Detail Log
        EDXActivityDetailLogDto detailLogDto = new EDXActivityDetailLogDto();
        //detailLogDto.setEdxActivityDetailLogUid(12345678L);
        //detailLogDto.setEdxActivityLogUid(72276L);
        detailLogDto.setRecordId("RC2341");
        detailLogDto.setRecordType("TEST Record Type1");
        detailLogDto.setRecordName("TEST_Record Name1");
        detailLogDto.setLogType("TEST Log Type1");
        detailLogDto.setComment("TEST Comment text12331");

        edxActivityLogContainer.setEdxActivityLogDto(edxActivityLogDto);
//        edxActivityLogContainer.setEdxActivityDetailLogDto(detailLogDto);

        Gson gson = new Gson();
        String activityLogJsonString = gson.toJson(edxActivityLogContainer);
        System.out.println("--json string to topic edx activity log---:" + activityLogJsonString);
        kafkaManagerProducer.sendDataEdxActivityLog(activityLogJsonString);
    }
    public void updateActivityLogDT(NbsInterfaceModel nbsInterfaceModel, EdxLabInformationDto edxLabInformationDto) {
        EDXActivityLogDto edxActivityLogDto = edxLabInformationDto.getEdxActivityLogDto();
        Date dateTime = new Date();
        Timestamp time = new Timestamp(dateTime.getTime());
        nbsInterfaceModel.setRecordStatusTime(time);

        edxActivityLogDto.setLogDetailAllStatus(true);
        edxActivityLogDto.setSourceUid(Long.valueOf(nbsInterfaceModel.getNbsInterfaceUid()));
        edxActivityLogDto.setTargetUid(edxLabInformationDto.getRootObserbationUid());

        setActivityLogExceptionTxt(edxActivityLogDto, edxLabInformationDto.getErrorText());

        edxActivityLogDto.setImpExpIndCd("I");
        edxActivityLogDto.setRecordStatusTime(time);
        edxActivityLogDto.setSourceTypeCd("INT");
        edxActivityLogDto.setTargetTypeCd("LAB");
        edxActivityLogDto.setDocType(EdxELRConstant.ELR_DOC_TYPE_CD);
        edxActivityLogDto.setRecordStatusCd(edxLabInformationDto.getStatus().toString());

        if (edxLabInformationDto.getFillerNumber() != null && edxLabInformationDto.getFillerNumber().length() > 100) {
            edxActivityLogDto.setAccessionNbr(edxLabInformationDto.getFillerNumber().substring(0, 100));
        } else {
            edxActivityLogDto.setAccessionNbr(edxLabInformationDto.getFillerNumber());
        }

        edxActivityLogDto.setMessageId(edxLabInformationDto.getMessageControlID());
        edxActivityLogDto.setEntityNm(edxLabInformationDto.getEntityName());

        edxActivityLogDto.setSrcName(edxLabInformationDto.getSendingFacilityName());
        edxActivityLogDto.setBusinessObjLocalId(edxLabInformationDto.getLocalId());
        edxActivityLogDto.setAlgorithmName(edxLabInformationDto.getDsmAlgorithmName());
        edxActivityLogDto.setAlgorithmAction(edxLabInformationDto.getAction());

    }

    private void setActivityDetailLog(ArrayList<EDXActivityDetailLogDto> detailLogs, String id, EdxRuleAlgorothmManagerDto.STATUS_VAL status, String comment) {
        EDXActivityDetailLogDto edxActivityDetailLogDto = new EDXActivityDetailLogDto();
        edxActivityDetailLogDto.setRecordId(id);
        edxActivityDetailLogDto.setRecordType(EdxELRConstant.ELR_RECORD_TP);
        edxActivityDetailLogDto.setRecordName(EdxELRConstant.ELR_RECORD_NM);
        edxActivityDetailLogDto.setLogType(status.name());
        edxActivityDetailLogDto.setComment(comment);
        detailLogs.add(edxActivityDetailLogDto);
    }
    private void setActivityLogExceptionTxt(EDXActivityLogDto edxActivityLogDto, String errorText) {
        System.out.println("setActivityLogExceptionTxt errorText: " + errorText);
        switch (errorText) {
            case EdxELRConstant.ELR_MASTER_LOG_ID_1:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_1);
            case EdxELRConstant.ELR_MASTER_LOG_ID_2:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_2);
            case EdxELRConstant.ELR_MASTER_LOG_ID_3:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_3);
            case EdxELRConstant.ELR_MASTER_LOG_ID_4:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_4);
            case EdxELRConstant.ELR_MASTER_LOG_ID_5:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_5);
            case EdxELRConstant.ELR_MASTER_LOG_ID_6:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_6);
            case EdxELRConstant.ELR_MASTER_LOG_ID_7:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_7);
            case EdxELRConstant.ELR_MASTER_LOG_ID_8:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_8);
            case EdxELRConstant.ELR_MASTER_LOG_ID_9:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_9);
            case EdxELRConstant.ELR_MASTER_LOG_ID_10:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_10);
            case EdxELRConstant.ELR_MASTER_LOG_ID_11:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_11);
            case EdxELRConstant.ELR_MASTER_LOG_ID_12:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_12);
            case EdxELRConstant.ELR_MASTER_LOG_ID_13:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_13);
            case EdxELRConstant.ELR_MASTER_LOG_ID_14:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_14);
            case EdxELRConstant.ELR_MASTER_LOG_ID_15:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_15);
            case EdxELRConstant.ELR_MASTER_LOG_ID_16:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_16);
            case EdxELRConstant.ELR_MASTER_LOG_ID_17:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_17);
            case EdxELRConstant.ELR_MASTER_LOG_ID_18:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_18);
            case EdxELRConstant.ELR_MASTER_LOG_ID_19:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_19);
            case EdxELRConstant.ELR_MASTER_LOG_ID_20:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_20);
            case EdxELRConstant.ELR_MASTER_LOG_ID_21:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_21);
            case EdxELRConstant.ELR_MASTER_LOG_ID_22:
                edxActivityLogDto.setExceptionTxt(EdxELRConstant.ELR_MASTER_MSG_ID_22);
            default:
                //return;
        }
    }

    public void addActivityDetailLogs(EdxLabInformationDto edxLabInformationDto, String detailedMsg) {
        System.out.println(" addActivityDetailLogs detailedMsg:" + detailedMsg);
        try{
            ArrayList<EDXActivityDetailLogDto> detailList =
                    (ArrayList<EDXActivityDetailLogDto>) edxLabInformationDto.getEdxActivityLogDto().getEDXActivityLogDTDetails();
            if (detailList == null) {
                detailList = new ArrayList<>();
            }
            String id = String.valueOf(edxLabInformationDto.getLocalId());
            boolean errorReturned = false;

            // TODO: Need to complete the detail activity logs

            if (edxLabInformationDto.isInvalidXML()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, EdxELRConstant.INVALID_XML);
                errorReturned = true;
            } else if (edxLabInformationDto.isMultipleOBR()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.MULTIPLE_OBR);
                errorReturned = true;
            } else if (!edxLabInformationDto.isFillerNumberPresent()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.FILLER_FAIL);
                errorReturned = true;
            } else if (edxLabInformationDto.isOrderTestNameMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_ORDTEST_NAME + " " + detailedMsg);
                errorReturned = true;
            } else if (edxLabInformationDto.isReflexOrderedTestCdMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_REFLEX_ORDERED_NM);
                errorReturned = true;
            } else if (edxLabInformationDto.isReflexResultedTestCdMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_REFLEX_RESULT_NM + " " + detailedMsg);
                errorReturned = true;
            } else if (edxLabInformationDto.isResultedTestNameMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_RESULT_NAME + " " + detailedMsg);
                errorReturned = true;
            } else if (edxLabInformationDto.isReasonforStudyCdMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_REASON_FOR_STUDY + " " + detailedMsg);
                errorReturned = true;
            } else if (edxLabInformationDto.isDrugNameMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_DRUG_NAME);
                errorReturned = true;
            } else if (edxLabInformationDto.isMultipleSubject()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.MULTIPLE_SUBJECT);
                errorReturned = true;
            } else if (edxLabInformationDto.isNoSubject()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_SUBJECT);
                errorReturned = true;
            } else if (edxLabInformationDto.isChildOBRWithoutParent()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.CHILD_OBR_WITHOUT_PARENT);
                errorReturned = true;
            } else if (edxLabInformationDto.isOrderOBRWithParent()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.ORDER_OBR_WITH_PARENT);
                errorReturned = true;
            } else if (!edxLabInformationDto.isObsStatusTranslated()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.TRANSLATE_OBS_STATUS);
                errorReturned = true;
            } else if (edxLabInformationDto.isUniversalServiceIdMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.UNIVSRVCID);
                errorReturned = true;
            } else if (edxLabInformationDto.isActivityToTimeMissing()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.ODSACTIVTOTIME_FAIL);
                errorReturned = true;
            } else if (edxLabInformationDto.isActivityTimeOutOfSequence()) {
                String msg = EdxELRConstant.LABTEST_SEQUENCE.replace("%1", edxLabInformationDto.getFillerNumber());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                errorReturned = true;
            } else if (edxLabInformationDto.isFinalPostCorrected()) {
                String msg = EdxELRConstant.FINAL_POST_CORRECTED.replace("%1",
                        edxLabInformationDto.getLocalId()).replace("%2",
                        edxLabInformationDto.getFillerNumber());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                errorReturned = true;
            } else if (edxLabInformationDto.isPreliminaryPostFinal()) {
                String msg = EdxELRConstant.PRELIMINARY_POST_FINAL.replace("%1",
                        edxLabInformationDto.getLocalId()).replace("%2",
                        edxLabInformationDto.getFillerNumber());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                errorReturned = true;
            } else if (edxLabInformationDto.isPreliminaryPostCorrected()) {
                String msg = EdxELRConstant.PRELIMINARY_POST_CORRECTED.replace("%1",
                        edxLabInformationDto.getLocalId()).replace("%2",
                        edxLabInformationDto.getFillerNumber());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                errorReturned = true;
            } else if (edxLabInformationDto.isMissingOrderingProviderandFacility()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_ORDERINGPROVIDER);
                errorReturned = true;
            } else if (edxLabInformationDto.isUnexpectedResultType()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.UNEXPECTED_RESULT_TYPE);
                errorReturned = true;
            } else if (edxLabInformationDto.isChildSuscWithoutParentResult()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.CHILD_SUSC_WITH_NO_PARENT_RESULT);
                errorReturned = true;
            } else if (!edxLabInformationDto.isCreateLabPermission()) {
                String msg = EdxELRConstant.NO_LAB_CREATE_PERMISSION.replace("%1", edxLabInformationDto.getUserName());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                errorReturned = true;
            } else if (!edxLabInformationDto.isUpdateLabPermission()) {
                String msg = EdxELRConstant.NO_LAB_UPDATE_PERMISSION.replace("%1", edxLabInformationDto.getUserName());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                errorReturned = true;
            } else if (!edxLabInformationDto.isMarkAsReviewPermission()) {
                String msg = EdxELRConstant.NO_LAB_MARK_REVIEW_PERMISSION.replace("%1", edxLabInformationDto.getUserName());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
            } else if (!edxLabInformationDto.isCreateInvestigationPermission()) {
                String msg = EdxELRConstant.NO_INV_PERMISSION.replace("%1", edxLabInformationDto.getUserName());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.OFCI);
            } else if (!edxLabInformationDto.isCreateNotificationPermission()) {
                String msg = EdxELRConstant.NO_NOT_PERMISSION.replace("%1", edxLabInformationDto.getUserName());
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, msg);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.OFCN);
            } else if (edxLabInformationDto.isFieldTruncationError()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, detailedMsg);
            } else if (edxLabInformationDto.isInvalidDateError()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, detailedMsg);
                errorReturned = true;
            } else if (!errorReturned && edxLabInformationDto.isSystemException()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure, detailedMsg);
                if (edxLabInformationDto.getLabResultProxyContainer() == null)
                    errorReturned = true;
                if (edxLabInformationDto.isLabIsCreate() && !edxLabInformationDto.isLabIsCreateSuccess())
                    errorReturned = true;
                else if ((edxLabInformationDto.isLabIsUpdateDRRQ() || edxLabInformationDto.isLabIsUpdateDRSA()) && !edxLabInformationDto.isLabIsUpdateSuccess())
                    errorReturned = true;
                if (edxLabInformationDto.isInvestigationSuccessfullyCreated())
                    setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                            EdxELRConstant.OFCN);
                else if (edxLabInformationDto.isLabIsCreateSuccess())
                    setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                            EdxELRConstant.OFCI);
            }
            if (errorReturned) {
                edxLabInformationDto.getEdxActivityLogDto().setEDXActivityLogDTWithVocabDetails(detailList);
                return;
            }
            if (edxLabInformationDto.isMultipleSubjectMatch()) {
                String msg = EdxELRConstant.SUBJECTMATCH_MULT.replace("%1",
                        edxLabInformationDto.getEntityName()).replace("%2",
                        edxLabInformationDto.getPersonParentUid() + "");
                setActivityDetailLog(detailList,
                        edxLabInformationDto.getPersonParentUid() + "",
                        EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
            }
            if (edxLabInformationDto.isPatientMatch()) {
                String msg = EdxELRConstant.SUBJECT_MATCH_FOUND.replace("%1",
                        edxLabInformationDto.getEntityName()).replace("%2",
                        edxLabInformationDto.getPersonParentUid() + "");
                setActivityDetailLog(detailList,
                        edxLabInformationDto.getPersonParentUid() + "",
                        EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
            }
            if (!edxLabInformationDto.isMultipleSubjectMatch() && !edxLabInformationDto.isPatientMatch() && edxLabInformationDto.getPersonParentUid() != 0) {
                String msg = EdxELRConstant.SUJBECTMATCH_NO.replace("%1",
                        edxLabInformationDto.getEntityName()).replace("%2",
                        edxLabInformationDto.getPersonParentUid() + "");
                setActivityDetailLog(detailList,
                        edxLabInformationDto.getPersonParentUid() + "",
                        EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
            }
            if (edxLabInformationDto.isLabIsCreateSuccess() && edxLabInformationDto.getJurisdictionName() != null) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.JURISDICTION_DERIVED);
            }
            if (edxLabInformationDto.isLabIsCreateSuccess() && edxLabInformationDto.getJurisdictionName() == null) {
                edxLabInformationDto.setSystemException(false);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_JURISDICTION_DERIVED);
            }
            if (edxLabInformationDto.isLabIsCreateSuccess() && edxLabInformationDto.getProgramAreaName() != null) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.PROG_AREA_DERIVED);
            }
            if (edxLabInformationDto.isLabIsCreateSuccess() && edxLabInformationDto.getProgramAreaName() == null) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_PROG_AREA_DERIVED);
            }
            if (!edxLabInformationDto.isMatchingAlgorithm()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure,
                        EdxELRConstant.NO_MATCHING_ALGORITHM);
            }
            if (edxLabInformationDto.isLabIsCreateSuccess()) {
                String msg = EdxELRConstant.LAB_CREATE_SUCCESS.replace("%1", id);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, EdxELRConstant.DOC_CREATE_SUCCESS);
            }
            if (edxLabInformationDto.isLabIsUpdateSuccess()) {
                String msg = null;
                if (edxLabInformationDto.isLabIsUpdateDRRQ())
                    msg = EdxELRConstant.LAB_UPDATE_SUCCESS_DRRQ.replace("%1", id);
                else if (edxLabInformationDto.isLabIsUpdateDRSA())
                    msg = EdxELRConstant.LAB_UPDATE_SUCCESS_DRSA.replace("%1", id);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, EdxELRConstant.DOC_CREATE_SUCCESS);
            }
            if (!edxLabInformationDto.isMissingOrderingProviderandFacility() && edxLabInformationDto.isMissingOrderingProvider()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MISSING_ORD_PROV);
            }
            if (!edxLabInformationDto.isMissingOrderingProviderandFacility() && edxLabInformationDto.isMissingOrderingFacility()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MISSING_ORD_FAC);
            }
            if (edxLabInformationDto.isMultipleOrderingProvider()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MULTIPLE_PROVIDER);
            }
            if (edxLabInformationDto.isMultipleCollector()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MULTIPLE_COLLECTOR);
            }
            if (edxLabInformationDto.isMultiplePrincipalInterpreter()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MULTIPLE_INTERP);
            }
            if (edxLabInformationDto.isMultipleOrderingFacility()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MULTIPLE_ORDERFAC);
            }
            if (edxLabInformationDto.isMultipleReceivingFacility()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MULTIPLE_RECEIVEFAC);
            }
            if (edxLabInformationDto.isMultipleSpecimen()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.MULTIPLE_SPECIMEN);
            }
            if (!edxLabInformationDto.isEthnicityCodeTranslated()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.TRANSLATE_ETHN_GRP);
            }
            if (!edxLabInformationDto.isObsMethodTranslated()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.TRANSLATE_OBS_METH);
            }
            if (!edxLabInformationDto.isRaceTranslated()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.TRANSLATE_RACE);
            }
            if (!edxLabInformationDto.isSexTranslated()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.TRANSLATE_SEX);
            }
            if (edxLabInformationDto.isSsnInvalid()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.INFO_SSN_INVALID);
            }
            if (edxLabInformationDto.isNullClia()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.NULL_CLIA);
            }
            if (edxLabInformationDto.isInvestigationSuccessfullyCreated()) {
                String msg = EdxELRConstant.INV_SUCCESS_CREATED.replace("%1", edxLabInformationDto.getPublicHealthCaseUid() + "");
                setActivityDetailLog(detailList, edxLabInformationDto.getPublicHealthCaseUid() + "", EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
            }
            if (edxLabInformationDto.isLabAssociatedToInv()) {
                String msg = EdxELRConstant.LAB_ASSOCIATED_TO_INV.replace("%1", edxLabInformationDto.getPublicHealthCaseUid() + "");
                setActivityDetailLog(detailList, edxLabInformationDto.getPublicHealthCaseUid() + "", EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
            }

            if (edxLabInformationDto.isNotificationSuccessfullyCreated()) {
                String msg = EdxELRConstant.NOT_SUCCESS_CREATED.replace("%1", edxLabInformationDto.getNotificationUid() + "");
                setActivityDetailLog(detailList, edxLabInformationDto.getNotificationUid() + "", EdxRuleAlgorothmManagerDto.STATUS_VAL.Success, msg);
            }
            if (edxLabInformationDto.isInvestigationMissingFields()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.OFCI);
            }
            if (edxLabInformationDto.isNotificationMissingFields()) {
                setActivityDetailLog(detailList, id, EdxRuleAlgorothmManagerDto.STATUS_VAL.Success,
                        EdxELRConstant.OFCN);
            }
            edxLabInformationDto.getEdxActivityLogDto().setEDXActivityLogDTWithVocabDetails(detailList);
        }
        catch (Exception e) {
            String id = String.valueOf(edxLabInformationDto.getLocalId());
            ArrayList<EDXActivityDetailLogDto> delailList = (ArrayList<EDXActivityDetailLogDto>)edxLabInformationDto.getEdxActivityLogDto().getEDXActivityLogDTWithVocabDetails();
            if (delailList == null) {
                delailList = new ArrayList<EDXActivityDetailLogDto>();
            }
            return;
        }
    }
}
