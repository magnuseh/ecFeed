package com.testify.ecfeed.ui.editor;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;

import com.testify.ecfeed.ui.modelif.IModelUpdateContext;

public abstract class JavaDocCommentsSection extends TabFolderCommentsSection {

	private class TabFolderEditButtonListener extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().editComments();
		}
	}

	private Text fCommentsText;
	private Text fJavadocText;

	public JavaDocCommentsSection(ISectionContext sectionContext, IModelUpdateContext updateContext) {
		super(sectionContext, updateContext);

		fCommentsText = addTextTab("Comments", true);
		fJavadocText = addTextTab("JavaDoc", false);

		addEditListener(new TabFolderEditButtonListener());
	}

	@Override
	public void refresh(){
		super.refresh();
		fCommentsText.setText(getTargetIf().getComments());
		getEditButton().setText(fCommentsText.getText().length() > 0 ? "Edit comment" : "Add comment");
	}

	protected Text getJavaDocText(){
		return fJavadocText;
	}

	protected abstract SelectionAdapter getExportAdapter();
	protected abstract SelectionAdapter getImportAdapter();
}
