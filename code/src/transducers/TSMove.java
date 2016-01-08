package transducers;

import java.util.HashMap;
import java.util.HashSet;

public class TSMove extends CTMove{

	public static final Integer freshVar=1000;
	
	HashSet<Integer> eq0Test;
	
	// They all map to positive integers for constants and negative integers for var names
	// geqTest(3)=-4  => x3=x4
	HashMap<Integer, Integer> geqTest;
	HashMap<Integer, Integer> setVars;	

	public TSMove(int from, int to, HashSet<Integer> eq0Test, HashMap<Integer, Integer> geqTest,
			HashMap<Integer, Integer> setVars) {
		super(from, to);
		this.eq0Test = eq0Test;
		this.geqTest = geqTest;
		this.setVars = setVars;
	}

	// Variables that if alive at TO won't follow through at FROM
	public HashSet<Integer> getBlockedVars(){		
		HashSet<Integer> testedAgainstF = testedAgainstFresh();
		HashSet<Integer> vars = new HashSet<>();
		for(Integer from : setVars.keySet()){
			Integer to = setVars.get(from);
			if(to<0){
				//x=f and no y.y>=f in guard => add y
				if(testedAgainstF.isEmpty() && to!=-freshVar)
					vars.addAll(testedAgainstF);
			}else{
				vars.add(from);
			}
		}
		return vars;
	}
	
	//Variable that matter at from (those tested)
	public HashSet<Integer> getAliveVarsAtFrom(){
		HashSet<Integer> vars = new HashSet<>(eq0Test);
		vars.addAll(geqTest.keySet());
		return vars;
	}
	
	//Checks whether the fresh variable appears in any test
	public HashSet<Integer> testedAgainstFresh(){
		HashSet<Integer> vars = new HashSet<>();
		for(Integer from : geqTest.keySet()){
			Integer to = geqTest.get(from);
			if(to== -freshVar){
				vars.add(from);
			}				
		}
		return vars;
	}
	
	@Override
	public Object clone(){
		//TODO
		return this;
	}

	@Override
	public String toString(){	
		return from + " -"+ "-"+to;
	}
	
}