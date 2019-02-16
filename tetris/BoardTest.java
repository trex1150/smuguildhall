package edu.stanford.cs108.tetris;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoardTest {
	
	private Piece stick1, stick2, l1_1, l1_2, l1_3, l1_4, l2_1, l2_2, l2_3, l2_4, s1_1, s1_2, s2_1, s2_2, square1, pyr1, pyr2, pyr3, pyr4;
	
	private Board smallBoard;
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;


	@Before
	public void setUp() throws Exception {
		pyr1 = new Piece(Piece.PYRAMID_STR);	
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		square1 = new Piece(Piece.SQUARE_STR);;
		
		stick1 = new Piece(Piece.STICK_STR);
		stick2 = stick1.computeNextRotation();
	
		s1_1 = new Piece(Piece.S1_STR);
		s1_2 = s1_1.computeNextRotation();

		s2_1 = new Piece(Piece.S2_STR);
		s2_2 = s2_1.computeNextRotation();
		
		l1_1 = new Piece(Piece.L1_STR);
		l1_2 = l1_1.computeNextRotation();
		l1_3 = l1_2.computeNextRotation();
		l1_4 = l1_3.computeNextRotation();
		
		l2_1 = new Piece(Piece.L2_STR);
		l2_2 = l2_1.computeNextRotation();
		l2_3 = l2_2.computeNextRotation();
		l2_4 = l2_3.computeNextRotation();	
		
		smallBoard = new Board(3, 6);

	}
	
	@Test
	public void simpleTest() {
		int returnVal = 0;
		
		returnVal = smallBoard.place(stick1, 0, 0);
		assertEquals(PLACE_OK, returnVal);
		assertEquals(4, smallBoard.getColumnHeight(0));
		assertEquals(4, smallBoard.getMaxHeight());
		assertEquals(4, smallBoard.dropHeight(square1, 0));
		smallBoard.undo();
		assertEquals(0, smallBoard.getMaxHeight());
		assertEquals(0, smallBoard.getColumnHeight(0));
		assertEquals(0, smallBoard.dropHeight(square1, 0));
		
		returnVal = smallBoard.place(pyr1, 0, 0);
		assertEquals(PLACE_ROW_FILLED, returnVal);
		assertEquals(3, smallBoard.getRowWidth(0));
		assertEquals(1, smallBoard.getRowWidth(1));
		assertEquals(2, smallBoard.getMaxHeight());
		smallBoard.commit();
		
		returnVal = smallBoard.place(stick2, 0, 3);
		assertEquals(PLACE_OUT_BOUNDS, returnVal);
		smallBoard.undo();
		
		returnVal = smallBoard.place(pyr1, 0, 1);
		assertEquals(returnVal, PLACE_BAD);
		smallBoard.undo();
		
		returnVal = smallBoard.place(pyr4, 0, 1);
		assertEquals(PLACE_ROW_FILLED, returnVal);
		assertEquals(2, smallBoard.getRowWidth(1));
		assertEquals(2, smallBoard.getRowWidth(2));
		assertEquals(4, smallBoard.getColumnHeight(0));
		assertEquals(3, smallBoard.getColumnHeight(1));
		assertEquals(1, smallBoard.getColumnHeight(2));
		assertEquals(4, smallBoard.getMaxHeight());
		smallBoard.commit();
		
		smallBoard.clearRows();
		assertEquals(3, smallBoard.getMaxHeight());
		assertEquals(3, smallBoard.getColumnHeight(0));
		assertEquals(0, smallBoard.getColumnHeight(2));
		smallBoard.undo();
		assertEquals(4, smallBoard.getMaxHeight());
		smallBoard.clearRows();
		smallBoard.commit();
		
		returnVal = smallBoard.place(square1, 1, 4);
		assertEquals(PLACE_OK, returnVal);
		assertEquals(6, smallBoard.getMaxHeight());
		smallBoard.undo();
		
		returnVal = smallBoard.place(s1_1, 2, 2);
		assertEquals(PLACE_OUT_BOUNDS, returnVal);
		smallBoard.undo();
		returnVal = smallBoard.place(s1_1, 0, 4);
		assertTrue(smallBoard.getGrid(0, 4));
		assertEquals(PLACE_OK, returnVal);
		smallBoard.undo();
		
		assertEquals(3, smallBoard.getMaxHeight());
	}
	
	@Test
	public void complexText() {
		int returnVal = 0;
		Board medBoard = new Board(6, 10);
		
		returnVal = medBoard.place(l1_1, 0, 0);
		assertEquals(PLACE_OK, returnVal);
		medBoard.commit();
		
		returnVal = medBoard.place(square1, 1, 1);
		assertEquals(PLACE_OK, returnVal);
		medBoard.commit();

		returnVal = medBoard.place(stick2, 0, 3);
		assertEquals(PLACE_OK, returnVal);
		medBoard.commit();

		returnVal = medBoard.place(l2_1, 2, 0);
		assertEquals(PLACE_OK, returnVal);
		medBoard.commit();

		returnVal = medBoard.place(stick1, 4, 0);
		assertEquals(PLACE_OK, returnVal);
		medBoard.commit();

		returnVal = medBoard.place(stick1, 5, 0);
		assertEquals(PLACE_ROW_FILLED, returnVal);
		medBoard.commit();

		assertTrue(medBoard.getGrid(0, 0));
		assertTrue(medBoard.getGrid(5, 3));
		assertEquals(6, medBoard.getRowWidth(0));
		assertEquals(6, medBoard.getRowWidth(1));
		assertEquals(6, medBoard.getRowWidth(2));
		assertEquals(6, medBoard.getRowWidth(3));
		assertEquals(4, medBoard.getColumnHeight(0));
		assertEquals(4, medBoard.getColumnHeight(3));
		assertEquals(4, medBoard.getMaxHeight());
		medBoard.clearRows();
		
		assertEquals(0, medBoard.getRowWidth(0));
		assertEquals(0, medBoard.getColumnHeight(0));
		assertEquals(0, medBoard.getMaxHeight());
		medBoard.undo();
		assertEquals(4, medBoard.getMaxHeight());
		medBoard.clearRows();
		medBoard.commit();
		
		returnVal = medBoard.place(s2_2, 3, 0);
		assertEquals(PLACE_OK, returnVal);
		assertEquals(2, medBoard.getColumnHeight(3));
		medBoard.commit();
		returnVal = medBoard.place(stick1, 0, 0);
		System.out.println(medBoard);
		assertEquals(4, medBoard.getColumnHeight(0));
		assertEquals(2, medBoard.getRowWidth(2));
		assertEquals(3, medBoard.getRowWidth(1));
		medBoard.clearRows();
		
		assertEquals(4, medBoard.getColumnHeight(0));
		assertEquals(2, medBoard.getRowWidth(2));
		assertEquals(3, medBoard.getRowWidth(1));
		
	}
	
	
	

	

}
