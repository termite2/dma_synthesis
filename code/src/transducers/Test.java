package transducers;

import java.util.ArrayList;

import javafx.scene.shape.SVGPath;

public class Test {

	public static void main(String[] args) {
		ArrayList<Integer> v = new ArrayList<Integer>();
		v.add(1);
		v.add(2);
		ArrayList<ArrayList<ArrayList<Integer>>> l = new ArrayList<ArrayList<ArrayList<Integer>>>();
		
		
		ArrayList<ArrayList<Integer>> ll = new ArrayList<ArrayList<Integer>>();
		ll.add(v);
		ll.add(v);
		l.add(ll);
		l.add(ll);
		
		SemiLinearSet s = new SemiLinearSet(v,l);
		SemiLinearSet s2 = new SemiLinearSet(v,l);

		s.union(s2);
		System.out.print(s);
	}

}
