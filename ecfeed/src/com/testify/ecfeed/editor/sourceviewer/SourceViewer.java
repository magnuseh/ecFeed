package com.testify.ecfeed.editor.sourceviewer;

import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;

import com.testify.ecfeed.editor.ColorManager;
import com.testify.ecfeed.editor.EcMultiPageEditor;
import com.testify.ecfeed.editor.IModelUpdateListener;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.parsers.EcWriter;

public class SourceViewer extends TextEditor implements IModelUpdateListener{
	
	private ColorManager fColorManager;
	
	public class DocumentProvider extends FileDocumentProvider{
		@Override
		protected IDocument createDocument(Object element) throws CoreException{
			IDocument document = super.createDocument(element);
			if(document != null){
				
				IPartitionTokenScanner scanner = new XmlPartitionScanner();
				String[] legalContentTypes = new String[]
				{
						XmlPartitionScanner.XML_START_TAG,
						XmlPartitionScanner.XML_PI,
						XmlPartitionScanner.XML_END_TAG,
				};
				
				IDocumentPartitioner partitioner = new FastPartitioner(scanner , legalContentTypes);
				partitioner.connect(document);
				document.setDocumentPartitioner(partitioner);
			}
			return document;
		}
		
	}

	public SourceViewer(EcMultiPageEditor editor){
		super();
		fColorManager = new ColorManager();
		setSourceViewerConfiguration(new ViewerConfiguration(fColorManager));
		setDocumentProvider(new DocumentProvider());
		if(editor != null){
			editor.registerModelUpdateListener(this);
		}
	}
	
	public void dispose() {
		fColorManager.dispose();
		super.dispose();
	}
	
	public IDocument getDocument(){
		return getSourceViewer().getDocument();
	}

	@Override
	public boolean isEditable(){
		return false;
	}

	@Override
	public void modelUpdated(RootNode model) {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		EcWriter writer = new EcWriter(ostream);
		writer.writeXmlDocument(model);
		getDocument().set(ostream.toString());
	}
}
