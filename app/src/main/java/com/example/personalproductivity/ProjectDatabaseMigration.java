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
            }, new Migration(2, 3) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE TaskTimeRecord (id INTEGER NOT NULL, startTimeStamp INTEGER NOT NULL," +
                            " daysSinceEpoch INTEGER NOT NULL, length INTEGER NOT NULL, taskId INTEGER NOT NULL, PRIMARY KEY(id))");
                }
            }, new Migration(3, 4) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("DROP TABLE TaskTimeRecord");
                    database.execSQL("CREATE TABLE TaskTimeRecord (startTimeStamp INTEGER NOT NULL," +
                            " daysSinceEpoch INTEGER NOT NULL, length INTEGER NOT NULL, taskId INTEGER NOT NULL, PRIMARY KEY(startTimeStamp))");
                }
            }, new Migration(4, 5) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE Day (daysSinceEpoch INTEGER NOT NULL, targetWorkTime INTEGER NOT NULL, PRIMARY KEY(daysSinceEpoch))");
                }
            }, new Migration(5, 6) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Day ADD COLUMN schoolTime INTEGER NOT NULL DEFAULT 0");
                }
            }, new Migration(6, 7) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE TaskTimeRecord ADD COLUMN privateStudy INTEGER NOT NULL DEFAULT 0");
                }
            }
    };
}
