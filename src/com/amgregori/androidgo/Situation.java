package com.amgregori.androidgo;

public class Situation {
	String position;
	char turn;
	
	Situation(String position, char turn){
		this.position = position;
		this.turn = turn;
	}
	
	public String getPosition(){
		return position;
	}
	
	public char getTurn(){
		return turn;
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
}
