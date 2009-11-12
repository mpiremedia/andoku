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

package com.googlecode.andoku;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.googlecode.andoku.model.PuzzleType;

class ColorTheme implements Theme {
	private final float borderStrokeWidth;

	private final Drawable background;
	private final int puzzleBackgroundColor;

	private final int nameTextColor;
	private final int difficultyTextColor;
	private final int sourceTextColor;
	private final int timerTextColor;

	private final Paint gridPaint;
	private final Paint regionBorderPaint;
	private final Paint extraRegionPaint;
	private final Paint valuePaint;
	private final Paint digitPaint;
	private final Paint cluePaint;
	private final Paint previewCluePaint;
	private final Paint errorPaint;
	private final Paint markedCellPaint;
	private final Paint markedCluePaint;
	private final Paint outerBorderPaint;

	private final float outerBorderRadius;

	private final boolean drawAreaColors;
	private final boolean drawAreaColorsIfExtra;
	private final int[] areaColors2;
	private final int[] areaColors3;
	private final int[] areaColors4;

	private final Drawable congratsDrawable;
	private final Drawable pausedDrawable;

	public static final class Builder {
		private final Resources resources;

		public int backgroudColor = 0xffeeeeee;
		public int puzzleBackgroundColor = 0xffffffff;
		public int nameTextColor = 0xff222222;
		public int difficultyTextColor = 0xff222222;
		public int sourceTextColor = 0xff222222;
		public int timerTextColor = 0xff222222;
		public int gridColor = 0x40000000;
		public int borderColor = 0xff000000;
		public int extraRegionColor = 0xcd99abff;
		public int valueColor = 0xff006000;
		public int clueColor = 0xff000000;
		public int errorColor = 0xffff0000;
		public int markedCellColor = 0x7000ff00;
		public int markedClueColor = 0x70ff0000;
		public boolean drawAreaColors = true;
		public boolean drawAreaColorsIfExtra = false;
		public int[] areaColors2 = { 0xffffffff, 0xffe0e0e0 };
		public int[] areaColors3 = { 0xffffd9d9, 0xffd9ffd9, 0xffd9d9ff }; // triad
		public int[] areaColors4 = { 0xffffffd9, 0xffd9ffec, 0xffd9d9ff, 0xffffd9ec }; // tetrad

		public Builder(Resources resources) {
			this.resources = resources;
		}

		public Theme build() {
			return new ColorTheme(this);
		}
	}

	private ColorTheme(Builder builder) {
		Resources resources = builder.resources;

		float displayDensity = resources.getDisplayMetrics().density;
		float gridWidth = Math.max(1, displayDensity);

		borderStrokeWidth = Math.max(2, 3 * displayDensity);

		background = new ColorDrawable(builder.backgroudColor);

		puzzleBackgroundColor = builder.puzzleBackgroundColor;

		nameTextColor = builder.nameTextColor;
		difficultyTextColor = builder.difficultyTextColor;
		sourceTextColor = builder.sourceTextColor;
		timerTextColor = builder.timerTextColor;

		gridPaint = new Paint();
		gridPaint.setStrokeWidth(gridWidth);
		gridPaint.setAntiAlias(false);
		gridPaint.setColor(builder.gridColor);
		gridPaint.setStrokeCap(Cap.BUTT);
		// gridPaint.setShadowLayer(1, 1, 1, 0xff000000);

		regionBorderPaint = new Paint();
		regionBorderPaint.setStrokeWidth(borderStrokeWidth);
		regionBorderPaint.setAntiAlias(false);
		regionBorderPaint.setColor(builder.borderColor);
		regionBorderPaint.setStrokeCap(Cap.ROUND);

		extraRegionPaint = new Paint();
		extraRegionPaint.setAntiAlias(false);
		extraRegionPaint.setColor(builder.extraRegionColor);

		Typeface typeface = Typeface.SANS_SERIF;
		valuePaint = new Paint();
		valuePaint.setAntiAlias(true);
		valuePaint.setColor(builder.valueColor);
		valuePaint.setTextAlign(Align.CENTER);
		valuePaint.setTypeface(typeface);

		Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
		digitPaint = new Paint();
		digitPaint.setAntiAlias(true);
		digitPaint.setColor(builder.valueColor);
		digitPaint.setTextAlign(Align.CENTER);
		digitPaint.setTypeface(boldTypeface);

		cluePaint = new Paint();
		cluePaint.setAntiAlias(true);
		cluePaint.setColor(builder.clueColor);
		cluePaint.setTextAlign(Align.CENTER);
		cluePaint.setTypeface(boldTypeface);

		previewCluePaint = new Paint(cluePaint);
		previewCluePaint.setAlpha(128);

		errorPaint = new Paint();
		errorPaint.setStrokeWidth(borderStrokeWidth);
		errorPaint.setAntiAlias(true);
		errorPaint.setColor(builder.errorColor);
		errorPaint.setStyle(Style.STROKE);
		errorPaint.setStrokeCap(Cap.BUTT);

		markedCellPaint = new Paint();
		markedCellPaint.setAntiAlias(false);
		markedCellPaint.setColor(builder.markedCellColor);

		markedCluePaint = new Paint();
		markedCluePaint.setAntiAlias(false);
		markedCluePaint.setColor(builder.markedClueColor);

		outerBorderPaint = new Paint();
		outerBorderPaint.setStrokeWidth(Math.round(borderStrokeWidth));
		outerBorderPaint.setAntiAlias(true);
		outerBorderPaint.setColor(builder.borderColor);
		outerBorderPaint.setStyle(Style.STROKE);

		outerBorderRadius = 6 * displayDensity;

		drawAreaColors = builder.drawAreaColors;
		drawAreaColorsIfExtra = builder.drawAreaColorsIfExtra;
		areaColors2 = copy(builder.areaColors2, 2);
		areaColors3 = copy(builder.areaColors3, 3);
		areaColors4 = copy(builder.areaColors4, 4);

		congratsDrawable = resources.getDrawable(R.drawable.congrats);
		congratsDrawable.setAlpha(144);

		pausedDrawable = resources.getDrawable(R.drawable.paused);
		pausedDrawable.setAlpha(144);
	}

	private int[] copy(int[] colors, int length) {
		if (colors.length != length)
			throw new IllegalArgumentException();

		int[] copy = new int[length];
		System.arraycopy(colors, 0, copy, 0, length);
		return copy;
	}

	public char getSymbol(int value) {
		return "123456789".charAt(value);
	}

	public int[] getPuzzlePadding() {
		final int padding = Math.round(borderStrokeWidth);
		return new int[] { padding, padding, padding, padding };
	}

	public Drawable getBackground() {
		return background;
	}

	public int getPuzzleBackgroundColor() {
		return puzzleBackgroundColor;
	}

	public int getNameTextColor() {
		return nameTextColor;
	}

	public int getDifficultyTextColor() {
		return difficultyTextColor;
	}

	public int getSourceTextColor() {
		return sourceTextColor;
	}

	public int getTimerTextColor() {
		return timerTextColor;
	}

	public Paint getGridPaint() {
		return gridPaint;
	}

	public Paint getRegionBorderPaint() {
		return regionBorderPaint;
	}

	public Paint getExtraRegionPaint() {
		return extraRegionPaint;
	}

	public Paint getValuePaint() {
		return valuePaint;
	}

	public Paint getDigitPaint() {
		return digitPaint;
	}

	public Paint getCluePaint(boolean preview) {
		return preview ? previewCluePaint : cluePaint;
	}

	public Paint getErrorPaint() {
		return errorPaint;
	}

	public Paint getMarkedCellPaint() {
		return markedCellPaint;
	}

	public Paint getMarkedCluePaint() {
		return markedCluePaint;
	}

	public Paint getOuterBorderPaint() {
		return outerBorderPaint;
	}

	public float getOuterBorderRadius() {
		return outerBorderRadius;
	}

	public boolean isDrawAreaColors(PuzzleType puzzleType) {
		if (drawAreaColors) {
			boolean hasExtraRegions = puzzleType.isHyper() || puzzleType.isX();
			if (hasExtraRegions)
				return drawAreaColorsIfExtra;
			else
				return true;
		}
		else {
			return false;
		}
	}

	public int getAreaColor(int colorNumber, int numberOfColors) {
		switch (numberOfColors) {
			case 2:
				return areaColors2[colorNumber];
			case 3:
				return areaColors3[colorNumber];
			case 4:
				return areaColors4[colorNumber];
			default:
				return areaColors4[colorNumber % 4];
		}
	}

	public Drawable getCongratsDrawable() {
		return congratsDrawable;
	}

	public Drawable getPausedDrawable() {
		return pausedDrawable;
	}
}
