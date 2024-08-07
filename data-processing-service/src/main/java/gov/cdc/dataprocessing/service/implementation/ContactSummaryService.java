package gov.cdc.dataprocessing.service.implementation;

import gov.cdc.dataprocessing.constant.CTConstants;
import gov.cdc.dataprocessing.constant.elr.NBSBOLookup;
import gov.cdc.dataprocessing.constant.elr.NEDSSConstant;
import gov.cdc.dataprocessing.exception.DataProcessingException;
import gov.cdc.dataprocessing.model.CTContactSummaryDT;
import gov.cdc.dataprocessing.model.dto.person.PersonNameDto;
import gov.cdc.dataprocessing.repository.nbs.odse.model.person.PersonName;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.CustomRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.person.PersonNameRepository;
import gov.cdc.dataprocessing.service.interfaces.IContactSummaryService;
import gov.cdc.dataprocessing.service.interfaces.IRetrieveSummaryService;
import gov.cdc.dataprocessing.utilities.component.QueryHelper;
import org.springframework.stereotype.Service;

import java.util.*;

import static gov.cdc.dataprocessing.constant.ComplexQueries.*;

@Service
public class ContactSummaryService implements IContactSummaryService {

    private final QueryHelper queryHelper;
    private final PersonNameRepository personNameRepository;
    private final CustomRepository customRepository;
    private final IRetrieveSummaryService retrieveSummaryService;

    public ContactSummaryService(QueryHelper queryHelper,
                                 PersonNameRepository personNameRepository,
                                 CustomRepository customRepository, IRetrieveSummaryService retrieveSummaryService) {
        this.queryHelper = queryHelper;
        this.personNameRepository = personNameRepository;
        this.customRepository = customRepository;
        this.retrieveSummaryService = retrieveSummaryService;
    }

    public Collection<Object> getContactListForInvestigation(Long publicHealthCaseUID) throws DataProcessingException {
        Collection<Object> coll = new ArrayList<>();
        try{
            coll.addAll(getPHCContactNamedByPatientSummDTColl(publicHealthCaseUID));
            coll.addAll(getPHCPatientNamedAsContactSummDTColl(publicHealthCaseUID));
            coll.addAll(getPHCPatientOtherNamedAsContactSummDTColl(publicHealthCaseUID));
        }catch(Exception ex){
            throw new DataProcessingException(ex.toString());
        }
        return coll;
    }

    private Collection<Object> getPHCContactNamedByPatientSummDTColl(Long publicHealthCaseUID) throws DataProcessingException {
        try{
            String dataAccessWhereClause = queryHelper.getDataAccessWhereClause(NBSBOLookup.CT_CONTACT,"VIEW", "");

            if (dataAccessWhereClause == null) {
                dataAccessWhereClause = "";
            }
            else {
                dataAccessWhereClause = " AND " + dataAccessWhereClause;
                dataAccessWhereClause = dataAccessWhereClause.replaceAll("program_jurisdiction_oid", "CT_CONTACT.program_jurisdiction_oid");
                dataAccessWhereClause = dataAccessWhereClause.replaceAll("shared_ind", "CT_CONTACT.shared_ind_cd");
            }

            String dataAccessWhereClause1 = queryHelper.getDataAccessWhereClause(NBSBOLookup.INVESTIGATION, "VIEW", "");

            if (dataAccessWhereClause1 == null) {
                dataAccessWhereClause1 = "";
            }
            else {
                dataAccessWhereClause1 = " AND " + dataAccessWhereClause1;
                dataAccessWhereClause1 = dataAccessWhereClause1.replaceAll("program_jurisdiction_oid", "contact.program_jurisdiction_oid");
                dataAccessWhereClause1 = dataAccessWhereClause1.replaceAll("shared_ind", "contact.shared_ind");
            }
            Collection<Object>  PHCcTContactNameByPatientSummDTColl  = new ArrayList<Object> ();
            String sql  =SELECT_PHCPAT_NAMED_BY_PATIENT_COLLECTION1 + dataAccessWhereClause1
                    + SELECT_PHCPAT_NAMED_BY_PATIENT_COLLECTION3 + publicHealthCaseUID+ dataAccessWhereClause;
            PHCcTContactNameByPatientSummDTColl = getContactNamedByPatientDTColl(sql);
            return PHCcTContactNameByPatientSummDTColl;
        }catch(Exception ex){
            throw new DataProcessingException(ex.toString());
        }
    }

    private Collection<Object> getPHCPatientNamedAsContactSummDTColl(Long publicHealthCaseUID) throws DataProcessingException {
        try{
            String dataAccessWhereClause = queryHelper.getDataAccessWhereClause(NBSBOLookup.INVESTIGATION, "VIEW", "");
            if (dataAccessWhereClause == null) {
                dataAccessWhereClause = "";
            }
            else {
                dataAccessWhereClause = " AND " + dataAccessWhereClause;
                dataAccessWhereClause = dataAccessWhereClause.replaceAll("program_jurisdiction_oid", "subject.program_jurisdiction_oid");
                dataAccessWhereClause = dataAccessWhereClause.replaceAll("shared_ind", "subject.shared_ind");
            }
            String dataAccessWhereClause1 = queryHelper.getDataAccessWhereClause(NBSBOLookup.CT_CONTACT, "VIEW", "");
            if (dataAccessWhereClause1 == null) {
                dataAccessWhereClause1 = "";
            }
            else {
                dataAccessWhereClause1 = " AND " + dataAccessWhereClause1;
                dataAccessWhereClause1 = dataAccessWhereClause1.replaceAll("program_jurisdiction_oid", "CT_CONTACT.program_jurisdiction_oid");
                dataAccessWhereClause1 = dataAccessWhereClause1.replaceAll("shared_ind", "CT_CONTACT.shared_ind_cd");
            }
            Collection<Object>  PHCcTContactNameByPatientSummDTColl  = new ArrayList<Object> ();
            String sql  = SELECT_PHCPAT_NAMED_BY_CONTACT_COLLECTION +publicHealthCaseUID
                    + dataAccessWhereClause + dataAccessWhereClause1;
            PHCcTContactNameByPatientSummDTColl = getPatientNamedAsContactSummDTColl(sql, false);
            return PHCcTContactNameByPatientSummDTColl;
        }catch(Exception ex){
            throw new DataProcessingException(ex.toString());
        }
    }


    private Collection<Object> getPHCPatientOtherNamedAsContactSummDTColl(Long publicHealthCaseUID) throws DataProcessingException {
        try{
            String dataAccessWhereClause = queryHelper.getDataAccessWhereClause(NBSBOLookup.INVESTIGATION, "VIEW", "");
            if (dataAccessWhereClause == null) {
                dataAccessWhereClause = "";
            }
            else {
                dataAccessWhereClause = " AND " + dataAccessWhereClause;
                dataAccessWhereClause = dataAccessWhereClause.replaceAll("program_jurisdiction_oid", "subject.program_jurisdiction_oid");
                dataAccessWhereClause = dataAccessWhereClause.replaceAll("shared_ind", "subject.shared_ind");
            }
            String dataAccessWhereClause1 = queryHelper.getDataAccessWhereClause(NBSBOLookup.CT_CONTACT, "VIEW", "");

            if (dataAccessWhereClause1 == null) {
                dataAccessWhereClause1 = "";
            }
            else {
                dataAccessWhereClause1 = " AND " + dataAccessWhereClause1;
                dataAccessWhereClause1 = dataAccessWhereClause1.replaceAll("program_jurisdiction_oid", "CT_CONTACT.program_jurisdiction_oid");
                dataAccessWhereClause1 = dataAccessWhereClause1.replaceAll("shared_ind", "CT_CONTACT.shared_ind_cd");
            }
            Collection<Object>  PHCcTContactNameByPatientSummDTColl  = new ArrayList<Object> ();
            String sql  =SELECT_PHCPAT_OTHER_NAMED_BY_CONTACT_COLLECTION + publicHealthCaseUID
                    + dataAccessWhereClause+dataAccessWhereClause1;
            PHCcTContactNameByPatientSummDTColl = getPatientNamedAsContactSummDTColl(sql, true);
            return PHCcTContactNameByPatientSummDTColl;
        }catch(Exception ex){
            throw new DataProcessingException(ex.toString());
        }
    }


    private  Collection<Object> getContactNamedByPatientDTColl(String sql) throws DataProcessingException {
        CTContactSummaryDT cTContactSummaryDT  = new CTContactSummaryDT();
        ArrayList<CTContactSummaryDT>  cTContactNameByPatientSummDTColl  = new ArrayList<> ();
        ArrayList<Object>  returnCTContactNameByPatientSummDTColl  = new ArrayList<Object> ();
        try
        {
            cTContactNameByPatientSummDTColl  = new ArrayList<>(customRepository.getContactByPatientInfo(sql));

            Iterator<CTContactSummaryDT> it = cTContactNameByPatientSummDTColl.iterator();
            while(it.hasNext()){
                CTContactSummaryDT cTContactSumyDT = (CTContactSummaryDT)it.next();
                cTContactSumyDT.setContactNamedByPatient(true);
                Long contactEntityUid = cTContactSumyDT.getContactEntityUid();
                var lst = personNameRepository.findByParentUid(contactEntityUid);
                Collection personNameColl = new ArrayList<>();
                if (lst.isPresent()) {
                    personNameColl = lst.get();
                }

                //add the contact summary dt
                returnCTContactNameByPatientSummDTColl.add(cTContactSumyDT);

                var lst2 = personNameRepository.findByParentUid(contactEntityUid);
                Collection contactNameColl = new ArrayList<>();
                if (lst.isPresent()) {
                    contactNameColl = lst.get();
                }

                if(contactNameColl != null && contactNameColl.size() > 0) {
                    Iterator iter = contactNameColl.iterator();
                    while(iter.hasNext()){
                        PersonNameDto personNameDT = (PersonNameDto)iter.next();
                        if(personNameDT.getNmUseCd().equalsIgnoreCase(NEDSSConstant.LEGAL_NAME)){
                            String lastName = (personNameDT.getLastNm()==null)?"No Last":personNameDT.getLastNm();
                            String firstName = (personNameDT.getFirstNm()==null)?"No First":personNameDT.getFirstNm();
                            String personName = lastName + ", " + firstName;
                            cTContactSumyDT.setName(personName);
                            cTContactSumyDT.setContactName(personName);
                            break;
                        }
                    } //name iter
                }//name coll not null
                //Other Infected Person is seldom present 
                Long otherEntityUid = cTContactSumyDT.getThirdPartyEntityUid();
                if (otherEntityUid != null) {
                    Collection<PersonName> ctOtherNameColl = new ArrayList<>();
                    var lst3 = personNameRepository.findByParentUid(otherEntityUid);
                    if (lst3.isPresent()) {
                        ctOtherNameColl = lst3.get();
                    }
                    if(ctOtherNameColl != null && ctOtherNameColl.size() > 0) {
                        Iterator<PersonName> otherIter = ctOtherNameColl.iterator();
                        while(otherIter.hasNext()){
                            PersonNameDto personNameDT =  new PersonNameDto(otherIter.next());
                            if (personNameDT.getNmUseCd().equalsIgnoreCase(NEDSSConstant.LEGAL_NAME)) {
                                String lastName = (personNameDT.getLastNm()==null)?"No Last":personNameDT.getLastNm();
                                String firstName = (personNameDT.getFirstNm()==null)?"No First":personNameDT.getFirstNm();
                                String personName =lastName + ", " + firstName;
                                cTContactSumyDT.setOtherInfectedPatientName(personName);
                                break;
                            }
                        }
                    }
                }
                //Business rule with convoluted logic, if contact Processing Decision is RSC or SR and the contiact's investigation disposition
                // is A, the disposition of the Contact Record will be Z. 
                //If the disposition on the Contact�s existing investigation is C, the disposition of the Contact Record will be E.
                if (cTContactSumyDT.getContactProcessingDecisionCd() != null &&
                        cTContactSumyDT.getDispositionCd() != null &&
                        (cTContactSumyDT.getContactProcessingDecisionCd().equals(CTConstants.RecordSearchClosure)
                                || cTContactSumyDT.getContactProcessingDecisionCd().equals(CTConstants.SecondaryReferral))) {
                    if (cTContactSumyDT.getDispositionCd().equals("A")) //preventative treatment
                        cTContactSumyDT.setDispositionCd("Z"); //prev preventative treated
                    else if (cTContactSumyDT.getDispositionCd().equals("C")) //infected brought to treat
                        cTContactSumyDT.setDispositionCd("E"); //prev treated
                }
            } //while
        }
        catch (Exception ex) {
            throw new DataProcessingException(ex.toString());
        }
        return returnCTContactNameByPatientSummDTColl;
    }


    private Collection<Object> getPatientNamedAsContactSummDTColl(String sql, boolean otherInfected) throws  DataProcessingException {
        CTContactSummaryDT  ctContactSummaryDT  = new CTContactSummaryDT();
        ArrayList<CTContactSummaryDT>  ctNameByPatientSummDTColl  = new ArrayList<> ();
        ArrayList<Object>  returnCTNameByPatientSummDTColl  = new ArrayList<Object> ();
        try
        {
            ctNameByPatientSummDTColl  = new ArrayList<>(customRepository.getContactByPatientInfo(sql));
            Iterator<CTContactSummaryDT> it = ctNameByPatientSummDTColl.iterator();
            while(it.hasNext()){
                CTContactSummaryDT cTContactSumyDT = (CTContactSummaryDT)it.next();
                cTContactSumyDT.setContactNamedByPatient(false);
                cTContactSumyDT.setPatientNamedByContact(true);
                cTContactSumyDT.setOtherNamedByPatient(otherInfected);

                cTContactSumyDT.setAssociatedMap(retrieveSummaryService.getAssociatedDocumentList(
                                cTContactSumyDT.getCtContactUid(),
                                NEDSSConstant.CLASS_CD_CONTACT,
                                NEDSSConstant.ACT_CLASS_CD_FOR_DOC));
                //go ahead and add the summary dt into the collection
                returnCTNameByPatientSummDTColl.add(cTContactSumyDT);

                //get the subject name
                Long contactSubjectEntityUid = cTContactSumyDT.getSubjectEntityUid();


                Collection<PersonName> subjectNameColl = new ArrayList<>();

                var lst = personNameRepository.findByParentUid(contactSubjectEntityUid);
                if(lst.isPresent()) {
                    subjectNameColl = lst.get();
                }

                if(subjectNameColl != null && subjectNameColl.size() > 0) {
                    Iterator<PersonName> iter = subjectNameColl.iterator();
                    while(iter.hasNext()){
                        PersonNameDto personNameDT = new PersonNameDto(iter.next());
                        if(personNameDT.getNmUseCd().equalsIgnoreCase(NEDSSConstant.LEGAL_NAME)){
                            String lastName = (personNameDT.getLastNm()==null)?"No Last":personNameDT.getLastNm();
                            String firstName = (personNameDT.getFirstNm()==null)?"No First":personNameDT.getFirstNm();
                            String personName =lastName + ", " + firstName;
                            cTContactSumyDT.setNamedBy(personName);
                            cTContactSumyDT.setSubjectName(personName);
                            break;
                        }
                    }
                }

                //get the Contact Name 
                Long contactEntityUid = cTContactSumyDT.getContactEntityUid();
                if (contactEntityUid != null) {
                    Collection<PersonName> contactNameColl = new ArrayList<>();

                    var lst2 = personNameRepository.findByParentUid(contactEntityUid);
                    if(lst2.isPresent()) {
                        contactNameColl = lst.get();
                    }


                    if(contactNameColl != null && contactNameColl.size() > 0) {
                        Iterator<PersonName> ctIter = contactNameColl.iterator();
                        while(ctIter.hasNext()){
                            PersonNameDto personNameDT = new PersonNameDto(ctIter.next());
                            if (personNameDT.getNmUseCd().equalsIgnoreCase(NEDSSConstant.LEGAL_NAME)) {
                                String lastName = (personNameDT.getLastNm()==null)?"No Last":personNameDT.getLastNm();
                                String firstName = (personNameDT.getFirstNm()==null)?"No First":personNameDT.getFirstNm();
                                String personName =lastName + ", " + firstName;
                                cTContactSumyDT.setContactName(personName);
                                break;
                            }
                        }
                    }
                } //contact Entity not null
                //Other Infected Person is seldom present 
                Long otherEntityUid = cTContactSumyDT.getThirdPartyEntityUid();
                if (otherEntityUid != null) {
                    Collection<PersonName> ctOtherNameColl = new ArrayList<>();
                    var lst3 = personNameRepository.findByParentUid(otherEntityUid);
                    if(lst3.isPresent()) {
                        ctOtherNameColl = lst.get();
                    }

                    if(ctOtherNameColl != null && ctOtherNameColl.size() > 0) {
                        Iterator<PersonName> otherIter = ctOtherNameColl.iterator();
                        while(otherIter.hasNext()){
                            PersonNameDto personNameDT = new PersonNameDto(otherIter.next());
                            if (personNameDT.getNmUseCd().equalsIgnoreCase(NEDSSConstant.LEGAL_NAME)) {
                                String lastName = (personNameDT.getLastNm()==null)?"No Last":personNameDT.getLastNm();
                                String firstName = (personNameDT.getFirstNm()==null)?"No First":personNameDT.getFirstNm();
                                String personName =lastName + ", " + firstName;
                                cTContactSumyDT.setOtherInfectedPatientName(personName);
                                break;
                            }
                        }
                    }
                } //other entity
                //Setting the disposition to the source patient's disposition for the section 'patient named by contacts'
                cTContactSumyDT.setDispositionCd(cTContactSumyDT.getSourceDispositionCd());

            } //has next
        }
        catch (Exception ex) {
            throw new DataProcessingException(ex.toString());
        }
        return returnCTNameByPatientSummDTColl;
    }





}
