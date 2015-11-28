import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

	/*
	 * @param data: The filename with the data
	 * @param counts: A hash map for the term=>count data
	 * @return numTransactions: The number of transactions in the file
	 * 
	 * For each comma separated term in the file data, populate counts with the number of
	 * times that it occurs. Return the total number of transactions in data.
	 */
	public static void termCount(String data, HashMap<String, Float> counts) throws IOException {
		float numTransactions = (float)0.0;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));
		String line = null;

		while ((line = br.readLine()) != null)  {
			numTransactions++;
			String[] terms = line.split(",");
			for (String term : terms) {
				if (counts.containsKey(term)) {
					counts.put(term, counts.get(term) + 1);
				} else {
					counts.put(term, (float)1.0);
				}
			}
		}
		for (String term : counts.keySet()) {
			counts.put(term, (counts.get(term)/numTransactions));
		}
		br.close();
	}
	
	/*
	 * @param data: The filename with the data
	 * @param itemset: An ArrayList with items in a given itemset
	 * @return count: The number of occurrences of transactions containing all items in data. 
	 * 
	 * For each comma separated term in the file data, verify the occurrence of all elements
	 * in itemset, return total occurrences.
	 */
	public static float itemsetCount(String data, ArrayList<String> itemset) {
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
