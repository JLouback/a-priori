import java.util.ArrayList;

public class Rule {
	ArrayList<String> LHS;
	String RHS;
	float support;
	float confidence;
	
	public Rule(ArrayList<String> LHS, String RHS) {
		this.LHS = new ArrayList<String>(LHS);
		this.RHS = RHS;
	}
	
	public String toString() {
		return String.format("%s => [%s] (Conf: %.2f%%, Supp: %.2f%%", LHS, RHS, confidence*100, support*100);
	}
}
