package com.amgregori.androidgo;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayStack<V> implements Stack<V>{
	private ArrayList<V> data;

	ArrayStack(){
		this.data = new ArrayList<V>();
	}

	@Override
	public V getLast(){
		return data.get(data.size() - 1);
	}
	
	@Override
	public V push(V value) {
		if(data.add(value))
			return value;
		return null;
	}
	
	@Override
	public V pop() {
		if(data.size() == 0)
			return null;
		int index = data.size() - 1;
		V value = data.get(data.size() - 1);
		data.remove(index);
		return value;
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public Iterator<V> iterator() {
		return data.iterator();
	}

		
}
