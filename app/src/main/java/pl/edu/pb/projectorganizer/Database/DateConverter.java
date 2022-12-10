package pl.edu.pb.projectorganizer.Database;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timescamp) {
        return timescamp == null ? null : new Date(timescamp);
    }

    @TypeConverter
    public static Long toTimeStamp(Date date) {return date == null ? null : date.getTime();}

}
