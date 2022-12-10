package pl.edu.pb.projectorganizer.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "task")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "Description")
    private String description;
    @ColumnInfo(name = "Place")
    private int place;

    @ColumnInfo(name = "Priority")
    private int priority;
    @ColumnInfo(name = "Date")
    private Date date;
    @ColumnInfo(name = "Checked")
    private boolean checked;



    @Ignore
    public Task(String description, int place, int priority, Date date, boolean checked) {
        this.description = description;
        this.place = place;
        this.priority = priority;
        this.date = date;
        this.checked = checked;
    }

    public Task(int id, String description, int place,int priority, Date date, boolean checked){
        this.id = id;
        this.description = description;
        this.place = place;
        this.priority = priority;
        this.date = date;
        this.checked = checked;
    }

    public int getId(){ return this.id; }
    public void setId(int id){ this.id = id; }


    public String getDescription(){ return this.description; }
    public void setDescription(String description){ this.description = description; }


    public int getPlace(){ return this.place; }
    public void setPlace(int place){ this.place = place; }


    public int getPriority() { return this.priority; }
    public void setPriority(int priority) { this.priority = priority; }


    public Date getDate(){ return this.date; }
    public void setDate(Date date){ this.date = date; }


    public boolean isChecked() { return this.checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}