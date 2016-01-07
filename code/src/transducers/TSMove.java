package transducers;

import java.util.HashMap;
import java.util.HashSet;

public class TSMove extends CTMove{

	Integer freshVar;
	
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

	public HashSet<Integer> get0TestedVars(){
		return eq0Test;
	}
	
	//Checks whether the fresh variable appears in any test
	public boolean freshInLeftHandSide(){
		for(Integer from : geqTest.keySet()){
			Integer to = geqTest.get(from);
			if(to== -freshVar){
				return true;
			}				
		}
		return false;
	}
	
	//Returns variables that are effectively reset by this transition
	// x=5 or
	// x=r but r doesn't appear in a test
	public HashSet<Integer> getResetVars(){
		HashSet<Integer> vars = new HashSet<>();
		for(Integer from : setVars.keySet()){
			Integer to = setVars.get(from);
			if(to<0){
				if(!freshInLeftHandSide())
					vars.add(from);
			}else{
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

}