import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Partition {

	private static int V;
	private static int Partitions;
	static ArrayList<ArrayList<Integer>> tables = new ArrayList<ArrayList<Integer>>();

	public Partition(int V, int Partitions) {
		setV(V);
		setPartitions(Partitions);
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public ArrayList<ArrayList<Integer>> loadPartition() throws IOException {
		File f = new File("Partitions/Partition_" + getV() + ".dat");
		if (f.exists() && !f.isDirectory()) {
			return loadFromFile();
		} else {
			System.out.println("Writing partitions into file.");
			genPartitions(getV(), getV(), "");
			writePartitions();
			return tables;
		}
	}

	private static void writePartitions() throws IOException {
		File f = new File("Partitions/");
		f.mkdirs();
		BufferedWriter bw = new BufferedWriter(new FileWriter("Partitions/Partition_" + getV() + ".dat", true));
		for (int i = 0; i < tables.size(); i++) {
			ArrayList<Integer> table = tables.get(i);
			for (int j = 0; j < table.size(); j++) {
				if (j != table.size() - 1)
					bw.write(table.get(j) + " ");
				else
					bw.write(""+table.get(j));
			}
			bw.write("\n");
		}
		bw.close();
	}

	public void outPartitions() {
		for (int i = 0; i < tables.size(); i++) {
			ArrayList<Integer> table = tables.get(i);
			for (int j = 0; j < table.size(); j++) {
				if (j != table.size() - 1)
					System.out.print(table.get(j) + " ");
				else
					System.out.print(table.get(j));
			}
			System.out.println("");
		}
	}

	private static ArrayList<ArrayList<Integer>> loadFromFile() throws FileNotFoundException {
		System.out.println("Partition file existing. Loading from file");
		Scanner input = new Scanner(new File("Partitions/Partition_" + getV() + ".dat"));
		while (input.hasNextLine()) {
			ArrayList<Integer> table = new ArrayList<Integer>();
			Scanner colReader = new Scanner(input.nextLine());
			while (colReader.hasNextInt()) {
				table.add(colReader.nextInt());
			}
			colReader.close();
			Collections.sort(table, Collections.reverseOrder());
			tables.add(table);
		}
		input.close();
		return tables;
	}

	public void genPartitions(int n, int max, String prefix) {
		if (n == 0) {
			if (prefix.contains(" ")) {
				String[] tokens = prefix.split(" ");
				if (tokens.length > Partitions) {
					ArrayList<Integer> table = new ArrayList<Integer>();
					for (String token : tokens) {
						if (isNumeric(token)) {
							table.add(Integer.parseInt(token));
						}
					}
					Collections.sort(table, Collections.reverseOrder());
					tables.add(table);
				}
			}
		}

		for (int i = Math.min(max, n); i >= 3; i--) {
			genPartitions(n - i, i, prefix + " " + i);
		}
	}

	public static int getV() {
		return V;
	}

	public static void setV(int v) {
		V = v;
	}

	public static int getPartitions() {
		return Partitions;
	}

	public static void setPartitions(int partitions) {
		Partitions = partitions;
	}
}
