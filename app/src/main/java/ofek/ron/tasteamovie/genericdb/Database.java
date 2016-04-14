package ofek.ron.tasteamovie.genericdb;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Ofek on 22/09/2015.
 */
public abstract class Database extends SQLiteOpenHelper {
    private final Context context;
    private ArrayList<Table<?>> tables = new ArrayList<Table<?>>();
    private SQLiteDatabase database;

    public <T> Table<T> registerTable(Table<T> table) {
        tables.add(table);
        return table;
    }
    public Database(Context context, String name, int version) {
        this(context, name, null, version, null);
    }

    /**
     *
     * @param context
     * @param name
     * @param factory - can be null
     * @param version
     */
    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        registerTables();
        database = getWritableDatabase();
        this.context = context;
    }

    protected abstract void registerTables();

    @Override
    public void onCreate(SQLiteDatabase db) {
        for ( Table<?> t : tables ) {
            t.create(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        for ( Table<?> t : tables ) {
            t.setDatabse(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        for ( Table<?> t : tables ) {
            t.setDatabse(db);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for ( Table<?> t : tables ) {
            t.setDatabse(db);
        }


    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    public void close() {
        tables.clear();
        database.close();
        database =null;
    }
    public void destroy() {
        context.deleteDatabase(getDatabaseName());
    }

    public Context getContext() {
        return context;
    }



}
