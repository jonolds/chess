import java.util.ArrayList;
import java.util.Random;

public class Game {
	Agent wh, bl;
	State s;
	Random r = new Random();

	Game(int wh_lookahead, int bl_lookahead) {
		this.wh = new Agent(true, wh_lookahead, this);
		this.bl = new Agent(false, bl_lookahead, this);
		s = new State();
		s.resetBoard();
		s.printBoard(System.out);
		System.out.println();

//		s.move(1, 1, 4, 7);
		System.out.println(s.getPiece(4, 7));

		s.move(1, 0, 2, 2);
		s.printBoard(System.out);
		System.out.println(s.heuristic(r));
	}

	void play() {

	}



	public static void main(String[] args) {
		System.out.println(args[0] + " " + args[1]);
		Game g = new Game(1, 5);
		g.play();
	}

	class Agent {
		int lookahead;
		Game game;
		boolean is_white;
		Agent(boolean is_wh, int la_depth, Game g) {
			is_white = is_wh;
			game = g;
			lookahead = la_depth;
		}

		State.Move findBestMove() {
			//Get best move as index related to State.getPossMovesList(is_white)
			int best_move = alphaBeta(s, lookahead, true, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

			ArrayList<State.Move> poss_moves = s.getPossMovesList(is_white);
			return poss_moves.get(best_move);
		}

		int alphaBeta(State node, int depth, boolean isMax, int alpha, int beta, boolean top) {
			//RETURN if at terminal node or winning move has been found
			if(depth == 0 || node.isGameOver())
				return node.heuristic(r);

			//get all the possible moves
			ArrayList<State.Move> poss_moves = node.getPossMovesList(isMax);
			int best_index = 0;
			//MAX turn
			if(isMax) {
				int value = Integer.MIN_VALUE;
				for(int i = 0; i < poss_moves.size(); i++) {
					State copy = new State(node);
					State.Move mv = poss_moves.get(i);
					copy.move(mv.x1, mv.y1, mv.x2, mv.y2);
					int tmp = alphaBeta(copy, depth-1, false, alpha, beta, false);
					if(tmp > value) {
						value = tmp;
						best_index = i;
					}
					alpha = Math.max(alpha, value);
					if(alpha >= beta)
						break;
				}
				return (top) ? value : best_index;
			}
			//MIN turn
			else {
				int value = Integer.MAX_VALUE;
				for(int i = 0; i < poss_moves.size(); i++) {
					State copy = new State(node);
					State.Move mv = poss_moves.get(i);
					copy.move(mv.x1, mv.y1, mv.x2, mv.y2);
					int tmp = alphaBeta(copy, depth-1, true, alpha, beta, false);
					if(tmp < value) {
						value = tmp;
						best_index = i;
					}
					beta = Math.min(beta, value);
					if(alpha >= beta)
						break;
				}
				return (top) ? value : best_index;
			}
		}
	}
}