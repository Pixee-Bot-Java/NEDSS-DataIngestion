package gov.cdc.dataprocessing.model.classic_model.dto;

import gov.cdc.dataprocessing.model.classic_model.vo.AbstractVO;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class EntityLocatorParticipationDT extends AbstractVO {

    private Long locatorUid;
    private String addReasonCd;
    private Timestamp addTime;
    private Long addUserId;
    private Timestamp asOfDate;
    private String cd;
    private String cdDescTxt;
    private String classCd;
    private String durationAmt;
    private String durationUnitCd;
    private Timestamp fromTime;
    private String lastChgReasonCd;
    private Timestamp lastChgTime;
    private Long lastChgUserId;
    private String locatorDescTxt;
    private String recordStatusCd;
    private Timestamp recordStatusTime;
    private String statusCd;
    private Timestamp statusTime;
    private Timestamp toTime;
    private String useCd;
    private String userAffiliationTxt;
    private String validTimeTxt;
    private Long entityUid;
    private PostalLocatorDT thePostalLocatorDT;
    private PhysicalLocatorDT thePhysicalLocatorDT;
    private TeleLocatorDT theTeleLocatorDT;
    private Integer versionCtrlNbr;
}
