/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
 *
 * This file is part of Andoku.
 *
 * Andoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Andoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku.db;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.googlecode.andoku.TickListener;
import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.source.PuzzleSourceIds;
import com.googlecode.andoku.util.MockPuzzleSource;

public class AndokuDatabaseSaveGameTest extends AndroidTestCase {
	private AndokuDatabase db;

	@Override
	protected void setUp() throws Exception {
		db = new AndokuDatabase(getContext());
		db.resetAll();
	}

	@Override
	protected void tearDown() throws Exception {
		db.close();
	}

	public void testSaveAndLoadGame() throws Exception {
		int number = 0;
		AndokuPuzzle puzzleSave = MockPuzzleSource.createPuzzle(number);
		puzzleSave.setValues(0, 1, ValueSet.of(7));
		puzzleSave.setValues(0, 2, ValueSet.of(3));

		TickTimer timerSave = new TickTimer(new MockTickListener());
		timerSave.setTime(700);

		PuzzleId puzzleId = new PuzzleId(MockPuzzleSource.SOURCE_ID, number);
		db.saveGame(puzzleId, puzzleSave, timerSave);

		AndokuPuzzle puzzleLoad = MockPuzzleSource.createPuzzle(number);
		TickTimer timerLoad = new TickTimer(new MockTickListener());

		assertTrue(db.loadGame(puzzleId, puzzleLoad, timerLoad));
		assertEquals(ValueSet.of(7), puzzleLoad.getValues(0, 1));
		assertEquals(ValueSet.of(3), puzzleLoad.getValues(0, 2));
		assertEquals(700, timerLoad.getTime());
	}

	public void testDeleteGame() throws Exception {
		int number = 0;
		AndokuPuzzle puzzle = MockPuzzleSource.createPuzzle(number);
		TickTimer timer = new TickTimer(new MockTickListener());

		PuzzleId puzzleId = new PuzzleId(MockPuzzleSource.SOURCE_ID, number);
		db.saveGame(puzzleId, puzzle, timer);
		assertTrue(db.loadGame(puzzleId, puzzle, timer));

		db.delete(puzzleId);
		assertFalse(db.loadGame(puzzleId, puzzle, timer));
	}

	public void testDeleteAllGame() throws Exception {
		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = MockPuzzleSource.createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		final String source1 = "mock:1";
		final String source2 = "mock:2";
		db.saveGame(new PuzzleId(source1, 1), puzzle1, timer1);
		db.saveGame(new PuzzleId(source1, 2), puzzle2, timer2);
		db.saveGame(new PuzzleId(source2, 3), puzzle3, timer3);

		assertEquals(2, countGames(source1));
		assertEquals(1, countGames(source2));

		db.deleteAll(source1);

		assertEquals(0, countGames(source1));
		assertEquals(1, countGames(source2));

		db.deleteAll(source2);

		assertEquals(0, countGames(source1));
		assertEquals(0, countGames(source2));
	}

	private int countGames(String sourceId) {
		Cursor cursor = db.findGamesBySource(sourceId);
		try {
			return cursor.getCount();
		}
		finally {
			cursor.close();
		}
	}

	public void testFindAllGames() throws Exception {
		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = MockPuzzleSource.createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		long t1 = System.currentTimeMillis();
		db.saveGame(new PuzzleId("mock:17", 1), puzzle1, timer1);
		long t2 = System.currentTimeMillis();
		db.saveGame(new PuzzleId("mock:16", 2), puzzle2, timer2);
		long t3 = System.currentTimeMillis();
		db.saveGame(new PuzzleId("mock:17", 3), puzzle3, timer3);
		long t4 = System.currentTimeMillis();

		Cursor cursor = db.findAllGames();
		// cursor: ID, SOURCE, NUMBER, TYPE, TIMER, CREATED_DATE, MODIFIED_DATE

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(1, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(puzzle1.getPuzzleType().ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(timer1.getTime(), cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) >= t1);
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) < t2);
		assertEquals(cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE), cursor
				.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals("mock:16", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(2, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(puzzle2.getPuzzleType().ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(timer2.getTime(), cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) >= t2);
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) < t3);
		assertEquals(cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE), cursor
				.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(3, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(puzzle3.getPuzzleType().ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(timer3.getTime(), cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) >= t3);
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) < t4);
		assertEquals(cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE), cursor
				.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));

		assertFalse(cursor.moveToNext());

		cursor.close();
	}

	public void testFindUnfinishedGames() throws Exception {
		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = MockPuzzleSource.createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		long t1 = System.currentTimeMillis();
		db.saveGame(new PuzzleId("mock:17", 1), puzzle1, timer1);
		long t2 = System.currentTimeMillis();
		db.saveGame(new PuzzleId("mock:16", 2), puzzle2, timer2);
		long t3 = System.currentTimeMillis();
		db.saveGame(new PuzzleId("mock:17", 3), puzzle3, timer3);
		long t4 = System.currentTimeMillis();

		Cursor cursor = db.findGamesInProgress();
		// cursor: ID, SOURCE, NUMBER, TYPE, TIMER, CREATED_DATE, MODIFIED_DATE

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(3, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(puzzle3.getPuzzleType().ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(timer3.getTime(), cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) >= t3);
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) < t4);
		assertEquals(cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE), cursor
				.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(1, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(puzzle1.getPuzzleType().ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(timer1.getTime(), cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) >= t1);
		assertTrue(cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE) < t2);
		assertEquals(cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE), cursor
				.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));

		assertFalse(cursor.moveToNext());

		cursor.close();
	}

	public void testFindGamesBySource() throws Exception {
		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = MockPuzzleSource.createSolvedPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		db.saveGame(new PuzzleId("mock:17", 1), puzzle1, timer1);
		db.saveGame(new PuzzleId("mock:16", 2), puzzle2, timer2);
		db.saveGame(new PuzzleId("mock:17", 3), puzzle3, timer3);

		Cursor cursor = db.findGamesBySource("mock:16");
		// cursor: NUMBER, SOLVED

		assertTrue(cursor.moveToNext());
		assertEquals(2, cursor.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_NUMBER));
		assertEquals(1, cursor.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_SOLVED));

		assertFalse(cursor.moveToNext());

		cursor.close();

		cursor = db.findGamesBySource("mock:17");

		assertTrue(cursor.moveToNext());
		assertEquals(1, cursor.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_NUMBER));
		assertEquals(0, cursor.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_SOLVED));

		assertTrue(cursor.moveToNext());
		assertEquals(3, cursor.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_NUMBER));
		assertEquals(1, cursor.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_SOLVED));

		assertFalse(cursor.moveToNext());

		cursor.close();
	}

	public void testGetStatistics() throws Exception {
		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = MockPuzzleSource.createSolvedPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		db.saveGame(new PuzzleId("mock:17", 1), puzzle1, timer1);
		db.saveGame(new PuzzleId("mock:17", 2), puzzle2, timer2);
		db.saveGame(new PuzzleId("mock:17", 3), puzzle3, timer3);

		GameStatistics statistics = db.getStatistics("mock:17");
		assertEquals(2, statistics.numGamesSolved);
		assertEquals(800, statistics.minTime);
		assertEquals(800 + 900, statistics.sumTime);
		assertEquals((800 + 900) / 2, statistics.getAverageTime());
	}

	public void testGetPuzzleIdByRowId() throws Exception {
		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		AndokuPuzzle puzzle3 = MockPuzzleSource.createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());

		db.saveGame(new PuzzleId("mock:17", 1), puzzle1, timer1);
		db.saveGame(new PuzzleId("mock:16", 2), puzzle2, timer2);
		db.saveGame(new PuzzleId("mock:17", 3), puzzle3, timer3);

		Cursor cursor = db.findAllGames();
		assertTrue(cursor.moveToNext());
		long id1 = cursor.getLong(0);
		assertTrue(cursor.moveToNext());
		long id2 = cursor.getLong(0);
		assertTrue(cursor.moveToNext());
		long id3 = cursor.getLong(0);
		assertFalse(cursor.moveToNext());
		cursor.close();

		assertEquals(new PuzzleId("mock:17", 1), db.puzzleIdByRowId(id1));
		assertEquals(new PuzzleId("mock:16", 2), db.puzzleIdByRowId(id2));
		assertEquals(new PuzzleId("mock:17", 3), db.puzzleIdByRowId(id3));
	}

	public void testDeleteFolderDeletesSavedGames() throws Exception {
		long folderId1 = db.createFolder("folder");
		String sourceId1 = PuzzleSourceIds.forDbFolder(folderId1);
		long folderId2 = db.createFolder(folderId1, "folder");
		String sourceId2 = PuzzleSourceIds.forDbFolder(folderId2);

		AndokuPuzzle puzzle1 = MockPuzzleSource.createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		AndokuPuzzle puzzle2 = MockPuzzleSource.createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());

		db.saveGame(new PuzzleId(sourceId1, 1), puzzle1, timer1);
		db.saveGame(new PuzzleId(sourceId2, 2), puzzle2, timer2);

		assertTrue(db.loadGame(new PuzzleId(sourceId1, 1), puzzle1, timer1));
		assertTrue(db.loadGame(new PuzzleId(sourceId2, 2), puzzle1, timer1));

		db.deleteFolder(folderId1);

		assertFalse(db.loadGame(new PuzzleId(sourceId1, 1), puzzle1, timer1));
		assertFalse(db.loadGame(new PuzzleId(sourceId2, 2), puzzle1, timer1));
	}

	private static final class MockTickListener implements TickListener {
		public void onTick(long time) {
		}
	}
}
