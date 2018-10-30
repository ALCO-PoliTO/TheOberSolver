import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.chocosolver.solver.exception.ContradictionException;
import ilog.concert.IloException;

public class QuickTests {

	public static void main(String[] args) throws ErrorThrower, IloException, IOException, ContradictionException {

		System.out.println("Quick Tests");
		Partition prt = new Partition(45, 3);
		ArrayList<ArrayList<Integer>> tables = new ArrayList<ArrayList<Integer>>();
		tables = prt.loadPartition();
		int cnt = 0, cntI = 0;
		for (int j = 0; j < tables.size(); j++) {
			ArrayList<Integer> tcopy = new ArrayList<Integer>(tables.get(j));
			Boolean Verbose = false;
			Boolean ExportModels = false;
			Boolean Choco = false;
			Boolean TimeLimit = true;
			int SolLimit = 1;
			String Path = "";
			DecimalFormat df = new DecimalFormat("0.0000");
			TwoRotational instance = new TwoRotational(Verbose, SolLimit, ExportModels, Path, TimeLimit, Choco);
			ArrayList<TwoRRotational_Solution> Solutions = instance.solve(tcopy);
			if (Solutions.size() > 0) {
				for (int i = 0; i < Solutions.size(); i++) {
					System.out.println("Solution for " + Solutions.get(i).getOP_name());
					System.out.println("\tStatus: " + Solutions.get(i).getStatus());
					if (Solutions.get(i).getStatus().equals("Solved"))
						cnt++;
					else
						cntI++;
					System.out.println("\tLabellingTime: " + df.format(Solutions.get(i).getLabellingTime()));
					System.out.println("\tNotes: " + Solutions.get(i).getNotes());
					System.out.println("\tLabels.Size: " + Solutions.get(i).getLabels().length);
					System.out.println("\tSolution: " + Solutions.get(i).getSolution());

				}
			} else {
				System.out.println("No Solution found.");
				cntI++;
			}
		}

		System.out.print("Solved: " + cnt + "\tvs" + tables.size());
		System.out.println("Encountered " + cntI + " fails");
	}

}
