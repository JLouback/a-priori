import java.util.ArrayList;

public class Itemset implements Comparable<Object> {

	protected ArrayList<String> items;
	protected int support;

	public Itemset(String item, int support) {
		this.items = new ArrayList<String>();
		this.items.add(item);
		this.support = support;
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
		if (this.items.size() < that.items.size())
			return -1;
		if (this.items.size() > that.items.size())
			return 1;
		
		for (String item : this.items)
			if (!that.items.contains(item))
				return -1;
		
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
