package edu.stanford.cs108.tetris;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated;
	private Piece stick1, stick2, stick3, stick4;
	private Piece square1, square2, square3, square4;
	private Piece s1_1, s1_2, s1_3, s1_4;
	private Piece s2_1, s2_2, s2_3, s2_4;
	private Piece l1_1, l1_2, l1_3, l1_4;
	private Piece l2_1, l2_2, l2_3, l2_4;
	

	@Before
	public void setUp() throws Exception {
		
		//Piece[] pieces = Piece.getPieces();
		
		pyr1 = new Piece(Piece.PYRAMID_STR);	
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		square1 = new Piece(Piece.SQUARE_STR);;
		square2 = square1.computeNextRotation();
		square3 = square2.computeNextRotation();
		square4 = square3.computeNextRotation();
		
		stick1 = new Piece(Piece.STICK_STR);
		stick2 = stick1.computeNextRotation();
		stick3 = stick2.computeNextRotation();
		stick4 = stick3.computeNextRotation();
		
		s1_1 = new Piece(Piece.S1_STR);
		s1_2 = s1_1.computeNextRotation();
		s1_3 = s1_2.computeNextRotation();
		s1_4 = s1_3.computeNextRotation();
		
		s2_1 = new Piece(Piece.S2_STR);
		s2_2 = s2_1.computeNextRotation();
		s2_3 = s2_2.computeNextRotation();
		s2_4 = s2_3.computeNextRotation();
		
		l1_1 = new Piece(Piece.L1_STR);
		l1_2 = l1_1.computeNextRotation();
		l1_3 = l1_2.computeNextRotation();
		l1_4 = l1_3.computeNextRotation();
		
		l2_1 = new Piece(Piece.L2_STR);
		l2_2 = l2_1.computeNextRotation();
		l2_3 = l2_2.computeNextRotation();
		l2_4 = l2_3.computeNextRotation();
				
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
	}
	
	@Test
	public void testSizes() {
		//pyramid and a rotation
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		//square 
		assertEquals(2, square1.getWidth());
		assertEquals(2, square1.getHeight());
		
		//stick and a rotation
		assertEquals(4, stick1.getHeight());
		assertEquals(1, stick1.getWidth());
		assertEquals(1, stick2.getHeight());
		assertEquals(4, stick2.getWidth());
		
		//s1 and all rotations
		assertEquals(2, s1_1.getHeight());
		assertEquals(3, s1_1.getWidth());
		assertEquals(3, s1_2.getHeight());
		assertEquals(2, s1_2.getWidth());
		assertEquals(2, s1_3.getHeight());
		assertEquals(3, s1_3.getWidth());
		assertEquals(3, s1_4.getHeight());
		assertEquals(2, s1_4.getWidth());
		
		//s2 and a rotation
		assertEquals(2, s2_1.getHeight());
		assertEquals(3, s2_1.getWidth());
		assertEquals(3, s2_2.getHeight());
		assertEquals(2, s2_2.getWidth());
		
		//l1 and all rotations
		assertEquals(3, l1_1.getHeight());
		assertEquals(2, l1_1.getWidth());
		assertEquals(2, l1_2.getHeight());
		assertEquals(3, l1_2.getWidth());
		assertEquals(3, l1_3.getHeight());
		assertEquals(2, l1_3.getWidth());
		assertEquals(2, l1_4.getHeight());
		assertEquals(3, l1_4.getWidth());
		
		//l2 and all rotations
		assertEquals(3, l2_1.getHeight());
		assertEquals(2, l2_1.getWidth());
		assertEquals(2, l2_2.getHeight());
		assertEquals(3, l2_2.getWidth());
		assertEquals(3, l2_3.getHeight());
		assertEquals(2, l2_3.getWidth());
		assertEquals(2, l2_4.getHeight());
		assertEquals(3, l2_4.getWidth());
		
	}
	
	@Test
	public void testRotations() {
		//pyramid rotation tests
		Piece pyr1Test = new Piece("0 0  1 0  1 1  2 0");
		assertTrue(pyr1Test.equals(pyr1));
		
		Piece pyr2Test = new Piece("0 1  1 0  1 1  1 2");
		assertTrue(pyr2Test.equals(pyr2));
		
		Piece pyr3Test = new Piece("0 1  1 0  1 1  2 1");
		assertTrue(pyr3Test.equals(pyr3));
		
		Piece pyr4Test = new Piece("0 0  0 1  0 2  1 1");
		assertTrue(pyr4Test.equals(pyr4));
		
		assertTrue(pyr4.computeNextRotation().equals(pyr1));
		
		//square rotation tests
		assertTrue(square1.equals(square2));
		assertTrue(square2.equals(square3));
		assertTrue(square3.equals(square4));
		
		//stick rotation tests
		Piece stick1Test = new Piece("0 0  0 1  0 2  0 3");
		assertTrue(stick1Test.equals(stick1));
		
		Piece stick2Test = new Piece("0 0  1 0  2 0  3 0");
		assertTrue(stick2Test.equals(stick2));
		
		assertTrue(stick1Test.equals(stick3));
		assertTrue(stick2Test.equals(stick4));
		
		//s1 rotation tests
		Piece s1_1Test = new Piece("0 0  1 0  1 1  2 1");
		assertTrue(s1_1Test.equals(s1_1));
		
		Piece s1_2Test = new Piece("0 2  0 1  1 1  1 0");
		assertTrue(s1_2Test.equals(s1_2));

		assertTrue(s1_1Test.equals(s1_3));
		assertTrue(s1_2Test.equals(s1_4));
		
		//s2 rotation tests
		Piece s2_1Test = new Piece("0 1  1 1  1 0  2 0");
		assertTrue(s2_1Test.equals(s2_1));
		
		Piece s2_2Test = new Piece("0 0  0 1  1 1  1 2");
		assertTrue(s2_2Test.equals(s2_2));
		
		assertTrue(s2_1Test.equals(s2_3));
		assertTrue(s2_2Test.equals(s2_4));

		//l1 tests
		Piece l1_1Test = new Piece("0 0  0 1  0 2  1 0");
		assertTrue(l1_1Test.equals(l1_1));
		
		Piece l1_2Test = new Piece("0 0  1 0  2 0  2 1");
		assertTrue(l1_2Test.equals(l1_2));
		
		Piece l1_3Test = new Piece("0 2  1 2  1 1  1 0");
		assertTrue(l1_3Test.equals(l1_3));
		
		Piece l1_4Test = new Piece("0 0  0 1  1 1  2 1");
		assertTrue(l1_4Test.equals(l1_4));
		
		//l2 tests
		Piece l2_1Test = new Piece("0 0  1 0  1 1  1 2");
		assertTrue(l2_1Test.equals(l2_1));
		
		Piece l2_2Test = new Piece("0 1  1 1  2 1  2 0");
		assertTrue(l2_2Test.equals(l2_2));
		
		Piece l2_3Test = new Piece("0 0  0 1  0 2  1 2");
		assertTrue(l2_3Test.equals(l2_3));
		
		Piece l2_4Test = new Piece("0 0  0 1  1 0  2 0");
		assertTrue(l2_4Test.equals(l2_4));
		
	}
	
	
	@Test
	public void testSkirts() {
		//pyramid skirt
		assertTrue(Arrays.equals(pyr1.getSkirt(), new int[] {0, 0, 0}));
		assertTrue(Arrays.equals(pyr2.getSkirt(), new int[] {1, 0}));
		assertTrue(Arrays.equals(pyr3.getSkirt(), new int[] {1, 0, 1}));
		
		//square skirt
		assertTrue(Arrays.equals(square1.getSkirt(), new int[] {0, 0}));
		
		//stick skirt
		assertTrue(Arrays.equals(stick1.getSkirt(), new int[] {0}));
		assertTrue(Arrays.equals(stick2.getSkirt(), new int[] {0, 0, 0, 0}));
		
		//s1 skirt
		assertTrue(Arrays.equals(s1_1.getSkirt(), new int[] {0, 0, 1}));
		assertTrue(Arrays.equals(s1_2.getSkirt(), new int[] {1, 0}));
		
		//s2 skirt
		assertTrue(Arrays.equals(s2_1.getSkirt(), new int[] {1, 0, 0}));
		assertTrue(Arrays.equals(s2_2.getSkirt(), new int[] {0, 1}));
		
		//l1 skirt
		assertTrue(Arrays.equals(l1_1.getSkirt(), new int[] {0, 0}));
		assertTrue(Arrays.equals(l1_2.getSkirt(), new int[] {0, 0, 0}));
		
		//l2 skirt
		assertTrue(Arrays.equals(l2_1.getSkirt(), new int[] {0, 0}));
		assertTrue(Arrays.equals(l2_2.getSkirt(), new int[] {1, 1, 0}));
		
	}
	
	@Test
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
	}
	
	
	// Test the skirt returned by a few pieces
	@Test
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));
	}
	
	@Test
	public void testEquals() {		
		Piece p1 = new Piece("2 0  1 0  0 0  1 1");
		
		Piece p2 = new Piece(Piece.PYRAMID_STR);
		assertTrue(p2.equals(p1));
	}
	
	
}
