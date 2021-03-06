/*
    Copyright (c) 2010-2015 Darshan-Josiah Barber

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
*/

package com.darshancomputing.BatteryIndicatorPro;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabase {
    private static final String DATABASE_NAME    = "alarms.db";
    private static final int    DATABASE_VERSION = 5;
    private static final String ALARM_TABLE_NAME = "alarms";

    public static final String KEY_ID        = "_id";
    public static final String KEY_ENABLED   = "enabled";
    public static final String KEY_TYPE      = "type";
    public static final String KEY_THRESHOLD = "threshold";
    public static final String KEY_RINGTONE  = "ringtone";
    public static final String KEY_VIBRATE   = "vibrate";
    public static final String KEY_LIGHTS    = "lights";

    /* Is this a safe practice, or do I need to use Cursor.getColumnIndexOrThrow()? */
    public static final int INDEX_ID        = 0;
    public static final int INDEX_ENABLED   = 1;
    public static final int INDEX_TYPE      = 2;
    public static final int INDEX_THRESHOLD = 3;
    public static final int INDEX_RINGTONE  = 4;
    public static final int INDEX_VIBRATE   = 5;
    public static final int INDEX_LIGHTS    = 6;

    private final SQLOpenHelper mSQLOpenHelper;
    private SQLiteDatabase rdb;
    private SQLiteDatabase wdb;

    public AlarmDatabase(Context context) {
        mSQLOpenHelper = new SQLOpenHelper(context);

        openDBs();
    }

    private void openDBs(){
        if (rdb == null || !rdb.isOpen()) {
            try {
                rdb = mSQLOpenHelper.getReadableDatabase();
            } catch (SQLiteException e) {
                rdb = null;
            }
        }

        if (wdb == null || !wdb.isOpen()) {
            try {
                wdb = mSQLOpenHelper.getWritableDatabase();
            } catch (SQLiteException e) {
                rdb = null;
            }
        }
    }

    public void close() {
        if (rdb != null)
            rdb.close();
        if (wdb != null)
            wdb.close();
    }

    public Cursor getAllAlarms(Boolean reversed) {
        String order = "DESC";
        if (reversed) order = "ASC";

        openDBs();

        try {
            return rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " ORDER BY " + KEY_ID + " " + order, null);
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getAlarm(int id) {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE "+ KEY_ID + "=" + id + " LIMIT 1", null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean anyActiveAlarms() {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE ENABLED=1 LIMIT 1", null);
            Boolean b = (c.getCount() > 0);
            c.close();
            return b;
        } catch (Exception e) {
            return false;
        }
    }

    public Cursor activeAlarmFull() {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE "+ KEY_TYPE + "='fully_charged' AND ENABLED=1 LIMIT 1", null);

            if (c.getCount() == 0) {
                c.close();
                return null;
            }

            c.moveToFirst();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor activeAlarmChargeDrops(int current, int previous) {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE "+ KEY_TYPE +
                                    "='charge_drops' AND ENABLED=1 AND " +
                                    KEY_THRESHOLD + ">"  + current + " AND " +
                                    KEY_THRESHOLD + "<=" + previous +
                                    " LIMIT 1", null);

            if (c.getCount() == 0) {
                c.close();
                return null;
            }

            c.moveToFirst();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor activeAlarmChargeRises(int current, int previous) {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE "+ KEY_TYPE +
                                    "='charge_rises' AND ENABLED=1 AND " +
                                    KEY_THRESHOLD + "<"  + current + " AND " +
                                    KEY_THRESHOLD + ">=" + previous +
                                    " LIMIT 1", null);

            if (c.getCount() == 0) {
                c.close();
                return null;
            }

            c.moveToFirst();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor activeAlarmTempRises(int current, int previous) {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE "+ KEY_TYPE +
                                    "='temp_rises' AND ENABLED=1 AND " +
                                    KEY_THRESHOLD + "<"  + current + " AND " +
                                    KEY_THRESHOLD + ">=" + previous +
                                    " LIMIT 1", null);

            if (c.getCount() == 0) {
                c.close();
                return null;
            }

            c.moveToFirst();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor activeAlarmFailure() {
        openDBs();

        try {
            Cursor c = rdb.rawQuery("SELECT * FROM " + ALARM_TABLE_NAME + " WHERE "+ KEY_TYPE + "='health_failure' AND ENABLED=1 LIMIT 1", null);

            if (c.getCount() == 0) {
                c.close();
                return null;
            }

            c.moveToFirst();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public int addAlarm(Boolean enabled, String type, String threshold, String ringtone, Boolean vibrate, Boolean lights) {
        openDBs();

        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_ENABLED, enabled ? 1 : 0);
            cv.put(KEY_TYPE, type);
            cv.put(KEY_THRESHOLD, threshold);
            cv.put(KEY_RINGTONE, ringtone);
            cv.put(KEY_VIBRATE, vibrate ? 1 : 0);
            cv.put(KEY_LIGHTS, lights ? 1 : 0);
            return (int) wdb.insert(ALARM_TABLE_NAME, null, cv);
        } catch (Exception e) {
            return -1;
        }
    }

    public int addAlarm() {
        return addAlarm(true, "fully_charged", "", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString(), false, true);
    }

    public int setEnabled(int id, Boolean enabled) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ENABLED, enabled ? 1 : 0);

        openDBs();

        try {
            return wdb.update(ALARM_TABLE_NAME, cv, KEY_ID + "=" + id, null);
        } catch (Exception e) {
            return -1;
        }
    }

    public int setVibrate(int id, Boolean vibrate) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_VIBRATE, vibrate ? 1 : 0);

        openDBs();

        try {
            return wdb.update(ALARM_TABLE_NAME, cv, KEY_ID + "=" + id, null);
        } catch (Exception e) {
            return -1;
        }
    }

    public int setLights(int id, Boolean lights) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_LIGHTS, lights ? 1 : 0);

        openDBs();

        try {
            return wdb.update(ALARM_TABLE_NAME, cv, KEY_ID + "=" + id, null);
        } catch (Exception e) {
            return -1;
        }
    }

    public Boolean toggleEnabled(int id) {
        openDBs();

        try {
            Cursor c = rdb.query(ALARM_TABLE_NAME, new String[] {KEY_ENABLED}, KEY_ID + "=" + id, null, null, null, null, null);
            c.moveToFirst();
            Boolean newEnabled = !(c.getInt(0) == 1);
            c.close();

            setEnabled(id, newEnabled);

            return newEnabled;
        } catch (Exception e) {
            return false;
        }
    }

    public int setType(int id, String type) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TYPE, type);

        openDBs();

        try {
            return wdb.update(ALARM_TABLE_NAME, cv, KEY_ID + "=" + id, null);
        } catch (Exception e) {
            return -1;
        }
    }

    public int setThreshold(int id, String threshold) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_THRESHOLD, threshold);

        openDBs();

        try {
            return wdb.update(ALARM_TABLE_NAME, cv, KEY_ID + "=" + id, null);
        } catch (Exception e) {
            return -1;
        }
    }

    public int setRingtone(int id, String ringtone) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_RINGTONE, ringtone);

        openDBs();

        try {
            return wdb.update(ALARM_TABLE_NAME, cv, KEY_ID + "=" + id, null);
        } catch (Exception e) {
            return -1;
        }
    }

    public void deleteAlarm(int id) {
        openDBs();

        try {
            wdb.delete(ALARM_TABLE_NAME, KEY_ID + "=" + id, null);
        } catch (Exception e) {
        }
    }

    public void deleteAllAlarms() {
        mSQLOpenHelper.reset();
    }

    private static class SQLOpenHelper extends SQLiteOpenHelper {
        public SQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + ALARM_TABLE_NAME + " ("
                       + KEY_ID        + " INTEGER PRIMARY KEY,"
                       + KEY_ENABLED   + " INTEGER,"
                       + KEY_TYPE      + " STRING,"
                       + KEY_THRESHOLD + " STRING,"
                       + KEY_RINGTONE  + " STRING,"
                       + KEY_VIBRATE   + " INTEGER,"
                       + KEY_LIGHTS    + " INTEGER"
                       + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (false) {
            } else {
                db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE_NAME);
                onCreate(db);
            }
        }

        public void reset() {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE_NAME);
            onCreate(db);
        }
    }
}
