/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.ui.editor.modeleditor;

import java.util.Collection;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeNodeContentProvider;

import com.testify.ecfeed.api.IConstraint;
import com.testify.ecfeed.api.ITestGenAlgorithm;
import com.testify.ecfeed.constants.Constants;
import com.testify.ecfeed.constants.DialogStrings;
import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.Constraint;
import com.testify.ecfeed.model.constraint.StaticStatement;
import com.testify.ecfeed.ui.common.TreeCheckStateListener;
import com.testify.ecfeed.ui.dialogs.AddTestCaseDialog;
import com.testify.ecfeed.ui.dialogs.GenerateTestSuiteDialog;
import com.testify.ecfeed.ui.dialogs.RenameTestSuiteDialog;
import com.testify.ecfeed.ui.dialogs.TestMethodRenameDialog;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.wb.swt.TableViewerColumnSorter;
import org.eclipse.jface.window.Window;

public class MethodNodeDetailsPage extends GenericNodeDetailsPage{
	private Label fMethodNameLabel;
	private MethodNode fSelectedMethod;
	private CheckboxTreeViewer fTestCasesViewer;
	private CheckboxTableViewer fConstraintsViewer;
	private TableViewer fParametersViewer;
	private Section fMainSection;
	private Section fConstraintsSection;

	private class TestCaseViewerContentProvider extends TreeNodeContentProvider implements ITreeContentProvider{
		public final Object[] EMPTY_ARRAY = new Object[]{};

		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof MethodNode){
				return ((MethodNode)inputElement).getTestSuites().toArray();
			}
			return null;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof String){
				Collection<TestCaseNode> testCases = fSelectedMethod.getTestCases((String)parentElement);
				if(testCases.size() < Constants.MAX_DISPLAYED_TEST_CASES_PER_SUITE){
					return testCases.toArray();
				}
			}
			return EMPTY_ARRAY;
		}

		@Override
		public Object getParent(Object element) {
			if(element instanceof TestCaseNode){
				return ((TestCaseNode)element).getName();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
		
	}
	
	public MethodNodeDetailsPage(ModelMasterDetailsBlock parentBlock){
		super(parentBlock);
	}

	public void createContents(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		fMainSection = getToolkit().createSection(parent, Section.TITLE_BAR);
		getToolkit().paintBordersFor(fMainSection);
		fMainSection.setText("Method");
		
		Composite mainComposite = new Composite(fMainSection, SWT.NONE);
		getToolkit().adapt(mainComposite);
		getToolkit().paintBordersFor(mainComposite);
		fMainSection.setClient(mainComposite);
		mainComposite.setLayout(new GridLayout(1, false));
		
		createMethodNameComposite(mainComposite);
		
		createParametersSection(mainComposite);
		
		createConstraintsSection(mainComposite);

		createTestCasesSection(mainComposite);
	}

	private void createMethodNameComposite(Composite composite) {
		Composite methodNameComposite = new Composite(composite, SWT.NONE);
		methodNameComposite.setLayout(new GridLayout(2, false));
		methodNameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		getToolkit().adapt(methodNameComposite);
		getToolkit().paintBordersFor(methodNameComposite);
		
		fMethodNameLabel = new Label(methodNameComposite, SWT.NONE);
		fMethodNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		getToolkit().adapt(fMethodNameLabel, true, true);
		
		createButton(methodNameComposite, "Change", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				TestMethodRenameDialog dialog = new TestMethodRenameDialog(getActiveShell(), fSelectedMethod);
				if(dialog.open() == IDialogConstants.OK_ID){
					MethodNode selectedMethod = dialog.getSelectedMethod();
					fSelectedMethod.setName(selectedMethod.getName());
					ArrayList<CategoryNode> parameters = fSelectedMethod.getCategories();
					ArrayList<CategoryNode> newParameters = selectedMethod.getCategories();
					for(int i = 0; i < parameters.size(); i++){
						parameters.get(i).setName(newParameters.get(i).getName());
					}
					updateModel(fSelectedMethod);
				}
			}
		});
	}

	private void createParametersSection(Composite composite) {
		Section parametersSection = getToolkit().createSection(composite, Section.EXPANDED | Section.TWISTIE | Section.TITLE_BAR);
		parametersSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		getToolkit().paintBordersFor(parametersSection);
		parametersSection.setText("Parameters");
		parametersSection.setExpanded(false);
		
		Composite parametersComposite = new Composite(parametersSection, SWT.NONE);
		getToolkit().adapt(parametersComposite);
		getToolkit().paintBordersFor(parametersComposite);
		parametersSection.setClient(parametersComposite);
		
		createParametersViewer(parametersComposite);
	}
	
	private void createParametersViewer(Composite composite) {
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		fParametersViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		fParametersViewer.setContentProvider(new ArrayContentProvider());
		fParametersViewer.addDoubleClickListener(new ChildrenViewerDoubleClickListener());
		Table parametersTable = fParametersViewer.getTable();
		parametersTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		parametersTable.setHeaderVisible(true);
		getToolkit().paintBordersFor(parametersTable);
		
		createParametersColumns();
	}
	
	private void createParametersColumns() {
		createTableViewerColumn(fParametersViewer, "Name", 100, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((CategoryNode)element).getName();
			}
		});
		createTableViewerColumn(fParametersViewer, "Type", 100, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((CategoryNode)element).getType();
			}
		});
	}
	
	private void createConstraintsSection(Composite composite) {
		fConstraintsSection = getToolkit().createSection(composite, Section.EXPANDED | Section.TWISTIE | Section.TITLE_BAR);
		fConstraintsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		getToolkit().paintBordersFor(fConstraintsSection);
		fConstraintsSection.setText("Constraints");
		fConstraintsSection.setExpanded(false);
		
		Composite constraintsComposite = new Composite(fConstraintsSection, SWT.NONE);
		getToolkit().adapt(constraintsComposite);
		getToolkit().paintBordersFor(constraintsComposite);
		fConstraintsSection.setClient(constraintsComposite);
		constraintsComposite.setLayout(new GridLayout(1, false));
		constraintsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createConstraintsViewer(constraintsComposite);
		
		createConstraintsButtons(constraintsComposite);

	}
	

	
	private void createConstraintsViewer(Composite composite) {
		fConstraintsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);
		fConstraintsViewer.setContentProvider(new ArrayContentProvider());
		fConstraintsViewer.addDoubleClickListener(new ChildrenViewerDoubleClickListener());
		Table constraintsTable = fConstraintsViewer.getTable();
		constraintsTable.setHeaderVisible(true);
		constraintsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		getToolkit().paintBordersFor(constraintsTable);
		
		TableViewerColumn constraintNameViewerColumn = createTableViewerColumn(fConstraintsViewer, "Name", 
				130, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((ConstraintNode)element).getName();
			}
		});
		
		new TableViewerColumnSorter(constraintNameViewerColumn) {
			protected Object getValue(Object o) {
				return ((ConstraintNode)o).getName();
			}
		};

		createTableViewerColumn(fConstraintsViewer, "Definition", 100, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((ConstraintNode)element).getConstraint().toString();
			}
		});
	}

	private void createConstraintsButtons(Composite composite) {
		Composite constraintsButtonsComposite = new Composite(composite, SWT.NONE);
		constraintsButtonsComposite.setLayout(new RowLayout());

		createAddConstraintButton(constraintsButtonsComposite);
		createRemoveSelectedConstraintsButton(constraintsButtonsComposite);
	}

	private void createAddConstraintButton(Composite composite) {
		createButton(composite, "Add Constraint", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				String name = Constants.DEFAULT_CONSTRAINT_NAME;
				BasicStatement premise = new StaticStatement(true);
				BasicStatement consequence = new StaticStatement(true);
				fSelectedMethod.addConstraint(new ConstraintNode(name, new Constraint(premise, consequence)));
				updateModel(fSelectedMethod);
				fConstraintsSection.layout();
				fMainSection.layout();
			}
		});
	}
	
	private void createRemoveSelectedConstraintsButton(Composite composite) {
		createButton(composite, "Remove Selected", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				MessageDialog infoDialog = new MessageDialog(Display.getDefault().getActiveShell(), 
						DialogStrings.DIALOG_REMOVE_CONSTRAINTS_TITLE, 
						Display.getDefault().getSystemImage(SWT.ICON_QUESTION), 
						DialogStrings.DIALOG_REMOVE_CONSTRAINTS_MESSAGE,
						MessageDialog.QUESTION_WITH_CANCEL, 
						new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, IDialogConstants.OK_ID);
				if(infoDialog.open() == IDialogConstants.OK_ID){
					for(Object constraint : fConstraintsViewer.getCheckedElements()){
						fSelectedMethod.removeConstraint((ConstraintNode)constraint);
					}
					updateModel((RootNode)fSelectedMethod.getRoot());
				}
			}
		});
	}

	private void createTestCasesSection(Composite mainComposite) {
		Section testCasesSection = getToolkit().createSection(mainComposite, Section.TITLE_BAR);
		testCasesSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		getToolkit().paintBordersFor(testCasesSection);
		testCasesSection.setText("Test Cases");
		
		Composite testCasesComposite = new Composite(testCasesSection, SWT.NONE);
		getToolkit().adapt(testCasesComposite);
		getToolkit().paintBordersFor(testCasesComposite);
		testCasesSection.setClient(testCasesComposite);
		testCasesComposite.setLayout(new GridLayout(2, false));
		
		createTestCasesViewer(testCasesComposite);
		
		createTestCasesSectionButtons(testCasesComposite);

	}
	private void createTestCasesViewer(Composite composite) {
		fTestCasesViewer = new CheckboxTreeViewer(composite);
		fTestCasesViewer.setContentProvider(new TestCaseViewerContentProvider());
		fTestCasesViewer.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element){
				if(element instanceof String){
					int testCasesCount = fSelectedMethod.getTestCases((String)element).size();
					return (String)element + " [" +  testCasesCount + " test case" + 
							(testCasesCount == 1?"":"s") + "]";
				}
				else if(element instanceof TestCaseNode){
					return fSelectedMethod.getName() + "(" + ((TestCaseNode)element).testDataString() + ")";
				}
				return null;
			}
		});
		fTestCasesViewer.addDoubleClickListener(new ChildrenViewerDoubleClickListener());
		fTestCasesViewer.addCheckStateListener(new TreeCheckStateListener(fTestCasesViewer));
		fTestCasesViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	private void createTestCasesSectionButtons(Composite testCasesComposite) {
		Composite testCasesButonsComposite = new Composite(testCasesComposite, SWT.NONE);
		testCasesButonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		getToolkit().adapt(testCasesButonsComposite);
		getToolkit().paintBordersFor(testCasesButonsComposite);
		testCasesButonsComposite.setLayout(new GridLayout(1, false));
		
		createAddTestCaseButton(testCasesButonsComposite);

		createRenameSuiteButton(testCasesButonsComposite);
		
		createGenerateTestSuiteButton(testCasesButonsComposite);
		
		createRemoveSelectedButton(testCasesButonsComposite);
	}
	
	private void createAddTestCaseButton(Composite testCasesButonsComposite) {
		Button button = createButton(testCasesButonsComposite, "Add Test Case", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				AddTestCaseDialog dialog = new AddTestCaseDialog(getActiveShell(), fSelectedMethod);
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					String testSuite = dialog.getTestSuite();
					ArrayList<PartitionNode> testData = dialog.getTestData();
					fSelectedMethod.addTestCase(new TestCaseNode(testSuite, testData));
					updateModel(fSelectedMethod);
				}
			}
		});
		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	private void createRenameSuiteButton(Composite testCasesButonsComposite) {
		Button button = createButton(testCasesButonsComposite, "Rename Suite...", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				RenameTestSuiteDialog dialog = 
						new RenameTestSuiteDialog(Display.getDefault().getActiveShell(), fSelectedMethod.getTestSuites());
				dialog.create();
				if (dialog.open() == Window.OK) {
					String oldName = dialog.getRenamedTestSuite();
					String newName = dialog.getNewName();
					Collection<TestCaseNode> testSuite = fSelectedMethod.getTestCases(oldName);
					for(TestCaseNode testCase : testSuite){
						testCase.setName(newName);
					}
					updateModel((RootNode)fSelectedMethod.getRoot());
				}
			}
		});
		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	private void createGenerateTestSuiteButton(Composite testCasesButonsComposite) {
		Button button = createButton(testCasesButonsComposite, "Generate Test Suite", new SelectionAdapter() {
			@SuppressWarnings({"rawtypes"})
			@Override
			public void widgetSelected(SelectionEvent e){
				GenerateTestSuiteDialog dialog = new GenerateTestSuiteDialog(getActiveShell(), fSelectedMethod);
				if(dialog.open() == IDialogConstants.OK_ID){
					ITestGenAlgorithm selectedAlgorithm = dialog.getSelectedAlgorithm();
					ArrayList[] algorithmInput = dialog.getAlgorithmInput();
					IConstraint[] constraints = dialog.getConstraints();
					
					ArrayList[] generatedData = selectedAlgorithm.generate(algorithmInput, constraints);
					if(generatedData == null){
						return;
					}
					if(generatedData.length > 0){
						if(generatedData.length > Constants.TEST_SUITE_SIZE_WARNING_LIMIT){
							MessageDialog warningDialog = new MessageDialog(Display.getDefault().getActiveShell(), 
									DialogStrings.DIALOG_LARGE_TEST_SUITE_GENERATED_TITLE, 
									Display.getDefault().getSystemImage(SWT.ICON_WARNING), 
									DialogStrings.DIALOG_LARGE_TEST_SUITE_GENERATED_MESSAGE(generatedData.length),
									MessageDialog.WARNING, 
									new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, IDialogConstants.OK_ID);
							if(warningDialog.open() == IDialogConstants.CANCEL_ID){
								return;
							}
						}
						addTestSuiteToModel(dialog, generatedData);
					}
					else{
						new MessageDialog(Display.getDefault().getActiveShell(), 
								DialogStrings.DIALOG_EMPTY_TEST_SUITE_GENERATED_TITLE, 
								Display.getDefault().getSystemImage(SWT.ICON_INFORMATION), 
								DialogStrings.DIALOG_EMPTY_TEST_SUITE_GENERATED_MESSAGE,
								MessageDialog.INFORMATION, 
								new String[] {IDialogConstants.OK_LABEL}, IDialogConstants.OK_ID).open();
					}
				}
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			private void addTestSuiteToModel(GenerateTestSuiteDialog dialog,
					ArrayList[] generatedData) {
				for(ArrayList testCase : generatedData){
					ArrayList<PartitionNode> testData = (ArrayList<PartitionNode>)testCase;
					TestCaseNode testCaseNode = new TestCaseNode(dialog.getTestSuiteName(), testData);
					fSelectedMethod.addTestCase(testCaseNode);
				}
				updateModel(fSelectedMethod);
			}
		});
		
		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	private void createRemoveSelectedButton(Composite testCasesButonsComposite) {
		Button button = createButton(testCasesButonsComposite, "Remove Selected", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog infoDialog = new MessageDialog(Display.getDefault().getActiveShell(), 
						DialogStrings.DIALOG_REMOVE_TEST_CASES_TITLE, Display.getDefault().getSystemImage(SWT.ICON_QUESTION), 
						DialogStrings.DIALOG_REMOVE_TEST_CASES_MESSAGE,
						MessageDialog.QUESTION_WITH_CANCEL, 
						new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, IDialogConstants.OK_ID);
				if(infoDialog.open() == IDialogConstants.OK_ID){
					removeCheckedTestSuites();
					removeCheckedTestCases();
					
					for(String testSuite : fSelectedMethod.getTestSuites()){
						fTestCasesViewer.setGrayChecked(testSuite, false);
					}
					updateModel((RootNode)fSelectedMethod.getRoot());
				}
			}

			private void removeCheckedTestSuites() {
				for(String testSuite : fSelectedMethod.getTestSuites()){
					if(fTestCasesViewer.getChecked(testSuite) && !fTestCasesViewer.getGrayed(testSuite)){
						fSelectedMethod.removeTestSuite(testSuite);
					}
				}
			}

			private void removeCheckedTestCases() {
				for(Object element : fTestCasesViewer.getCheckedElements()){
					if(element instanceof TestCaseNode){
						fSelectedMethod.removeChild((TestCaseNode)element);
					}
				}
			}
		});
		
		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	public void selectionChanged(IFormPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		fSelectedMethod = (MethodNode)fSelectedNode;
		fParametersViewer.setInput(fSelectedMethod.getCategories());
		fConstraintsViewer.setInput(fSelectedMethod.getConstraints());
		fTestCasesViewer.setInput(fSelectedMethod);
		refresh();
	}

	public void refresh() {
		if(fSelectedMethod == null){
			return;
		}
		fMainSection.setText(fSelectedMethod.toString());
		fMethodNameLabel.setText("Method name: " + fSelectedMethod.toString());
		fParametersViewer.refresh();
		fConstraintsViewer.refresh();
		fTestCasesViewer.refresh();
		fConstraintsSection.layout();
		fMainSection.layout();
	}
}