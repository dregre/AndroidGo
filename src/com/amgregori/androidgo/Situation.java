package com.amgregori.androidgo;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Situation implements Parcelable {
	private final static String CAPTURES = "caps";
	
	String position;
	char turn;
	HashMap<Character, Integer> captures;

	Situation(String position, char turn){
		this(position, turn, new HashMap<Character, Integer>());
	}
	
	Situation(String position, char turn, HashMap<Character, Integer> captures){
		this.position = position;
		this.turn = turn;
		this.captures = new HashMap<Character, Integer>(captures);
	}
	
	public String getPosition(){
		return position;
	}
	
	public char getTurn(){
		return turn;
	}
	
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
