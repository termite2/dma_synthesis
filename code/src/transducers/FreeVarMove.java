package transducers;

import java.util.HashMap;
import java.util.HashSet;

public class FreeVarMove extends TMove{
	
	HashSet<Integer> geqFV;
	HashSet<Integer> setFV;	

	public FreeVarMove(int from, int to, HashSet<Integer> geqFVTest,
			HashSet<Integer> setFVVars) {
		super(from, to);
		this.geqFV = geqFVTest;
		this.setFV = setFVVars;
		int prevsize = geqFVTest.size();
		geqFVTest.removeAll(setFVVars);
		if(geqFVTest.size()!=prevsize)
			throw new IllegalArgumentException("you shouldn't do x>n/x=n");
	}

	// Variables that if alive at TO won't follow through at FROM
	public HashSet<Integer> getBlockedVars(){		
		return setFV;
	}
	
	
	// y in d(x) if y>=f / x=f
	public HashMap<Integer, HashSet<Integer>> getDepRel(){		
		HashMap<Integer, HashSet<Integer>> dep = new HashMap<>();
		
		for(Integer from : setFV)
			dep.put(from,geqFV);
		
		return dep;
	}

	//Variable that matter at from (those tested)
	public HashSet<Integer> getAliveVarsAtFrom(){
		return geqFV;
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
		for(int i:geqFV){
			sb.append("x"+i+">=f");
		}
		sb.append("/");
		for(int i:setFV){
			sb.append("x"+i+":=f");
		}
		sb.append("-> ");
		sb.append(to);
		return sb.toString();
	}
	
}