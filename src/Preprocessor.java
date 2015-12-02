/*
 * Preprocessor.java
 * Processes the crime data before passing into the algorithm for creating association rules
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

public class Preprocessor {
	
	public static void processCrimeData() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("last_crime_nyc.csv")));
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("integrated-dataset.csv", true)));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			
			for (int k = 0; k < split.length; k++)
				split[k] = split[k].trim();
			
			Arrays.sort(split);
			
			StringBuilder row = new StringBuilder("");
			for (String word : split) {
			    row.append(word).append(",");
			}
			row.deleteCharAt(row.length()-1);
			out.println(row.toString());
			
		}
		
		out.close();
		br.close();
		
		System.out.println("Done with crime data");
	}

	public static void main(String[] args) throws IOException {
		processCrimeData();
	}
}