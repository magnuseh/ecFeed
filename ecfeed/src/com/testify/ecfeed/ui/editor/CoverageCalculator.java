package com.testify.ecfeed.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.generators.algorithms.Tuples;
import com.testify.ecfeed.generators.algorithms.utils.NWiseUtils;
import com.testify.ecfeed.model.AbstractCategoryNode;
import com.testify.ecfeed.model.ExpectedCategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.ui.common.TreeCheckStateListener;

public class CoverageCalculator {

	private int N;
	private int[] fTuplesCovered;
	private int[] fTotalWork;
	private Map<Integer, PartitionNode> fExpectedPartitions;
	private double[] fResults;
	private boolean cancelled;
	Set<ChangeListener> fListeners;

	private List<List<PartitionNode>> fInput;
	protected List<Map<List<PartitionNode>, Integer>> fTuples;

	private CoverageTreeViewerListener fTreeListener;
	private final MethodNode fMethod;

	public final NWiseUtils<PartitionNode> fCoverageUtils = new NWiseUtils<>();

	public CoverageCalculator(MethodNode method) {
		this.fMethod = method;

		initialize();
	}

	private void initialize() {
		fInput = prepareInput();
		N = fInput.size();
		fTuplesCovered = new int[N];
		fTotalWork = new int[N];
		fResults = new double[N];

		fListeners = new HashSet<>();
		fTuples = new ArrayList<Map<List<PartitionNode>, Integer>>();
		fExpectedPartitions = prepareExpectedPartitions();

		for (int n = 0; n < fTotalWork.length; n++) {
			fTotalWork[n] = fCoverageUtils.calculateTotalTuples(fInput, n + 1, 100);
			fTuples.add(new HashMap<List<PartitionNode>, Integer>());
		}
	}

	private class CalculatorRunnable implements IRunnableWithProgress {
		private List<List<PartitionNode>> fTestCases;
		private CoverageCalculator fCalculator;
		private int N;
		// if true - add occurences, else substract them
		private boolean fIsAdding;
		boolean isCanceled;

		CalculatorRunnable(CoverageCalculator calculator, List<List<PartitionNode>> testCases, boolean isAdding) {
			fCalculator = calculator;
			fTestCases = testCases;
			N = calculator.N;
			fIsAdding = isAdding;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			int n = 0;
			List<Map<List<PartitionNode>, Integer>> coveredTuples = new ArrayList<>();

			monitor.beginTask("Calculating Coverage", fTestCases.size() * N);

			while (!monitor.isCanceled() && n < N) {
				Map<List<PartitionNode>, Integer> mapForN = new HashMap<>();
				for (List<PartitionNode> tcase : fTestCases) {
					if (monitor.isCanceled())
						break;
					Tuples<PartitionNode> tuples = new Tuples<PartitionNode>(tcase, n + 1);
					for (List<PartitionNode> pnode : tuples.getAll()) {
						addTuplesToMap(mapForN, pnode);
					}
					monitor.worked(1);
				}
				if (!monitor.isCanceled()) {
					coveredTuples.add(mapForN);
					n++;
				}
			}

			n = 0;
			if (!monitor.isCanceled()) {
				for (Map<List<PartitionNode>, Integer> map : coveredTuples) {
					mergeOccurrenceMaps(fCalculator.fTuples.get(n), map, fIsAdding);
					fCalculator.fTuplesCovered[n] = fCalculator.fTuples.get(n).size();
					fResults[n] = (((double) fTuplesCovered[n]) / ((double) fTotalWork[n])) * 100;
					n++;
				}
			} else {
				isCanceled = true;
			}
			monitor.done();
		}
	}

	public class CoverageTreeViewerListener extends TreeCheckStateListener{
		protected CheckboxTreeViewer fViewer;
		CoverageCalculator fCalculator;
		List<TestCaseNode> fTestCases;
		String fTestSuiteName;
		boolean fIsSelection;
		// saved tree state
		Object fTreeState[];

		public CoverageTreeViewerListener(CoverageCalculator calculator, CheckboxTreeViewer treeViewer) {
			super(treeViewer);
			this.fCalculator = calculator;
			fViewer = treeViewer;
			fTestCases = new ArrayList<>();
			fTreeState = fViewer.getCheckedElements();
		}

		public void revertLastTreeChange() {
			fViewer.setCheckedElements(fTreeState);
		}

		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object element = event.getElement();
			fIsSelection = event.getChecked();
			// if action is selection
			if (fIsSelection) {
				// TestSuite
				if (element instanceof String) {
					fTestCases.clear();
					fTestSuiteName = (String) element;
					fTestCases.addAll(fMethod.getTestCases(fTestSuiteName));
				}
				// TestCaseNode
				else {
					fTreeState = null;
					fTestCases.clear();
					fTestSuiteName = null;
					fTestCases.add((TestCaseNode) element);
				}
			}
			// if action is deselection
			else {
				// TestSuite
				if (element instanceof String) {
					fTestCases.clear();
					fTestSuiteName = (String) element;

					// if parent is grayed
					for (Object tcase : fTreeState) {
						if (fTestSuiteName.equals(fContentProvider.getParent(tcase))) {
							fTestCases.add((TestCaseNode) tcase);
						}
					}
					// if parent has no children shown in the tree, but they all
					// are implicitly selected
					if (fTestCases.isEmpty()) {
						fTestCases.addAll(fMethod.getTestCases(fTestSuiteName));
					}
				}
				// TestCaseNode
				else {
					fTreeState = null;
					fTestCases.clear();
					fTestSuiteName = null;
					fTestCases.add((TestCaseNode) element);
				}
			}

			fViewer.setSubtreeChecked(element, fIsSelection);
			setParentGreyed(element);

			// Execute core calculator function
			if (fCalculator.calculateCoverage()) {
				//if succeed - save changes to the tree
				fTreeState = fViewer.getCheckedElements();
			}
		}

		/*
		 * @return the cases selected or deselected in the last operation;
		 */
		public List<TestCaseNode> getTestCases() {
			return fTestCases;
		}

		/*
		 * @return if last action was selection (false if it was deselection);
		 */
		public boolean getLastAction() {
			return fIsSelection;
		}

	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~ CORE METHOD ~~~~~~~~~~~~~~~~~~~~~~~~~~
	public boolean calculateCoverage() {
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		try {
			CalculatorRunnable runnable =
					new CalculatorRunnable(this, prepareCasesToAdd(fTreeListener.getTestCases()), fTreeListener.getLastAction());
			progressDialog.open();
			progressDialog.run(true, true, runnable);
			if (runnable.isCanceled) {
				fTreeListener.revertLastTreeChange();
				return false;
			} else {
				notifyListeners();
				return true;
			}

		} catch (InvocationTargetException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Exception", "Invocation: " + e.getCause());
			return false;
		} catch (InterruptedException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Exception", "Interrupted: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

	}

	protected List<List<PartitionNode>> prepareInput() {
		List<List<PartitionNode>> input = new ArrayList<List<PartitionNode>>();
		for (AbstractCategoryNode cnode : fMethod.getCategories()) {
			List<PartitionNode> category = new ArrayList<PartitionNode>();
			for (PartitionNode pnode : cnode.getLeafPartitions()) {
				category.add(pnode);
			}
			input.add(category);
		}
		return input;
	}

	private Map<Integer, PartitionNode> prepareExpectedPartitions() {
		int n = 0;
		Map<Integer, PartitionNode> expected = new HashMap<>();
		for (AbstractCategoryNode cnode : fMethod.getCategories()) {
			if (cnode instanceof ExpectedCategoryNode) {
				expected.put(n, ((ExpectedCategoryNode)cnode).getDefaultValuePartition());
			}
			n++;
		}
		return expected;
	}

	private List<List<PartitionNode>> prepareCasesToAdd(List<TestCaseNode> TestCases) {
		List<List<PartitionNode>> cases = new ArrayList<>();
		if (fExpectedPartitions.isEmpty()) {
			for (TestCaseNode tcnode : TestCases) {
				List<PartitionNode> partitions = new ArrayList<>();
				for (PartitionNode pnode : tcnode.getTestData()) {
					partitions.add(pnode);
				}
				cases.add(partitions);
			}
		} else {
			for (TestCaseNode tcnode : TestCases) {
				List<PartitionNode> partitions = new ArrayList<>();
				int n = 0;
				for (PartitionNode pnode : tcnode.getTestData()) {
					if (fExpectedPartitions.containsKey(n)) {
						partitions.add(fExpectedPartitions.get(n));
					} else {
						partitions.add(pnode);
					}
					n++;
				}
				cases.add(partitions);
			}

		}
		return cases;
	}

	protected static void addTuplesToMap(Map<List<PartitionNode>, Integer> map, List<PartitionNode> tuple) {
		if (!map.containsKey(tuple)) {
			map.put(tuple, 1);
		} else {
			map.put(tuple, map.get(tuple) + 1);
		}
	}

	protected static void mergeOccurrenceMaps(Map<List<PartitionNode>, Integer> targetMap, Map<List<PartitionNode>, Integer> sourceMap,
			boolean isAdding) {
		if (isAdding) {
			for (List<PartitionNode> key : sourceMap.keySet()) {
				if (!targetMap.containsKey(key)) {
					targetMap.put(key, sourceMap.get(key));
				} else {
					targetMap.put(key, sourceMap.get(key) + targetMap.get(key));
				}
			}
		} else {
			for (List<PartitionNode> key : sourceMap.keySet()) {
				if (!targetMap.containsKey(key)) {
					System.err.println("Negative occurences...");
					// targetMap.put(key, -sourceMap.get(key));
				} else {
					int i = targetMap.get(key) - sourceMap.get(key);
					if (i <= 0)
						targetMap.remove(key);
					else
						targetMap.put(key, i);
				}
			}
		}
	}
	
	//notify GUI to update
	public void notifyListeners() {
		for (ChangeListener listener : fListeners) {
			listener.stateChanged(new ChangeEvent(fResults));
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~GETTERS & SETTERS~~~~~~~~~~~~~~~~~~~~~~

	public int getN() {
		return N;
	}

	public double[] getResults() {
		return fResults;
	}

	public int[] getTuplesCovered() {
		return fTuplesCovered;
	}

	public int[] getTotalWork() {
		return fTotalWork;
	}

	public List<List<PartitionNode>> getInput() {
		return fInput;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void cancel() {
		this.cancelled = true;
	}

	public MethodNode getMethod() {
		return fMethod;
	}

	public CoverageTreeViewerListener generateTreeViewerListener(CheckboxTreeViewer treeViewer) {
		fTreeListener = new CoverageTreeViewerListener(this, treeViewer);
		return fTreeListener;
	}

	public void addChangeListener(ChangeListener listener) {
		fListeners.add(listener);
	}

	public Map<Integer, PartitionNode> getExpectedPartitions() {
		return fExpectedPartitions;
	}

	public List<Map<List<PartitionNode>, Integer>> getTuples() {
		return fTuples;
	}

}
