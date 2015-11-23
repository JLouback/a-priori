import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	public static int termCount(String data, HashMap<String, Integer> counts) throws IOException {
		int numTransactions = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));
		String line = null;

		while ((line = br.readLine()) != null)  {
			numTransactions++;
			String[] terms = line.split(",");
			for (String term : terms) {
				if (counts.containsKey(term)) {
					counts.put(term, counts.get(term) + 1);
				} else {
					counts.put(term, 1);
				}
			}
		}
		br.close();

		return numTransactions;
	}
}
