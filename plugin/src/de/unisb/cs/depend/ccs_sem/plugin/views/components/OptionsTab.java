package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.awt.Dimension;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import de.unisb.cs.depend.ccs_sem.plugin.MyPreferenceStore;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.plugin.utils.ISemanticDependend;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;

public class OptionsTab extends CTabItem implements ISemanticDependend {

    protected static final double ZOOM_FACTOR = 1.25;
    protected GrappaFrame gFrame;
    protected Button buttonZoomIn;
    protected Button buttonZoomOut;
    protected Button buttonSemanticSwitch;

    protected boolean scaleToFit = true;
    
    private IPropertyChangeListener propListener;

    public OptionsTab(CTabFolder parent, int style, GrappaFrame grappaFrame, final CCSFrame ccsFrame) {
        super(parent, style);

        this.gFrame = grappaFrame;

        setText("Options");
        final ScrolledComposite optionsScrollComp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        optionsScrollComp.setExpandHorizontal(true);
        optionsScrollComp.setExpandVertical(true);
        final Composite optionsComp = new Composite(optionsScrollComp, SWT.NONE);
        optionsScrollComp.setContent(optionsComp);
        setControl(optionsScrollComp);

        optionsComp.setLayout(new GridLayout(1, true));

        final Group scalingGroup = new Group(optionsComp, SWT.NONE);
        scalingGroup.setText("Scaling");
        scalingGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        scalingGroup.setLayout(new GridLayout(2, true));

        final Button buttonScaleToFit = new Button(scalingGroup, SWT.CHECK);
        buttonScaleToFit.setSelection(true);
        buttonScaleToFit.setText("Scale to fit");
        final GridData buttonScaleToFitGridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        buttonScaleToFitGridData.horizontalSpan = 2;
        buttonScaleToFit.setLayoutData(buttonScaleToFitGridData);

        buttonZoomIn = new Button(scalingGroup, SWT.PUSH);
        buttonZoomIn.setEnabled(false);
        buttonZoomIn.setText("Zoom in");
        buttonZoomIn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        buttonZoomOut = new Button(scalingGroup, SWT.PUSH);
        buttonZoomOut.setEnabled(false);
        buttonZoomOut.setText("Zoom out");
        buttonZoomOut.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        final Button buttonMinimize = new Button(optionsComp, SWT.CHECK);
        buttonMinimize.setSelection(false);
        buttonMinimize.setText("Minimize LTS");
        buttonMinimize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        buttonSemanticSwitch = new Button(optionsComp, SWT.CHECK);
        buttonSemanticSwitch.setSelection(Expression.getVisibleTau());
        buttonSemanticSwitch.setText("Interaction visible");
        buttonMinimize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        final Group layoutGroup = new Group(optionsComp, SWT.NONE);
        layoutGroup.setText("Layout");
        layoutGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final FillLayout layoutGroupLayout = new FillLayout(SWT.VERTICAL);
        layoutGroupLayout.marginHeight = layoutGroupLayout.marginWidth = 5;
        layoutGroupLayout.spacing = 3;
        layoutGroup.setLayout(layoutGroupLayout);

        final Button buttonShowNodeLabels = new Button(layoutGroup, SWT.CHECK);
        buttonShowNodeLabels.setSelection(true);
        buttonShowNodeLabels.setText("Show node labels");

        final Button buttonShowEdgeLabels = new Button(layoutGroup, SWT.CHECK);
        buttonShowEdgeLabels.setSelection(true);
        buttonShowEdgeLabels.setText("Show edge labels");

        final Button buttonLayoutTopToBottom = new Button(layoutGroup, SWT.RADIO);
        buttonLayoutTopToBottom.setSelection(false);
        buttonLayoutTopToBottom.setText("Layout top to bottom");

        final Button buttonLayoutLeftToRight = new Button(layoutGroup, SWT.RADIO);
        buttonLayoutLeftToRight.setSelection(true);
        buttonLayoutLeftToRight.setText("Layout left to right");

        optionsScrollComp.setMinSize(optionsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        buttonScaleToFit.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                scaleToFit = buttonScaleToFit.getSelection();
                gFrame.setScaleToFit(scaleToFit);
                updateZoomButtonStates();
            }

        });
        buttonZoomIn.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.zoom(ZOOM_FACTOR);
                updateZoomButtonStates();
            }

        });
        buttonZoomOut.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.zoom(1.0/ZOOM_FACTOR);
                updateZoomButtonStates();
            }

        });

        buttonShowNodeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.setShowNodes(buttonShowNodeLabels.getSelection(), true);
            }

        });
        buttonShowEdgeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.setShowEdges(buttonShowEdgeLabels.getSelection(), true);
            }

        });

        buttonMinimize.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                ccsFrame.setMinimize(buttonMinimize.getSelection(), true);
            }

        });
        
        buttonSemanticSwitch.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				Expression.setVisibleTau(buttonSemanticSwitch.getSelection());
				MyPreferenceStore.setVisibleTauSemantic(
						buttonSemanticSwitch.getSelection());
				ccsFrame.updateEvaluation(true);
			}
        });
        
        buttonLayoutTopToBottom.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (!buttonLayoutTopToBottom.getSelection())
                    return;
                gFrame.setLayoutLeftToRight(!buttonLayoutTopToBottom.getSelection(), true);
            }

        });
        buttonLayoutLeftToRight.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (!buttonLayoutLeftToRight.getSelection())
                    return;
                gFrame.setLayoutLeftToRight(buttonLayoutLeftToRight.getSelection(), true);
            }

        });
    }

    protected void updateZoomButtonStates() {
        final Dimension size = gFrame.getGrappaPanelSize();
        final double nextHeight = size.height * ZOOM_FACTOR * ZOOM_FACTOR;
        final double nextWidth = size.width * ZOOM_FACTOR * ZOOM_FACTOR;
        final boolean allowZoomIn = nextHeight * nextWidth < 10000000 // 1e7
            && nextHeight <= 32768
            && nextWidth <= 32768;
        buttonZoomIn.setEnabled(allowZoomIn && !scaleToFit);
        buttonZoomOut.setEnabled(!scaleToFit);
    }

    public void update(EvaluationStatus evalStatus) {
        // we don't have to update
    }
    
    @Override
    public void dispose() {
    	super.dispose();
    	MyPreferenceStore.getStore().removePropertyChangeListener(propListener);
    }

	public void updateSemantic() {
		if( isDisposed() )
			return;
		
		buttonSemanticSwitch.setSelection(
				MyPreferenceStore.getVisibleTauSemantic()
				);
	}
}
