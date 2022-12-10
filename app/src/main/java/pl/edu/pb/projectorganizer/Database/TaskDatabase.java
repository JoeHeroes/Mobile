package pl.edu.pb.projectorganizer.Database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class TaskDatabase extends RoomDatabase {

    private static final String LOG_TAG = TaskDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "todolist";
    private static TaskDatabase Instance;

    public static TaskDatabase getInstance(Context context) {
        if(Instance == null){
            synchronized (LOCK){
                Log.d(LOG_TAG, "Creating new database instance");
                Instance = Room.databaseBuilder(context.getApplicationContext(),
                        TaskDatabase.class, TaskDatabase.DATABASE_NAME).build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return Instance;
    }

    public abstract TaskDao taskDao();
}
