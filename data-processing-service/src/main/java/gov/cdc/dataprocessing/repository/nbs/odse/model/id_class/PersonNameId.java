package gov.cdc.dataprocessing.repository.nbs.odse.model.id_class;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PersonNameId implements Serializable {
    private Long personUid;

    private Integer personNameSeq;
}
