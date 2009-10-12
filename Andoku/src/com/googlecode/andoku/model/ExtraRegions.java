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

package com.googlecode.andoku.model;

import java.util.ArrayList;
import java.util.List;

public class ExtraRegions {
	private static final ExtraRegion[] NONE = {};

	private ExtraRegions() {
	}

	public static ExtraRegion[] none() {
		return NONE;
	}

	public static ExtraRegion[] x(int size) {
		List<Position> diag1 = new ArrayList<Position>();
		List<Position> diag2 = new ArrayList<Position>();

		for (int i = 0; i < size; i++) {
			diag1.add(new Position(i, i));
			diag2.add(new Position(i, size - 1 - i));
		}

		return new ExtraRegion[] { new ExtraRegion(diag1), new ExtraRegion(diag2) };
	}

	public static int hyperCount(int size) {
		return size == 9 ? 4 : 0;
	}

	public static ExtraRegion[] hyper(int size) {
		if (size != 9)
			throw new IllegalArgumentException("Hyper restricted to 9x9 for now..");

		return new ExtraRegion[] { square(1, 1), square(5, 1), square(1, 5), square(5, 5) };
	}

	private static ExtraRegion square(int rowOffset, int colOffset) {
		List<Position> positions = new ArrayList<Position>();

		for (int row = rowOffset; row < rowOffset + 3; row++) {
			for (int col = colOffset; col < colOffset + 3; col++) {
				positions.add(new Position(row, col));
			}
		}

		return new ExtraRegion(positions);
	}
}