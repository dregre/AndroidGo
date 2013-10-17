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
 * History of a Go game.
 *
 */
public class History implements Parcelable{
	private static final String DATA_KEY = "data"; 

	private SituationList data;
	private int cursor;

	/**
	 * Constructs an empty <code>History</code>.
	 */
	public History(){
		data = new SituationList();
		cursor = 1;
	}

	/**
	 * Constructor used internally to reconstruct a <code>History</code>
	 * from a <code>Parcel</code>.
	 * 
	 * @param data
	 * @param cursor
	 * @param cumulativeCaptures
	 */
	protected History(SituationList data, int cursor, HashMap<Character, Integer> cumulativeCaptures){
		this.data = data;
		this.cursor = cursor;
	}

	/**
	 * Add a <code>Situation</code> to the history.
	 * @param s	<code>Situation</code> to be added.
	 */
	public void add(Situation s){
		if(cursor < data.size())
			data.removeRange(cursor, data.size());
		data.add(s);
		cursor = data.size();
	}

	/**
	 * Returns the previous <code>Situation</code>.
	 * @return	Previous <code>Situation</code> if there is one,
	 * <code>null</code> otherwise.
	 */
	public Situation previous(){
		if(cursor > 1){
			cursor--;
			return data.get(cursor-1);
		}
		return null;
	}

	/**
	 * Returns the next <code>Situation</code>.
	 * @return	Next <code>Situation</code> if there is one,
	 * <code>null</code> otherwise.
	 */
	public Situation next(){
		if(cursor < data.size()){
			cursor++;
			return data.get(cursor-1);
		}
		return null;
	}

	/**
	 * Returns the current <code>Situation</code>.
	 * @return	Current <code>Situation</code>
	 */
	public Situation current(){
		return cursor > 0 ? data.get(cursor-1) : null;
	}

	/**
	 * Returns the first <code>Situation</code>.
	 * @return	First <code>Situation</code>, <code>null</code> if the
	 * history is empty.
	 */
	public Situation first(){
		if(data.size() <= 0)
			return null;
		cursor = 1;
		return data.get(0);

	}

	/**
	 * Returns the last <code>Situation</code>.
	 * @return	Last <code>Situation</code>, <code>null</code> if the
	 * history is empty.
	 */
	public Situation last(){
		if(data.size() <= 0)
			return null;
		cursor = data.size();
		return data.get(cursor-1);
	}

	/**
	 * Returns the size of the history.
	 * @return	Number of elements in the history.
	 */
	public int size(){
		return data.size();
	}

	/**
	 * Check if history up to current move contains a given
	 * <code>Situation</code>.
	 * @param s
	 * @return	<code>true</code> if yes, <code>false</code> if no.
	 */
	public boolean contains(Situation s){
		return data.subList(0, cursor).contains(s);
	}
	
	/**
	 * Checks to see if the game is over due to two passes.
	 * @return	<code>true</code> if game is over, <code>false</code>
	 * if not.
	 */
	public boolean checkGameOver(){
		if(cursor >= 3){
			String p1 = data.get(cursor-1).getPosition();
			String p2 = data.get(cursor-2).getPosition();
			String p3 = data.get(cursor-3).getPosition();
			if(p1.equals(p2) && p2.equals(p3))
				return true;
		}
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeList(data);
		parcel.writeInt(cursor);
		Bundle b = new Bundle();
		b.putSerializable(DATA_KEY, data);
	}

	public static final Parcelable.Creator<History> CREATOR
	= new Parcelable.Creator<History>() {
		public History createFromParcel(Parcel in) {
			SituationList data = new SituationList();
			in.readList(data, data.getClass().getClassLoader());
			int cursor = in.readInt();
			HashMap<Character, Integer> cumulativeCaptures = (HashMap<Character, Integer>) in.readSerializable();
			return new History(data, cursor, cumulativeCaptures);
		}

		public History[] newArray(int size) {
			return new History[size];
		}
	};
}