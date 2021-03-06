package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class CCSSourceViewerConfiguration extends SourceViewerConfiguration {

    private static final String[] CONTENT_TYPES = { IDocument.DEFAULT_CONTENT_TYPE };

    private final ColorManager colorManager;

    private final CCSEditor editor;

    public CCSSourceViewerConfiguration(ColorManager colorManager, CCSEditor editor) {
        this.colorManager = colorManager;
        this.editor = editor;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return CONTENT_TYPES;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        return new CCSPresentationReconciler(colorManager, editor);
    }

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer,
            String contentType) {
        return new CCSTextHover();
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new CCSAnnotationHover();
    }

}