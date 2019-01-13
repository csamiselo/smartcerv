package zm.gov.moh.core.repository.database.dao.domain;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import zm.gov.moh.core.repository.database.entity.domain.Obs;

@Dao
public interface ObsDao {

    //gets all persons
    @Query("SELECT * FROM obs")
    LiveData<List<Obs>> getAll();

    // Inserts single getPersons
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Obs obs);

    // Inserts single getPersons
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Obs... obs);

    //get getPersons by id
    @Query("SELECT * FROM obs WHERE person_id = :id")
    List<Obs> findByPatientId(long id);

    @Query("SELECT * FROM obs WHERE concept_id = :id")
    Obs findByConceptId(long id);

    @Query("SELECT * FROM obs WHERE concept_id IN (:ids)")
    List<Obs> findByConceptId(long... ids);
}