package com.amgregori.androidgo;

import java.util.HashSet;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;



public class Game implements Parcelable {
	// Point constants.  Must be chars of positive integers.
	public static final char WHITE = '0';
	public static final char BLACK = '1';
	public static final char EMPTY = '2';
	public static final char OUT_OF_BOUNDS = '3';

	// Rules constants.  Must be positive integers.
	public static final int POSITIONAL = 0;
	public static final int SITUATIONAL = 1;
	public static final int JAPANESE = 2;
	
	// Properties constants (to serve as keys in ContentValues).  Must be Strings.
	public static final String POSITION = "pos";
	public static final String NEXT_TURN = "nxt";
	public static final String RUNNING = "run";
	public static final String HISTORY = "his";

	// Instance variables
	int koRule;
	boolean suicideRule;
	Board board;
	int passes;
	char nextTurn;
	HashSet<Situation> history;
	boolean running;

	public static char invertColor(char color){
		return color == WHITE ? BLACK : color == BLACK ? WHITE : OUT_OF_BOUNDS;
	}
	
	protected Game(String position, char nextTurn, boolean running, String[] historicalPositions, char[] historicalTurns, int koRule, boolean suicideRule){
		this.board = new Board(position);
		this.running = running;
		this.nextTurn = nextTurn;
		
		this.history = new HashSet<Situation>();
		for(int i = 0; i < historicalTurns.length; i++){
			history.add(new Situation(historicalPositions[i], historicalTurns[i]));
		}
		
		this.koRule = koRule;
		this.suicideRule = suicideRule;
	}
	
	Game(int koRule, boolean suicideRule, int boardSize){
		this.koRule = koRule;
		this.suicideRule = suicideRule;
		
		this.board = new Board(boardSize);
		this.nextTurn = BLACK;
		this.history = new HashSet<Situation>();
		this.running = true;
		
		Situation s = new Situation(this.board.toString(), WHITE);
		this.history.add(s);
	}
	
	Game(){
		this(POSITIONAL, false, 19);
	}
	
	public char[] getPosition(){
		return board.getPosition();
	}
	
	public int getBoardSize(){
		return board.getBoardSize();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags){
		String[] positions = new String[history.size()];
		char[] turns = new char[history.size()];
		int i = 0;
		for(Situation s : history){
			positions[i] = s.getPosition();
			turns[i] = s.getTurn();
			i++;
		}
		
		dest.writeString(board.toString());
		dest.writeCharArray(turns);
		dest.writeStringArray(positions);
		dest.writeIntArray(new int[]{
				(int) nextTurn,
				koRule
				});
		dest.writeBooleanArray(new boolean[]{
				suicideRule,
				running
				});
	}
	
	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>(){
		@Override
		public Game createFromParcel(Parcel parcel){
			String position = parcel.readString();
			char[] historicalTurns = parcel.createCharArray();
			String[] historicalPositions = parcel.createStringArray();
			
			int[] intArray = parcel.createIntArray();
			char nextTurn = (char) intArray[0];
			int koRule = intArray[1];
			
			boolean[] booleanArray = parcel.createBooleanArray();
			boolean suicideRule = booleanArray[0];
			boolean running = booleanArray[1];
			
			return new Game(position, nextTurn, running, historicalPositions, historicalTurns, koRule, suicideRule);
		}

		@Override
		public Game[] newArray(int size) {
			return new Game[size];
		}
	};
	
	public void doCaptures(Board board, int x, int y) throws SuicideException{
		Point stone = new Point(x, y, board.getStone(x, y)); 
		for(Point p : board.getSurrounding(x, y)){
			if(p.getColor() == invertColor(stone.getColor()) && board.isCaptured(p.getX(), p.getY())){
				board.removeStones(board.getChain(p.getX(), p.getY()));
			}
		}
		if(suicideRule && board.isCaptured(x, y)){
			board.removeStones(board.getChain(x, y));
		}else if(!suicideRule && board.isCaptured(x, y)){
			throw new SuicideException();
		}
	}
	
	public void checkVacancy(int x, int y) throws PositionOccupiedException{
		if(board.getStone(x, y) != EMPTY){
			throw new PositionOccupiedException();
		}
	}

	public HashSet<Integer> setStone(int index){
		return setStone(index % board.getBoardSize(), index / board.getBoardSize());
	}
	
	public HashSet<Integer> setStone(int x, int y){
		HashSet<Integer> changes = new HashSet<Integer>();
		try{
			checkRunning();
			checkVacancy(x, y);
			Board newBoard = board.clone();
			newBoard.setStone(x, y, nextTurn);
			doCaptures(newBoard, x, y);
			checkKo(newBoard.toString());
			Situation s = new Situation(newBoard.toString(), nextTurn);
			history.add(s);
			for(int i = 0; i < board.getPosition().length; i++){
				if(newBoard.getPosition()[i] != board.getPosition()[i]){
					changes.add(i);
				}
			}
			board = newBoard;
			nextTurn = invertColor(nextTurn);
			passes = 0;
//			board.print();
		}catch(GameOverException ex) {
			Log.v("1", "Game over.  No more moves allowed.");
		}catch(KoException ex) {
			Log.v("1", "Illegal move. Ko is violated.");
		}catch(SuicideException ex){
			Log.v("1", "Illegal move. Suicide is not allowed.");			
		}catch(PositionOccupiedException ex){
			Log.v("1", "Illegal move. Position is occupied.");			
		}
		return changes;
	}
	
	public void passTurn(){
		try{
			checkRunning();
			if(passes < 1){
				Situation s = new Situation(board.toString(), nextTurn);
				history.add(s);
				nextTurn = invertColor(nextTurn);
				passes += 1;
			}else{
				running = false;
				throw new GameOverException();
			}
		}catch(GameOverException ex){
			Log.v("1", "Game over.  No more moves allowed.");
		}
	}
	
	public void checkRunning() throws GameOverException{
		if(!running)
			throw new GameOverException();
	}
	
	public boolean isRunning(){
		return running;
	}
	
	public void checkKo(String position) throws KoException{
		// Japapnese ko
		/* insert here */
		// Situational superko
		Situation s = new Situation(position, nextTurn);
		if(koRule == SITUATIONAL &&
				history.contains(s)){
					throw new KoException();
		}
		// Positional superko
		if(koRule == POSITIONAL){
			Situation t = new Situation(position, invertColor(nextTurn));
			if(history.contains(s) ||
					history.contains(t)){
				throw new KoException();
			}
		}
	}
	
	public String toString(){
		return board.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

}

class KoException extends Exception{
	KoException(){ super(); }
}
class SuicideException extends Exception{
	SuicideException(){ super(); }	
}
class PositionOccupiedException extends Exception{
	PositionOccupiedException(){ super(); }
}
class GameOverException extends Exception{
	GameOverException(){ super(); }
}