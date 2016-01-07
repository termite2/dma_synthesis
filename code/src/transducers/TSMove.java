package transducers;

public class TSMove extends CTMove{

	int from, to;		
	SemiLinearSet s;	
		
	public TSMove(int from, int to, SemiLinearSet s) {
		super(from,to);
		this.s = s;
	}
	

	@Override
	public Object clone(){
		//TODO
		return this;
	}

}