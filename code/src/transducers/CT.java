package transducers;



import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CT{

	protected Collection<Integer> states;
	protected Collection<Integer> finalStates;
	protected Integer initialState;

	protected int totCounters;
	protected Integer maxStateId;

	protected Collection<Integer> variables;
	protected int numberOfCounters;
	
	// Moves are inputs or epsilon
	protected Map<Integer, Collection<SLMove>> slTransitionsFrom;
	protected Map<Integer, Collection<SLMove>> slTransitionsTo;
	
	// Moves are inputs or epsilon
	protected Map<Integer, Collection<TSMove>> tsTransitionsFrom;
	protected Map<Integer, Collection<TSMove>> tsTransitionsTo;

	
	Map<Integer, HashSet<Integer>> influenceRel = new HashMap<>();
		
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
		influenceRel = new HashMap<Integer, HashSet<Integer>>();
	}

	/*
	 * Create an automaton (removes unreachable states)
	 */
	public static CT MkCT(
			Collection<CTMove> transitions,
			Integer initialState,
			Collection<Integer> finalStates,	
			HashSet<Integer> variables) {

		CT aut = new CT();

		// Initialize state set
		aut.initialState = initialState;
		aut.states = new HashSet<Integer>();
		aut.states.add(initialState);
		aut.states.addAll(finalStates);
		aut.finalStates=finalStates;
		aut.variables = variables;

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
	
	public void getInfluenceRelation(){
		// at each state what variable values still matter
		
		for(Integer state: states)
			influenceRel.put(state, new HashSet<Integer>());
		
		for(TSMove tsmove: getTSMoves()){
			Set<Integer> aliveVars = tsmove.getAliveVarsAtFrom();			
			influenceRelRec(aliveVars, tsmove.from, new HashSet<Integer>());			
		}
		
	}
	
	private void influenceRelRec(
			Set<Integer> aliveVarsAtCurrState,
			Integer currState,
			Set<Integer> visited		
			){
		if(aliveVarsAtCurrState.isEmpty())
			return;
		influenceRel.get(currState).addAll(aliveVarsAtCurrState);
		if(!visited.contains(currState)){
			visited.add(currState);		
			//SL move don't change much
			for(SLMove move: getSLMovesTo(currState))
				influenceRelRec(aliveVarsAtCurrState,move.from,visited);
			//TS move have to check what keeps flowing
			for(TSMove move: getTSMovesTo(currState)){
				HashSet<Integer> bl = move.getBlockedVars();
				HashSet<Integer> rem =  new HashSet<Integer>(aliveVarsAtCurrState);
				rem.removeAll(bl);
				influenceRelRec(rem,move.from,visited);
			}
		}
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
	public Collection<SLMove> getSLMovesTo(
			Collection<Integer> stateSet) {
		Collection<SLMove> transitions = new LinkedList<SLMove>();
		for (Integer state : stateSet)
			transitions.addAll(getSLMovesTo(state));
		return transitions;
	}

	public Collection<SLMove> getSLMoves() {
		return getSLMovesFrom(states);
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
	
	public Collection<TSMove> getTSMoves() {
		return getTSMovesFrom(states);
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