import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.chocosolver.solver.exception.ContradictionException;
import ilog.concert.IloException;

public class Parser4t {
	private static void writeDemon(int V_in) throws ErrorThrower {
		Calendar now = Calendar.getInstance();
		String unique = now.get(Calendar.HOUR_OF_DAY) + "_" + now.get(Calendar.MINUTE) + "-"
				+ ThreadLocalRandom.current().nextInt(0, 100 + 1);
		try {
	
			FileOutputStream fout = new FileOutputStream("parser/Instance_"+(V_in+1)+"-" + unique + ".txt");
			MultiOutputStream multiOut = new MultiOutputStream(System.out, fout);
			PrintStream stdout = new PrintStream(multiOut);
			System.setOut(stdout);
		} catch (FileNotFoundException ex) {
			throw new ErrorThrower("File for output not found.");
		}
	}
	public static void main(String[] args) throws ErrorThrower, IloException, IOException, ContradictionException {

		int V = 55;
		writeDemon(V);
		Reader reader = Files.newBufferedReader(Paths.get("parser/Solutions_" +V + "Excel.csv"));
		CSVFormat csvFormat = CSVFormat.EXCEL.withHeader().withDelimiter(';');
		CSVParser csvParser = csvFormat.parse(reader);
		DecimalFormat df = new DecimalFormat("0.0000");
		BufferedWriter CSV_Writer = new BufferedWriter(new FileWriter("parser/Solutions_" + (V + 1) + ".csv"));
		CSVPrinter CSV_Printer = new CSVPrinter(CSV_Writer,
				CSVFormat.DEFAULT.withHeader("OP_Name", "Instance", "Status", "ColorTime", "LabellingTime", "UsingMIP",
						"UsingPolyColors", "Notes", "TotalTime", "Solution"));

		double total_time = 0;
		for (CSVRecord csvRecord : csvParser) {
			if (csvRecord.get(2).equals("Solved")) {
				long Clock = System.nanoTime();
				String OP_name = csvRecord.get(0);
				String Solution = csvRecord.get(9);
				System.out.println(OP_name);
				ArrayList<Integer> tables = new ArrayList<Integer>();
				ArrayList<Integer> colors = new ArrayList<Integer>();
				OP_name = OP_name.replace("OP(", "");
				OP_name = OP_name.replace(")", "");
				String[] tables_t = OP_name.split(",");
				int size = 0;
				for (int i = 0; i < tables_t.length; i++) {
					tables.add(Integer.parseInt(tables_t[i]));
					size += Integer.parseInt(tables_t[i]);
				}
				tables.set(0, tables.get(0) - 1);
				TwoRotational_Solution Mod3Solution = new TwoRotational_Solution(tables, size);
				int[] labels = new int[size - 1];
				Mod3Solution.setStatus("Solved");
				Mod3Solution.setName(csvRecord.get(1));
				Mod3Solution.setColorTime(Double.parseDouble(csvRecord.get(3).replace(",", ".")));
				Mod3Solution.setLabellingTime(Double.parseDouble(csvRecord.get(4).replace(",", ".")));
				Mod3Solution.setMIP(Boolean.parseBoolean(csvRecord.get(5)));
				Mod3Solution.setPolyColor(Boolean.parseBoolean(csvRecord.get(6)));
				Mod3Solution.setNotes(csvRecord.get(7));
				Mod3Solution.setTotalTime(Double.parseDouble(csvRecord.get(8).replace(",", ".")));
				String[] Solution_t = Solution.split("\\) \\(");
				Solution_t[0] = Solution_t[0].replace("(\\infty", "(-1,-1)");
				Solution_t[Solution_t.length - 1] = Solution_t[Solution_t.length - 1].replace("))", ")");
				int counter = 0;
				for (int i = 0; i < Solution_t.length; i++) {
					String[] table_t = Solution_t[i].split("\\),");
					for (int j = 0; j < table_t.length; j++) {
						table_t[j] = table_t[j].replace("(", "");
						table_t[j] = table_t[j].replace(")", "");
						String[] value_t = table_t[j].split(",");
						if (!(Integer.parseInt(value_t[0].replace(" ", "")) == (-1))
								&& !(Integer.parseInt(value_t[1].replace(" ", "")) == (-1))) {
							colors.add(Integer.parseInt(value_t[1].replace(" ", "")));
							labels[counter] = Integer.parseInt(value_t[0].replace(" ", ""));
							counter++;
							//System.out.println(labels[counter - 1] + " " + colors.get(counter - 1));
						}
					}
				}
				Mod3Solution.setLabels(labels);
				Mod3Solution.setColors(colors);
				TwoRotational_Solution_M0 Solutions_Mod0 = new TwoRotational_Solution_M0(Mod3Solution);
				Solutions_Mod0.setTotalTime(Solutions_Mod0.getTotalTime() + (System.nanoTime() - Clock) / 1000000000F);
				total_time+=Solutions_Mod0.getTotalTime();
				System.out.println("Solution for " + Solutions_Mod0.getOP_name());
				System.out.println("\tStatus: " + Solutions_Mod0.getStatus());
				System.out.println("\tCritical Difference: " + Solutions_Mod0.getCriticDiff());
				System.out.println("\tCritical Table: " + Solutions_Mod0.getCriticTable());
				System.out.println("\tColorTime: " + df.format(Solutions_Mod0.getColorTime()) + " - LabellingTime: "
						+ df.format(Solutions_Mod0.getLabellingTime()));
				System.out.println("\tColorintTries: " + Solutions_Mod0.getColorTries() + " - UsingMIP: "
						+ Solutions_Mod0.getMIP());
				System.out.println("\tColors.Size: " + Solutions_Mod0.getColors().size());
				System.out.println("\tLabels.Size: " + Solutions_Mod0.getLabels().length);

				CSV_Printer.printRecord(Solutions_Mod0.getOP_name(), Solutions_Mod0.getName(),
						Solutions_Mod0.getStatus(), df.format(Solutions_Mod0.getColorTime()),
						df.format(Solutions_Mod0.getLabellingTime()), Solutions_Mod0.getMIP(),
						Solutions_Mod0.getPolyColor(), Solutions_Mod0.getNotes(),
						df.format(Solutions_Mod0.getTotalTime()), Solutions_Mod0.getSolution());
				CSV_Printer.flush();
			}
		}
		CSV_Printer.close();
		System.out.println("Virtual total time: "+total_time);
	}

}
