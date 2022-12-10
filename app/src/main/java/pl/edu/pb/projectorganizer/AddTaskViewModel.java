package pl.edu.pb.projectorganizer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import pl.edu.pb.projectorganizer.Database.Task;
import pl.edu.pb.projectorganizer.Database.TaskDatabase;

public class AddTaskViewModel extends ViewModel {
    private LiveData<Task> task;

    public AddTaskViewModel(TaskDatabase database, int taskId) {
        task =  database.taskDao().loadTaskById(taskId);
    }

    public LiveData<Task> getTask() {
        return task;
    }
}
