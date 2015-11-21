import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Utils {

	public static HashMap<String, Integer> termCount(String data) throws IOException {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));  
		String line = null;  
		while ((line = br.readLine()) != null)  {
			String[] terms = line.split(",");
			for (String term : terms) {
				if (counts.containsKey(term)) {
					counts.put(term, counts.get(term) + 1);
				} else {
					counts.put(term, 1);
				}
			}
		}
		return counts;
	}
}
