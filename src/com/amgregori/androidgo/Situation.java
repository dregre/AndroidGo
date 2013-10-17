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

import java.util.HashMap;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * Model for a Go game's situation. A <code>Situation</code> stores a
 * board position (i.e., the state of all board points), information
 * about who is to play, and a cumulative captures count. 
 *
 */
public class Situation implements Parcelable {
	private final static String CAPTURES = "caps";
	
	String position;
	char turn;
	HashMap<Character, Integer> captures;

	/**
	 * Constructs a Situation with the given position and turn.
	 * @param position	A String representing the state of all board
	 * points. 
	 * @param turn	Whose turn it is to move.
	 */
	public Situation(String position, char turn){
		this(position, turn, new HashMap<Character, Integer>());
	}

	/**
	 * Constructs a Situation with the given position, turn, and
	 * cumulative capture counts.
	 * @param position	A String representing the state of all board
	 * points. 
	 * @param turn	Whose turn it is to move.
	 * @param captures	Map containing cumulative capture counts keyed to
	 * the color of the captures.
	 */
	protected Situation(String position, char turn, HashMap<Character, Integer> captures){
		this.position = position;
		this.turn = turn;
		this.captures = new HashMap<Character, Integer>(captures);
	}
	
	/**
	 * Returns the position, the state of all board points. 
	 * @return	String representation of the position.
	 */
	public String getPosition(){
		return position;
	}
	
	/**
	 * Returns whose turn it is to move. 
	 * @return	Color of whose turn it is to move
	 */
	public char getTurn(){
		return turn;
	}
	
	/**
	 * Returns cumulative capture counts. 
	 * @return	Map containing cumulative capture counts keyed to
	 * the color of the captures.
	 */
	public HashMap<Character, Integer> getCaptures(){
		return new HashMap<Character, Integer>(captures);
	}
	
	@Override
	public int hashCode(){
		return position.hashCode();
	}
	
	@Override
	public boolean equals(Object other){
		if(this == other){
			return true;
		}else if(other instanceof Situation &&
				((Situation) other).getPosition().equals(position) &&
				((Situation) other).getTurn() == turn){
			return true;
		}
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(position);
		dest.writeCharArray(new char[]{turn});
		Bundle b = new Bundle();
		b.putSerializable(CAPTURES, captures);
	}
	
	public static final Parcelable.Creator<Situation> CREATOR = new Parcelable.Creator<Situation>(){
        public Situation createFromParcel(Parcel in) {
            String position = in.readString();
            char turn = in.createCharArray()[0];
            Bundle b = in.readBundle();
            HashMap<Character, Integer> captures = (HashMap<Character, Integer>) b.getSerializable(CAPTURES);
        	return new Situation(position, turn, captures);
        }

        public Situation[] newArray(int size) {
            return new Situation[size];
        }
	};
}
