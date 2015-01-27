package com.testify.ecfeed.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.testify.ecfeed.model.AbstractNode;
import com.testify.ecfeed.ui.common.JavaDocSupport;
import com.testify.ecfeed.ui.modelif.IModelUpdateContext;

public class JavaDocCommentsSection extends TabFolderCommentsSection {

	private class TabFolderEditButtonListener extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().editComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getCommentsItem()));
		}
	}

	protected class ExportSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().exportCommentsToJavadoc(getTargetIf().getComments());
			getTabFolder().setSelection(getTabFolder().indexOf(getJavaDocItem()));
		}
	}

	protected class ImportSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().importJavadocComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getCommentsItem()));
		}
	}

	protected class JavadocExportAllSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().exportAllComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getJavaDocItem()));
		}
	}

	protected class JavadocImportAllSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().importAllJavadocComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getCommentsItem()));
		}
	}

	private TabItem fCommentsTab;
	private TabItem fJavadocTab;
	private Button fExportButton;
	private Button fImportButton;
	private Menu fExportButtonMenu;
	private Menu fImportButtonMenu;

	public JavaDocCommentsSection(ISectionContext sectionContext, IModelUpdateContext updateContext) {
		super(sectionContext, updateContext);

		fCommentsTab = addTextTab("Comments");
		fJavadocTab = addTextTab("JavaDoc");
	}

	@Override
	public void refresh(){
		super.refresh();

		boolean importExportEnabled = importExportEnabled();
		if(getExportButton() != null && getExportButton().isDisposed() == false){
			getExportButton().setEnabled(importExportEnabled);
		}
		if(getImportButton() != null && getImportButton().isDisposed() == false){
			getImportButton().setEnabled(importExportEnabled);
		}

		String comments = getTargetIf().getComments();
		getCommentsText().setText(comments != null ? comments : "");
		String javadoc = JavaDocSupport.getJavadoc(getTarget());
		getJavaDocText().setText(javadoc != null ? javadoc : "");
	}

	@Override
	protected void setInput(AbstractNode input) {
		super.setInput(input);
		refresh();
	}

	protected TabItem getCommentsItem(){
		return fCommentsTab;
	}

	protected TabItem getJavaDocItem(){
		return fJavadocTab;
	}

	protected Text getJavaDocText(){
		return getTextFromTabItem(fJavadocTab);
	}

	protected Text getCommentsText(){
		return getTextFromTabItem(fCommentsTab);
	}

	@Override
	protected void createCommentsButtons(){
		super.createCommentsButtons();

		fExportButton = addButton("Export...", null);
		fImportButton = addButton("Import...", null);

		fExportButton.addSelectionListener(createExportButtonSelectionListener());
		fImportButton.addSelectionListener(createImportButtonSelectionListener());
	}

	protected Button getExportButton(){
		return fExportButton;
	}

	protected Button getImportButton(){
		return fImportButton;
	}

	protected SelectionListener createExportButtonSelectionListener(){
		fExportButtonMenu = new Menu(fExportButton);
		createExportMenuItems();

		return new AbstractSelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fExportButtonMenu.setVisible(true);
			}
		};
	}

	protected SelectionListener createImportButtonSelectionListener(){
		fImportButtonMenu = new Menu(fImportButton);
		createImportMenuItems();
		return new AbstractSelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fImportButtonMenu.setVisible(true);
			}
		};
	}

	protected void createExportMenuItems() {
		MenuItem exportAllItem = new MenuItem(getExportButtonMenu(), SWT.NONE);
		exportAllItem.setText("Export all");
		exportAllItem.addSelectionListener(new JavadocExportAllSelectionAdapter());
		MenuItem exportItem = new MenuItem(getExportButtonMenu(), SWT.NONE);
		exportItem.setText("Export comments of this element only");
		exportItem.addSelectionListener(new ExportSelectionAdapter());
	}

	protected void createImportMenuItems() {
		MenuItem importAllItem = new MenuItem(getImportButtonMenu(), SWT.NONE);
		importAllItem.setText("Import all");
		importAllItem.addSelectionListener(new JavadocImportAllSelectionAdapter());
		MenuItem importItem = new MenuItem(getImportButtonMenu(), SWT.NONE);
		importItem.setText("Import comments of this element only");
		importItem.addSelectionListener(new ImportSelectionAdapter());
	}

	protected boolean importExportEnabled(){
		return getTargetIf().commentsImportExportEnabled();
	}

	protected Menu getExportButtonMenu(){
		return fExportButtonMenu;
	}

	protected Menu getImportButtonMenu(){
		return fImportButtonMenu;
	}

	@Override
	protected SelectionAdapter createEditButtonSelectionAdapter(){
		return new TabFolderEditButtonListener();
	}
}
