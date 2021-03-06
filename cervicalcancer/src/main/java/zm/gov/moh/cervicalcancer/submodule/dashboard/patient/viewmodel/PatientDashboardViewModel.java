package zm.gov.moh.cervicalcancer.submodule.dashboard.patient.viewmodel;

import android.app.Application;
import android.os.Bundle;

import com.google.common.collect.ImmutableList;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import zm.gov.moh.cervicalcancer.ModuleConfig;
import zm.gov.moh.cervicalcancer.submodule.dashboard.patient.model.VisitState;
import zm.gov.moh.core.model.Key;
import zm.gov.moh.core.repository.database.Database;
import zm.gov.moh.core.repository.database.DatabaseUtils;
import zm.gov.moh.core.repository.database.dao.derived.GenericDao;
import zm.gov.moh.core.repository.database.dao.domain.ConceptDao;
import zm.gov.moh.core.repository.database.dao.domain.VisitDao;
import zm.gov.moh.core.repository.database.entity.domain.Obs;
import zm.gov.moh.core.repository.database.entity.domain.Visit;
import zm.gov.moh.core.utils.BaseAndroidViewModel;
import zm.gov.moh.core.utils.InjectableViewModel;

public class PatientDashboardViewModel extends BaseAndroidViewModel implements InjectableViewModel {

    private MutableLiveData<Integer> emitVisitState;
    private Bundle bundle;
    private VisitState visitState;
    private Visit visit;
    VisitDao visitDao = getRepository().getDatabase().visitDao();
    Database db = getRepository().getDatabase();
    long person_id;

    private MutableLiveData<LinkedHashMap<Long,Collection<Boolean>>> screeningDataEmitter;
    private MutableLiveData<LinkedHashMap<Long,Collection<Boolean>>> referralDataEmitter;
    private MutableLiveData<LinkedHashMap<Long,Collection<Boolean>>> treatmentDataEmitter;
    private MutableLiveData<LinkedHashMap<Long,Collection<String>>> providerDataEmitter;

    public PatientDashboardViewModel(Application application){
        super(application);

        this.visitState = new VisitState();


    }

    public void setVisitState(final int state){

        visitState.setState(state);
        persistVisit(state);

        emitVisitState.setValue(state);
    }

    public MutableLiveData<Integer> getEmitVisitState() {

        if(emitVisitState == null)
            emitVisitState = new MutableLiveData<>();

        return emitVisitState;
    }

    public MutableLiveData<LinkedHashMap<Long,Collection<Boolean>>> getScreeningDataEmitter() {

        if(screeningDataEmitter == null)
            screeningDataEmitter = new MutableLiveData<>();

        return screeningDataEmitter;
    }

    public MutableLiveData<LinkedHashMap<Long,Collection<Boolean>>> getReferralDataEmitter() {

        if(referralDataEmitter == null)
            referralDataEmitter = new MutableLiveData<>();

        return referralDataEmitter;
    }

    public MutableLiveData<LinkedHashMap<Long,Collection<Boolean>>> getTreatmentDataEmitter() {

        if(treatmentDataEmitter == null)
            treatmentDataEmitter = new MutableLiveData<>();

        return treatmentDataEmitter;
    }

    public MutableLiveData<LinkedHashMap<Long,Collection<String>>> getProviderDataEmitter() {

        if(providerDataEmitter == null)
            providerDataEmitter = new MutableLiveData<>();

        return providerDataEmitter;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;

        person_id = (Long) bundle.get(Key.PERSON_ID);
    }

    public Bundle getBundle() {

        if(bundle == null)
            bundle = new Bundle();

        return bundle;
    }

    public VisitState getVisitState() {
        return visitState;
    }

    public void persistVisit(int visitState){


        if(visit == null)
            visit = new Visit();

        if(visitState == VisitState.STARTED)
            getRepository().consumeAsync(this::createVisit, this::onError, bundle);
        else if(visit.getDate_started() != null) {

            visit.setDate_stopped(LocalDateTime.now());
            getRepository().consumeAsync( visit-> visitDao.updateVisit(visit),this::onError,visit);
        }
    }


    public LinkedHashMap<Long, Collection<Boolean>> extractScreeningData(List<Visit> visits){

        final long CONCEPT_ID_VIA_INSPECTION_DONE = db.conceptDao().getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_VIA_INSPECTION_DONE);

        if(visits.size() > 0) {

            ConceptDao conceptDao = getRepository().getDatabase().conceptDao();
            GenericDao genericDao = getRepository().getDatabase().genericDao();
            LinkedHashMap<Long, Collection<Boolean>> screeningResults = new LinkedHashMap<>();

            for(Visit visit: visits) {
                long currentVisitId = visit.visit_id;

                LinkedHashMap<Long, Boolean> screeningData = new LinkedHashMap<>();

                List<Obs> obsIterator = getRepository()
                        .getDatabase()
                        .genericDao()
                        .getPatientObsByEncounterTypeAndVisitId(person_id,visit.getVisit_id(), ModuleConfig.ENCOUNTER_TYPE_UUID_TEST_RESULT);

                List<Obs> obsList = db.genericDao().getPatientObsByConceptIdVisitId(34L,db.conceptDao().getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_VIA_SCREENING_RESULT),9223372036854725890L);


                for (Obs obs:obsIterator) {

                    //Obs obs = obsIterator.next();

                    Long i = genericDao.getPatientObsCodedValueByEncounterIdConceptId(person_id,CONCEPT_ID_VIA_INSPECTION_DONE,obs.getEncounter_id());

                    screeningData.put(CONCEPT_ID_VIA_INSPECTION_DONE, false);
                    screeningData.put(conceptDao.getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_VIA_NEGATIVE), false);
                    screeningData.put(conceptDao.getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_VIA_POSITIVE), false);
                    screeningData.put(conceptDao.getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_SUSPECTED_CANCER), false);


                    if (screeningData.containsKey(obs.getValue_coded())|| i != null) {

                        if(i != null && i == 1)
                            screeningData.put(CONCEPT_ID_VIA_INSPECTION_DONE, true);

                        Long datetime = obs.getObs_datetime().toInstant(ZoneOffset.UTC).getEpochSecond();

                        if(screeningData.containsKey(obs.getValue_coded())) {
                            screeningData.put(obs.getValue_coded(), true);
                            screeningResults.put(datetime, ImmutableList.copyOf(screeningData.values()));
                        }
                    }
                }
            }
            return screeningResults;
        }
        return null;
    }


    public LinkedHashMap<Long, Collection<Boolean>> extractReferralData(List<Visit> visits){

        if(visits.size() > 0) {

            LinkedHashMap<Long, Collection<Boolean>> referralResults = new LinkedHashMap<>();
            final long CONCEPT_ID_REASON_FOR_REFERRAL = db.conceptDao().getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_REASON_FOR_REFERRAL);

            for(Visit visit: visits) {

                long datatime = visit.getDate_started().toInstant(ZoneOffset.UTC).getEpochSecond();
                LinkedHashMap<Long, Boolean> referralData = new LinkedHashMap<>();
                List<Long> codedValues = db.genericDao().getPatientObsCodedValueByVisitIdConceptId(person_id,visit.getVisit_id(),CONCEPT_ID_REASON_FOR_REFERRAL);

                referralData.put(db.conceptDao().getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_LARGE_LESION_REFFERAL), false);
                referralData.put(db.conceptDao().getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_SUSPECTED_CANCER_REFFERAL), false);

                if(codedValues != null && !codedValues.isEmpty()){

                    for(Long codedValue: codedValues)
                        referralData.put(codedValue,true);

                    referralResults.put(datatime, ImmutableList.copyOf(referralData.values()));
                }
            }
            return referralResults;
        }
        return null;
    }

    public LinkedHashMap<Long, Collection<Boolean>> extractTreatmentData(List<Visit> visits){

        final Long cryoThermolDoneToday = 165174165175L;
        final Long cryoThermolDoneDelayed = 165334176165177L;
        final Long cryoThermolDonePostponed = 165176165177L;

        if(visits.size() > 0) {

            LinkedHashMap<Long, Collection<Boolean>> treatResults = new LinkedHashMap<>();
            final long CONCEPT_ID_VIA_TREATMENT_TYPE_DONE = db.conceptDao().getConceptIdByUuid(ModuleConfig.CONCEPT_UUID_VIA_TREATMENT_TYPE_DONE);

            for(Visit visit: visits) {

                long datatime = visit.getDate_started().toInstant(ZoneOffset.UTC).getEpochSecond();
                LinkedHashMap<Long, Boolean> treatmentData = new LinkedHashMap<>();
                List<Long> codedValues = db.genericDao().getPatientObsCodedValueByVisitIdConceptId(person_id,visit.getVisit_id(), CONCEPT_ID_VIA_TREATMENT_TYPE_DONE);

                treatmentData.put(cryoThermolDoneToday, false);
                treatmentData.put(cryoThermolDonePostponed, false);
                treatmentData.put(cryoThermolDoneDelayed, false);

                if(codedValues != null && !codedValues.isEmpty()){

                    for(Long codedValue: codedValues) {

                        if(cryoThermolDoneToday.toString().contains(codedValue.toString()))
                            treatmentData.put(cryoThermolDoneToday, true);

                        if(cryoThermolDoneDelayed.toString().contains(codedValue.toString()))
                            treatmentData.put(cryoThermolDoneDelayed, true);

                    }

                    treatResults.put(datatime, ImmutableList.copyOf(treatmentData.values()));
                }
            }
            return treatResults;
        }
        return null;
    }

    public void onVisitsRetrieved(List<Visit> visits){

        getRepository().asyncFunction(this::extractScreeningData, getScreeningDataEmitter()::setValue, visits, this::onError);
        getRepository().asyncFunction(this::extractReferralData, getReferralDataEmitter()::setValue, visits, this::onError);
        getRepository().asyncFunction(this::extractTreatmentData, getTreatmentDataEmitter()::setValue, visits, this::onError);
        getRepository().asyncFunction(this::extractProviderData, getProviderDataEmitter()::setValue, visits, this::onError);
    }

    public void onError(Throwable throwable){

    }

    public void createVisit(Bundle bundle){

        long visit_id = DatabaseUtils.generateLocalId(getRepository().getDatabase().visitDao()::getMaxId);
        long visit_type_id = (Long) bundle.get(Key.VISIT_TYPE_ID);
        long location_id = (Long) bundle.get(Key.LOCATION_ID);
        long creator = (Long) bundle.get(Key.USER_ID);
        LocalDateTime start_time = LocalDateTime.now();

        visit = new Visit(visit_id,visit_type_id,person_id,location_id,creator,start_time);
        visitDao.insert(visit);
        bundle.putLong(Key.VISIT_ID, visit_id);
    }

    public LinkedHashMap<Long, Collection<String>> extractProviderData(List<Visit> visits){


        LinkedHashMap<Long, String> providerData = new LinkedHashMap<>();

        if(visits.size() > 0) {

            LinkedHashMap<Long, Collection<String>> providerResults = new LinkedHashMap<>();

            for(Visit visit: visits) {
                long datatime = visit.getDate_started().toInstant(ZoneOffset.UTC).getEpochSecond();
                Long treatmentEncounterId = db.genericDao().getPatientEncounterIdByVisitIdEncounterTypeId(person_id,visit.getVisit_id(),ModuleConfig.ENCOUNTER_TYPE_UUID_TREAMENT);
                Long screeningEncounterId = db.genericDao().getPatientEncounterIdByVisitIdEncounterTypeId(person_id,visit.getVisit_id(),ModuleConfig.ENCOUNTER_TYPE_UUID_TEST_RESULT);


                if(treatmentEncounterId !=null && screeningEncounterId != null) {
                    long treatmentProviderId = db.encounterProviderDao().getByEncounterId(treatmentEncounterId).provider_id;
                    long screeningProviderId = db.encounterProviderDao().getByEncounterId(screeningEncounterId).provider_id;

                    String treatmentProviderName = db.providerDao().getById(treatmentProviderId).name;
                    String screeningProviderName = db.providerDao().getById(screeningProviderId).name;

                    if (treatmentProviderName != null && screeningProviderName != null) {

                        providerData.put(1L, screeningProviderName);
                        providerData.put(2L, treatmentProviderName);

                        providerResults.put(datatime, providerData.values());
                    }
                }
            }
            return providerResults;
        }
        return null;
    }
}
