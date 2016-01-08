package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import transducers.CT;
import transducers.CTMove;
import transducers.SLMove;
import transducers.TSMove;

public class TestCT {

	@Test
	public void testInfluenceRel() {
				
		int initialState = 1;
		HashSet<Integer> finalStates = new HashSet<>();
		finalStates.add(3);
		
		HashSet<Integer> variables = new HashSet<>();
		variables.add(0);
		variables.add(1);
		variables.add(2);
		
		HashSet<Integer> test0em = new HashSet<>();
		HashSet<Integer> test0x0 = new HashSet<>();
		test0x0.add(0);
		HashSet<Integer> test0x2 = new HashSet<>();
		test0x2.add(2);
		
		HashMap<Integer,Integer> testmapem = new HashMap<>();
		HashMap<Integer,Integer> testx1n = new HashMap<>();
		testx1n.put(1, TSMove.freshVar);
		
		HashMap<Integer,Integer> setnone = new HashMap<>();
		HashMap<Integer,Integer> setx0fv = new HashMap<>();
		setx0fv.put(0, TSMove.freshVar);
		HashMap<Integer,Integer> setx1max = new HashMap<>();
		setx1max.put(1, 1200);
		
		HashMap<Integer,Integer> setx2n = new HashMap<>();
		setx2n.put(2, TSMove.freshVar);
		
		List<CTMove> transitions = new LinkedList<>();
		transitions.add(new TSMove(1, 2, test0em, testmapem, setx0fv));
		transitions.add(new TSMove(2, 3, test0x0, testmapem, setnone));
		transitions.add(new TSMove(2, 4, test0em, testmapem, setx1max));
		transitions.add(new SLMove(4, 41, null));
		transitions.add(new TSMove(41, 5, test0em, testx1n, setx2n));
		transitions.add(new SLMove(5, 51, null));
		transitions.add(new TSMove(51, 2, test0x2, testmapem, setnone));
		
		CT ct = CT.MkCT(transitions, initialState, finalStates, variables);
		
		ct.getInfluenceRelation();			
		
		System.out.println("done");
	}

}
