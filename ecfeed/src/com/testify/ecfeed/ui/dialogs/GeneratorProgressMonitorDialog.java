package com.testify.ecfeed.ui.dialogs;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.testify.ecfeed.generators.api.IGenerator;

public class GeneratorProgressMonitorDialog extends ProgressMonitorDialog {
	
	private IGenerator<?> fGenerator;

	public GeneratorProgressMonitorDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	public GeneratorProgressMonitorDialog(Shell parent, IGenerator<?> generator) {
		super(parent);
		this.fGenerator = generator;
		// TODO Auto-generated constructor stub
	}

	public IGenerator<?> getGenerator() {
		return fGenerator;
	}

	public void setGenerator(IGenerator<?> fGenerator) {
		this.fGenerator = fGenerator;
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
		fGenerator.cancel();
	}

}
