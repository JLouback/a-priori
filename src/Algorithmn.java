import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;

public class Algorithmn {

	private String data;		/* The data file */
	private int num_t;			/* The # of transactions */
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
		while (it.hasNext()) {
			Itemset p = it.next();
			Iterator<Itemset> it2 = itemsets.tailSet(p).iterator();
			while (it2.hasNext()) {
				Itemset q = it2.next();
				if (validJoin(p, q))
					candidates.add(new Itemset(p, q));
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
		
		/* Create the hashset for the transaction's items */
		for (String item : transaction.split(","))
			items.add(item);
		
		/* Get the candidates supported by this transaction */
		for (Itemset itemset : candidates)
			if (itemsContainsItemset(items, itemset))
				candidates_t.add(itemset);
		
		return candidates_t;
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
		    		out.println(itemset.items.toString() + ", " + (int)((float)itemset.support / num_t * 100) + "%");

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
	private ArrayList<Rule> getRules(ArrayList<TreeSet<Itemset>> L, RandomAccessFile raf) throws IOException {
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
		RandomAccessFile raf = new RandomAccessFile(this.data, "r");
		TreeSet<Itemset> previous;
		
		L.add(firstItemset());
		previous = L.get(0);
		for (int k = 2; !previous.isEmpty(); k++) {
			TreeSet<Itemset> candidates = aprioriGen(previous, k);
			TreeSet<Itemset> survivors = new TreeSet<Itemset>();
			
			/* For each transaction, increment the support for itemsets that are a subset */
			String transaction = null;
			raf.seek(0);
			while ((transaction = raf.readLine()) != null) {
				TreeSet<Itemset> candidates_t = candidatesInTransaction(candidates, transaction);
				for (Itemset itemset : candidates_t)
					itemset.support++;
			}
			
			/* Only keep candidates that meet the support threshold */
			for (Itemset itemset : candidates)
				if ((float)itemset.support / this.num_t >= this.min_sup)
					survivors.add(itemset);
			
			L.add(survivors);
			previous = survivors;
		}
		writeFrequentItemsets(L);
		
		ArrayList<Rule> rules = getRules(L, raf);
		System.out.println(rules);
		raf.close();
		
		System.out.println("Done");
	}
}
