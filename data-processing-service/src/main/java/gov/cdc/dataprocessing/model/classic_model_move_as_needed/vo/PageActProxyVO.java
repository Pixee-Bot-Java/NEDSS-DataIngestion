package gov.cdc.dataprocessing.model.classic_model_move_as_needed.vo;

import gov.cdc.dataprocessing.model.NbsNoteDto;
import gov.cdc.dataprocessing.model.container.BaseContainer;
import gov.cdc.dataprocessing.model.container.BasePamContainer;
import gov.cdc.dataprocessing.model.container.OrganizationContainer;
import gov.cdc.dataprocessing.model.dto.act.ActRelationshipDto;
import gov.cdc.dataprocessing.model.classic_model_move_as_needed.dto.ExportReceivingFacilityDT;
import gov.cdc.dataprocessing.model.dto.log.MessageLogDto;
import gov.cdc.dataprocessing.model.dto.participation.ParticipationDto;
import gov.cdc.dataprocessing.model.container.PersonContainer;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PageActProxyVO  extends BaseContainer {
    private static final long         serialVersionUID = 1L;

    public String                     pageProxyTypeCd  = "";
    // page business type INV, IXS, etc.
    private PublicHealthCaseVO        publicHealthCaseVO;
    private InterviewVO               interviewVO;
    private NotificationVO            theNotificationVO;
    private InterventionVO			  interventionVO;

    private Long                      patientUid;
    private String                    currentInvestigator;
    private String                    fieldSupervisor;
    private String                    caseSupervisor;
    private boolean                   isSTDProgramArea = false;
    private Collection<PersonContainer> thePersonContainerCollection;

    private BasePamContainer pageVO;
    // contains answer maps

    private Collection<Object>        theVaccinationSummaryVOCollection;
    private Collection<Object>        theNotificationSummaryVOCollection;
    private Collection<Object>        theTreatmentSummaryVOCollection;
    private Collection<Object>        theLabReportSummaryVOCollection;
    private Collection<Object>        theMorbReportSummaryVOCollection;
    protected Collection<ParticipationDto> theParticipationDtoCollection;

    private Collection<ActRelationshipDto> theActRelationshipDtoCollection;
    private Collection<Object>        theInvestigationAuditLogSummaryVOCollection;
    protected Collection<OrganizationContainer> theOrganizationContainerCollection;
    private Collection<Object>        theCTContactSummaryDTCollection;
    private Collection<Object>        theInterviewSummaryDTCollection;
    private Collection<Object>        theNotificationVOCollection;
    private Collection<Object>        theCSSummaryVOCollection;
    private Collection<Object>        nbsAttachmentDTColl;
    private Collection<NbsNoteDto>        nbsNoteDTColl;
    private Collection<Object>        theDocumentSummaryVOCollection;
    private boolean                   isOOSystemInd;
    private boolean                   isOOSystemPendInd;
    private boolean                   associatedNotificationsInd;
    private boolean                   isUnsavedNote;
    private boolean                   isMergeCase;


    private Collection<Object> theEDXDocumentDTCollection;


    private boolean                   isRenterant;
    private boolean					  isConversionHasModified;

    private ExportReceivingFacilityDT exportReceivingFacilityDT;
    private Map<String, MessageLogDto> messageLogDTMap  = new HashMap<String, MessageLogDto>();

    public Object deepCopy() throws CloneNotSupportedException, IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deepCopy = ois.readObject();

        return  deepCopy;
    }
}
