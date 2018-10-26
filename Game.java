
public class Game {
	Agent lt, dk;
	ChessState s;

	Game(int lt_lookahead, int dk_lookahead) {
		this.lt = new Agent(lt_lookahead, this);
		this.dk = new Agent(dk_lookahead, this);
		s = new ChessState();
		s.resetBoard();
		s.printBoard(System.out);
		System.out.println();
		s.move(1/*B*/, 0/*1*/, 2/*C*/, 2/*3*/);
		s.printBoard(System.out);
	}

	public static void main(String[] args) {
		System.out.println(args[0] + " " + args[1]);
		Game g = new Game(1, 5);
	}

	class Agent {
		int lookahead;
		Game game;
		Agent(int la_depth, Game g) {
			game = g;
			lookahead = la_depth;
		}



		int alphaBetaPrune(ChessState node, int depth, int alpha, int beta, boolean myturn) {
			return 0;
		}
	}
}