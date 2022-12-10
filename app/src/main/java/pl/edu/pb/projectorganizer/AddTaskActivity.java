package pl.edu.pb.projectorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import pl.edu.pb.projectorganizer.Database.Task;
import pl.edu.pb.projectorganizer.Database.TaskDatabase;

public class AddTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extraTaskId";
    public static final String INSTANCE_TASK_ID = "instanceTaskId";

    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;


    public static final int PLACE_HOME = 1;
    public static final int PLACE_STUDIES = 2;
    public static final int PLACE_GYM = 3;
    public static final int PLACE_WORK = 4;

    private static final int DEFAULT_TASK_ID = -1;
    private static final String TAG = AddTaskActivity.class.getSimpleName();

    EditText mEditText;
    RadioGroup mRadioGroupPriority ;
    RadioGroup mRadioGroupPlace ;
    Button mButton;

    private int mTaskId = DEFAULT_TASK_ID;

    private TaskDatabase taskDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();

        taskDB = TaskDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TASK_ID)) {
            mTaskId = savedInstanceState.getInt(INSTANCE_TASK_ID, DEFAULT_TASK_ID);
        }


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TASK_ID)) {
            mButton.setText(R.string.update_button);
            setTitle("Update Task");
            if (mTaskId == DEFAULT_TASK_ID) {
                mTaskId = intent.getIntExtra(EXTRA_TASK_ID, DEFAULT_TASK_ID);

                AddTaskViewModelFactory factory = new AddTaskViewModelFactory(taskDB, mTaskId);
                final AddTaskViewModel viewModel = new ViewModelProvider((ViewModelStoreOwner) this, (ViewModelProvider.Factory) factory).get(AddTaskViewModel.class);
                viewModel.getTask().observe(this, new Observer<Task>() {
                    @Override
                    public void onChanged(Task taskEntry) {
                        viewModel.getTask().removeObserver(this);
                        populateUI(taskEntry);
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_TASK_ID, mTaskId);
        super.onSaveInstanceState(outState);
    }


    private void initViews() {
        mEditText = findViewById(R.id.editTextTaskDescription);
        mRadioGroupPriority = findViewById(R.id.radioGroupPriority);
        mRadioGroupPlace = findViewById(R.id.radioGroupPlace);


        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }


    private void populateUI(Task task) {
        if (task == null ){
            return;
        }
        mEditText.setText(task.getDescription());
        setPriorityInViews(task.getPriority());
        setPlaceInViews(task.getPlace());
    }


    public void onSaveButtonClicked() {
        String description = mEditText.getText().toString();
        int place = getPlaceFromViews();
        int priority = getPriorityFromViews();
        Date date = new Date();




        if(description.equals("")){
            Toast.makeText(this,"Describe your task", Toast.LENGTH_SHORT).show();
        }else{
            final Task taskEntry = new Task(description, place, priority, date, false );

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if(mTaskId == DEFAULT_TASK_ID){
                        taskDB.taskDao().insertTask(taskEntry);
                    }else {
                        taskEntry.setId(mTaskId);
                        taskDB.taskDao().updateTask(taskEntry);
                    }
                    finish(); //automatically return to main activity
                }
            });
        }
    }


    public int getPriorityFromViews() {
        int priority = 1;
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroupPriority)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButtonPriority1:
                priority = PRIORITY_HIGH;
                break;
            case R.id.radButtonPriority2:
                priority = PRIORITY_MEDIUM;
                break;
            case R.id.radButtonPriority3:
                priority = PRIORITY_LOW;
        }
        return priority;
    }


    public void setPriorityInViews(int priority) {
        switch (priority) {
            case PRIORITY_HIGH:
                ((RadioGroup) findViewById(R.id.radioGroupPriority)).check(R.id.radButtonPriority1);
                break;
            case PRIORITY_MEDIUM:
                ((RadioGroup) findViewById(R.id.radioGroupPriority)).check(R.id.radButtonPriority2);
                break;
            case PRIORITY_LOW:
                ((RadioGroup) findViewById(R.id.radioGroupPriority)).check(R.id.radButtonPriority3);
        }
    }

    public int getPlaceFromViews() {
        int place = 1;
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroupPlace)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButtonPlace1:
                place = PLACE_HOME;
                break;
            case R.id.radButtonPlace2:
                place = PLACE_STUDIES;
                break;
            case R.id.radButtonPlace3:
                place = PLACE_GYM;
                break;
            case R.id.radButtonPlace4:
                place = PLACE_WORK;
                break;
        }
        return place;
    }


    public void setPlaceInViews(int place) {
        switch (place) {
            case PLACE_HOME:
                ((RadioGroup) findViewById(R.id.radioGroupPlace)).check(R.id.radButtonPlace1);
                break;
            case PLACE_STUDIES:
                ((RadioGroup) findViewById(R.id.radioGroupPlace)).check(R.id.radButtonPlace2);
                break;
            case PLACE_GYM:
                ((RadioGroup) findViewById(R.id.radioGroupPlace)).check(R.id.radButtonPlace3);
                break;
            case PLACE_WORK:
                ((RadioGroup) findViewById(R.id.radioGroupPlace)).check(R.id.radButtonPlace4);
        }
    }
}
