package com.eng;

import com.altova.TraceTarget;

public class TraceTargetConsole implements TraceTarget {

	public void writeTrace(String info) {
		System.out.println(" INFO " + info);	
	}
	
}
