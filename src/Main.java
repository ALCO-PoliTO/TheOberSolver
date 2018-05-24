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
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import ilog.concert.IloException;

public class Main {
	private static long Clock;
	private static String Path = "";
	private static CSVPrinter CSV_Printer = null;

	@SuppressWarnings("unused")
	private static void TimeElapsed() {
		System.out.println("Time elapsed:" + (System.nanoTime() - Clock) / 1000000000F + "s");
	}

	private static void writeDemon(int V_in) throws ErrorThrower {
		Calendar now = Calendar.getInstance();
		String unique = now.get(Calendar.HOUR_OF_DAY) + "_" + now.get(Calendar.MINUTE) + "-"
				+ ThreadLocalRandom.current().nextInt(0, 100 + 1);
		try {
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

	private static void writeDemonCSV(int V_in) throws IOException {
		BufferedWriter CSV_Writer = new BufferedWriter(new FileWriter(Path + "Solutions_" + V_in + ".csv"));
		CSV_Printer = new CSVPrinter(CSV_Writer, CSVFormat.DEFAULT.withHeader("OP_Name", "Instance", "Status",
				"ColorTime", "LabellingTime", "UsingMIP", "UsingPolyColors", "Notes", "TotalTime", "Solution"));
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws ErrorThrower, IloException, IOException {

		Clock = System.nanoTime();
		System.out.println("Excuse me, are you lazy?");
		System.out.println("1 of course - 0 not at all");
		Scanner input_zero = new Scanner(System.in);
		Scanner input = new Scanner(System.in);
		Boolean Verbose;
		Boolean AllYR;
		Boolean ExportModels;
		int SolLimit = 0;
		Boolean Check;
		if (input_zero.nextInt() == 0) {
			input_zero.close();
			System.out.println("0 quiet - 1 verbose");
			if (input.nextInt() == 1)
				Verbose = true;
			else
				Verbose = false;
			System.out.println("0 one_YR - 1 all_YR");
			if (input.nextInt() == 1)
				AllYR = true;
			else
				AllYR = false;
			System.out.println("0 no_export_models - 1 export_models");
			if (input.nextInt() == 1)
				ExportModels = true;
			else
				ExportModels = false;
			System.out.println("0 max_solutions - n solutions");
			SolLimit = input.nextInt();
			System.out.println("0 no_check_sol - 1 check_sol");
			if (input.nextInt() == 1)
				Check = true;
			else
				Check = false;

		} else {
			Verbose = false;
			AllYR = false;
			ExportModels = true;
			SolLimit = 1;
			Check = false;
		}

		System.out.println("0 partitions - 1 for instance");
		if (input.nextInt() == 0) {
			System.out.println("Insert number of nodes");
			int V_in = input.nextInt();
			TimeElapsed();
			if (!(V_in % 4 == 3)) {
				throw new ErrorThrower("V % 4 != 3");
			}
			writeDemon(V_in);
			writeDemonCSV(V_in);
			DecimalFormat df = new DecimalFormat("0.0000");
			Partition prt = new Partition(V_in, 3);
			ArrayList<ArrayList<Integer>> tables = prt.loadPartition();
			TwoRotational instance = new TwoRotational(Verbose, AllYR, Check, SolLimit, ExportModels, Path);
			for (int i = 0; i < tables.size(); i++) {
				ArrayList<TwoRotational_Solution> Solutions = instance.solve(tables.get(i));
				if (Solutions.size() > 0) {
					for (int j = 0; j < Solutions.size(); j++) {
						System.out.println("Solution for " + Solutions.get(j).getOP_name());
						System.out.println("\tStatus: " + Solutions.get(j).getStatus());
						System.out.println("\tColorTime: " + df.format(Solutions.get(j).getColorTime())
								+ " - LabellingTime: " + df.format(Solutions.get(j).getLabellingTime()));
						System.out.println("\tColorintTries: " + Solutions.get(j).getColorTries() + " - UsingMIP: "
								+ Solutions.get(j).getMIP());
						System.out.println("\tColors.Size: " + Solutions.get(j).getColors().size());
						System.out.println("\tLabels.Size: " + Solutions.get(j).getLabels().length);
						if (Check)
							System.out.println("\tVerify: " + Solutions.get(j).verify());
						CSV_Printer.printRecord(Solutions.get(j).getOP_name(), Solutions.get(j).getName(),
								Solutions.get(j).getStatus(), df.format(Solutions.get(j).getColorTime()),
								df.format(Solutions.get(j).getLabellingTime()), Solutions.get(j).getMIP(),
								Solutions.get(j).getPolyColor(), Solutions.get(j).getNotes(),
								df.format(Solutions.get(j).getTotalTime()), Solutions.get(j).getSolution());

					}
					CSV_Printer.flush();
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
			if (!(V_in % 4 == 3)) {
				throw new ErrorThrower("V % 4 != 3");
			}
			writeDemon(V_in);
			writeDemonCSV(V_in);
			DecimalFormat df = new DecimalFormat("0.0000");
			TwoRotational instance = new TwoRotational(Verbose, AllYR, Check, SolLimit, ExportModels, Path);
			ArrayList<TwoRotational_Solution> Solutions = instance.solve(tables);
			if (Solutions.size() > 0) {
				for (int i = 0; i < Solutions.size(); i++) {
					System.out.println("Solution for " + Solutions.get(i).getOP_name());
					System.out.println("\tStatus: " + Solutions.get(i).getStatus());
					System.out.println("\tColorTime: " + Solutions.get(i).getColorTime() + " - LabellingTime: "
							+ Solutions.get(i).getLabellingTime());
					System.out.println("\tColorintTries: " + Solutions.get(i).getColorTries() + " - UsingMIP: "
							+ Solutions.get(i).getMIP());
					System.out.println("\tColors.Size: " + Solutions.get(i).getColors().size());
					System.out.println("\tLabels.Size: " + Solutions.get(i).getLabels().length);
					System.out.println("\tVerify: " + Solutions.get(i).verify());
					CSV_Printer.printRecord(Solutions.get(i).getOP_name(), Solutions.get(i).getName(),
							Solutions.get(i).getStatus(), df.format(Solutions.get(i).getColorTime()),
							df.format(Solutions.get(i).getLabellingTime()), Solutions.get(i).getMIP(),
							Solutions.get(i).getPolyColor(), Solutions.get(i).getNotes(),
							df.format(Solutions.get(i).getTotalTime()), Solutions.get(i).getSolution());
				}
				CSV_Printer.close();
			} else {
				System.out.println("No Solution found.");
			}
		}
		TimeElapsed();
		input.close();

	}

}
