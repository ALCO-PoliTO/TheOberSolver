import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ilog.cp.IloCP;
import ilog.cp.IloSearchPhase;
import ilog.cplex.IloCplex;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;

import org.chocosolver.parser.json.JSON;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

public class TwoRotational {
	private static long Clock;
	private static Boolean Verbose = true;
	private static String OP_name = "";
	private static Boolean Check = false;
	private static Boolean exportModels = false;
	private static Boolean TimeLimit = false;
	private static Boolean Choco = false;
	private static String FilePath = "";
	private static int V = 0;
	private static int SymmetryValue = -1;
	private static Boolean Symmetry = false;

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

	private static Boolean isOne(Double var) {
		if (var > 0.9 && var < 1.1)
			return true;
		else
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

	private static boolean ArrayList_search(ArrayList<Integer[]> tables_in, ArrayList<Integer> search) {
		if (search.size() > tables_in.size()) {
			return false;
		} else {
			ArrayList<Integer> clone = new ArrayList<Integer>();
			for (int i = 0; i < tables_in.size(); i++)
				clone.add(tables_in.get(i)[0]);

			int count = 0;
			int target = search.size();
			for (int t = 0; t < clone.size(); t++) {
				for (int z = 0; z < search.size(); z++) {
					if (clone.get(t) == search.get(z)) {
						search.remove(z);
						clone.remove(t);
						z--;
						t--;
						count++;
						z = search.size();
					}
				}
			}
			if (count == target)
				return true;
			else
				return false;
		}
	}

	public static ArrayList<Integer> generateColors_Poly(TwoRotational_Solution Solution) throws ErrorThrower {
		long start = System.nanoTime();
		ArrayList<Integer> tables = Solution.getTables();
		tables.set(0, tables.get(0) + 1);
		ArrayList<Integer[]> tables_map = new ArrayList<Integer[]>();
		ArrayList<ArrayList<Integer>> tables_colors = new ArrayList<ArrayList<Integer>>();

		for (int t = 0; t < tables.size(); t++) {
			tables_map.add(new Integer[] { tables.get(t), tables.get(t) });
			tables_colors.add(new ArrayList<Integer>());
			while (tables_map.get(t)[0] >= 7) {
				tables_map.set(t, new Integer[] { tables_map.get(t)[0] - 4, tables_map.get(t)[1], 1 });
				tables_colors.get(t).add(2);
				tables_colors.get(t).add(2);
				tables_colors.get(t).add(1);
				tables_colors.get(t).add(1);
			}
			if (tables_map.get(t)[0] == 4) {
				tables_map.set(t, new Integer[] { 0, tables_map.get(t)[1], 1 });
				tables_colors.get(t).add(2);
				tables_colors.get(t).add(2);
				tables_colors.get(t).add(1);
				tables_colors.get(t).add(1);
			}
		}

		ArrayList<Integer> search = new ArrayList<Integer>();
		Boolean flag = true;

		while (flag) {
			flag = false;

			// Set 3-5
			search.clear();
			search.add(5);
			search.add(3);
			Boolean Plus4 = false;
			if (ArrayList_search(tables_map, search)) {
				// System.out.println("Found 3-5");

				for (int z = 0; z < tables_map.size(); z++) {
					if (tables_map.get(z).length > 2) {
						for (int i = 0; i < tables_colors.get(z).size(); i++) {
							if (tables_colors.get(z).get(i) == 2
									&& tables_colors.get(z).get((i + 1) % tables_colors.get(z).size()) == 2
									&& tables_colors.get(z).get((i + 2) % tables_colors.get(z).size()) == 1
									&& tables_colors.get(z).get((i + 3) % tables_colors.get(z).size()) == 1) {
								tables_colors.get(z).set((i + 2) % tables_colors.get(z).size(), 2);
								i = tables_colors.get(z).size();
								Plus4 = true;
							}
						}
						z = tables_map.size();
					}
				}

				if (Plus4) {
					int count = 0;
					int count2 = 0;
					flag = true;
					for (int z = 0; z < tables_map.size() && (count + count2 < 2); z++) {
						if (tables_map.get(z)[0] == 3 && count < 1) {
							// System.out.println("\tRemoving 3");
							tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });
							count++;
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
							}
						}
						if (tables_map.get(z)[0] == 5 && count2 < 1) {
							// System.out.println("\tRemoving 5");
							tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });

							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
							}
							count2++;
						}
					}
				} else {
					int count = 0;
					int count2 = 0;
					flag = true;
					for (int z = 0; z < tables_map.size() && (count + count2 < 2); z++) {
						if (tables_map.get(z)[0] == 3 && count < 1) {
							/*
							 * int idx = 0; for (int h = 0; h < z; h++) { idx += tables_map.get(h)[1]; }
							 * System.out.println("Table (3): " + z + " at " + idx);
							 */
							// System.out.println("\tRemoving 3");
							tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });
							count++;
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
							}
						}
						if (tables_map.get(z)[0] == 5 && count2 < 1) {
							// System.out.println("\tRemoving 5");
							/*
							 * int idx = 0; for (int h = 0; h < z; h++) { idx += tables_map.get(h)[1]; }
							 * System.out.println("Table (5): " + z + " at " + idx);
							 */
							tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });

							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
							}
							count2++;
						}
					}

				}

			}

			// Set 5x4times
			search.clear();
			for (int h = 0; h < 4; h++)
				search.add(5);
			if (ArrayList_search(tables_map, search)) {
				// System.out.println("Found 5-5-5-5");
				int count = 0;
				flag = true;
				for (int z = 0; z < tables_map.size() && (count < 4); z++) {
					if (tables_map.get(z)[0] == 5) {
						tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });
						//// System.out.println("\tRemoving 5");
						if (count == 0) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
							}
						} else if (count > 0 && count < 3) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
							}
						} else if (count == 3) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
							}
						}
						count++;
					}
				}

			}

			// Set 3x8times
			search.clear();
			for (int h = 0; h < 8; h++)
				search.add(3);
			if (ArrayList_search(tables_map, search)) {
				int count = 0;
				// System.out.println("Found 8times a 3");
				flag = true;
				for (int z = 0; z < tables_map.size() && (count < 8); z++) {
					if (tables_map.get(z)[0] == 3) {
						tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });
						//// System.out.println("\tRemoving 3");
						if (count == 0) {
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(1);
						} else if (count > 0 && count < 4) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
							}
						} else if (count == 4) {
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
						} else if (count > 4) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);

							}
						}
						count++;
					}
				}

			}

			// Pair 6-6
			search.clear();
			search.add(6);
			search.add(6);
			if (ArrayList_search(tables_map, search)) {
				flag = true;
				// System.out.println("Found a 6-6");
				int count = 0;
				for (int z = 0; z < tables_map.size() && (count < 2); z++) {
					if (tables_map.get(z)[0] == 6) {
						tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });
						// System.out.println("\tRemoving 6");
						if (count == 0) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
							} else {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(2);
							}
						} else if (count == 1) {
							if (tables_colors.get(z).size() > 0
									&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 2) {
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
							} else {
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(2);
								tables_colors.get(z).add(1);
								tables_colors.get(z).add(1);
							}

						}
						count++;
					}
				}
			}

			// Set 6-3-3
			search.clear();
			search.add(6);
			search.add(3);
			search.add(3);
			if (ArrayList_search(tables_map, search)) {
				// Set 6-3-3
				// System.out.println("Found 6 3 3");
				int count = 0;
				int count2 = 0;
				flag = true;
				for (int z = 0; z < tables_map.size() && (count + count2 < 3); z++) {
					if (tables_map.get(z)[0] == 3 && (count < 2)) {
						// System.out.println("\tRemoving 3");
						tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });
						count++;
						if (tables_colors.get(z).size() > 0
								&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(1);
						} else {
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(2);
						}

					}
					if (tables_map.get(z)[0] == 6 && count2 < 1) {
						// System.out.println("\tRemoving 6");
						tables_map.set(z, new Integer[] { 0, tables_map.get(z)[1] });

						if (tables_colors.get(z).size() > 0
								&& tables_colors.get(z).get(tables_colors.get(z).size() - 1) == 1) {
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
						} else {
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(1);
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
							tables_colors.get(z).add(2);
						}
						count2++;
					}
				}

			}

		}

		int count = 0;
		int indexes[] = new int[5];
		String baseSolution = "";
		ArrayList<Integer> missing = new ArrayList<Integer>();
		for (int t = 0; t < tables_map.size(); t++)
			if (tables_map.get(t)[0] > 0) {
				missing.add(tables_map.get(t)[0]);
				indexes[count] = t;
				count++;
			}
		int zero = -1;
		// System.out.println("\tYR reduced " + tables.size() + "->" + missing.size());

		if (missing.size() == 1 && missing.contains(3)) {
			baseSolution = "OP(3)";
			// OP(3)
			// System.out.println("\tFound OP(3) basic configuration");
			if (tables_colors.get(indexes[0]).size() > 0
					&& tables_colors.get(indexes[0]).get(tables_colors.get(indexes[0]).size() - 1) == 1) {
				tables_colors.get(indexes[0]).add(2);
				tables_colors.get(indexes[0]).add(0, 0);
				tables_colors.get(indexes[0]).add(1, 1);
			} else {
				tables_colors.get(indexes[0]).add(1);
				tables_colors.get(indexes[0]).add(0, 0);
				tables_colors.get(indexes[0]).add(1, 2);
			}
			zero = indexes[0];
		} else if (missing.size() == 2 && missing.contains(5) && missing.contains(6) && !missing.contains(3)) {
			// OP(5,6)
			// System.out.println("\tFound OP(5,6) basic configuration");
			baseSolution = "OP(5,6)";
			for (int z = 0; z < 2; z++) {

				if (missing.get(z) == 5) {
					if (tables_colors.get(indexes[z]).size() > 0
							&& tables_colors.get(indexes[z]).get(tables_colors.get(indexes[z]).size() - 1) == 2) {
						tables_colors.get(indexes[z]).add(0);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);
					} else {
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(0);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(1);
					}

					zero = indexes[z];
				}
				if (missing.get(z) == 6) {
					if (tables_colors.get(indexes[z]).size() > 0
							&& tables_colors.get(indexes[z]).get(tables_colors.get(indexes[z]).size() - 1) == 1) {
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(1);
					} else {
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);

					}
				}
			}

		}

		else if (missing.size() == 5 && !missing.contains(5) && !missing.contains(6) && missing.contains(3)) {
			// OP(3,3,3,3,3)
			// System.out.println("\tFound OP(3,3,3,3,3) basic configuration");
			baseSolution = "OP(3,3,3,3,3)";
			for (int z = 0; z < 5; z++) {
				if (z == 0) {
					if (tables_colors.get(indexes[z]).size() > 0
							&& tables_colors.get(indexes[z]).get(tables_colors.get(indexes[z]).size() - 1) == 1) {
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(0);
						tables_colors.get(indexes[z]).add(1);
					} else {
						tables_colors.get(indexes[z]).add(0);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(1);
					}
					zero = indexes[z];
				} else if (z == 1) {
					tables_colors.get(indexes[z]).add(1);
					tables_colors.get(indexes[z]).add(1);
					tables_colors.get(indexes[z]).add(1);
				} else {
					if (tables_colors.get(indexes[z]).size() > 0
							&& tables_colors.get(indexes[z]).get(tables_colors.get(indexes[z]).size() - 1) == 1) {
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(1);
					} else {
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
					}
				}
			}
		}

		else if (missing.size() == 3 && missing.contains(5) && !missing.contains(6) && !missing.contains(3)) {
			// OP(5,5,5)
			// System.out.println("\tFound OP(5,5,5) basic configuration");
			baseSolution = "OP(5,5,5)";
			for (int z = 0; z < 3; z++) {
				if (z == 0) {
					if (tables_colors.get(indexes[z]).size() > 0
							&& tables_colors.get(indexes[z]).get(tables_colors.get(indexes[z]).size() - 1) == 1) {
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(0);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
					} else {
						tables_colors.get(indexes[z]).add(0);
						tables_colors.get(indexes[z]).add(1);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
						tables_colors.get(indexes[z]).add(2);
					}
					zero = indexes[z];
				} else if (z == 1) {
					tables_colors.get(indexes[z]).add(2);
					tables_colors.get(indexes[z]).add(1);
					tables_colors.get(indexes[z]).add(1);
					tables_colors.get(indexes[z]).add(2);
					tables_colors.get(indexes[z]).add(1);
				} else {
					tables_colors.get(indexes[z]).add(2);
					tables_colors.get(indexes[z]).add(2);
					tables_colors.get(indexes[z]).add(1);
					tables_colors.get(indexes[z]).add(1);
					tables_colors.get(indexes[z]).add(1);
				}
			}
		} else

		{
			System.out.println("\t!!!!Unknown basic configuration found!!!!");
			for (int t = 0; t < tables_map.size(); t++)
				if (tables_map.get(t)[0] > 0) {
					System.out.println("\t\tTable " + (t + 1) + " has " + tables_map.get(t)[0] + " ("
							+ tables_map.get(t)[1] + ")");
				}
		}

		if (zero != 0)
			Collections.swap(tables_colors, 0, zero);
		tables.clear();
		for (int z = 0; z < tables_colors.size(); z++) {
			tables.add(tables_colors.get(z).size());
		}

		int zero_rot = tables_colors.get(0).indexOf(0);
		if (zero_rot != 0)
			Collections.rotate(tables_colors.get(0), tables_colors.get(0).size() - zero_rot);
		// System.out.println("Putting \\Inf in position. Rotating from " + zero_rot + "
		// to "
		// + +tables_colors.get(0).indexOf(0) + " on size " +
		// tables_colors.get(0).size());

		tables_colors.get(0).remove(0);
		tables.set(0, tables.get(0) - 1);
		ArrayList<Integer> YR = new ArrayList<Integer>();

		for (int z = 0; z < tables_colors.size(); z++) {
			for (int t = 0; t < tables_colors.get(z).size(); t++) {
				YR.add(tables_colors.get(z).get(t));
			}
		}
		/*
		 * int scroll = 0; System.out.println("\nColor configuration"); for (int z = 0;
		 * z < tables_colors.size(); z++) { System.out.println("Table " + z); for (int t
		 * = 0; t < tables_colors.get(z).size(); t++) { System.out.println("\tNode " +
		 * (scroll + t) + ": " + tables_colors.get(z).get(t)); } scroll +=
		 * tables_colors.get(z).size(); }
		 */
		Solution.setColorTime((System.nanoTime() - start) / 1000000000F);
		Solution.setTables(tables);
		Solution.setNotes(baseSolution);
		return YR;
	}

	public static ArrayList<ArrayList<Integer>> generateColors_CP_Choco(TwoRotational_Solution Solution, int num)
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
		IntVar _S = choco.intVar(size);
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
		choco.count(1, multi_count, _S).post();

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

	public static ArrayList<ArrayList<Integer>> generateColors_CP(TwoRotational_Solution Solution, int num)
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
				mono_count[j * size + i] = mono[i][j];
				multi_count[j * size + i] = multi[i][j];
			}
		}
		cp.add(cp.eq(cp.count(multi_count, 1), size));

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

	public static Boolean generateLabels_CP_Choco(TwoRotational_Solution Solution) throws ContradictionException {
		Model choco = new Model("generateLabels_CP");
		int Vx = Solution.getV() - 1;
		int _D = Vx / 2;
		IntVar[] S = choco.intVarArray("S", Vx, 0, _D - 1);
		IntVar[] Y = choco.intVarArray("Y", _D, 0, _D - 1);
		IntVar[] R = choco.intVarArray("R", _D, 0, _D - 1);
		IntVar[] dY = choco.intVarArray("dY", _D - 1, 1, _D - 1);
		IntVar[] dR = choco.intVarArray("dR", _D - 1, 1, _D - 1);
		IntVar[] dYR = choco.intVarArray("dYR", _D, 0, _D - 1);

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
		if (Symmetry) {
			if (SymmetryValue != -1) {
				choco.arithm(S[Solution.getTables().get(0) - 1], "=", S[0]).post();
				choco.arithm(S[0], "=", SymmetryValue).post();
			} else

				choco.arithm(S[Solution.getTables().get(0) - 1], "=", S[0]).post();
		}

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
			// Close the table
			if (t != 0) {
				alpha = scroll + Solution.getTables().get(t) - 1;
				beta = scroll;
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
			scroll += Solution.getTables().get(t);
		}

		Solver solver = choco.getSolver();
		solver.propagate();
		if (Verbose)
			solver.showShortStatistics();

		if (exportModels)
			JSON.write(choco,
					new File(FilePath + "solved/Color_" + Solution.getName() + "_" + param_getOP_name() + ".json"));

		int Tl = 10 * (1 + V / 50);
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
			Solution.setLabellingTime(solver.getTimeCount());
			if (TimeLimit)
				if (solver.getTimeCount() >= Tl)
					Solution.setStatus("TimeLimit");
			// Solution.setStatus("TimeLimit");
			if (exportModels) {
				JSON.write(choco, new File(FilePath + "infeasibles/" + "Labelling_YR" + Solution.getName() + "_"
						+ param_getOP_name() + ".json"));
			}
			return false;
		}
	}

	public static Boolean generateLabels_CP(TwoRotational_Solution Solution) throws IloException {
		IloCP cpx = new IloCP();
		int Vx = Solution.getV() - 1;
		int _D = Vx / 2;
		IloIntVar[] S = new IloIntVar[Vx];
		S = cpx.intVarArray(Vx, 0, _D - 1, "S");
		IloIntVar[] Y = new IloIntVar[_D];
		Y = cpx.intVarArray(_D, 0, _D - 1, "Y");
		IloIntVar[] R = new IloIntVar[_D];
		R = cpx.intVarArray(_D, 0, _D - 1, "R");
		IloIntVar[] dY = new IloIntVar[_D - 1];
		dY = cpx.intVarArray(_D - 1, 1, _D - 1, "dY");
		IloIntVar[] dR = new IloIntVar[_D - 1];
		dR = cpx.intVarArray(_D - 1, 1, _D - 1, "dR");
		IloIntVar[] dYR = new IloIntVar[_D];
		dYR = cpx.intVarArray(_D, 0, _D - 1, "dYR");
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
		if (Symmetry) {
			if (SymmetryValue != -1) {
				cpx.add(cpx.eq(S[Solution.getTables().get(0) - 1], S[0]));
				cpx.add(cpx.eq(S[0], SymmetryValue));
			} else
				cpx.add(cpx.eq(S[Solution.getTables().get(0) - 1], S[0]));

		}

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
			// Close the table
			if (t != 0) {
				alpha = scroll + Solution.getTables().get(t) - 1;
				beta = scroll;
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
			scroll += Solution.getTables().get(t);
		}
		if (!Verbose)
			cpx.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
		int Tl = 5 * (1 + V / 50);

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
				cpx.exportModel(
						FilePath + "solved/" + "Labelling_YR" + Solution.getName() + "_" + param_getOP_name() + ".cpo");
			}
			cpx.end();
			return true;
		} else {
			Solution.setColorTries(Solution.getColorTries() + 1);
			Solution.setLabellingTime(cpx.getInfo(IloCP.DoubleInfo.TotalTime));
			if (TimeLimit)
				if (cpx.getInfo(IloCP.DoubleInfo.TotalTime) >= Tl)
					Solution.setStatus("TimeLimit");
			if (exportModels) {
				cpx.exportModel(FilePath + "infeasibles/" + "Labelling_YR" + Solution.getName() + "_"
						+ param_getOP_name() + ".cpo");
			}
			cpx.end();
			return false;
		}
	}

	@SuppressWarnings("unused")
	private static Boolean generateLabels_MIP(TwoRotational_Solution Solution) throws IloException {
		IloCplex mip = new IloCplex();
		IloIntVar[][] x = new IloIntVar[Solution.getColors().size()][];
		for (int i = 0; i < Solution.getColors().size(); i++) {
			x[i] = mip.intVarArray(Solution.getColors().size() / 2, 0, 1);
		}
		IloIntVar[][] a = new IloIntVar[(3 * Solution.getColors().size() - 2) / 2][];

		IloLinearNumExpr[] allDiff_r = new IloLinearNumExpr[(3 * Solution.getColors().size() - 2) / 2];
		for (int i = 0; i < (3 * Solution.getColors().size() - 2) / 2; i++) {
			a[i] = mip.intVarArray(Solution.getColors().size() / 2, 0, 1);
			allDiff_r[i] = mip.linearNumExpr();
			for (int j = 0; j < Solution.getColors().size() / 2; j++) {
				a[i][j].setName("a" + i + "_" + j);
				allDiff_r[i].addTerm(1.0, a[i][j]);
			}
			mip.addEq(allDiff_r[i], 1, "allDiff_C_c_" + i);
		}

		IloIntVar[] h = mip.intVarArray((3 * Solution.getColors().size() - 2) / 2, 0, 1);
		IloNumVar[] k = mip.numVarArray((3 * Solution.getColors().size() - 2) / 2, 0,
				Solution.getColors().size() / 2 - 1);

		IloLinearNumExpr ActiveVars = mip.linearNumExpr();
		for (int i = 0; i < Solution.getColors().size(); i++) {
			for (int j = 0; j < Solution.getColors().size() / 2; j++) {
				x[i][j].setName("x" + i + "_" + j);
				ActiveVars.addTerm(1.0, x[i][j]);
			}
		}
		mip.addEq(ActiveVars, Solution.getColors().size(), "Count(X_ijEQ1)EQ" + Solution.getColors().size());

		IloLinearNumExpr[] labelPosition = new IloLinearNumExpr[Solution.getColors().size()];
		IloLinearNumExpr[] labelLimit = new IloLinearNumExpr[Solution.getColors().size()];

		for (int i = 0; i < Solution.getColors().size(); i++) {
			labelLimit[i] = mip.linearNumExpr();
			labelPosition[i] = mip.linearNumExpr();
			for (int j = 0; j < Solution.getColors().size() / 2; j++) {
				labelPosition[i].addTerm(j, x[i][j]);
				labelLimit[i].addTerm(1.0, x[i][j]);
			}
			mip.addEq(labelLimit[i], 1, "Count(x_" + i + "jEQ1)EQ1");

		}
		int cntY = 0;
		int cntR = 0;
		IloLinearNumExpr[] labelUniqueR = new IloLinearNumExpr[Solution.getColors().size() / 2];
		IloLinearNumExpr[] labelUniqueY = new IloLinearNumExpr[Solution.getColors().size() / 2];
		for (int j = 0; j < Solution.getColors().size() / 2; j++) {
			labelUniqueR[cntR] = mip.linearNumExpr();
			labelUniqueY[cntY] = mip.linearNumExpr();
			for (int i = 0; i < Solution.getColors().size(); i++) {
				if (Solution.getColors().get(i) == 1)
					labelUniqueR[cntR].addTerm(1.0, x[i][j]);
				if (Solution.getColors().get(i) == 2)
					labelUniqueY[cntY].addTerm(1.0, x[i][j]);
			}
			mip.addEq(labelUniqueR[cntR], 1, "Red_label_" + j);
			mip.addEq(labelUniqueY[cntY], 1, "Yellow_label_" + j);
			cntR++;
			cntY++;
		}
		int cCont = 0;
		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		ArrayList<Integer> R = new ArrayList<Integer>();
		ArrayList<Integer> Y = new ArrayList<Integer>();
		ArrayList<Integer> YR = new ArrayList<Integer>();

		cntY = cntR = 0;
		for (int t = 0; t < Solution.getTables().size(); t++) {
			for (int i = 0; i < Solution.getTables().get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;

				// YY
				if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 1) {
					if (Verbose)
						System.out.println("Writing constraints YY for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[alpha], labelPosition[beta]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YY_" + alpha + "to" + beta);
					mip.addLe(1, k[cCont]);
					k[cCont].setName("k_c_" + cCont);
					h[cCont].setName("h_c_" + cCont);
					Y.add(cCont);
					cCont++;
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[beta], labelPosition[alpha]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YY_" + beta + "to" + alpha);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(1, k[cCont]);
					Y.add(cCont);
					cCont++;
				}
				// RR
				if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 2) {
					if (Verbose)
						System.out.println("Writing constraints RR for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[alpha], labelPosition[beta]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "RR_" + alpha + "to" + beta);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(1, k[cCont]);
					R.add(cCont);
					cCont++;
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[beta], labelPosition[alpha]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "RR_" + beta + "to" + alpha);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(1, k[cCont]);
					R.add(cCont);
					cCont++;
				}
				// YR
				if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 2) {
					if (Verbose)
						System.out.println("Writing constraints YR for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[alpha], labelPosition[beta]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YR_" + alpha + "to" + beta);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(0, k[cCont]);
					YR.add(cCont);
					cCont++;
				}
				// RY
				if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 1) {
					if (Verbose)
						System.out.println("Writing constraints RY for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[beta], labelPosition[alpha]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YR_" + beta + "to" + alpha);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(0, k[cCont]);
					YR.add(cCont);
					cCont++;
				}
			}
			// Close the table
			if (t != 0) {
				alpha = scroll + Solution.getTables().get(t) - 1;
				beta = scroll;

				// YY
				if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 1) {
					if (Verbose)
						System.out.println("Writing constraints YY for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[alpha], labelPosition[beta]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YY_" + alpha + "to" + beta);
					mip.addLe(1, k[cCont]);
					k[cCont].setName("k_c_" + cCont);
					h[cCont].setName("h_c_" + cCont);
					Y.add(cCont);
					cCont++;
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[beta], labelPosition[alpha]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YY_" + beta + "to" + alpha);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(1, k[cCont]);
					Y.add(cCont);
					cCont++;
				}
				// RR
				if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 2) {
					if (Verbose)
						System.out.println("Writing constraints RR for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[alpha], labelPosition[beta]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "RR_" + alpha + "to" + beta);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(1, k[cCont]);
					R.add(cCont);
					cCont++;
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[beta], labelPosition[alpha]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "RR_" + beta + "to" + alpha);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(1, k[cCont]);
					R.add(cCont);
					cCont++;
				}
				// YR
				if (Solution.getColors().get(alpha) == 1 && Solution.getColors().get(beta) == 2) {
					if (Verbose)
						System.out.println("Writing constraints YR for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[alpha], labelPosition[beta]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YR_" + alpha + "to" + beta);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(0, k[cCont]);
					YR.add(cCont);
					cCont++;
				}
				// RY
				if (Solution.getColors().get(alpha) == 2 && Solution.getColors().get(beta) == 1) {
					if (Verbose)
						System.out.println("Writing constraints RY for " + alpha + "->" + beta);
					mip.addEq(k[cCont],
							mip.sum(mip.diff(labelPosition[beta], labelPosition[alpha]),
									mip.prod((Solution.getColors().size()) / 2, h[cCont])),
							cCont + "YR_" + beta + "to" + alpha);
					h[cCont].setName("h_c_" + cCont);
					k[cCont].setName("k_c_" + cCont);
					mip.addLe(0, k[cCont]);
					YR.add(cCont);
					cCont++;
				}

			}
			scroll += Solution.getTables().get(t);
		}
		IloLinearNumExpr ActiveMonoRed = mip.linearNumExpr();
		IloLinearNumExpr ActiveMonoYellow = mip.linearNumExpr();
		IloLinearNumExpr ActiveMonoBi = mip.linearNumExpr();

		for (int j = 0; j < Y.size(); j++) {
			ActiveMonoYellow.addTerm(1.0, h[Y.get(j)]);
			mip.addEq(a[Y.get(j)][0], 0);
			for (int i = 1; i < Solution.getColors().size() / 2; i++) {
				mip.add(mip.ifThen(mip.eq(k[Y.get(j)], i), mip.eq(a[Y.get(j)][i], 1)));
			}
		}
		IloLinearNumExpr[] allDiffYellow = new IloLinearNumExpr[Solution.getColors().size() / 2];
		for (int i = 1; i < Solution.getColors().size() / 2; i++) {
			allDiffYellow[i] = mip.linearNumExpr();
			for (int j = 0; j < Y.size(); j++) {
				allDiffYellow[i].addTerm(1, a[Y.get(j)][i]);
			}
			mip.addEq(allDiffYellow[i], 1, "allDiffY_" + i);
		}

		for (int j = 0; j < R.size(); j++) {
			ActiveMonoRed.addTerm(1.0, h[R.get(j)]);
			mip.addEq(a[R.get(j)][0], 0);
			for (int i = 0; i < Solution.getColors().size() / 2; i++) {
				mip.add(mip.ifThen(mip.eq(k[R.get(j)], i), mip.eq(a[R.get(j)][i], 1)));
			}
		}

		IloLinearNumExpr[] allDiffRed = new IloLinearNumExpr[Solution.getColors().size() / 2];
		for (int i = 1; i < Solution.getColors().size() / 2; i++) {
			allDiffRed[i] = mip.linearNumExpr();
			for (int j = 0; j < R.size(); j++) {
				allDiffRed[i].addTerm(1, a[R.get(j)][i]);
			}
			mip.addEq(allDiffRed[i], 1, "allDiffR_" + i);
		}

		for (int j = 0; j < YR.size(); j++) {
			ActiveMonoBi.addTerm(1.0, h[YR.get(j)]);
			for (int i = 0; i < Solution.getColors().size() / 2; i++) {
				mip.add(mip.ifThen(mip.eq(k[YR.get(j)], i), mip.eq(a[YR.get(j)][i], 1)));
			}
		}

		IloLinearNumExpr[] allDiffYR = new IloLinearNumExpr[Solution.getColors().size() / 2];
		for (int i = 0; i < Solution.getColors().size() / 2; i++) {
			allDiffYR[i] = mip.linearNumExpr();
			for (int j = 0; j < YR.size(); j++) {
				allDiffYR[i].addTerm(1, a[YR.get(j)][i]);
			}
			mip.addEq(allDiffYR[i], 1, "allDiffYR_" + i);
		}
		mip.addEq(ActiveMonoYellow, Solution.getColors().size() / 4, "Count(h_Y)EQ" + Solution.getColors().size() / 4);
		mip.addEq(ActiveMonoRed, Solution.getColors().size() / 4, "Count(h_R)EQ" + Solution.getColors().size() / 4);
		mip.addEq(ActiveMonoBi, Solution.getColors().size() / 4, "Count(h_YR)EQ" + Solution.getColors().size() / 4);

		IloNumExpr objective = mip.numExpr();
		for (int i = 0; i < (3 * Solution.getColors().size() - 2) / 2; i++) {
			for (int j = 0; j < Solution.getColors().size() / 2; j++) {
				objective = mip.sum(objective, a[i][j]);
			}
		}
		mip.addMaximize(objective);
		mip.exportModel(FilePath + "infeasibles/MIP_" + Solution.getName() + "_" + param_getOP_name() + ".lp");
		if (!Verbose)
			mip.setParam(IloCplex.Param.MIP.Display, 0);
		Date start = new Date();
		if (mip.solve()) {
			int[] label = new int[Solution.getColors().size()];
			mip.writeSolution(FilePath + "infeasibles/MIP_" + Solution.getName() + "_" + param_getOP_name() + ".sol");
			for (int i = 0; i < Solution.getColors().size(); i++) {
				for (int j = 0; j < Solution.getColors().size() / 2; j++) {
					if (isOne(mip.getValue(x[i][j])))
						label[i] = j;

				}
			}
			Solution.setLabels(label);
			mip.end();
			return true;
		} else {
			Date end = new Date();
			Solution.setLabellingTime((end.getTime() - start.getTime()) / 1000);
			Solution.setMIP(true);
			Solution.setStatus("Infeasible");
			Solution.setColorTries(Solution.getColorTries() + 1);
			mip.end();
			return false;
		}

	}

	public ArrayList<TwoRotational_Solution> solve(ArrayList<Integer> tables)
			throws ErrorThrower, IloException, ContradictionException {

		V = getOPsize(tables);
		if (!((V % 4) == 3)) {
			throw new ErrorThrower("V % 4 != ( 3 || 0) (V=" + V + ")");
		}
		tables.set(0, tables.get(0) - 1);
		TwoRotational_Solution Solution = new TwoRotational_Solution(tables, V);
		ArrayList<Integer> firstYR = generateColors_Poly(Solution);
		Solution.setColors(firstYR);
		YR_Pool = new ArrayList<ArrayList<Integer>>();
		YR_Isomorphisms = new ArrayList<ArrayList<ArrayList<Integer>>>();
		YR_Pool.add(firstYR);
		YR_Isomorphisms.add(getColorInfo(firstYR, Solution.getTables()));

		if (!Solution.verifyColors()) {
			System.out.println(Solution.getColorsString());
			throw new ErrorThrower("Unable to generate color configuration with Poly alg.");
		}
		tables = Solution.getTables();
		param_setOP_name(tables);
		int Solve_count = 0;
		@SuppressWarnings("unused")
		Double time = 0.0;
		boolean Flag = true;
		int iteration = 0;
		ArrayList<ArrayList<Integer>> YR_cp = null;
		ArrayList<TwoRotational_Solution> Solutions = new ArrayList<TwoRotational_Solution>();

		while (Flag) {
			Clock = System.nanoTime();
			Solution = new TwoRotational_Solution(tables, V);
			Solution.setName("" + iteration);
			Solution.setOP_name(param_getOP_name());
			if (iteration == 0) {
				// System.out.println("Using Polynomial coloring.");
				Solution.setColors(generateColors_Poly(Solution));
				Solution.setPolyColor(true);
			}
			if (iteration != 0) {
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
				System.out.println("No solution found with CP.");
				/*
				 * if (generateLabels_MIP(Solution)) { Solve_count++;
				 * System.out.println("Solution found with MIP."); } else {
				 * System.out.println("Proven infeasible with MIP."); }
				 */
				if (Solution.getStatus().equals("Solved"))
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

	public ArrayList<TwoRotational_Solution> solve_onlyPoly(ArrayList<Integer> tables)
			throws ErrorThrower, IloException, ContradictionException {

		V = getOPsize(tables);
		if (!((V % 4) == 3)) {
			throw new ErrorThrower("V % 4 != ( 3 || 0) (V=" + V + ")");
		}
		tables.set(0, tables.get(0) - 1);
		TwoRotational_Solution Solution = new TwoRotational_Solution(tables, V);
		ArrayList<Integer> firstYR = generateColors_Poly(Solution);
		Solution.setColors(firstYR);

		if (!Solution.verifyColors()) {
			System.out.println(Solution.getColorsString());
			throw new ErrorThrower("Unable to generate color configuration with Poly alg.");
		}
		tables = Solution.getTables();
		param_setOP_name(tables);
		ArrayList<TwoRotational_Solution> Solutions = new ArrayList<TwoRotational_Solution>();

		Clock = System.nanoTime();
		Solution = new TwoRotational_Solution(tables, V);
		Solution.setName("0");
		Solution.setOP_name(param_getOP_name());
		Solution.setColors(generateColors_Poly(Solution));
		Solution.setPolyColor(true);
		TimeLimit = false;
		Boolean res = false;
		if (Choco)
			res = generateLabels_CP_Choco(Solution);
		else
			res = generateLabels_CP(Solution);
		if (!res) {
			System.out.println("No solution found with CP.");
			/*
			 * if (generateLabels_MIP(Solution)) { Solve_count++;
			 * System.out.println("Solution found with MIP."); } else {
			 * System.out.println("Proven infeasible with MIP."); }
			 */
			if (Solution.getStatus() == "Solved")
				Solution.setStatus("Infeasible");
		}
		Solutions.add(Solution);
		Solution.setTotalTime((System.nanoTime() - Clock) / 1000000000F);

		return Solutions;
	}

	public ArrayList<TwoRotational_Solution> solve_onlyCP(ArrayList<Integer> tables)
			throws ErrorThrower, IloException, ContradictionException {

		V = getOPsize(tables);
		if (!((V % 4) == 3)) {
			throw new ErrorThrower("V % 4 != 3 (V=" + V + ")");
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
		ArrayList<TwoRotational_Solution> Solutions = new ArrayList<TwoRotational_Solution>();

		while (Flag) {
			Clock = System.nanoTime();
			TwoRotational_Solution Solution = new TwoRotational_Solution(tables, V);
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
				System.out.println("No solution found with CP.");
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

	public TwoRotational(boolean Verbose, int SolLimit, Boolean exportModels, String FilePath, Boolean TimeLimit,
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
		TwoRotational.exportModels = exportModels;
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

	public static Boolean param_getSymmetry() {
		return Symmetry;
	}

	public void param_setSymmetry(Boolean symmetry) {
		Symmetry = symmetry;
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

	public static int param_getSymmetryValue() {
		return SymmetryValue;
	}

	public void param_setSymmetryValue(int symmetryValue) {
		SymmetryValue = symmetryValue;
	}

}
