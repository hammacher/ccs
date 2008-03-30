package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.StepByStepTraverseFrame;


public class StepByStepTraverseView extends ViewPart implements ISelectionListener, IPartListener {

    private PageBook myPages;

    private Composite defaultComp;

    private final Map<CCSEditor, StepByStepTraverseFrame> frames =
        new HashMap<CCSEditor, StepByStepTraverseFrame>();

    private final Set<CCSEditor> closedCCSEditors = new HashSet<CCSEditor>();

    @Override
    public void createPartControl(Composite parent) {

        myPages = new PageBook(parent, SWT.None);

        defaultComp = new Composite(myPages, SWT.NONE);
        defaultComp.setLayout(new GridLayout(1, true));

        final Label defaultLabel = new Label(defaultComp, SWT.None);
        defaultLabel.setText("No CCS file opened.");
        defaultLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        myPages.showPage(defaultComp);

        final IWorkbenchPartSite site = getSite();
        final IWorkbenchPage page = site == null ? null : site.getPage();
        if (page != null) {
            page.addSelectionListener(this);
            page.addPartListener(this);
        }

        final IEditorPart activeEditor = page.getActiveEditor();
        if (activeEditor != null)
            changeEditor(activeEditor);
    }

    @Override
    public void dispose() {
        final IWorkbenchPartSite site = getSite();
        final IWorkbenchPage page = site == null ? null : site.getPage();
        if (page != null)
            page.removeSelectionListener(this);
        super.dispose();
    }

    @Override
    public void setFocus() {
        myPages.setFocus();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof IEditorPart)
            changeEditor((IEditorPart) part);
    }

    public synchronized void changeEditor(IEditorPart activeEditor) {
        if (activeEditor instanceof CCSEditor) {
            final CCSEditor editor = (CCSEditor) activeEditor;

            StepByStepTraverseFrame stepByStepTraverseFrame = frames.get(editor);
            if (stepByStepTraverseFrame == null)
                frames.put(editor, stepByStepTraverseFrame = new StepByStepTraverseFrame(myPages, SWT.NONE, editor));

            myPages.showPage(stepByStepTraverseFrame);

            // and now, dispose all frames whose editor has been closed
            final Set<CCSEditor> toDispose = new HashSet<CCSEditor>(closedCCSEditors);
            toDispose.retainAll(frames.keySet());
            for (final CCSEditor closed: toDispose) {
                final StepByStepTraverseFrame frame = frames.get(closed);
                if (frame != null) {
                    frames.remove(closed);
                    closedCCSEditors.remove(closed);
                    frame.dispose();
                }
            }
        } else {
            myPages.showPage(defaultComp);
        }
    }

    public void partActivated(IWorkbenchPart part) {
        // ignore
    }

    public void partBroughtToTop(IWorkbenchPart part) {
        // ignore
    }

    public void partClosed(IWorkbenchPart part) {
        if (part instanceof CCSEditor) {
            closedCCSEditors.add((CCSEditor)part);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
        // ignore
    }

    public void partOpened(IWorkbenchPart part) {
        // ignore
    }

}
