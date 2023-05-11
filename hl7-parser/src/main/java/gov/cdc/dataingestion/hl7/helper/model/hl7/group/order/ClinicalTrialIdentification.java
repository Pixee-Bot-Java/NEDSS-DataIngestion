package gov.cdc.dataingestion.hl7.helper.model.hl7.group.order;
import gov.cdc.dataingestion.hl7.helper.model.hl7.messageDataType.Ce;
import gov.cdc.dataingestion.hl7.helper.model.hl7.messageDataType.Ei;
import lombok.Getter;


@Getter
public class ClinicalTrialIdentification {
    Ei sponsorStudyId;
    Ce studyPhaseIdentifier;
    Ce studyScheduledTimePoint;
    public ClinicalTrialIdentification(ca.uhn.hl7v2.model.v251.segment.CTI cti) {
        this.sponsorStudyId = new Ei(cti.getSponsorStudyID());
        this.studyPhaseIdentifier = new Ce(cti.getStudyPhaseIdentifier());
        this.studyScheduledTimePoint = new Ce(cti.getCti3_StudyScheduledTimePoint());
    }
}