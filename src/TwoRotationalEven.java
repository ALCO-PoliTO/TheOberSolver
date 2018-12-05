import java.io.File;
import java.util.ArrayList;

import ilog.cp.IloCP;
import ilog.cp.IloSearchPhase;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;

import org.chocosolver.parser.json.JSON;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

public class TwoRotationalEven {
	private static long Clock;
	private static Boolean Verbose = true;
	private static String OP_name = "";
	private static Boolean Check = false;
	private static Boolean exportModels = false;
	private static Boolean TimeLimit = false;
	private static Boolean Choco = false;
	private static String FilePath = "";
	private static int V = 0;

	private static int SolLimit = 0;
	private static ArrayList<ArrayList<Integer>> YR_Pool = null;
	private static ArrayList<ArrayList<ArrayList<Integer>>> YR_Isomorphisms = null;

	public static boolean has_TrivialIsomorphism(ArrayList<Integer> currentYR) {
		ArrayList<Integer> currentYR_s = new ArrayList<Integer>();
		if (YR_Pool.size() > 0) {
			for (int j = 0; j < currentYR.size(); j++) {
				if (currentYR.get(j) == 1)
					currentYR_s.add(2);
				if (currentYR.get(j) == 2)
					currentYR_s.add(1);
			}

			for (int i = 0; i < YR_Pool.size(); i++) {
				if (YR_Pool.get(i).equals(currentYR)) {

					return true;
				}
				if (YR_Pool.get(i).equals(currentYR_s)) {
					return true;
				}
			}
			return false;
		} else
			return false;

	}

	public static boolean has_Isomorphism(ArrayList<ArrayList<Integer>> configYR) {
		if (YR_Isomorphisms.size() > 0) {
			for (int i = 0; i < YR_Isomorphisms.size(); i++) {
				if (YR_Isomorphisms.get(i).equals(configYR)) {
					return true;
				}
			}
		} else
			return false;
		return false;

	}

	private static ArrayList<ArrayList<Integer>> getColorInfo(ArrayList<Integer> colors, ArrayList<Integer> tables) {
		ArrayList<ArrayList<Integer>> config = new ArrayList<ArrayList<Integer>>();
		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int nY = 0;
		int nYR = 0;
		for (int t = 0; t < tables.size(); t++) {
			nY = nYR = 0;
			ArrayList<Integer> table = new ArrayList<Integer>();
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					nY++;
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					nYR++;
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					nYR++;
				}
			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					nY++;
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					nYR++;
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					nYR++;
				}
			}
			table.add(nY);
			table.add(nYR);
			config.add(table);
			scroll += tables.get(t);
		}
		return config;
	}

	private static int getOPsize(ArrayList<Integer> table) throws ErrorThrower {
		int size = 0;
		for (int t = 0; t < table.size(); t++) {
			if (table.get(t) < 2)
				throw new ErrorThrower("Each table must have more than 1 participant");
			size += table.get(t);
		}
		return size;
	}

	public int getEdges(ArrayList<Integer> tables) {
		int A = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++)
				A++;

			if (t != 0)
				A++;

		}
		return A;
	}

	public int[][] getAdjacency(ArrayList<Integer> tables) {
		int[][] ADJ = new int[V][V];
		int scroll = 0, alpha = 0, beta = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				ADJ[alpha][beta] = 1;
				ADJ[beta][alpha] = 1;
			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				ADJ[alpha][scroll] = 1;
				ADJ[scroll][alpha] = 1;
			}
			scroll += tables.get(t);
		}
		return ADJ;
	}

	public static ArrayList<ArrayList<Integer>> generateColors_CP_Choco(TwoRotationalOdd_Solution Solution, int num)
			throws ErrorThrower, ContradictionException {
		float start = System.nanoTime();
		ArrayList<Integer> tables = Solution.getTables();
		int size = getOPsize(tables);
		int __D = size / 2;
		int[][] ADJ = new int[size][size];
		Model choco = new Model("generateColors_CP");
		ArrayList<ArrayList<Integer>> YR = new ArrayList<ArrayList<Integer>>();
		IntVar[] N = choco.intVarArray("N", size, 1, 2);
		IntVar _D = choco.intVar(__D);
		IntVar _Edges = choco.intVar(__D * 2 - 2);
		choco.count(1, N, _D).post();
		choco.count(2, N, _D).post();
		choco.arithm(N[0], "=", 1).post();
		choco.arithm(N[tables.get(0) - 1], "=", 2).post();

		IntVar[][] mono = choco.intVarMatrix("mono", size, size, 0, 1);
		IntVar[][] multi = choco.intVarMatrix("multi", size, size, 0, 1);

		int scroll = 0, alpha = 0, beta = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				ADJ[alpha][beta] = 1;
				ADJ[beta][alpha] = 1;
			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				ADJ[alpha][scroll] = 1;
				ADJ[scroll][alpha] = 1;
			}
			scroll += tables.get(t);
		}

		IntVar[] multi_count = choco.intVarArray("MultiCount", size * size, 0, 1);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				choco.arithm(mono[i][j], "+", multi[i][j], "<=", 1).post();
				if (ADJ[i][j] == 0) {
					choco.arithm(mono[i][j], "=", 0).post();
					choco.arithm(multi[i][j], "=", 0).post();
				}
				multi_count[j * size + i] = multi[i][j];
			}
		}
		choco.count(1, multi_count, _Edges).post();

		scroll = 0;
		alpha = 0;
		beta = 0;
		ArrayList<Integer> forbiddenList = new ArrayList<Integer>();

		int altCnt = 0;
		for (int t = 0; t < tables.size(); t++) {
			if (tables.get(t) > 3) {
				int tsize = tables.get(t);
				for (int i = 0; i < tsize; i++) {
					int a = scroll + ((i) % tsize);
					int b = scroll + ((i + 1) % tsize);
					int c = scroll + ((i + 2) % tsize);
					int d = scroll + ((i + 3) % tsize);
					// System.out.println(a+"-"+b+"-"+c+"-"+d+"\tt="+t);
					if ((t == 0 && b > a && c > b && d > c) || (t != 0)) {
						altCnt++;
						forbiddenList.add(a);
						forbiddenList.add(b);
						forbiddenList.add(c);
						forbiddenList.add(d);
						// System.out.println("A"+a + "to" + d);
					}
					// System.out.println("R"+a + "to" + d);
				}
			}
			scroll += tables.get(t);
		}

		scroll = 0;
		IntVar[] A = choco.intVarArray("A", altCnt, 0, 1);
		for (int i = 0; i < altCnt; i++) {
			// System.out.println("A["+i+"]\t"+scroll+" to "+(scroll+3));
			choco.ifThenElse(
					choco.and(
							choco.and(choco.arithm(N[forbiddenList.get(scroll)], "=", 1),
									choco.arithm(N[forbiddenList.get(scroll + 1)], "=", 1)),
							choco.and(choco.and(choco.arithm(N[forbiddenList.get(scroll + 2)], "=", 2),
									choco.arithm(N[forbiddenList.get(scroll + 3)], "=", 2)))),
					choco.arithm(A[i], "=", 1), choco.arithm(A[i], "=", 0));
			scroll = scroll + 4;
		}
		choco.sum(A, ">=", 1).post();

		scroll = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (Verbose)
					System.out.println("Writing constraints for " + alpha + "->" + beta);
				// YR and RY
				choco.ifThen(
						choco.or(choco.and(choco.arithm(N[alpha], "=", 1), choco.arithm(N[beta], "=", 2)),
								choco.and(choco.arithm(N[alpha], "=", 2), choco.arithm(N[beta], "=", 1))),
						choco.and(choco.arithm(mono[alpha][beta], "=", 0), choco.arithm(mono[beta][alpha], "=", 0),
								choco.arithm(multi[alpha][beta], "=", 1), choco.arithm(multi[beta][alpha], "=", 1)));
				// RR and YY
				choco.ifThen(
						choco.or(choco.and(choco.arithm(N[alpha], "=", 1), choco.arithm(N[beta], "=", 1)),
								choco.and(choco.arithm(N[alpha], "=", 2), choco.arithm(N[beta], "=", 2))),
						choco.and(choco.arithm(mono[alpha][beta], "=", 1), choco.arithm(mono[beta][alpha], "=", 1),
								choco.arithm(multi[alpha][beta], "=", 0), choco.arithm(multi[beta][alpha], "=", 0)));
			}
			// Close the table
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				if (Verbose)
					System.out.println("Writing closing constraints for " + alpha + "->" + beta);
				// YR and RY
				choco.ifThen(
						choco.or(choco.and(choco.arithm(N[alpha], "=", 1), choco.arithm(N[beta], "=", 2)),
								choco.and(choco.arithm(N[alpha], "=", 2), choco.arithm(N[beta], "=", 1))),
						choco.and(choco.arithm(mono[alpha][beta], "=", 0), choco.arithm(mono[beta][alpha], "=", 0),
								choco.arithm(multi[alpha][beta], "=", 1), choco.arithm(multi[beta][alpha], "=", 1)));
				// RR and YY
				choco.ifThen(
						choco.or(choco.and(choco.arithm(N[alpha], "=", 1), choco.arithm(N[beta], "=", 1)),
								choco.and(choco.arithm(N[alpha], "=", 2), choco.arithm(N[beta], "=", 2))),
						choco.and(choco.arithm(mono[alpha][beta], "=", 1), choco.arithm(mono[beta][alpha], "=", 1),
								choco.arithm(multi[alpha][beta], "=", 0), choco.arithm(multi[beta][alpha], "=", 0)));

			}
			scroll += tables.get(t);
		}
		if (YR_Pool.size() > 0) {
			Constraint master = null;
			for (int i = 0; i < YR_Pool.size(); i++) {
				if (Verbose)
					System.out.println("Excluding color layout " + i + " from generation");
				Constraint expr = null;
				Boolean flag = true;
				for (int j = 0; j < YR_Pool.get(i).size(); j++) {
					if (j != 0 && j != (tables.get(0) - 1)) {
						if (flag) {
							expr = choco.arithm(N[j], "!=", YR_Pool.get(i).get(j));
							flag = false;
						} else
							expr = choco.or(expr, choco.arithm(N[j], "!=", YR_Pool.get(i).get(j)));
					}
				}
				if (i == 0) {
					master = expr;
				} else if (i > 0) {
					master = choco.and(master, expr);
				}
			}
			master.post();
		}

		Solver solver = choco.getSolver();
		solver.propagate();
		if (Verbose)
			solver.showShortStatistics();

		if (exportModels)
			JSON.write(choco,
					new File(FilePath + "solved/Color_" + Solution.getName() + "_" + param_getOP_name() + ".json"));

		int count = 0;
		int Iso_trivial_count = 0;
		int Iso_nontrivial_count = 0;
		int iteration = 0;
		while (solver.solve() && count < num) {
			ArrayList<Integer> CurrentYR = new ArrayList<Integer>();
			iteration++;
			for (int i = 0; i < size; i++) {
				if (N[i].getValue() == 1) {
					if (Verbose)
						System.out.println("Location " + i + "\t\tYELLOW");
					CurrentYR.add(1);
				} else {
					if (Verbose)
						System.out.println("Location " + i + "\t\tRED");
					CurrentYR.add(2);
				}
			}
			if (has_TrivialIsomorphism(CurrentYR)) {
				Iso_trivial_count++;
				if (Verbose)
					System.out.println("\t!!Trivial YR Isomorphism detected. Skipping.!!");
			} else {
				ArrayList<ArrayList<Integer>> currentYR_Iso = getColorInfo(CurrentYR, Solution.getTables());
				if (has_Isomorphism(currentYR_Iso)) {
					Iso_nontrivial_count++;
					if (Verbose)
						System.out.println("\t!!Non trivial YR Isomorphism detected. Skipping.!!");
				} else {
					YR_Isomorphisms.add(currentYR_Iso);
					YR_Pool.add(CurrentYR);
					YR.add(CurrentYR);
					
					ArrayList<ArrayList<Integer>> criticalPaths = new ArrayList<ArrayList<Integer>>();
					for (int i = 0; i < altCnt; i++) {
						if (A[i].getValue() > 0) {
							ArrayList<Integer> forbiddenPath = new ArrayList<Integer>();
							forbiddenPath.clear();
							forbiddenPath.add(forbiddenList.get(i * 4));
							forbiddenPath.add(forbiddenList.get(i * 4 + 1));
							forbiddenPath.add(forbiddenList.get(i * 4 + 2));
							forbiddenPath.add(forbiddenList.get(i * 4 + 3));
							criticalPaths.add(forbiddenPath);
						}
					}
					Solution.setCriticalPaths(criticalPaths);
					
					Solution.setColorTime(solver.getTimeCount());
					count++;
				}
			}
		}
		Solution.setColorTime((System.nanoTime() - start) / 1000000000F);
		if (Verbose) {
			System.out.println("\t\t" + iteration + " iterations of CP.");
			System.out.println("\t\t" + Iso_nontrivial_count + " nontrivial isomorphisms | " + Iso_trivial_count
					+ " trivial isomorphisms");
			System.out.println("\t\t" + count + " unique layouts.");
		}
		solver.reset();
		return YR;
	}

	public static ArrayList<ArrayList<Integer>> generateColors_CP(TwoRotationalOdd_Solution Solution, int num)
			throws IloException, ErrorThrower {
		float start = System.nanoTime();
		ArrayList<Integer> tables = Solution.getTables();
		int size = getOPsize(tables);
		int _D = size / 2;
		int[][] ADJ = new int[size][size];
		IloCP cp = new IloCP();
		ArrayList<ArrayList<Integer>> YR = new ArrayList<ArrayList<Integer>>();
		IloIntVar[] N = new IloIntVar[size];
		N = cp.intVarArray(size, 1, 2, "Nodes");
		cp.add(cp.eq(cp.count(N, 1), _D));
		cp.add(cp.eq(cp.count(N, 2), _D));
		cp.add(cp.eq(N[0], 1));
		cp.add(cp.eq(N[tables.get(0) - 1], 2));
		IloIntVar[][] mono = new IloIntVar[size][];
		IloIntVar[][] multi = new IloIntVar[size][];
		for (int i = 0; i < size; i++) {
			mono[i] = cp.intVarArray(size, 0, 1, "mono" + i);
			multi[i] = cp.intVarArray(size, 0, 1, "multi" + i);
		}

		int scroll = 0, alpha = 0, beta = 0;

		scroll = 0;
		ArrayList<Integer> forbiddenList = new ArrayList<Integer>();

		int altCnt = 0;
		for (int t = 0; t < tables.size(); t++) {
			if (tables.get(t) > 3) {
				int tsize = tables.get(t);
				for (int i = 0; i < tsize; i++) {
					int a = scroll + ((i) % tsize);
					int b = scroll + ((i + 1) % tsize);
					int c = scroll + ((i + 2) % tsize);
					int d = scroll + ((i + 3) % tsize);
					// System.out.println(a+"-"+b+"-"+c+"-"+d+"\tt="+t);
					if ((t == 0 && b > a && c > b && d > c) || (t != 0)) {
						altCnt++;
						forbiddenList.add(a);
						forbiddenList.add(b);
						forbiddenList.add(c);
						forbiddenList.add(d);
						// System.out.println("A"+a + "to" + d);
					}
					// System.out.println("R"+a + "to" + d);
				}
			}
			scroll += tables.get(t);
		}

		scroll = 0;
		IloIntVar[] A = cp.intVarArray(altCnt, 0, 1, "A");
		for (int i = 0; i < altCnt; i++) {
			// System.out.println("A["+i+"]\t"+scroll+" to "+(scroll+3));
			cp.add(cp.ifThenElse(cp.and(
					cp.and(cp.eq(N[forbiddenList.get(scroll)], 1), cp.eq(N[forbiddenList.get(scroll + 1)], 1)),
					cp.and(cp.eq(N[forbiddenList.get(scroll + 2)], 2), cp.eq(N[forbiddenList.get(scroll + 3)], 2))),
					cp.eq(A[i], 1), cp.eq(A[i], 0)));
			scroll = scroll + 4;
		}
		cp.addGe(cp.count(A, 1), 1);

		scroll = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				ADJ[alpha][beta] = 1;
				ADJ[beta][alpha] = 1;

			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				ADJ[alpha][scroll] = 1;
				ADJ[scroll][alpha] = 1;

			}
			scroll += tables.get(t);
		}

		IloIntVar[] mono_count = new IloIntVar[size * size];
		mono_count = cp.intVarArray(size * size, 0, 1, "MonoCount");
		IloIntVar[] multi_count = new IloIntVar[size * size];
		multi_count = cp.intVarArray(size * size, 0, 1, "MultiCount");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				cp.add(cp.le(cp.sum(mono[i][j], multi[i][j]), 1));
				if (ADJ[i][j] == 0) {
					cp.add(cp.eq(mono[i][j], 0));
					cp.add(cp.eq(multi[i][j], 0));
				}
				cp.addEq(mono_count[j * size + i], mono[i][j]);
				cp.addEq(multi_count[j * size + i], multi[i][j]);
			}
		}
		cp.add(cp.eq(cp.count(multi_count, 1), (_D - 1) * 2));
		// cp.add(cp.eq(cp.count(mono_count, 1), _D * 2));
		// cp.add(cp.eq(cp.count(mono_count, 1), (_D ) * 2));

		scroll = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (Verbose)
					System.out.println("Writing constraints for " + alpha + "->" + beta);
				// YR and RY
				cp.add(cp.ifThen(
						cp.or(cp.and(cp.eq(N[alpha], 1), cp.eq(N[beta], 2)),
								cp.and(cp.eq(N[alpha], 2), cp.eq(N[beta], 1))),
						cp.and(cp.and(cp.eq(mono[alpha][beta], 0), cp.eq(multi[alpha][beta], 1)),
								cp.and(cp.eq(mono[beta][alpha], 0), cp.eq(multi[beta][alpha], 1)))));
				// RR and YY
				cp.add(cp.ifThen(
						cp.or(cp.and(cp.eq(N[alpha], 1), cp.eq(N[beta], 1)),
								cp.and(cp.eq(N[alpha], 2), cp.eq(N[beta], 2))),
						cp.and(cp.and(cp.eq(mono[alpha][beta], 1), cp.eq(multi[alpha][beta], 0)),
								cp.and(cp.eq(mono[beta][alpha], 1), cp.eq(multi[beta][alpha], 0)))));

			}
			// Close the table
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				if (Verbose)
					System.out.println("Writing closing constraints for " + alpha + "->" + beta);
				// YR and RY
				cp.add(cp.ifThen(
						cp.or(cp.and(cp.eq(N[alpha], 1), cp.eq(N[beta], 2)),
								cp.and(cp.eq(N[alpha], 2), cp.eq(N[beta], 1))),
						cp.and(cp.and(cp.eq(mono[alpha][beta], 0), cp.eq(multi[alpha][beta], 1)),
								cp.and(cp.eq(mono[beta][alpha], 0), cp.eq(multi[beta][alpha], 1)))));
				// RR and YY
				cp.add(cp.ifThen(
						cp.or(cp.and(cp.eq(N[alpha], 1), cp.eq(N[beta], 1)),
								cp.and(cp.eq(N[alpha], 2), cp.eq(N[beta], 2))),
						cp.and(cp.and(cp.eq(mono[alpha][beta], 1), cp.eq(multi[alpha][beta], 0)),
								cp.and(cp.eq(mono[beta][alpha], 1), cp.eq(multi[beta][alpha], 0)))));

			}
			scroll += tables.get(t);
		}

		if (YR_Pool.size() > 0) {
			IloConstraint master = null;
			for (int i = 0; i < YR_Pool.size(); i++) {
				if (Verbose)
					System.out.println("Excluding color layout " + i + " from generation");
				IloConstraint expr = null;
				Boolean flag = true;
				for (int j = 0; j < YR_Pool.get(i).size(); j++) {
					if (j != 0 && j != (tables.get(0) - 1)) {
						if (flag) {
							expr = cp.neq(N[j], YR_Pool.get(i).get(j));
							flag = false;
						} else
							expr = cp.or(expr, cp.neq(N[j], YR_Pool.get(i).get(j)));
					}
				}
				if (i == 0) {
					master = expr;
				} else if (i > 0) {
					master = cp.and(master, expr);
				}
			}
			cp.add(master);
		}

		IloSearchPhase phaseOne = cp.searchPhase(N);
		cp.setSearchPhases(phaseOne);
		cp.propagate();

		if (!Verbose)
			cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
		cp.startNewSearch();
		if (exportModels)
			cp.exportModel(FilePath + "solved/Color_" + Solution.getName() + "_" + param_getOP_name() + ".cpo");
		int count = 0;
		int Iso_trivial_count = 0;
		int Iso_nontrivial_count = 0;
		int iteration = 0;
		while (cp.next() && count < num) {
			ArrayList<Integer> CurrentYR = new ArrayList<Integer>();
			iteration++;
			for (int i = 0; i < size; i++) {
				if (cp.getIntValue(N[i]) == 1) {
					if (Verbose)
						System.out.println("Location " + i + "\t\tYELLOW");
					CurrentYR.add(1);
				} else {
					if (Verbose)
						System.out.println("Location " + i + "\t\tRED");
					CurrentYR.add(2);
				}
			}

			if (has_TrivialIsomorphism(CurrentYR)) {
				Iso_trivial_count++;
				if (Verbose)
					System.out.println("\t!!Trivial YR Isomorphism detected. Skipping.!!");
			} else {
				ArrayList<ArrayList<Integer>> currentYR_Iso = getColorInfo(CurrentYR, Solution.getTables());
				if (has_Isomorphism(currentYR_Iso)) {
					Iso_nontrivial_count++;
					if (Verbose)
						System.out.println("\t!!Non trivial YR Isomorphism detected. Skipping.!!");
				} else {
					YR_Isomorphisms.add(currentYR_Iso);
					YR_Pool.add(CurrentYR);
					YR.add(CurrentYR);
					Solution.setColorTime(cp.getInfo(IloCP.DoubleInfo.SolveTime));
					ArrayList<ArrayList<Integer>> criticalPaths = new ArrayList<ArrayList<Integer>>();
					for (int i = 0; i < altCnt; i++) {
						if (cp.getIntValue(A[i]) > 0) {
							ArrayList<Integer> forbiddenPath = new ArrayList<Integer>();
							forbiddenPath.clear();
							forbiddenPath.add(forbiddenList.get(i * 4));
							forbiddenPath.add(forbiddenList.get(i * 4 + 1));
							forbiddenPath.add(forbiddenList.get(i * 4 + 2));
							forbiddenPath.add(forbiddenList.get(i * 4 + 3));
							criticalPaths.add(forbiddenPath);
						}
					}
					Solution.setCriticalPaths(criticalPaths);
					count++;
				}
			}
		}
		Solution.setColorTime((System.nanoTime() - start) / 1000000000F);
		if (Verbose) {
			System.out.println("\t\t" + iteration + " iterations of CP.");
			System.out.println("\t\t" + Iso_nontrivial_count + " nontrivial isomorphisms | " + Iso_trivial_count
					+ " trivial isomorphisms");
			System.out.println("\t\t" + count + " unique layouts.");
		}
		cp.end();
		return YR;
	}

	@SuppressWarnings("unused")
	public static Boolean generateLabels_CP_Choco(TwoRotationalOdd_Solution Solution) throws ContradictionException {
		Model choco = new Model("generateLabels_CP");
		int Vx = Solution.getV() - 1;
		int _D = Vx / 2;
		IntVar One = choco.intVar(1);
		IntVar[] S = choco.intVarArray("S", Vx, 0, _D - 1);
		IntVar[] Y = choco.intVarArray("Y", _D, 0, _D - 1);
		IntVar[] R = choco.intVarArray("R", _D, 0, _D - 1);
		IntVar[] dY = choco.intVarArray("dY", _D - 2, 1, _D - 1);
		IntVar[] dR = choco.intVarArray("dR", _D - 2, 1, _D - 1);
		IntVar[] dYR = choco.intVarArray("dYR", _D - 2, 1, _D - 1);

		ArrayList<ArrayList<Integer>> criticalPaths = Solution.getCriticalPaths();
		if (Verbose)
			System.out.println("Critical Path: " + criticalPaths.toString());

		choco.allDifferent(Y).post();
		choco.allDifferent(R).post();
		choco.allDifferent(dY).post();
		choco.allDifferent(dR).post();
		choco.allDifferent(dYR).post();

		int cR = 0, cY = 0, cYR = 0;
		for (int i = 0; i < Solution.getColors().size(); i++) {
			if (Solution.getColors().get(i) == 1) {
				// System.out.println("Node" + i + " is yellow");
				choco.arithm(S[i], "=", Y[cY]).post();
				cY++;
			} else {
				choco.arithm(S[i], "=", R[cR]).post();
				// System.out.println("Node" + i + " is red");
				cR++;
			}
		}
		/*
		 * // OP(3,4,4,4,4) cpx.add(cpx.eq(Y[0], 8)); cpx.add(cpx.eq(R[0], 8));
		 * cpx.add(cpx.eq(S[1], Y[0])); cpx.add(cpx.eq(S[0], R[0]));
		 * 
		 * cpx.add(cpx.eq(R[1], 7)); cpx.add(cpx.eq(R[2], 0)); cpx.add(cpx.eq(S[2],
		 * R[1])); cpx.add(cpx.eq(S[3], R[2]));
		 * 
		 * cpx.add(cpx.eq(Y[1], 1)); cpx.add(cpx.eq(Y[2], 2)); cpx.add(cpx.eq(S[4],
		 * Y[1])); cpx.add(cpx.eq(S[5], Y[2]));
		 * 
		 * 
		 * cpx.add(cpx.eq(Y[3], 0)); cpx.add(cpx.eq(Y[4], 4)); cpx.add(cpx.eq(S[6],
		 * Y[3])); cpx.add(cpx.eq(S[7], Y[4]));
		 */
		float time = 0;
		Double Tl = 0.0;
		for (int a = 0; a < criticalPaths.size(); a++) {
			ArrayList<Integer> forbiddenList = criticalPaths.get(a);
			choco.arithm(S[forbiddenList.get(0)], "=", 0).post();
			choco.arithm(S[forbiddenList.get(1)], "=", S[forbiddenList.get(2)]).post();
			choco.arithm(S[forbiddenList.get(3)], "=", 0).post();
			choco.arithm(S[forbiddenList.get(1)], "=", _D / 2).post();
			if (Verbose)
				System.out.println("Critical:" + forbiddenList.toString()+"\t_D/2="+ _D / 2);

			Solution.setNotes("CriticalPath " + forbiddenList.toString());

			cR = 0;
			cY = 0;
			cYR = 0;
			int scroll = 0;
			int alpha = 0;
			int beta = 0;
			for (int t = 0; t < Solution.getTables().size(); t++) {
				for (int i = 0; i < Solution.getTables().get(t) - 1; i++) {
					alpha = scroll + i;
					beta = scroll + i + 1;

					if (!(forbiddenList.contains(alpha) && forbiddenList.contains(beta))) {
						// YY
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 1) {

							if (Verbose)
								System.out.println("Writing constraints YY for " + alpha + "->" + beta);
							S[alpha].sub(S[beta]).add(_D).mod(_D).eq(dY[cY]).post();
							cY++;
							S[beta].sub(S[alpha]).add(_D).mod(_D).eq(dY[cY]).post();
							cY++;
						}
						// RR
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing constraints RR for " + alpha + "->" + beta);
							S[alpha].sub(S[beta]).add(_D).mod(_D).eq(dR[cR]).post();
							cR++;
							S[beta].sub(S[alpha]).add(_D).mod(_D).eq(dR[cR]).post();
							cR++;

						}
						// YR
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing constraints YR for " + alpha + "->" + beta);
							S[alpha].sub(S[beta]).add(_D).mod(_D).eq(dYR[cYR]).post();
							cYR++;
						}
						// RY
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 1) {
							if (Verbose)
								System.out.println("Writing constraints RY for " + alpha + "->" + beta);
							S[beta].sub(S[alpha]).add(_D).mod(_D).eq(dYR[cYR]).post();
							cYR++;
						}

					}
				}
				// Close the table
				if (t != 0) {
					alpha = scroll + Solution.getTables().get(t) - 1;
					beta = scroll;

					if (!(forbiddenList.contains(alpha) && forbiddenList.contains(beta))) {
						// YY
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 1) {

							if (Verbose)
								System.out.println("Writing constraints YY for " + alpha + "->" + beta);
							S[alpha].sub(S[beta]).add(_D).mod(_D).eq(dY[cY]).post();
							cY++;
							S[beta].sub(S[alpha]).add(_D).mod(_D).eq(dY[cY]).post();
							cY++;
						}
						// RR
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing constraints RR for " + alpha + "->" + beta);
							S[alpha].sub(S[beta]).add(_D).mod(_D).eq(dR[cR]).post();
							cR++;
							S[beta].sub(S[alpha]).add(_D).mod(_D).eq(dR[cR]).post();
							cR++;

						}
						// YR
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing constraints YR for " + alpha + "->" + beta);
							S[alpha].sub(S[beta]).add(_D).mod(_D).eq(dYR[cYR]).post();
							cYR++;
						}
						// RY
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 1) {
							if (Verbose)
								System.out.println("Writing constraints RY for " + alpha + "->" + beta);
							S[beta].sub(S[alpha]).add(_D).mod(_D).eq(dYR[cYR]).post();
							cYR++;
						}

					}
				}
				scroll += Solution.getTables().get(t);
			}
			if (Verbose)
				System.out.println("dY:" + cY + "\tdR" + cR + "\tdYR" + cYR);

			Solver solver = choco.getSolver();
			solver.propagate();
			if (Verbose)
				solver.showShortStatistics();

			if (exportModels)
				JSON.write(choco,
						new File(FilePath + "solved/Color_" + Solution.getName() + "_" + param_getOP_name() + ".json"));

			Tl = (double) (10 * (1 + V / 50));
			if (TimeLimit)
				solver.limitTime(Tl + "s");

			if (solver.solve()) {
				Solution.setLabellingTime(solver.getTimeCount());
				int[] labels = new int[Vx];
				for (int i = 0; i < Vx; i++) {
					labels[i] = S[i].getValue();
					// System.out.println("Node "+i+" is "+Solution.getColors().get(i)+" and labeled
					// as "+labels[i]);
				}
				Solution.setLabels(labels);
				if (exportModels) {
					JSON.write(choco, new File(FilePath + "solved/" + "Labelling_YR" + Solution.getName() + "_"
							+ param_getOP_name() + ".json"));
				}
				return true;

			} else {
				Solution.setColorTries(Solution.getColorTries() + 1);
				Solution.setLabellingTime(time);
				if (TimeLimit)
					if (time >= Tl)
						Solution.setStatus("TimeLimit");
				// Solution.setStatus("TimeLimit");
				if (exportModels) {
					JSON.write(choco, new File(FilePath + "infeasibles/" + "Labelling_YR" + Solution.getName() + "_"
							+ param_getOP_name() + ".json"));
				}
				return false;
			}
		}
		return false;

	}

	public static Boolean generateLabels_CP(TwoRotationalOdd_Solution Solution) throws IloException {
		IloCP cpx = new IloCP();
		int Vx = Solution.getV() - 1;
		int _D = Vx / 2;
		double Tl = 0.0;
		// System.out.println("Vx:" + Vx + "\t_D:" + _D);
		IloIntVar[] S = new IloIntVar[Vx];
		S = cpx.intVarArray(Vx, 0, _D - 1, "S");
		IloIntVar[] Y = new IloIntVar[_D];
		Y = cpx.intVarArray(_D, 0, _D - 1, "Y");
		IloIntVar[] R = new IloIntVar[_D];
		R = cpx.intVarArray(_D, 0, _D - 1, "R");
		IloIntVar[] dY = new IloIntVar[_D - 1];
		dY = cpx.intVarArray(_D - 2, 1, _D - 1, "dY");
		IloIntVar[] dR = new IloIntVar[_D - 1];
		dR = cpx.intVarArray(_D - 2, 1, _D - 1, "dR");
		IloIntVar[] dYR = new IloIntVar[_D];
		dYR = cpx.intVarArray(_D - 2, 1, _D - 1, "dYR");

		cpx.add(cpx.allDiff(Y));
		cpx.add(cpx.allDiff(R));
		cpx.add(cpx.allDiff(dY));
		cpx.add(cpx.allDiff(dR));
		cpx.add(cpx.allDiff(dYR));

		int cR = 0, cY = 0, cYR = 0;
		for (int i = 0; i < Solution.getColors().size(); i++) {
			if (Solution.getColors().get(i) == 1) {
				// System.out.println("Node" + i + " is yellow");
				cpx.add(cpx.eq(S[i], Y[cY]));
				cY++;
			} else {
				cpx.add(cpx.eq(S[i], R[cR]));
				// System.out.println("Node" + i + " is red");
				cR++;
			}
		}

		int scroll = 0;
		int alpha = 0;
		int beta = 0;

		ArrayList<ArrayList<Integer>> criticalPaths = Solution.getCriticalPaths();
		if (Verbose)
			System.out.println("Critical Path: " + criticalPaths.toString());

		for (int i = 1; i < _D; i++) {
			cpx.add(cpx.eq(cpx.count(dY, i), cpx.count(dR, i)));
			cpx.add(cpx.eq(cpx.count(dR, i), cpx.count(dYR, i)));
		}

		for (int a = 0; a < criticalPaths.size(); a++) {
			ArrayList<Integer> forbiddenList = criticalPaths.get(a);
			cpx.add(cpx.eq(S[forbiddenList.get(0)], 0));
			cpx.add(cpx.eq(S[forbiddenList.get(1)], S[forbiddenList.get(2)]));
			cpx.add(cpx.eq(S[forbiddenList.get(3)], 0));
			cpx.addEq(S[forbiddenList.get(1)], _D / 2);
			if (Verbose)
				System.out.println("Critical:" + forbiddenList.toString());

			Solution.setNotes("CriticalPath " + forbiddenList.toString());
			scroll = 0;
			cR = 0;
			cY = 0;
			cYR = 0;

			for (int t = 0; t < Solution.getTables().size(); t++) {
				for (int i = 0; i < Solution.getTables().get(t) - 1; i++) {
					alpha = scroll + i;
					beta = scroll + i + 1;

					if (!(forbiddenList.contains(alpha) && forbiddenList.contains(beta))) {
						// YY

						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 1) {

							if (Verbose)
								System.out.println("Writing constraints YY for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dY[cY], cpx.modulo(cpx.sum(cpx.diff(S[alpha], S[beta]), _D), _D)));
							cY++;
							cpx.add(cpx.eq(dY[cY], cpx.modulo(cpx.sum(cpx.diff(S[beta], S[alpha]), _D), _D)));
							cY++;
						}
						// RR
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing constraints RR for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dR[cR], cpx.modulo(cpx.sum(cpx.diff(S[alpha], S[beta]), _D), _D)));
							cR++;
							cpx.add(cpx.eq(dR[cR], cpx.modulo(cpx.sum(cpx.diff(S[beta], S[alpha]), _D), _D)));
							cR++;

						}
						// YR
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing constraints YR for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dYR[cYR], cpx.modulo(cpx.sum(cpx.diff(S[alpha], S[beta]), _D), _D)));
							cYR++;
						}
						// RY
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 1) {
							if (Verbose)
								System.out.println("Writing constraints RY for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dYR[cYR], cpx.modulo(cpx.sum(cpx.diff(S[beta], S[alpha]), _D), _D)));
							cYR++;
						}

					}
				}
				// Close the table
				if (t != 0) {
					alpha = scroll + Solution.getTables().get(t) - 1;
					beta = scroll;

					if (!(forbiddenList.contains(alpha) && forbiddenList.contains(beta))) {
						// YY
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 1) {

							if (Verbose)
								System.out.println("Writing C constraints YY for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dY[cY], cpx.modulo(cpx.sum(cpx.diff(S[alpha], S[beta]), _D), _D)));
							cY++;
							cpx.add(cpx.eq(dY[cY], cpx.modulo(cpx.sum(cpx.diff(S[beta], S[alpha]), _D), _D)));
							cY++;
						}
						// RR
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing C constraints RR for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dR[cR], cpx.modulo(cpx.sum(cpx.diff(S[alpha], S[beta]), _D), _D)));
							cR++;
							cpx.add(cpx.eq(dR[cR], cpx.modulo(cpx.sum(cpx.diff(S[beta], S[alpha]), _D), _D)));
							cR++;

						}
						// YR
						if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 2) {
							if (Verbose)
								System.out.println("Writing C constraints YR for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dYR[cYR], cpx.modulo(cpx.sum(cpx.diff(S[alpha], S[beta]), _D), _D)));
							cYR++;
						}
						// RY
						if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 1) {
							if (Verbose)
								System.out.println("Writing C constraints RY for " + alpha + "->" + beta);
							cpx.add(cpx.eq(dYR[cYR], cpx.modulo(cpx.sum(cpx.diff(S[beta], S[alpha]), _D), _D)));
							cYR++;
						}

					}

				}
				scroll += Solution.getTables().get(t);
			}

			if (Verbose)
				System.out.println("dY:" + cY + "\tdR" + cR + "\tdYR" + cYR);

			if (!Verbose)
				cpx.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
			Tl = 5 * (1 + V / 50);

			if (TimeLimit)
				cpx.setParameter(IloCP.DoubleParam.TimeLimit, Tl);

			IloSearchPhase phaseOne = cpx.searchPhase(S);
			cpx.setSearchPhases(phaseOne);
			cpx.propagate();
			if (cpx.solve()) {
				Solution.setLabellingTime(cpx.getInfo(IloCP.DoubleInfo.SolveTime));
				int[] labels = new int[Vx];
				for (int i = 0; i < Vx; i++) {
					labels[i] = cpx.getIntValue(S[i]);
					// System.out.println("Node "+i+" is "+Solution.getColors().get(i)+" and labeled
					// as "+labels[i]);
				}
				Solution.setLabels(labels);
				if (exportModels) {
					cpx.exportModel(FilePath + "solved/" + "Labelling_YR" + Solution.getName() + "_"
							+ param_getOP_name() + ".cpo");
				}

				cpx.end();
				return true;
			}
		}
		Solution.setColorTries(Solution.getColorTries() + 1);
		Solution.setLabellingTime(cpx.getInfo(IloCP.DoubleInfo.TotalTime));
		if (TimeLimit)
			if (cpx.getInfo(IloCP.DoubleInfo.TotalTime) >= Tl)
				Solution.setStatus("TimeLimit");
		if (exportModels) {
			cpx.exportModel(FilePath + "infeasibles/" + "Labelling_YR" + Solution.getName() + "_" + param_getOP_name()
					+ ".cpo");
		}
		cpx.end();
		return false;

	}

	public ArrayList<TwoRotationalOdd_Solution> solve(ArrayList<Integer> tables)
			throws ErrorThrower, IloException, ContradictionException {

		V = getOPsize(tables);
		if (!((V % 4) == 1)) {
			throw new ErrorThrower("V % 4 != 3 (V=" + V + ")");
		}
		Boolean flag = true;
		for (int i = 0; i < tables.size() && flag; i++) {
			if (tables.get(i) > 3)
				flag = false;
		}
		if (flag) {
			System.out.println("At least one table of 4.");
			ArrayList<TwoRotationalOdd_Solution> Solutions = new ArrayList<TwoRotationalOdd_Solution>();
			TwoRotationalOdd_Solution Solution = new TwoRotationalOdd_Solution(tables, V);
			Solution.setStatus("ExsistenceNotMet");
			Solutions.add(Solution);
			return Solutions;
		}
		tables.set(0, tables.get(0) - 1);
		param_setOP_name(tables);
		YR_Pool = new ArrayList<ArrayList<Integer>>();
		YR_Isomorphisms = new ArrayList<ArrayList<ArrayList<Integer>>>();
		int Solve_count = 0;
		@SuppressWarnings("unused")
		Double time = 0.0;
		boolean Flag = true;
		int iteration = 0;
		ArrayList<ArrayList<Integer>> YR_cp = null;
		ArrayList<TwoRotationalOdd_Solution> Solutions = new ArrayList<TwoRotationalOdd_Solution>();

		while (Flag) {
			Clock = System.nanoTime();
			TwoRotationalOdd_Solution Solution = new TwoRotationalOdd_Solution(tables, V);
			Solution.setName("" + iteration);
			Solution.setOP_name(param_getOP_name());
			if (Verbose)
				System.out.println("Searching for color layouts with CP...");
			int num = 1;
			if (Choco)
				YR_cp = generateColors_CP_Choco(Solution, num);
			else
				YR_cp = generateColors_CP(Solution, num);
			if (YR_cp.size() > 0) {
				time = Solution.getColorTime();
				Solution.setColors(YR_cp.get(0));
			} else {
				Flag = false;
				break;
			}
			Boolean res = false;
			if (Choco)
				res = generateLabels_CP_Choco(Solution);
			else
				res = generateLabels_CP(Solution);
			if (res) {
				Solve_count++;
				Solutions.add(Solution);
			} else {
				System.out
						.println("No solution found with CP. CriticalPath: " + Solution.getCriticalPaths().toString());
				/*
				 * if (generateLabels_MIP(Solution)) { Solve_count++;
				 * System.out.println("Solution found with MIP."); } else {
				 * System.out.println("Proven infeasible with MIP."); }
				 */
				if (Solution.getStatus() == "Solved")
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

	public TwoRotationalEven(boolean Verbose, int SolLimit, Boolean exportModels, String FilePath, Boolean TimeLimit,
			Boolean Choco) throws ErrorThrower {
		param_setVerbose(Verbose);
		param_setExportModels(exportModels);
		param_setSolLimit(SolLimit);
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

	public static int param_getSolLimit() {
		return SolLimit;
	}

	public static void param_setSolLimit(int solLimit) {
		SolLimit = solLimit;
	}

	public static Boolean param_getExportModels() {
		return exportModels;
	}

	public static void param_setExportModels(Boolean exportModels) {
		TwoRotationalEven.exportModels = exportModels;
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

	public static String param_getFilePath() {
		return FilePath;
	}

	public static void param_setFilePath(String filePath) {
		FilePath = filePath;
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
