package gov.cdc.dataprocessing.model.container;

import gov.cdc.dataprocessing.model.dto.nbs.NbsActEntityDto;
import gov.cdc.dataprocessing.model.dto.nbs.NbsAnswerDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class BasePamContainer {
    private static final long serialVersionUID = 1L;
    private Map<Object, Object> pamAnswerDTMap;
    private Collection<NbsActEntityDto> actEntityDTCollection;
    private Map<Object, NbsAnswerDto> pageRepeatingAnswerDTMap;
    private Map<Object, NbsAnswerDto>  answerDTMap;

}
