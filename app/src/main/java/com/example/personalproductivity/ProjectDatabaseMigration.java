package com.example.personalproductivity;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class ProjectDatabaseMigration {

    public static final Migration[] migrations =  {
            new Migration(1, 2) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Project ADD COLUMN completionStatus TEXT DEFAULT 'IN_PROGRESS'");
//                    database.execSQL("ALTER TABLE Project ADD COLUMN countAsWork INTEGER DEFAULT 1");
                    database.execSQL("ALTER TABLE TaskGroup ADD COLUMN completionStatus TEXT DEFAULT 'IN_PROGRESS'");
                    database.execSQL("ALTER TABLE Task ADD COLUMN completionStatus TEXT DEFAULT 'IN_PROGRESS'");
                }
            }
    };
}
