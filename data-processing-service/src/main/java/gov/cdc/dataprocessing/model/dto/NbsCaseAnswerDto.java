package gov.cdc.dataprocessing.model.dto;

import gov.cdc.dataprocessing.model.dto.nbs.NbsAnswerDto;
import gov.cdc.dataprocessing.repository.nbs.odse.model.NbsCaseAnswer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NbsCaseAnswerDto extends NbsAnswerDto {
    private static final long serialVersionUID = 1L;
    private Long nbsCaseAnswerUid;
    private Long nbsTableMetadataUid;
    private String code;
    private String value;
    private String type;
    private String OtherType;
    private boolean updateNbsQuestionUid;

    public NbsCaseAnswerDto() {
    }


    public NbsCaseAnswerDto(NbsCaseAnswer nbsCaseAnswer) {
        this.actUid = nbsCaseAnswer.getActUid();
        this.addTime = nbsCaseAnswer.getAddTime();
        this.addUserId = nbsCaseAnswer.getAddUserId();
        this.answerTxt = nbsCaseAnswer.getAnswerTxt();
        this.nbsQuestionUid = nbsCaseAnswer.getNbsQuestionUid();
        this.nbsQuestionVersionCtrlNbr = nbsCaseAnswer.getNbsQuestionVersionCtrlNbr();
        this.lastChgTime = nbsCaseAnswer.getLastChgTime();
        this.lastChgUserId = nbsCaseAnswer.getLastChgUserId();
        this.recordStatusCd = nbsCaseAnswer.getRecordStatusCd();
        this.recordStatusTime = nbsCaseAnswer.getRecordStatusTime();
        this.seqNbr = nbsCaseAnswer.getSeqNbr();
        //TODO CLOB
//        this.answerLargeTxt = nbsCaseAnswer.getAnswerLargeTxt();
        this.nbsTableMetadataUid = nbsCaseAnswer.getNbsTableMetadataUid();
        this.nbsQuestionVersionCtrlNbr = nbsCaseAnswer.getNbsUiMetadataVerCtrlNbr();
        this.answerGroupSeqNbr = nbsCaseAnswer.getAnswerGroupSeqNbr();
    }


//    public NbsCaseAnswerDto(NbsAnswerDto answerDT) {
//        super(answerDT);
//        if (answerDT.getNbsAnswerUid() != null)
//            nbsCaseAnswerUid = answerDT.getNbsAnswerUid();
//    }
}
