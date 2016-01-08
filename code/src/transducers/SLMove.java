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

	
	@Override
	public String toString(){	
		StringBuilder sb = new StringBuilder();
		sb.append(from);
		sb.append(" -");
		sb.append("SL");			
		sb.append("-> ");
		sb.append(to);
		return sb.toString();
	}
}