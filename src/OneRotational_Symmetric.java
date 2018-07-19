import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.chocosolver.parser.json.JSON;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cp.IloCP;

public class OneRotational_Symmetric {

	private static long Clock;
	private static Boolean Verbose = true;
	private static Boolean Choco = true;
	private static String OP_name = "";
	private static Boolean Check = false;
	private static Boolean TimeLimit = false;
	private static Boolean exportModels = false;
	private static String FilePath = "";
	private static int V = 0;
	private static int SolLimit = 0;
	private static Map<Integer, Integer> tableSizes = null;
	private static ArrayList<Integer> tablesCP = null;
	private static ArrayList<Integer> openCycles = null;
	private static ArrayList<Integer> doubleTables = null;
	private static int V_CP = 0;
	private static int D_CP = 0;

	private static int getOPsize(ArrayList<Integer> table) throws ErrorThrower {
		int size = 0;
		for (int t = 0; t < table.size(); t++) {
			if (table.get(t) < 2)
				throw new ErrorThrower("Each table must have more than 1 participant");
			size += table.get(t);
		}
		return size;
	}

	private static Boolean preProcess(OneRotational_SolutionSymmetric Solution) {
		// Decompose the configuration into the minimal one
		// Rest of graph is constructed
		Boolean infiniteTable = false;
		tablesCP = new ArrayList<Integer>();
		int infiniteIndex = 0;

		ArrayList<Integer> tablesWorking = new ArrayList<Integer>(Solution.getTables());
		Map<Integer, Integer> sizesWorking = new HashMap<Integer, Integer>(tableSizes);
		openCycles = new ArrayList<Integer>();
		doubleTables = new ArrayList<Integer>();
		int counter = 0;
		V_CP = 0;
		D_CP = 0;
		for (int t = 0; t < tablesWorking.size(); t++) {
			if (tablesWorking.get(t) % 2 == 1) {
				// Infinite table is the one with odd cardinality
				if (sizesWorking.get(tablesWorking.get(t)) > 0) {
					if (!infiniteTable && sizesWorking.get(tablesWorking.get(t)) % 2 == 1) {
						infiniteIndex = counter;
						tablesCP.add((tablesWorking.get(t) - 1) / 2);
						V_CP += (tablesWorking.get(t) - 1) / 2;
						D_CP += (tablesWorking.get(t) + 1) / 2 - 2;
						sizesWorking.replace(tablesWorking.get(t), sizesWorking.get(tablesWorking.get(t)) - 1);
						infiniteTable = true;
						counter++;
					}
					if (sizesWorking.get(tablesWorking.get(t)) > 1) {
						// Double cycle of odd cardinality
						tablesCP.add(tablesWorking.get(t));
						V_CP += tablesWorking.get(t);
						D_CP += tablesWorking.get(t);
						// Remove the redundant table
						doubleTables.add(counter);
						sizesWorking.replace(tablesWorking.get(t), sizesWorking.get(tablesWorking.get(t)) - 2);
						counter++;
					}
				}
			} else {
				// Even tables
				if (sizesWorking.get(tablesWorking.get(t)) > 1) {
					tablesCP.add(tablesWorking.get(t));
					V_CP += tablesWorking.get(t);
					D_CP += tablesWorking.get(t);
					doubleTables.add(counter);
					sizesWorking.replace(tablesWorking.get(t), sizesWorking.get(tablesWorking.get(t)) - 2);
					counter++;
				}
				if (sizesWorking.get(tablesWorking.get(t)) == 1) {
					// Open cycle
					openCycles.add(counter);
					V_CP += tablesWorking.get(t) / 2;
					D_CP += tablesWorking.get(t) / 2;
					tablesCP.add((tablesWorking.get(t) / 2));
					sizesWorking.replace(tablesWorking.get(t), sizesWorking.get(tablesWorking.get(t)) - 1);
					counter++;
				}
			}
		}
		if (infiniteIndex != 0)
			Collections.swap(tablesCP, 0, infiniteIndex);
		if (openCycles.contains(0)) {
			int criticalIndex = -1;
			for (int t = 0; t < openCycles.size(); t++)
				if (openCycles.get(t) == 0) {
					criticalIndex = t;
					break;
				}
			openCycles.set(criticalIndex, infiniteIndex);
			if (criticalIndex == -1) {
				for (int t = 0; t < doubleTables.size(); t++)
					if (doubleTables.get(t) == 0) {
						criticalIndex = t;
						break;
					}
				doubleTables.set(criticalIndex, infiniteIndex);
			}

		}

		if (Verbose) {
			System.out.println("Total Vertexs to compute: " + V_CP);
			System.out.println("Total Differences to compute: " + D_CP);
			for (int i = 0; i < tablesCP.size(); i++) {
				String flag = "";
				if (openCycles.contains(i))
					flag += "OPEN";
				if (doubleTables.contains(i))
					flag += "DOUBLE";
				System.out.println("Table of " + tablesCP.get(i) + " " + flag);
			}
		}
		return true;
	}

	public static Boolean generateLabels_CP(OneRotational_SolutionSymmetric Solution) throws IloException {

		preProcess(Solution);
		Solution.setTablesRed(tablesCP);

		IloCP cpx = new IloCP();
		int n = (Solution.getV() - 1) / 2;

		IloIntVar[] N = cpx.intVarArray(V_CP, 0, (2 * n - 1), "N");
		IloIntVar[] Diff = cpx.intVarArray(D_CP * 2, 1, 2 * n - 1, "Diff");
		cpx.add(cpx.eq(cpx.count(Diff, n), 0));
		for (int i = 0; i < n; i++) {
			cpx.add(cpx.eq(cpx.sum(cpx.count(N, i), cpx.count(N, i + n)), 1));
		}

		cpx.add(cpx.allDiff(N));
		cpx.add(cpx.allDiff(Diff));

		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int cnt = 0;
		for (int t = 0; t < tablesCP.size(); t++) {
			if (Verbose)
				System.out.println("Table " + t + " with cardinality " + tablesCP.get(t));
			for (int i = 0; i < tablesCP.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (Verbose)
					System.out.println("Writing constraints for " + alpha + "->" + beta);
				cpx.add(cpx.eq(Diff[cnt], cpx.modulo(cpx.sum(cpx.diff(N[alpha], N[beta]), 2 * n), 2 * n)));
				cnt++;
				cpx.add(cpx.eq(Diff[cnt], cpx.modulo(cpx.sum(cpx.diff(N[beta], N[alpha]), 2 * n), 2 * n)));
				cnt++;
			}
			// Close the table
			if (t != 0 && !openCycles.contains(t)) {
				alpha = scroll + tablesCP.get(t) - 1;
				beta = scroll;
				if (Verbose)
					System.out.println("Writing closing constraints for " + alpha + "->" + beta);
				cpx.add(cpx.eq(Diff[cnt], cpx.modulo(cpx.sum(cpx.diff(N[alpha], N[beta]), 2 * n), 2 * n)));
				cnt++;
				cpx.add(cpx.eq(Diff[cnt], cpx.modulo(cpx.sum(cpx.diff(N[beta], N[alpha]), 2 * n), 2 * n)));
				cnt++;
			}
			// Relation between complementary (modulo) label in open cycles
			if (openCycles.contains(t)) {
				cpx.add(cpx.eq(Diff[cnt],
						cpx.modulo(cpx.sum(
								cpx.diff(cpx.modulo(cpx.sum(N[scroll], n), 2 * n), N[scroll + tablesCP.get(t) - 1]),
								2 * n), 2 * n)));
				cnt++;
				cpx.add(cpx.eq(Diff[cnt],
						cpx.modulo(cpx.sum(
								cpx.diff(N[scroll + tablesCP.get(t) - 1], cpx.modulo(cpx.sum(N[scroll], n), 2 * n)),
								2 * n), 2 * n)));
				cnt++;
			}
			scroll += tablesCP.get(t);
		}
		if (!Verbose)
			cpx.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
		int Tl = V;
		if (TimeLimit)
			cpx.setParameter(IloCP.DoubleParam.TimeLimit, Tl);
		if (cpx.propagate()) {
			System.out.println("Popagation sorted effects.");
		}
		// cpx.exportModel("test.cpo");
		if (cpx.solve()) {
			Solution.setLabellingTime(cpx.getInfo(IloCP.DoubleInfo.SolveTime));
			int[] labels = new int[V];
			ArrayList<Integer> table = new ArrayList<Integer>();
			ArrayList<Integer> newTables = new ArrayList<Integer>();
			// Table with infinite
			labels[0] = -1;
			scroll = 1;
			int scroll_sol = 0;
			// System.out.println("--Infinite table");
			for (int j = 0; j < tablesCP.get(0); j++) {
				labels[scroll] = cpx.getIntValue(N[scroll_sol]);
				table.add((labels[scroll] + n) % (2 * n));
				// System.out.println(scroll + " = " + labels[scroll]);
				scroll++;
				scroll_sol++;
			}
			ListIterator<Integer> iter = table.listIterator(table.size());
			while (iter.hasPrevious()) {
				labels[scroll] = iter.previous();
				// System.out.println(scroll + " = " + labels[scroll]);
				scroll++;
			}
			newTables.add(tablesCP.get(0) * 2 + 1);
			table.clear();

			for (int j = 1; j < tablesCP.size(); j++) {
				table.clear();
				if (openCycles.contains(j)) {
					// System.out.println("--Cycle table of "+tablesCP.get(j));
					for (int z = 0; z < tablesCP.get(j); z++) {
						labels[scroll] = cpx.getIntValue(N[scroll_sol]);
						table.add((labels[scroll] + n) % (2 * n));
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
						scroll_sol++;
					}
					for (int z = 0; z < table.size(); z++) {
						labels[scroll] = table.get(z);
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
					}
					newTables.add(tablesCP.get(j) * 2);
				} else if (doubleTables.contains(j)) {
					// System.out.println("--Double table of "+tablesCP.get(j));
					for (int z = 0; z < tablesCP.get(j); z++) {
						labels[scroll] = cpx.getIntValue(N[scroll_sol]);
						table.add((labels[scroll] + n) % (2 * n));
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
						scroll_sol++;
					}
					// System.out.println("--Copy table of "+table.size());
					for (int z = 0; z < table.size(); z++) {
						labels[scroll] = table.get(z);
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
					}
					newTables.add(tablesCP.get(j));
					newTables.add(tablesCP.get(j));
				}

			}
			param_setOP_name(newTables);
			Solution.setOP_name(param_getOP_name());
			Solution.setTables(newTables);
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
				cpx.exportModel(FilePath + "infeasibles/" + "Labelling_" + Solution.getName() + "_" + param_getOP_name()
						+ ".cpo");
			}
			cpx.end();
			return false;
		}

	}

	public static Boolean generateLabels_CP_Choco(OneRotational_SolutionSymmetric Solution)
			throws ContradictionException {

		preProcess(Solution);
		Solution.setTablesRed(tablesCP);

		Model choco = new Model("generateLabels_CP");
		int n = (Solution.getV() - 1) / 2;

		IntVar[] N = choco.intVarArray("N", V_CP, 0, (2 * n - 1));
		IntVar[] Diff = choco.intVarArray("Diff", D_CP * 2, 1, 2 * n - 1);
		IntVar zero = choco.intVar(0);
		IntVar one = choco.intVar(1);

		choco.count(n, Diff, zero).post();
		for (int i = 0; i < n; i++) {
			choco.among(one, N, new int[] { i, (i + n) }).post();
		}

		choco.allDifferent(N).post();
		choco.allDifferent(Diff).post();

		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int cnt = 0;
		for (int t = 0; t < tablesCP.size(); t++) {
			if (Verbose)
				System.out.println("Table " + t + " with cardinality " + tablesCP.get(t));
			for (int i = 0; i < tablesCP.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (Verbose)
					System.out.println("Writing constraints for " + alpha + "->" + beta);
				Diff[cnt].eq(N[alpha].sub(N[beta]).add(2 * n).mod(2 * n)).post();
				cnt++;
				Diff[cnt].eq(N[beta].sub(N[alpha]).add(2 * n).mod(2 * n)).post();
				cnt++;
			}
			// Close the table
			if (t != 0 && !openCycles.contains(t)) {
				alpha = scroll + tablesCP.get(t) - 1;
				beta = scroll;
				if (Verbose)
					System.out.println("Writing closing constraints for " + alpha + "->" + beta);
				Diff[cnt].eq(N[alpha].sub(N[beta]).add(2 * n).mod(2 * n)).post();
				cnt++;
				Diff[cnt].eq(N[beta].sub(N[alpha]).add(2 * n).mod(2 * n)).post();
				cnt++;
			}
			// Relation between complementary (modulo) label in open cycles
			if (openCycles.contains(t)) {
				Diff[cnt].eq((N[scroll].add(n).mod(2 * n)).sub(N[scroll + tablesCP.get(t) - 1]).add(2 * n).mod(2 * n))
						.post();
				cnt++;
				Diff[cnt].eq((N[scroll + tablesCP.get(t) - 1]).sub(N[scroll].add(n).mod(2 * n)).add(2 * n).mod(2 * n))
						.post();
				cnt++;
			}
			scroll += tablesCP.get(t);
		}
		Solver solver = choco.getSolver();
		// JSON.write(choco, new File("chocotest.json"));
		solver.propagate();
		if (Verbose)
			solver.showShortStatistics();
		if (solver.solve()) {
			Solution.setLabellingTime(solver.getTimeCount());
			int[] labels = new int[V];
			ArrayList<Integer> table = new ArrayList<Integer>();
			ArrayList<Integer> newTables = new ArrayList<Integer>();
			// Table with infinite
			labels[0] = -1;
			scroll = 1;
			int scroll_sol = 0;
			// System.out.println("--Infinite table");
			for (int j = 0; j < tablesCP.get(0); j++) {
				labels[scroll] = N[scroll_sol].getValue();
				table.add((labels[scroll] + n) % (2 * n));
				// System.out.println(scroll + " = " + labels[scroll]);
				scroll++;
				scroll_sol++;
			}
			ListIterator<Integer> iter = table.listIterator(table.size());
			while (iter.hasPrevious()) {
				labels[scroll] = iter.previous();
				// System.out.println(scroll + " = " + labels[scroll]);
				scroll++;
			}
			newTables.add(tablesCP.get(0) * 2 + 1);
			table.clear();

			for (int j = 1; j < tablesCP.size(); j++) {
				table.clear();
				if (openCycles.contains(j)) {
					// System.out.println("--Cycle table of "+tablesCP.get(j));
					for (int z = 0; z < tablesCP.get(j); z++) {
						labels[scroll] = N[scroll_sol].getValue();
						table.add((labels[scroll] + n) % (2 * n));
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
						scroll_sol++;
					}
					for (int z = 0; z < table.size(); z++) {
						labels[scroll] = table.get(z);
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
					}
					newTables.add(tablesCP.get(j) * 2);
				} else if (doubleTables.contains(j)) {
					// System.out.println("--Double table of "+tablesCP.get(j));
					for (int z = 0; z < tablesCP.get(j); z++) {
						labels[scroll] = N[scroll_sol].getValue();
						table.add((labels[scroll] + n) % (2 * n));
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
						scroll_sol++;
					}
					// System.out.println("--Copy table of "+table.size());
					for (int z = 0; z < table.size(); z++) {
						labels[scroll] = table.get(z);
						// System.out.println(scroll + " = " + labels[scroll]);
						scroll++;
					}
					newTables.add(tablesCP.get(j));
					newTables.add(tablesCP.get(j));
				}

			}
			param_setOP_name(newTables);
			Solution.setOP_name(param_getOP_name());
			Solution.setTables(newTables);
			Solution.setLabels(labels);

			if (exportModels) {
				JSON.write(choco, new File(
						FilePath + "solved/" + "Labelling_" + Solution.getName() + "_" + param_getOP_name() + ".json"));
			}

			return true;
		} else {
			Solution.setStatus("Infeasible");
			if (exportModels) {
				JSON.write(choco, new File(
						FilePath + "solved/" + "Labelling_" + Solution.getName() + "_" + param_getOP_name() + ".json"));
			}
			return false;
		}

	}

	public ArrayList<OneRotational_SolutionSymmetric> solve(ArrayList<Integer> tables)
			throws ErrorThrower, IloException, ContradictionException {
		V = getOPsize(tables);
		if (V < 1) {
			throw new ErrorThrower("Less than 3 nodes!");
		}
		if ((V % 2) == 0) {
			throw new ErrorThrower("Order of graph must be odd!");
		}

		// Only one table of with odd order
		tableSizes = new HashMap<Integer, Integer>();
		for (int t = 0; t < tables.size(); t++) {
			if (tableSizes.containsKey(tables.get(t)))
				tableSizes.replace(tables.get(t), tableSizes.get(tables.get(t)) + 1);
			else
				tableSizes.put(tables.get(t), 1);
		}
		int checkFlag = 0;
		for (Map.Entry<Integer, Integer> entry : tableSizes.entrySet()) {
			if ((entry.getKey() % 2) == 1) {
				if ((entry.getValue() % 2) == 1) {
					checkFlag++;
				}
			}
		}
		if (checkFlag > 1) {
			throw new ErrorThrower("More than odd table with odd participants.");
		}

		OneRotational_SolutionSymmetric Solution = null;
		ArrayList<OneRotational_SolutionSymmetric> Solutions = new ArrayList<OneRotational_SolutionSymmetric>();
		param_setOP_name(tables);

		int Solve_count = 0;
		boolean Flag = true;
		int iteration = 0;

		while (Flag) {
			Clock = System.nanoTime();
			Solution = new OneRotational_SolutionSymmetric(tables, V);
			Solution.setName("" + iteration);
			Solution.setOP_name(param_getOP_name());
			Boolean res = false;
			if (Choco)
				res = generateLabels_CP_Choco(Solution);
			else
				res = generateLabels_CP(Solution);
			if (res) {
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

	public OneRotational_Symmetric(boolean Verbose, int SolLimit, Boolean exportModels, String FilePath,
			Boolean TimeLimit, Boolean Choco) {
		param_setVerbose(Verbose);
		param_setSolLimit(SolLimit);
		param_setExportModels(exportModels);
		param_setFilePath(FilePath);
		param_setTimeLimit(TimeLimit);
		param_setChoco(Choco);
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

	public static void param_setOP_name(ArrayList<Integer> tcopy) {
		String OP = "OP(";
		for (int t = 0; t < tcopy.size(); t++) {
			if (t != (tcopy.size() - 1)) {
				OP += tcopy.get(t) + ",";
			} else
				OP += tcopy.get(t) + ")";

		}
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
		OneRotational_Symmetric.exportModels = exportModels;
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

	public static Boolean param_getTimeLimit() {
		return TimeLimit;
	}

	public static void param_setTimeLimit(Boolean timeLimit) {
		TimeLimit = timeLimit;
	}

	public static Boolean param_getChoco() {
		return Choco;
	}

	public static void param_setChoco(Boolean choco) {
		Choco = choco;
	}

}
