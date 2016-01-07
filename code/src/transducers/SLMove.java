package transducers;

public class SLMove extends CTMove{

	TestVar tv;
	SetVar sv;

	public SLMove(int from, int to, TestVar tv, SetVar sv) {
		super(from,to);
		this.tv = tv;
		this.sv = sv;
	}


	@Override
	public Object clone(){
		//TODO
		return this;
	}

}