/*
 * Copyright (C) 2013 Andre Gregori and Mark Garro 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.amgregori.androidgo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


/**
 * 
 * Model of a Go game.  Validates moves, stores history.
 *
 */
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

	// Static methods.
	public static char invertColor(char color){
		return color == WHITE ? BLACK : color == BLACK ? WHITE : OUT_OF_BOUNDS;
	}

	// Constructors.
	
	/**
	 * Constructor used internally to reconstruct game from
	 * <code>Parcel</code>.
	 * @param position
	 * @param nextTurn
	 * @param running
	 * @param history
	 * @param koRule
	 * @param suicideRule
	 * @param captures
	 */
	protected Game(String position, char nextTurn, boolean running, History history, int koRule, boolean suicideRule, HashMap<Character, Integer> captures){
		this.board = new Board(position);
		this.running = running;
		this.nextTurn = nextTurn;

		this.history = history;

		this.koRule = koRule;
		this.suicideRule = suicideRule;
	}

	/**
	 * Constructs game with special rules.
	 * @param koRule	Determines the game's ko rule. Either
	 * <code>SITUATIONAL</code>, <code>POSITIONAL</code>, or
	 * <code>JAPANESE</code>.
	 *   
	 * @param suicideRule	Switch to enable/disable suicide rule.  Use
	 * <code>true</code> for enabled, <code>false</code> for disabled.
	 * @param boardSize	Number of vertical or horizontal lines.
	 * For example, a 19x19 board has a <code>boardSize</code> of 19.
	 */
	public Game(int koRule, boolean suicideRule, int boardSize){
		this.koRule = koRule;
		this.suicideRule = suicideRule;

		this.board = new Board(boardSize);
		this.nextTurn = BLACK;
		this.history = new History();
		this.running = true;

		Situation s = new Situation(this.board.toString(), nextTurn);
		this.history.add(s);
	}

	/**
	 * Constructs a game with positional superko, no suicide rule, and a
	 * board size of 19x19.
	 */
	public Game(){
		this(POSITIONAL, false, 19);
	}

	// Accessor & mutator methods.
	
	/**
	 * Returns the position of the board.
	 * @return	A character array representing the board's position.
	 */
	public char[] getPosition(){
		return board.getPosition();
	}

	/**
	 * Returns the size of the board.
	 * @return	Number of vertical or horizontal lines.
	 * For example, a 19x19 board will return 19.
	 */
	public int getBoardSize(){
		return board.getBoardSize();
	}

	/**
	 * Counts the number of stones captured in the last move for the
	 * provided <code>color</code>.
	 * @param color	Either <code>BLACK</code> or <code>WHITE</code>.
	 * @return	Number of captured stones.
	 */
	public int getCapturedStones(char color){
		Integer captures = history.current().getCaptures().get(color);
		return  captures != null ? captures : 0;
	}

	/**
	 * Sets a stone on the board, checking if the move is valid, and
	 * performs any captures.
	 * @param index	
	 * @return The indexes of any changed stones in the board's position. 
	 */
	public Collection<Integer> setStone(int index){
		return setStone(index % board.getBoardSize(), index / board.getBoardSize());
	}
	
	private Collection<Integer> setStone(int x, int y){
		HashSet<Integer> changes = new HashSet<Integer>();
		try{
			checkRunning();
			checkVacancy(x, y);
			Board newBoard = board.clone();
			newBoard.setStone(x, y, nextTurn);
			HashMap<Character, Integer> capturesCount = changeCaptures(doCaptures(newBoard, x, y), history.current().getCaptures(), ADD);
			checkKo(newBoard.toString());
			nextTurn = invertColor(nextTurn);
			Situation s = new Situation(newBoard.toString(), nextTurn, capturesCount);
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

	/**
	 * Goes to a certain point in the game's history (either the previous,
	 * next, first, or last moves).
	 * 
	 * @param direction	Either <code>PREVIOUS</code>, <code>NEXT</code>,
	 * <code>FIRST</code>, or <code>LAST</code>.
	 */
	public void stepHistory(int direction){
		if(history.size() > 1){
			Situation step = null;
			switch(direction){
				case PREVIOUS:
					step = history.previous();
					break;
				case NEXT:
					step = history.next();
					break;
				case FIRST:
					step = history.first();
					break;
				case LAST:
					step = history.last();
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
	private HashMap<Character, Integer> doCaptures(Board board, int x, int y) throws SuicideException{
		HashMap<Character, Integer> capturesCount = new HashMap<Character, Integer>();
		Point stone = new Point(x, y, board.getColor(x, y));
		HashSet<Point> removenda = null;
		int count;
		for(Point p : board.getSurrounding(x, y)){
			if(p.getColor() == invertColor(stone.getColor()) && board.isCaptured(p.getX(), p.getY())){
				removenda = board.getChain(p.getX(), p.getY());
				if(capturesCount.containsKey(p.getColor()))
					count = capturesCount.get(p.getColor()) + removenda.size();
				else
					count = removenda.size(); 
				capturesCount.put(p.getColor(), count);
				board.removeStones(removenda);
			}
		}
		if(suicideRule && board.isCaptured(x, y)){
			removenda = board.getChain(x, y); 
			capturesCount.put(board.getColor(x, y), removenda.size());
			board.removeStones(removenda);
		}else if(!suicideRule && board.isCaptured(x, y)){
			throw new SuicideException();
		}
		return capturesCount;
	}

	/**
	 * Pass the current turn.
	 */
	public void passTurn(){
		try{
			checkRunning();
			nextTurn = invertColor(nextTurn);
			HashMap<Character, Integer> capturesCount = changeCaptures(new HashMap<Character, Integer>(), history.current().getCaptures(), ADD);
			Situation s = new Situation(board.toString(), nextTurn, capturesCount);
			history.add(s);
			if(history.checkGameOver()){
				running = false;
				throw new GameOverException();				
			}
		}catch(GameOverException ex){
			Log.v("1", "Game over.  No more moves allowed.");
		}
	}

	/**
	 * Check if game is not over.
	 * @return	<code>true</code> if game is running, <code>false</code>
	 * if game is over.
	 */
	public boolean isRunning(){
		return running;
	}

	private HashMap<Character, Integer> changeCaptures(HashMap<Character, Integer> original, HashMap<Character, Integer> changes, int operation){
		for(HashMap.Entry<Character, Integer> e : changes.entrySet()){
			char key = e.getKey();
			int value = e.getValue();
			Integer addend = original.get(key);
			if(addend != null)
				original.put(key, addend + value*operation);
			else
				original.put(key, value*operation);
		}
		return original;
	}

	private void checkVacancy(int x, int y) throws PositionOccupiedException{
		if(board.getColor(x, y) != EMPTY){
			throw new PositionOccupiedException();
		}
	}

	private void checkRunning() throws GameOverException{
		if(!running)
			throw new GameOverException();
	}

	private void checkKo(String position) throws KoException{
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