import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class Algorithmn {

	private String data;		// The data file
	private int num_t;			// The # of transactions
	private float min_sup;		// The minimum support threshold
	private float min_conf;		// The minimum confidence threshold
	
	public Algorithmn(String data, float min_sup, float min_conf) {
		this.data = data;
		this.min_sup = min_sup;
		this.min_conf = min_conf;
	}
	
	public boolean validJoin(Itemset p, Itemset q, int k) {
		// Check if first k-2 elements are the same;
		for (int i=0; i<k-1; i++) {
			if (!p.items.get(i).equals(q.items.get(i))) return false;
		}
		// Check if p.k-1 < q.k-1 lexicographically.
		if (p.items.get(k-2).compareTo(q.items.get(k-2)) > 0) return false;
		return true;
	}
	
	public TreeSet<Itemset> join(TreeSet<Itemset> itemsets, int k) {
		TreeSet<Itemset> candidates = new TreeSet<Itemset>();
		Itemset candidate;
		Iterator<Itemset> it = itemsets.iterator(); 
		while (it.hasNext()) {
			Itemset p = it.next();
			Iterator<Itemset> it2 = itemsets.tailSet(p).iterator();
			while (it2.hasNext()) {
				Itemset q = it2.next();
				if (validJoin(p, q, k)) {
					candidate = p;
					candidate.items.add(q.items.get(k-2));
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}
	
	public boolean containsSubsets(TreeSet<Itemset> itemsets, Itemset candidate, int k) {
		
		return false;
	}
	
	public TreeSet<Itemset> prune(TreeSet<Itemset> itemsets, TreeSet<Itemset>candidates, int k) {
		for (Itemset candidate : candidates) {
			if (!containsSubsets(itemsets, candidate, k)) {
				candidates.remove(candidate);
			}
		}
		return candidates;
	}
	
	public TreeSet<Itemset> aprioriGen(TreeSet<Itemset> itemsets, int k) {
		TreeSet<Itemset> candidates = join(itemsets, k);
		return prune(itemsets, candidates, k);
	}
	
	private TreeSet<Itemset> firstItemset() throws IOException {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		TreeSet<Itemset> singlesets = new TreeSet<Itemset>();
		
		this.num_t = Utils.termCount(data, counts);
		for (String term : counts.keySet())
			if ((float)counts.get(term) / this.num_t >= min_sup)
				singlesets.add(new Itemset(term, counts.get(term)));
		
		return singlesets;
	}
	
	public TreeSet<Itemset> updateCounts(TreeSet<Itemset> candidates) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));  
		String line = null; 
		while ((line = br.readLine()) != null)  {
			
		}
		return null;
	}
	
	public TreeSet<Itemset> pruneBySupport(TreeSet<Itemset> candidates) {
		for (Itemset candidate : candidates) {
			if (candidate.support < min_sup) candidates.remove(candidate);
		}
		return candidates;
	}
	
	public void write(TreeSet<Itemset> itemsets) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("outptu.txt", true)));
		    for (Itemset set : itemsets) {
		    	out.println(set.items.toString());
		    }
		    out.close();
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}

	public void execute() throws IOException{
		TreeSet<Itemset> L1 = firstItemset();
		
		/* Begin Debugging - shows items in L1 */
		for (Itemset item: L1)
			System.out.println(item);
		/* End Debugging */
		
		TreeSet<Itemset> previous = L1;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));  
		String line = null;  
		for (int k=2; !previous.isEmpty(); k++) {
			TreeSet<Itemset> candidates = aprioriGen(previous, k);
			candidates = updateCounts(candidates);
			candidates = pruneBySupport(candidates);
			write(candidates);
			previous = candidates;
		}
	}
}
