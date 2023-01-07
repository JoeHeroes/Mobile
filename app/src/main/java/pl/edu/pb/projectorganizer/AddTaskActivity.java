package pl.edu.pb.projectorganizer;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import pl.edu.pb.projectorganizer.Database.Task;
import pl.edu.pb.projectorganizer.Database.TaskDatabase;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AddTaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String EXTRA_TASK_ID = "extraTaskId";
    public static final String INSTANCE_TASK_ID = "instanceTaskId";
    private static final String DATE_FORMAT = "dd/MM/yyy";

    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;


    public static final int PLACE_HOME = 1;
    public static final int PLACE_STUDIES = 2;
    public static final int PLACE_GYM = 3;
    public static final int PLACE_WORK = 4;

    private static final int DEFAULT_TASK_ID = -1;
    private static final String TAG = AddTaskActivity.class.getSimpleName();
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();

    EditText DescriptionText;
    RadioGroup RadioGroupPriority ;
    RadioGroup RadioGroupPlace ;
    EditText DateText;
    Button mButton;
    Button wButton;
    Date currentData;
    private int mTaskId = DEFAULT_TASK_ID;

    private TaskDatabase taskDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        wButton = findViewById(R.id.weatherButton);

        wButton.setOnClickListener((v)->{
            Intent intent = new Intent(AddTaskActivity.this,WeatherActivity.class);
            intent.putExtra("SHOW_WELCOME", true);
            startActivity(intent);
        });

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
        DescriptionText = findViewById(R.id.editTextTaskDescription);
        RadioGroupPriority = findViewById(R.id.radioGroupPriority);
        RadioGroupPlace = findViewById(R.id.radioGroupPlace);
        DateText = findViewById(R.id.editTextTaskDate);
        setupDateFieldValue(new Date());
        DatePickerDialog.OnDateSetListener date=(view12, year, month, day)->{
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            setupDateFieldValue(calendar.getTime());
        };

        DateText.setOnClickListener(view1->
                new DatePickerDialog(this,date,calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
                        .show());


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
        DescriptionText.setText(task.getDescription());

        String data = dateFormat.format(task.getDate());
        DateText.setText(data);
        setPriorityInViews(task.getPriority());
        setPlaceInViews(task.getPlace());
    }
    private void setupDateFieldValue(Date date) {
        Locale locale=new Locale("pl","PL");
        SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy",locale);
        DateText.setText(dateFormat.format(date));
        currentData = date;
    }




    public void onSaveButtonClicked() {
        String description = DescriptionText.getText().toString();
        int place = getPlaceFromViews();
        int priority = getPriorityFromViews();
        Date date = currentData;
        currentData = null;




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

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

    }
}
