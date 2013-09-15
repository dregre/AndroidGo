package com.amgregori.androidgo;

import java.util.Collection;
import java.util.HashMap;
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
	History history;
	boolean running;
	int capturedWhites;
	int capturedBlacks;

	public static char invertColor(char color){
		return color == WHITE ? BLACK : color == BLACK ? WHITE : OUT_OF_BOUNDS;
	}
	
	protected Game(String position, char nextTurn, boolean running, History history, int koRule, boolean suicideRule, int capturedWhites, int capturedBlacks){
		this.board = new Board(position);
		this.running = running;
		this.nextTurn = nextTurn;
		this.capturedWhites = capturedWhites;
		this.capturedBlacks = capturedBlacks;
		
		this.history = history;
		
		this.koRule = koRule;
		this.suicideRule = suicideRule;
	}
	
	Game(int koRule, boolean suicideRule, int boardSize){
		this.koRule = koRule;
		this.suicideRule = suicideRule;
		
		this.board = new Board(boardSize);
		this.nextTurn = BLACK;
		this.history = new History();
		this.running = true;
		
		Situation s = new Situation(this.board.toString(), nextTurn);
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
		dest.writeString(board.toString());
		dest.writeIntArray(new int[]{
				(int) nextTurn,
				koRule,
				capturedWhites,
				capturedBlacks
				});
		dest.writeBooleanArray(new boolean[]{
				suicideRule,
				running
				});
		dest.writeParcelable(history, 0);
	}
	
	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>(){
		@Override
		public Game createFromParcel(Parcel parcel){
			String position = parcel.readString();
			
			int[] intArray = parcel.createIntArray();
			char nextTurn = (char) intArray[0];
			int koRule = intArray[1];
			int capturedWhites = intArray[2];
			int capturedBlacks = intArray[3];
			
			boolean[] booleanArray = parcel.createBooleanArray();
			boolean suicideRule = booleanArray[0];
			boolean running = booleanArray[1];
			
			History history = (History) parcel.readParcelable(History.class.getClassLoader());
			
			return new Game(position, nextTurn, running, history,
					        koRule, suicideRule, capturedWhites, capturedBlacks);
		}

		@Override
		public Game[] newArray(int size) {
			return new Game[size];
		}
	};
	
	public HashMap<Character, Integer> doCaptures(Board board, int x, int y) throws SuicideException{
		HashMap<Character, Integer> capturedCount = new HashMap<Character, Integer>();
		Point stone = new Point(x, y, board.getColor(x, y));
		HashSet<Point> removenda = null;
		int count;
		for(Point p : board.getSurrounding(x, y)){
			if(p.getColor() == invertColor(stone.getColor()) && board.isCaptured(p.getX(), p.getY())){
				removenda = board.getChain(p.getX(), p.getY());
				if(capturedCount.containsKey(p.getColor()))
					count = capturedCount.get(p.getColor()) + removenda.size();
				else
					count = removenda.size(); 
				capturedCount.put(p.getColor(), count);
				board.removeStones(removenda);
			}
		}
		if(suicideRule && board.isCaptured(x, y)){
			removenda = board.getChain(x, y); 
			capturedCount.put(board.getColor(x, y), removenda.size());
			board.removeStones(removenda);
		}else if(!suicideRule && board.isCaptured(x, y)){
			throw new SuicideException();
		}
		return capturedCount;
	}
	
	public void checkVacancy(int x, int y) throws PositionOccupiedException{
		if(board.getColor(x, y) != EMPTY){
			throw new PositionOccupiedException();
		}
	}
	
	public void incrementCapturedStones(char color, int value){
		if(color == BLACK)
			capturedBlacks += value;
		else if(color == WHITE)
			capturedWhites += value;
	}
	
	public void setCapturedStones(char color, int value){
		if(color == BLACK)
			capturedBlacks = value;
		else if(color == WHITE)
			capturedWhites = value;
	}
	
	public int getCapturedStones(char color){
		return color == BLACK ? capturedBlacks : color == WHITE ? capturedWhites : 0;
	}

	public Collection<Integer> setStone(int index){
		return setStone(index % board.getBoardSize(), index / board.getBoardSize());
	}
	
	public Collection<Integer> setStone(int x, int y){
		HashSet<Integer> changes = new HashSet<Integer>();
		try{
			checkRunning();
			checkVacancy(x, y);
			Board newBoard = board.clone();
			newBoard.setStone(x, y, nextTurn);
			HashMap<Character, Integer> capturedCount = doCaptures(newBoard, x, y);
			checkKo(newBoard.toString());
			for(HashMap.Entry<Character, Integer> entry : capturedCount.entrySet()) {
				incrementCapturedStones(entry.getKey(), entry.getValue());
			}
			nextTurn = invertColor(nextTurn);
			Situation s = new Situation(newBoard.toString(), nextTurn, capturedCount);
			history.add(s);
			for(int i = 0; i < board.getPosition().length; i++){
				if(newBoard.getPosition()[i] != board.getPosition()[i]){
					changes.add(i);
				}
			}
			board = newBoard;
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
			nextTurn = invertColor(nextTurn);
			Situation s = new Situation(board.toString(), nextTurn);
			history.add(s);
			if(history.checkGameOver()){
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
	
	public void lastMove(){
		Situation last = history.last();
		board = new Board(last.getPosition());
		nextTurn = last.getTurn();
		for(HashMap.Entry<Character, Integer> e : history.getCumulativeCaptures().entrySet())
			setCapturedStones(e.getKey(), e.getValue());
		running = !history.checkGameOver();
	}
	
	public void firstMove(){
		Situation first = history.first();
		board = new Board(first.getPosition());
		nextTurn = first.getTurn();
		capturedBlacks = 0;
		capturedWhites = 0;
		running = !history.checkGameOver();
	}	
	
	public void undoMove(){
		Situation current = history.current();
		Situation previous = history.previous();
		if(previous != null){
			board = new Board(previous.getPosition());
			nextTurn = previous.getTurn();
			for(HashMap.Entry<Character, Integer> e : current.getCaptures().entrySet())
				incrementCapturedStones(e.getKey(), e.getValue()*-1);
			running = !history.checkGameOver();
		}
	}
	
	public void redoMove(){
		Situation next = history.next();
		if(next != null){
			board = new Board(next.getPosition());
			nextTurn = next.getTurn();
			for(HashMap.Entry<Character, Integer> e : next.getCaptures().entrySet())
				incrementCapturedStones(e.getKey(), e.getValue());
			running = !history.checkGameOver();
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