import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;

public class Algorithmn {

	private String data;		/* The data file */
	private float min_sup;		/* The minimum support threshold */
	private float min_conf;		/* The minimum confidence threshold */
	
	public Algorithmn(String data, float min_sup, float min_conf) {
		this.data = data;
		this.min_sup = min_sup;
		this.min_conf = min_conf;
	}
	
	public boolean validJoin(Itemset p, Itemset q) {
		int i;
		
		// Check if first k-2 elements are the same;
		for (i = 0; i < p.items.size()-1; i++)
			if (!p.items.get(i).equals(q.items.get(i))) 
				return false;
		
		// Check if p.k-1 < q.k-1 lexicographically.
		if (p.items.get(i).compareTo(q.items.get(i)) >= 0) 
			return false;
		
		return true;
	}
	
	public TreeSet<Itemset> join(TreeSet<Itemset> itemsets) {
		TreeSet<Itemset> candidates = new TreeSet<Itemset>();
		Iterator<Itemset> it = itemsets.iterator(); 
		Itemset candidate = null;

		while (it.hasNext()) {
			Itemset p = it.next();
			Iterator<Itemset> it2 = itemsets.tailSet(p).iterator();
			while (it2.hasNext()) {
				Itemset q = it2.next();
				if (validJoin(p, q)) {
					candidate = new Itemset(p, q);
					candidate.support = Utils.itemsetSupport(data, candidate.items);
					/* Ignore item sets with support less than required */
					if (candidate.support >= min_sup)
						candidates.add(candidate);
				}
			}
		}

		return candidates;
	}
	
	public boolean containsAllSubsets(TreeSet<Itemset> itemsets, Itemset grown_candidate) {
		for (int i = 0; i < grown_candidate.items.size(); i++) {
			String removed = grown_candidate.items.remove(i);
			if (!itemsets.contains(grown_candidate))
				return false;
			grown_candidate.items.add(i, removed);
		}

		return true;
	}
	
	public TreeSet<Itemset> prune(TreeSet<Itemset> itemsets, TreeSet<Itemset> grown_candidates) {
		TreeSet<Itemset> survivors = new TreeSet<Itemset>();

		for (Itemset grown_candidate : grown_candidates) {
			if (containsAllSubsets(itemsets, grown_candidate)) {
				System.out.println("Survivor: " + grown_candidate);
				survivors.add(grown_candidate);
			}
		}

		return survivors;
	}
	
	public TreeSet<Itemset> aprioriGen(TreeSet<Itemset> itemsets, int k) {
		TreeSet<Itemset> candidates = join(itemsets);
		return prune(itemsets, candidates);
	}
	
	/*
	 * Return the first itemset for the algorithm
	 */
	private TreeSet<Itemset> firstItemset() throws IOException {
		HashMap<String, Float> counts = new HashMap<String, Float>();
		TreeSet<Itemset> singlesets = new TreeSet<Itemset>();
		
		Utils.termCount(data, counts);
		for (String term : counts.keySet())
			if (counts.get(term) >= min_sup)
				singlesets.add(new Itemset(term, counts.get(term)));
		
		return singlesets;
	}
	
	/*
	 * Writes the frequent itemsets section of the output
	 */
	public void writeFrequentItemsets(ArrayList<TreeSet<Itemset>> L) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("output.txt", true)));
		    
		    out.println("==Frequent itemsets (min_sup=" + (int)(min_sup*100) + "%)");
		    for (TreeSet<Itemset> treeSet : L)
		    	for (Itemset itemset : treeSet)
		    		out.println(itemset.items.toString() + ", " + itemset.support*100 + "%");

		    out.close();
		} catch (IOException e) {
		}
	}
	
	/*
	 * @param rules: A list to add rules to as they are discovered
	 * @param itemset: The frequent itemset we are using to discover rules
	 * @param raf: The file with the dataset
	 */
	private void addConfidentRules(ArrayList<Rule> rules, Itemset itemset, RandomAccessFile raf) throws IOException {
		if (itemset.items.size() <= 1)
			return;
		
		ArrayList<String> lhs = new ArrayList<String>(itemset.items);
		for (int i = 0; i < itemset.items.size(); i++) {
			String rhs = lhs.remove(i);
			float numSupporting = 0;
			float total = 0;
			
			String transaction = null;
			raf.seek(0);
			while ((transaction = raf.readLine()) != null) {
				String[] items = transaction.split(",");
				boolean isRelevant = true;
				
				for (String item : lhs) {
					if (Arrays.binarySearch(items, item) < 0) {
						isRelevant = false;
						break;
					}
				}
				
				if (!isRelevant)
					continue;
				
				total++;
				if (Arrays.binarySearch(items, rhs) >= 0)
					numSupporting++;
			}
			
			if (numSupporting / total >= this.min_conf)
				rules.add(new Rule(lhs, rhs));
			
			lhs.add(i, rhs);
		}
	}
	
	/*
	 * @param L: The levels from the iterations when computing the frequent itemsets
	 * @param raf: the file with the dataset
	 * 
	 * Returns a list of associative rules >= min_conf
	 */
	private ArrayList<Rule> getRules(ArrayList<TreeSet<Itemset>> L) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(this.data, "r");
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		for (TreeSet<Itemset> treeset : L)
			for (Itemset itemset : treeset)
				addConfidentRules(rules, itemset, raf);
		
		return rules;
	}

	/*
	 * Follows the main algorithm of Section 2.1 in the referenced paper
	 */
	public void execute() throws IOException{
		ArrayList<TreeSet<Itemset>> L = new ArrayList<TreeSet<Itemset>>();
		TreeSet<Itemset> previous;
		
		L.add(firstItemset());
		previous = L.get(0);
		for (int k = 2; !previous.isEmpty(); k++) {
			TreeSet<Itemset> candidates = aprioriGen(previous, k);
			L.add(candidates);
			previous = candidates;
		}
		writeFrequentItemsets(L);
		
		ArrayList<Rule> rules = getRules(L);
		System.out.println(rules);
		
		System.out.println("Done");
	}
}
