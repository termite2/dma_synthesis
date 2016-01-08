package transducers;

public class SLMove extends CTMove{

	SemiLinearSet s;	
		
	public SLMove(int from, int to, SemiLinearSet s) {
		super(from,to);
		this.s = s;
	}
	

	@Override
	public Object clone(){
		//TODO
		return this;
	}

}