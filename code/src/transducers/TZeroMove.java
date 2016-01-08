package transducers;

import java.util.HashMap;
import java.util.HashSet;

public class TZeroMove extends TMove{
	
	HashSet<Integer> eq0Test;

	public TZeroMove(int from, int to, HashSet<Integer> eq0Test) {
		super(from, to);
		this.eq0Test = eq0Test;
	}

	// Variables that if alive at TO won't follow through at FROM
	public HashSet<Integer> getBlockedVars(){		
		return new HashSet<>();
	}
		
	//
	public HashMap<Integer, HashSet<Integer>> getDepRel(){		
		HashMap<Integer, HashSet<Integer>> dep = new HashMap<>();
		return dep;
	}

	//Variable that matter at from (those tested)
	public HashSet<Integer> getAliveVarsAtFrom(){
		HashSet<Integer> vars = new HashSet<>(eq0Test);
		return vars;
	}
	
	@Override
	public Object clone(){
		//TODO
		return this;
	}

	@Override
	public String toString(){	
		StringBuilder sb = new StringBuilder();
		sb.append(from);
		sb.append(" -");
		for(int i:eq0Test){
			sb.append("x"+i+"==0");
		}		
		sb.append("-> ");
		sb.append(to);
		return sb.toString();
	}
	
}