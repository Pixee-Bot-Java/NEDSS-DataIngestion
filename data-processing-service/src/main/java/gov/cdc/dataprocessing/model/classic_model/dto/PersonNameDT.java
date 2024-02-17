package gov.cdc.dataprocessing.model.classic_model.dto;

import gov.cdc.dataprocessing.model.classic_model.vo.AbstractVO;
import gov.cdc.dataprocessing.repository.nbs.odse.model.PersonName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@Getter
@Setter
public class PersonNameDT
        extends AbstractVO {
    private Long personUid;
    private Integer personNameSeq;
    private String addReasonCd;
    private Timestamp addTime;
    private Long addUserId;
    private Timestamp asOfDate;
    private String defaultNmInd;
    private String durationAmt;
    private String durationUnitCd;
    private String firstNm;
    private String firstNmSndx;
    private Timestamp fromTime;
    private String lastChgReasonCd;
    private Timestamp lastChgTime;
    private Long lastChgUserId;
    private String lastNm;
    private String lastNmSndx;
    private String lastNm2;
    private String lastNm2Sndx;
    private String middleNm;
    private String middleNm2;
    private String nmDegree;
    private String nmPrefix;
    private String nmSuffix;
    private String nmSuffixCd;
    private String nmUseCd;
    private String recordStatusCd;
    private Timestamp recordStatusTime;
    private String statusCd;
    private Timestamp statusTime;
    private Timestamp toTime;
    private String userAffiliationTxt;
    private String progAreaCd = null;
    private String jurisdictionCd = null;
    private Long programJurisdictionOid = null;
    private String sharedInd = null;
    private boolean itDirty = false;
    private boolean itNew = true;
    private boolean itDelete = false;
    private Integer versionCtrlNbr;
    private String localId;

    public PersonNameDT() {

    }
    public PersonNameDT(PersonName personName) {
        this.personUid = personName.getPersonUid();
        this.personNameSeq = personName.getPersonNameSeq();
        this.addReasonCd = personName.getAddReasonCd();
        this.addTime = personName.getAddTime();
        this.addUserId = personName.getAddUserId();
        this.defaultNmInd = personName.getDefaultNmInd();
        this.durationAmt = personName.getDurationAmt();
        this.durationUnitCd = personName.getDurationUnitCd();
        this.firstNm = personName.getFirstNm();
        this.firstNmSndx = personName.getFirstNmSndx();
        this.fromTime = personName.getFromTime();
        this.lastChgReasonCd = personName.getLastChgReasonCd();
        this.lastChgTime = personName.getLastChgTime();
        this.lastChgUserId = personName.getLastChgUserId();
        this.lastNm = personName.getLastNm();
        this.lastNmSndx = personName.getLastNmSndx();
        this.lastNm2 = personName.getLastNm2();
        this.lastNm2Sndx = personName.getLastNm2Sndx();
        this.middleNm = personName.getMiddleNm();
        this.middleNm2 = personName.getMiddleNm2();
        this.nmDegree = personName.getNmDegree();
        this.nmPrefix = personName.getNmPrefix();
        this.nmSuffix = personName.getNmSuffix();
        this.nmUseCd = personName.getNmUseCd();
        this.recordStatusCd = personName.getRecordStatusCd();
        this.recordStatusTime = personName.getRecordStatusTime();
        this.statusCd = personName.getStatusCd();
        this.statusTime = personName.getStatusTime();
        this.toTime = personName.getToTime();
        this.userAffiliationTxt = personName.getUserAffiliationTxt();
        this.asOfDate = personName.getAsOfDate();
    }
}
