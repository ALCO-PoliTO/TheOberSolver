import java.io.File;
import java.util.ArrayList;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cp.IloCP;

public class OneRotational {

	private static long Clock;
	private static Boolean Verbose = true;
	private static String OP_name = "";
	private static Boolean Check = false;
	private static Boolean exportModels = false;
	private static String FilePath = "";
	private static int V = 0;
	private static int SolLimit = 0;

	private static int getOPsize(ArrayList<Integer> table) throws ErrorThrower {
		int size = 0;
		for (int t = 0; t < table.size(); t++) {
			if (table.get(t) < 2)
				throw new ErrorThrower("Each table must have more than 1 participant");
			size += table.get(t);
		}
		return size;
	}

	public static Boolean generateLabels_CP(OneRotational_Solution Solution) throws IloException {
		IloCP cpx = new IloCP();
		int n = Solution.getV();

		IloIntVar[] A = new IloIntVar[n];
		IloIntVar[] Diff = null;
		A = cpx.intVarArray(n, 0, n - 1, "A");

		if (n % 2 == 1) {
			// n is odd
			if (Verbose)System.out.println("n="+n+" is odd");
			Diff = new IloIntVar[n - 1];
			Diff = cpx.intVarArray(n - 1, 1, n/2, "Diff");
			for (int i = 1; i <= (n - 1)/2; i++) {
				cpx.add(cpx.eq(cpx.count(Diff, i), 2));
				if (Verbose)System.out.println("\t\t"+i+" (2 times)");
			}
		} else {
			// n is even
			if (Verbose)System.out.println("n="+n+" is even");
			Diff = new IloIntVar[(n - 2) + 1];
			Diff = cpx.intVarArray((n - 2) + 1, 1, n/2, "Diff");
			for (int i = 1; i <= (n - 2)/2; i++) {
				cpx.add(cpx.eq(cpx.count(Diff, i), 2));
				if (Verbose)System.out.println("\t\t"+i+" (2 times)");
			}
			cpx.add(cpx.eq(cpx.count(Diff, (n / 2)), 1));
			if (Verbose)System.out.println("\t\t"+(n/2)+" (1 time)");
		}

		cpx.add(cpx.allDiff(A));

		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int cnt = 0;
		for (int t = 0; t < Solution.getTables().size(); t++) {
			for (int i = 0; i < Solution.getTables().get(t) -1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (Verbose)
					System.out.println("Writing constraints for " + alpha + "->" + beta);
				cpx.add(cpx.addEq(Diff[cnt], cpx.min(cpx.abs(cpx.diff(A[alpha], A[beta])),
						cpx.diff(n, cpx.abs(cpx.diff(A[alpha], A[beta]))))));
				cnt++;
			}
			// Close the table
			if (t != 0) {
				alpha = scroll + Solution.getTables().get(t) - 1;
				beta = scroll;
				if (Verbose)
					System.out.println("Writing closing constraints for " + alpha + "->" + beta);
				cpx.add(cpx.addEq(Diff[cnt], cpx.min(cpx.abs(cpx.diff(A[alpha], A[beta])),
						cpx.diff(n, cpx.abs(cpx.diff(A[alpha], A[beta]))))));
				cnt++;
			}
			scroll += Solution.getTables().get(t);
		}

		if (!Verbose)
			cpx.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
		int Tl = V;
		cpx.setParameter(IloCP.DoubleParam.TimeLimit, Tl);
		cpx.propagate();
		if (cpx.solve()) {
			Solution.setLabellingTime(cpx.getInfo(IloCP.DoubleInfo.SolveTime));
			int[] labels = new int[n];
			for (int i = 0; i < n; i++) {
				labels[i] = cpx.getIntValue(A[i]);
				// System.out.println("Node "+i+" is labeled as "+labels[i]);
			}
			Solution.setLabels(labels);
			
			if (exportModels) {
				cpx.exportModel(
						FilePath + "solved/" + "Labelling_" + Solution.getName() + "_" + param_getOP_name() + ".cpo");
			}
			cpx.end();
			return true;
		} else {
			Solution.setStatus("Infeasible");
			if (exportModels) {
				cpx.exportModel(
						FilePath + "infeasibles/" + "Labelling_" + Solution.getName() + "_" + param_getOP_name() + ".cpo");
			}
			cpx.end();
			return false;
		}
	}

	public ArrayList<OneRotational_Solution> solve(ArrayList<Integer> tables) throws ErrorThrower, IloException {
		tables.set(0, tables.get(0) - 1);
		V = getOPsize(tables);
		if (V < 1) {
			throw new ErrorThrower("Less than 3 nodes!");
		}
		
		OneRotational_Solution Solution = null;
		ArrayList<OneRotational_Solution> Solutions = new ArrayList<OneRotational_Solution>();
		param_setOP_name(tables);
		

		int Solve_count = 0;
		boolean Flag = true;
		int iteration = 0;

		while (Flag) {
			Clock = System.nanoTime();
			Solution = new OneRotational_Solution(tables, V);
			Solution.setName("" + iteration);
			Solution.setOP_name(param_getOP_name());
			
			if (generateLabels_CP(Solution)) {
				Solve_count++;
				Solutions.add(Solution);
			} else {
				System.out.println("No solution found with CP.");
				/*
				 * if (generateLabels_MIP(Solution)) { Solve_count++;
				 * System.out.println("Solution found with MIP."); } else {
				 * System.out.println("Proven infeasible with MIP."); }
				 */
				Solution.setStatus("Infeasible");
				Solutions.add(Solution);
			}
			if (SolLimit != 0) {
				if (Solve_count == SolLimit)
					Flag = false;
			}
			Solution.setTotalTime((System.nanoTime() - Clock) / 1000000000F);
			iteration++;
		}
		return Solutions;
	}

	public OneRotational(boolean Verbose, boolean Check, int SolLimit, Boolean exportModels, String FilePath) {
		param_setVerbose(Verbose);
		param_setCheck(Check);
		param_setSolLimit(SolLimit);
		param_setExportModels(exportModels);
		param_setFilePath(FilePath);
		if (exportModels) {
			File f = new File(FilePath);
			f.mkdirs();
			File f2 = new File(FilePath + "solved/");
			f2.mkdirs();
			File f3 = new File(FilePath + "infeasibles/");
			f3.mkdirs();
		}
	}

	public static String param_getOP_name() {
		return OP_name;
	}

	public static void param_setOP_name(ArrayList<Integer> tables) {
		ArrayList<Integer> tcopy = new ArrayList<Integer>(tables);
		tcopy.set(0, tcopy.get(0) + 1);
		String OP = "OP(";
		for (int t = 0; t < tcopy.size(); t++) {
			if (t != (tcopy.size() - 1)) {
				OP += tcopy.get(t) + ",";
			} else
				OP += tcopy.get(t) + ")";

		}
		tcopy.clear();
		OP_name = OP;
	}

	public static Boolean param_getVerbose() {
		return Verbose;
	}

	public static void param_setVerbose(Boolean verbose) {
		Verbose = verbose;
	}

	public static Boolean param_getCheck() {
		return Check;
	}

	public static void param_setCheck(Boolean check) {
		Check = check;
	}

	public static Boolean param_getExportModels() {
		return exportModels;
	}

	public static void param_setExportModels(Boolean exportModels) {
		OneRotational.exportModels = exportModels;
	}

	public static String param_getFilePath() {
		return FilePath;
	}

	public static void param_setFilePath(String filePath) {
		FilePath = filePath;
	}

	public static int param_getSolLimit() {
		return SolLimit;
	}

	public static void param_setSolLimit(int solLimit) {
		SolLimit = solLimit;
	}

	public static int param_getV() {
		return V;
	}

	public static void param_setV(int v) {
		V = v;
	}

}
