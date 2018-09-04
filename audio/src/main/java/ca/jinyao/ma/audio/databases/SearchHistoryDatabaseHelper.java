package ca.jinyao.ma.audio.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

/**
 * Class SearchHistoryDatabaseHelper
 * create by jinyaoMa 0022 2018/8/22 16:06
 */
public class SearchHistoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String name = "SearchHistoryDatabaseHelper.db";
    private static final int version = 1;

    public final String table = "records";
    public final String[] columns = {"_id", "keyword"};
    public final String KEYWORD = "keyword";

    public SearchHistoryDatabaseHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(Locale.getDefault(),
                "CREATE TABLE %s ( %s INTEGER PRIMARY KEY, %s TEXT NOT NULL)",
                table, columns[0], columns[1]));
    }

    public void insertRecord(String keyword) {
        if (getReadableDatabase().query(table, columns, columns[1] + "=?", new String[]{keyword}, null, null, null).getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(columns[1], keyword);
            getWritableDatabase().insert(table, null, values);
        }
    }

    public Cursor getRecordsCursor() {
        return getReadableDatabase().query(table, columns, null, null, null, null, columns[1]);
    }

    public Cursor getRecordsCursor(String keyword) {
        return getReadableDatabase().query(table, columns, columns[1] + " like ?", new String[]{"%" + keyword + "%"}, null, null, columns[1]);
    }

    public void clearRecords() {
        getWritableDatabase().delete(table, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
