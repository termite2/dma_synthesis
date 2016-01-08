package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import transducers.CT;
import transducers.CTMove;
import transducers.SLMove;
import transducers.SetConstMove;
import transducers.FreeVarMove;
import transducers.TZeroMove;

public class TestCT {

	@Test
	public void testInfluenceRel() {
				
		int initialState = 1;
		int finalState=3;
		
		HashSet<Integer> variables = new HashSet<>();
		variables.add(0);
		variables.add(1);
		variables.add(2);
		
		HashSet<Integer> empHS = new HashSet<>();
		HashSet<Integer> test0x0 = new HashSet<>();
		test0x0.add(0);
		HashSet<Integer> test0x2 = new HashSet<>();
		test0x2.add(2);
		
		HashSet<Integer> testx1n = new HashSet<Integer>();
		testx1n.add(1);
		
		
		HashSet<Integer>  setx0fv = new HashSet<Integer>();
		setx0fv.add(0);
		HashMap<Integer,Integer>   setx1max = new HashMap<Integer,Integer> ();
		setx1max.put(1,1000);
		
		HashSet<Integer> setx2n = new HashSet<Integer>();
		setx2n.add(2);
		
		List<CTMove> transitions = new LinkedList<>();
		transitions.add(new FreeVarMove(1, 2, empHS, setx0fv));
		transitions.add(new TZeroMove(2, 3, test0x0));
		transitions.add(new SetConstMove(2, 4, setx1max));
		transitions.add(new SLMove(4, 41, null));
		transitions.add(new FreeVarMove(41, 5, testx1n, setx2n));
		transitions.add(new SLMove(5, 51, null));
		transitions.add(new TZeroMove(51, 2, test0x2));
		
		CT ct = CT.MkCT(transitions, initialState, finalState, variables);		
		
		System.out.println("done");
	}

}
