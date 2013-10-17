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

import java.util.HashSet;

/**
 * 
 * Model of a Go board.  Stores the state of each board point.  
 *
 */
public class Board {
	int boardSize;
	char[] position;
	
	/**
	 * Constructs a <code>Board</code> from <code>position</code> with
	 * <code>boardSize</code>. 
	 * @param boardSize	Number of vertical or horizontal lines.
	 * For example, a 19x19 board has a <code>boardSize</code> of 19.
	 * @param position	A string of length <code>boardSize * boardSize
	 * </code> where each character represents a point on the board with
	 * the coordinates <code>x = length % boardSize</code> and
	 * <code>y = length / boardSize</code>.
	 */
	private Board(int boardSize, String position){
		if(position.length() != boardSize*boardSize)
			throw new BoardSizeException();

		this.position = position.toCharArray();
		this.boardSize = boardSize; 
	}
	
	/**
	 * Constructs a <code>Board</code> from <code>position</code>. 
	 * @param position	A string where each character represents a point
	 * on the board with the coordinates <code>x = length % boardSize
	 * </code> and <code>y = length / boardSize</code>.
	 */
	public Board(String position){
		int boardSize = (int) Math.sqrt(position.length());
		if(position.length() != boardSize*boardSize)
			throw new BoardSizeException(); 
		
		this.position = position.toCharArray();
		this.boardSize = boardSize;
	}
	
	/**
	 * Constructs a <code>Board</code> from <code>position</code> with
	 * the number of vertical lines and the number of horizontal lines
	 * equal to <code>boardSize</code>. 
	 * @param boardSize	Number of vertical or horizontal lines.
	 * For example, a 19x19 board has a <code>boardSize</code> of 19.
	 */
	public Board(int boardSize){
		this(boardSize, emptyBoardPosition(boardSize));
	}

	/**
	 * Constructs a standard <code>Board</code> of size 19x19. 
	 */
	public Board(){
		this(19);
	}
	
	/**
	 * Returns a <code>char</code> array containing the current position
	 * of this board.  Each <code>char</code> in the array represents a
	 * point on the board with the coordinates
	 * <code>x = length % boardSize</code> and
	 * <code>y = length / boardSize</code>.  Possible values for each
	 * <code>char</code> are: <code>Game.WHITE</code>,
	 * <code>Game.BLACK</code>, and <code>Game.EMPTY</code>.
	 * 
	 * @return A <code>char</code> array containing the current position
	 * of this board.
	 * @see Game
	 */
	public char[] getPosition(){
		return position;
	}

	private static String emptyBoardPosition(int boardSize){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < boardSize*boardSize; i++)
			sb.append(Game.EMPTY);
		return sb.toString();
	}

	/**
	 * Remove stones from board.
	 * @param stones	Set of <code>Point</code>s to make empty.
	 */
	public void removeStones(HashSet<Point> stones){
		for(Point s : stones){
			setStone(s.getX(), s.getY(), Game.EMPTY);
		}
	}

	/**
	 * Get color of stones from given <code>x</code> and <code>y</code>
	 * coordinates. 
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @return Either Game.BLACK, Game.WHITE, Game.EMPTY, or
	 * Game.OUT_OF_BOUNDS if the coordinates are outside the board's
	 * boundaries.
	 * @see Game
	 */
	public char getColor(int x, int y){
		if(x >= 0 && x < this.boardSize && y >= 0 && y < boardSize) 
			return this.position[y * this.boardSize + x];
		return Game.OUT_OF_BOUNDS;
	}

	public String toString(){
		return new String(this.position);
	}

	/**
	 * Return the number of vertical or horizontal lines in the board.
	 * @return	 the number of vertical or horizontal lines in the board.
	 */
	public int getBoardSize(){
		return this.boardSize;
	}

	/**
	 * Add stone to the board (without checking any of the Go rules for 
	 * validation). 
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @param color Either Game.WHITE or Game.BLACK	
	 */
	public void setStone(int x, int y, char color){
		this.position[y * this.boardSize + x] = color;
	}
	
	/**
	 * Return the state (either white, black, empty, or out-of-bounds) of
	 * the points to the top, left, right, and bottom of the provided x
	 * and y coordinate.  
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @return	A four-element <code>Point</code> array.  Each Point
	 * represents a surrounding state.
	 * @see Point 
	 */
	public Point[] getSurrounding(int x, int y){
		Point[] surrounding = {
				// Left
				new Point(x-1, y, getColor(x-1, y)),
				// Right
				new Point(x+1, y, getColor(x+1, y)),
				// Top
				new Point(x, y-1, getColor(x, y-1)),
				// Bottom
				new Point(x, y+1, getColor(x, y+1))
		};
		return surrounding;
	}

	/**
	 * Return all <code>Point</code>s of the same color that are
	 * immediately adjacent (i.e., not diagonal) to that stone or
	 * recursively to other immediately adjacent stones.  
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @return	Set of <code>Point</code>.
	 */
	public HashSet<Point> getChain(int x, int y){ 
		HashSet<Point> chain = new HashSet<Point>();
		Point p = new Point(x, y, getColor(x, y));
		if(p.getColor() != Game.WHITE && p.getColor() != Game.BLACK){
			return chain;
		}
		chain.add(p);
		return getChain(x, y, chain);
	}

	private HashSet<Point> getChain(int referenceX, int referenceY, HashSet<Point> chain){
		for(Point p : getSurrounding(referenceX, referenceY)){
			if(p.getColor() == getColor(referenceX, referenceY) &&
					chain.add(new Point(p.getX(), p.getY(), p.getColor()))){
				chain = getChain(p.getX(), p.getY(), chain);
			}
		}
		return chain;
	}

	/**
	 * Determine if the stone at location x, y is captured.
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @return	<code>true</code> if captured, <code>false</code> if not.
	 */
	public boolean isCaptured(int x, int y){
		// Raise an exception if the point in x, y is neither white nor black.
		if(getColor(x, y) != Game.WHITE && getColor(x, y) != Game.BLACK){
			// Exception
			throw new IllegalPointException();
		}

		// Check if chain has any liberties.  If so, return true, otherwise false. 
		for(Point p : getChain(x, y)){
			for(Point s : getSurrounding(p.getX(), p.getY())){
				if(s.getColor() == Game.EMPTY)
					return false;
			}
		}
		return true;
	}

	/**
	 * Return all of the liberties of a given chain.
	 * @param x	x coordinate of any stone in the chain
	 * @param y	y coordinate of any stone in the chain
	 * @return	Set of <code>Point</code>s that are liberties. 
	 */
	public HashSet<Point> getChainLiberties(int x, int y){
		HashSet<Point> liberties = new HashSet<Point>(); 
		for(Point p : getChain(x, y)){
			for(Point s : getSurrounding(p.getX(), p.getY())){
				if(s.getColor() == Game.EMPTY)
					liberties.add(new Point(s.getX(), s.getY(), Game.EMPTY));
			}
		}
		return liberties;
	}

	/**
	 * Make a deep copy of the board.
	 * @return	Deep copy of the board. 
	 */
	public Board clone(){
		return new Board(boardSize, toString());
	}

	/**
	 * Print out to System a graphical representation of the board.
	 */
	public void print(){
		StringBuilder sb = new StringBuilder();
		StringBuilder nb = new StringBuilder();

		for(int i = 0; i < boardSize; i++){
			if(i > 0){
				nb.append(String.format("%3s", Integer.toString(i)));
			}else{
				nb.append(String.format("%6s", Integer.toString(i)));
			}
		}

		String n = nb.toString();
		sb.append(n);
		sb.append('\n');

		for(int i = 0; i < this.position.length; i++){
			if(i == 0){
				sb.append(String.format("%3s", Integer.toString(0)));
				sb.append(' ');
			}else if(i > 0 && i % this.boardSize == 0){
				sb.append(' ');
				sb.append(i / this.boardSize - 1);
				sb.append('\n');
				sb.append(String.format("%3s", Integer.toString(i / this.boardSize )));
				sb.append(' ');
			}
			switch(this.position[i]){
			case Game.EMPTY: sb.append("-+-");
			break;
			case Game.WHITE: sb.append("-0-");
			break;
			case Game.BLACK: sb.append("-X-");
			break;
			}
		}

		sb.append(' ');
		sb.append(this.boardSize-1);
		sb.append('\n');
		sb.append(n);

		System.out.println(sb.toString());
	}

}

class BoardSizeException extends RuntimeException{
	BoardSizeException(){ super(); }
}

class IllegalPointException extends RuntimeException{
	IllegalPointException(){ super(); }
}