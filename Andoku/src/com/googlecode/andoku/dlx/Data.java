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

package com.googlecode.andoku.dlx;

public class Data {
	protected Header column;

	protected Data up;
	protected Data down;
	protected Data left;
	protected Data right;

	private final Object payload;

	public Data(Object payload) {
		up = this;
		down = this;
		left = this;
		right = this;

		this.payload = payload;
	}

	public Header getColumn() {
		return column;
	}

	public Data getUp() {
		return up;
	}

	public Data getDown() {
		return down;
	}

	public Data getLeft() {
		return left;
	}

	public Data getRight() {
		return right;
	}

	public Object getPayload() {
		return payload;
	}
}
