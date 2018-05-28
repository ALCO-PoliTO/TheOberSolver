import java.util.ArrayList;
import java.util.Collections;

public class OneRotational_Solution {
	private static ArrayList<Integer> tables = new ArrayList<Integer>();
	private int V;
	private String name;
	private String OP_name;
	private String Status = "Solved";
	private double labellingTime = -1.0;
	private Boolean MIP = false;
	private String notes = "";
	private double totalTime = -1.0;
	private int[] labels = null;

	public OneRotational_Solution(ArrayList<Integer> tables, int v) {
		super();
		setTables(tables);
		setV(v);
		setLabels(new int[V - 1]);
	}

	public boolean verify() {
		ArrayList<Integer> Diff = new ArrayList<Integer>();
		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int a = 0;
		int b = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				a = Math.abs(labels[alpha] - labels[beta]);
				b = getV() - Math.abs(labels[alpha] - labels[beta]);
				if (a < b)
					Diff.add(a);
				else
					Diff.add(b);

			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				a = Math.abs(labels[alpha] - labels[beta]);
				b = getV() - Math.abs(labels[alpha] - labels[beta]);
				if (a < b)
					Diff.add(a);
				else
					Diff.add(b);
			}
			scroll += tables.get(t);
		}
		Collections.sort(Diff);

		int nDiff = 0;
		// System.out.println("v:" + getV());
		// System.out.println("Diff:" + Diff.size());
		if (labels.length % 2 == 0) {
			int i = 0;
			int j = 1;
			while (i < getV() - 2) {
				// System.out.println(i + " and " + (i + 1) + " should be equal to " + j);
				// System.out.println(Diff.get(i) + " and " +Diff.get(i+1));
				if ((Diff.get(i) == j) && (Diff.get(i + 1) == j))
					nDiff = nDiff + 2;
				i = i + 2;
				j++;
			}
			// System.out.println((Diff.size() - 1) + " should be equal to " + (getV() /
			// 2));
			if (Diff.get(Diff.size() - 1) == (getV() / 2))
				nDiff++;
			// System.out.println(((getV() - 2) + 1)+" vs "+nDiff);
			if (nDiff == ((getV() - 2) + 1))
				return true;
			else
				return false;
		}

		if (labels.length % 2 == 1) {
			int i = 0;
			int j = 1;
			while (i < getV() - 1) {
				// System.out.println(i + " and " + (i + 1) + " should be equal to " + j);
				// System.out.println(Diff.get(i) + " and " +Diff.get(i+1));
				if ((Diff.get(i) == j) && (Diff.get(i + 1) == j))
					nDiff = nDiff + 2;
				i = i + 2;
				j++;
			}
			if (nDiff == (getV() - 1))
				return true;
			else
				return false;
		}

		return true;
	}

	public int getV() {
		return V;
	}

	public void setV(int v) {
		V = v;
	}

	public ArrayList<Integer> getTables() {
		return tables;
	}

	public void setTables(ArrayList<Integer> tables_in) {
		tables = tables_in;
	}

	public int[] getLabels() {
		return labels;
	}

	public void setLabels(int[] labels) {
		this.labels = labels;
	}

	public String getName() {
		return name;
	}

	public String getSolution() {

		String ret = "";
		int scroll = 0;
		for (int t = 0; t < tables.size(); t++) {
			ret += "(";
			for (int i = 0; i < tables.get(t); i++) {
				if (i == 0 && t == 0)
					ret += "\\infty, ";

				ret += "" + labels[i + scroll];
				if (i != tables.get(t) - 1)
					ret += ", ";
			}
			ret += ") ";
			if (t != tables.size() - 1)
				scroll += tables.get(t);
		}
		return ret;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLabellingTime() {
		return labellingTime;
	}

	public void setLabellingTime(double labellingTime) {
		this.labellingTime = labellingTime;
	}

	public Boolean getMIP() {
		return MIP;
	}

	public void setMIP(Boolean mIP) {
		MIP = mIP;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public String getOP_name() {
		return OP_name;
	}

	public void setOP_name(String oP_name) {
		OP_name = oP_name;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

}
