Name: Trevor Rex

Directory structure: All files are contained within this directory. The files that I wrote myself are Board.java, BoardTest.java, Piece.java, and PieceTest.java. The remainder of the files contain logic to run the game in a text interface or graphical interface.

Explanation/Description: The Board and Piece classes create a framework for the Tetris game to operate upon. The Board.java file creates a double array of booleans that operate as the board, with a true value meaning that a piece is placed there and a false value meaning otherwise. Within the file are methods that exist to check for collisions with the side and bottom of the boards and clear a row from the board when correct conditions are met. There is also an undo feature that returns the board to its previous state one move before the current one. The Piece.java file is utilized by the Board.java file, and there are useful methods for returning information about each piece such as width and height. There are also methods for computing the next orientation of the piece after rotation. The Test files are unit test that I implemented in order to ensure that my code operated correctly and to detect bugs before I deployed the code over to an Android Studio App. 

Date: January 2018
