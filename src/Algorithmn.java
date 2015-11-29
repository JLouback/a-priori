import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.ArrayList;

public class Algorithmn {
	/*
	 * A map of basket items to a bit vector representing the transactions
	 * that item is present in.
	 */
	private Map<String, BitSet> invertedBitSets;
	private final String kOutputFile = "output.txt";
	
	private double num_trans;   /* The number of transactions */
	private float min_sup;		/* The minimum support threshold */
	private float min_conf;		/* The minimum confidence threshold */
	
	public Algorithmn(String data, float min_sup, float min_conf) throws IOException {
		this.min_sup = min_sup;
		this.min_conf = min_conf;
		this.invertedBitSets = new HashMap<String, BitSet>();
		this.num_trans = Utils.getInvertedBitSets(data, this.invertedBitSets);
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
	
	private float getItemsetSupport(ArrayList<String> items) {
		BitSet union = new BitSet((int) this.num_trans);
		union.set(0, (int)this.num_trans);
		
		for (String str : items)
			union.and(this.invertedBitSets.get(str));
		
		return (float) (union.cardinality() / this.num_trans);
	}
	
	private TreeSet<Itemset> join(TreeSet<Itemset> itemsets) {
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
					candidate.support = getItemsetSupport(candidate.items);
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
	
	private HashMap<String, Float> singleTermsSupport() {
		HashMap<String, Float> supports = new HashMap<String, Float>();
		
		for (String str : invertedBitSets.keySet())
			supports.put(str, (float)(invertedBitSets.get(str).cardinality() / this.num_trans));
		
		return supports;
	}
	
	/*
	 * Return the first itemset for the algorithm
	 */
	private TreeSet<Itemset> firstItemset() throws IOException {
		HashMap<String, Float> supports = singleTermsSupport();
		TreeSet<Itemset> singlesets = new TreeSet<Itemset>();
		
		for (String term : supports.keySet())
			if (supports.get(term) >= min_sup)
				singlesets.add(new Itemset(term, supports.get(term)));
		
		return singlesets;
	}
	
	/*
	 * Writes the frequent itemsets section of the output
	 */
	public void writeFrequentItemsets(ArrayList<TreeSet<Itemset>> L) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(kOutputFile, true)));
		    
		    out.println("==Frequent itemsets (min_sup=" + (int)(min_sup*100) + "%)");
		    for (TreeSet<Itemset> treeSet : L)
		    	for (Itemset itemset : treeSet)
		    		out.println(itemset.items.toString() + ", " + itemset.support*100 + "%");

		    out.println();
		    out.close();
		} catch (IOException e) {
		}
	}
	
	public void writeRules(ArrayList<Rule> rules) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(kOutputFile, true)));
		    
		    out.println("==High-confidence association rules (min_conf=" + (int)(min_sup*100) + "%)");
		    for (Rule rule : rules)
		    	out.println(rule);

		    out.println();
		    out.close();
		} catch (IOException e) {
		}
	}
	
	/*
	 * @param rules: A list to add rules to as they are discovered
	 * @param itemset: The frequent itemset we are using to discover rules
	 * @param raf: The file with the dataset
	 */
	private void addConfidentRules(ArrayList<Rule> rules, Itemset itemset) {
		if (itemset.items.size() <= 1)
			return;
		
		ArrayList<String> lhs = new ArrayList<String>(itemset.items);
		for (int i = 0; i < itemset.items.size(); i++) {
			String rhs = lhs.remove(i);
			float numSupporting = 0;
			float total = 0;
			BitSet lhs_bs = new BitSet((int)num_trans);
			BitSet rhs_bs = new BitSet((int)num_trans);
			
			/* Initialize the bit sets */
			lhs_bs.set(0, (int)num_trans);
			rhs_bs.or(invertedBitSets.get(rhs));
			
			for (String str : lhs)
				lhs_bs.and(invertedBitSets.get(str));
			
			total = lhs_bs.cardinality();
			lhs_bs.and(rhs_bs);
			numSupporting = lhs_bs.cardinality(); 
			
			if (numSupporting / total >= this.min_conf)
				rules.add(new Rule(lhs, rhs));
			
			lhs.add(i, rhs);
		}
	}
	
	/*
	 * @param L: The levels from the iterations when computing the frequent itemsets
	 * 
	 * Returns a list of associative rules >= min_conf
	 */
	private ArrayList<Rule> getRules(ArrayList<TreeSet<Itemset>> L) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		for (TreeSet<Itemset> treeset : L)
			for (Itemset itemset : treeset)
				addConfidentRules(rules, itemset);
		
		return rules;
	}

	/*
	 * Follows the main algorithm of Section 2.1 in the referenced paper
	 */
	public void execute() throws IOException{
		/* Delete old output file if it exists */
		File oldOutput = new File(kOutputFile);
		if (oldOutput.exists())
			oldOutput.delete();
		
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
		writeRules(rules);
	}
}
