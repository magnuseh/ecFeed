package ObsoleteNWise;

/*******************************************************************************
 * Copyright (c) 2013 Testify AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html                                     
 *                                                                               
 * Contributors:                                                                 
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.testify.ecfeed.generators.algorithms.Tuples;
import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.ui.common.TreeCheckStateListener;

public class ObsoletePrimitiveCoverageSetupDialog extends TitleAreaDialog {
	private Button fOkButton;
	private MethodNode fMethod;
	private CheckboxTreeViewer fTestCasesViewer;

	private Composite fMainContainer;

	private int fContent;
	private int N;
	private double[] fResults;
	private int[] fTotalWork;
	private Canvas fCanvas;

	private final String fTitle;
	private final String fMessage;
	private IContentProvider fContentProvider;
	private IBaseLabelProvider fLabelProvider;
	private List<List<PartitionNode>> fInput;

	public final static int TEST_CASE_COMPOSITE = 1;
	public final static int COVERAGE_GRAPH_COMPOSITE = 1 << 1;

	public ObsoletePrimitiveCoverageSetupDialog(Shell parentShell, MethodNode method, int content, String title, String message,
			IContentProvider contentProvider, IBaseLabelProvider labelProvider) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE);
		fContent = content;
		fTitle = title;
		fMessage = message;
		fContentProvider = contentProvider;
		fLabelProvider = labelProvider;

		initialize(method);
	}

	protected void initialize(MethodNode method) {
		fMethod = method;
		N = method.getCategories().size();
		fResults = new double[N];
		fTotalWork = new int[N];
		fInput = getInput();

		for (int n = 0; n < fTotalWork.length; n++) {
			fTotalWork[n] = calculateTotalTuples(n + 1);
		}

	}

	@Override
	public Point getInitialSize() {
		return new Point(600, 800);
	}

	@Override
	public void okPressed() {

		super.okPressed();
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
		fOkButton.setEnabled(true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(fTitle);
		setMessage(fMessage);
		Composite area = (Composite) super.createDialogArea(parent);
		fMainContainer = new Composite(area, SWT.NONE);
		fMainContainer.setLayout(new GridLayout(1, false));
		fMainContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		if ((fContent & TEST_CASE_COMPOSITE) > 0) {
			createTestCaseComposite(fMainContainer);
		}

		if ((fContent & COVERAGE_GRAPH_COMPOSITE) > 0) {
			createCoverageGraphComposite(fMainContainer);
		}
		return area;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~TEST
	// AREA~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void createTestCaseComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label selectTestCasesLabel = new Label(composite, SWT.WRAP);
		selectTestCasesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		selectTestCasesLabel.setText(Messages.DIALOG_CALCULATE_COVERAGE_MESSAGE);

		createTestCaseViewer(composite);
		createUtilityButtons(composite);
	}

	private void createTestCaseViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.CHECK | SWT.BORDER);
		tree.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		fTestCasesViewer = new CheckboxTreeViewer(tree);
		fTestCasesViewer.setContentProvider(fContentProvider);
		fTestCasesViewer.setLabelProvider(fLabelProvider);
		fTestCasesViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTestCasesViewer.setInput(fMethod);
		for (CategoryNode category : fMethod.getCategories()) {
			fTestCasesViewer.setSubtreeChecked(category, true);
		}
		fTestCasesViewer.addCheckStateListener(new TreeCheckStateListener(fTestCasesViewer));

	}

	private void createCoverageGraphComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER | SWT.FILL);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createCoverageGraphViewer(composite);
	}

	private void createCoverageGraphViewer(Composite parent) {
		fCanvas = new Canvas(parent, SWT.FILL | SWT.SCROLL_PAGE);
		fCanvas.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		// fCanvas.setSize(fCanvas.getParent().getSize().x,
		// fCanvas.getParent().getSize().y);
		// fCanvas.setSize(fCanvas.getParent().getSize().x, N * 50);
		fCanvas.getParent().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {

				updateBarGraph();
			}
		});
	}

	private void updateBarGraph() {
		if (N != 0 && fResults.length > 0) {
			Display display = Display.getCurrent();
			fCanvas.setSize(fCanvas.getParent().getSize().x, N * 50);

			GC gc = new GC(fCanvas);
			gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.fillRectangle(0, 0, fCanvas.getSize().x, fCanvas.getSize().y);

			int spacing = 10;
			int width = fCanvas.getSize().x - spacing * 3;
			int height = fCanvas.getSize().y - spacing;

			// Clear the canvas

			Color outworldTeal = new Color(display, 16, 208, 208);
			gc.setBackground(outworldTeal);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));

			double widthunit = (double) width / 100;
			double heightunit = (double) height / N;
			int fontsize = (int) (heightunit / 4);
			Font font = new Font(display, display.getSystemFont().getFontData()[0].getName(), fontsize, 0);
			gc.setFont(font);

			int linewidth = 2;
			int topborder;
			int topbarborder;
			int bottomborder = 10;
			int fontspacing = (fontsize * 8) / 5;

			for (int n = 0; n < N; n++) {
				topborder = bottomborder + spacing;
				topbarborder = topborder + fontspacing;
				bottomborder = topborder + (int) heightunit - spacing;

				gc.fillRectangle(0, topbarborder, (int) (fResults[n] * widthunit), bottomborder - topbarborder);
				gc.setLineWidth(linewidth);
				gc.drawLine(linewidth, bottomborder, (int) width - linewidth, bottomborder);
				gc.drawLine(linewidth / 2, topbarborder, linewidth / 2, bottomborder);
				gc.drawLine((int) (width) - linewidth / 2, topbarborder, (int) (width) - linewidth / 2, bottomborder);

				DecimalFormat df = new DecimalFormat("#.00");
				String nlabel = "For N= " + (n + 1) + ":  ";
				String percentvalue = df.format(fResults[n]) + "%";
				gc.drawString(nlabel, 10, topborder, true);
				gc.drawString(percentvalue, (width / 2) - fontspacing, (int) (topbarborder), true);
			}
			font.dispose();
			outworldTeal.dispose();
			gc.dispose();
		}
	}

	// private Image createBarGraph(Display display, int width, int height) {
	// try {
	// Image img = new Image(display, width, height);
	// img.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	//
	// if (N > 0) {
	// GC gc = new GC(img);
	// Color outworldTeal = new Color(display, 16, 208, 208);
	// gc.setBackground(outworldTeal);// display.getSystemColor(SWT.COLOR_RED));
	// gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
	//
	// double widthunit = (double) width / 100;
	// double heightunit = (double) height / N;
	//
	// int linewidth = 2;
	//
	// for (int n = 0; n < N; n++) {
	// gc.fillRectangle(0, (int) ((n + 0.25) * heightunit), (int) (fResults[n] *
	// widthunit), (int) (0.25 * heightunit));
	// gc.setLineWidth(linewidth);
	// gc.drawLine(linewidth, (int) ((n + 0.5) * heightunit), (int) (fResults[n]
	// * widthunit), (int) ((n + 0.5) * heightunit));
	// gc.drawLine(linewidth / 2, (int) ((n + 0.25) * heightunit), linewidth /
	// 2, (int) ((n + 0.5) * heightunit));
	// gc.drawLine((int) (fResults[n] * widthunit) - linewidth / 2, (int) ((n +
	// 0.25) * heightunit),
	// (int) (fResults[n] * widthunit) - linewidth / 2, (int) ((n + 0.5) *
	// heightunit));
	// }
	// gc.dispose();
	// outworldTeal.dispose();
	// }
	// return img;
	// } catch (java.lang.IllegalArgumentException e) {
	// System.err.println("Tried to initialize image with size of: " + width +
	// ", " + height);
	// }
	// return null;
	// }

	private void createUtilityButtons(Composite parent) {
		Composite buttonsComposite = new Composite(parent, SWT.NONE);
		buttonsComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		Button refreshButton = new Button(buttonsComposite, SWT.NONE);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateCoverage();
			}
		});
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~TEST
	// AREA~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected List<List<PartitionNode>> getInput() {
		List<List<PartitionNode>> input = new ArrayList<List<PartitionNode>>();
		for (CategoryNode cnode : fMethod.getCategories()) {
			List<PartitionNode> category = new ArrayList<PartitionNode>();
			for (PartitionNode pnode : cnode.getLeafPartitions()) {
				category.add(pnode);
			}
			input.add(category);
		}

		return input;
	}

	protected List<List<PartitionNode>> getSelectedTestCases() {
		List<List<PartitionNode>> cases = new ArrayList<List<PartitionNode>>();
		TestCaseNode tcnode = new TestCaseNode("n", null);

		for (Object obj : fTestCasesViewer.getCheckedElements()) {
			if (obj.getClass().isInstance(tcnode)) {
				tcnode = (TestCaseNode) obj;
				List<PartitionNode> partitions = new ArrayList<>();
				for (PartitionNode pnode : tcnode.getTestData()) {
					partitions.add(pnode);
				}
				cases.add(partitions);
			}
		}
		return cases;
	}

	protected Set<List<PartitionNode>> getTuples(List<PartitionNode> vector) {
		return (new Tuples<PartitionNode>(vector, 2)).getAll();
	}

	private int calculateTotalTuples(int N) {
		int totalWork = 0;

		Tuples<List<PartitionNode>> tuples = new Tuples<List<PartitionNode>>(fInput, N);
		while (tuples.hasNext()) {
			long combinations = 1;
			List<List<PartitionNode>> tuple = tuples.next();
			for (List<PartitionNode> category : tuple) {
				combinations *= category.size();
			}
			totalWork += combinations;
		}

		return totalWork;
	}

	protected int calculateCoveredTuples(int n) {
		HashSet<List<PartitionNode>> covered = new HashSet<List<PartitionNode>>();

		List<List<PartitionNode>> tcases = getSelectedTestCases();
		for (List<PartitionNode> tcase : tcases) {
			Tuples<PartitionNode> tuples = new Tuples<PartitionNode>(tcase, n);

			covered.addAll(tuples.getAll());
		}

		return covered.size();
	}

	private void updateCoverage() {
		for (int n = 1; n <= N; n++) {
			int covered = calculateCoveredTuples(n);
			if (fTotalWork[n - 1] == 0)
				fResults[n - 1] = 0;
			else
				fResults[n - 1] = ((double) covered / (double) fTotalWork[n - 1]) * 100;
			updateBarGraph();
			System.out.println("N: " + n + " => " + fResults[n - 1]);
		}
	}
}
