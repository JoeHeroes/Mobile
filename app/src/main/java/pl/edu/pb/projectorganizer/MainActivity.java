package pl.edu.pb.projectorganizer;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import pl.edu.pb.projectorganizer.Database.Task;
import pl.edu.pb.projectorganizer.Database.TaskDatabase;

public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener, TaskAdapter.CheckBoxCheckListener{

    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;
    private ProgressBar mprogressBar;
    private TextView mProgressValue;
    private TextView mEmptyView;
    private ConstraintLayout mConstraintLayout;


    private double mTotalProgressPercent;
    private TaskDatabase taskDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mprogressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recyclerViewTasks);
        mProgressValue = findViewById(R.id.progressValue);
        mEmptyView = findViewById(R.id.emptyView);
        mConstraintLayout = findViewById(R.id.constraintLayout);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new TaskAdapter(this, this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        taskDB = TaskDatabase.getInstance(getApplicationContext());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mConstraintLayout.getLayoutParams();
        params.bottomMargin = 0;


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            boolean drag = false;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();

                Collections.swap(mAdapter.getTasks(), position_dragged, position_target);
                mAdapter.notifyItemMoved(position_dragged, position_target);

                return false;
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    drag = true;
                }

                if(actionState == ItemTouchHelper.ACTION_STATE_IDLE && drag) {
                    Log.d("DragTest","DRAGGGING stop");
                    drag= false;

                    final List<Task> NewTasks =  mAdapter.getTasks();
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                taskDB.taskDao().deleteAll();

                                for(int i =0; i < NewTasks.size(); i++){
                                    Task task = NewTasks.get(i);
                                    taskDB.taskDao().insertTask(new Task(
                                            task.getDescription(),
                                            task.getPlace(),
                                            task.getPriority(),
                                            task.getDate(),
                                            task.isChecked()
                                    ));
                                }
                            }catch (Exception ignored){}

                        }
                    });
                }
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                List<Task> tasks =  mAdapter.getTasks();
                final Task taskToBeDeleted = tasks.get(position);

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            taskDB.taskDao().deleteTask(taskToBeDeleted);
                        }catch (Exception ignored){}
                    }
                });

                Snackbar snackbar = Snackbar
                        .make(viewHolder.itemView, "Task deleted!", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    taskDB.taskDao().insertTask(taskToBeDeleted);
                                }catch (Exception ignored){}
                            }
                        });

                    }
                }).show();
            }
        }).attachToRecyclerView(mRecyclerView);


        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });
        setupViewModel();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_all_tasks:
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("Delete all tasks")
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        taskDB.taskDao().deleteAll();
                                    }
                                });
                            }
                        })
                        .setMessage( "Do you want to delete all tasks?")
                        .show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }

    @Override
    public void onCheckBoxCheckListener(final Task taskEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                taskDB.taskDao().updateTask(taskEntry);
            }
        });
    }


    private void setupViewModel() {
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> taskEntries) { //runs on main thread


                if(taskEntries.isEmpty()){
                    mprogressBar.setVisibility(View.INVISIBLE);
                    mProgressValue.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }else {
                    mprogressBar.setVisibility(View.VISIBLE);
                    mProgressValue.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }

                calculatePercent(taskEntries);
                mprogressBar.setProgress((int)mTotalProgressPercent);
                mProgressValue.setText((int)mTotalProgressPercent + " %");

                mAdapter.setTasks(taskEntries);
            }
        });
    }

    private void calculatePercent(List<Task> taskEntries) {
        int countChecked = 0;
        for(Task i: taskEntries){
            if(i.isChecked()) countChecked++;
        }
        mTotalProgressPercent = (double)countChecked/taskEntries.size() *100;
    }
}
