package org.ktu.dndtransformations.transforms;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.core.options.ActivityLayouterOptionsGroup;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.layout.activity.ActivityDiagramLayouter;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.impl.ElementProducer;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.dndtransformations.impl.PropertyManager;
import org.ktu.dndtransformations.impl.MagicDrawRenderer;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractActivityTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.rendered.RenderedActivityTransformation;

class MagicDrawActivityTransformation extends AbstractActivityTransformation<Element, Stereotype> {

    public MagicDrawActivityTransformation() throws TransformationConfigurationException {
        super();
    }

    @Override
    public ElementMapper<Element, ConnectableElement, Stereotype> getElementMapper() {
        return MagicDrawMapper.getInstance();
    }

    @Override
    public AbstractPropertyManager<Element, Stereotype, TypedElement> getPropertyManager() {
        return PropertyManager.getInstance();
    }

    @Override
    public AbstractElementProducer<Element, Stereotype> getElementProducer() {
        return ElementProducer.getInstance();
    }

    @Override
    public ElementSearch<Element, Stereotype> getElementSearch() {
        return MagicDrawSearch.getInstance();
    }

}

/**
 * A factory class which performs transformation to Activity elements
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 *
 */
public class ActivityTransformationFactory extends RenderedActivityTransformation<Element, Stereotype, PresentationElement> {

    private static ActivityTransformationFactory INSTANCE;

    public ActivityTransformationFactory(AbstractActivityTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
    }

    public ActivityTransformationFactory() throws TransformationConfigurationException {
        super(new MagicDrawActivityTransformation());
    }

    /**
     * @return An instance of {@link ActivityTransformationFactory}
     */
    public static ActivityTransformationFactory getInstance() {
        if (INSTANCE == null)
            try {
                INSTANCE = new ActivityTransformationFactory();
            } catch (TransformationConfigurationException ex) {
                Logger.getLogger(ActivityTransformationFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        return INSTANCE;
    }

    @Override
    protected List<PresentationElement> drawContainedElements(Map<Object, 
            Map<String, Set<Element>>> drawableItems, PresentationElement diagram, Point location) {
        List<PresentationElement> layout = super.drawContainedElements(drawableItems, diagram, location);
        diagram.setSelected(layout);
        if (diagram instanceof DiagramPresentationElement) {
            ActivityLayouterOptionsGroup opt = new ActivityLayouterOptionsGroup();
            opt.setOrientation(ActivityLayouterOptionsGroup.LEFT_TO_RIGHT);
            opt.setMakePreferredLayoutSize(true);
            opt.setMoveToFreeSpace(true);
            ((DiagramPresentationElement) diagram).layout(false, new ActivityDiagramLayouter(), opt);
        }
        return layout;
    }

    @Override
    public ElementRenderer<Element, PresentationElement> getElementRenderer() {
        return MagicDrawRenderer.getInstance();
    }



}
