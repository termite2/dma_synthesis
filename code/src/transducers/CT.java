package transducers;



import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.nio.cs.ext.DoubleByte.Decoder_EBCDIC;


public class CT{

	protected Collection<Integer> states;
	protected Integer finalState;
	protected Integer initialState;

	protected int totCounters;
	protected Integer maxStateId;

	protected Collection<Integer> variables;
	protected int numberOfCounters;
	
	// Moves are inputs or epsilon
	protected Map<Integer, Collection<SLMove>> slTransitionsFrom;
	protected Map<Integer, Collection<SLMove>> slTransitionsTo;
	
	// Moves are inputs or epsilon
	protected Map<Integer, Collection<TMove>> tTransitionsFrom;
	protected Map<Integer, Collection<TMove>> tTransitionsTo;

	
	Map<Integer, HashSet<Integer>> aliveVars = new HashMap<>();
	Map<Integer, Map<Integer, HashSet<Integer>> > dependenceRel = new HashMap<>();
		
	public Integer stateCount() {
		return states.size();
	}


	protected CT() {
		states = new HashSet<Integer>();
		slTransitionsFrom = new HashMap<Integer, Collection<SLMove>>();
		slTransitionsTo = new HashMap<Integer, Collection<SLMove>>();
		tTransitionsFrom = new HashMap<Integer, Collection<TMove>>();
		tTransitionsTo = new HashMap<Integer, Collection<TMove>>();
		maxStateId = 0;
		initialState = 0;
		aliveVars = new HashMap<Integer, HashSet<Integer>>();
	}

	/*
	 * Create an automaton (removes unreachable states)
	 */
	public static CT MkCT(
			Collection<CTMove> transitions,
			Integer initialState,
			int finalState,	
			Collection<Integer> variables2) {

		CT aut = new CT();

		// Initialize state set
		aut.initialState = initialState;
		aut.states = new HashSet<Integer>();
		aut.states.add(initialState);
		aut.states.add(finalState);
		aut.finalState=finalState;
		aut.variables = variables2;

		for (CTMove t : transitions)
			aut.addTransition(t);

		aut.computeInfluenceRelation();
		return aut;
	}
	
	public boolean[][] getReachabilityRelation(HashSet<Integer> variables){
		//reachability modulo aliveVars
		int n = maxStateId+1;
		//Can go from i to j in k steps
		boolean[][][] rr = new boolean[n][n][n+1]; 
		//Initialize to identity
		for(Integer fr: states)
			for(Integer to: states)
				for(int i = 0;i<n+1;i++)
					rr[fr][to][i]=(fr==to && variables.contains(fr));
		
		for(int i = 1;i<n+1;i++){
			for(Integer fr: states){
				for(Integer to: states){
					if(rr[fr][to][i-1]){
						rr[fr][to][i]=true;
					}else{
						for(SLMove moveFromTo: slTransitionsFrom.get(to)){
							if(variables.contains(moveFromTo.to))
								rr[fr][moveFromTo.to][i]=true;								
						}
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
	
	
	
	private void computeInfluenceRelation(){
		// at each state what variable values still matter
		
		for(Integer state: states){
			aliveVars.put(state, new HashSet<Integer>());
			HashMap<Integer, HashSet<Integer>> m = new HashMap<>();
			for(Integer var: variables)
				m.put(var, new HashSet<>());
			dependenceRel.put(state, m);
		}
		
		for(TMove tmove: getTMoves()){
			Set<Integer> aliveVars = tmove.getAliveVarsAtFrom();
			Map<Integer,HashSet<Integer>> depRel = tmove.getDepRel();
			//If >0 some other transition will compute it
			if(depRel.keySet().size()==0)
				influenceRelRec(aliveVars, depRel, tmove.from, new HashSet<Integer>());			
		}
		
	}
	
	//TODO what if there are multiple transitions? Do variables stay alive?
	private void influenceRelRec(
			Set<Integer> aliveVarsAtCurrState,
			Map<Integer,HashSet<Integer>> dependenceAtState,
			Integer currState,
			Set<Integer> visited		
			){
		//this checks that no variables should be tracked anymore
		if(aliveVarsAtCurrState.isEmpty())
			return;					
		//add alives to set of alive
		aliveVars.get(currState).addAll(aliveVarsAtCurrState);
		
		//Add new deps to already computed
		Map<Integer,HashSet<Integer>> depSoFar =  dependenceRel.get(currState);		
		for(int variable: dependenceAtState.keySet())
			depSoFar.get(variable).addAll(dependenceAtState.get(variable));
		
		if(!visited.contains(currState)){
			visited.add(currState);		
			//SL move don't change anything and propagate to from state
			for(SLMove move: getSLMovesTo(currState))
				influenceRelRec(aliveVarsAtCurrState, dependenceAtState, move.from,visited);				
			
			//TS move have to check what keeps flowing
			for(TMove move: getTMovesTo(currState)){
				HashSet<Integer> bl = move.getBlockedVars();
				HashSet<Integer> aliveAtFrom =  new HashSet<Integer>(aliveVarsAtCurrState);
				aliveAtFrom.removeAll(bl);
				aliveAtFrom.addAll(move.getAliveVarsAtFrom());
				
				Map<Integer,HashSet<Integer>> depAtFrom =  new HashMap<Integer, HashSet<Integer>>();
				Map<Integer,HashSet<Integer>> depOfMove =  move.getDepRel();
				for(int variable: variables){
					HashSet<Integer> depV = new HashSet<>();
					if(dependenceAtState.containsKey(variable)){
						for(int v : dependenceAtState.get(variable))
							if(aliveAtFrom.contains(v))
								depV.add(v);
					}
					if(depOfMove.containsKey(variable))
						depV.addAll(depOfMove.get(variable));
					if(!depV.isEmpty())
						depAtFrom.put(variable, depV);
				}
					
				
				influenceRelRec(aliveAtFrom,depAtFrom,move.from,visited);
			}
		}
	}
	
	public SemiLinearSet solveReachability(){
		//TODO update touched states properly
		HashSet<Integer> alVars = aliveVars.get(finalState);
		
		//Go backwards and remove cycles
		int newInitialState = initialState;
		int newFinal = finalState;
		HashSet<Integer> newVariables = new HashSet<>(variables);
		List<CTMove> transitions = new LinkedList<>();
		int freshName = maxStateId+1;
		
		HashSet<Integer> visited = new HashSet<>();
		LinkedList<Integer> toVisit = new LinkedList<>();
		toVisit.add(finalState);
		//TODO will have to do something about touched states
		HashSet<Integer> touchedStates = new HashSet<>();
		while(!toVisit.isEmpty())
		{
			int currState = toVisit.removeFirst();	
			visited.add(currState);
			//remove loops and replace				
			CT subGraph = extractCycleGraph(currState, null);
			
			SemiLinearSet sumSubGraph = null; //TODO this should be the 0 summary for all vars
			if(subGraph.states.size()==1){
				sumSubGraph = subGraph.solveReachability();
			}
			//Create new state
			int newState = freshName;
			freshName++;
			
			//Edge replacing cycle
			SLMove newMove = new SLMove(newState, currState, sumSubGraph);
			transitions.add(newMove);
			for(CTMove m: getMovesTo(currState)){
				CTMove newM=copyMoveRepToWithSIfS1(m, newState, currState);
				transitions.add(newM);
				toVisit.add(m.from);
			}
		}
		
		CT linear = CT.MkCT(transitions, newInitialState, newFinal, variables);
		
		return linear.solveAcycGraph();
	}
	
	private SemiLinearSet solveAcycGraph(){
		return null;
	}
	
	private CT extractCycleGraph(int state, HashSet<Integer> touchedStates){
		
		HashSet<Integer> alVars =  aliveVars.get(state);
		
		int newInitialState = maxStateId+1;
		int newFinal = state;
		HashSet<Integer> newVariables = new HashSet<>(variables);
		List<CTMove> transitions = new LinkedList<>();
		
		HashSet<Integer> visited = new HashSet<>();
		LinkedList<Integer> toVisit = new LinkedList<>();
		toVisit.add(state);
		while(!toVisit.isEmpty())
		{
			int currState = toVisit.removeFirst();	
			visited.add(currState);
			if(touchedStates.contains(currState)){
				throw new IllegalArgumentException("loops are not in simple"
						+ "form we can't currently solve this instance");
			}
			touchedStates.add(currState);
			
			boolean[][] rr = getReachabilityRelation(alVars);
			for(CTMove m : getMovesFrom(currState)){
				//TODO This part is tricky
				if(rr[m.to][state]){
					if(alVars.contains(m.to)){					
						transitions.add(copyMoveRepFromWithSIfS1(m, newInitialState, state));
					}
				}
			}					
		}
		touchedStates.remove(newInitialState);
		CT summary = CT.MkCT(transitions, newInitialState, newFinal, newVariables);  
		return summary;
	}
	
	public CTMove copyMoveRepFromWithSIfS1(CTMove m, int s, int s1){
		int newFrom = m.from==s1? s : s1; 
		if(m instanceof SLMove){
			SLMove sl = (SLMove) m;
			return new SLMove(newFrom, sl.to, sl.s);
		}
		if(m instanceof TZeroMove){
			TZeroMove sl = (TZeroMove) m;
			return new TZeroMove(newFrom, sl.to, sl.eq0Test);
		}
		if(m instanceof FreeVarMove){
			FreeVarMove sl = (FreeVarMove) m;
			return new FreeVarMove(newFrom, sl.to, sl.geqFV, sl.setFV);
		}
		if(m instanceof SetConstMove){
			SetConstMove sl = (SetConstMove) m;
			return new SetConstMove(newFrom, sl.to, sl.setVars);
		}
		return null;
	}
	
	public CTMove copyMoveRepToWithSIfS1(CTMove m, int s, int s1){
		int newTo = m.to==s1? s : s1; 
		if(m instanceof SLMove){
			SLMove sl = (SLMove) m;
			return new SLMove(sl.from, newTo, sl.s);
		}
		if(m instanceof TZeroMove){
			TZeroMove sl = (TZeroMove) m;
			return new TZeroMove(sl.from, newTo, sl.eq0Test);
		}
		if(m instanceof FreeVarMove){
			FreeVarMove sl = (FreeVarMove) m;
			return new FreeVarMove(sl.from, newTo, sl.geqFV, sl.setFV);
		}
		if(m instanceof SetConstMove){
			SetConstMove sl = (SetConstMove) m;
			return new SetConstMove(sl.from, newTo, sl.setVars);
		}
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
			getTMovesFrom(transition.from).add((TMove)transition);
			getTMovesTo(transition.to).add((TMove)transition);
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
	public Collection<SLMove> getTMovesTo(
			Collection<Integer> stateSet) {
		Collection<SLMove> transitions = new LinkedList<SLMove>();
		for (Integer state : stateSet)
			transitions.addAll(getSLMovesTo(state));
		return transitions;
	}
	
	/**
	 * Returns the set of transitions from state <code>s</code>
	 */
	public Collection<TMove> getTMovesFrom(Integer state) {
		Collection<TMove> trset = tTransitionsFrom.get(state);
		if (trset == null) {
			trset = new HashSet<TMove>();
			tTransitionsFrom.put(state, trset);
		}
		return trset;
	}

	/**
	 * Returns the set of transitions starting set of states
	 */
	public Collection<TMove> getTMovesFrom(
			Collection<Integer> stateSet) {
		Collection<TMove> transitions = new LinkedList<TMove>();
		for (Integer state : stateSet)
			transitions.addAll(getTMovesFrom(state));
		return transitions;
	}

	/**
	 * Returns the set of input transitions to state <code>s</code>
	 */
	public Collection<TMove> getTMovesTo(Integer state) {
		Collection<TMove> trset = tTransitionsTo.get(state);
		if (trset == null) {
			trset = new HashSet<TMove>();
			tTransitionsTo.put(state, trset);
		}
		return trset;
	}	
	
	public Collection<TMove> getTMoves() {
		return getTMovesFrom(states);
	}
	
	public Collection<CTMove> getMovesTo(int state) {
		Collection<CTMove> trset = new LinkedList<>();
		for(TMove m: getTMovesTo(state))
			trset.add(m);
		for(SLMove m: getSLMovesTo(state))
			trset.add(m);
		return trset;
	}
	
	public Collection<CTMove> getMovesFrom(int state) {
		Collection<CTMove> trset = new LinkedList<>();
		for(TMove m: getTMovesFrom(state))
			trset.add(m);
		for(SLMove m: getSLMovesFrom(state))
			trset.add(m);
		return trset;
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