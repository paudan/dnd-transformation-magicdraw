package org.ktu.dndtransformations.transforms;

import java.awt.Point;
import java.util.List;
import java.util.Set;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.impl.ElementProducer;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.dndtransformations.impl.MagicDrawRenderer;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.dndtransformations.impl.PropertyManager;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractMultipleTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.rendered.RenderedMultipleTransformation;

class MagicDrawMultipleTransformation extends AbstractMultipleTransformation<Element, Stereotype> {

    public MagicDrawMultipleTransformation() throws TransformationConfigurationException {
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
 * A factory class which performs transformation to multiple elements, which are not connected by any classifier
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 *
 */
@SuppressWarnings({"rawtypes"})
public class MultipleTransformationFactory extends RenderedMultipleTransformation<Element, Stereotype, PresentationElement> {

    private static MultipleTransformationFactory INSTANCE;

    private MultipleTransformationFactory(AbstractMultipleTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
    }

    private MultipleTransformationFactory() throws TransformationConfigurationException {
        super(new MagicDrawMultipleTransformation());
    }

    /** @return An instance of {@link MultipleTransformationFactory} */
    public static MultipleTransformationFactory getInstance() {
        if (INSTANCE == null)
            try {
                INSTANCE = new MultipleTransformationFactory();
            } catch (TransformationConfigurationException ex) {
                Logger.getLogger(MultipleTransformationFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        return INSTANCE;
    }

    @Override
    public List<PresentationElement> drawGeneratedItems(Set<Object> drawableItems, PresentationElement elementOver, Point location) {
        List<PresentationElement> layout = super.drawGeneratedItems(drawableItems, elementOver, location);
        elementOver.setSelected(layout);
        if (elementOver instanceof DiagramPresentationElement)
            ((DiagramPresentationElement) elementOver).layout(false);
        return layout;
    }

    @Override
    public ElementRenderer<Element, PresentationElement> getElementRenderer() {
        return MagicDrawRenderer.getInstance();
    }

}
