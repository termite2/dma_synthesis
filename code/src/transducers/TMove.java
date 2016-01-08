package transducers;

import java.util.HashMap;
import java.util.HashSet;

public abstract class TMove extends CTMove{
	
	public TMove(int from, int to) {
		super(from, to);
	}

	public abstract HashSet<Integer> getBlockedVars();
	
	public abstract HashMap<Integer, HashSet<Integer>> getDepRel();	
	
	public abstract HashSet<Integer> getAliveVarsAtFrom();
	
}
