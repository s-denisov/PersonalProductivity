package com.example.personalproductivity;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Project.class, TaskGroup.class, Task.class}, version = 1)
public abstract class ProjectDatabase extends RoomDatabase {

    public abstract ProjectDao projectDao();

    private static ProjectDatabase INSTANCE;

    private static final RoomDatabase.Callback callback = new RoomDatabase.Callback() {};
    public static ProjectDatabase getDatabase(Context context) {
        if (INSTANCE != null) return INSTANCE;
        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    ProjectDatabase.class, "ProjectDatabase").addCallback(callback).build();
        return INSTANCE;
    }
}
