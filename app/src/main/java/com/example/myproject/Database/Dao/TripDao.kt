package com.example.myproject.Database.Dao
import android.credentials.CredentialDescription
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripType

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_table")
    fun getListOfTrips(): LiveData<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(trip: Trip): Long

    @Query("SELECT MAX(id) FROM trip_table WHERE destination = null OR destination = ''")
    fun getLastTripId(): Int

    @Query("SELECT * FROM trip_table WHERE type = :type")
    fun getTripsByType(type: TripType): LiveData<List<Trip>>

    @Query("UPDATE trip_table SET destination = :destination, endDate = :endDate WHERE id = :id")
    fun updateTrip(id: Int, destination: String, endDate: String)

    @Query("DELETE FROM trip_table WHERE id = :id")
    fun delete(id: Int)

    @Query("UPDATE trip_table SET description = :description WHERE id= :id")
    fun addDescription(id: Int, description: String)

    @Query("SELECT id FROM trip_table WHERE (type = 'NO_PROGRAM' AND (destination IS NULL OR destination = '')) OR (date(startDate) <= date('now') AND date(endDate) >= date('now')) ORDER BY id DESC LIMIT 1")
    fun getCurrentTripId(): Int?

    @Query("SELECT count(*) FROM trip_table WHERE (type = 'NO_PROGRAM' AND (destination IS NULL OR destination = '')) OR (date(startDate) <= date('now') AND date(endDate) >= date('now')) ORDER BY id DESC LIMIT 1")
    fun getNumberCurrentTrip(): Int
    @Query("SELECT * FROM trip_table WHERE (:type IS NULL OR type = :type) AND (:fromDate IS NULL OR :fromDate = '' OR date(startDate) >= date(:fromDate)) AND (:toDate IS NULL OR :toDate = '' OR (endDate IS NOT NULL AND date(endDate) <= date(:toDate))) AND endDate IS NOT NULL\n")
    fun getFilteredTrips(type: TripType?, fromDate: String?, toDate: String?): LiveData<List<Trip>>

    @Query("SELECT pt.* FROM trip_table as t JOIN tripplace as tp ON t.id = tp.tripId JOIN place_table as pt ON pt.id = tp.placeId WHERE t.id = :tripId")
    fun getPlacesForTrip(tripId: Int): LiveData<List<Place>>

    @Query("SELECT * FROM trip_table WHERE date(endDate) < date('now') ORDER BY date(endDate) DESC LIMIT 1")
    fun getMostRecentEndedTrip(): Trip?

    @Query("SELECT * FROM trip_table WHERE endDate IS NOT NULL AND endDate != '' AND date(endDate) <= date('now')")
    fun getAllEndedTrips(): LiveData<List<Trip>>

    @Query("SELECT count(*) FROM trip_table WHERE id = :id")
    fun getTripCountById(id: Int): Int

    @Query("SELECT t.* FROM place_table p JOIN tripplace tp ON p.id = tp.placeId JOIN trip_table t ON t.id = tp.tripId WHERE t.startDate >= :fromDate AND t.endDate != '' AND t.endDate <= date('now') ")
    fun getTripsSince(fromDate: String): LiveData<List<Trip>>


}