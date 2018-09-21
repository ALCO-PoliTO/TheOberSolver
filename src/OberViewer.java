import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.chocosolver.solver.exception.ContradictionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import swing2swt.layout.BoxLayout;
import org.eclipse.swt.widgets.List;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import ilog.concert.IloException;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.RowLayout;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Canvas;

public class OberViewer {

	protected Shell shlTheOberviewer;
	private Text output;
	private Text input;
	private Group controls;
	private Composite InputOutput;
	private Combo solver;
	private Button SymmetrySwitch;
	private Composite MainCnt;
	private Composite SymmetryValueCnt;
	private Label lblSymmetryValue;
	private Text SymmetryValue;
	private Composite TimeLimitCnt;
	private Label lblTimeLimit;
	private Button TimeLimit;
	private Composite SolLimitCnt;
	private Label lblNumberOfSols;
	private Text SolutionNumber;
	private Group GenericControls;
	private Composite ColoringCnt;
	private Label lblColoringAlgorithm;
	private Combo Coloring;
	private TabFolder tabFolder;
	private TabItem tbtmMain;
	private Composite composite;
	private TabItem tbtmAbout;

	
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
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			OberViewer window = new OberViewer();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents(display);
		shlTheOberviewer.open();
		shlTheOberviewer.layout();
		while (!shlTheOberviewer.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents(Display display) {
		shlTheOberviewer = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN );
		shlTheOberviewer.setMinimumSize(new Point(540, 450));
		shlTheOberviewer.setSize(567, 450);
		shlTheOberviewer.setText("The Oberviewer");
		shlTheOberviewer.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		tabFolder = new TabFolder(shlTheOberviewer, SWT.NONE);
		
		tbtmMain = new TabItem(tabFolder, SWT.NONE);
		tbtmMain.setText("Main");
		
				MainCnt = new Composite(tabFolder, SWT.NONE);
				tbtmMain.setControl(MainCnt);
				RowLayout rl_MainCnt = new RowLayout(SWT.HORIZONTAL);
				rl_MainCnt.spacing = 5;
				rl_MainCnt.marginTop = 5;
				rl_MainCnt.marginLeft = 5;
				rl_MainCnt.marginRight = 5;
				rl_MainCnt.marginBottom = 5;
				rl_MainCnt.marginHeight = 5;
				rl_MainCnt.pack = false;
				MainCnt.setLayout(rl_MainCnt);
				
						controls = new Group(MainCnt, SWT.NONE);
						RowLayout rl_controls = new RowLayout(SWT.VERTICAL);
						rl_controls.pack = false;
						rl_controls.fill = true;
						rl_controls.center = true;
						rl_controls.marginLeft = 5;
						rl_controls.marginRight = 5;
						rl_controls.marginTop = 5;
						rl_controls.spacing = 5;
						controls.setLayout(rl_controls);
						
								GenericControls = new Group(controls, SWT.NONE);
								GenericControls.setText("Generic options");
								RowLayout rl_GenericControls = new RowLayout(SWT.VERTICAL);
								rl_GenericControls.center = true;
								rl_GenericControls.fill = true;
								GenericControls.setLayout(rl_GenericControls);
								
										Composite SolverCnt = new Composite(GenericControls, SWT.NONE);
										RowLayout rl_SolverCnt = new RowLayout(SWT.HORIZONTAL);
										rl_SolverCnt.center = true;
										SolverCnt.setLayout(rl_SolverCnt);
										
												Label lblSolver = new Label(SolverCnt, SWT.NONE);
												lblSolver.setText("Solver");
												
														solver = new Combo(SolverCnt, SWT.NONE);
														solver.setVisibleItemCount(4);
														solver.setToolTipText("IBM Ilog CPLEX");
														solver.setItems(new String[] { "IBM Ilog CPLEX", "Choco Solver" });
														solver.setText("IBM Ilog CPLEX");
														
																SolLimitCnt = new Composite(GenericControls, SWT.NONE);
																RowLayout rl_SolLimitCnt = new RowLayout(SWT.HORIZONTAL);
																rl_SolLimitCnt.center = true;
																SolLimitCnt.setLayout(rl_SolLimitCnt);
																
																		lblNumberOfSols = new Label(SolLimitCnt, SWT.NONE);
																		lblNumberOfSols.setText("Number of Sols.");
																		
																				SolutionNumber = new Text(SolLimitCnt, SWT.BORDER);
																				SolutionNumber.setText("1");
																				
																						TimeLimitCnt = new Composite(GenericControls, SWT.NONE);
																						RowLayout rl_TimeLimitCnt = new RowLayout(SWT.HORIZONTAL);
																						rl_TimeLimitCnt.center = true;
																						TimeLimitCnt.setLayout(rl_TimeLimitCnt);
																						
																								lblTimeLimit = new Label(TimeLimitCnt, SWT.NONE);
																								lblTimeLimit.setText("Time Limit");
																								
																										TimeLimit = new Button(TimeLimitCnt, SWT.CHECK);
																										
																												Group TwoRotControls = new Group(controls, SWT.NONE);
																												TwoRotControls.setText("4t+3 Controls");
																												RowLayout rl_TwoRotControls = new RowLayout(SWT.VERTICAL);
																												rl_TwoRotControls.fill = true;
																												rl_TwoRotControls.center = true;
																												TwoRotControls.setLayout(rl_TwoRotControls);
																												
																														Composite SymmetryCnt = new Composite(TwoRotControls, SWT.NONE);
																														RowLayout rl_SymmetryCnt = new RowLayout(SWT.HORIZONTAL);
																														rl_SymmetryCnt.center = true;
																														SymmetryCnt.setLayout(rl_SymmetryCnt);
																														
																																Label lblSymmetry = new Label(SymmetryCnt, SWT.NONE);
																																lblSymmetry.setText("Symmetry");
																																
																																		SymmetrySwitch = new Button(SymmetryCnt, SWT.CHECK);
																																		
																																				SymmetryValueCnt = new Composite(TwoRotControls, SWT.NONE);
																																				RowLayout rl_SymmetryValueCnt = new RowLayout(SWT.HORIZONTAL);
																																				rl_SymmetryValueCnt.center = true;
																																				SymmetryValueCnt.setLayout(rl_SymmetryValueCnt);
																																				
																																						lblSymmetryValue = new Label(SymmetryValueCnt, SWT.NONE);
																																						lblSymmetryValue.setText("Symmetry Value");
																																						
																																								SymmetryValue = new Text(SymmetryValueCnt, SWT.BORDER);
																																								
																																										ColoringCnt = new Composite(TwoRotControls, SWT.NONE);
																																										RowLayout rl_ColoringCnt = new RowLayout(SWT.HORIZONTAL);
																																										rl_ColoringCnt.center = true;
																																										ColoringCnt.setLayout(rl_ColoringCnt);
																																										
																																												lblColoringAlgorithm = new Label(ColoringCnt, SWT.NONE);
																																												lblColoringAlgorithm.setText("Coloring Algorithm");
																																												
																																														Coloring = new Combo(ColoringCnt, SWT.NONE);
																																														Coloring.setVisibleItemCount(4);
																																														Coloring.setToolTipText("IBM Ilog CPLEX");
																																														Coloring.setItems(new String[] { "Auto", "Polynomial", "CP" });
																																														Coloring.setText("Auto");
																																														
																																																Button SolveBtn = new Button(controls, SWT.NONE);
																																																SolveBtn.addSelectionListener(new SelectionAdapter() {
																																																	@Override
																																																	public void widgetSelected(SelectionEvent e) {
																																																		String inputConfig = input.getText();
																																																		Boolean timeLim = TimeLimit.getSelection();
																																																		DecimalFormat df = new DecimalFormat("0.0000");
																																																		Integer SolNum = 1;
																																																		if (SolutionNumber.getText().matches("[0-9/]+"))
																																																			SolNum = Integer.parseInt(SolutionNumber.getText());

																																																		if (inputConfig.matches("[0-9, /,]+")) {
																																																			output.setText("Processing...");
																																																			String[] tableItems = inputConfig.split(",");
																																																			Boolean Choco = false;
																																																			if (solver.getText().equals("Choco Solver"))
																																																				Choco = true;
																																																			int[] tableInt = Arrays.stream(tableItems).mapToInt(Integer::parseInt).toArray();
																																																			ArrayList<Integer> Tables = new ArrayList<Integer>();
																																																			int V = 0;
																																																			for (int i : tableInt) {
																																																				Tables.add(i);
																																																				V += i;
																																																			}
																																																			if ((V % 4 == 3) || (V % 4 == 0)) {
																																																				// 4T+3 or 4T
																																																				try {
																																																					TwoRotational instance = new TwoRotational(false, SolNum, false, "", timeLim, Choco);
																																																					output.setText("");
																																																					if (SymmetrySwitch.getSelection()) {
																																																						instance.param_setSymmetry(true);
																																																						if (SymmetryValue.getText().matches("[0-9, /]+")
																																																								&& Integer.parseInt(SymmetryValue.getText()) >= 0
																																																								&& Integer.parseInt(SymmetryValue.getText()) <= ((V - 1) / 2) - 1) {
																																																							output.append("Symmetry value set to " + SymmetryValue.getText() + " \n");
																																																							instance.param_setSymmetryValue(Integer.parseInt(SymmetryValue.getText()));
																																																						} else
																																																							output.append("Symmetry value ignored: should be between 0 and  "
																																																									+ (((V - 1) / 2) - 1) + "\n");
																																																					}
																																																					Boolean Mod0 = false;
																																																					if (V % 4 == 0) {
																																																						Mod0 = true;
																																																					}
																																																					Boolean flag = false;
																																																					if (Mod0) {
																																																						for (int u = 0; u < Tables.size() && !flag; u++) {
																																																							if (Tables.get(u) > 3 || (u == 0 && Tables.get(u) > 3)) {
																																																								Tables.set(u, (Tables.get(u) - 1));
																																																								flag = true;
																																																							}
																																																						}
																																																					}
																																																					ArrayList<TwoRotational_Solution> Solution = null;
																																																					switch (Coloring.getText()) {
																																																					case "Polynomial": {
																																																						output.append("Using only Polynomial coloring.\n");
																																																						Solution = instance.solve_onlyPoly(Tables);
																																																					}
																																																						break;
																																																					case "CP": {
																																																						output.append("Using only CP coloring.\n");
																																																						Solution = instance.solve_onlyCP(Tables);
																																																					}
																																																						break;
																																																					default: {
																																																						Solution = instance.solve(Tables);
																																																					}
																																																					}

																																																					if (Solution.size() > 0) {
																																																						output.append("Using 4t+3 2Rotational rules.\n\n");
																																																						for (int j = 0; j < Solution.size(); j++) {
																																																							if (!Mod0) {
																																																								output.append("Solution for " + Solution.get(j).getOP_name() + "\n");
																																																								output.append("Status: " + Solution.get(j).getStatus() + "\n");
																																																								output.append("ColorTime: " + df.format(Solution.get(j).getColorTime())
																																																										+ " - LabellingTime: " + df.format(Solution.get(j).getLabellingTime())
																																																										+ "\n");
																																																								output.append("ColorintTries: " + Solution.get(j).getColorTries()
																																																										+ " - UsingMIP: " + Solution.get(j).getMIP() + "\n");
																																																								output.append("\nSolution:\n " + Solution.get(j).getSolution() + "\n-----\n\n");
																																																							} else {
																																																								if (flag) {
																																																									output.append("Converting 4t+3 to 4t...\n");
																																																									TwoRotational_Solution_M0 Solutions_Mod0 = new TwoRotational_Solution_M0(
																																																											Solution.get(j));
																																																									output.append("Solution for " + Solutions_Mod0.getOP_name() + "\n");
																																																									output.append("Status: " + Solutions_Mod0.getStatus() + "\n");

																																																									output.append(
																																																											"Critical Difference: " + Solutions_Mod0.getCriticDiff() + "\n");
																																																									output.append("Critical Table: " + Solutions_Mod0.getCriticTable() + "\n");
																																																									output.append("ColorTime: " + df.format(Solutions_Mod0.getColorTime())
																																																											+ " - LabellingTime: "
																																																											+ df.format(Solutions_Mod0.getLabellingTime()) + "\n");
																																																									output.append("ColorintTries: " + Solutions_Mod0.getColorTries()
																																																											+ " - UsingMIP: " + Solutions_Mod0.getMIP() + "\n");
																																																									output.append(
																																																											"\nSolution:\n " + Solutions_Mod0.getSolution() + "\n-----\n\n");
																																																								}
																																																							}
																																																						}
																																																					} else
																																																						output.setText("No solution found...");
																																																				} catch (ErrorThrower | IloException | ContradictionException e1) {
																																																					// TODO Auto-generated catch block
																																																					e1.printStackTrace();
																																																				}
																																																			}

																																																			if ((V % 4 == 1) || (V % 4 == 2)) {
																																																				OneRotational instance = new OneRotational(false, SolNum, false, "", timeLim, Choco);
																																																				ArrayList<OneRotational_Solution> Solutions = new ArrayList<OneRotational_Solution>();
																																																				ArrayList<Integer> tcopy = null;
																																																				output.setText("");
																																																				Boolean Mod2 = false;
																																																				if (V % 4 == 2) {
																																																					Mod2 = true;
																																																				}
																																																				if (Mod2) {
																																																					Boolean flag = true;

																																																					for (int u = 0; u < Tables.size() && flag; u++) {
																																																						tcopy = new ArrayList<Integer>(Tables);
																																																						tcopy.set(u, tcopy.get(u) - 1);
																																																						if (instance.validConfiguration(tcopy))
																																																							flag = false;
																																																					}
																																																					if (flag == false) {
																																																						try {
																																																							Solutions = instance.solve(tcopy);
																																																						} catch (ErrorThrower | IloException | ContradictionException e1) {
																																																							// TODO Auto-generated catch block
																																																							e1.printStackTrace();
																																																						}
																																																						if (Solutions.size() > 0) {
																																																							output.append("Using 4t+1 1Rotational rules.\n");
																																																							output.append("Converting 4t+1 to 4t+2...\n\n");
																																																							for (int j = 0; j < Solutions.size(); j++) {
																																																								if (Solutions.get(j).getStatus().equals("Solved")) {
																																																									OneRotational_SolutionM2 Solutions_Mod2 = new OneRotational_SolutionM2(
																																																											Solutions.get(j));

																																																									output.append("Solution for " + Solutions_Mod2.getOP_name() + "\n");
																																																									output.append("Derived from " + getOP_name(tcopy) + "\n");
																																																									output.append("Minimal Problem: " + Solutions_Mod2.getOP_nameRed() + "\n");
																																																									output.append("Status: " + Solutions_Mod2.getStatus() + "\n");
																																																									output.append("LabellingTime: "
																																																											+ df.format(Solutions_Mod2.getLabellingTime()) + "\n");
																																																									output.append(
																																																											"\nSolution:\n " + Solutions_Mod2.getSolution() + "\n-----\n\n");

																																																								} else {
																																																									output.setText("Infeasible. No solution found for this 4t+1");
																																																								}
																																																							}
																																																						} else {
																																																							output.setText("Infeasible. No solution found");
																																																						}
																																																					} else {
																																																						output.setText("Table config is not valid.");
																																																					}

																																																				} else {
																																																					if (instance.validConfiguration(Tables)) {
																																																						try {
																																																							Solutions = instance.solve(Tables);
																																																						} catch (ErrorThrower | IloException | ContradictionException e1) {
																																																							// TODO Auto-generated catch block
																																																							e1.printStackTrace();
																																																						}
																																																						if (Solutions.size() > 0) {
																																																							output.append("Using 4t+1 1Rotational rules.\n");
																																																							for (int j = 0; j < Solutions.size(); j++) {
																																																								output.append("Solution for " + Solutions.get(j).getOP_name() + "\n");
																																																								output.append("Minimal Problem: " + Solutions.get(j).getOP_nameRed() + "\n");
																																																								output.append("Status: " + Solutions.get(j).getStatus() + "\n");
																																																								output.append("LabellingTime: " + df.format(Solutions.get(j).getLabellingTime())
																																																										+ "\n");
																																																								output.append(
																																																										"\nSolution:\n " + Solutions.get(j).getSolution() + "\n-----\n\n");

																																																							}
																																																						} else {
																																																							output.setText("Infeasible. No solution found");
																																																						}
																																																					} else {
																																																						output.setText("Table config is not valid.");
																																																					}

																																																				}

																																																			}

																																																		} else {
																																																			output.setText("Invalid table configuration");
																																																		}

																																																	}
																																																});
																																																SolveBtn.setText("Solve");
																																																
																																																		InputOutput = new Composite(MainCnt, SWT.NONE);
																																																		InputOutput.setLayout(new FillLayout(SWT.VERTICAL));
																																																		
																																																				input = new Text(InputOutput, SWT.BORDER | SWT.V_SCROLL | SWT.CENTER | SWT.MULTI);
																																																				input.setToolTipText("Insert table configuration");
																																																				input.setText("5,5,5,5,3");
																																																				
																																																						output = new Text(InputOutput, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.CENTER | SWT.MULTI);
																																																						output.setEditable(false);
																																																						output.setText("Output");
																																																						
																																																						tbtmAbout = new TabItem(tabFolder, SWT.NONE);
																																																						tbtmAbout.setText("About");
																																																						
																																																						composite = new Composite(tabFolder, SWT.NONE);
																																																						tbtmAbout.setControl(composite);
																																																						RowLayout rl_composite = new RowLayout(SWT.HORIZONTAL);
																																																						composite.setLayout(rl_composite);
																																																						
																																																						Label lblNewLabel = new Label(composite, SWT.WRAP);
																																																						lblNewLabel.setText("TheOberSolver\n\nThis project is developed and maintained by ALCO@Politecnico di Torino. TheObersolver is a pre-release software for solving the Oberwolfach Problem. Check out our page on gitHub @ ALCO-PoliTO.\n\nThis software uses Choco Solver and can be run as well with IBM CPLEX. As for the former one, scholars are entitled to obtain a free license via the IBM Academic Initiative.\n");
																																																						
																																																						Label lblNewLabel_1 = new Label(composite, SWT.NONE);

	}
}
