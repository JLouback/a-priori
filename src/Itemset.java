import java.util.ArrayList;

public class Itemset {

	protected ArrayList<String> itemset;
	protected int support;


	public Itemset(String item, int support) {
		this.itemset = new ArrayList<String>();
		this.itemset.add(item);
		this.support = support;
	}

	public void setSupport(int support) {
		this.support = support;
	}
	
}
