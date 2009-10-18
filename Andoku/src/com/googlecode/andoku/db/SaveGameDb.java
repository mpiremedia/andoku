/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.googlecode.andoku.Constants;
import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;

public class SaveGameDb {
	private static final String TAG = SaveGameDb.class.getName();

	public static final String DATABASE_NAME = "save_games.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME = "games";

	public static final String ID = BaseColumns._ID;
	public static final String PUZZLE_ID = "pid";
	public static final String SOURCE = "source";
	public static final String NUMBER = "number";
	public static final String TYPE = "type";
	public static final String PUZZLE = "puzzle";
	public static final String TIMER = "timer";
	public static final String SOLVED = "solved";
	public static final String CREATED_DATE = "created";
	public static final String MODIFIED_DATE = "modified";

	private static final String[] COLUMNS_ID = new String[] { ID };
	private static final String[] COLUMNS_PUZZLE_ID = new String[] { PUZZLE_ID };
	private static final String[] COLUMNS_PUZZLE_TIMER = new String[] { PUZZLE, TIMER };
	private static final String[] COLUMNS_UNFINISHED_GAMES = new String[] { ID, PUZZLE_ID, TYPE,
			TIMER, CREATED_DATE, MODIFIED_DATE };
	private static final String[] COLUMNS_ALL_GAMES = COLUMNS_UNFINISHED_GAMES;
	private static final String[] COLUMNS_GAMES_BY_SOURCE = new String[] { NUMBER, SOLVED };

	// indexes to COLUMNS_UNFINISHED_GAMES
	public static final int UNFINISHED_COL_ID = 0;
	public static final int UNFINISHED_COL_PUZZLE_ID = 1;
	public static final int UNFINISHED_COL_TYPE = 2;
	public static final int UNFINISHED_COL_TIMER = 3;
	public static final int UNFINISHED_COL_CREATED_DATE = 4;
	public static final int UNFINISHED_COL_MODIFIED_DATE = 5;

	// indexes to COLUMNS_GAMES_BY_SOURCE
	public static final int GAMES_BY_SOURCE_COL_NUMBER = 0;
	public static final int GAMES_BY_SOURCE_COL_SOLVED = 1;

	private DatabaseHelper openHelper;

	public SaveGameDb(Context context) {
		if (Constants.LOG_V)
			Log.v(TAG, "SaveGameDb()");

		openHelper = new DatabaseHelper(context);
	}

	public void saveGame(String puzzleId, AndokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "saveGame(" + puzzleId + ")");

		long now = System.currentTimeMillis();

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			Cursor cursor = db.query(TABLE_NAME, COLUMNS_ID, PUZZLE_ID + "=?",
					new String[] { puzzleId }, null, null, null);

			long rowId = -1;
			if (cursor.moveToFirst()) {
				rowId = cursor.getLong(0);
			}

			cursor.close();

			ContentValues values = new ContentValues();
			values.put(PUZZLE, serialize(puzzle.saveToMemento()));
			values.put(TIMER, timer.getTime());
			values.put(SOLVED, puzzle.isSolved());
			values.put(MODIFIED_DATE, now);

			if (rowId == -1) {
				int idx = puzzleId.lastIndexOf(':');
				String source = puzzleId.substring(0, idx);
				int number = Integer.parseInt(puzzleId.substring(idx + 1));

				values.put(PUZZLE_ID, puzzleId);
				values.put(SOURCE, source);
				values.put(NUMBER, number);
				values.put(TYPE, puzzle.getPuzzleType().ordinal());
				values.put(CREATED_DATE, now);
				long insertedRowId = db.insert(TABLE_NAME, null, values);
				if (insertedRowId == -1)
					return;
			}
			else {
				int updated = db.update(TABLE_NAME, values, ID + "=?", new String[] { String
						.valueOf(rowId) });
				if (updated == 0)
					return;
			}

			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}

		db.close();
	}

	public boolean loadGame(String puzzleId, AndokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "loadGame(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor cursor = db.query(TABLE_NAME, COLUMNS_PUZZLE_TIMER, PUZZLE_ID + "=?",
				new String[] { puzzleId }, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return false;
		}

		Object memento = deserialize(cursor.getBlob(0));
		long time = cursor.getLong(1);
		cursor.close();

		if (!puzzle.restoreFromMemento(memento)) {
			Log.w(TAG, "Could not restore puzzle memento for " + puzzleId);
			return false;
		}

		timer.setTime(time);
		return true;
	}

	public void delete(String puzzleId) {
		if (Constants.LOG_V)
			Log.v(TAG, "delete(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_NAME, PUZZLE_ID + "=?", new String[] { puzzleId });

		db.close();
	}

	public void deleteAll() {
		if (Constants.LOG_V)
			Log.v(TAG, "deleteAll()");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_NAME, null, null);

		db.close();
	}

	public Cursor findAllGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findAllGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return db.query(TABLE_NAME, COLUMNS_ALL_GAMES, null, null, null, null, null);
	}

	public Cursor findUnfinishedGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findUnfinishedGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return db.query(TABLE_NAME, COLUMNS_UNFINISHED_GAMES, SOLVED + "=0", null, null, null,
				MODIFIED_DATE + " DESC");
	}

	public Cursor findGamesBySource(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "findGamesBySource(" + puzzleSourceId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return db.query(TABLE_NAME, COLUMNS_GAMES_BY_SOURCE, SOURCE + "=?",
				new String[] { puzzleSourceId }, null, null, NUMBER);
	}

	public GameStatistics getStatistics(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getStatistics(" + puzzleSourceId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor c = db.query(TABLE_NAME, new String[] { "COUNT(*)", "SUM(timer)", "MIN(timer)",
				"MAX(timer)" }, SOURCE + "=? AND " + SOLVED + "=1", new String[] { puzzleSourceId },
				null, null, null);
		try {
			c.moveToFirst();

			return new GameStatistics(c.getInt(0), c.getLong(1), c.getLong(2));
		}
		finally {
			c.close();
		}
	}

	public String puzzleIdByRowId(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "puzzleIdByRowId(" + rowId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor cursor = db.query(TABLE_NAME, COLUMNS_PUZZLE_ID, ID + "=?", new String[] { Long
				.toString(rowId) }, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		String puzzleId = cursor.getString(0);
		cursor.close();
		return puzzleId;
	}

	public void close() {
		if (Constants.LOG_V)
			Log.v(TAG, "close()");

		openHelper.close();
	}

	private byte[] serialize(Serializable memento) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(baos);
			oout.writeObject(memento);
			oout.close();
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Object deserialize(byte[] blob) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			ObjectInputStream oin = new ObjectInputStream(bais);
			Object object = oin.readObject();
			oin.close();
			return object;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY," + PUZZLE_ID
					+ " TEXT," + SOURCE + " TEXT," + NUMBER + " INTEGER," + TYPE + " INTEGER," + PUZZLE
					+ " BLOB," + TIMER + " INTEGER," + SOLVED + " BOOLEAN," + CREATED_DATE + " INTEGER,"
					+ MODIFIED_DATE + " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ "; which will destroy all old data!");

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}