package transducers;

import java.util.HashMap;
import java.util.HashSet;

public class CTMove {

	int from, to;	
	
	String inputConstructor;
	IBPred[] argPredicates;
	
	String outputConstructor;
	IBTerm[] outputArgs;
	
	HashSet<Integer> testZeroCounters; 		// Set of counters that are checked to be equal to 0
	HashSet<Integer> decrementedCounters; 	// Set of counters that are decremented
	HashMap<Integer,ITerm> counterResets;	// Resets performed on counters
	
	public CTMove(int from, int to, String inputConstructor, IBPred[] argPredicates, String outputConstructor,
			IBTerm[] outputArgs, HashSet<Integer> testZeroCounters, HashSet<Integer> decrementedCounters,
			HashMap<Integer, ITerm> counterResets) {
		super();
		this.from = from;
		this.to = to;
		this.inputConstructor = inputConstructor;
		this.argPredicates = argPredicates;
		this.outputConstructor = outputConstructor;
		this.outputArgs = outputArgs;
		this.testZeroCounters = testZeroCounters;
		this.decrementedCounters = decrementedCounters;
		this.counterResets = counterResets;
	}


	public boolean isReset(){
		return !counterResets.isEmpty();
	}
	

	@Override
	public Object clone(){
		//TODO
		return this;
	}

}