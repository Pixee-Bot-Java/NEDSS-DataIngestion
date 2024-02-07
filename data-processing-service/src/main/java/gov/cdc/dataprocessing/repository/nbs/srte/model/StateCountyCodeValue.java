package gov.cdc.dataprocessing.repository.nbs.srte.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "State_county_code_value")
public class StateCountyCodeValue {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "assigning_authority_cd")
    private String assigningAuthorityCd;

    @Column(name = "assigning_authority_desc_txt")
    private String assigningAuthorityDescTxt;

    @Column(name = "code_desc_txt")
    private String codeDescTxt;

    @Column(name = "code_short_desc_txt")
    private String codeShortDescTxt;

    @Column(name = "effective_from_time")
    private Date effectiveFromTime;

    @Column(name = "effective_to_time")
    private Date effectiveToTime;

    @Column(name = "excluded_txt")
    private String excludedTxt;

    @Column(name = "Indent_level_nbr")
    private Short indentLevelNbr;

    @Column(name = "is_modifiable_ind")
    private Character isModifiableInd;

    @Column(name = "parent_is_cd")
    private String parentIsCd;

    @Column(name = "status_cd")
    private String statusCd;

    @Column(name = "status_time")
    private Date statusTime;

    @Column(name = "code_set_nm")
    private String codeSetNm;

    @Column(name = "seq_num")
    private Short seqNum;

    @Column(name = "nbs_uid")
    private Integer nbsUid;

    @Column(name = "source_concept_id")
    private String sourceConceptId;

    @Column(name = "code_system_cd")
    private String codeSystemCd;

    @Column(name = "code_system_desc_txt")
    private String codeSystemDescTxt;

    // Getters and setters
}
