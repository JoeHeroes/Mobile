package pl.edu.pb.projectorganizer;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import pl.edu.pb.projectorganizer.Database.TaskDatabase;

public class AddTaskViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final TaskDatabase taskDB;
    private final int mTaskId;

    public AddTaskViewModelFactory(TaskDatabase database, int mTaskId){
        taskDB = database;
        this.mTaskId = mTaskId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AddTaskViewModel(taskDB, mTaskId);
    }
}

