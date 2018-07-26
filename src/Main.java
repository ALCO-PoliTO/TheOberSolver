import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.chocosolver.solver.exception.ContradictionException;

import ilog.concert.IloException;

public class Main {
	private static long Clock;
	private static String Path = "";
	private static CSVPrinter CSV_Printer = null;

	@SuppressWarnings("unused")
	private static void TimeElapsed() {
		System.out.println("Time elapsed:" + (System.nanoTime() - Clock) / 1000000000F + "s");
	}

	public static String getOP_name(ArrayList<Integer> tcopy) {
		String OP = "OP(";
		for (int t = 0; t < tcopy.size(); t++) {
			if (t != (tcopy.size() - 1)) {
				OP += tcopy.get(t) + ",";
			} else
				OP += tcopy.get(t) + ")";

		}
		return OP;
	}

	@SuppressWarnings("unused")
	private static void exportMatlab(ArrayList<Integer> tables, ArrayList<Integer> colors, String name)
			throws IOException, ErrorThrower {

		File f = new File(Path + "Matlab/");
		f.mkdirs();
		BufferedWriter bw = new BufferedWriter(new FileWriter(Path + "Matlab/Adjacency_" + name + ".txt"));
		int V = 0;
		for (int i = 0; i < tables.size(); i++)
			V += tables.get(i);
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

		int[][] defADJ = new int[V + 1][V + 1];

		for (int i = 0; i < V; i++)
			for (int j = 0; j < V; j++)
				defADJ[i + 1][j + 1] = ADJ[i][j];

		defADJ[0][1] = 1;
		defADJ[1][0] = 1;
		defADJ[0][tables.get(0)] = 1;
		defADJ[tables.get(0)][0] = 1;

		for (int i = 0; i < defADJ.length; i++) {
			for (int j = 0; j < defADJ.length; j++) {
				if (j != V) {
					bw.write(defADJ[i][j] + "\t");
				} else {
					bw.write("" + defADJ[i][j]);
				}
			}
			bw.write("\n");

		}
		bw.close();
		bw = new BufferedWriter(new FileWriter(Path + "YR_" + name + ".txt"));
		ArrayList<Integer> Y = new ArrayList<Integer>();
		ArrayList<Integer> R = new ArrayList<Integer>();
		for (int i = 0; i < V; i++) {
			if (colors.get(i) == 1)
				Y.add(i + 1);
			else
				R.add(i + 1);
		}
		Collections.sort(Y);
		Collections.sort(R);
		for (int i = 0; i < Y.size(); i++) {
			if (i != Y.size())
				bw.write(Y.get(i) + "\t");
			else
				bw.write("" + Y.get(i));

		}
		bw.write("\n");
		for (int i = 0; i < R.size(); i++) {
			if (i != Y.size())
				bw.write(R.get(i) + "\t");
			else
				bw.write("" + R.get(i));

		}
		bw.close();
	}

	private static void writeDemon(int V_in) throws ErrorThrower {
		Calendar now = Calendar.getInstance();
		String unique = now.get(Calendar.HOUR_OF_DAY) + "_" + now.get(Calendar.MINUTE) + "-"
				+ ThreadLocalRandom.current().nextInt(0, 100 + 1);
		try {
			File o = new File("out/");
			o.mkdirs();
			Path = "out/OP_" + V_in + "_at" + unique + "/";
			File f = new File(Path);
			f.mkdirs();
			String Solved = Path + "solved/";
			File f2 = new File(Solved);
			f2.mkdirs();
			String Infeasible = Path + "infeasibles/";
			File f3 = new File(Infeasible);
			f3.mkdirs();
			FileOutputStream fout = new FileOutputStream(Path + "/Instance_" + unique + ".txt");
			MultiOutputStream multiOut = new MultiOutputStream(System.out, fout);
			PrintStream stdout = new PrintStream(multiOut);
			System.setOut(stdout);
		} catch (FileNotFoundException ex) {
			throw new ErrorThrower("File for output not found.");
		}
	}

	private static void writeDemonCSV(int V_in, int Type) throws IOException {
		BufferedWriter CSV_Writer = new BufferedWriter(new FileWriter(Path + "Solutions_" + V_in + ".csv"));
		if (Type == 2)
			CSV_Printer = new CSVPrinter(CSV_Writer, CSVFormat.DEFAULT.withHeader("OP_Name", "Instance", "Status",
					"ColorTime", "LabellingTime", "UsingMIP", "UsingPolyColors", "Notes", "TotalTime", "Solution"));
		else
			CSV_Printer = new CSVPrinter(CSV_Writer, CSVFormat.DEFAULT.withHeader("OP_Name", "Instance", "Status",
					"LabellingTime", "Notes", "TotalTime", "Solution"));

	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws ErrorThrower, IloException, IOException, ContradictionException {

		Clock = System.nanoTime();
		System.out.println("Excuse me, are you lazy?");
		System.out.println("1 of course - 0 not at all");
		Scanner input = new Scanner(System.in);
		Boolean Verbose = false;
		int RotationalType = 3;
		int SymmetryValue = 2;
		Boolean ExportModels = false;
		Boolean Choco = false;
		Boolean onlyPoly = false;
		Boolean onlyCP = true;
		Boolean TimeLimit = false;
		Boolean Symmetry = false;
		int SolLimit = 0;
		Boolean Check = false;
		if (input.nextInt() == 0) {
			System.out.println("1 OneRotational - 2 TwoRotational");
			switch (input.nextInt()) {
			case 1:
				RotationalType = 1;
				break;
			case 2:
				RotationalType = 2;
				break;
			default:
				RotationalType = 2;
			}
			System.out.println("0 quiet - 1 verbose");
			if (input.nextInt() == 1)
				Verbose = true;
			else
				Verbose = false;
			if (RotationalType == 2) {
				System.out.println("0 all_colorings - 1 only_algorithm - 2 only_CP");
				if (input.nextInt() == 1) {
					onlyPoly = true;
					onlyCP = false;
				} else if (input.nextInt() == 0) {
					onlyPoly = false;
					onlyCP = false;
				} else if (input.nextInt() == 2) {
					onlyCP = true;
					onlyPoly = false;
				}
				System.out.println("0 no_symmetry - 1 symmetry");
				if (input.nextInt() == 1)
					Symmetry = true;
				else
					Symmetry = false;
			}
			System.out.println("0 no_export_models - 1 export_models");
			if (input.nextInt() == 1)
				ExportModels = true;
			else
				ExportModels = false;
			System.out.println("0 no_TimeLimit - 1 TimeLimit");
			if (input.nextInt() == 1)
				TimeLimit = true;
			else
				TimeLimit = false;
			System.out.println("0 max_solutions - n solutions");
			SolLimit = input.nextInt();
			System.out.println("0 no_check_sol - 1 check_sol");
			if (input.nextInt() == 1)
				Check = true;
			else
				Check = false;
			System.out.println("0 CPLEX - 1 Choco");
			if (input.nextInt() == 1)
				Choco = true;
			else
				Choco = false;

		} else {
			System.out.println("Lazy configuration: ");
			TimeLimit = true;
			Choco = false;
			Verbose = false;
			onlyPoly = false;
			onlyCP = true;
			ExportModels = false;
			SolLimit = 1;
			Symmetry = false;
			SymmetryValue = 6;
			System.out.println("\tChoco=" + Choco + ";Timelimit=" + TimeLimit + ";Verbose=" + Verbose + ";onlyPoly="
					+ onlyPoly + ";onlyCP=" + onlyCP + ";ExportModels=" + ExportModels + ";SolLimit=" + SolLimit
					+ ";Symmetry=" + Symmetry + ";");
			System.out.println("1 OneRotational - 2 TwoRotational");
			switch (input.nextInt()) {
			case 1:
				RotationalType = 1;
				break;
			case 2:
				RotationalType = 2;
				break;
			default:
				RotationalType = 2;
			}
		}

		switch (RotationalType) {
		case 2: {
			System.out.println("0 partitions - 1 for instance");
			if (input.nextInt() == 0) {
				System.out.println("Insert number of nodes");
				int V_in = input.nextInt();
				TimeElapsed();
				if (!(V_in % 4 == 3) && !(V_in % 4 == 0)) {
					throw new ErrorThrower("V % 4 != 3");
				}
				Boolean Mod0 = false;
				if (V_in % 4 == 0) {
					Mod0 = true;
				}
				writeDemon(V_in);
				writeDemonCSV(V_in, 2);
				DecimalFormat df = new DecimalFormat("0.0000");
				Partition prt = new Partition(V_in, 3);
				ArrayList<ArrayList<Integer>> tables = prt.loadPartition();
				TwoRotational instance = new TwoRotational(Verbose, SolLimit, ExportModels, Path, TimeLimit, Choco);
				if (Symmetry)
					instance.param_setSymmetry(true);
				if (Symmetry && SymmetryValue != -1)
					instance.param_setSymmetryValue(SymmetryValue);
				for (int i = 0; i < tables.size(); i++) {
					ArrayList<TwoRotational_Solution> Solutions = new ArrayList<TwoRotational_Solution>();

					Boolean flag = false;
					// 4t Solution
					if (Mod0) {

						for (int u = 0; u < tables.get(i).size() && !flag; u++) {
							if (tables.get(i).get(u) > 3 || (u == 0 && tables.get(i).get(u) > 3)) {
								tables.get(i).set(u, (tables.get(i).get(u) - 1));
								flag = true;
							}
						}
					}

					if (onlyPoly && !onlyCP)
						Solutions = instance.solve_onlyPoly(tables.get(i));
					else if (!onlyPoly && onlyCP)
						Solutions = instance.solve_onlyCP(tables.get(i));
					else
						Solutions = instance.solve(tables.get(i));

					// 4t solution

					if (Solutions.size() > 0) {
						for (int j = 0; j < Solutions.size(); j++) {
							if (!Mod0) {
								System.out.println("Solution for " + Solutions.get(j).getOP_name());
								System.out.println("\tStatus: " + Solutions.get(j).getStatus());
								System.out.println("\tColorTime: " + df.format(Solutions.get(j).getColorTime())
										+ " - LabellingTime: " + df.format(Solutions.get(j).getLabellingTime()));
								System.out.println("\tColorintTries: " + Solutions.get(j).getColorTries()
										+ " - UsingMIP: " + Solutions.get(j).getMIP());
								System.out.println("\tColors.Size: " + Solutions.get(j).getColors().size());
								System.out.println("\tLabels.Size: " + Solutions.get(j).getLabels().length);
								if (Check)
									System.out.println("\tVerify: " + Solutions.get(j).verify());
								if (Solutions.get(j).getStatus().equals("Solved"))
									CSV_Printer.printRecord(Solutions.get(j).getOP_name(), Solutions.get(j).getName(),
											Solutions.get(j).getStatus(), df.format(Solutions.get(j).getColorTime()),
											df.format(Solutions.get(j).getLabellingTime()), Solutions.get(j).getMIP(),
											Solutions.get(j).getPolyColor(), Solutions.get(j).getNotes(),
											df.format(Solutions.get(j).getTotalTime()), Solutions.get(j).getSolution());
								else
									CSV_Printer.printRecord(Solutions.get(j).getOP_name(), Solutions.get(j).getName(),
											Solutions.get(j).getStatus(), df.format(Solutions.get(j).getColorTime()),
											df.format(Solutions.get(j).getLabellingTime()), Solutions.get(j).getMIP(),
											Solutions.get(j).getPolyColor(), Solutions.get(j).getNotes(),
											df.format(Solutions.get(j).getTotalTime()),
											Solutions.get(j).getColorsString());
							} else {
								if (flag) {
									if (Solutions.get(j).getStatus().equals("Solved")) {
										System.out.println("Converting 4t+3 to 4t...");
										TwoRotational_Solution_M0 Solutions_Mod0 = new TwoRotational_Solution_M0(
												Solutions.get(j));

										System.out.println("Solution for " + Solutions_Mod0.getOP_name());
										System.out.println("\tStatus: " + Solutions_Mod0.getStatus());
										System.out.println("\tCritical Difference: " + Solutions_Mod0.getCriticDiff());
										System.out.println("\tCritical Table: " + Solutions_Mod0.getCriticTable());
										System.out.println("\tColorTime: " + df.format(Solutions_Mod0.getColorTime())
												+ " - LabellingTime: " + df.format(Solutions_Mod0.getLabellingTime()));
										System.out.println("\tColorintTries: " + Solutions_Mod0.getColorTries()
												+ " - UsingMIP: " + Solutions_Mod0.getMIP());
										System.out.println("\tColors.Size: " + Solutions_Mod0.getColors().size());
										System.out.println("\tLabels.Size: " + Solutions_Mod0.getLabels().length);

										if (Solutions.get(j).getStatus().equals("Solved"))
											CSV_Printer.printRecord(Solutions_Mod0.getOP_name(),
													Solutions_Mod0.getName(), Solutions_Mod0.getStatus(),
													df.format(Solutions_Mod0.getColorTime()),
													df.format(Solutions_Mod0.getLabellingTime()),
													Solutions_Mod0.getMIP(), Solutions_Mod0.getPolyColor(),
													Solutions_Mod0.getNotes(), df.format(Solutions_Mod0.getTotalTime()),
													Solutions_Mod0.getSolution());
										else
											CSV_Printer.printRecord(Solutions_Mod0.getOP_name(),
													Solutions_Mod0.getName(), Solutions_Mod0.getStatus(),
													df.format(Solutions_Mod0.getColorTime()),
													df.format(Solutions_Mod0.getLabellingTime()),
													Solutions_Mod0.getMIP(), Solutions_Mod0.getPolyColor(),
													Solutions_Mod0.getNotes(), df.format(Solutions_Mod0.getTotalTime()),
													Solutions_Mod0.getColorsString());
									}
								}
							}
						}
						CSV_Printer.flush();
					}
					System.gc();
				}
				CSV_Printer.close();
			} else {
				System.out.println("Insert the length of the next table. -1 to end input");
				ArrayList<Integer> tables = new ArrayList<Integer>();
				int num;
				int V_in = 0;
				while ((num = input.nextInt()) > 0) {
					tables.add(num);
					V_in += num;
				}
				if (!(V_in % 4 == 3) && !(V_in % 4 == 0)) {
					throw new ErrorThrower("V % 4 != 3");
				}
				Boolean Mod0 = false;
				if (V_in % 4 == 0) {
					Mod0 = true;
				}
				writeDemon(V_in);
				writeDemonCSV(V_in, 2);
				DecimalFormat df = new DecimalFormat("0.0000");
				TwoRotational instance = new TwoRotational(Verbose, SolLimit, ExportModels, Path, TimeLimit, Choco);
				if (Symmetry)
					instance.param_setSymmetry(true);
				if (Symmetry && SymmetryValue != -1)
					instance.param_setSymmetryValue(SymmetryValue);
				ArrayList<TwoRotational_Solution> Solutions = new ArrayList<TwoRotational_Solution>();

				Boolean flag = false;
				// 4t Solution
				if (Mod0) {
					for (int u = 0; u < tables.size() && !flag; u++) {
						if (tables.get(u) > 3 || (u == 0 && tables.get(u) > 3)) {
							tables.set(u, (tables.get(u) - 1));
							flag = true;
						}
					}
				}

				if (onlyPoly && !onlyCP)
					Solutions = instance.solve_onlyPoly(tables);
				else if (!onlyPoly && onlyCP)
					Solutions = instance.solve_onlyCP(tables);
				else {
					Solutions = instance.solve(tables);
				}

				if (Solutions.size() > 0) {
					for (int i = 0; i < Solutions.size(); i++) {

						if (!Mod0) {
							System.out.println("Solution for " + Solutions.get(i).getOP_name());
							System.out.println("\tStatus: " + Solutions.get(i).getStatus());
							System.out.println("\tColorTime: " + df.format(Solutions.get(i).getColorTime())
									+ " - LabellingTime: " + df.format(Solutions.get(i).getLabellingTime()));
							System.out.println("\tColorintTries: " + Solutions.get(i).getColorTries() + " - UsingMIP: "
									+ Solutions.get(i).getMIP());
							System.out.println("\tColors.Size: " + Solutions.get(i).getColors().size());
							System.out.println("\tLabels.Size: " + Solutions.get(i).getLabels().length);
							if (Check)
								System.out.println("\tVerify: " + Solutions.get(i).verify());
							if (Solutions.get(i).getStatus().equals("Solved"))
								CSV_Printer.printRecord(Solutions.get(i).getOP_name(), Solutions.get(i).getName(),
										Solutions.get(i).getStatus(), df.format(Solutions.get(i).getColorTime()),
										df.format(Solutions.get(i).getLabellingTime()), Solutions.get(i).getMIP(),
										Solutions.get(i).getPolyColor(), Solutions.get(i).getNotes(),
										df.format(Solutions.get(i).getTotalTime()), Solutions.get(i).getSolution());
							else
								CSV_Printer.printRecord(Solutions.get(i).getOP_name(), Solutions.get(i).getName(),
										Solutions.get(i).getStatus(), df.format(Solutions.get(i).getColorTime()),
										df.format(Solutions.get(i).getLabellingTime()), Solutions.get(i).getMIP(),
										Solutions.get(i).getPolyColor(), Solutions.get(i).getNotes(),
										df.format(Solutions.get(i).getTotalTime()), Solutions.get(i).getColorsString());
						} else {
							if (flag) {
								if (Solutions.get(i).getStatus().equals("Solved")) {
									TwoRotational_Solution_M0 Solutions_Mod0 = new TwoRotational_Solution_M0(
											Solutions.get(i));
									System.out.println("Solution for " + Solutions_Mod0.getOP_name());
									System.out.println("\tStatus: " + Solutions_Mod0.getStatus());
									System.out.println("\tCritical Difference: " + Solutions_Mod0.getCriticDiff());
									System.out.println("\tCritical Table: " + Solutions_Mod0.getCriticTable());
									System.out.println("\tColorTime: " + df.format(Solutions_Mod0.getColorTime())
											+ " - LabellingTime: " + df.format(Solutions_Mod0.getLabellingTime()));
									System.out.println("\tColorintTries: " + Solutions_Mod0.getColorTries()
											+ " - UsingMIP: " + Solutions_Mod0.getMIP());
									System.out.println("\tColors.Size: " + Solutions_Mod0.getColors().size());
									System.out.println("\tLabels.Size: " + Solutions_Mod0.getLabels().length);
									if (Solutions.get(i).getStatus().equals("Solved"))
										CSV_Printer.printRecord(Solutions_Mod0.getOP_name(), Solutions_Mod0.getName(),
												Solutions_Mod0.getStatus(), df.format(Solutions_Mod0.getColorTime()),
												df.format(Solutions_Mod0.getLabellingTime()), Solutions_Mod0.getMIP(),
												Solutions_Mod0.getPolyColor(), Solutions_Mod0.getNotes(),
												df.format(Solutions_Mod0.getTotalTime()), Solutions_Mod0.getSolution());
									else
										CSV_Printer.printRecord(Solutions_Mod0.getOP_name(), Solutions_Mod0.getName(),
												Solutions_Mod0.getStatus(), df.format(Solutions_Mod0.getColorTime()),
												df.format(Solutions_Mod0.getLabellingTime()), Solutions_Mod0.getMIP(),
												Solutions_Mod0.getPolyColor(), Solutions_Mod0.getNotes(),
												df.format(Solutions_Mod0.getTotalTime()),
												Solutions_Mod0.getColorsString());
								}
							}
						}
					}

					CSV_Printer.close();
				} else {
					System.out.println("No Solution found.");
				}
			}
		}
			break;
		case 1: {

			System.out.println("0 partitions - 1 for instance");
			if (input.nextInt() == 0) {
				System.out.println("Insert number of nodes");
				int V_in = input.nextInt();
				TimeElapsed();
				if (!(V_in > 2)) {
					throw new ErrorThrower("V < 3");
				}
				if (!(V_in % 4 == 1) && !(V_in % 4 == 2)) {
					throw new ErrorThrower("V % 4 != 2 && V % 4 != 1");
				}
				Boolean Mod2 = false;
				if (V_in % 4 == 2) {
					Mod2 = true;
				}

				writeDemon(V_in);
				writeDemonCSV(V_in, 1);
				DecimalFormat df = new DecimalFormat("0.0000");
				Partition prt = new Partition(V_in, 3);
				ArrayList<ArrayList<Integer>> tables = prt.loadPartition();
				OneRotational instance = new OneRotational(Verbose, SolLimit, ExportModels, Path, TimeLimit, Choco);
				for (int i = 0; i < tables.size(); i++) {
					ArrayList<OneRotational_Solution> Solutions = new ArrayList<OneRotational_Solution>();
					ArrayList<Integer> tcopy = null;
					// 4t Solution
					if (Mod2) {
						Boolean flag = true;

						for (int u = 0; u < tables.get(i).size() && flag; u++) {
							tcopy = new ArrayList<Integer>(tables.get(i));
							tcopy.set(u, tcopy.get(u) - 1);
							if (instance.validConfiguration(tcopy))
								flag = false;

						}
						if (flag == false) {
							Solutions = instance.solve(tcopy);
							System.out.println("\tConverting 4t+1 to 4t+2...");
							if (Solutions.size() > 0) {
								for (int j = 0; j < Solutions.size(); j++) {
									if (Solutions.get(j).getStatus().equals("Solved")) {
										OneRotational_SolutionM2 Solutions_Mod2 = new OneRotational_SolutionM2(
												Solutions.get(j));

										System.out.println("Solution for " + Solutions_Mod2.getOP_name());
										System.out.println("\tDerived from " + getOP_name(tcopy));
										System.out.println("\tMinimal Problem: " + Solutions_Mod2.getOP_nameRed());
										System.out.println("\tStatus: " + Solutions_Mod2.getStatus());
										System.out.println(
												"\tLabellingTime: " + df.format(Solutions_Mod2.getLabellingTime()));
										System.out.println("\tLabels.Size: " + Solutions_Mod2.getLabels().length);
										if (Check)
											System.out.println("\tVerify: " + Solutions_Mod2.verify());

										CSV_Printer.printRecord(Solutions_Mod2.getOP_name(), Solutions_Mod2.getName(),
												Solutions_Mod2.getStatus(),
												df.format(Solutions_Mod2.getLabellingTime()), Solutions_Mod2.getNotes(),
												df.format(Solutions_Mod2.getTotalTime()), Solutions_Mod2.getSolution());

									} else {
										CSV_Printer.printRecord(getOP_name(tables.get(i)), Solutions.get(j).getName(),
												Solutions.get(j).getStatus(),
												df.format(Solutions.get(j).getLabellingTime()),
												"No solution for 4t+1 " + getOP_name(tcopy) + " "
														+ Solutions.get(j).getNotes(),
												df.format(Solutions.get(j).getTotalTime()),
												Solutions.get(j).getSolution());

									}
								}
								CSV_Printer.flush();
							} else {
								CSV_Printer.printRecord(getOP_name(tables.get(i)), "", "Infeasible", "",
										"No solution found", "", "");
							}
						} else {
							CSV_Printer.printRecord(getOP_name(tables.get(i)), "", "Infeasible", "",
									"Table config is not valid", "", "");
						}

					} else {
						if (instance.validConfiguration(tables.get(i))) {
							Solutions = instance.solve(tables.get(i));
							if (Solutions.size() > 0) {
								for (int j = 0; j < Solutions.size(); j++) {
									System.out.println("Solution for " + Solutions.get(j).getOP_name());
									System.out.println("\tMinimal Problem: " + Solutions.get(j).getOP_nameRed());
									System.out.println("\tStatus: " + Solutions.get(j).getStatus());
									System.out.println(
											"\tLabellingTime: " + df.format(Solutions.get(j).getLabellingTime()));
									System.out.println("\tLabels.Size: " + Solutions.get(j).getLabels().length);
									if (Check)
										System.out.println("\tVerify: " + Solutions.get(j).verify());

									CSV_Printer.printRecord(Solutions.get(j).getOP_name(), Solutions.get(j).getName(),
											Solutions.get(j).getStatus(),
											df.format(Solutions.get(j).getLabellingTime()), Solutions.get(j).getNotes(),
											df.format(Solutions.get(j).getTotalTime()), Solutions.get(j).getSolution());

								}
								CSV_Printer.flush();
							} else {
								CSV_Printer.printRecord(getOP_name(tables.get(i)), "", "Infeasible", "",
										"No solution found", "", "");
							}
						} else {
							CSV_Printer.printRecord(getOP_name(tables.get(i)), "", "Infeasible", "",
									"Table config is not valid", "", "");
						}

					}

				}
				CSV_Printer.close();

			} else {
				System.out.println("Insert the length of the next table. -1 to end input");
				ArrayList<Integer> tables = new ArrayList<Integer>();
				int num;
				int V_in = 0;
				while ((num = input.nextInt()) > 0) {
					tables.add(num);
					V_in += num;
				}
				if (!(V_in > 2)) {
					throw new ErrorThrower("V < 3");
				}
				Boolean Mod2 = false;
				if (V_in % 4 == 2) {
					Mod2 = true;
				}
				writeDemon(V_in);
				writeDemonCSV(V_in, 1);
				DecimalFormat df = new DecimalFormat("0.0000");
				OneRotational instance = new OneRotational(Verbose, SolLimit, ExportModels, Path, TimeLimit, Choco);
				ArrayList<OneRotational_Solution> Solutions = new ArrayList<OneRotational_Solution>();
				ArrayList<Integer> tcopy = null;

				if (Mod2) {
					Boolean flag = true;

					for (int u = 0; u < tables.size() && flag; u++) {
						tcopy = new ArrayList<Integer>(tables);
						tcopy.set(u, tcopy.get(u) - 1);
						if (instance.validConfiguration(tcopy))
							flag = false;
					}
					if (flag == false) {
						Solutions = instance.solve(tcopy);
						System.out.println("\tConverting 4t+1 to 4t+2...");
						if (Solutions.size() > 0) {
							for (int j = 0; j < Solutions.size(); j++) {
								if (Solutions.get(j).getStatus().equals("Solved")) {
									OneRotational_SolutionM2 Solutions_Mod2 = new OneRotational_SolutionM2(
											Solutions.get(j));

									System.out.println("Solution for " + Solutions_Mod2.getOP_name());
									System.out.println("\tDerived from " + getOP_name(tcopy));
									System.out.println("\tMinimal Problem: " + Solutions_Mod2.getOP_nameRed());
									System.out.println("\tStatus: " + Solutions_Mod2.getStatus());
									System.out.println(
											"\tLabellingTime: " + df.format(Solutions_Mod2.getLabellingTime()));
									System.out.println("\tLabels.Size: " + Solutions_Mod2.getLabels().length);
									if (Check)
										System.out.println("\tVerify: " + Solutions_Mod2.verify());

									CSV_Printer.printRecord(Solutions_Mod2.getOP_name(), Solutions_Mod2.getName(),
											Solutions_Mod2.getStatus(), df.format(Solutions_Mod2.getLabellingTime()),
											Solutions_Mod2.getNotes(), df.format(Solutions_Mod2.getTotalTime()),
											Solutions_Mod2.getSolution());

								} else {
									CSV_Printer.printRecord(getOP_name(tables), Solutions.get(j).getName(),
											Solutions.get(j).getStatus(),
											df.format(Solutions.get(j).getLabellingTime()),
											"No solution for 4t+1 " + getOP_name(tcopy) + " "
													+ Solutions.get(j).getNotes(),
											df.format(Solutions.get(j).getTotalTime()), Solutions.get(j).getSolution());
								}
							}
							CSV_Printer.flush();
						} else {
							CSV_Printer.printRecord(getOP_name(tcopy), "", "Infeasible", "", "No solution found", "",
									"");
						}
					} else {
						CSV_Printer.printRecord(getOP_name(tcopy), "", "Infeasible", "", "Table config is not valid",
								"", "");
					}

				} else {
					if (instance.validConfiguration(tables)) {
						Solutions = instance.solve(tables);
						if (Solutions.size() > 0) {
							for (int j = 0; j < Solutions.size(); j++) {
								System.out.println("Solution for " + Solutions.get(j).getOP_name());
								System.out.println("\tMinimal Problem: " + Solutions.get(j).getOP_nameRed());
								System.out.println("\tStatus: " + Solutions.get(j).getStatus());
								System.out
										.println("\tLabellingTime: " + df.format(Solutions.get(j).getLabellingTime()));
								System.out.println("\tLabels.Size: " + Solutions.get(j).getLabels().length);
								if (Check)
									System.out.println("\tVerify: " + Solutions.get(j).verify());

								CSV_Printer.printRecord(Solutions.get(j).getOP_name(), Solutions.get(j).getName(),
										Solutions.get(j).getStatus(), df.format(Solutions.get(j).getLabellingTime()),
										Solutions.get(j).getNotes(), df.format(Solutions.get(j).getTotalTime()),
										Solutions.get(j).getSolution());

							}
							CSV_Printer.flush();
						} else {
							CSV_Printer.printRecord(getOP_name(tables), "", "Infeasible", "", "No solution found", "",
									"");
						}
					} else {
						CSV_Printer.printRecord(getOP_name(tcopy), "", "Infeasible", "", "Table config is not valid",
								"", "");
					}

				}
				CSV_Printer.close();
			}

		}
			break;
		}

		TimeElapsed();
		input.close();

	}

}
