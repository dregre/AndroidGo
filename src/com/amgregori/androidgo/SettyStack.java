package com.amgregori.androidgo;

import java.util.ArrayList;
import java.util.Collection;

public class SettyStack<V> extends ArrayList<V> implements Stack<V>{
	
	SettyStack(){
		super();
	}

	SettyStack(Collection<? extends V> c){
		super(c);
	}
	
	public V push(V value){
		if(add(value))
			return value;
		return null;
	}
	
	public V getLast(){
		if(size() < 1)
			return null;
		return get(size()-1);		
	}
	
	public V getSecondToLast(){
		if(size() < 2)
			return null;
		return get(size()-2);		
	}

	public V pop(){
		if(size() == 0)
			return null;
		return super.remove(size()-1);
	}
}