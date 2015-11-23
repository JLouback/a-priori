import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;

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
	
	/*
	 * Return the first itemset for the algorithm
	 */
	private TreeSet<Itemset> firstItemset() throws IOException {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		TreeSet<Itemset> singlesets = new TreeSet<Itemset>();
		
		this.num_t = Utils.termCount(data, counts);
		for (String term : counts.keySet())
			if ((float)counts.get(term) / this.num_t >= min_sup)
				singlesets.add(new Itemset(term, counts.get(term)));
		
		return singlesets;
	}
	
	/*
	 * @param items: A hashset representing the items for a transaction
	 * @param itemset: The itemset being compared against items
	 * 
	 * Returns true if itemset.items is a subset of items, false otherwise.
	 */
	private boolean itemsContainsItemset(HashSet<String>items, Itemset itemset) {
		for (String item: itemset.items)
			if (!items.contains(item))
				return false;
		
		return true;
	}
	
	/*
	 * @param transaction: A string representing the items in the transaction "A,B,C"
	 * @param candidates: Candidate itemsets
	 * 
	 * Returns the candidate itemsets with items that are a subset of the items represented by transaction
	 */
	private TreeSet<Itemset> candidatesInTransaction(TreeSet<Itemset> candidates, String transaction) {
		TreeSet<Itemset> candidates_t = new TreeSet<Itemset>();
		HashSet<String> items = new HashSet<String>();
		
		// Create the hashset for the transaction's items
		for (String item : transaction.split(","))
			items.add(item);
		
		// Get the candidates supported by this transaction
		for (Itemset itemset : candidates)
			if (itemsContainsItemset(items, itemset))
				candidates_t.add(itemset);
		
		return candidates_t;
	}

	public void execute() throws IOException{
		ArrayList<TreeSet<Itemset>> L = new ArrayList<TreeSet<Itemset>>();
		RandomAccessFile raf = new RandomAccessFile(this.data, "r");
		TreeSet<Itemset> previous;
		
		L.add(firstItemset());
		previous = L.get(0);
		for (int k = 2; !previous.isEmpty(); k++) {
			TreeSet<Itemset> candidates = aprioriGen(previous, k);
			TreeSet<Itemset> survivors = new TreeSet<Itemset>();
			
			// For each transaction, increment the support for itemsets that are a subset 
			String transaction = null;
			raf.seek(0);
			while ((transaction = raf.readLine()) != null) {
				TreeSet<Itemset> candidates_t = candidatesInTransaction(candidates, transaction);
				for (Itemset itemset : candidates_t)
					itemset.support++;
			}
			
			// Only keep candidates that meet the support threshold
			for (Itemset itemset : candidates)
				if ((float)itemset.support / this.num_t >= this.min_sup)
					survivors.add(itemset);
			
			L.add(survivors);
			previous = survivors;
			
//			write(candidates);
		}
		
		raf.close();
	}
}
