import java.util.ArrayList;
import java.util.Collections;

public class TwoRRotational_Solution {
	private static ArrayList<Integer> tables = new ArrayList<Integer>();
	private int V;
	private String name;
	private String OP_name;
	private String Status = "Solved";
	private static ArrayList<ArrayList<Integer>> criticalPaths = new ArrayList<ArrayList<Integer>>();
	private double labellingTime = -1.0;
	private Boolean MIP = false;
	private Boolean PolyColor = false;
	private int ColorTries = 0;
	private String notes = "";
	private double colorTime = -1.0;
	private double totalTime = -1.0;
	private ArrayList<Integer> colors = new ArrayList<Integer>();
	private int[] labels = null;

	public TwoRRotational_Solution(ArrayList<Integer> tables, int v) {
		super();
		this.setTables(tables);
		setV(v);
		setLabels(new int[V - 1]);
	}

	public boolean verify() {
		ArrayList<Integer> dR = new ArrayList<Integer>();
		ArrayList<Integer> dY = new ArrayList<Integer>();
		ArrayList<Integer> dYR = new ArrayList<Integer>();
		int scroll = 0;
		int alpha = 0;
		int beta = 0;
		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					dY.add((labels[alpha] - labels[beta] + V / 2) % (V / 2));
					dY.add((labels[beta] - labels[alpha] + V / 2) % (V / 2));
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 2) {
					dR.add((labels[alpha] - labels[beta] + V / 2) % (V / 2));
					dR.add((labels[beta] - labels[alpha] + V / 2) % (V / 2));
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					dYR.add((labels[alpha] - labels[beta] + V / 2) % (V / 2));
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					dYR.add((labels[beta] - labels[alpha] + V / 2) % (V / 2));
				}
			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					dY.add((labels[alpha] - labels[beta] + V / 2) % (V / 2));
					dY.add((labels[beta] - labels[alpha] + V / 2) % (V / 2));
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 2) {
					dR.add((labels[alpha] - labels[beta] + V / 2) % (V / 2));
					dR.add((labels[beta] - labels[alpha] + V / 2) % (V / 2));
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					dYR.add((labels[alpha] - labels[beta] + V / 2) % (V / 2));
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					dYR.add((labels[beta] - labels[alpha] + V / 2) % (V / 2));
				}
			}
			scroll += tables.get(t);
		}
		Collections.sort(dY);
		Collections.sort(dR);
		Collections.sort(dYR);

		// System.out.println("dY");
		// for (int i=0;i<dY.size();i++)
		// System.out.print(dY.get(i)+" ");
		// System.out.println("\ndR");
		// for (int i=0;i<dR.size();i++)
		// System.out.print(dR.get(i)+" ");
		// System.out.println("\ndYR");
		// for (int i=0;i<dYR.size();i++)
		// System.out.print(dYR.get(i)+" ");
		// System.out.println("");
		int nR = 0;
		int nY = 0;
		int nYR = 0;
		for (int i = 0; i < dY.size(); i++)
			if (dY.get(i) == (i + 1))
				nY++;
		for (int i = 0; i < dR.size(); i++)
			if (dR.get(i) == (i + 1))
				nR++;
		for (int i = 0; i < dYR.size(); i++)
			if (dYR.get(i) == (i))
				nYR++;
		if (nR == nY && nY == (V / 2 - 1) && nYR == (V / 2))
			return true;
		else
			return false;
	}

	public boolean verifyColors() {
		int dR = 0;
		int dY = 0;
		int dYR = 0;
		int scroll = 0;
		int alpha = 0;
		int beta = 0;

		@SuppressWarnings("unused")
		int B = 0, R = 0;

		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t); i++) {
				if (colors.get(scroll + i) == 1)
					B++;
				if (colors.get(scroll + i) == 2)
					R++;
			}
			scroll += tables.get(t);
		}

		scroll = 0;

		for (int t = 0; t < tables.size(); t++) {
			for (int i = 0; i < tables.get(t) - 1; i++) {
				alpha = scroll + i;
				beta = scroll + i + 1;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					dY++;
					dY++;
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 2) {
					dR++;
					dR++;
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					dYR++;
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					dYR++;
				}
			}
			if (t != 0) {
				alpha = scroll + tables.get(t) - 1;
				beta = scroll;
				if (colors.get(alpha) == 1 && colors.get(beta) == 1) {
					dY++;
					dY++;
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 2) {
					dR++;
					dR++;
				} else if (colors.get(alpha) == 1 && colors.get(beta) == 2) {
					dYR++;
				} else if (colors.get(alpha) == 2 && colors.get(beta) == 1) {
					dYR++;
				}
			}
			scroll += tables.get(t);
		}
		// System.out.println("V: " + V + " - |dY|: " + dY + " - |dR|: " + dR + " -
		// |dYR|:" + dYR);
		// System.out.println("Y: " + B + " - R: " + R);
		if ((dY == dR) && (dYR == ((V - 1) / 2)) && (dYR == (dY + 1)))
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
		if (Status.equals("Solved") && colors.size() == (V - 1) && labels.length == (V - 1)) {

			String ret = "";
			int scroll = 0;
			for (int t = 0; t < tables.size(); t++) {
				ret += "(";
				for (int i = 0; i < tables.get(t); i++) {
					if (i == 0 && t == 0)
						ret += "\\infty, ";

					ret += "(" + labels[i + scroll] + "," + colors.get(i + scroll) + ")";
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
	

	public String getColorsString() {
		if (colors.size() == (V - 1)) {

			String ret = "";
			int scroll = 0;
			for (int t = 0; t < tables.size(); t++) {
				ret += "(";
				for (int i = 0; i < tables.get(t); i++) {
					if (i == 0 && t == 0)
						ret += "\\infty, ";

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

	public ArrayList<ArrayList<Integer>> getCriticalPaths() {
		return criticalPaths;
	}

	public void setCriticalPaths(ArrayList<ArrayList<Integer>> criticalPaths) {
		TwoRRotational_Solution.criticalPaths = criticalPaths;
	}


}
