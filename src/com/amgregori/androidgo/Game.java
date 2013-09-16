package com.amgregori.androidgo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.os.Bundle;
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
	
	// History step direction constants.  Must be int.
	public static final int PREVIOUS = 0;
	public static final int NEXT = 1;
	public static final int FIRST = 2;
	public static final int LAST = 3;

	// Properties constants (to serve as keys for Bundles).  Must be Strings.
	public static final String CAPTURES_KEY = "cap";
	
	// Operations
	public static final int ADD = 1;
	public static final int SUBTRACT = -1;

	// Instance variables
	private int koRule;
	private boolean suicideRule;
	private Board board;
	private char nextTurn;
	private History history;
	private boolean running;
	private HashMap<Character, Integer> captures;

	// Static methods.
	public static char invertColor(char color){
		return color == WHITE ? BLACK : color == BLACK ? WHITE : OUT_OF_BOUNDS;
	}

	// Constructors.
	protected Game(String position, char nextTurn, boolean running, History history, int koRule, boolean suicideRule, HashMap<Character, Integer> captures){
		this.board = new Board(position);
		this.running = running;
		this.nextTurn = nextTurn;
		this.captures = captures;

		this.history = history;

		this.koRule = koRule;
		this.suicideRule = suicideRule;
	}

	public Game(int koRule, boolean suicideRule, int boardSize){
		this.koRule = koRule;
		this.suicideRule = suicideRule;

		this.board = new Board(boardSize);
		this.nextTurn = BLACK;
		this.history = new History();
		this.running = true;
		this.captures = new HashMap<Character, Integer>();
		this.captures.put(WHITE, 0);
		this.captures.put(BLACK, 0);

		Situation s = new Situation(this.board.toString(), nextTurn);
		this.history.add(s);
	}

	public Game(){
		this(POSITIONAL, false, 19);
	}

	// Accessor & mutator methods.
	public char[] getPosition(){
		return board.getPosition();
	}

	public int getBoardSize(){
		return board.getBoardSize();
	}

	public int getCapturedStones(char color){
		return captures.get(color);
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
			changeCaptures(capturedCount, ADD);
			nextTurn = invertColor(nextTurn);
			Situation s = new Situation(newBoard.toString(), nextTurn, capturedCount);
			history.add(s);
			for(int i = 0; i < board.getPosition().length; i++){
				if(newBoard.getPosition()[i] != board.getPosition()[i]){
					changes.add(i);
				}
			}
			board = newBoard;
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

	// Parcelable implementation.
	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeString(board.toString());
		dest.writeIntArray(new int[]{
				(int) nextTurn,
				koRule
		});
		dest.writeBooleanArray(new boolean[]{
				suicideRule,
				running
		});
		dest.writeParcelable(history, 0);
		Bundle b = new Bundle();
		b.putSerializable(CAPTURES_KEY, captures);
		dest.writeBundle(b);
	}

	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>(){
		@Override
		public Game createFromParcel(Parcel parcel){
			String position = parcel.readString();

			int[] intArray = parcel.createIntArray();
			char nextTurn = (char) intArray[0];
			int koRule = intArray[1];

			boolean[] booleanArray = parcel.createBooleanArray();
			boolean suicideRule = booleanArray[0];
			boolean running = booleanArray[1];

			History history = (History) parcel.readParcelable(History.class.getClassLoader());
			
			Bundle b = parcel.readBundle();
			HashMap<Character, Integer> captures = (HashMap<Character, Integer>) b.getSerializable(CAPTURES_KEY);

			return new Game(position, nextTurn, running, history, koRule, suicideRule, captures);
		}

		@Override
		public Game[] newArray(int size) {
			return new Game[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	// Undo, redo, first, last.
	public void stepHistory(int direction){
		if(history.size() > 1){
			Situation step = null;
			switch(direction){
				case PREVIOUS:
					Situation current = history.current();
					step = history.previous();
					if(step != null) changeCaptures(current.getCaptures(), SUBTRACT);
					break;
				case NEXT:
					step = history.next();
					if(step != null) changeCaptures(step.getCaptures(), ADD);
					break;
				case FIRST:
					step = history.first();
					captures.put(BLACK, 0);
					captures.put(WHITE, 0);
					break;
				case LAST:
					step = history.last();
					captures = history.getCumulativeCaptures();
					break;
			}
			if(step != null){
				board = new Board(step.getPosition());
				nextTurn = step.getTurn();
				running = !history.checkGameOver();
			}
		}
	}
	
	// Other methods.
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

	public boolean isRunning(){
		return running;
	}

	public void changeCaptures(HashMap<Character, Integer> changes, int operation){
		for(HashMap.Entry<Character, Integer> e : changes.entrySet())
			captures.put(e.getKey(), captures.get(e.getKey()) + e.getValue()*operation);
	}

	public void checkVacancy(int x, int y) throws PositionOccupiedException{
		if(board.getColor(x, y) != EMPTY){
			throw new PositionOccupiedException();
		}
	}

	public void checkRunning() throws GameOverException{
		if(!running)
			throw new GameOverException();
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


	// toString
	public String toString(){
		return board.toString();
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