package com.amgregori.androidgo;

import java.util.Iterator;

public interface Stack<V> extends Iterable<V>{

	public V getLast();
	
	public V push(V value);
	
	public V pop();

	public int size();

	public boolean contains(Object o);

	public Iterator<V> iterator();
	
}
