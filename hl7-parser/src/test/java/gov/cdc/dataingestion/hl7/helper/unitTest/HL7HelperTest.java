package gov.cdc.dataingestion.hl7.helper.unitTest;

import com.google.gson.Gson;
import gov.cdc.dataingestion.hl7.helper.HL7Helper;
import gov.cdc.dataingestion.hl7.helper.integration.exception.DiHL7Exception;
import gov.cdc.dataingestion.hl7.helper.model.hl7.messageDataType.Ce;
import gov.cdc.dataingestion.hl7.helper.model.hl7.messageType.OruR1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;

import static gov.cdc.dataingestion.hl7.helper.unitTest.Hl7TestData.*;

class HL7HelperTest {
    private HL7Helper target;
    private String data = "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5\r"
            + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
            + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
            + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
            + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
            + "OBX|1|ST|||Test Value";

    private String validData = "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5\r"
            + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
            + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
            + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
            + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
            + "OBX|1|ST|||Test Value";

    private String invalidData = "TEST TEST";
    @BeforeEach
    public void setUp() {
        target = new HL7Helper();
    }



    @Test
    void hl7StringValidatorTest_ReturnValidMessage() throws DiHL7Exception {
        var result = target.hl7StringValidator(data);
        Assertions.assertEquals(validData, result);
    }

    @Test
    void hl7StringParser_ReturnValidMessage() throws  DiHL7Exception {
        var result = target.hl7StringParser(testMessageForXmlIssue);
        Gson gson = new Gson();
        String json = gson.toJson(result);
        Assertions.assertEquals("R01", result.getEventTrigger());
    }

    @Test
    void hl7StringParser_ReturnValidFromRhapsodyMessage() throws  DiHL7Exception {
        var result = target.hl7StringParser(messageByRhapsody);
        Assertions.assertEquals("R01", result.getEventTrigger());
    }


    @Test
    void hl7StringConvert231To251_ReturnValidMessage() throws  DiHL7Exception {
        var result = target.convert231To251(testMessage);

        Assertions.assertEquals("R01", result.getEventTrigger());
    }

    @Test
    void hl7v231StringParser_ReturnException()  {

        var exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.convert231To251(messageByRhapsody);
        });

        Assertions.assertNotNull(exception);
    }

    @Test
    void hl7StringParserWith231_ReturnValidMessage() throws  DiHL7Exception {
        var result = target.hl7StringParser(testMessage);

        Assertions.assertEquals("R01", result.getEventTrigger());
    }

    @Test
    void hl7StringParserWith251_ReturnValidMessage_RandomV1() throws  DiHL7Exception {
        var result = target.hl7StringParser(randomGenerated251WithDataInAllField);
        Assertions.assertEquals("R01", result.getEventTrigger());
    }

    @Test
    void hl7StringParserWith251_ReturnValidMessage_RandomV2() throws  DiHL7Exception {
        var result = target.hl7StringParser(randomGenerated251WithDataInAllFieldV2);
        var oru = (OruR1) result.getParsedMessage();
        Assertions.assertEquals("R01", result.getEventTrigger());

        //region Observation Request
        var observationRequest = oru.getPatientResult().get(0).getOrderObservation().get(0).getObservationRequest();
        var observationRequestCnn = observationRequest.getPrincipalResultInterpreter().getName();
        Assertions.assertEquals("20230615",observationRequestCnn.getIdNumber());
        Assertions.assertNull(observationRequestCnn.getFamilyName());
        Assertions.assertNull(observationRequestCnn.getGivenName());
        Assertions.assertNull(observationRequestCnn.getSecondAndFurtherGivenNameOrInitial());
        Assertions.assertNull(observationRequestCnn.getSuffix());
        Assertions.assertNull(observationRequestCnn.getPrefix());
        Assertions.assertNull(observationRequestCnn.getDegree());
        Assertions.assertNull(observationRequestCnn.getSourceTable());
        Assertions.assertNull(observationRequestCnn.getAssignAuthorityNamespaceId());
        Assertions.assertNull(observationRequestCnn.getAssignAuthorityUniversalId());
        Assertions.assertNull(observationRequestCnn.getAssignAuthorityUniversalIdType());
        Assertions.assertEquals("1",observationRequest.getSetIdObr());
        Assertions.assertEquals("20230615",observationRequest.getPriorityObr());
        Assertions.assertEquals("20230615",observationRequest.getSpecimenActionCode());
        Assertions.assertEquals("20230615",observationRequest.getRelevantClinicalInformation());
        Assertions.assertEquals("20230615",observationRequest.getPlacerField1());
        Assertions.assertEquals("20230615",observationRequest.getPlacerField2());
        Assertions.assertEquals("20230615",observationRequest.getFillerField1());
        Assertions.assertEquals("20230615",observationRequest.getFillerField2());
        Assertions.assertEquals("20230615",observationRequest.getDiagnosticServSectId());
        Assertions.assertEquals("20230615",observationRequest.getResultStatus());
        Assertions.assertEquals("20230615",observationRequest.getTransportationMode());
        Assertions.assertEquals("20230615",observationRequest.getTransportArranged());
        Assertions.assertEquals("20230615",observationRequest.getEscortRequired());
        Assertions.assertEquals("20230615",observationRequest.getResultHandling());
        //endregion

        //region finance
        var finance = oru.getPatientResult().get(0).getOrderObservation().get(0).getFinancialTransaction().get(0);
        var financeCne = finance.getNdcCode();
        var financeCp = finance.getTransactionAmountExt();
        var financeCx = finance.getPaymentReferenceId();
        var financeCwe = finance.getMedicallyNecessaryDuplicateProcedureReason();
        var financePl = finance.getAssignedPatientLocation();
        var financeXcn = finance.getPerformedByCode().get(0);
        var financeEi = finance.getFillerOrderNumber();
        Assertions.assertEquals("20230615",financeEi.getEntityIdentifier());
        Assertions.assertNull(financeEi.getNameSpaceId());
        Assertions.assertNull(financeEi.getUniversalId());
        Assertions.assertNull(financeEi.getUniversalIdType());
        Assertions.assertEquals("20230615",financeXcn.getIdNumber());
        Assertions.assertNull(financeXcn.getGivenName());
        Assertions.assertNull(financeXcn.getSecondAndFurtherGivenNameOrInitial());
        Assertions.assertNull(financeXcn.getSuffix());
        Assertions.assertNull(financeXcn.getPrefix());
        Assertions.assertNull(financeXcn.getDegree());
        Assertions.assertNull(financeXcn.getSourceTable());
        Assertions.assertNull(financeXcn.getNameTypeCode());
        Assertions.assertNull(financeXcn.getIdentifierCheckDigit());
        Assertions.assertNull(financeXcn.getCheckDigitScheme());
        Assertions.assertNull(financeXcn.getIdentifierTypeCode());
        Assertions.assertNull(financeXcn.getNameRepresentationCode());
        Assertions.assertNull(financeXcn.getNameAssemblyOrder());
        Assertions.assertNull(financeXcn.getProfessionalSuffix());
        Assertions.assertEquals("20230615",financePl.getPointOfCare());
        Assertions.assertNull(financePl.getRoom());
        Assertions.assertNull(financePl.getBed());
        Assertions.assertNull(financePl.getPersonLocationType());
        Assertions.assertNull(financePl.getBuilding());
        Assertions.assertNull(financePl.getFloor());
        Assertions.assertNull(financePl.getLocationDescription());
        Assertions.assertEquals("20230615",financeCwe.getIdentifier());
        Assertions.assertNull(financeCwe.getText());
        Assertions.assertNull(financeCwe.getNameOfAlterCodeSystem());
        Assertions.assertNull(financeCwe.getNameOfCodingSystem());
        Assertions.assertNull(financeCwe.getAlternateIdentifier());
        Assertions.assertNull(financeCwe.getAlternateText());
        Assertions.assertNull(financeCwe.getCodeSystemVerId());
        Assertions.assertNull(financeCwe.getAlterCodeSystemVerId());
        Assertions.assertNull(financeCwe.getOriginalText());
        Assertions.assertEquals("20230615",financeCx.getIdNumber());
        Assertions.assertNull(financeCx.getCheckDigit());
        Assertions.assertNull(financeCx.getCheckDigitScheme());
        Assertions.assertNull(financeCx.getIdentifierTypeCode());
        Assertions.assertNull(financeCx.getEffectiveDate());
        Assertions.assertNull(financeCx.getExpirationDate());
        Assertions.assertNull(financeCp.getPriceType());
        Assertions.assertNull(financeCp.getFromValue());
        Assertions.assertNull(financeCp.getToValue());
        Assertions.assertNull(financeCp.getRangeType());
        Assertions.assertEquals("20230615",financeCne.getIdentifier());
        Assertions.assertNull(financeCne.getText());
        Assertions.assertNull(financeCne.getNameOfCodingSystem());
        Assertions.assertNull(financeCne.getAlternateIdentifier());
        Assertions.assertNull(financeCne.getAlternateText());
        Assertions.assertNull(financeCne.getNameOfAlternateCodingSystem());
        Assertions.assertNull(financeCne.getCodingSystemVersionId());
        Assertions.assertNull(financeCne.getAlternateCodingSystemVersionId());
        Assertions.assertNull(financeCne.getOriginalText());
        Assertions.assertEquals("1",finance.getSetIdFT1());
        Assertions.assertEquals("20230615",finance.getTransactionId());
        Assertions.assertEquals("20230615",finance.getTransactionBatchId());
        Assertions.assertEquals("20230615",finance.getTransactionType());
        Assertions.assertEquals("20230615",finance.getTransactionDescription());
        Assertions.assertEquals("20230615",finance.getTransactionDescriptionAlter());
        Assertions.assertEquals("20230615",finance.getTransactionQuantity());
        Assertions.assertEquals("20230615",finance.getFeeSchedule());
        Assertions.assertEquals("20230615",finance.getPatientType());
        //endregion

        //region Time Qty
        var timeQty = oru.getPatientResult().get(0).getOrderObservation().get(0).getTimingQty().get(0).getTimeQuantity();
        var timeQtyRelation = oru.getPatientResult().get(0).getOrderObservation().get(0).getTimingQty().get(0).getTimeQuantityRelationship();
        Assertions.assertEquals("1",timeQty.getSetIdTq1());
        Assertions.assertEquals("20230615",timeQty.getConditionText());
        Assertions.assertEquals("20230615",timeQty.getTextInstruction());
        Assertions.assertEquals("20230615",timeQty.getConjunction());
        Assertions.assertEquals("20230615",timeQty.getTotalOccurrences());
        Assertions.assertEquals("1",timeQtyRelation.get(0).getSetIdTq2());
        Assertions.assertEquals("20230615",timeQtyRelation.get(0).getSequenceResultFlag());
        Assertions.assertEquals("20230615",timeQtyRelation.get(0).getSequenceConditionCode());
        Assertions.assertEquals("20230615",timeQtyRelation.get(0).getCyclicGroupMaximumNumberOfRepeats());
        Assertions.assertEquals("20230615",timeQtyRelation.get(0).getSpecialServiceRequestRelationship());
        //endregion

        //region Observation Result
        var observationResult = oru.getPatientResult().get(0).getOrderObservation().get(0).getObservation().get(0).getObservationResult();
        Assertions.assertEquals("1",observationResult.getSetIdObx());
        Assertions.assertEquals("ST",observationResult.getValueType());
        Assertions.assertEquals("20230615",observationResult.getObservationSubId());
        Assertions.assertEquals("20230615",observationResult.getReferencesRange());
        Assertions.assertEquals("21",observationResult.getProbability());
        Assertions.assertEquals("20230615",observationResult.getObservationResultStatus());
        Assertions.assertEquals("20230615",observationResult.getUserDefinedAccessChecks());
        Assertions.assertEquals("Varies[20230615]",observationResult.getReservedForHarmonizationWithV261());
        Assertions.assertEquals("Varies[20230615]",observationResult.getReservedForHarmonizationWithV262());
        Assertions.assertEquals("Varies[20230615]",observationResult.getReservedForHarmonizationWithV263());
        //endregion

        //region patient visit
        var patientVisit = oru.getPatientResult().get(0).getPatient().getVisit().getPatientVisit();
        Assertions.assertEquals("20230615",patientVisit.getSetIdPv1());
        Assertions.assertEquals("20230615",patientVisit.getPatientClass());
        Assertions.assertEquals("20230615",patientVisit.getAdmissionType());
        Assertions.assertEquals("20230615",patientVisit.getHospitalService());
        Assertions.assertEquals("20230615",patientVisit.getPreadmitTestIndicator());
        Assertions.assertEquals("20230615",patientVisit.getReAdmissionIndicator());
        Assertions.assertEquals("20230615",patientVisit.getAdmitSource());
        Assertions.assertEquals("20230615",patientVisit.getVipStatus());
        Assertions.assertEquals("20230615",patientVisit.getPatientType());
        Assertions.assertEquals("20230615",patientVisit.getInterestCode());
        Assertions.assertEquals("20230615",patientVisit.getTransferToBadDebtCode());
        Assertions.assertEquals("20230615",patientVisit.getTransferToBadDebtDate());
        Assertions.assertEquals("20230615",patientVisit.getBadDebtAgencyCode());
        Assertions.assertEquals("20230615",patientVisit.getBadDebtTransferAmount());
        Assertions.assertEquals("20230615",patientVisit.getBadDebtRecoveryAmount());
        Assertions.assertEquals("20230615",patientVisit.getDeleteAccountDate());
        Assertions.assertEquals("20230615",patientVisit.getDischargeDisposition());
        Assertions.assertEquals("20230615",patientVisit.getServicingFacility());
        Assertions.assertEquals("20230615",patientVisit.getBedStatus());
        Assertions.assertEquals("20230615",patientVisit.getAccountStatus());
        Assertions.assertEquals("20230615",patientVisit.getCurrentPatientBalance());
        Assertions.assertEquals("20230615",patientVisit.getTotalCharge());
        Assertions.assertEquals("20230615",patientVisit.getTotalAdjustment());
        Assertions.assertEquals("20230615",patientVisit.getTotalPayment());
        Assertions.assertEquals("20230615",patientVisit.getVisitIndicator());
        //endregion

        //region patient identification
        var patientIdentify = oru.getPatientResult().get(0).getPatient().getPatientIdentification();
        Assertions.assertNotNull(patientIdentify.getPatientId());
        Assertions.assertEquals("20230615", patientIdentify.getAdministrativeSex());
        Assertions.assertEquals("20230615", patientIdentify.getCountyCode());
        Assertions.assertEquals("20230615", patientIdentify.getBirthPlace());
        Assertions.assertEquals("20230615", patientIdentify.getMultipleBirthIndicator());
        Assertions.assertEquals("20230615", patientIdentify.getBirthOrder());
        Assertions.assertEquals("20230615", patientIdentify.getPatientDeathIndicator());
        Assertions.assertEquals("20230615", patientIdentify.getIdentityUnknownIndicator());
        Assertions.assertEquals("20230615", patientIdentify.getStrain());
        //endregion

        //region patient additional demo
        var patientAdditionalDemo = oru.getPatientResult().get(0).getPatient().getPatientAdditionalDemographic();
        Assertions.assertEquals("20230615",patientAdditionalDemo.getLivingDependency().get(0));
        Assertions.assertEquals("20230615",patientAdditionalDemo.getLivingArrangement());
        Assertions.assertNotNull(patientAdditionalDemo.getPatientPrimaryFacility().get(0));
        Assertions.assertEquals("20230615",patientAdditionalDemo.getStudentIndicator());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getHandiCap());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getLivingWillCode());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getOrganDonorCode());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getSeparateBill());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getProtectionIndicator());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getProtectionIndicatorEffectiveDate());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getImmunizationRegistryStatus());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getImmunizationRegistryStatusEffectiveDate());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getPublicityCodeEffectiveDate());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getMilitaryBranch());
        Assertions.assertEquals("20230615",patientAdditionalDemo.getMilitaryRank());
        Assertions.assertNull(patientAdditionalDemo.getMilitaryStatus());
        //endregion

        //region NEXT OF KIN
        var nextKin = oru.getPatientResult().get(0).getPatient().getNextOfKin().get(0);
        Assertions.assertEquals("1", nextKin.getSetIdNK1());
        var familyName = nextKin.getNkName().get(0).getFamilyName();
        Assertions.assertEquals("TESTNOK114B",familyName.getSurname());
        Assertions.assertNull(familyName.getOwnSurnamePrefix());
        Assertions.assertNull(familyName.getOwnSurname());
        Assertions.assertNull(familyName.getSurnameFromPartner());
        Assertions.assertNull(familyName.getSurnamePrefixFromPartner());
        Assertions.assertEquals("FIRSTNOK1", nextKin.getNkName().get(0).getGivenName());
        Assertions.assertEquals("X", nextKin.getNkName().get(0).getSecondAndFurtherGivenNameOrInitial());
        Assertions.assertEquals("JR", nextKin.getNkName().get(0).getSuffix());
        Assertions.assertEquals("DR", nextKin.getNkName().get(0).getPrefix());
        Assertions.assertEquals("MD", nextKin.getNkName().get(0).getDegree());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameTypeCode());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameRepresentationCode());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameContext().getIdentifier());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameContext().getText());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameContext().getNameOfCodingSystem());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameContext().getAlternateIdentifier());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameContext().getAlternateText());
        Assertions.assertNull( nextKin.getNkName().get(0).getNameContext().getNameOfAlternateCodingSystem());
        Assertions.assertNull( nextKin.getNkName().get(0).getProfessionalSuffix());
        Assertions.assertEquals("20230615", nextKin.getStartDate());
        Assertions.assertEquals("20230615", nextKin.getEndDate());
        Assertions.assertEquals("20230615", nextKin.getNextOfKinAssociatedPartiesJobTitle());
        Assertions.assertEquals("20230615", nextKin.getAdministrativeSex());
        Assertions.assertEquals("20230615", nextKin.getLivingArrangement());
        Assertions.assertEquals("20230615", nextKin.getProtectionIndicator());
        Assertions.assertEquals("20230615", nextKin.getStudentIndicator());
        Assertions.assertEquals("20230615", nextKin.getJobStatus());
        Assertions.assertEquals("20230615", nextKin.getHandicap());
        Assertions.assertEquals("20230615", nextKin.getContactPersonSocialSecurityNumber());
        Assertions.assertEquals("20230615", nextKin.getNextOfKinBirthPlace());
        Assertions.assertEquals("20230615", nextKin.getVipIndicator());
        //endregion

        Assertions.assertEquals("20230615",oru.getContinuationPointer().getContinuationPointer());
        Assertions.assertEquals("20230615",oru.getContinuationPointer().getContinuationStyle());

        //region HEADER
        Assertions.assertEquals("|",oru.getMessageHeader().getFieldSeparator());
        Assertions.assertEquals("^~\\&",oru.getMessageHeader().getEncodingCharacters());
        Assertions.assertEquals("20230615",oru.getMessageHeader().getSecurity());
        Assertions.assertEquals("123456789",oru.getMessageHeader().getMessageControlId());

        Assertions.assertEquals("20230615",oru.getMessageHeader().getSequenceNumber());
        Assertions.assertEquals("20230615",oru.getMessageHeader().getContinuationPointer());
        Assertions.assertEquals("20230615",oru.getMessageHeader().getAcceptAckType());
        Assertions.assertEquals("20230615",oru.getMessageHeader().getApplicationAckType());
        Assertions.assertEquals("20230615",oru.getMessageHeader().getCountryCode());
        Assertions.assertEquals("20230615",oru.getMessageHeader().getAlternateCharacterSetHandlingScheme());
        //endregion

        Assertions.assertEquals("20230615",oru.getSoftwareSegment().get(0).getSoftwareCertifiedVersionOrReleaseNumber());
        Assertions.assertEquals("20230615",oru.getSoftwareSegment().get(0).getSoftwareProductName());
        Assertions.assertEquals("20230615",oru.getSoftwareSegment().get(0).getSoftwareBinaryId());
        Assertions.assertEquals("20230615",oru.getSoftwareSegment().get(0).getSoftwareProductInformation());

        Assertions.assertEquals("1",oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).getSetIdNte());
        Assertions.assertEquals("20230615",oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).getSourceOfComment());


        // Code coverage for setter
        oru.getContinuationPointer().setContinuationPointer("AA");
        oru.getContinuationPointer().setContinuationStyle("AA");
        Assertions.assertEquals("AA",oru.getContinuationPointer().getContinuationPointer());
        Assertions.assertEquals("AA",oru.getContinuationPointer().getContinuationStyle());

        oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).setSetIdNte("3");
        oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).setSourceOfComment("AA");
        oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).setComment(new ArrayList<>());
        oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).setCommentType(new Ce());
        Assertions.assertEquals("3",oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).getSetIdNte());
        Assertions.assertEquals("AA",oru.getPatientResult().get(0).getPatient().getNoteAndComment().get(0).getSourceOfComment());


    }

    @Test
    void hl7StringParserWith231_ReturnValidMessage_RandomV1() throws  DiHL7Exception {
        var result = target.hl7StringParser(randomGenerated231WithDataInAllFieldV1);
        Gson gson = new Gson();
        var test = gson.toJson(result);
        Assertions.assertEquals("R01", result.getEventTrigger());
    }

    @ParameterizedTest
    @CsvSource({
            "'test with string carrier\\r', 'test with string carrier\r'",
            "'test with string new line\\n', 'test with string new line\r'",
            "'test with new line\r', 'test with new line\r'",
            "'test with carrier and new line\r\r', 'test with carrier and new line\r'",
            "'test\r', 'test\r'"
    })
    void hl7MessageStringValidationAllScenario(String msg, String expectedMsg) throws DiHL7Exception {
        var result = target.hl7StringValidator(msg);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedMsg, result);
    }



    @Test
    void hl7Validation_TestValidMessage() throws DiHL7Exception {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|ST|||Test Value";
        var result = target.hl7Validation(oruR1MessageSmall);
        Assertions.assertEquals(oruR1MessageSmall, result);
    }

    @Test
    void hl7Validation_Invalid_MissingMSH() {
       String oruR1MessageSmall =
               "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                + "OBX|1|ST|||Test Value";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message Determine encoding for message. The following is the first 50 chars of the message for reference, although this may not be where the issue is: PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_JumbleMSH() {
        String oruR1MessageSmall =
                         "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|ST|||Test Value";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message Determine encoding for message. The following is the first 50 chars of the message for reference, although this may not be where the issue is: PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_ExceedingRequiredLength() {
        String oruR1MessageSmall =
                "MSH|^~\\&|MedSeries|CAISI_1-2|PLS|3910|200903230934||ADT^A31^ADT_A05|75535037-1237815294895|P^T|2.5.1\r\r"
          				+ "EVN|0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789|200903230934\r\r"
          				+ "PID|1||29^^CAISI_1-2^PI~\"\"||Test300^Leticia^^^^^L||19770202|M||||||||||||||||||||||";

        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.validation.ValidationException: Validation failed: Primitive value '0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789' requires to be shorter than 200 characters at EVN-1(0)";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_OBX_ExceededLength() {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|EXCEEDED_LENGTH|||Test Value";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message 'EXCEEDED_LENGTH' in record 1 is invalid for version 2.5.1 at OBX-2(0)";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_DateTime() {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||2009AAA05011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|ST|||Test Value";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.validation.ValidationException: Validation failed: Primitive value '2009AAA05011106' requires to be empty or a HL7 datetime string at OBR-14(0)";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_Number() {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|ST|||Test Value||||TEST NUMBER";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.validation.ValidationException: Validation failed: Primitive value 'TEST NUMBER' requires to be empty or a number with optional decimal digits at OBX-9(0)";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_BogusSegment() {
        String oruR1MessageSmall =
                "MSH|^~\\&|LABADT|MCM|LAAAB|ELAB-3|20230630122000||ORU^R01|CNTRL-3456|P|2.5.1\r" +
                        "OBR|1|86427531000^LAB|17747^LAB|008342^CBC (INCLUDEES DIFF/PLT)^L|||202306301221|||L|||^456^123^A||1234567890^M10||202306301228|||F\r" +
                        "PV1||O|O/R|||01^DOCTOR\r" +
                        "PID|||1234^5^M11||EVERYMANN^ADAM^A||19610615|X|||222 W MAIN ST^^BOSTON^MA^111^USA||(617)555-1212|(617)555-1212||S||123456789|876-54-3210\r" +
                        "ORC|RE|86427531000^LAB|17747^LAB||Final||202306301220||||674-111^^^ACME HOSPITAL^L\r" +
                        "AAA|RE|86427531000^LAB|17747^LAB||Final||202306301220||||674-111^^^ACME HOSPITAL^L\r" +
                        "OBX|1|NM|008342^WBC^L||11|X10^3/uL^Universal|||123||||||202306301228|||^^^456^123";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message Found unknown segment: AAA at AAA";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_BadSegment() {
        String oruR1MessageSmall =
                "MSH|^~\\&|LABADT|MCM|LAAAB|ELAB-3|20230630122000||ORU^R01|CNTRL-3456|P|2.5.1\r" +
                        "OBR|1|86427531000^LAB|17747^LAB|008342^CBC (INCLUDEES DIFF/PLT)^L|||202306301221|||L|||^456^123^A||1234567890^M10||202306301228|||F\r" +
                        "PV1||O|O/R|||01^DOCTOR\r" +

                        "PID|||1234^5^M11||EVERYMANN^ADAM^A||19610615|X|||222 W MAIN ST^^BOSTON^MA^111^USA||(617)555-1212|(617)555-1212||S||123456789|876-54-3210\r" +
                        "ORC|RE|86427531000^LAB|17747^LAB||Final||202306301220||||674-111^^^ACME HOSPITAL^L\r" +
                        "OBX|1|NM|008342^WBC^L||11|X10^3/uL^Universal|||123||||||202306301228|||^^^456^123";


        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.HL7Exception: Patient Group is Empty";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_NoPatientName() {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO|RET^RETICULOCYTE COUNT^HL79901 literal|||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|ST|||Test Value";
        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.HL7Exception: Error Occurred at PID-5";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_NoOrderObservationGroup() {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST";
        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.HL7Exception: Order Observation is Empty";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_Invalid_NoObrIdentifierCode() {
        String oruR1MessageSmall =
                "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01|20169838-v25|T|2.5.1\r"
                        + "PID|||7005728^^^TML^MR||TEST^RACHEL^DIAMOND||19310313|F|||200 ANYWHERE ST^^TORONTO^ON^M6G 2T9||(416)888-8888||||||1014071185^KR\r"
                        + "PV1|1||OLIS||||OLIST^BLAKE^DONALD^THOR^^^^^921379^^^^OLIST\r"
                        + "ORC|RE||T09-100442-RET-0^^OLIS_Site_ID^ISO|||||||||OLIST^BLAKE^DONALD^THOR^^^^L^921379\r"
                        + "OBR|0||T09-100442-RET-0^^OLIS_Site_ID^ISO||||200905011106|||||||200905011106||OLIST^BLAKE^DONALD^THOR^^^^L^921379||7870279|7870279|T09-100442|MOHLTC|200905011130||B7|F||1^^^200905011106^^R\r"
                        + "OBX|1|ST|||Test Value";
        Exception exception = Assertions.assertThrows(DiHL7Exception.class, () -> {
            target.hl7Validation(oruR1MessageSmall);
        });

        String expectedMessage = "Invalid Message ca.uhn.hl7v2.HL7Exception: Error Occurred at OBR-4";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void hl7Validation_FHSMessage() throws DiHL7Exception {
        String message =
                "FHS|^~\\&|ELR|NIST|NIST|NIST|20150708||FHSeg|Generated by ELR Route|FHSAppGen20150708|Application Generate\r" +
                "BHS|^~\\&|NIST|NIST|NIST|NIST|20150708||BHSeg|Application Generated|BHSAppGen20150708|P\r" +
                "MSH|^~\\&|NIST^TBD^ISO|NIST^TBD^ISO|NIST^TBD^ISO|NIST^TBD^ISO|20120821140551-0500000||ORU^R01|NIST-ELR-001.03|T|2.3.1|||NE|NE\r" +
                "PID|1||18789547^^^NIST MPI&TBD&ISO^MR^Shady Tree&TBD&ISO~131131313^^^SSN&TBD&ISO^SS^SSA&TBD&ISO||Sanchez^Abel^G.^Jr^^^L~DUC^Luis^Munoz^Jr^^^B|Sanchez^Rose^G.^Sr^Dr^^M|19800320|M||W|6 Maple Drive^Apt 1^Philadelphia^PA^19136^USA^H^^42101~131 Broad Street^Apt 31^Philadelphia^PA^19136^USA^C^^42101||^PRN^CP^^1^215^5551214^4^call before 8PM~^NET^Internet^sangel@yahoo.com^^^^^home|^WPN^PH^^1^215^5551214^4^call before 8PM||||||||H||||||||N\r" +
                "ORC|RE|3456723^NIST_Placer _App^TBD^ISO|system generated|system generated^NIST_Sending_App^TBD^ISO||||||||131131131^Remala^Swathi^T^JR^DR^^^NPI&TBD&ISO^L^^^NPI^NPI_Facility&TBD&ISO||^WPN^PH^^1^215^5556600^13^Hospital Line~^BPN^BP^^1^215^5553531^131^Beeper number|||||||Shady Tree Free Clinic^L^^^^NIST sending app&TBD&ISO^XX|900 Emerson Road^Building 1^Philadelphia^PA^19137^USA^L^^42101|^WPN^PH^^1^215^5556600^13^Call  9AM  to 5PM|1200 Professional Way^Suite 114^Philadelphia^PA^19131^USA^B^^42101\r" +
                "OBR|1|3456723^NIST_Placer _App^TBD^ISO|system generated|24467-3^CD3+CD4+ (T4 helper) cells [#/volume] in Blood^LN^2153^ABSOLUTE CD4+ CELLS^L|||20120801|20120801|5^mL||||Confirmed HIV infection|20120802080000500|258580003&Whole blood sample (specimen)&SNM&BLD&Whole Blood EDTA&L^Potassium/K EDTA^^244001006&Antecubital fossa&SNM&ELBL&Elbow, Left&L^263925004&Venous (qualifier value)&SNM&Ven&Venous&L^82078001&Collection of blood specimen for laboratory (procedure)&SNM&VBD&Venous Blood Draw&L|131131131^Remala^Swathi^T^JR^DR^^^NPI&TBD&ISO^L^^^NPI^NPI_Facility&TBD&ISO|^WPN^PH^^1^215^5556600^13^Hospital Line~^BPN^BP^^1^215^5553531^131^Beeper number|||||20120802160000500|||F||||||042^Human immunodeficiency virus [HIV] disease [42]^TBD^HIV^HIV/Aids^L|131&Varma&Raja&Rami&JR&DR&PHD&&NIST_Sending_App2.16.840.1.113883.3.72.5.21ISO\r" +
                "OBX|1|SN|24467-3^CD3+CD4+ (T4 helper) cells [#/volume] in Blood^LN^2153^ABSOLUTE CD4+ CELLS^L||=^440|{Cells}/uL^cells per microliter^UCUM^cells/mcL^cells per microliter^L|649-1346 cells/mcL|L|||F|||20120801|01D7654321^Philadelphia City Public Health Lab^CLIA||0251^Flow Cytometry (FC)^TBD^FC^Flow Cytometry^L\r" +
                "NTE|1||Performing Lab Address: 7901 State Road, Philadelphia, PA  19137\r" +
                "NTE|2||Age at Specimen Collection(35659-2[LN]): =\\S\\32 Year\r" +
                "BTS|1|Generated by ELR Route\r" +
                "FTS|1|Generated by NBS ELR Route";

        var result = target.hl7StringParser(message);

        Assertions.assertNotNull(result);
    }

    @Test
    void hl7Validation_FHSMessage231MissingDate() throws DiHL7Exception {
        String message =
                "FHS|^~\\&|ELR|Lexington Medical Center|SCDOH|SC|20150325||FHSeg|Generated by ELR Route|FHSAppGen20150325|Application Generate\n" +
                        "BHS|^~\\&|SQ|Lexington Medical Center|SCDOH|SC|20150325||BHSeg|Application Generated|BHSAppGen20150325|P\n" +
                        "MSH|^~\\&|SQ^TBD^ISO|Lexington Medical Center^TBD^CLIA|SCDOH^TBD^ISO|SC^TBD^ISO|||ORU^R01|1|P|2.3.1|||NE|NE\n" +
                        "PID|1||M001141754^^^Lexington Medical Center&TBD&ISO^MR^SQ&TBD&ISO~06489^^^SSN&TBD&ISO^SS^SSA&TBD&ISO||MOUSEHAPPY^MICKEY^K^^^^L~MOUSE^MISTER^E^^^^A||19591220|F||W|123 Main St^^SWANSEA^SC^29160^^H||^PRN^PH^^^803^5555555||ENG^English^TBD|D||||||N\n" +
                        "ORC|RE|48785475-0^Lexington Medical Center^TBD^ISO|H2847920150305|H28479^SQ^TBD^ISO||||||||2834^Blizzard^Dale^^^^^^SQ&TBD&ISO^L^^^DN^SQ&TBD&ISO||^WPN^PH^^^803^5682000|||||||Lexington Medical Center^L^^^^Lexington Medical Center&TBD&ISO^XX|2720 Sunset Blvd^^West Columbia^SC^29169^^B|^WPN^PH^^^803^7912401|LMC Swansea^935 West 2nd St^Swansea^SC^29160^^B\n" +
                        "OBR|1|48785475-0^Lexington Medical Center^TBD^ISO|H2847920150305|634-6^Bacteria identified in Unspecified specimen by Aerobe culture^LN^AERC^AEROBIC CULTURE^L||||||187^NONLABORATORY,PERSONNEL|||SITE IS DORSAL RIGHT FOOT||SKN&SKIN|2834^Blizzard^Dale^^^^^^SQ&TBD&ISO^L^^^DN^SQ&TBD&ISO|^WPN^PH^^^803^5682000|5436||H28479|||||F\n" +
                        "OBX|1|CE|634-6^Bacteria identified in Unspecified specimen by Aerobe culture^LN^AERC^AEROBIC CULTURE^L|1.1|80166006^Streptococcus pyogenes (organism)^SNM^GABS^STREPTOCOCCUS PYOGENES(GROUP A BETA-HEMOLYTIC STREPT)^L|||A|||F||||42D0665325^LMC^CLIA\n" +
                        "NTE|1|L|3+ Streptococcus pyogenes (organism)\n" +
                        "NTE|2||Performing Lab Address: 2720 Sunset Blvd, West Columbia, SC  29169\n" +
                        "BTS|1|Generated by ELR Route\n" +
                        "FTS|1|Generated by NBS ELR Route";

        message = target.hl7StringValidator(message);
        message = target.hl7Validation(message);
        var result = target.convert231To251(message);

        Assertions.assertNotNull(result);
    }

    @Test
    void hl7Validation_FullHL7() throws DiHL7Exception {
        String message =
                "MSH|^~\\&|OneAbbottSol.STAG^2.16.840.1.113883.3.8589.4.2.7.2^ISO|AbbottInformatics^00Z0000002^CLIA|AIMS.INTEGRATION.STG^2.16.840.1.114222.4.3.15.2^ISO|AIMS.PLATFORM^2.16.840.1.114222.4.1.217446^ISO|20210128162413-0500||ORU^R01^ORU_R01|20210128162413.806_P21-0000105078|T|2.5.1|||NE|NE|||||PHLabReport-NoAck^ELR251R1_Rcvr_Prof^2.16.840.1.113883.9.11^ISO\n" +
                        "SFT|Abbott Informatics|PH12.1|STARLIMS PH|Binary ID Unknown\n" +
                        "PID|1||P21-0000105078^^^OneAbbottSol.STAG&2.16.840.1.113883.3.8589.4.2.7.2&ISO^SID^OneAbbottSol.STAG&2.16.840.1.113883.3.8589.4.2.7.2&ISO||Han^Solo^^||19880121190000-0500|F||2054-5^Black or African American^HL70005^^^^Vunknown|11 Norman drive^^Palatine^IL^60067^USA^^^||^^^^^847^2260356|||||||||U^Unknown^HL70189^^^^Vunknown||||||||\n" +
                        "ORC|RE|2gcxDYvIHHLr+e6hO9Lxrg^OneAbbottSol.STAG^2.16.840.1.113883.3.8589.4.2.7.2^ISO|P21-0000105078^OneAbbottSol.STAG^2.16.840.1.113883.3.8589.4.2.7.2^ISO|||||||||^^SA.OverTheCounter|||20210128160603-0500||||||SA.OverTheCounter|11 Norman drive^^Palatine^IL^60067^USA^^^|^^^^^847^2260356|\n" +
                        "OBR|1|2gcxDYvIHHLr+e6hO9Lxrg^OneAbbottSol.STAG^2.16.840.1.113883.3.8589.4.2.7.2^ISO|P21-0000105078^OneAbbottSol.STAG^2.16.840.1.113883.3.8589.4.2.7.2^ISO|95209-3^SARS-CoV+SARS-CoV-2 (COVID-19) Ag [Presence] in Respiratory specimen by Rapid immunoassay^LN^1^SARS Coronavirus 2 Ag^L^Vunknown^Vunknown|||20210128160603-0500|||||||||^^SA.OverTheCounter||||||20210128160621-0500||LAB|F|||||||||||\n" +
                        "OBX|1|SN|30525-0^Age^LN^^^^Vunknown||^33|a^year^UCUM^^^^Vunknown|||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounter^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|2|CWE|95417-2^Whether this is the patient's first test for the condition of interest^LN^^^^2.69||N^No^HL70136^^^^Vunknown||||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounter^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|3|CWE|95418-0^Whether patient is employed in a healthcare setting^LN^^^^2.69||N^No^HL70136^^^^Vunknown||||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounterr^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|4|CWE|95419-8^Whether patient has symptoms related to condition of interest^LN^^^^2.69||Y^Yes^HL70136^^^^Vunknown||||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounter^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|5|CWE|77974-4^Whether patient was hospitalized because of this condition^LN^^^^2.69||N^No^HL70136^^^^Vunknown||||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounterr^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|6|CWE|95421-4^Whether patient resides in a congregate care setting^LN^^^^2.69||Y^Yes^HL70136^^^^Vunknown||||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounter^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|7|CWE|82810-3^Pregnancy status^LN^^^^2.69||60001007^Not pregnant^SCT^^^^Vunknown||||||F|||20210128160603-0500|00Z0000014||||||||SA.OverTheCounter^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||QST\n" +
                        "OBX|8|CWE|95209-3^SARS-CoV+SARS-CoV-2 (COVID-19) Ag [Presence] in Respiratory specimen by Rapid immunoassay^LN^^^^Vunknown^||260373001^Detected^SCT^SARS Coronavirus 2 A^LA6576-8^L^Vunknown^Vunknown^LA6576-8||||||F|||20210128160603-0500|00Z0000014||Ellume COVID-19 Home Test_Ellume Limited_EUA^^99ELR^^^^Vunknown||20210128160621-0500||||SA.OverTheCounter^^^^^CLIA&2.16.840.1.113883.4.7&ISO^XX^^^00Z0000014|11 Fake AtHome Test Street^^Yakutat^AK^99689^^^^02282|||||\n" +
                        "NTE|1|L|Ellume COVID-19 Home Test_Ellume Limited_EUA\n" +
                        "SPM|1|^P21-0000105078&OneAbbottSol.STAG&2.16.840.1.113883.3.8589.4.2.7.2&ISO||445297001^Swab of internal nose^SCT^^^^Vunknown|||||||||||||20210128160603-0500|20210128160603-0500";

        message = target.hl7StringValidator(message);
        message = target.hl7Validation(message);
        var result = target.hl7StringParser(message);

        Assertions.assertNotNull(result);
    }

}
