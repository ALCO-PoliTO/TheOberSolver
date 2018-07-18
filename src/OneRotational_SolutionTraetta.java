import java.util.ArrayList;
import java.util.Collections;

public class OneRotational_SolutionTraetta {
	private static ArrayList<Integer> tables = new ArrayList<Integer>();
	private static ArrayList<Integer> tablesRed = new ArrayList<Integer>();
	private int V;
	private String name;
	private String OP_name;
	private String OP_name_red;
	private String Status = "Solved";
	private double labellingTime = -1.0;
	private Boolean MIP = false;
	private String notes = "";
	private double totalTime = -1.0;
	private int[] labels = null;

	public OneRotational_SolutionTraetta(ArrayList<Integer> tables, int v) {
		super();
		setTables(tables);
		setV(v);
		setLabels(new int[V]);
	}

	public boolean verify() {
		ArrayList<Integer> Diff = new ArrayList<Integer>();
		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int a = 0;
		int b = 0;
		int n = (V - 1) / 2;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (labels[alpha] != -1 && labels[beta] != -1) {
					a = (labels[alpha] - labels[beta] + 2 * n) % (2 * n);
					b = (labels[beta] - labels[alpha] + 2 * n) % (2 * n);
					Diff.add(a);
					Diff.add(b);
				}
			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				if (labels[alpha] != -1 && labels[beta] != -1) {
					a = (labels[alpha] - labels[beta] + 2 * n) % (2 * n);
					b = (labels[beta] - labels[alpha] + 2 * n) % (2 * n);
					Diff.add(a);
					Diff.add(b);
				}
			}
			scroll += tables.get(t);
		}
		int count = 0;
		int count2 = 0;
		ArrayList<Integer> labelsList = new ArrayList<Integer>();
		for (int i = 0; i < labels.length; i++)
			labelsList.add(labels[i]);

		for (int i = 1; i < (2 * n); i++)
			if (Collections.frequency(Diff, i) == 2)
				count++;
		for (int i = 0; i < (2 * n - 1); i++)
			if (Collections.frequency(labelsList, i) == 1)
				count2++;

		if (Collections.frequency(labelsList, -1) == 1)
			count2++;

		if (count == (2 * n - 1) && count2 == (2 * n))
			return true;
		else
			return false;

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
				if (labels[i + scroll] == (-1))
					ret += "\\infty, ";
				else
					ret += "" + labels[i + scroll];
				if (i != tables.get(t) - 1 && labels[i + scroll] != -1)
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

	public String getOP_nameRed() {
		return OP_name_red;
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

	public static ArrayList<Integer> getTablesRed() {
		return tablesRed;
	}

	public void setTablesRed(ArrayList<Integer> tred) {
		tablesRed = tred;
		String OP = "(";
		for (int t = 0; t < tablesRed.size(); t++) {
			if (t != (tablesRed.size() - 1)) {
				OP += tablesRed.get(t) + ",";
			} else
				OP += tablesRed.get(t) + ")";

		}
		OP_name_red = OP;
	}

}
