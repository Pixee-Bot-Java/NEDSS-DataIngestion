package gov.cdc.dataingestion.nbs.ecr.model.cases;

import gov.cdc.nedss.phdc.cda.POCDMT000040Observation;
import gov.cdc.nedss.phdc.cda.POCDMT000040StructuredBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CdaCaseFieldSectionCommon {
    POCDMT000040StructuredBody output;
    int c;
    POCDMT000040Observation element;
}
