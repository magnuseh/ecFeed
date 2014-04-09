package com.testify.ecfeed.ui.dialogs;

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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.ui.editor.CoverageCalculator;

public class CoverageSetupDialog extends TitleAreaDialog implements ChangeListener {
	private Button fOkButton;
	private CheckboxTreeViewer fTestCasesViewer;
	private CoverageCalculator fCalculator;

	private Composite fMainContainer;

	private int fContent;
	private int N;
	private double[] fResults;
	private Canvas[] fCanvasSet;

	private final String fTitle;
	private final String fMessage;
	private IContentProvider fContentProvider;
	private IBaseLabelProvider fLabelProvider;

	public final static int TEST_CASE_COMPOSITE = 1;
	public final static int COVERAGE_GRAPH_COMPOSITE = 1 << 1;

	public CoverageSetupDialog(Shell parentShell, CoverageCalculator calculator, int content, String title, String message,
			IContentProvider contentProvider, IBaseLabelProvider labelProvider) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE);
		fContent = content;
		fTitle = title;
		fMessage = message;
		fContentProvider = contentProvider;
		fLabelProvider = labelProvider;

		fCalculator = calculator;
		initialize(fCalculator);
	}

	protected void initialize(CoverageCalculator calculator) {
		N = calculator.getN();
		fResults = new double[N];
		fCalculator.addChangeListener(this);
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
		fMainContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if ((fContent & TEST_CASE_COMPOSITE) > 0) {
			createTestCaseComposite(fMainContainer);
		}

		if ((fContent & COVERAGE_GRAPH_COMPOSITE) > 0) {
			createCoverageGraphComposite(fMainContainer);
		}
		return area;
	}

	private void createTestCaseComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData griddata = new GridData(SWT.FILL, SWT.FILL, true, false);
		griddata.minimumHeight = 250;
		griddata.grabExcessVerticalSpace = true;
		composite.setLayoutData(griddata);

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
		fTestCasesViewer.setInput(fCalculator.getMethod());

		fTestCasesViewer.addCheckStateListener(fCalculator.generateTreeViewerListener(this.fTestCasesViewer));
	}

	private void createCoverageGraphComposite(Composite parent) {
		ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.BORDER | SWT.FILL | SWT.V_SCROLL);
		GridData scrolledgriddata = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledgriddata.minimumWidth = 100;
		scrolledgriddata.minimumHeight = 100;
		scrolled.setLayout(new GridLayout(1, false));
		scrolled.setLayoutData(scrolledgriddata);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);

		Composite composite = new Composite(scrolled, SWT.BORDER | SWT.FILL);
		composite.setLayout(new GridLayout(1, false));
		GridData griddata = new GridData(SWT.FILL, SWT.FILL, true, true);
		griddata.minimumHeight = 100;
		griddata.grabExcessVerticalSpace = true;
		composite.setLayoutData(griddata);
		createCoverageGraphViewer(composite);

		scrolled.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolled.setContent(composite);
		final ScrollBar vBar = scrolled.getVerticalBar();
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				drawBarGraph();
			}
		};
		vBar.addSelectionListener(listener);
	}

	private void createCoverageGraphViewer(Composite parent) {
		fCanvasSet = new Canvas[N];
		for (int n = 0; n < N; n++) {
			fCanvasSet[n] = new Canvas(parent, SWT.FILL);
			fCanvasSet[n].setSize(getInitialSize().x, 40);
			GridData griddata = new GridData(SWT.FILL, SWT.FILL, true, false);
			griddata.minimumHeight = 40;
			griddata.grabExcessVerticalSpace = true;
			fCanvasSet[n].setLayoutData(griddata);
		}

		parent.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				drawBarGraph();
			}
		});
	}

	private void drawBarGraph() {
		if (N != 0 && fResults.length > 0) {
			for (int n = 0; n < N; n++) {
				Display display = Display.getCurrent();
				Canvas fCanvas = fCanvasSet[n];
				fCanvas.setSize(fCanvas.getParent().getSize().x, 40);

				GC gc = new GC(fCanvas);
				gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				gc.fillRectangle(0, 0, fCanvas.getSize().x, fCanvas.getSize().y);

				int spacing = 5;
				int width = fCanvas.getSize().x - spacing * 8;
				int height = fCanvas.getSize().y;

				// Clear the canvas
				Color outworldTeal = new Color(display, 16, 224, 224);
				gc.setBackground(outworldTeal);
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));

				double widthunit = (double) width / 100;
				int fontsize = (int) (height / 4);
				Font font = new Font(display, display.getSystemFont().getFontData()[0].getName(), fontsize, 1);
				gc.setFont(font);

				int linewidth = 2;
				int topborder;
				int topbarborder;
				int bottomborder = 0;
				int fontspacing = (fontsize * 8) / 5;

				topborder = 0;
				topbarborder = topborder + fontspacing;
				bottomborder = height - spacing;

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
				font.dispose();
				outworldTeal.dispose();
				gc.dispose();
			}
		}
	}

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

	private void updateCoverage() {
		fResults = fCalculator.getResults();
		for (int n = 1; n <= N; n++) {
			drawBarGraph();
			System.out.println("N: " + n + " => " + fResults[n - 1]);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateCoverage();
	}
}
