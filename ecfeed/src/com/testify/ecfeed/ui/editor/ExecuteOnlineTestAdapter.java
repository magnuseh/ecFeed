package com.testify.ecfeed.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.generators.api.IConstraint;
import com.testify.ecfeed.generators.api.IGenerator;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.runner.ParameterizedMethod;
import com.testify.ecfeed.ui.dialogs.ExecuteOnlineSetupDialog;

public class ExecuteOnlineTestAdapter extends ExecuteTestAdapter {

	private MethodDetailsPage fPage;

	private class ExecuteRunnable implements IRunnableWithProgress{

		private IGenerator<PartitionNode> fGenerator;
		private List<List<PartitionNode>> fInput;
		private Collection<IConstraint<PartitionNode>> fConstraints;
		private Map<String, Object> fParameters;

		ExecuteRunnable(IGenerator<PartitionNode> generator, 
				List<List<PartitionNode>> input, 
				Collection<IConstraint<PartitionNode>> constraints, 
				Map<String, Object> parameters){
			fGenerator = generator;
			fInput = input;
			fConstraints = constraints;
			fParameters = parameters;
		}
		
		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			Class<?> testClass = loadTestClass();
			Method testMethod = getTestMethod(testClass, getMethodModel());
			List<PartitionNode> next;
			try {
				fGenerator.initialize(fInput, fConstraints, fParameters);
				monitor.beginTask("Executing test function with generated parameters", fGenerator.totalWork());
				while((next = fGenerator.next()) != null && monitor.isCanceled() == false){
					List<TestCaseNode> testCases = new ArrayList<TestCaseNode>();
					testCases.add(new TestCaseNode("", next));
					ParameterizedMethod frameworkMethod = new ParameterizedMethod(testMethod, testCases);
					frameworkMethod.invokeExplosively(testClass.newInstance(), new Object[]{});
					monitor.worked(fGenerator.workProgress());
				}
				monitor.done();
			} catch (Throwable e) {
				throw new InvocationTargetException(e, e.getMessage());
			}
		}
		
	}

	public ExecuteOnlineTestAdapter(MethodDetailsPage page) {
		fPage = page;
	}

	@Override
	public void widgetSelected(SelectionEvent e){
		ExecuteOnlineSetupDialog dialog = new ExecuteOnlineSetupDialog(fPage.getActiveShell(), 
				getMethodModel());
		if(dialog.open() == IDialogConstants.OK_ID){
			IGenerator<PartitionNode> selectedGenerator = dialog.getSelectedGenerator();
			List<List<PartitionNode>> algorithmInput = dialog.getAlgorithmInput();
			Collection<IConstraint<PartitionNode>> constraints = dialog.getConstraints();
			Map<String, Object> parameters = dialog.getGeneratorParameters();
			
			executeTest(selectedGenerator, algorithmInput, constraints, parameters);
		}
	}
	
	@Override
	protected MethodNode getMethodModel() {
		return fPage.getSelectedMethod();
	}

	private void executeTest(IGenerator<PartitionNode> generator,
			List<List<PartitionNode>> input,
			Collection<IConstraint<PartitionNode>> constraints,
			Map<String, Object> parameters) {

		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(fPage.getActiveShell());
		ExecuteRunnable runnable = new ExecuteRunnable(generator, input, constraints, parameters);
		progressDialog.open();
		try {
			progressDialog.run(true,  true, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Exception:\n", e.getMessage());
		}
	}

}