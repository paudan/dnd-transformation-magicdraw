package org.ktu.dndtransformations.transforms;

import org.ktu.transformations.transforms.rendered.RenderedSingleTransformation;
import org.ktu.transformations.transforms.AbstractSingleTransformation;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.Collection;
import java.util.Set;
import org.ktu.dndtransformations.impl.ElementProducer;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.dndtransformations.impl.PropertyManager;
import org.ktu.dndtransformations.impl.MagicDrawRenderer;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.renderers.ElementRenderer;

class MagicDrawSingleTransformation extends AbstractSingleTransformation<Element, Stereotype> {

    @Override
    public ElementMapper<Element, ConnectableElement, Stereotype> getElementMapper() {
        return MagicDrawMapper.getInstance();
    }

    @Override
    public AbstractPropertyManager<Element, Stereotype, TypedElement> getPropertyManager() {
        return PropertyManager.getInstance();
    }

    @Override
    public ElementProducer getElementProducer() {
        return ElementProducer.getInstance();
    }

    @Override
    public ElementSearch<Element, Stereotype> getElementSearch() {
        return MagicDrawSearch.getInstance();
    }
    
    @Override
    public Set<Object> createSingleElement(SpecificationReader specReader, Element targetCl, 
            Element targetPackage, Element selected, Collection<NotificationObserver> observers) 
            throws ElementGenerationException {
        Set<Object> objects = super.createSingleElement(specReader, targetCl, targetPackage, selected, observers);
        if (specReader instanceof MDSpecificationReader)
            for (Object newel: objects) {
                Object type = specReader.getIntegrationType();
                if (type != null)
                    getElementProducer().createIntegration((Element) newel, getSelectedElement(), (EnumerationLiteral)type);
            }
        return objects;
    }
        
}
/**
 * Generates single target Element, corresponding to the source Element
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 */
@SuppressWarnings("unchecked")
public class SingleTransformationFactory extends RenderedSingleTransformation<Element, Stereotype, PresentationElement> {

    private static SingleTransformationFactory INSTANCE;

    /** @return An instance of {@link SingleTransformationFactory} */
    public static SingleTransformationFactory getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SingleTransformationFactory();
        return INSTANCE;
    }

    public SingleTransformationFactory() {
        super(new MagicDrawSingleTransformation());
    }

    public SingleTransformationFactory(AbstractSingleTransformation<Element, Stereotype> transform) {
        super(transform);
    }

    @Override
    public ElementRenderer<Element, PresentationElement> getElementRenderer() {
        return MagicDrawRenderer.getInstance();
    }

}
