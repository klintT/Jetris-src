package net.solver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.sourceforge.jetris.Figure;
import net.sourceforge.jetris.TetrisGrid;

public class JetrisSolver {

	private JPanel[][] cells;
	private Figure f;
	private TetrisGrid tg;
	private int[] gridOffsets;

	public JetrisSolver() {
		gridOffsets = new int[2];
		gridOffsets[0] = 4;
		gridOffsets[1] = 0;
	}

	public void resetOffsets() {
		gridOffsets[0] = 4;
		gridOffsets[1] = 0;
	}

	public int[] getGridOffsets() {
		return gridOffsets;
	}

	public RestPoint findBestMoveSequence(Figure f, TetrisGrid tg, JPanel[][] cells) {

		this.cells = cells;
		this.f = f;
		this.tg = tg;

		RestPoint point = new RestPoint();
		List<RestPoint> points = new ArrayList<RestPoint>();
		int loops;

		if (f.getGridVal() == 2 || f.getGridVal() == 4) {
			loops = 4;
		} else if (f.getGridVal() == 3 || f.getGridVal() == 5) {
			loops = 3;
		} else if (f.getGridVal() == 6 || f.getGridVal() == 7 || f.getGridVal() == 1) {
			loops = 2;
		} else {
			loops = 1;
		}

		for (int t = 0; t < loops; t++) {
			for (int x = 9; x >= 0; x--) {
				for (int y = 19; y >= 0; y--) {
					if (tg.isNextMoveValid(f, x, y)) {
						RestPoint rp = new RestPoint();
						rp.x = x;
						rp.y = y;
						rp.transform = t;
						points.add(rp);
						break;
					}
				}
			}
			f.rotationRight();
		}

		int currentRotation = 0;
		int bestTransform = 0, bestX = 0, bestY = 0;
		double bestRating = -1;
		for (RestPoint rp : points) {
			int loop = rp.transform - currentRotation;
			while (loop != 0) {
				f.rotationRight();
				currentRotation++;
				loop = rp.transform - currentRotation;
			}

			if (tg.isNextMoveValid(f, rp.x, rp.y)) {
				/*
				 * changeOffsets(rp.x, rp.y); tg.addFiguretoGrid(f);
				 * System.out.println(tg.toString());
				 * System.out.println("----------------------------------");
				 * tg.removeFigureFromGrid(f); bestX = rp.x; bestY = rp.y;
				 */
				if (pointRating(rp) > bestRating) {
					bestX = rp.x;
					bestY = rp.y;
					bestTransform = rp.transform;
					bestRating = pointRating(rp);
				}
			}

		}

		// Rotate Back to 0
		f.rotationRight();
		currentRotation = 0;

		// Loop to the best Transform
		int loop = bestTransform - currentRotation;
		while (loop != 0) {
			f.rotationRight();
			currentRotation++;
			loop = bestTransform - currentRotation;
		}

		changeOffsets(bestX, bestY);
		tg.addFiguretoGrid(f);

		gridOffsets[0] = bestX;
		gridOffsets[1] = bestY;

		return point;

	}

	private boolean isAtBottom(RestPoint rp) {
		if (!tg.isNextMoveValid(f, rp.x, rp.y + 1)) {
			return true;
		}
		return false;
	}

	private double pointRating(RestPoint rp) {
		double lineRating = getLine(rp) * Weights.LINE_WEIGHT;
		double lineCompleteRating = getLineComplete(rp) * Weights.LINE_COMPLETE_WEIGHT;
		double heightRating = getHeight(rp) * Weights.HEIGHT_WEIGHT;

		return lineRating + lineCompleteRating + heightRating;
	}

	private int getHeight(RestPoint rp) {
		int height = 0;

		for (int j = 0; j < f.arrX.length; j++) {
			int tempHeight = f.arrY[j];
			if (height > tempHeight) {
				height = tempHeight;
			}
		}

		if (height == 1) {
			return 20;
		} else if (height == 2) {
			return 10;
		} else if (height == 3) {
			return 0;
		}
		return 0;
	}

	private LinkedList<int[]> cloneList(LinkedList<int[]> pList) {
		LinkedList<int[]> cLines = new LinkedList<int[]>();

		int lines = 0;
		for (Iterator iter = pList.iterator(); iter.hasNext();) {
			int[] el = (int[]) iter.next();
			cLines.add(el);
		}

		return cLines;
	}

	private int getLineComplete(RestPoint rp) {

		// Create a clone of the current lines
		LinkedList<int[]> copyLines = cloneList(this.tg.gLines);

		// Adds our figure to the grid at the point rp
		this.tg.addFiguretoGrid(rp, f, copyLines);

		// Get the lines destroyed
		int lines = this.tg.countLinesEliminated(copyLines);

		// Removes our figure to the grid at the point rp
		this.tg.removeFigureFromGrid(rp, f, copyLines);

		// Should we reward multiple lines? Does it really matter if the bot's
		// end goal is to go as long as possible?
		// But that is almost like speculating that this should be rated low.
		// The real question is "are multiple lines
		// more valuable than a single line?". I can't really say. Maybe we
		// should give them values and let Jet find out.
		// The bot is now Jet.

		if (lines > 0) {
			// Here we return 20 if the line is complete to give this field an
			// even
			// best value to the height field
			// Look up what doing that is called
			return 20;
		} else {
			return 0;
		}
	}

	private int getLine(RestPoint rp) {
		int line = 0;

		for (int j = 0; j < f.arrX.length; j++) {
			int tempLine = f.arrY[j] + rp.y;
			if (line < tempLine) {
				line = tempLine;
			}
		}

		return line;
	}

	private void changeOffsets(final int x, final int y) {
		f.offsetXLast = f.offsetX;
		f.offsetYLast = f.offsetY;

		f.offsetX = x;
		f.offsetY = y;
	}

	private void paintTG() {
		int i = 0;
		Color c;
		for (int[] arr : tg.gLines) {
			for (int j = 0; j < arr.length; j++) {
				if (arr[j] != 0) {
					switch (arr[j]) {
					case Figure.I:
						c = Figure.COL_I;
						break;
					case Figure.T:
						c = Figure.COL_T;
						break;
					case Figure.O:
						c = Figure.COL_O;
						break;
					case Figure.J:
						c = Figure.COL_J;
						break;
					case Figure.L:
						c = Figure.COL_L;
						break;
					case Figure.S:
						c = Figure.COL_S;
						break;
					default:
						c = Figure.COL_Z;
						break;
					}
					cells[i][j].setBackground(c);
					cells[i][j].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				} else {
					cells[i][j].setBackground(Color.WHITE);
					cells[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
				}
			}
			i++;
		}
	}

	private void clearOldPosition() {
		for (int j = 0; j < 4; j++) {
			cells[f.arrY[j] + f.offsetYLast][f.arrX[j] + f.offsetXLast].setBackground(Color.WHITE);
			cells[f.arrY[j] + f.offsetYLast][f.arrX[j] + f.offsetXLast].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		}
	}

	private void paintNewPosition() {
		for (int j = 0; j < 4; j++) {
			cells[f.arrY[j] + f.offsetY][f.arrX[j] + f.offsetX].setBackground(f.getGolor());
			cells[f.arrY[j] + f.offsetY][f.arrX[j] + f.offsetX].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		}
	}
}
