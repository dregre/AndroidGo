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

/**
 * 
 * Model of an intersection on a Go board.  Stores the x and y
 * coordinates and the color of the stone (can also be empty or
 * out-of-bounds).
 *
 */
public class Point {
	final int x;
	final int y;
	final char color;

	/**
	 * Construct a <code>Point</code> with the given coordinates and
	 * color.
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @param color	Color of the stone located on this point (can also be
	 * empty or out-of-bounds)
	 */
	Point(int x, int y, char color){
		this.x = x;
		this.y = y;
		this.color = color;
	}

	/**
	 * Returns the point's x coordinate.
	 * @return x coordinate
	 */
	public int getX(){
		return this.x;
	}

	/**
	 * Returns the point's y coordinate.
	 * @return y coordinate
	 */
	public int getY(){
		return this.y;
	}

	/**
	 * Returns the color of the stone located on this point.
	 * @return color
	 */
	public char getColor(){
		return this.color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if(this == other){
			return true;
		}else if(other instanceof Point &&
				((Point) other).getX() == x &&
				((Point) other).getY() == y &&
				((Point) other).getColor() == color){
			return true;
		}
		return false;
	}

	//public int hashCode() {
	// // Get the number of possible digits in this board's X or Y coordinates. 
	// String digits = Integer.toString(Integer.toString(this.boardSize).length());
	// 
	// // Construct an int in the form 1[X][Y][color] and return it.
	// StringBuilder sb = new StringBuilder();
	// sb.append('1');
	// sb.append(String.format("%0" + digits + "d", this.x));
	// sb.append(String.format("%0" + digits + "d", this.y));
	// sb.append(this.color);
	// return Integer.parseInt(sb.toString());
	//}

}