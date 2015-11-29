import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

public class Utils {
	
	public static double getInvertedBitSets(String data, Map<String, BitSet> invertedBitSets) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));
		String line = null;
		int i = 0;
		
		while ((line = br.readLine()) != null) {
			String[] terms = line.split(",");
			for (String str : terms) {
				BitSet bs = invertedBitSets.get(str);
				if (bs == null) {
					bs = new BitSet();
					invertedBitSets.put(str, bs);
				}
				bs.set(i);
			}
			i++;
		}
		br.close();
		
		return (double) i;
	}
	
	/*
	 * @param data: The filename with the data
	 * @param itemset: An ArrayList with items in a given itemset
	 * @return count: The number of occurrences of transactions containing all items in data. 
	 * 
	 * For each comma separated term in the file data, verify the occurrence of all elements
	 * in itemset, return total occurrences.
	 */
	public static float itemsetSupport(String data, ArrayList<String> itemset) {
		int count = 0;
		int total = 0;
		String line = null;
		boolean containsAll;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));
			while ((line = br.readLine()) != null)  {
				containsAll = true;
				for (String item : itemset) {
					if (!line.contains(item)) {
						containsAll = false;
					}
				}
				if (containsAll)
					count++;
				total++;
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return count/(float) total;
	}
}
