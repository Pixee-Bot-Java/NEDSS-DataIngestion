package gov.cdc.dataprocessing.utilities.component.patient;

import gov.cdc.dataprocessing.constant.elr.NEDSSConstant;
import gov.cdc.dataprocessing.constant.enums.LocalIdClass;
import gov.cdc.dataprocessing.exception.DataProcessingException;
import gov.cdc.dataprocessing.model.container.PersonContainer;
import gov.cdc.dataprocessing.model.dto.entity.EntityIdDto;
import gov.cdc.dataprocessing.model.dto.entity.EntityLocatorParticipationDto;
import gov.cdc.dataprocessing.model.dto.entity.RoleDto;
import gov.cdc.dataprocessing.model.dto.person.PersonEthnicGroupDto;
import gov.cdc.dataprocessing.model.dto.person.PersonNameDto;
import gov.cdc.dataprocessing.model.dto.person.PersonRaceDto;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.entity.EntityIdRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.entity.EntityLocatorParticipationRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.entity.RoleRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.locator.PhysicalLocatorRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.locator.PostalLocatorRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.locator.TeleLocatorRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.model.entity.EntityId;
import gov.cdc.dataprocessing.repository.nbs.odse.model.entity.EntityLocatorParticipation;
import gov.cdc.dataprocessing.repository.nbs.odse.model.entity.Role;
import gov.cdc.dataprocessing.repository.nbs.odse.model.locator.PhysicalLocator;
import gov.cdc.dataprocessing.repository.nbs.odse.model.locator.PostalLocator;
import gov.cdc.dataprocessing.repository.nbs.odse.model.locator.TeleLocator;
import gov.cdc.dataprocessing.repository.nbs.odse.model.other_move_as_needed.LocalUidGenerator;
import gov.cdc.dataprocessing.repository.nbs.odse.model.person.*;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.person.PersonEthnicRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.person.PersonNameRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.person.PersonRaceRepository;
import gov.cdc.dataprocessing.repository.nbs.odse.repos.person.PersonRepository;
import gov.cdc.dataprocessing.service.interfaces.core.IOdseIdGeneratorService;
import gov.cdc.dataprocessing.utilities.auth.AuthUtil;
import gov.cdc.dataprocessing.utilities.component.entity.EntityRepositoryUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PatientRepositoryUtil {
    private static final Logger logger = LoggerFactory.getLogger(PatientRepositoryUtil.class);
    private final PersonRepository personRepository;
    private final EntityRepositoryUtil entityRepositoryUtil;
    private final PersonNameRepository personNameRepository;
    private final PersonRaceRepository personRaceRepository;
    private final PersonEthnicRepository personEthnicRepository;
    private final EntityIdRepository entityIdRepository;
    private final EntityLocatorParticipationRepository entityLocatorParticipationRepository;
    private final RoleRepository roleRepository;
    private final TeleLocatorRepository teleLocatorRepository;
    private final PostalLocatorRepository postalLocatorRepository;
    private final PhysicalLocatorRepository physicalLocatorRepository;
    private final IOdseIdGeneratorService odseIdGeneratorService;


    public PatientRepositoryUtil(
            PersonRepository personRepository,
            EntityRepositoryUtil entityRepositoryUtil,
            PersonNameRepository personNameRepository,
            PersonRaceRepository personRaceRepository,
            PersonEthnicRepository personEthnicRepository,
            EntityIdRepository entityIdRepository,
            EntityLocatorParticipationRepository entityLocatorParticipationRepository,
            RoleRepository roleRepository,
            TeleLocatorRepository teleLocatorRepository,
            PostalLocatorRepository postalLocatorRepository,
            PhysicalLocatorRepository physicalLocatorRepository,
            IOdseIdGeneratorService odseIdGeneratorService) {
        this.personRepository = personRepository;
        this.entityRepositoryUtil = entityRepositoryUtil;
        this.personNameRepository = personNameRepository;
        this.personRaceRepository = personRaceRepository;
        this.personEthnicRepository = personEthnicRepository;
        this.entityIdRepository = entityIdRepository;
        this.entityLocatorParticipationRepository = entityLocatorParticipationRepository;
        this.roleRepository = roleRepository;
        this.teleLocatorRepository = teleLocatorRepository;
        this.postalLocatorRepository = postalLocatorRepository;
        this.physicalLocatorRepository = physicalLocatorRepository;
        this.odseIdGeneratorService = odseIdGeneratorService;
    }

    @Transactional
    public Long updateExistingPersonEdxIndByUid(Long uid) {
        return (long) personRepository.updateExistingPersonEdxIndByUid(uid);
    }

    @Transactional
    public Person findExistingPersonByUid(Long personUid) {
        var result = personRepository.findById(personUid);
        return result.get();
    }

    @Transactional
    public Person createPerson(PersonContainer personContainer) throws DataProcessingException {
        //TODO: Implement unique id generator here
        Long personUid = 212121L;
        String localUid = "Unique Id here";
        //var localIdModel = localUidGeneratorRepository.findById(PERSON);
        var localIdModel = odseIdGeneratorService.getLocalIdAndUpdateSeed(LocalIdClass.PERSON);
        personUid = localIdModel.getSeedValueNbr();
        localUid = localIdModel.getUidPrefixCd() + personUid + localIdModel.getUidSuffixCd();


        ArrayList<Object>  arrayList = new ArrayList<>();

        if(personContainer.getThePersonDto().getLocalId() == null || personContainer.getThePersonDto().getLocalId().trim().length() == 0) {
            personContainer.getThePersonDto().setLocalId(localUid);
        }

        if(personContainer.getThePersonDto().getPersonParentUid() == null) {
            personContainer.getThePersonDto().setPersonParentUid(personUid);
        }

        // set new person uid in entity table
        personContainer.getThePersonDto().setPersonUid(personUid);

        arrayList.add(personUid);
        arrayList.add(NEDSSConstant.PERSON);

        //NOTE: Create Entitty
        try {
            //NOTE: OK
            entityRepositoryUtil.preparingEntityReposCallForPerson(personContainer.getThePersonDto(), personUid, NEDSSConstant.PERSON, NEDSSConstant.UPDATE);
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }

        //NOTE: Create Person
        Person person = new Person(personContainer.getThePersonDto());
        try {
            personRepository.save(person);
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
        //NOTE: Create Person Name
        if  (personContainer.getThePersonNameDtoCollection() != null && !personContainer.getThePersonNameDtoCollection().isEmpty()) {
            try {
                createPersonName(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create Person Race
        if  (personContainer.getThePersonRaceDtoCollection() != null && !personContainer.getThePersonRaceDtoCollection().isEmpty()) {
            try {
                createPersonRace(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create Person Ethnic
        if  (personContainer.getThePersonEthnicGroupDtoCollection() != null && !personContainer.getThePersonEthnicGroupDtoCollection().isEmpty()) {
            try {
                createPersonEthnic(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create EntityID
        if  (personContainer.getTheEntityIdDtoCollection() != null && !personContainer.getTheEntityIdDtoCollection().isEmpty()) {
            try {
                createEntityId(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create Entity Locator Participation
        if  (personContainer.getTheEntityLocatorParticipationDtoCollection() != null && !personContainer.getTheEntityLocatorParticipationDtoCollection().isEmpty()) {
            try {
                createEntityLocatorParticipation(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create Role
        if  (personContainer.getTheRoleDtoCollection() != null && !personContainer.getTheRoleDtoCollection().isEmpty()) {
            try {
                createRole(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        logger.debug("Person Uid\t" + person.getPersonUid());
        logger.debug("Person Parent Uid\t" + person.getPersonParentUid());

        return person;
    }

    @Transactional
    public void updateExistingPerson(PersonContainer personContainer) throws DataProcessingException {
        //TODO: Implement unique id generator here


        ArrayList<Object>  arrayList = new ArrayList<>();

        arrayList.add(NEDSSConstant.PERSON);

        //NOTE: Update Person
        Person person = new Person(personContainer.getThePersonDto());
        try {
            personRepository.save(person);
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }

        //NOTE: Create Person Name
        if  (personContainer.getThePersonNameDtoCollection() != null && !personContainer.getThePersonNameDtoCollection().isEmpty()) {
            try {
                updatePersonName(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create Person Race
        if  (personContainer.getThePersonRaceDtoCollection() != null && !personContainer.getThePersonRaceDtoCollection().isEmpty()) {
            try {
                createPersonRace(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Create Person Ethnic
        if  (personContainer.getThePersonEthnicGroupDtoCollection() != null && !personContainer.getThePersonEthnicGroupDtoCollection().isEmpty()) {
            try {
                createPersonEthnic(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }


        //NOTE: Upsert EntityID
        if  (personContainer.getTheEntityIdDtoCollection() != null && !personContainer.getTheEntityIdDtoCollection().isEmpty()) {
            try {
                createEntityId(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }


        //NOTE: Create Entity Locator Participation
        if  (personContainer.getTheEntityLocatorParticipationDtoCollection() != null && !personContainer.getTheEntityLocatorParticipationDtoCollection().isEmpty()) {
            try {
                updateEntityLocatorParticipation(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }
        //NOTE: Upsert Role
        if  (personContainer.getTheRoleDtoCollection() != null && !personContainer.getTheRoleDtoCollection().isEmpty()) {
            try {
                createRole(personContainer);
            } catch (Exception e) {
                throw new DataProcessingException(e.getMessage(), e);
            }
        }

    }

    private void updatePersonName(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<PersonNameDto>  personList = (ArrayList<PersonNameDto> ) personContainer.getThePersonNameDtoCollection();
        try {
            var pUid = personContainer.getThePersonDto().getPersonUid();
            List<PersonName> persons = personNameRepository.findBySeqIdByParentUid(pUid);

            Integer seqId = 0;

            StringBuilder sbFromInput = new StringBuilder();
            sbFromInput.append(personContainer.getThePersonDto().getFirstNm());
            sbFromInput.append(personContainer.getThePersonDto().getLastNm());
            sbFromInput.append(personContainer.getThePersonDto().getMiddleNm());
            sbFromInput.append(personContainer.getThePersonDto().getNmPrefix());
            sbFromInput.append(personContainer.getThePersonDto().getNmSuffix());


            List<String> personNameForComparing = new ArrayList<>();
            for(PersonName item : persons) {
                StringBuilder sb = new StringBuilder();
                sb.append(item.getFirstNm());
                sb.append(item.getLastNm());
                sb.append(item.getMiddleNm());
                sb.append(item.getNmPrefix());
                sb.append(item.getNmSuffix());
                if(!personNameForComparing.contains(sb.toString().toUpperCase())) {
                    personNameForComparing.add(sb.toString().toUpperCase());
                }
            }


            //Only save new record if new name is actually new
            if (!personNameForComparing.contains(sbFromInput.toString().toUpperCase())) {
                persons = persons.stream().sorted(Comparator.comparing(PersonName::getPersonNameSeq).reversed()).collect(Collectors.toList());
                if (!persons.isEmpty()) {
                    seqId = persons.get(0).getPersonNameSeq();
                }

                for (PersonNameDto personNameDto : personList) {
                    seqId++;
                    personNameDto.setPersonUid(pUid);
                    if (personNameDto.getStatusCd() == null) {
                        personNameDto.setStatusCd("A");
                    }
                    if (personNameDto.getStatusTime() == null) {
                        personNameDto.setStatusTime(new Timestamp(new Date().getTime()));
                    }
                    personNameDto.setPersonNameSeq(seqId);
                    personNameDto.setRecordStatusCd("ACTIVE");
                    personNameDto.setAddReasonCd("Add");
                    personNameRepository.save(new PersonName(personNameDto));
                }
            }


        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void createPersonName(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<PersonNameDto>  personList = (ArrayList<PersonNameDto> ) personContainer.getThePersonNameDtoCollection();
        try {
            var pUid = personContainer.getThePersonDto().getPersonUid();
            for(int i = 0; i < personList.size(); i++) {
                personList.get(i).setPersonUid(pUid);
                if (personList.get(i).getStatusCd() == null) {
                    personList.get(i).setStatusCd("A");
                }
                if (personList.get(i).getStatusTime() == null) {
                    personList.get(i).setStatusTime(new Timestamp(new Date().getTime()));
                }
                personList.get(i).setRecordStatusCd("ACTIVE");
                personList.get(i).setAddReasonCd("Add");
                personNameRepository.save(new PersonName( personList.get(i)));
            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void createPersonRace(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<PersonRaceDto>  personList = (ArrayList<PersonRaceDto> ) personContainer.getThePersonRaceDtoCollection();
        try {
            for(int i = 0; i < personList.size(); i++) {
                var pUid = personContainer.getThePersonDto().getPersonUid();
                personList.get(i).setPersonUid(pUid);
                personList.get(i).setAddReasonCd("Add");
                personRaceRepository.save(new PersonRace(personList.get(i)));
            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void createPersonEthnic(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<PersonEthnicGroupDto>  personList = (ArrayList<PersonEthnicGroupDto> ) personContainer.getThePersonEthnicGroupDtoCollection();
        try {
            for(int i = 0; i < personList.size(); i++) {
                var pUid = personContainer.getThePersonDto().getPersonUid();
                personList.get(i).setPersonUid(pUid);
                personEthnicRepository.save(new PersonEthnicGroup(personList.get(i)));
            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void createEntityId(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<EntityIdDto>  personList = (ArrayList<EntityIdDto> ) personContainer.getTheEntityIdDtoCollection();
        try {
            for(int i = 0; i < personList.size(); i++) {
                var pUid = personContainer.getThePersonDto().getPersonUid();
                personList.get(i).setEntityUid(pUid);
                personList.get(i).setAddReasonCd("Add");
                if (personList.get(i).getAddUserId() == null) {
                    personList.get(i).setAddUserId(AuthUtil.authUser.getAuthUserUid());
                }
                if (personList.get(i).getLastChgUserId() == null) {
                    personList.get(i).setLastChgUserId(AuthUtil.authUser.getAuthUserUid());
                }
                entityIdRepository.save(new EntityId(personList.get(i)));
            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void updateEntityLocatorParticipation(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<EntityLocatorParticipationDto>  personList = (ArrayList<EntityLocatorParticipationDto> ) personContainer.getTheEntityLocatorParticipationDtoCollection();
        List<EntityLocatorParticipation> entityLocatorParticipations = entityLocatorParticipationRepository.findByParentUid(personContainer.getThePersonDto().getPersonUid()).get();

        if (!entityLocatorParticipations.isEmpty()) {
            List<EntityLocatorParticipation> physicalLocators;
            List<EntityLocatorParticipation> postalLocators;
            List<EntityLocatorParticipation> teleLocators;

            physicalLocators = entityLocatorParticipations.stream().filter(x -> x.getClassCd()
                    .equalsIgnoreCase(NEDSSConstant.PHYSICAL))
                    .sorted(Comparator.comparing(EntityLocatorParticipation::getRecordStatusTime).reversed())
                    .collect(Collectors.toList());
            postalLocators = entityLocatorParticipations.stream().filter(x -> x.getClassCd()
                    .equalsIgnoreCase(NEDSSConstant.POSTAL))
                    .sorted(Comparator.comparing(EntityLocatorParticipation::getRecordStatusTime).reversed())
                    .collect(Collectors.toList());
            teleLocators = entityLocatorParticipations.stream().filter(x -> x.getClassCd()
                    .equalsIgnoreCase(NEDSSConstant.TELE))
                    .sorted(Comparator.comparing(EntityLocatorParticipation::getRecordStatusTime).reversed())
                    .collect(Collectors.toList());


            EntityLocatorParticipation physicalLocator;
            EntityLocatorParticipation postalLocator;
            EntityLocatorParticipation teleLocator;

            StringBuilder comparingString = new StringBuilder();
            for(int i = 0; i < personList.size(); i++) {

                LocalUidGenerator localUid = odseIdGeneratorService.getLocalIdAndUpdateSeed(LocalIdClass.PERSON);
                boolean newLocator = true;
                if (personList.get(i).getClassCd().equals(NEDSSConstant.PHYSICAL) && personList.get(i).getThePhysicalLocatorDto() != null) {
                    newLocator = true;
                    if (!physicalLocators.isEmpty()) {
                        var existingLocator = physicalLocatorRepository.findByPhysicalLocatorUids(
                                physicalLocators.stream()
                                        .map(x -> x.getLocatorUid())
                                        .collect(Collectors.toList()));

                        List<String> compareStringList = new ArrayList<>();

                        if (existingLocator.isPresent()) {
                            for(int j = 0; j < existingLocator.get().size(); j++) {
                                comparingString.setLength(0);
                                comparingString.append(existingLocator.get().get(j).getImageTxt());
                                compareStringList.add(comparingString.toString().toUpperCase());
                            }


                            if (!compareStringList.contains(personList.get(i).getThePhysicalLocatorDto().getImageTxt().toString().toUpperCase())) {
                                personList.get(i).getThePhysicalLocatorDto().setPhysicalLocatorUid(localUid.getSeedValueNbr());
                                physicalLocatorRepository.save(new PhysicalLocator(personList.get(i).getThePhysicalLocatorDto()));
                            }
                            else {
                                newLocator = false;
                            }
                        }
                        else {
                            personList.get(i).getThePhysicalLocatorDto().setPhysicalLocatorUid(localUid.getSeedValueNbr());
                            physicalLocatorRepository.save(new PhysicalLocator(personList.get(i).getThePhysicalLocatorDto()));
                        }

                        comparingString.setLength(0);
                    }
                    else {
                        personList.get(i).getThePhysicalLocatorDto().setPhysicalLocatorUid(localUid.getSeedValueNbr());
                        physicalLocatorRepository.save(new PhysicalLocator(personList.get(i).getThePhysicalLocatorDto()));
                    }
                }
                else if (personList.get(i).getClassCd().equals(NEDSSConstant.POSTAL) && personList.get(i).getThePostalLocatorDto() != null) {
                    newLocator = true;
                    if (!postalLocators.isEmpty()) {
                        var existingLocator = postalLocatorRepository.findByPostalLocatorUids(
                                postalLocators.stream()
                                        .map(x -> x.getLocatorUid())
                                        .collect(Collectors.toList()));

                        List<String> compareStringList = new ArrayList<>();
                        if (existingLocator.isPresent()) {
                            for(int j = 0; j < existingLocator.get().size(); j++) {
                                comparingString.setLength(0);
                                comparingString.append(existingLocator.get().get(j).getCityCd());
                                comparingString.append(existingLocator.get().get(j).getCityDescTxt());
                                comparingString.append(existingLocator.get().get(j).getCntryCd());
                                comparingString.append(existingLocator.get().get(j).getCntryDescTxt());
                                comparingString.append(existingLocator.get().get(j).getCntyCd());
                                comparingString.append(existingLocator.get().get(j).getCntyDescTxt());
                                comparingString.append(existingLocator.get().get(j).getStateCd());
                                comparingString.append(existingLocator.get().get(j).getStreetAddr1());
                                comparingString.append(existingLocator.get().get(j).getStreetAddr2());
                                comparingString.append(existingLocator.get().get(j).getZipCd());

                                compareStringList.add(comparingString.toString().toUpperCase());
                            }


                            StringBuilder existComparingLocator = new StringBuilder();
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getCityCd());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getCityDescTxt());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getCntryCd());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getCntryDescTxt());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getCntyCd());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getCntyDescTxt());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getStateCd());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getStreetAddr1());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getStreetAddr2());
                            existComparingLocator.append(personList.get(i).getThePostalLocatorDto().getZipCd());


                            if (!compareStringList.contains(existComparingLocator.toString().toUpperCase())) {
                                personList.get(i).getThePostalLocatorDto().setPostalLocatorUid(localUid.getSeedValueNbr());
                                postalLocatorRepository.save(new PostalLocator(personList.get(i).getThePostalLocatorDto()));
                            }
                            else {
                                newLocator = false;
                            }
                        }
                        else {
                            personList.get(i).getThePostalLocatorDto().setPostalLocatorUid(localUid.getSeedValueNbr());
                            postalLocatorRepository.save(new PostalLocator(personList.get(i).getThePostalLocatorDto()));
                        }
                        comparingString.setLength(0);
                    }
                    else {
                        personList.get(i).getThePostalLocatorDto().setPostalLocatorUid(localUid.getSeedValueNbr());
                        postalLocatorRepository.save(new PostalLocator(personList.get(i).getThePostalLocatorDto()));
                    }
                }
                else if (personList.get(i).getClassCd().equals(NEDSSConstant.TELE) && personList.get(i).getTheTeleLocatorDto() != null) {
                    newLocator = true;
                    if (!teleLocators.isEmpty()) {
                        var existingLocator = teleLocatorRepository.findByTeleLocatorUids(
                                teleLocators.stream()
                                        .map(x -> x.getLocatorUid())
                                        .collect(Collectors.toList()));
                        List<String> compareStringList = new ArrayList<>();

                        if (existingLocator.isPresent()) {
                            for(int j = 0; j < existingLocator.get().size(); j++) {
                                comparingString.setLength(0);
                                comparingString.append(existingLocator.get().get(j).getCntryCd());
                                comparingString.append(existingLocator.get().get(j).getEmailAddress());
                                comparingString.append(existingLocator.get().get(j).getExtensionTxt());
                                comparingString.append(existingLocator.get().get(j).getPhoneNbrTxt());
                                comparingString.append(existingLocator.get().get(j).getUrlAddress());
                                compareStringList.add(comparingString.toString().toUpperCase());
                            }

                            StringBuilder existComparingLocator = new StringBuilder();
                            existComparingLocator.append(personList.get(i).getTheTeleLocatorDto().getCntryCd());
                            existComparingLocator.append(personList.get(i).getTheTeleLocatorDto().getEmailAddress());
                            existComparingLocator.append(personList.get(i).getTheTeleLocatorDto().getExtensionTxt());
                            existComparingLocator.append(personList.get(i).getTheTeleLocatorDto().getPhoneNbrTxt());
                            existComparingLocator.append(personList.get(i).getTheTeleLocatorDto().getUrlAddress());

                            if (!compareStringList.contains(existComparingLocator.toString().toUpperCase())) {
                                personList.get(i).getTheTeleLocatorDto().setTeleLocatorUid(localUid.getSeedValueNbr());
                                teleLocatorRepository.save(new TeleLocator(personList.get(i).getTheTeleLocatorDto()));
                            }
                            else {
                                newLocator = false;
                            }
                        }
                        else {
                            personList.get(i).getTheTeleLocatorDto().setTeleLocatorUid(localUid.getSeedValueNbr());
                            teleLocatorRepository.save(new TeleLocator(personList.get(i).getTheTeleLocatorDto()));
                        }

                        comparingString.setLength(0);
                    }
                    else {
                        personList.get(i).getTheTeleLocatorDto().setTeleLocatorUid(localUid.getSeedValueNbr());
                        teleLocatorRepository.save(new TeleLocator(personList.get(i).getTheTeleLocatorDto()));
                    }
                }

                // ONLY persist new participation locator if new locator actually exist
                if (newLocator) {
                    personList.get(i).setEntityUid(personContainer.getThePersonDto().getPersonUid());
                    personList.get(i).setLocatorUid(localUid.getSeedValueNbr());

                    if (personList.get(i).getVersionCtrlNbr() == null) {
                        personList.get(i).setVersionCtrlNbr(1);
                    }
                    entityLocatorParticipationRepository.save(new EntityLocatorParticipation(personList.get(i)));
                }

            }
        }
    }

    private void createEntityLocatorParticipation(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<EntityLocatorParticipationDto>  personList = (ArrayList<EntityLocatorParticipationDto> ) personContainer.getTheEntityLocatorParticipationDtoCollection();
        try {
            for(int i = 0; i < personList.size(); i++) {
                boolean inserted = false;
                LocalUidGenerator localUid = odseIdGeneratorService.getLocalIdAndUpdateSeed(LocalIdClass.PERSON);
                if (personList.get(i).getClassCd().equals(NEDSSConstant.PHYSICAL) && personList.get(i).getThePhysicalLocatorDto() != null) {
                    personList.get(i).getThePhysicalLocatorDto().setPhysicalLocatorUid(localUid.getSeedValueNbr());
                    physicalLocatorRepository.save(new PhysicalLocator(personList.get(i).getThePhysicalLocatorDto()));
                    inserted = true;
                }
                else if (personList.get(i).getClassCd().equals(NEDSSConstant.POSTAL) && personList.get(i).getThePostalLocatorDto() != null) {
                    personList.get(i).getThePostalLocatorDto().setPostalLocatorUid(localUid.getSeedValueNbr());
                    postalLocatorRepository.save(new PostalLocator(personList.get(i).getThePostalLocatorDto()));
                    inserted = true;
                }
                else if (personList.get(i).getClassCd().equals(NEDSSConstant.TELE) && personList.get(i).getTheTeleLocatorDto() != null) {
                    personList.get(i).getTheTeleLocatorDto().setTeleLocatorUid(localUid.getSeedValueNbr());
                    teleLocatorRepository.save(new TeleLocator(personList.get(i).getTheTeleLocatorDto()));
                    inserted = true;
                }

                if (inserted) {
                    personList.get(i).setEntityUid(personContainer.getThePersonDto().getPersonUid());
                    personList.get(i).setLocatorUid(localUid.getSeedValueNbr());

                    if (personList.get(i).getVersionCtrlNbr() == null) {
                        personList.get(i).setVersionCtrlNbr(1);
                    }
                    entityLocatorParticipationRepository.save(new EntityLocatorParticipation(personList.get(i)));
                }

            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }

    private void createRole(PersonContainer personContainer) throws DataProcessingException {
        ArrayList<RoleDto>  personList = (ArrayList<RoleDto> ) personContainer.getTheRoleDtoCollection();
        try {
            for(int i = 0; i < personList.size(); i++) {
                RoleDto obj = personList.get(i);
                roleRepository.save(new Role(obj));
            }
        } catch (Exception e) {
            throw new DataProcessingException(e.getMessage(), e);
        }
    }




    /**
     * @roseuid 3E7B17250186
     * @J2EE_METHOD -- preparePersonNameBeforePersistence
     */
    @Transactional
    public PersonContainer preparePersonNameBeforePersistence(PersonContainer personContainer) throws DataProcessingException {
        try {
            Collection<PersonNameDto> namesCollection = personContainer
                    .getThePersonNameDtoCollection();
            if (namesCollection != null && namesCollection.size() > 0) {

                Iterator<PersonNameDto> namesIter = namesCollection.iterator();
                PersonNameDto selectedNameDT = null;
                while (namesIter.hasNext()) {
                    PersonNameDto thePersonNameDto = (PersonNameDto) namesIter.next();
                    if (thePersonNameDto.getNmUseCd() != null
                            && !thePersonNameDto.getNmUseCd().trim().equals("L"))
                        continue;
                    if (thePersonNameDto.getAsOfDate() != null) {
                        if (selectedNameDT == null)
                            selectedNameDT = thePersonNameDto;
                        else if (selectedNameDT.getAsOfDate()!=null && thePersonNameDto.getAsOfDate()!=null  && thePersonNameDto.getAsOfDate().after(
                                selectedNameDT.getAsOfDate())) {
                            selectedNameDT = thePersonNameDto;
                        }
                    } else {
                        if (selectedNameDT == null)
                            selectedNameDT = thePersonNameDto;
                    }
                }
                if (selectedNameDT != null) {
                    personContainer.getThePersonDto().setLastNm(selectedNameDT.getLastNm());
                    personContainer.getThePersonDto().setFirstNm(
                            selectedNameDT.getFirstNm());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("EntityControllerEJB.preparePersonNameBeforePersistence: " + e.getMessage(), e);
            throw new DataProcessingException(e.getMessage(), e);
        }

        return personContainer;
    }




}