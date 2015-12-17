package transducers;

public abstract class CTMove {

	int from, to;	
	
	public CTMove(Integer from, Integer to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public abstract Object clone();

}