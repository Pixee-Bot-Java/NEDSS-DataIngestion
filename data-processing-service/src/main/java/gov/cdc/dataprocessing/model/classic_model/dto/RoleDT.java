package gov.cdc.dataprocessing.model.classic_model.dto;

import gov.cdc.dataprocessing.model.classic_model.vo.AbstractVO;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class RoleDT extends AbstractVO {
    private Long roleSeq;
    private String addReasonCd;
    private Timestamp addTime;
    private Long addUserId;
    private String cd;
    private String cdDescTxt;
    //private String classCd;
    private String effectiveDurationAmt;
    private String effectiveDurationUnitCd;
    private Timestamp effectiveFromTime;
    private Timestamp effectiveToTime;
    private String lastChgReasonCd;
    private Timestamp lastChgTime;
    private Long lastChgUserId;
    private String recordStatusCd;
    private Timestamp recordStatusTime;
    private String scopingRoleCd;
    private String statusCd;
    private Timestamp statusTime;
    private String userAffiliationTxt;
    private Long scopingEntityUid;
    private Integer scopingRoleSeq;
    private Long subjectEntityUid;
    private String scopingClassCd;
    private String subjectClassCd;

}
