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
		return LHS + " => " + RHS;
	}
}
