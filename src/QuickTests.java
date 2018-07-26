import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.chocosolver.solver.exception.ContradictionException;
import ilog.concert.IloException;

public class QuickTests {

	public static void main(String[] args) throws ErrorThrower, IloException, IOException, ContradictionException {

		System.out.println("Quick Tests");
		ArrayList<Integer> tables = new ArrayList<Integer>();
		tables.add(3);
		tables.add(3);
		tables.add(3);
		tables.add(4);
		Boolean Verbose = false;
		Boolean ExportModels = false;
		Boolean Choco = false;
		Boolean TimeLimit = false;
		int SolLimit = 1;
		String Path = "";
		DecimalFormat df = new DecimalFormat("0.0000");
		OneRotational instance = new OneRotational(Verbose, SolLimit, ExportModels, Path, TimeLimit,
				Choco);
		ArrayList<OneRotational_Solution> Solutions = instance.solve(tables);
		if (Solutions.size() > 0) {
			for (int i = 0; i < Solutions.size(); i++) {
				OneRotational_SolutionM2 Soluzionen = new OneRotational_SolutionM2(Solutions.get(i));
				System.out.println("Solution for " + Soluzionen.getOP_name());
				System.out.println("\tMinimal Problem: " + Soluzionen.getOP_nameRed());
				System.out.println("\tStatus: " + Soluzionen.getStatus());
				System.out.println("\tLabellingTime: " + df.format(Soluzionen.getLabellingTime()));
				System.out.println("\tUsingMIP: " + Soluzionen.getMIP());
				System.out.println("\tLabels.Size: " + Soluzionen.getLabels().length);
				System.out.println("\tSolution: "+Soluzionen.getSolution());
				System.out.println("\tVerification: "+Soluzionen.verify());
			}
		} else {
			System.out.println("No Solution found.");
		}
	}

}
