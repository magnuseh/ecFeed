package com.testify.ecfeed.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.model.AbstractNode;
import com.testify.ecfeed.ui.modelif.IModelUpdateContext;

public abstract class AbstractCommentsSection extends ButtonsCompositeSection {

	private final static int STYLE = Section.TITLE_BAR | Section.COMPACT | Section.TWISTIE;
	private final static String SECTION_TITLE = "Comments";

	private Button fEditButton;

	private AbstractNode fTarget;

	public AbstractCommentsSection(ISectionContext sectionContext,
			IModelUpdateContext updateContext) {
		super(sectionContext, updateContext, STYLE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		getSection().setLayoutData(gd);
		getSection().setText(SECTION_TITLE);
	}

	@Override
	protected void createContent(){
		super.createContent();
	}

	@Override
	protected Composite createClientComposite() {
		Composite client = super.createClientComposite();
		createCommentsComposite();
		createCommentsButtons();
		return client;
	}

	protected void createCommentsButtons() {
		fEditButton = addButton("Edit", null);
	}

	protected void addEditListener(SelectionAdapter listener){
		fEditButton.addSelectionListener(listener);
	}

	protected void setInput(AbstractNode input){
		fTarget = input;
	}

	protected AbstractNode getTarget(){
		return fTarget;
	}

	protected Button getEditButton(){
		return fEditButton;
	}

	protected abstract void createCommentsComposite();
}