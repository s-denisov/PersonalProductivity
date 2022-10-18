package com.example.personalproductivity.db;

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
            }, new Migration(7, 8) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE Event (id INTEGER NOT NULL, name TEXT, startTimeStamp INTEGER NOT NULL," +
                            " daysSinceEpoch INTEGER NOT NULL, length INTEGER NOT NULL, PRIMARY KEY(id))");
                }
            }, new Migration(8, 9) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE ProjectTemp (id INTEGER NOT NULL PRIMARY KEY AUTOiNCREMENT," +
                            " name TEXT, completionStatus TEXT)");
                    database.execSQL("INSERT INTO ProjectTemp (name, completionStatus)" +
                            "SELECT name, completionStatus FROM Project");
                    database.execSQL("DROP TABLE Project");
                    database.execSQL("ALTER TABLE ProjectTemp RENAME TO Project");
                }
            }, new Migration(9, 10) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE TaskGroupTemp (id INTEGER NOT NULL, parentProjectId INTEGER NOT NULL, " +
                            " name TEXT, completionStatus TEXT, PRIMARY KEY(id))");
                    database.execSQL("INSERT INTO TaskGroupTemp (id, parentProjectId, name, completionStatus)" +
                            "SELECT id, id, name, completionStatus FROM TaskGroup");
                    database.execSQL("UPDATE TaskGroupTemp SET parentProjectId=(SELECT id FROM PROJECT WHERE name=" +
                            "(SELECT parentProjectName FROM TaskGroup WHERE id=TaskGroupTemp.id))");
                    database.execSQL("DROP TABLE TaskGroup");
                    database.execSQL("ALTER TABLE TaskGroupTemp RENAME TO TaskGroup");
                }
            }, new Migration(10, 11) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE TaskTemp (id INTEGER NOT NULL, parentTaskGroupId INTEGER NOT NULL, " +
                            "name TEXT, lastUsed INTEGER NOT NULL, completionStatus TEXT, PRIMARY KEY(id))");
                    database.execSQL("INSERT INTO TaskTemp (id, parentTaskGroupId, name, lastUsed, completionStatus)" +
                            "SELECT id, parentTaskGroupId, name, lastUsed, completionStatus FROM Task");
                    database.execSQL("DROP TABLE Task");
                    database.execSQL("ALTER TABLE TaskTemp RENAME TO Task");
                }
            },  new Migration(11, 12) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Task ADD COLUMN priority INTEGER NOT NULL DEFAULT 60");
                }
            },  new Migration(12, 13) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Task ADD COLUMN deadlineDate INTEGER NOT NULL DEFAULT 18700");
                    database.execSQL("ALTER TABLE Task ADD COLUMN expectedTime INTEGER NOT NULL DEFAULT 3600");
                }
            },  new Migration(13, 14) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("CREATE VIEW `TaskView` AS SELECT Task.id AS taskId, Task.name AS name, " +
                            "Task.completionStatus AS completionStatus, Task.parentTaskGroupId AS parentId, " +
                            "Sum(TaskTimeRecord.length) AS totalLength, SUM(CASE WHEN TaskTimeRecord.daysSinceEpoch=(SELECT MAX(daysSinceEpoch) " +
                            "FROM TaskTimeRecord) THEN TaskTimeRecord.length ELSE 0 END) AS lengthToday FROM Task INNER JOIN TaskTimeRecord ON TaskTimeRecord.taskId=Task.id");
                }
            },  new Migration(14, 15) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                }
            },  new Migration(15, 16) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Day ADD COLUMN missedSleep INTEGER NOT NULL DEFAULT 0");
                }
            },  new Migration(16, 17) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Event ADD COLUMN schoolLessons INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Event ADD COLUMN choreLength INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Event ADD COLUMN funLength INTEGER NOT NULL DEFAULT 0");
                }
            }
    };
}
