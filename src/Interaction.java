import java.io.IOException;

public class Interaction {

	public static void main (String[] args) throws IOException {
		String data = args[0];
		float min_sup = Float.valueOf(args[1]);
		int min_conf = Integer.valueOf(args[2]);
		
		System.out.println("Data: " + data + "\nmin_sup: " + min_sup + "\nmin_conf: " + min_conf);
		
		Algorithmn apriori = new Algorithmn(data, min_sup, min_conf);
		apriori.execute();
	}

}
