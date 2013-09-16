package com.amgregori.androidgo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


public class History implements Parcelable{
	private static final String DATA_KEY = "data"; 

	private SituationList data;
	private int cursor;
	private HashMap<Character, Integer> cumulativeCaptures;

	public History(){
		data = new SituationList();
		cursor = 1;
		cumulativeCaptures = new HashMap<Character, Integer>();
		cumulativeCaptures.put(Game.BLACK, 0);
		cumulativeCaptures.put(Game.WHITE, 0);
	}
	
	protected History(SituationList data, int cursor, HashMap<Character, Integer> cumulativeCaptures){
		this.data = data;
		this.cursor = cursor;
		this.cumulativeCaptures = cumulativeCaptures;
	}

	public void add(Situation s){
		if(cursor < data.size()){
			for(int i = cursor-1; i < data.size(); i++){
				for(HashMap.Entry<Character, Integer> e : data.get(i).getCaptures().entrySet())
					cumulativeCaptures.put(e.getKey(), cumulativeCaptures.get(e.getKey()) - e.getValue());
			}
			data.removeRange(cursor, data.size());
		}
		data.add(s);
		for(HashMap.Entry<Character, Integer> e : s.getCaptures().entrySet())
			cumulativeCaptures.put(e.getKey(), cumulativeCaptures.get(e.getKey()) + e.getValue());
		cursor = data.size();
	}

	public Situation previous(){
		if(cursor > 1){
			cursor--;
			return data.get(cursor-1);
		}
		return null;
	}

	public Situation next(){
		if(cursor < data.size()){
			cursor++;
			return data.get(cursor-1);
		}
		return null;
	}

	public Situation current(){
		return cursor > 0 ? data.get(cursor-1) : null;
	}

	public Situation first(){
		cursor = 1;
		return data.get(0);
	}

	public Situation last(){
		cursor = data.size();
		return data.get(cursor-1);
	}

	public int size(){
		return data.size();
	}
	
	public boolean contains(Situation s){
		return data.subList(0, cursor).contains(s);
	}

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

	public HashMap<Character, Integer> getCumulativeCaptures(){
		return new HashMap<Character, Integer>(cumulativeCaptures);
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