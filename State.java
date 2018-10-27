import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class State {
	Scanner sc = new Scanner(System.in);
	public static final int MAX_PIECE_MOVES = 27, PieceMask = 7, WhiteMask = 8, AllMask = 15;
	public static final int None = 0, Pawn = 1, Rook = 2, Knight = 3, Bishop = 4, Queen = 5, King = 6;
	int[] m_rows;

	//RETURNS -1 for bl win, 1 for wh win, 0 for game still in progress
	boolean isGameOver() {
		int wh_count = 0, bl_count = 0;
		for(int c = 0; c < 8; c++)
			for(int r = 0; r < 8; r++)
				if(getPiece(c, r) == 6) {
					if(isWhite(c, r)) wh_count++;
					else bl_count++;
				}
		if(wh_count == 0 || bl_count == 0)
			return true;
		return false;
	}

	int getPiece(int col, int row) { return (m_rows[row] >> (4 * col)) & PieceMask; }
	boolean isWhite(int col, int row) { return (((m_rows[row] >> (4*col)) & WhiteMask) > 0 ? true: false); }

	/// Sets the piece at square. If piece is None, then color doesn't matter
	void setPiece(int col, int row, int piece, boolean white) {
		m_rows[row] &= (~(AllMask << (4 * col)));
		m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
	}

	/// Returns true iff the parameters represent a valid move
	boolean isValid(int xSrc, int ySrc, int xDest, int yDest) {
		ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
		for(int i = 0; i < possible_moves.size(); i += 2) {
			if(possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
				return true;
		}
		return false;
	}

	/// Move piece from (xSrc, ySrc) to (xDest, yDest). Returns true if king taken and removes all other
	//pieces of that color. If pawn crosses it becomes queen.
	boolean move(int xSrc, int ySrc, int xDest, int yDest) {
		if(xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8)
			throw new RuntimeException("out of range");
		if(xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
			throw new RuntimeException("out of range");
		int target = getPiece(xDest, yDest);
		int p = getPiece(xSrc, ySrc);
		if(p == None)
			throw new RuntimeException("There is no piece in the source location");
		if(target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
			throw new RuntimeException("It is illegal to take your own piece");
		if(p == Pawn && (yDest == 0 || yDest == 7))
			p = Queen; // a pawn that crosses the board becomes a queen
		boolean white = isWhite(xSrc, ySrc);
		setPiece(xDest, yDest, p, white);
		setPiece(xSrc, ySrc, None, true);
		if(target == King) {
			//Take all pieces of a color if king taken
			int x, y;
			for(y = 0; y < 8; y++)
				for(x = 0; x < 8; x++)
					if(getPiece(x, y) != None)
						if(isWhite(x, y) != white)
							setPiece(x, y, None, true);
			return true;
		}
		return false;
	}



	ArrayList<Move> getPossMovesList(boolean white) {
		ArrayList<Move> move_list = new ArrayList<>();
		MoveIter it = iterator(white);
		while(it.hasNext())
			move_list.add(it.next());
		return move_list;
	}

	/// Returns an iterator that iterates over all possible moves for the specified color
	MoveIter iterator(boolean white) { return new MoveIter(this, white); }

/* CLASS CHESSMOVE - Represents a possible  move */
	static class Move {
		int x1, y1, x2, y2;
		Move() {}
		Move(int x1, int y1, int x2, int y2) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		}
	}

/* CLASS CHESSMOVEITERATOR - Iterates through all the possible moves for the specified color. */
	static class MoveIter {
		int x, y;
		ArrayList<Integer> moves;
		State state;
		boolean white;

		/// Constructs a move iterator
		MoveIter(State curState, boolean isWhite) {
			x = -1;
			y = 0;
			moves = null;
			state = curState;
			white = isWhite;
			advance();
		}

		private void advance() {
			if(moves != null && moves.size() >= 2) {
				moves.remove(moves.size() - 1);
				moves.remove(moves.size() - 1);
			}
			while(y < 8 && (moves == null || moves.size() < 2)) {
				if(++x >= 8) {
					x = 0;
					y++;
				}
				if(y < 8)
					moves = (state.getPiece(x, y) != State.None && state.isWhite(x, y) == white)
							? state.moves(x, y) : null;
			}
		}

		/// Returns true iff there is another move to visit
		boolean hasNext() { return (moves != null && moves.size() >= 2); }

		/// Returns the next move
		State.Move next() {
			State.Move m = new State.Move();
			m.x1 = x;
			m.y1 = y;
			m.x2 = moves.get(moves.size() - 2);
			m.y2 = moves.get(moves.size() - 1);
			advance();
			return m;
		}
	}

	/// Positive means white is favored. Negative means black is favored.
	int heuristic(Random rand) {
		int score = 0;
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				int p = getPiece(x, y);
				int value;
				switch(p) {
					case None: value = 0; break;
					case Pawn: value = 10; break;
					case Rook: value = 63; break;
					case Knight: value = 31; break;
					case Bishop: value = 36; break;
					case Queen: value = 88; break;
					case King: value = 500; break;
					default: throw new RuntimeException("what?");
				}
				score += (isWhite(x, y)) ? value : -value;
			}
		}
		return score + rand.nextInt(3) - 1;
	}

	//Returns poss moves for occupied space
	ArrayList<Integer> moves(int col, int row) {
		ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
		int p = getPiece(col, row);
		boolean bWhite = isWhite(col, row);
		int i, j;
//		int nMoves = 0;
		switch(p) {
			case Pawn:
				if(bWhite) {
					if(!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
						checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
				}
				else {
					if(!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
						checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
				}
				break;
			case Bishop:
				for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case Knight:
				checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
				checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
				checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
				checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
				break;
			case Rook:
				for(i = inc(col); true; i = inc(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(i = dec(col); true; i = dec(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(j = inc(row); true; j = inc(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(j = dec(row); true; j = dec(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				break;
			case Queen:
				for(i = inc(col); true; i = inc(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(i = dec(col); true; i = dec(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(j = inc(row); true; j = inc(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(j = dec(row); true; j = dec(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case King:
				checkMove(pOutMoves, inc(col), row, bWhite);
				checkMove(pOutMoves, inc(col), inc(row), bWhite);
				checkMove(pOutMoves, col, inc(row), bWhite);
				checkMove(pOutMoves, dec(col), inc(row), bWhite);
				checkMove(pOutMoves, dec(col), row, bWhite);
				checkMove(pOutMoves, dec(col), dec(row), bWhite);
				checkMove(pOutMoves, col, dec(row), bWhite);
				checkMove(pOutMoves, inc(col), dec(row), bWhite);
				break;
			default:
				break;
		}
		return pOutMoves;
	}

	//used for finding all possible moves for a piece at a given square
	boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite) {
		if(col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if(p > 0 && isWhite(col, row) == bWhite)
			return true;
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	//Can (probably) ignore for algorythm
	boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite) {
		if(col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if(bDiagonal)
			if(p == None || isWhite(col, row) == bWhite)
				return true;
		else
			if(p > 0)
				return true;
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}
	static int inc(int pos) { return (pos < 0 || pos >= 7) ? -1 : pos +1; }
	static int dec(int pos) { return (pos < 1) ? -1 : pos-1; }

	void printBoard(PrintStream stream) {
		stream.println("  0  1  2  3  4  5  6  7");
		stream.print(" +");
		for(int i = 0; i < 8; i++)
			stream.print("--+");
		stream.println();
		for(int j = 7; j >= 0; j--) {
			stream.print(Character.toString((char)(48 + j)));
			stream.print("|");
			for(int i = 0; i < 8; i++) {
				int p = getPiece(i, j);
				if(p != None)
					stream.print((isWhite(i, j)) ? "w": "b");
				switch(p) {
					case None: stream.print("  "); break;
					case Pawn: stream.print("p"); break;
					case Rook: stream.print("r"); break;
					case Knight: stream.print("n"); break;
					case Bishop: stream.print("b"); break;
					case Queen: stream.print("q"); break;
					case King: stream.print("K"); break;
					default: stream.print("?"); break;
				}
				stream.print("|");
			}
			stream.print(Character.toString((char)(48 + j)));
			stream.print("\n +");
			for(int i = 0; i < 8; i++)
				stream.print("--+");
			stream.println();
		}
		stream.println("  0  1  2  3  4  5  6  7");
	}
	void resetBoard2() {
		setPiece(0, 0, Rook, true);
		setPiece(1, 0, Knight, true);
		setPiece(2, 0, Bishop, true);
		setPiece(3, 0, Queen, true);
		setPiece(4, 0, King, true);
		setPiece(5, 0, Bishop, true);
		setPiece(6, 0, Knight, true);
		setPiece(7, 0, Rook, true);
		for(int i = 0; i < 8; i++)
			setPiece(i, 1, Pawn, true);
		for(int j = 2; j < 6; j++)
			for(int i = 0; i < 8; i++)
				setPiece(i, j, None, false);
		for(int i = 0; i < 8; i++)
			setPiece(i, 6, Pawn, false);
		setPiece(0, 7, Rook, false);
		setPiece(1, 7, Knight, false);
		setPiece(2, 7, Bishop, false);
		setPiece(3, 7, Queen, false);
		setPiece(4, 7, King, false);
		setPiece(5, 7, Bishop, false);
		setPiece(6, 7, Knight, false);
		setPiece(7, 7, Rook, false);
	}

	void resetBoard() {
		for(int j = 0; j < 0; j++)
			for(int i = 0; i < 8; i++)
				setPiece(i, j, None, false);
//		setPiece(0, 0, Rook, true);
//		setPiece(1, 0, Knight, true);
//		setPiece(2, 0, Bishop, true);
//		setPiece(3, 0, Queen, true);
		setPiece(4, 1, King, true);
		setPiece(3, 1, Pawn, true);
		setPiece(3, 2, Pawn, true);
		setPiece(4, 2, Pawn, true);
		setPiece(5, 2, Pawn, true);
		setPiece(5, 1, Pawn, true);
//		setPiece(5, 0, Bishop, true);
//		setPiece(6, 0, Knight, true);
//		setPiece(7, 0, Rook, true);
//		for(int i = 0; i < 8; i++)
//			setPiece(i, 1, Pawn, true);

//		for(int i = 0; i < 8; i++)
//			setPiece(i, 6, Pawn, false);
//		setPiece(0, 7, Rook, false);
//		setPiece(1, 7, Knight, false);
//		setPiece(2, 7, Bishop, false);
//		setPiece(3, 7, Queen, false);
		setPiece(4, 7, King, false);
		setPiece(3, 7, Pawn, false);
		setPiece(3, 6, Pawn, false);
		setPiece(4, 6, Pawn, false);
		setPiece(5, 6, Pawn, false);
		setPiece(5, 7, Pawn, false);
//		setPiece(5, 7, Bishop, false);
//		setPiece(6, 7, Knight, false);
//		setPiece(7, 7, Rook, false);
	}

	State() {
		m_rows = new int[8];
		resetBoard();
	}
	State(State that) {
		m_rows = new int[8];
		for(int i = 0; i < 8; i++)
			this.m_rows[i] = that.m_rows[i];
	}
}