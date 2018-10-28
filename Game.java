import java.util.Random;
import java.util.Scanner;

public class Game {
	Scanner sc = new Scanner(System.in);
	Random r = new Random();
	public static final int MAX = Integer.MAX_VALUE, MIN = Integer.MIN_VALUE;

	Agent wh, bl;
	State st;

	public static void main(String[] args) {
		Game g = new Game(7, 5);
		g.play();
	}

	Game(int wh_la, int bl_la) {
		this.wh = new Agent(true, wh_la);
		this.bl = new Agent(false, bl_la);
		st = new State();
	}

	void play() {
		wh.makeBestMove();
		bl.makeBestMove();
	}

	class Agent {
		int lookahead;
		boolean is_white;

		boolean makeBestMove() {
			int best_score = -1, index = -1;
			Move[] moves = st.getPossMoves(is_white);
			for(int i = 0; i < moves.length; i++) {
				State copy = new State(st, moves[i]);
				int score = alphaBeta(copy, lookahead, is_white, MIN, MAX);
				score = score * ((is_white) ? 1 : -1);
				if(score > best_score) {
					best_score = score;
					index = i;
				}
			}
			st.move(moves[index]);
			st.printBoard();
			return st.isGameOver();
		}

		int alphaBeta(State node, int depth, boolean isMax, int alpha, int beta) {
			if(depth == 0 || node.isGameOver())
				return node.heuristic(r);

			//get all the possible moves
			Move[] possible = node.getPossMoves(isMax);
//			node.printBoard();
//			printMoves(possible);

			//MAX turn
			if(isMax) {
				int value = Integer.MIN_VALUE;
				for(int i = 0; i < possible.length; i++) {
//					State copy = new State(node);
//					Move m = possible[i];
//					copy.move(m);
//					System.out.println("(" + m.x1 + " " + m.y1 + ")" + " " + "(" + m.x2 + " " + m.y2 + ")     Heur: " + copy.heuristic(r));
					State copy = new State(node, possible[i]);
					value = Math.max(value, alphaBeta(copy, depth-1, !isMax, alpha, beta));
					alpha = Math.max(alpha, value);
					if(alpha >= beta)
						break;
				}
				return value;
			}
			//MIN turn
			else {
				int value = Integer.MAX_VALUE;
				for(int i = 0; i < possible.length; i++) {
//					State copy = new State(node);
//					Move m = possible[i];
//					copy.move(m);
//					System.out.println("(" + m.x1 + " " + m.y1 + ")" + " " + "(" + m.x2 + " " + m.y2 + ")     Heur: " + copy.heuristic(r));
					State copy = new State(node, possible[i]);
					value = Math.min(value, alphaBeta(copy, depth-1, !isMax, alpha, beta));
					beta = Math.min(beta, value);
					if(alpha >= beta)
						break;
				}
				return value;
			}
		}

		String actualStr() {
			return (is_white) ? "white" : "black";
		}

		Agent(boolean is_wh, int lookahead_depth) {
			this.is_white = is_wh;
			this.lookahead = lookahead_depth;
		}
	}

	void printMoves(Move... moves ) {
		System.out.println("PRINTING MOVES: ");
		for(Move mv: moves)
			System.out.println(mv.x1 + " " + mv.y1 + " " + mv.x2 + " " + mv.y2);
	}
}