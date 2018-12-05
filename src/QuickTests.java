import java.io.IOException;
import java.util.ArrayList;

import org.chocosolver.solver.exception.ContradictionException;
import ilog.concert.IloException;

public class QuickTests {

	public static void main(String[] args) throws ErrorThrower, IloException, IOException, ContradictionException {

		TwoRotationalEven instance_b = new TwoRotationalEven(false, 1, false, "", true, true);
		ArrayList<Integer> table = new ArrayList<Integer>();
		table.add(13);table.add(5);table.add(3);
		ArrayList<TwoRotationalOdd_Solution> Solutions_b = instance_b.solve(table);
		if (Solutions_b.size() > 0) {
			for (int j = 0; j < Solutions_b.size(); j++) {
				System.out.println("Solution for " + Solutions_b.get(j).getOP_name());
				System.out.println("\tStatus: " + Solutions_b.get(j).getStatus());
				System.out.println(
						"\tLabellingTime: " + Solutions_b.get(j).getLabellingTime());
				System.out.println("\tLabels.Size: " + Solutions_b.get(j).getLabels().length);
				System.out.print(Solutions_b.get(j).getSolution());

			}
		} else {
			System.out.println("No sol1");
		}

	}

}
