import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class ValidInt {

	public static void main(String[] args) throws IOException {
		BufferedWriter CSV_Writer = new BufferedWriter(new FileWriter("out/Partitions.csv"));
		CSVPrinter CSV_Printer = new CSVPrinter(CSV_Writer,
				CSVFormat.DEFAULT.withHeader("Order", "Instances", "Valid Instances"));
		for (int i = 40; i < 61; i++) {
			Partition prt = new Partition(i, 3);
			ArrayList<ArrayList<Integer>> tables = prt.loadPartition();

			int cnt = 0;
			if (i % 4 == 1) {
				OneRotational instance = new OneRotational(false, 1, false, "", false, false);

				for (int j = 0; j < tables.size(); j++) {
					if (instance.validConfiguration(tables.get(j))) {
						cnt++;
					}
				}
				CSV_Printer.printRecord(i, tables.size(), cnt);
			}

			else if (i % 4 == 2) {
				OneRotational instance = new OneRotational(false, 1, false, "", false, false);
				for (int j = 0; j < tables.size(); j++) {
					Boolean flag = true;
					for (int u = 0; u < tables.get(j).size() && flag; u++) {
						ArrayList<Integer> tcopy = new ArrayList<Integer>(tables.get(j));
						tcopy.set(u, tcopy.get(u) - 1);
						if (instance.validConfiguration(tcopy))
							flag = false;

					}
					if (flag == false)
						cnt++;
				}
				CSV_Printer.printRecord(i, tables.size(), tables.size());
			} else {
				CSV_Printer.printRecord(i, tables.size(), tables.size());
			}
			CSV_Printer.flush();
		}
		CSV_Printer.close();
	}

}
