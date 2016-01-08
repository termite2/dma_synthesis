package transducers;

import java.util.HashMap;
import java.util.HashSet;

public class SetConstMove extends TMove{

	
	HashMap<Integer,Integer> setVars;	

	public SetConstMove(int from, int to, HashMap<Integer, Integer> setVars) {
		super(from, to);
		this.setVars = setVars;
	}

	// Variables that if alive at TO won't follow through at FROM
	public HashSet<Integer> getBlockedVars(){		
		return new HashSet<Integer>(setVars.keySet());
	}
		
	//
	public HashMap<Integer, HashSet<Integer>> getDepRel(){		
		HashMap<Integer, HashSet<Integer>> dep = new HashMap<>();
		return dep;
	}

	//Variable that matter at from (those tested)
	public HashSet<Integer> getAliveVarsAtFrom(){
		HashSet<Integer> vars = new HashSet<>();
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
		for(int i:setVars.keySet()){
			sb.append("x"+i+":="+setVars.get(i));
		}		
		sb.append("-> ");
		sb.append(to);
		return sb.toString();
	}
	
}