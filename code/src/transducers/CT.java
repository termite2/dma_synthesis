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
	protected Map<Integer, Collection<SLMove>> slTransitionsFrom;
	protected Map<Integer, Collection<SLMove>> slTransitionsTo;
	
	// Moves are inputs or epsilon
		protected Map<Integer, Collection<TSMove>> tsTransitionsFrom;
		protected Map<Integer, Collection<TSMove>> tsTransitionsTo;

	public Integer stateCount() {
		return states.size();
	}


	protected CT() {
		states = new HashSet<Integer>();
		slTransitionsFrom = new HashMap<Integer, Collection<SLMove>>();
		slTransitionsTo = new HashMap<Integer, Collection<SLMove>>();
		tsTransitionsFrom = new HashMap<Integer, Collection<TSMove>>();
		tsTransitionsTo = new HashMap<Integer, Collection<TSMove>>();
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
						for(SLMove moveFromTo: slTransitionsFrom.get(to)){
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
		
		if(transition instanceof SLMove){
			getSLMovesFrom(transition.from).add((SLMove)transition);
			getSLMovesTo(transition.to).add((SLMove)transition);
		}
		else{
			getTSMovesFrom(transition.from).add((TSMove)transition);
			getTSMovesTo(transition.to).add((TSMove)transition);
		}
	}

	// ACCESORIES METHODS

	// GET INPUT MOVES

	/**
	 * Returns the set of transitions from state <code>s</code>
	 */
	public Collection<SLMove> getSLMovesFrom(Integer state) {
		Collection<SLMove> trset = slTransitionsFrom.get(state);
		if (trset == null) {
			trset = new HashSet<SLMove>();
			slTransitionsFrom.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions starting set of states
	 */
	public Collection<SLMove> getSLMovesFrom(
			Collection<Integer> stateSet) {
		Collection<SLMove> transitions = new LinkedList<SLMove>();
		for (Integer state : stateSet)
			transitions.addAll(getSLMovesFrom(state));
		return transitions;
	}

	/**
	 * Returns the set of input transitions to state <code>s</code>
	 */
	public Collection<SLMove> getSLMovesTo(Integer state) {
		Collection<SLMove> trset = slTransitionsTo.get(state);
		if (trset == null) {
			trset = new HashSet<SLMove>();
			slTransitionsTo.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions to set of states
	 */
	public Collection<SLMove> getTSMovesTo(
			Collection<Integer> stateSet) {
		Collection<SLMove> transitions = new LinkedList<SLMove>();
		for (Integer state : stateSet)
			transitions.addAll(getSLMovesTo(state));
		return transitions;
	}
	
	/**
	 * Returns the set of transitions from state <code>s</code>
	 */
	public Collection<TSMove> getTSMovesFrom(Integer state) {
		Collection<TSMove> trset = tsTransitionsFrom.get(state);
		if (trset == null) {
			trset = new HashSet<TSMove>();
			tsTransitionsFrom.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions starting set of states
	 */
	public Collection<TSMove> getTSMovesFrom(
			Collection<Integer> stateSet) {
		Collection<TSMove> transitions = new LinkedList<TSMove>();
		for (Integer state : stateSet)
			transitions.addAll(getTSMovesFrom(state));
		return transitions;
	}

	/**
	 * Returns the set of input transitions to state <code>s</code>
	 */
	public Collection<TSMove> getTSMovesTo(Integer state) {
		Collection<TSMove> trset = tsTransitionsTo.get(state);
		if (trset == null) {
			trset = new HashSet<TSMove>();
			tsTransitionsTo.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions to set of states
	 */
	public Collection<SLMove> getSLMovesTo(
			Collection<Integer> stateSet) {
		Collection<SLMove> transitions = new LinkedList<SLMove>();
		for (Integer state : stateSet)
			transitions.addAll(getSLMovesTo(state));
		return transitions;
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