/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Elias Volanakis - initial API and implementation
?*******************************************************************************/
package org.eclipse.gef.examples.shapes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.examples.shapes.model.Connection;
import org.eclipse.gef.examples.shapes.model.EllipticalShape;
import org.eclipse.gef.examples.shapes.model.RectangularShape;
import org.eclipse.gef.examples.shapes.model.Shape;
import org.eclipse.gef.examples.shapes.model.ShapesDiagram;
import org.eclipse.gef.examples.shapes.model.TriangularShape;
import org.eclipse.gef.examples.shapes.parts.ShapesEditPartFactory;
import org.eclipse.gef.examples.shapes.parts.ShapesTreeEditPartFactory;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * A graphical editor with flyout palette that can edit .shapes files. The
 * binding between the .shapes file extension and this editor is done in
 * plugin.xml
 * 
 * @author Elias Volanakis
 */
public class ShapesEditor extends GraphicalEditorWithFlyoutPalette {

	/** This is the root of the editor's model. */
	private ShapesDiagram diagram;
	/** Palette component, holding the tools and shapes. */
	private static PaletteRoot PALETTE_MODEL;

	/** Create a new ShapesEditor instance. This is called by the Workspace. */
	public ShapesEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	/**
	 * Configure the graphical viewer before it receives contents.
	 * <p>
	 * This is the place to choose an appropriate RootEditPart and
	 * EditPartFactory for your editor. The RootEditPart determines the behavior
	 * of the editor's "work-area". For example, GEF includes zoomable and
	 * scrollable root edit parts. The EditPartFactory maps model elements to
	 * edit parts (controllers).
	 * </p>
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ShapesEditPartFactory());
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));

		// configure the context menu provider
		ContextMenuProvider cmProvider = new ShapesEditorContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu(cmProvider, viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#commandStackChanged(java.util
	 * .EventObject)
	 */
	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	private void createOutputStream(OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(getModel());
		oos.close();
	}

	private void createRawOutput(ByteArrayOutputStream out) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		Iterator it = getModel().getChildren()
				.iterator(), conit;
		HashSet<Connection> hs = new HashSet<Connection>();
		sb.append("Shapes:\n"
				+ getModel().getChildren()
						.size() + "\n");// 先输出shape个数
		while (it.hasNext()) {
			Shape sp = (Shape) it.next();
			sb.append(sp.toString() + "\n");
			sb.append("height:\n"
					+ sp.getSize().height
					+ "\nwidth:\n"
					+ sp.getSize().width + "\n");
			sb.append("location:\n"
					+ sp.getLocation().x + " "
					+ sp.getLocation().y + "\n");
			conit = sp.getSourceConnections()
					.iterator();
			while (conit.hasNext()) {
				hs.add((Connection) conit.next());
			}
			conit = sp.getTargetConnections()
					.iterator();
			while (conit.hasNext()) {
				hs.add((Connection) conit.next());
			}
			sb.append("\n");// 每个图形之间间隔一行
		}

		sb.append("Connections:\n" + hs.size()
				+ "\n");
		it = hs.iterator();
		while (it.hasNext()) {
			Connection con = (Connection) it
					.next();
			sb.append(con.getSource()
					.toString() + "\n");
			sb.append(con.getTarget()
					.toString() + "\n");
			sb.append("\n");// 每个连接之间间隔一行
		}
		out.write(sb.toString().getBytes());
		out.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * createPaletteViewerProvider()
	 */
	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			@Override
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				// create a drag source listener for this palette viewer
				// together with an appropriate transfer drop target listener,
				// this will enable
				// model element creation by dragging a
				// CombinatedTemplateCreationEntries
				// from the palette into the editor
				// @see ShapesEditor#createTransferDropTargetListener()
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(
						viewer));
			}
		};
	}

	/**
	 * Create a transfer drop target listener. When using a
	 * CombinedTemplateCreationEntry tool in the palette, this will enable model
	 * element creation by dragging from the palette.
	 * 
	 * @see #createPaletteViewerProvider()
	 */
	private TransferDropTargetListener createTransferDropTargetListener() {
		return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
			@Override
			protected CreationFactory getFactory(Object template) {
				return new SimpleFactory((Class) template);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor
	 * )
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			if(!file.getName().contains(".raw.obsp")){
				createOutputStream(out);
			}else{
				createRawOutput(out);
			}
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true, // keep
																				// saving,
																				// even
																				// if
																				// IFile
																				// is
																				// out
																				// of
																				// sync
																				// with
																				// the
																				// Workspace
					false, // dont keep history
					monitor); // progress monitor
			getCommandStack().markSaveLocation();
		} catch (CoreException ce) {
			ce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// Show a SaveAs dialog
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();

		IPath path = dialog.getResult();
		if (path != null) {
			// try to save the editor's contents under a different file name
			final IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(path);
			try {
				new ProgressMonitorDialog(shell).run(false, // don't fork
						false, // not cancelable
						new WorkspaceModifyOperation() { // run this operation
							@Override
							public void execute(final IProgressMonitor monitor) {
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								try {
									if (!file.getName().contains(".raw.obsp")) {
										createOutputStream(out);
									} else {
										createRawOutput(out);
									}
									file.create(
											new ByteArrayInputStream(out
													.toByteArray()), // contents
											true, // keep saving, even if
													// IFile
													// is out of sync with
													// the
													// Workspace
											monitor); // progress monitor
								} catch (CoreException ce) {
									ce.printStackTrace();
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							}
						});
				// set input to the new file
				setInput(new FileEditorInput(file));
				getCommandStack().markSaveLocation();
			} catch (InterruptedException ie) {
				// should not happen, since the monitor dialog is not cancelable
				ie.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}
	}

	@Override
	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class)
			return new ShapesOutlinePage(new TreeViewer());
		return super.getAdapter(type);
	}

	ShapesDiagram getModel() {
		return diagram;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot
	 * ()
	 */
	@Override
	protected PaletteRoot getPaletteRoot() {
		if (PALETTE_MODEL == null)
			PALETTE_MODEL = ShapesEditorPaletteFactory.createPalette();
		return PALETTE_MODEL;
	}

	private void handleLoadException(Exception e) {
		System.err.println("** Load failed. Using default model. **");
		e.printStackTrace();
		diagram = new ShapesDiagram();
	}

	/**
	 * Set up the editor's inital content (after creation).
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(getModel()); // set the contents of this editor

		// listen for dropped parts
		viewer.addDropTargetListener(createTransferDropTargetListener());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		try {
			IFile file = ((IFileEditorInput) input).getFile();
		if(!file.getName().contains(".raw.obsp")){//XXX.raw.obsp
			ObjectInputStream in = new ObjectInputStream(file.getContents());
			diagram = (ShapesDiagram) in.readObject();
			in.close();
		}else{
			diagram = new ShapesDiagram();
			
            InputStreamReader isr = new InputStreamReader(file.getContents());
            BufferedReader reader = new BufferedReader(isr);
            String tempString, name;
            ArrayList<String> shapename = new ArrayList<String>();
            int shapenum, connnum;
            reader.readLine();//"Shape:"
            tempString = reader.readLine();//shape number
            try{
            	shapenum = Integer.parseInt(tempString);
            }catch(NumberFormatException ex){
            	shapenum = 0;
            }
            for(int i = 0;i < shapenum;i++){
            	int height, width, x, y;
                name = reader.readLine();//shape name
                shapename.add(name);
                reader.readLine();//"height:"
                tempString = reader.readLine();//shape height
                try{
                	height = Integer.parseInt(tempString);
                }catch(NumberFormatException ex){
                	height = 0;
                }
                reader.readLine();//"width:"
                tempString = reader.readLine();//shape width
                try{
                	width = Integer.parseInt(tempString);
                }catch(NumberFormatException ex){
                	width = 0;
                }
                reader.readLine();//"location:"
                tempString = reader.readLine();//shape location
                try{
                	x = Integer.parseInt(tempString.split(" ")[0]);
                }catch(NumberFormatException ex){
                	x = 0;
                }
                try{
                	y = Integer.parseInt(tempString.split(" ")[1]);
                }catch(NumberFormatException ex){
                	y = 0;
                }
                reader.readLine();//empty line
                //add to the diagram
                Shape sp;
                if(name.contains("Ellipse")){
                	sp = new EllipticalShape();
                }else if(name.contains("Triangle")){
                	sp = new TriangularShape();
                }else if(name.contains("Rectangle")){
                	sp = new RectangularShape();
                }else{
                	System.err.println("Shape unsupported!");
                	sp = new EllipticalShape();
                }
                sp.setSize(new Dimension(width, height));
                sp.setLocation(new Point(x, y));
                diagram.addChild(sp);
            }

            reader.readLine();//"Connections:"
            tempString = reader.readLine();//connection num
            try{
            	connnum = Integer.parseInt(tempString);
            }catch(NumberFormatException ex){
            	connnum = 0;
            }
            
            for(int i = 0;i < connnum;i++)
            {
            	String source = reader.readLine();
            	String target = reader.readLine();
            	reader.readLine();//empty line
            	int sourceindex = shapename.indexOf(source);
            	int targetindex = shapename.indexOf(target);
            	if(sourceindex == -1 || targetindex == -1)
            	{
            		System.err.println("Cannot find this shape!");
            		continue;
            	}
            	Shape sourceshape = (Shape) diagram.getChildren().get(sourceindex);
            	Shape targetshape = (Shape) diagram.getChildren().get(targetindex);
            	Connection conn = new Connection(sourceshape, targetshape);
            }
            
            reader.close();
		}
			setPartName(file.getName());
		} catch (CoreException e) {
			handleLoadException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates an outline pagebook for this editor.
	 */
	public class ShapesOutlinePage extends ContentOutlinePage {
		/**
		 * Create a new outline page for the shapes editor.
		 * 
		 * @param viewer
		 *            a viewer (TreeViewer instance) used for this outline page
		 * @throws IllegalArgumentException
		 *             if editor is null
		 */
		public ShapesOutlinePage(EditPartViewer viewer) {
			super(viewer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite
		 * )
		 */
		@Override
		public void createControl(Composite parent) {
			// create outline viewer page
			getViewer().createControl(parent);
			// configure outline viewer
			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new ShapesTreeEditPartFactory());
			// configure & add context menu to viewer
			ContextMenuProvider cmProvider = new ShapesEditorContextMenuProvider(
					getViewer(), getActionRegistry());
			getViewer().setContextMenu(cmProvider);
			getSite().registerContextMenu(
					"org.eclipse.gef.examples.shapes.outline.contextmenu",
					cmProvider, getSite().getSelectionProvider());
			// hook outline viewer
			getSelectionSynchronizer().addViewer(getViewer());
			// initialize outline viewer with model
			getViewer().setContents(getModel());
			// show outline viewer
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.part.IPage#dispose()
		 */
		@Override
		public void dispose() {
			// unhook outline viewer
			getSelectionSynchronizer().removeViewer(getViewer());
			// dispose
			super.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.part.IPage#getControl()
		 */
		@Override
		public Control getControl() {
			return getViewer().getControl();
		}

		/**
		 * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
		 */
		@Override
		public void init(IPageSite pageSite) {
			super.init(pageSite);
			ActionRegistry registry = getActionRegistry();
			IActionBars bars = pageSite.getActionBars();
			String id = ActionFactory.UNDO.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = ActionFactory.REDO.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = ActionFactory.DELETE.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
		}
	}

}