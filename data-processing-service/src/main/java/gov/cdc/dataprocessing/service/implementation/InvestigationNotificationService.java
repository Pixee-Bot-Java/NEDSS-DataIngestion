package gov.cdc.dataprocessing.service.implementation;

import gov.cdc.dataprocessing.cache.SrteCache;
import gov.cdc.dataprocessing.constant.EdxPHCRConstants;
import gov.cdc.dataprocessing.constant.elr.EdxELRConstant;
import gov.cdc.dataprocessing.constant.elr.NEDSSConstant;
import gov.cdc.dataprocessing.exception.DataProcessingException;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.dto.PublicHealthCaseDT;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.vo.NotificationVO;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.vo.PageActProxyVO;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.vo.PublicHealthCaseVO;
import gov.cdc.dataprocessing.model.container.BasePamContainer;
import gov.cdc.dataprocessing.model.container.NotificationProxyContainer;
import gov.cdc.dataprocessing.model.container.PamProxyContainer;
import gov.cdc.dataprocessing.model.container.PersonContainer;
import gov.cdc.dataprocessing.model.dto.NbsQuestionMetadata;
import gov.cdc.dataprocessing.model.dto.act.ActIdDto;
import gov.cdc.dataprocessing.model.dto.act.ActRelationshipDto;
import gov.cdc.dataprocessing.model.dto.edx.EdxRuleAlgorothmManagerDto;
import gov.cdc.dataprocessing.model.dto.entity.EntityLocatorParticipationDto;
import gov.cdc.dataprocessing.model.dto.locator.PostalLocatorDto;
import gov.cdc.dataprocessing.model.dto.log.EDXActivityDetailLogDto;
import gov.cdc.dataprocessing.model.dto.notification.NotificationDto;
import gov.cdc.dataprocessing.model.dto.participation.ParticipationDto;
import gov.cdc.dataprocessing.model.dto.person.PersonDto;
import gov.cdc.dataprocessing.model.dto.person.PersonRaceDto;
import gov.cdc.dataprocessing.repository.nbs.odse.model.custom_model.QuestionRequiredNnd;
import gov.cdc.dataprocessing.repository.nbs.odse.model.person.Person;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.CustomNbsQuestionRepository;
import gov.cdc.dataprocessing.service.interfaces.IInvestigationNotificationService;
import gov.cdc.dataprocessing.service.interfaces.IInvestigationService;
import gov.cdc.dataprocessing.service.interfaces.notification.INotificationService;
import gov.cdc.dataprocessing.utilities.component.patient.PatientRepositoryUtil;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class InvestigationNotificationService  implements IInvestigationNotificationService {
    private IInvestigationService investigationService;
    private INotificationService notificationService;
    private CustomNbsQuestionRepository customNbsQuestionRepository;

    public InvestigationNotificationService(
            IInvestigationService investigationService,
            INotificationService notificationService,
            CustomNbsQuestionRepository customNbsQuestionRepository) {
        this.investigationService = investigationService;
        this.notificationService = notificationService;
        this.customNbsQuestionRepository = customNbsQuestionRepository;
    }

    public EDXActivityDetailLogDto sendNotification(Object pageObj, String nndComment) throws DataProcessingException {
        NotificationProxyContainer notProxyVO = null;
        // Create the Notification object
        PublicHealthCaseVO publicHealthCaseVO;
        if (pageObj instanceof PageActProxyVO) {
            publicHealthCaseVO = ((PageActProxyVO) pageObj).getPublicHealthCaseVO();
        } else if (pageObj instanceof PamProxyContainer) {
            publicHealthCaseVO = ((PamProxyContainer) pageObj).getPublicHealthCaseVO();
        } else if (pageObj instanceof PublicHealthCaseVO) {
            publicHealthCaseVO = ((PublicHealthCaseVO) pageObj);
        } else {
            throw new DataProcessingException("Cannot create Notification for unknown page type: " + pageObj.getClass().getCanonicalName());
        }
        NotificationDto notDT = new NotificationDto();
        notDT.setItNew(true);
        notDT.setNotificationUid(-1L);
        notDT.setAddTime(new java.sql.Timestamp(new Date().getTime()));
        notDT.setTxt(nndComment);
        notDT.setStatusCd("A");
        notDT.setCaseClassCd(publicHealthCaseVO.getThePublicHealthCaseDT().getCaseClassCd());
        notDT.setStatusTime(new java.sql.Timestamp(new Date().getTime()));
        notDT.setVersionCtrlNbr(1);
        notDT.setSharedInd("T");
        notDT.setCaseConditionCd(publicHealthCaseVO.getThePublicHealthCaseDT().getCd());
        notDT.setAutoResendInd("F");

        NotificationVO notVO = new NotificationVO();
        notVO.setTheNotificationDT(notDT);
        notVO.setItNew(true);

        // create the act relationship between the phc & notification
        ActRelationshipDto actDT1 = new ActRelationshipDto();
        actDT1.setItNew(true);
        actDT1.setTargetActUid(publicHealthCaseVO.getThePublicHealthCaseDT().getPublicHealthCaseUid());
        actDT1.setSourceActUid(notDT.getNotificationUid());
        actDT1.setAddTime(new java.sql.Timestamp(new Date().getTime()));
        actDT1.setRecordStatusCd(NEDSSConstant.RECORD_STATUS_ACTIVE);
        actDT1.setSequenceNbr(1);
        actDT1.setStatusCd("A");
        actDT1.setStatusTime(new java.sql.Timestamp(new Date().getTime()));
        actDT1.setTypeCd(NEDSSConstant.ACT106_TYP_CD);
        actDT1.setSourceClassCd(NEDSSConstant.ACT106_SRC_CLASS_CD);
        actDT1.setTargetClassCd(NEDSSConstant.ACT106_TAR_CLASS_CD);

        notProxyVO = new NotificationProxyContainer();
        notProxyVO.setItNew(true);
        notProxyVO.setThePublicHealthCaseVO(publicHealthCaseVO);
        notProxyVO.setTheNotificationVO(notVO);

        ArrayList<Object> actRelColl = new ArrayList<Object>();
        actRelColl.add(0, actDT1);
        notProxyVO.setTheActRelationshipDTCollection(actRelColl);

        // EdxPHCRDocumentUtil.sendProxyToEJB(notProxyVO, pageObj);
        EDXActivityDetailLogDto eDXActivityDetailLogDT = sendProxyToEJB(notProxyVO, pageObj);
        return eDXActivityDetailLogDT;
    }


    private EDXActivityDetailLogDto  sendProxyToEJB(NotificationProxyContainer notificationProxyVO, Object pageObj)throws DataProcessingException
    {
        HashMap<Object,Object> nndRequiredMap = new HashMap<>();
        EDXActivityDetailLogDto eDXActivityDetailLogDT = new EDXActivityDetailLogDto();

        eDXActivityDetailLogDT.setRecordType(EdxPHCRConstants.MSG_TYPE.Notification.name());
        eDXActivityDetailLogDT.setRecordName("PHCR_IMPORT");
        eDXActivityDetailLogDT.setLogType(EdxRuleAlgorothmManagerDto.STATUS_VAL.Success.name());

        try {
            boolean formatErr = false;

            PublicHealthCaseDT phcDT = notificationProxyVO.getThePublicHealthCaseVO().getThePublicHealthCaseDT();
            Long publicHealthCaseUid = phcDT.getPublicHealthCaseUid();
            try
            {
                Map<Object,Object> subMap = new HashMap<>();
                TreeMap<String, String> condAndFormCdTreeMap = SrteCache.investigationFormConditionCode;

                String investigationFormCd = condAndFormCdTreeMap.get(phcDT.getCd());
                Collection<QuestionRequiredNnd>  notifReqColl = new ArrayList<>();

                    notifReqColl = customNbsQuestionRepository.retrieveQuestionRequiredNnd(investigationFormCd);

                    if(notifReqColl != null && notifReqColl.size() > 0) {
                        for (QuestionRequiredNnd questionRequiredNnd : notifReqColl) {
                            NbsQuestionMetadata metaData = new NbsQuestionMetadata(questionRequiredNnd);
                            subMap.put(metaData.getNbsQuestionUid(), metaData);
                        }
                    }

                    Map<?,?> result = null;
                    try {
                        result= validatePAMNotficationRequiredFieldsGivenPageProxy(pageObj, publicHealthCaseUid, subMap,investigationFormCd);
                        StringBuffer errorText =new StringBuffer(20);
                        if(result!=null && result.size()>0){
                            int i =  result.size();
                            Collection<?> coll =result.values();
                            Iterator<?> it= coll.iterator();
                            while(it.hasNext()){
                                String label = (String)it.next();
                                --i;
                                errorText.append("["+label+"]");
                                if(it.hasNext()){
                                    errorText.append("; and ");
                                }
                                if(i==0)
                                    errorText.append(".");

                            }
                            formatErr = true;
                            eDXActivityDetailLogDT.setLogType(EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure.name());
                            eDXActivityDetailLogDT.setComment(EdxELRConstant.MISSING_NOTF_REQ_FIELDS+ errorText.toString());
                            return eDXActivityDetailLogDT;
                        }
                    }
                    catch (Exception e) {
                        throw new Exception(e.toString(), e);
                    }

            }
            catch (Exception ex) {
                throw new Exception(ex.toString(), ex);
            }
            String programAreaCd = notificationProxyVO.getThePublicHealthCaseVO().getThePublicHealthCaseDT().getProgAreaCd();
            NotificationVO notifVO = notificationProxyVO.getTheNotificationVO();
            NotificationDto notifDT = notifVO.getTheNotificationDT();
            notifDT.setProgAreaCd(programAreaCd);
            notifVO.setTheNotificationDT(notifDT);
            notificationProxyVO.setTheNotificationVO(notifVO);
            Long realNotificationUid = setNotificationProxy(notificationProxyVO);
            eDXActivityDetailLogDT.setRecordId(""+realNotificationUid);
            if (!formatErr)
            {
                eDXActivityDetailLogDT.setComment("Notification created (UID: "+realNotificationUid+")");
            }

        } catch (Exception e) {
            e.printStackTrace();
            eDXActivityDetailLogDT.setLogType(EdxRuleAlgorothmManagerDto.STATUS_VAL.Failure.name());
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String exceptionMessage = errors.toString();
            exceptionMessage = exceptionMessage.substring(0,Math.min(exceptionMessage.length(), 2000));
            eDXActivityDetailLogDT.setComment(exceptionMessage);
        }
        return eDXActivityDetailLogDT;
    }


    /**
     * Returns the list of Fields that are required (and not filled) to Create Notification from PAM Cases
     * @param publicHealthCaseUid
     * @param reqFields
     * @return
     */
    private Map<Object, Object> validatePAMNotficationRequiredFieldsGivenPageProxy(Object pageObj, Long publicHealthCaseUid,
                                                                                  Map<Object, Object>  reqFields, String formCd) throws DataProcessingException {

        Map<Object, Object>  missingFields = new TreeMap<>();

        try {
            BasePamContainer pamVO=null;
            Collection<ParticipationDto> participationDTCollection  = null;
            PublicHealthCaseDT publicHealthCaseDT = null;
            Collection<PersonContainer> personVOCollection = null;
            Map<Object, Object>  answerMap = null;
            Collection<ActIdDto>  actIdColl = null;

            //NOTE TODO: ONLY SPECIFIC USE CASE GOT TO THIS ONE -- HAVE NOT FOUND ONE SO FAR
            if(formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_RVCT)||formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_VAR))
            {
                PamProxyContainer proxyVO = new PamProxyContainer();
                if(pageObj == null || pageObj instanceof PublicHealthCaseVO)
                {
                    //TODO PAM
                    // proxyVO =  pamproxy.getPamProxy(publicHealthCaseUid);
                }
                else
                {
                    proxyVO = (PamProxyContainer) pageObj;
                }
                pamVO = proxyVO.getPamVO();
                answerMap = pamVO.getPamAnswerDTMap();
                if(pageObj == null || pageObj instanceof  PublicHealthCaseVO)
                {
                    participationDTCollection  = proxyVO.getPublicHealthCaseVO().getTheParticipationDTCollection();
                }
                else
                {
                    participationDTCollection = proxyVO.getTheParticipationDTCollection();
                }
                personVOCollection  = proxyVO.getThePersonVOCollection();
                publicHealthCaseDT = proxyVO.getPublicHealthCaseVO().getThePublicHealthCaseDT();
                actIdColl = proxyVO.getPublicHealthCaseVO().getTheActIdDTCollection();
            }
            else
            {
                // HIT THIS
                PageActProxyVO pageProxyVO  = null;
                if(pageObj == null  || pageObj instanceof  PublicHealthCaseVO)
                {
                    pageProxyVO =  investigationService.getPageProxyVO(NEDSSConstant.CASE, publicHealthCaseUid);
                }
                else
                {
                    pageProxyVO = (PageActProxyVO) pageObj;
                }
                PageActProxyVO pageActProxyVO=pageProxyVO;
                pamVO=pageActProxyVO.getPageVO();

                answerMap = (pageProxyVO).getPageVO().getPamAnswerDTMap();
                if(pageObj == null || pageObj instanceof  PublicHealthCaseVO)
                {
                    participationDTCollection  = pageActProxyVO.getPublicHealthCaseVO().getTheParticipationDTCollection();
                }
                else
                {
                    participationDTCollection = pageActProxyVO.getTheParticipationDtoCollection();
                }
                personVOCollection  = pageActProxyVO.getThePersonContainerCollection();
                publicHealthCaseDT = pageActProxyVO.getPublicHealthCaseVO().getThePublicHealthCaseDT();
                actIdColl = pageActProxyVO.getPublicHealthCaseVO().getTheActIdDTCollection();
            }


            PersonContainer personVO = getPersonVO(NEDSSConstant.PHC_PATIENT, participationDTCollection,personVOCollection );
            PersonDto personDT = personVO.getThePersonDto();


            String programAreaCode = publicHealthCaseDT.getProgAreaCd();
            String jurisdictionCode = publicHealthCaseDT.getJurisdictionCd();
            String shared = publicHealthCaseDT.getSharedInd();
            if (publicHealthCaseDT == null)
            {
                throw new DataProcessingException("publicHealthCaseDT is null ");
            }

            // TODO: SECURITY CHECK
//            if (!nbsSecurityObj.getPermission(NBSBOLookup.NOTIFICATION,
//                    "CREATE",
//                    programAreaCode,
//                    jurisdictionCode, shared)) {
//                if (!nbsSecurityObj.getPermission(NBSBOLookup.NOTIFICATION,
//                        NBSOperationLookup.
//                                CREATENEEDSAPPROVAL,
//                        programAreaCode,
//                        jurisdictionCode, shared))
//
//                {
//                    logger.info(
//                            "no review permissions for validateRvctNotficationRequiredFields");
//                    throw new NEDSSSystemException("NO CREATE or CREATE NEEDS APPROVAL PERMISSIONS for NotificcationProxyEJB - validatePAMNotficationRequiredFieldsGivenPageProxy");
//                }
//
//            }
            //Iterate through the reqFields  Map<Object, Object>  and find missing NND Req questions answered by looking @ datalocation
            for (Object o : reqFields.keySet()) {
                Long key = (Long) o;
                NbsQuestionMetadata metaData = (NbsQuestionMetadata) reqFields.get(key);
                String dLocation = metaData.getDataLocation() == null ? "" : metaData.getDataLocation();
                String label = metaData.getQuestionLabel() == null ? "" : metaData.getQuestionLabel();
                Long nbsQueUid = metaData.getNbsQuestionUid();
                if (!dLocation.equals("")) {
                    if (dLocation.startsWith("NBS_Answer.")) {
                        if (answerMap.get(key) == null) {
                            missingFields.put(metaData.getQuestionIdentifier(), metaData.getQuestionLabel());
                        }
                    } else if (dLocation.toLowerCase().startsWith("public_health_case.")) {
                        String attrToChk = dLocation.substring(dLocation.indexOf(".") + 1);

                        String getterNm = createGetterMethod(attrToChk);
                        Map<Object, Object> methodMap = getMethods(publicHealthCaseDT.getClass());
                        Method method = (Method) methodMap.get(getterNm.toLowerCase());
                        Object obj = method.invoke(publicHealthCaseDT, (Object[]) null);
                        checkObject(obj, missingFields, metaData);
                    } else if (dLocation.toLowerCase().startsWith("person.")) {
                        String attrToChk = dLocation.substring(dLocation.indexOf(".") + 1);
                        String getterNm = createGetterMethod(attrToChk);
                        Map<Object, Object> methodMap = getMethods(personDT.getClass());
                        Method method = (Method) methodMap.get(getterNm.toLowerCase());
                        Object obj = method.invoke(personDT, (Object[]) null);
                        checkObject(obj, missingFields, metaData);
                    } else if (dLocation.toLowerCase().startsWith("postal_locator.")) {
                        String attrToChk = dLocation.substring(dLocation.indexOf(".") + 1);
                        String getterNm = createGetterMethod(attrToChk);
                        PostalLocatorDto postalLocator = new PostalLocatorDto();
                        Map<Object, Object> methodMap = getMethods(postalLocator.getClass());
                        Method method = (Method) methodMap.get(getterNm.toLowerCase());
                        if (personVO != null
                                && personVO.getTheEntityLocatorParticipationDtoCollection() != null
                                && personVO.getTheEntityLocatorParticipationDtoCollection().size() > 0) {
                            for (EntityLocatorParticipationDto elp : personVO.getTheEntityLocatorParticipationDtoCollection()) {
                                if (elp.getThePostalLocatorDto() != null) {
                                    //check if this is the correct entity locator to check
                                    if (elp.getUseCd() != null &&
                                            metaData.getDataUseCd() != null &&
                                            metaData.getDataUseCd().equalsIgnoreCase(elp.getUseCd())) {
                                        postalLocator = elp.getThePostalLocatorDto();
                                        Object obj = method.invoke(postalLocator, (Object[]) null);
                                        checkObject(obj, missingFields, metaData);
                                    }
                                } else if (elp.getClassCd() != null
                                        && elp.getClassCd().equals("PST")
                                        && elp.getTheTeleLocatorDto() == null) {
                                    checkObject(null, missingFields, metaData);
                                }
                            }
                        } else {
                            checkObject(null, missingFields, metaData);
                        }
                    } else if (dLocation.toLowerCase().startsWith("person_race.")) {
                        String attrToChk = dLocation.substring(dLocation.indexOf(".") + 1);
                        String getterNm = createGetterMethod(attrToChk);
                        PersonRaceDto personRace = new PersonRaceDto();
                        Map<Object, Object> methodMap = getMethods(personRace.getClass());
                        Method method = (Method) methodMap.get(getterNm.toLowerCase());
                        if (personVO != null
                                && personVO.getThePersonRaceDtoCollection() != null
                                && personVO.getThePersonRaceDtoCollection().size() > 0) {
                            for (PersonRaceDto personRaceDto : personVO.getThePersonRaceDtoCollection()) {
                                personRace = personRaceDto;
                                Object obj = method.invoke(personRace, (Object[]) null);
                                checkObject(obj, missingFields, metaData);
                            }
                        } else {
                            checkObject(null, missingFields, metaData);
                        }
                    }
                    else if (dLocation.toLowerCase().startsWith("act_id."))
                    {
                        String attrToChk = dLocation.substring(dLocation.indexOf(".") + 1);
                        String getterNm = createGetterMethod(attrToChk);
                        if (actIdColl != null && actIdColl.size() > 0) {
                            for (ActIdDto adt : actIdColl) {
                                String typeCd = adt.getTypeCd() == null ? "" : adt.getTypeCd();
                                String value = adt.getRootExtensionTxt() == null ? "" : adt.getRootExtensionTxt();
                                if (typeCd.equalsIgnoreCase(NEDSSConstant.ACT_ID_STATE_TYPE_CD) && value.equals("") && (label.toLowerCase().indexOf("state") != -1)) {
                                    Map<Object, Object> methodMap = getMethods(adt.getClass());
                                    Method method = (Method) methodMap.get(getterNm.toLowerCase());
                                    Object obj = method.invoke(adt, (Object[]) null);
                                    checkObject(obj, missingFields, metaData);
                                } else if (typeCd.equalsIgnoreCase(NEDSSConstant.ACT_ID_STATE_TYPE_CD)
                                        && formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_RVCT)
                                        && (label.toLowerCase().indexOf("state") != -1)) {
                                    Map<Object, Object> methodMap = getMethods(adt.getClass());
                                    Method method = (Method) methodMap.get(getterNm.toLowerCase());
                                    Object obj = method.invoke(adt, (Object[]) null);
                                    checkObject(obj, missingFields, metaData);
                                } else if (typeCd.equalsIgnoreCase("CITY")
                                        && value.equals("") && (label.toLowerCase().indexOf("city") != -1)) {
                                    Map<Object, Object> methodMap = getMethods(adt.getClass());
                                    Method method = (Method) methodMap.get(getterNm.toLowerCase());
                                    Object obj = method.invoke(adt, (Object[]) null);
                                    checkObject(obj, missingFields, metaData);
                                }
                            }
                        } else if (formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_RVCT)
                                && (label.toLowerCase().indexOf("state") != -1)) {
                            missingFields.put(metaData.getQuestionIdentifier(), metaData.getQuestionLabel());
                        }
                    } else if (dLocation.toLowerCase().startsWith("nbs_case_answer.")
                            && !(formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_RVCT)
                            || formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_VAR))) {
                        if (answerMap == null || answerMap.size() == 0 || (answerMap.get(nbsQueUid) == null && answerMap.get(metaData.getQuestionIdentifier()) == null)) {
                            missingFields.put(metaData.getQuestionIdentifier(), metaData.getQuestionLabel());
                        }
                    } else if (dLocation.toLowerCase().startsWith("nbs_case_answer.") && (formCd.equalsIgnoreCase(NEDSSConstant.INV_FORM_RVCT))) {
                        if (answerMap == null || answerMap.size() == 0 || (answerMap.get(nbsQueUid) == null && answerMap.get(metaData.getQuestionIdentifier()) == null)) {
                            missingFields.put(metaData.getQuestionIdentifier(), metaData.getQuestionLabel());
                        }
                    }

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new DataProcessingException(e.getMessage(),e);
        }
        if (missingFields.size() == 0)
        {
            return null;
        }
        else
        {
            return missingFields;
        }
    }


    private String createGetterMethod(String attrToChk) throws DataProcessingException {

        try {
            StringTokenizer tokenizer = new StringTokenizer(attrToChk,"_");
            String methodName = "";
            while (tokenizer.hasMoreTokens()){
                String token = tokenizer.nextToken();
                methodName = methodName + Character.toUpperCase(token.charAt(0)) +
                        token.substring(1).toLowerCase();

            }
            return "get" + methodName;
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }


    private  Map<Object, Object>  getMethods(Class beanClass) throws DataProcessingException {
        try {
            Method[] gettingMethods = beanClass.getMethods();
            Map<Object, Object>  resultMap = new HashMap<Object, Object>();
            for (Method gettingMethod : gettingMethods) {
                Method method = (Method) gettingMethod;
                String methodName = method.getName().toLowerCase();
                resultMap.put(methodName, method);
            }
            return resultMap;
        } catch (SecurityException e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void checkObject(Object obj,  Map<Object, Object>  missingFields, NbsQuestionMetadata metaData) throws DataProcessingException {
        try {
            String value = obj == null ? "" : obj.toString();
            if(value == null || (value != null && value.trim().length() == 0)) {
                missingFields.put(metaData.getQuestionIdentifier(), metaData.getQuestionLabel());
            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }

    }

    private PersonContainer getPersonVO(String type_cd, Collection<ParticipationDto> participationDTCollection,
                                        Collection<PersonContainer> personVOCollection) throws DataProcessingException {
        try {
            ParticipationDto participationDT = null;
            PersonContainer personVO = null;
            if (participationDTCollection  != null) {
                Iterator<ParticipationDto> anIterator1 = null;
                Iterator<PersonContainer> anIterator2 = null;
                for (anIterator1 = participationDTCollection.iterator(); anIterator1.hasNext();) {
                    participationDT =  anIterator1.next();
                    if (participationDT.getTypeCd() != null && (participationDT.getTypeCd()).compareTo(type_cd) == 0) {
                        for (anIterator2 = personVOCollection.iterator(); anIterator2.hasNext();) {
                            personVO =  anIterator2.next();
                            if (personVO.getThePersonDto().getPersonUid().longValue() == participationDT
                                    .getSubjectEntityUid().longValue()) {
                                return personVO;
                            }
                            else
                            {
                                continue;
                            }
                        }
                    }
                    else
                    {
                        continue;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataProcessingException(e.getMessage(), e);
        }
    }


    private Long setNotificationProxy(NotificationProxyContainer notificationProxyVO) throws DataProcessingException
    {

        try {
            Long notificationUid = notificationService.setNotificationProxy(notificationProxyVO);
            return notificationUid;
        }
        catch (Exception ex) {
            throw new DataProcessingException(ex.getMessage(),ex);
        }
    }

}
