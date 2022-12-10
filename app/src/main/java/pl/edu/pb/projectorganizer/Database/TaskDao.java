package pl.edu.pb.projectorganizer.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao{

    @Query("SELECT * FROM task")
    LiveData<List<Task>> loadAllTasks();

    @Insert
    void insertTask(Task task);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM task")
    void deleteAll();

    @Query("SELECT * FROM task WHERE id = :id")
    LiveData<Task> loadTaskById(int id);
}