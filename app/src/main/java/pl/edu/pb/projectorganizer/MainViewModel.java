package pl.edu.pb.projectorganizer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import pl.edu.pb.projectorganizer.Database.Task;
import pl.edu.pb.projectorganizer.Database.TaskDatabase;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<Task>> tasks;

    public MainViewModel(@NonNull Application application){
        super(application);
        TaskDatabase database = TaskDatabase.getInstance(this.getApplication());
        tasks = database.taskDao().loadAllTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }
}