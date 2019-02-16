// Board.java
package edu.stanford.cs108.tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
 */
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean[][] bGrid;
	private boolean DEBUG = false;
	boolean committed;

	private int maxHeight;
	private int bMaxHeight;
	private int[] widths;
	private int[] bWidths;
	private int[] heights;
	private int[] bHeights;


	// Here a few trivial methods are provided:

	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		this.maxHeight = 0;
		this.bMaxHeight = 0;

		widths = new int[height];
		Arrays.fill(widths, 0);
		heights = new int[width];
		Arrays.fill(heights, 0);

		bWidths = new int[height];
		bHeights = new int[width];

		grid = new boolean[height][width];
		bGrid = new boolean[height][width];
		committed = true;



		for (int i = 0; i < height; i++) {
			Arrays.fill(grid[i], false);
			Arrays.fill(bGrid[i], false);
		}

	}


	/**
	 Returns the width of the board in blocks.
	 */
	public int getWidth() {
		return width;
	}


	/**
	 Returns the height of the board in blocks.
	 */
	public int getHeight() {
		return height;
	}


	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	 */
	public int getMaxHeight() {
		return maxHeight;
	}


	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	 */
	public void sanityCheck() {
		if (DEBUG) {

			int mHeight = 0;
			int[] cHeights = new int[width];
			Arrays.fill(cHeights, 0);
			for (int i = 0; i < height; i++) {
				int cWidth = 0;
				for (int j = 0; j < width; j++) {
					if (grid[i][j]) {
						//System.out.println("i is " + i + " and j is " + j);
						cWidth++;
						if (i + 1 > cHeights[j]) {
							cHeights[j] = i + 1;
							//System.out.println("i is " + i);
						}
						if (i + 1 > mHeight) mHeight = i + 1;

					}
				}
				//System.out.println(this.toString());
				//System.out.println("cWidth is " + cWidth + " but widths[" + i + "] is " + widths[i]);
				if (cWidth != widths[i]) {
					System.out.println(this.toString());
					throw new RuntimeException("widths array at " + i + " not consistent. " +
							"widths[" + i + "] is " + widths[i] + " and cWidth is " + cWidth);
				}

			}

			//System.out.println(this.toString());
			for (int i = 0 ; i < heights.length; i++) {
				//System.out.println("cHeight[" + i + "] = " + cHeights[i] + " and heights[" + i + "] is " + heights[i]);

			}
			//System.out.println("mHeight is " + mHeight + " and maxHeight is + " + maxHeight);

			System.arraycopy(cHeights, 0, heights, 0, heights.length);

			if (mHeight != maxHeight) throw new RuntimeException("maxHeight not consistent");
			if (!Arrays.equals(cHeights, heights)) throw new RuntimeException("heights array not consistent");

		}
	}

	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.

	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		int finalHeight = 0;
		int[] skirt = piece.getSkirt();
		for (int i = 0; i < skirt.length; i++) {
			int height = getColumnHeight(x + i);
			int curr = height - skirt[i];
			if (curr > finalHeight) finalHeight = curr;
		}
		return finalHeight;
	}


	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		return heights[x];
	}


	/**
	 Returns the number of filled blocks in
	 the given row.
	 */
	public int getRowWidth(int y) {
//		int rWidth = 0;
//		for(int i = 0; i < width; i++) {
//			if (grid[y][width]) rWidth++;
//		}
//		 return rWidth;
		return widths[y];
	}


	/**
	 Returns true if the given indices are in in-bounds
	 with respect to the board area, and false otherwise.
	 */
	private boolean inBounds(int x, int y) {
		return (x >= 0 && y >= 0 && x < height && y < width);
	}

	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	 */
	public boolean getGrid(int x, int y) {
		if (!inBounds(y, x)) return true;

		return grid[y][x];
	}


	private void backUpData() {
		for (int i = 0; i < height; i++) {
			System.arraycopy(grid[i], 0, bGrid[i], 0, width);
		}
		System.arraycopy(widths, 0, bWidths, 0, height);
		System.arraycopy(heights, 0, bHeights, 0, width);
		bMaxHeight = maxHeight;
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.

	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
		committed = false;

		backUpData();


		int result = PLACE_OK;

		//System.out.println("Height is " + height + " and width is " + width);

		for (TPoint p: piece.getBody()) {
			int curr_x = x + p.x;
			int curr_y = y + p.y;
			if (!inBounds(curr_y, curr_x)) {
				//System.out.println("Curr x is " + curr_x + " and curr_y is " + curr_y);
				sanityCheck();
				return PLACE_OUT_BOUNDS;
			}
			if (grid[curr_y][curr_x]) return PLACE_BAD; //reversed the x and y here
			grid[curr_y][curr_x] = true;
			widths[curr_y]++;
			if (curr_y + 1 > heights[curr_x]) heights[curr_x] = curr_y + 1;
			if (heights[curr_x] > maxHeight) maxHeight = heights[curr_x];
		}

		for (int i: widths) {
			if (i == width) {
				sanityCheck();
				return PLACE_ROW_FILLED;
			}
		}


		sanityCheck();
		return result;
	}

	private boolean rowIsEmpty(int row) {
		//return widths[row] == 0;
		for (int i = 0; i < width; i++) {
			if (grid[row][i]) {
				return false;
			}
		}
		return true;
	}

	private void collapseGrid(int numRows, int numCols) {	//change maxHeight and widths maybe isn't working

		for (int i = 0; i < maxHeight; i++) {
			//System.out.println(this.toString());
			if (rowIsEmpty(i) && i < numRows - 1) {
				//System.out.println("I IS EQUAL TO " + i);
				int offsetToNext = 1;
				while (i + offsetToNext < numRows - 1 && rowIsEmpty(i + offsetToNext)) {
					offsetToNext++;
				}
				for (int j = 0; j < numCols; j++) {
					grid[i][j] = grid[i + offsetToNext][j];
					if (grid[i][j]) {
						heights[j] = i + 1;
						//System.out.println("The i here is " + i + " and the j here is " + j + ". heights[" + j + "] = " + heights[j] + ". i + 1 = " + (int)(i + 1) );

					}
					grid[i + offsetToNext][j] = false;
				}
				//System.out.println("setting widths[" + i + "] = " + widths[i + offsetToNext]);
				widths[i] = widths[i + offsetToNext]; //move the offsets down THIS IS THE PART THAT"S STILL NOT FUCKING WORKING

			}
		}

	}

	private void removeRow(int row, int rowsCleared) {
//		for (int i = 0; i < widths.length; i++) {
//			System.out.println("Precollapse  " + widths[i]);
//		}

		for (int i = 0; i < width; i++) {
			grid[row][i] = false;
			heights[i]--; //preemptively decrease each of their heights before collapsion

			int counter = 1;
			while (counter <= row) {
				if (!grid[row - counter][i]) {
					heights[i]--;
					counter++;
				} else {
					//System.out.println("counter is " + counter);
					heights[i] = row - counter;
					break;
				}
			}

		}



		//widths[row] = 0;
		for (int i = row; i < widths.length; i++) {
			if (i - rowsCleared + 1 >= widths.length - 1) {
				widths[i] = 0;
			} else {
				widths[i - rowsCleared] = widths[i - rowsCleared + 1];
			}
		}

//		for (int i = 0; i < widths.length; i++) {
//			System.out.println("Postcollapse " + widths[i]);
//		}
		maxHeight--; //preemptively decrease maxHeight
	}

	private void updateHeights() {
		int[] cHeights = new int[heights.length];
		int[] cWidths = new int[widths.length];
		Arrays.fill(cHeights, 0);
		Arrays.fill(cWidths, 0);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (grid[i][j]) {
					cWidths[i]++;
					if (i + 1 > cHeights[j]) {
						cHeights[j] = i + 1;
					}
				}
			}
		}
		System.arraycopy(cHeights, 0, heights, 0, heights.length);
		System.arraycopy(cWidths, 0, widths, 0, widths.length);

	}

	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		if (committed) backUpData();
		committed = false;
		int rowsCleared = 0;
		int numRows = grid.length;
		int numCols = grid[0].length;
		Set<Integer> rowsToRemove = new HashSet<Integer>();

		for (int i = 0; i < width; i++) {
			if (widths[i] == width) rowsToRemove.add(i);
		}

		//System.out.println(rowsToRemove.toString());

		for (int row: rowsToRemove) {
			removeRow(row, rowsCleared);
			rowsCleared++;
		}

		collapseGrid(numRows, numCols);
		updateHeights();
		sanityCheck();
		return rowsCleared;
	}



	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	 */
	public void undo() {
		if (committed) return;
		committed = true;
		for (int i = 0; i < height; i++) {
			System.arraycopy(bGrid[i], 0, grid[i], 0, width);
		}
		System.arraycopy(bWidths, 0, widths, 0, height);
		System.arraycopy(bHeights, 0, heights, 0, width);
		maxHeight = bMaxHeight;
	}


	/**
	 Puts the board in the committed state.
	 */
	public void commit() {
		committed = true;
	}



	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


