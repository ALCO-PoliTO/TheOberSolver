import java.util.ArrayList;
import java.util.Collections;

public class TwoRotational_Solution_M0 {
	private static ArrayList<Integer> tables = new ArrayList<Integer>();
	private int V;
	private int criticTable = -1;
	private int criticIndex = -1;
	private int criticDiff = -1;
	private String name;
	private String OP_name;
	private String Status = "Solved";
	private double labellingTime = -1.0;
	private Boolean MIP = false;
	private Boolean PolyColor = false;
	private int ColorTries = 0;
	private String notes = "";
	private double colorTime = -1.0;
	private double totalTime = -1.0;
	private ArrayList<Integer> colors = new ArrayList<Integer>();
	private int[] labels = null;

	public TwoRotational_Solution_M0(TwoRotational_Solution Solution) {
		setTables(Solution.getTables());
		setV(Solution.getV() + 1);
		setLabels(Solution.getLabels());
		setColors(Solution.getColors());
		setLabellingTime(Solution.getLabellingTime());
		setColorTime(Solution.getColorTime());
		setMIP(Solution.getMIP());
		setColorTries(Solution.getColorTries());
		setNotes(Solution.getNotes() + " - Mod=0");
		setStatus(Solution.getStatus());
		setName(Solution.getName());
		setTotalTime(Solution.getTotalTime());

		int alpha = 0;
		int beta = 0;
		int scroll = 0;
		Boolean flag = true;
		for (int t = 0; t < tables.size() && flag; t++) {
			for (int i = 0; i < tables.get(t) - 1 && flag; i++) {
				alpha = (i % tables.get(t)) + scroll;
				beta = ((i % tables.get(t)) + 1) % tables.get(t) + scroll;

				if (colors.get(alpha) != colors.get(beta)) {
					flag = false;
					setCriticIndex(beta);
					setCriticTable(t);
					int mod = (getV() - 2) / 2;
					if (colors.get(alpha) == 1)
						setCriticDiff((labels[alpha] - labels[beta] + mod) % mod);
					else
						setCriticDiff((labels[beta] - labels[alpha] + mod) % mod);
					setNotes(Solution.getNotes() + " | 4t CDiff=" + getCriticDiff());
				}
			}
			scroll += tables.get(t);
		}
		tables.set(getCriticTable(), tables.get(getCriticTable()) + 1);
		tables.set(0, tables.get(0) + 1);
		ArrayList<Integer> Labels = new ArrayList<Integer>();
		for (int i = 0; i < labels.length; i++)
			Labels.add(labels[i]);
		Labels.add(getCriticIndex(), -1);
		Labels.add(0, -1);
		labels = new int[Labels.size()];
		for (int i = 0; i < Labels.size(); i++)
			labels[i] = Labels.get(i);

		// Add infinite-s
		colors.add(getCriticIndex(), 0);
		colors.add(0, 0);
		String OP = "OP(";
		for (int t = 0; t < tables.size(); t++) {
			if (t != (tables.size() - 1)) {
				OP += tables.get(t) + ",";
			} else
				OP += tables.get(t) + ")";

		}
		setOP_name(OP);
	}

	public boolean verify() {
		ArrayList<Integer> dR = new ArrayList<Integer>();
		ArrayList<Integer> dY = new ArrayList<Integer>();
		ArrayList<Integer> dYR = new ArrayList<Integer>();
		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		int mod = (getV() - 2) / 2;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t); i++) {
				alpha = (i % tables.get(t)) + scroll;
				beta = ((i % tables.get(t)) + 1) % tables.get(t) + scroll;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					dY.add((labels[alpha] - labels[beta] + mod) % mod);
					dY.add((labels[beta] - labels[alpha] + mod) % mod);
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 2) {
					dR.add((labels[alpha] - labels[beta] + mod) % mod);
					dR.add((labels[beta] - labels[alpha] + mod) % mod);
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					dYR.add((labels[alpha] - labels[beta] + mod) % mod);
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					dYR.add((labels[beta] - labels[alpha] + mod) % mod);
				}
			}
			scroll += tables.get(t);
		}
		Collections.sort(dY);
		Collections.sort(dR);
		Collections.sort(dYR);

		//System.out.println("dY");
		//for (int i = 0; i < dY.size(); i++)
		//	System.out.print(dY.get(i) + " ");
		//System.out.println("\ndR");
		//for (int i = 0; i < dR.size(); i++)
		//	System.out.print(dR.get(i) + " ");
		//System.out.println("\ndYR");
		//for (int i = 0; i < dYR.size(); i++)
		//	System.out.print(dYR.get(i) + " ");
		//System.out.println("");
		int nR = 0;
		int nY = 0;
		int nYR = 0;
		for (int i = 0; i < dY.size(); i++)
			if (dY.get(i) == (i + 1))
				nY++;
		for (int i = 0; i < dR.size(); i++)
			if (dR.get(i) == (i + 1))
				nR++;
		int index = 0;
		for (int i = 0; i < dYR.size()+1; i++) {
			if (i == getCriticDiff() && ((i+1) < dYR.size()))
				i++;

			if (dYR.get(index) == (i)) {
				nYR++;
				index++;
			}
		}
		if (nR == nY && nY == (mod - 1) && nYR == nY && !dYR.contains(getCriticDiff()))
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

	public ArrayList<Integer> getColors() {
		return colors;
	}

	public void setColors(ArrayList<Integer> colors) {
		this.colors = colors;
	}

	public String getName() {
		return name;
	}

	public String getSolution() {
		if (Status.equals("Solved")) {

			String ret = "";
			int scroll = 0;
			int infinity = 1;
			for (int t = 0; t < tables.size(); t++) {
				ret += "(";
				for (int i = 0; i < tables.get(t); i++) {
					if (labels[i + scroll] == -1) {
						ret += "\\infty_" + infinity;
						infinity++;
					} else
						ret += "(" + labels[i + scroll] + "," + colors.get(i + scroll) + ")";
					if (i != tables.get(t) - 1)
						ret += ", ";
				}
				ret += ") ";
				scroll += tables.get(t);
			}
			return ret;
		} else {
			return "";
		}
	}

	public String getColorsString() {
		if (colors.size() == (V - 1)) {

			String ret = "";
			int scroll = 0;
			int infinity = 1;
			for (int t = 0; t < tables.size(); t++) {
				ret += "(";
				for (int i = 0; i < tables.get(t); i++) {
					if (colors.get(i + scroll) == 0) {
						ret += "\\infty_" + infinity + ", ";
						infinity++;
					} else
						ret += "(" + colors.get(i + scroll) + ")";
					if (i != tables.get(t) - 1)
						ret += ", ";
				}
				ret += ") ";
				if (t != tables.size() - 1)
					scroll += tables.get(t);
			}
			return ret;
		} else {
			return "";
		}
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

	public double getColorTime() {
		return colorTime;
	}

	public void setColorTime(double colorTime) {
		this.colorTime = colorTime;
	}

	public Boolean getMIP() {
		return MIP;
	}

	public void setMIP(Boolean mIP) {
		MIP = mIP;
	}

	public int getColorTries() {
		return ColorTries;
	}

	public void setColorTries(int colorTries) {
		ColorTries = colorTries;
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

	public Boolean getPolyColor() {
		return PolyColor;
	}

	public void setPolyColor(Boolean polyColor) {
		PolyColor = polyColor;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getCriticIndex() {
		return criticIndex;
	}

	public void setCriticIndex(int criticIndex) {
		this.criticIndex = criticIndex;
	}

	public int getCriticTable() {
		return criticTable;
	}

	public void setCriticTable(int criticTable) {
		this.criticTable = criticTable;
	}

	public int getCriticDiff() {
		return criticDiff;
	}

	public void setCriticDiff(int criticDiff) {
		this.criticDiff = criticDiff;
	}

}
