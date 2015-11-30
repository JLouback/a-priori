import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.Map;

public class Utils {
	
	/*
	 *  Populates the bit vectors for each term in the dataset. 
	 *  Returns the number of transactions/rows.
	 */
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
}
