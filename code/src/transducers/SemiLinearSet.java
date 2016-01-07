package transducers;

import java.util.*;

public class SemiLinearSet {
	// public class ArrayList<Integer> extends ArrayList<Integer> {}
	//public class LinearSet extends ArrayList<ArrayList<Integer>> {}

	// list of variable names
	List<Integer> vars;

	// list of list of lists representing a set of linear expressions
	// e.g., [[1,2],[3,4]] --> (1,2) + (3,4)
	List<ArrayList<ArrayList<Integer>>> linsets;

	// constructor simply takes the set of variables and the semilinear sets
	public SemiLinearSet(ArrayList<Integer> vars_, ArrayList<ArrayList<ArrayList<Integer>>> linsets_) {
		vars = new ArrayList<Integer>(vars_);
		linsets = new ArrayList<ArrayList<ArrayList<Integer>>>(linsets_);
	}

	// empty constructor
	public SemiLinearSet() {
		vars = new ArrayList<Integer>();
		linsets = new ArrayList<ArrayList<ArrayList<Integer>>>();
	}

	// projects out variable v
	public void project(Integer var) {
		Integer i = vars.indexOf(var);
		vars.remove(i);

		for (ArrayList<ArrayList<Integer>> l : linsets) {
			for (ArrayList<Integer> v : l)
				v.remove(i);
		}
	}

	public void union (SemiLinearSet s){
		assert s.vars.equals(this.vars);
		this.linsets.addAll(s.linsets);
		
	}
	public void addVar(Integer var) {
		vars.add(var);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Variables: ");
		sb.append(vars.toString() + "\n");
		for (ArrayList<ArrayList<Integer>> l : linsets) {
			for (ArrayList<Integer> v : l) {
				sb.append(v.toString());
				sb.append(" + ");
			}

			if (l.size() > 0)
				sb.delete(sb.length() - 3, sb.length() - 1);

			sb.append("\n");
		}

		return sb.toString();
	}

}
