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

package com.testify.ecfeed.ui.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.GenericNode;
import com.testify.ecfeed.model.IModelVisitor;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.modelif.ModelOperationManager;
import com.testify.ecfeed.ui.common.Constants;
import com.testify.ecfeed.ui.editor.actions.AbstractAddChildAction;
import com.testify.ecfeed.ui.editor.actions.AddChildActionFactory;
import com.testify.ecfeed.ui.editor.actions.CollapseAction;
import com.testify.ecfeed.ui.editor.actions.CopyAction;
import com.testify.ecfeed.ui.editor.actions.CutAction;
import com.testify.ecfeed.ui.editor.actions.DeleteAction;
import com.testify.ecfeed.ui.editor.actions.ExpandAction;
import com.testify.ecfeed.ui.editor.actions.ModelViewerToolbarUpdater;
import com.testify.ecfeed.ui.editor.actions.MoveUpDownAction;
import com.testify.ecfeed.ui.editor.actions.PasteAction;
import com.testify.ecfeed.ui.editor.actions.RedoAction;
import com.testify.ecfeed.ui.editor.actions.SelectAllAction;
import com.testify.ecfeed.ui.editor.actions.UndoAction;
import com.testify.ecfeed.ui.modelif.CategoryInterface;
import com.testify.ecfeed.ui.modelif.GenericNodeInterface;
import com.testify.ecfeed.ui.modelif.IModelUpdateListener;
import com.testify.ecfeed.ui.modelif.SelectionInterface;

public class ModelMasterSection extends TreeViewerSection{
	private static final int STYLE = Section.EXPANDED | Section.TITLE_BAR;
	private static final int AUTO_EXPAND_LEVEL = 3;

	private Button fMoveUpButton;
	private Button fMoveDownButton;
	private ModelMasterDetailsBlock fMasterDetailsBlock;
	
	private class ModelWrapper{
		private RootNode fModel;
		
		public ModelWrapper(RootNode model){
			fModel = model;
		}
		
		public RootNode getModel(){
			return fModel;
		}
	}
	
	private class ModelContentProvider extends TreeNodeContentProvider implements ITreeContentProvider {

		public final Object[] EMPTY_ARRAY = {};

		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof ModelWrapper){
				RootNode root = ((ModelWrapper)inputElement).getModel(); 
				return new Object[]{root};
			}
			return getChildren(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			//Because of potentially large amount of children, MethodNode is special case
			//We filter out test suites with too many test cases
			if(parentElement instanceof MethodNode){
				MethodNode method = (MethodNode)parentElement;
				ArrayList<Object> children = new ArrayList<Object>();
				children.addAll(method.getCategories());
				children.addAll(method.getConstraintNodes());
				for(String testSuite : method.getTestSuites()){
					Collection<TestCaseNode> testCases = method.getTestCases(testSuite);
					if(testCases.size() <= Constants.MAX_DISPLAYED_TEST_CASES_PER_SUITE){
						children.addAll(testCases);
					}
				}
				return children.toArray();
			}

			if(parentElement instanceof CategoryNode){
				CategoryNode category = (CategoryNode)parentElement;
				if(category.isExpected() && CategoryInterface.isPrimitive(category.getType())){
					return EMPTY_ARRAY;
				}
			}
			if(parentElement instanceof GenericNode){
				GenericNode node = (GenericNode)parentElement;
				if(node.getChildren().size() < Constants.MAX_DISPLAYED_CHILDREN_PER_NODE){
					return node.getChildren().toArray();
				}
			}
			return EMPTY_ARRAY;
		}

		@Override
		public Object getParent(Object element) {
			if(element instanceof GenericNode){
				return ((GenericNode)element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	private class ModelLabelProvider extends LabelProvider {

		public String getText(Object element){
			if(element instanceof GenericNode){
				if(element instanceof CategoryNode){
					return ((CategoryNode)element).toShortString();
				}
				return element.toString();
			}
			return null;
		}

		public Image getImage(Object element){
			if (element instanceof RootNode){
				return getImage("root_node.png");
			} else if (element instanceof ClassNode){
				return getImage("class_node.png");
			} else if (element instanceof MethodNode){
				return getImage("method_node.png");
			} else if(element instanceof TestCaseNode){
				return getImage("test_case_node.png");
			} else if (element instanceof CategoryNode){
				return getImage("category_node.png");
			} else if (element instanceof ConstraintNode){
				return getImage("constraint_node.png");
			} else if (element instanceof PartitionNode){
				return getImage("partition_node.png");
			}
			return getImage("sample.png");
		}

		private Image getImage(String file) {
			Bundle bundle = FrameworkUtil.getBundle(ModelLabelProvider.class);
			URL url = FileLocator.find(bundle, new Path("icons/" + file), null);
			ImageDescriptor image = ImageDescriptor.createFromURL(url);
			return image.createImage();

		}
	}

	private class ModelLabelDecorator implements ILabelDecorator {

		Map<List<Image>, Image> fFusedImages;

		private class DecorationProvider implements IModelVisitor{
			GenericNodeInterface fNodeInterface;

			public DecorationProvider(){
				fNodeInterface = new GenericNodeInterface();
			}

			@Override
			public Object visit(RootNode node) throws Exception {
				return Arrays.asList(new Image[]{implementationStatusDecoration(node)});
			}

			@Override
			public Object visit(ClassNode node) throws Exception {
				return Arrays.asList(new Image[]{implementationStatusDecoration(node)});
			}

			@Override
			public Object visit(MethodNode node) throws Exception {
				return Arrays.asList(new Image[]{implementationStatusDecoration(node)});
			}

			@Override
			public Object visit(CategoryNode node) throws Exception {
				List<Image> decorations = new ArrayList<Image>();
				decorations.add(implementationStatusDecoration(node));
				if(node.isExpected()){
					decorations.add(getImage("expected.png"));
				}
				return decorations;
			}

			@Override
			public Object visit(TestCaseNode node) throws Exception {
				return Arrays.asList(new Image[]{implementationStatusDecoration(node)});
			}

			@Override
			public Object visit(ConstraintNode node) throws Exception {
				return Arrays.asList(new Image[]{implementationStatusDecoration(node)});
			}

			@Override
			public Object visit(PartitionNode node) throws Exception {
				List<Image> decorations = new ArrayList<Image>();
				decorations.add(implementationStatusDecoration(node));
				if(node.isAbstract()){
					decorations.add(getImage("abstract.png"));
				}
				return decorations;
			}

			private Image implementationStatusDecoration(GenericNode node) {
				switch (fNodeInterface.implementationStatus(node)){
				case IMPLEMENTED:
					return getImage("implemented.png");
				case PARTIALLY_IMPLEMENTED:
					return getImage("partially_implemented.png");
				case NOT_IMPLEMENTED:
					return getImage("unimplemented.png");
				case IRRELEVANT:
				default:
					return null;
				}
			}
		}

		public ModelLabelDecorator() {
			fFusedImages = new HashMap<List<Image>, Image>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Image decorateImage(Image image, Object element) {
			if(element instanceof GenericNode){
				try{
					List<Image> decorations = (List<Image>)((GenericNode)element).accept(new DecorationProvider());
					List<Image> all = new ArrayList<Image>(decorations);
					all.add(0, image);
					if(fFusedImages.containsKey(all) == false){
						Image decorated = new Image(Display.getCurrent(), image.getImageData());
						for(Image decoration : decorations){
							if(decoration != null){
								decorated = fuseImages(decorated, decoration, 0, 0);
							}
						}
						fFusedImages.put(decorations, decorated);
					}
					return fFusedImages.get(decorations);
				}catch(Exception e){}
			}
			return image;
		}

		@Override
		public String decorateText(String text, Object element) {
			return text;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		private Image getImage(String file) {
			Bundle bundle = FrameworkUtil.getBundle(ModelLabelDecorator.class);
			URL url = FileLocator.find(bundle, new Path("icons/" + file), null);
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
			return descriptor.createImage();
		}

		private Image fuseImages(Image icon, Image decorator, int x, int y){
			ImageData idIcon = (ImageData)icon.getImageData().clone();
			ImageData idDecorator = decorator.getImageData();
			if(idIcon.width <= x || idIcon.height <= y){
				return icon;
			}
			int rbw = (idDecorator.width + x > idIcon.width) ? (idDecorator.width + x - idIcon.width) : idDecorator.width;
			int rbh = (idDecorator.height + y > idIcon.height) ? (idDecorator.height + y - idIcon.height) : idDecorator.height;		

			int indexa = y*idIcon.scanlinePad + x;
			int indexb = 0;

			for(int row = 0; row < rbh; row ++){
				for(int col = 0; col < rbw; col++){
					if(idDecorator.alphaData[indexb] < 0){
						idIcon.alphaData[indexa] = (byte)-1;
						idIcon.data[4*indexa]=idDecorator.data[4*indexb];
						idIcon.data[4*indexa+1]=idDecorator.data[4*indexb+1];
						idIcon.data[4*indexa+2]=idDecorator.data[4*indexb+2];
					}
					indexa += 1;
					indexb += 1;
				}
				indexa += x;
			}
			return new Image(Display.getDefault(), idIcon);
		}
	}
	
	private class UpdateListener implements IModelUpdateListener{
		@Override
		public void modelUpdated(AbstractFormPart source) {
			source.markDirty();
			refresh();
			enableSortButtons(getSelection());
		}
	}

	private class MoveUpDownAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			boolean up = e.getSource() == fMoveUpButton;
			moveSelectedItem(up);
			enableSortButtons(getSelection());
		}

		@SuppressWarnings("unchecked")
		private void moveSelectedItem(boolean moveUp){
			SelectionInterface selectionIf = new SelectionInterface();
			selectionIf.setTarget(getSelection().toList());
			selectionIf.moveUpDown(moveUp, ModelMasterSection.this);
		}

	}

	private class ModelSelectionListener implements ISelectionChangedListener{
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			enableSortButtons(selection);
		}
	}
	
	private class ModelViewerMenuAdapter implements MenuListener{

		public class MenuOperation{
			private String fName;
			private Action fAction;

			public MenuOperation(String name, Action action){
				fName = name;
				fAction = action;
			}

			public void execute(){
				fAction.run();
			}

			public boolean isEnabled(){
				return fAction.isEnabled();
			}

			public String getName(){
				return fName;
			}
		}

		private class MenuSelectionAdapter extends SelectionAdapter{
			
			private MenuOperation fOperation;

			public MenuSelectionAdapter(MenuOperation operation){
				fOperation = operation;
			}
			
			@Override
			public void widgetSelected(SelectionEvent e){
				fOperation.execute();
			}
		}

		@Override
		public void menuHidden(MenuEvent e) {
		}

		@Override
		public void menuShown(MenuEvent e) {
			for(MenuItem item : ((Menu)e.getSource()).getItems()){
				item.dispose();
			}
			populate((Menu)e.getSource(), (IStructuredSelection)getSelection());
		}

		protected void populate(Menu menu, IStructuredSelection selection){
			if(selection.isEmpty() == false){
				populateMenu(menu, selection);
			}
		}

		@SuppressWarnings("unchecked")
		protected void populateMenu(Menu menu, IStructuredSelection selection) {
			List<GenericNode> selected = selection.toList();
			if(selected.size() == 1 && selected.get(0) instanceof GenericNode){
				addNewChildOperations(menu, (GenericNode)selected.get(0));
			}
			addCommonOperations(menu, selected);
			addTreeOperations(menu, selected);
			addMoveOperations(menu, selected);
		}

		protected void addOperation(Menu menu, MenuOperation operation){
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setText(operation.getName());
			item.setEnabled(operation.isEnabled());
			item.addSelectionListener(new MenuSelectionAdapter(operation));
		}
		
		private void addNewChildOperations(Menu menu, GenericNode node) {
			List<AbstractAddChildAction> actions = new AddChildActionFactory(getTreeViewer(), ModelMasterSection.this).getPossibleActions(node);
			for(AbstractAddChildAction action : actions){
				addOperation(menu, new MenuOperation(action.getName(), action));
			}
		}

		private void addCommonOperations(Menu menu, List<GenericNode> selected) {
			new MenuItem(menu, SWT.SEPARATOR);
			addOperation(menu, new MenuOperation("Undo\tCtrl+Z", new UndoAction(ModelMasterSection.this)));
			addOperation(menu, new MenuOperation("Redo\tCtrl+Shift+Z", new RedoAction(ModelMasterSection.this)));
			new MenuItem(menu, SWT.SEPARATOR);
			addOperation(menu, new MenuOperation("Copy\tCtrl+C", new CopyAction(getTreeViewer())));
			addOperation(menu, new MenuOperation("Cut\tCtrl+X", new CutAction(getTreeViewer(), ModelMasterSection.this)));
			addOperation(menu, new MenuOperation("Paste\tCtrl+V", new PasteAction(getTreeViewer(), ModelMasterSection.this)));
			addOperation(menu, new MenuOperation("Delete\tDEL", new DeleteAction(ModelMasterSection.this, ModelMasterSection.this)));
		}

		private void addTreeOperations(Menu menu, List<GenericNode> selected) {
			new MenuItem(menu, SWT.SEPARATOR);
			addOperation(menu, new MenuOperation("Select All\tCtrl+A", new SelectAllAction(getTreeViewer(), false)));
			addOperation(menu, new MenuOperation("Expand all\tCTRL+ALT+E", new ExpandAction(getTreeViewer())));
			addOperation(menu, new MenuOperation("Collapse\tCTRL+ALT+W", new CollapseAction(getTreeViewer())));
		}

		private void addMoveOperations(Menu menu, List<GenericNode> selected) {
			new MenuItem(menu, SWT.SEPARATOR);
			addOperation(menu, new MenuOperation("Move Up\tCtrl+UP", new MoveUpDownAction(true, getTreeViewer(), ModelMasterSection.this)));
			addOperation(menu, new MenuOperation("Move Down\tCtrl+DOWN", new MoveUpDownAction(false, getTreeViewer(), ModelMasterSection.this)));
		}
	}

	
	public ModelMasterSection(ModelMasterDetailsBlock masterDetailsBlock, Composite parent, FormToolkit toolkit, ModelOperationManager operationManager, IActionBars actionBars) {
		super(parent, toolkit, STYLE, null, operationManager);
		fMasterDetailsBlock = masterDetailsBlock;
		setModelUpdateListener(new UpdateListener());
		createMenu(operationManager, getUpdateListener());
		addKeyListener(SWT.DEL, new DeleteAction(getViewer(), this));
		setToolbarUpdater(new ModelViewerToolbarUpdater(getTreeViewer(), this, actionBars));
	}
	
	public void setInput(RootNode model){
		setInput(new ModelWrapper(model));
	}
	
	@Override
	public void refresh(){
		super.refresh();
		IDetailsPage page = fMasterDetailsBlock.getCurrentPage();
		if(page != null){
			page.refresh();
		}
	}

	@Override
	protected void createContent(){
		super.createContent();
		getSection().setText("Structure");
		fMoveUpButton = addButton("Move Up", new MoveUpDownAdapter());
		fMoveDownButton = addButton("Move Down", new MoveUpDownAdapter());
		getTreeViewer().setAutoExpandLevel(AUTO_EXPAND_LEVEL);
		addSelectionChangedListener(new ModelSelectionListener());
		addKeyListener(SWT.DEL, new DeleteAction(getViewer(), this));
	}
	
	@Override
	protected IContentProvider viewerContentProvider() {
		return new ModelContentProvider();
	}
	
	@Override 
	protected IBaseLabelProvider viewerLabelProvider() {
		return new DecoratingLabelProvider(new ModelLabelProvider(), new ModelLabelDecorator());
	}
	
	protected void createMenu(ModelOperationManager operationManager, IModelUpdateListener updateListener){
		Menu menu = new Menu(getTree());
		getTree().setMenu(menu);
		menu.addMenuListener(new ModelViewerMenuAdapter());
	}
	
	@SuppressWarnings("unchecked")
	private void enableSortButtons(IStructuredSelection selection) {
		boolean moveUpEnabled = true;
		boolean moveDownEnabled = true;
		SelectionInterface selectionIf = new SelectionInterface();
		if(selection.isEmpty() == false){
			selectionIf.setTarget(getSelection().toList());

			if (selection.getFirstElement() instanceof RootNode) {
				moveUpEnabled = moveDownEnabled = false;
			}
			moveUpEnabled &= selectionIf.moveUpDownEnabed(true);
			moveDownEnabled &= selectionIf.moveUpDownEnabed(false);
		}
		fMoveUpButton.setEnabled(moveUpEnabled);
		fMoveDownButton.setEnabled(moveDownEnabled);
	}
}
