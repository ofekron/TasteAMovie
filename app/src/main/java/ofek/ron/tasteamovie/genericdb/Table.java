package ofek.ron.tasteamovie.genericdb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Table<Item> {

	private String createCreateStatement(final Class<?> clazz) {
		final StringBuilder columns = new StringBuilder();
		final StringBuilder keys = new StringBuilder();
		int keysCount = 0;
		final ArrayList<Column> filedColumns = getAllFields("", new ArrayList<Column>(), clazz);

		for (final Column column : filedColumns)
			if (column.isKey())
				keysCount++;
		if (keysCount > 1) {
			for (final Column column : filedColumns) {
				columns.append(column);
				columns.append(',');
				if (column.isKey()) {
					keys.append(column.getName());
					keys.append(',');
				}
				this.columns.add(column);
			}
			keys.deleteCharAt(keys.length() - 1);
		} else {
			for (final Column column : filedColumns) {
				columns.append(column);
				if (column.isKey())
					columns.append(" PRIMARY KEY");
				columns.append(',');
				this.columns.add(column);
			}

		}
		columns.deleteCharAt(columns.length() - 1);
		DataTypeClass annotation = clazz.getAnnotation(DataTypeClass.class);
		String foreign = annotation == null ? "" : annotation.defineKeys();
		final String inside = columns.toString() + (keys.length() > 0 ? ",primary key ( " + keys + " )" : "") + (foreign.length() > 0 ? ", " + foreign : "");
		return "create table if not exists " + tableName + " ( " + inside + " ); ";

	}



	/**
	 * @param prefix
	 * @param clazz
	 * @return
	 */
	private ArrayList<Column> getAllFields(final String prefix, final ArrayList<Column> arrayList, final Class<?> clazz) {
		for (final Field f : clazz.getDeclaredFields())
			if (f.isAnnotationPresent(DataField.class)) {
				try {
					Column.getSqlType(f);
					arrayList.add(new Column(prefix, f));
				} catch (final IllegalArgumentException e) {
				}
			} else if (f.isAnnotationPresent(ComplexDataField.class)) {
				try {
					getAllFields(prefix.length() > 0 ? prefix + "_" + f.getName() : f.getName(), arrayList, f.getType());
				} catch (final IllegalArgumentException e) {
				}
			}
		return arrayList;
	}

	protected SQLiteDatabase database;
	protected ArrayList<Column> columns = new ArrayList<Column>();
	private final String tableName;
	protected Class<Item> clazz;


	public Table(final String tableName, final Class<Item> clazz) {
		this.clazz = clazz;
		if (!clazz.isAnnotationPresent(DataTypeClass.class))
			throw new IllegalArgumentException(clazz + " is not a DataTypeClass!");
		this.tableName = tableName;
		createCreateStatement(clazz);
	}

	public Table(final SQLiteDatabase database, final String tableName, final Class<Item> clazz) {
		this(tableName, clazz);
		create(database);

	}

	public Table(final Class<Item> clazz) {
		this(clazz.getSimpleName(), clazz);
	}

	public Table(final SQLiteDatabase database, final Class<Item> clazz) {
		this(database, clazz.getSimpleName(), clazz);
	}

	public void create(final SQLiteDatabase db) {
		database = db;
		database.execSQL("PRAGMA foreign_keys=ON;");
		database.execSQL(createCreateStatement(clazz));

	}

	public void create(final SQLiteDatabase... dbs) {
		for (final SQLiteDatabase db : dbs)
			create(db);
	}

	public void setDatabse(final SQLiteDatabase db) {
		database = db;
	}

	public long add(final Item a) {
		final ContentValues itemToValues = itemToValues(a, false);
		return database.insertOrThrow(tableName, columns.get(0).getName(), itemToValues);
	}

	public boolean insertIfNotExists(final Item i) {
		final Item item = get(i);
		if (item != null)
			return false;
		replace(i);
		return true;
	}

	public long replace(final Item a) {
		return database.replaceOrThrow(tableName, null, itemToValues(a, true));
	}

	private ContentValues itemToValues(final Item a, final boolean replace) {
		final ContentValues contentValues = new ContentValues();
		for (final Column c : columns) {
			if (c.isKey() && c.isAutoIncrement() && !replace)
				continue;
			final String type = c.getExtendedType();
			if (type.equals("long"))
				contentValues.put(c.getName(), (Long) c.get(a));
			else if (type.equals("int"))
				contentValues.put(c.getName(), (Integer) c.get(a));
			else if (type.equals("enum"))
				contentValues.put(c.getName(), ((Enum) c.get(a)).ordinal());
			else if (type.equals("boolean"))
				contentValues.put(c.getName(), (Boolean) c.get(a) ? 1 : 0);
			else if (type.equals("text"))
				contentValues.put(c.getName(), (String) c.get(a));
			else if (type.equals("real"))
				contentValues.put(c.getName(), Double.parseDouble(c.get(a).toString()));
			else if (type.equals("blob"))
				contentValues.put(c.getName(), (byte[]) c.get(a));
			else if (type.equals("image")) {
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				((Bitmap) c.get(a)).compress(Bitmap.CompressFormat.PNG, 100, bos);
				final byte[] bArray = bos.toByteArray();
				contentValues.put(c.getName(), bArray);
			}

		}

		return contentValues;
	}

	public void addAll(final ArrayList<Item> items) {
		for (final Item i : items)
			add(i);
	}

	public void remove(final Item i) {
		database.delete(tableName, getEqualityWhereClause(i), null);
	}

	public void remove(final String where) {
		database.delete(tableName, where, null);
	}

	public ArrayList<Item> getAll() {
		ArrayList<Item> items;
		final Cursor c = database.rawQuery("SELECT * FROM " + tableName, null);
		items = getAll(c);
		c.close();
		return items;
	}

	public ArrayList<Item> getAll(final String where) {
		ArrayList<Item> items;
		final Cursor c = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + where, null);
		items = getAll(c);
		c.close();
		return items;
	}

	public Handle getHandle(final String where) {
		return new HandleImpl(new Query() {
			@Override
			public Cursor query() {
				return database.rawQuery("SELECT * FROM " + tableName + " WHERE " + where, null);
			}
		}, where);
	}
	public interface Query {
		public Cursor query();
	}
	public interface HandleListener {
		public void invalidated();
	}
	public interface Handle<Item> {
		public Item get(int i);
		public int count();
		public Item get(int i,Item toBeFilled);
		public void close();
		public void invalidate();
		public void setListener(HandleListener listener);

	}
	public class HandleImpl implements Handle<Item> {
		private final Query query;
		private Cursor cursor;
		private HandleListener listener;

		public HandleImpl(final Query q, final String where) {
			query = q;
			cursor = query.query();
		}

		public Item get(int i) {
			if (!cursor.moveToPosition(i)) return null;
			try {
				return getItemFromCursor(cursor,clazz.newInstance());
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	public int count() {
		return cursor.getCount();
	}
		public Item get(int i,Item toBeFilled) {
			if (!cursor.moveToPosition(i)) return null;
			try {
				return getItemFromCursor(cursor,toBeFilled);
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}

		public void close() {
			cursor.close();
		}

		public void invalidate() {
			cursor = query.query();
			if (listener!=null)listener.invalidated();
		}
		public void setListener(HandleListener listener) {
			this.listener = listener;
		}


	}

	public ArrayList<Item> query(final String sqlQuery) {
		ArrayList<Item> items;
		final Cursor c = database.rawQuery(sqlQuery, null);
		items = getAll(c);
		c.close();
		return items;
	}

	public Item get(final String where, String... args) throws IllegalArgumentException {
		ArrayList<Item> items;
		final Cursor c = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + where, args);
		items = getAll(c);
		c.close();
		if (items.size() > 1)
			throw new IllegalArgumentException("query had more then 1 result");
		if (items.size() < 1)
			return null;
		return items.get(0);
	}

	protected ArrayList<Item> getAll(final Cursor c) {
		final ArrayList<Item> items = new ArrayList<Item>();
		if (c.moveToFirst()) {
			do {
				try {
					items.add(getItemFromCursor(c, clazz.newInstance()));
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			} while (c.moveToNext());
		}
		return items;
	}

	public Item getItemFromCursor(final Cursor c, final Item item) {
		for (final Column col : columns) {
			final String type = col.getExtendedType();
			if (type.equals("long"))
				col.set(item, c.getLong(c.getColumnIndexOrThrow(col.getName())));
			else if (type.equals("int"))
				col.set(item, c.getInt(c.getColumnIndexOrThrow(col.getName())));
			else if (type.equals("boolean"))
				col.set(item, c.getInt(c.getColumnIndexOrThrow(col.getName())) != 0);
			else if (type.equals("text"))
				col.set(item, c.getString(c.getColumnIndexOrThrow(col.getName())));
			else if (type.equals("real"))
				col.set(item, c.getFloat(c.getColumnIndexOrThrow(col.getName())));
			else if (type.equals("enum"))
				col.set(item, ((Class<? extends Enum>)col.getJavaType()).getEnumConstants()[c.getInt(c.getColumnIndexOrThrow(col.getName()))]);
			else {
				final byte[] blob = c.getBlob(c.getColumnIndexOrThrow(col.getName()));
				if (type.equals("blob"))
					col.set(item, blob);
				else if (type.equals("image"))
					col.set(item, BitmapFactory.decodeByteArray(blob, 0, blob.length));
			}
		}
		return item;
	}

	public String getEqualityWhereClause(final Item i) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (final Column c : columns) {
			if (c.isKey()) {
				stringBuilder.append(c.getName());
				stringBuilder.append('=');
				stringBuilder.append("'" + c.get(i) + "'");
				stringBuilder.append(" and ");
			}
		}
		final int length = stringBuilder.length();
		stringBuilder.delete(length - 4, length);
		return stringBuilder.toString();
	}

	public boolean contains(final Item i) {
		final String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + getEqualityWhereClause(i);
		final SQLiteStatement statement = database.compileStatement(sql);
		final long count = statement.simpleQueryForLong();
		return count > 0;
	}

	public Item get(final Item itemWithSameID) {
		Item item = null;
		final Cursor c = database.query(tableName, null, getEqualityWhereClause(itemWithSameID), null, null, null, null);
		if (c.moveToFirst())
			item = getItemFromCursor(c, itemWithSameID);
		c.close();
		return item;
	}

	public long size() {
		final String sql = "SELECT COUNT(*) FROM " + tableName;
		final SQLiteStatement statement = database.compileStatement(sql);
		final long count = statement.simpleQueryForLong();
		return count;
	}

	public long size(final String where) {
		final String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + where;
		final SQLiteStatement statement = database.compileStatement(sql);
		final long count = statement.simpleQueryForLong();
		return count;
	}

	public void clear() {
		database.delete(tableName, null, null);
	}

	/**
	 * if added return new row id else returns -1
	 *
	 * @param i
	 * @return
	 */
	public long update(final Item i) {
		if (database.update(tableName, itemToValues(i, true), getEqualityWhereClause(i), null) <= 0)
			return add(i);
		else
			return -1;
	}

	public String getTableName() {
		return tableName;
	}

	public ArrayList<Item> getAll(final String where, final Object... args) {
		ArrayList<Item> items;
		final Cursor c = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + where, Utils.toStrings(args));
		items = getAll(c);
		c.close();
		return items;
	}

	public Item get(final String where, final Object... args) throws IllegalArgumentException {
		ArrayList<Item> items;

		final Cursor c = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + where, Utils.toStrings(args));
		items = getAll(c);
		c.close();
		if (items.size() > 1)
			throw new IllegalArgumentException("query had more then 1 result");
		if (items.size() < 1)
			return null;
		return items.get(0);
	}

	public int removeAll(final String where) {
		return database.delete(tableName, where, null);
	}

	public void execute(String sql, Object... args) {
		database.execSQL(sql, args);
	}

	@Override
	public String toString() {
		return tableName;
	}
	public void drop(final SQLiteDatabase sqldb) {
		sqldb.execSQL("drop table IF EXISTS " + tableName);
	}
	public long count(String query, Object... args) {
		final SQLiteStatement s = database.compileStatement("select count(*) from (" + query + ") as toCount");
		s.bindAllArgsAsStrings(Utils.toStrings(args));
		return s.simpleQueryForLong();
	}

	public long queryForCount(String where) {
		final String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + where;
		final SQLiteStatement statement = database.compileStatement(sql);
		final long count = statement.simpleQueryForLong();
		return count;
	}
}
