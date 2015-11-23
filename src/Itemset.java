import java.util.ArrayList;
import java.util.Collections;

public class Itemset implements Comparable<Object> {

	protected ArrayList<String> items;
	protected int support;

	public Itemset(String item, int support) {
		this.items = new ArrayList<String>();
		this.items.add(item);
		this.support = support;
	}
	
	/*
	 * Create a new, larger itemset from p and q as described by the join algorithm int he paper (section 2.1.1)
	 * The items for p and q are assumed to be sorted already, with the last item of q being lexographically larger
	 * than the last item of p. 
	 */
	public Itemset(Itemset p, Itemset q) {
		this.items = new ArrayList<String>();
		this.items.addAll(p.items);
		this.items.add(q.items.get(q.items.size()-1));
		this.support = 0;
	}

	public void setSupport(int support) {
		this.support = support;
	}
	
	/*
	 * Let's view Itemsets with more items as "greater" than Itemsets with fewer items
	 */
	public int compareTo(Object object) throws ClassCastException {
		if (!(object instanceof Itemset))
			throw new ClassCastException("An Itemset object expected.");
		
		Itemset that = (Itemset)object;
		
		if (this.items.size() != that.items.size())
			throw new IllegalStateException("You should not be comparing itemsets of different sizes.");
		
		for (int i = 0; i < this.items.size(); i++)
			if (!this.items.get(i).equals(that.items.get(i)))
				return this.items.get(i).compareTo(that.items.get(i));
		
		return 0;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(items.get(0));
		for (int i = 1; i < items.size(); i++)
			sb.append(", " + items.get(i));
		
		return "Itemset support: " + support + ", for items: " + sb.toString();
	}
}
