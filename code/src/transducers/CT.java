package transducers;



import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


public class CT{

	protected Collection<Integer> states;
	protected Collection<Integer> finalStates;
	protected Integer initialState;

	protected int totCounters;
	protected Integer maxStateId;

	protected Collection<Integer> counters;
	protected int numberOfCounters;
	
	// Moves are inputs or epsilon
	protected Map<Integer, Collection<CTMove>> transitionsFrom;
	protected Map<Integer, Collection<CTMove>> transitionsTo;

	public Integer stateCount() {
		return states.size();
	}

	public Integer transitionCount() {
		return getTransitions().size();
	}

	protected CT() {
		states = new HashSet<Integer>();
		transitionsFrom = new HashMap<Integer, Collection<CTMove>>();
		transitionsTo = new HashMap<Integer, Collection<CTMove>>();
		maxStateId = 0;
		initialState = 0;
	}

	/*
	 * Create an automaton (removes unreachable states)
	 */
	public static CT MkCT(
			Collection<CTMove> transitions, 
			Integer initialState,
			Collection<Integer> finalStates,	
			int numberOfCounters) {

		CT aut = new CT();

		// Initialize state set
		aut.initialState = initialState;
		aut.states = new HashSet<Integer>();
		aut.states.add(initialState);
		aut.states.addAll(finalStates);
		aut.finalStates=finalStates;
		aut.numberOfCounters = numberOfCounters;

		for (CTMove t : transitions)
			aut.addTransition(t);

		return aut;
	}
	
	public boolean[][] getReachabilityRelation(){
		//TODO make sure states are indexed in correct way
		int n = maxStateId+1;
		//Can go from i to j in k steps
		boolean[][][] rr = new boolean[n][n][n+1]; 
		//Initialize to identity
		for(Integer fr: states)
			for(Integer to: states)
				for(int i = 0;i<n+1;i++)
					rr[fr][to][i]=(fr==to);
		
		for(int i = 1;i<n+1;i++){
			for(Integer fr: states){
				for(Integer to: states){
					if(rr[fr][to][i-1]){
						for(CTMove moveFromTo: transitionsFrom.get(to)){
							rr[fr][moveFromTo.to][i]=true;
						}
					}else{
						rr[fr][to][i]=true;
					}					
				}					
			}
		}
		
		boolean[][] rrFinal = new boolean[n][n]; 
		//Initialize to identity
		for(Integer fr: states)
			for(Integer to: states)
				rrFinal[fr][to]=rr[fr][to][n];
		return rrFinal;
	}
	
	public boolean[][] getDecrementReachabilityRelation(){
		//TODO need to figure this one out
		
		return null;
	}
	
	/**
	 * Add Transition
	 */
	private void addTransition(CTMove transition) {

		if (transition.from > maxStateId)
			maxStateId = transition.from;
		if (transition.to > maxStateId)
			maxStateId = transition.to;

		states.add(transition.from);
		states.add(transition.to);

		getMovesFrom(transition.from).add(transition);
		getMovesTo(transition.to).add(transition);
	}

	// ACCESORIES METHODS

	// GET INPUT MOVES

	/**
	 * Returns the set of transitions from state <code>s</code>
	 */
	public Collection<CTMove> getMovesFrom(Integer state) {
		Collection<CTMove> trset = transitionsFrom.get(state);
		if (trset == null) {
			trset = new HashSet<CTMove>();
			transitionsFrom.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions starting set of states
	 */
	public Collection<CTMove> getMovesFrom(
			Collection<Integer> stateSet) {
		Collection<CTMove> transitions = new LinkedList<CTMove>();
		for (Integer state : stateSet)
			transitions.addAll(getMovesFrom(state));
		return transitions;
	}

	/**
	 * Returns the set of input transitions to state <code>s</code>
	 */
	public Collection<CTMove> getMovesTo(Integer state) {
		Collection<CTMove> trset = transitionsTo.get(state);
		if (trset == null) {
			trset = new HashSet<CTMove>();
			transitionsTo.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions to set of states
	 */
	public Collection<CTMove> getMovesTo(
			Collection<Integer> stateSet) {
		Collection<CTMove> transitions = new LinkedList<CTMove>();
		for (Integer state : stateSet)
			transitions.addAll(getMovesTo(state));
		return transitions;
	}

	/**
	 * Returns the set of transitions starting set of states
	 */
	public Collection<CTMove> getTransitions() {
		return getMovesFrom(states);
	}

	@Override
	public Object clone() {
//		SST<P, F, S> cl = new SST<P, F, S>();
//
//		cl.isDeterministic = isDeterministic;
//		cl.isTotal = isTotal;
//		cl.isEmpty = isEmpty;
//		cl.isEpsilonFree = isEpsilonFree;
//
//		cl.maxStateId = maxStateId;
//
//		cl.states = new HashSet<Integer>(states);
//		cl.initialState = initialState;
//
//		cl.transitionsFrom = new HashMap<Integer, Collection<SSTInputMove<P, F, S>>>(
//				transitionsFrom);
//		cl.transitionsTo = new HashMap<Integer, Collection<SSTInputMove<P, F, S>>>(
//				transitionsTo);
//
//		cl.epsTransitionsFrom = new HashMap<Integer, Collection<SSTEpsilon<P, F, S>>>(
//				epsTransitionsFrom);
//		cl.epsTransitionsTo = new HashMap<Integer, Collection<SSTEpsilon<P, F, S>>>(
//				epsTransitionsTo);
//
//		cl.outputFunction = new HashMap<Integer, OutputUpdate<P, F, S>>(
//				outputFunction);
//
//		return cl;
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
//		sb.append(super.toString());
//
//		sb.append("Output Function \n");
//		for (int st : outputFunction.keySet()) {
//			sb.append("F(" + st + ")=" + outputFunction.get(st));
//		}

		return sb.toString();
	}
}