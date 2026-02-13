package org.ktu.dndtransformations.transforms;

import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.impl.ElementProducer;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.dndtransformations.impl.PropertyManager;
import org.ktu.dndtransformations.impl.MagicDrawRenderer;
import org.ktu.dndtransformations.parsers.MDElementMapping;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.AbstractContainerTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.rendered.RenderedContainerTransformation;

class MagicDrawContainerTransformation extends AbstractContainerTransformation<Element, Stereotype> {

    public MagicDrawContainerTransformation() throws TransformationConfigurationException {
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
    public ElementProducer getElementProducer() {
        return ElementProducer.getInstance();
    }

    @Override
    public ElementSearch<Element, Stereotype> getElementSearch() {
        return MagicDrawSearch.getInstance();
    }

    @Override
    public Set<Object> createElements(SpecificationReader specReader, 
            PatternParser<?, ?, Element, Stereotype> parser, ConnectableEntity targetCl, 
            Element targetPackage, Element dragged, Object elementOver, Collection<NotificationObserver> observers) 
            throws ElementGenerationException, InvalidPatternException {
        Set<Object> elements = super.createElements(specReader, parser, targetCl, targetPackage, dragged, elementOver, observers);
        ElementProducer producer = getElementProducer();
        for (Element el: integrations.keySet()) {
            IntegrationInfo<Element> intInfo = integrations.get(el);
            if (specReader instanceof MDSpecificationReader && intInfo.getMapping() instanceof MDElementMapping) {
                EnumerationLiteral literal = producer.getIntegrationType((MDSpecificationReader)specReader, 
                        (MDElementMapping)intInfo.getMapping(), intInfo.getTarget());
                producer.createIntegration(intInfo.getClient(), el, literal);
            }
        }
        return elements;
    }
        
}

/**
 * A factory class which performs transformation to container-type tuple of Elements (i.e., child elements are viewed as elements inside of a parent element)
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
@SuppressWarnings({"rawtypes"})
public class ContainerTransformationFactory extends RenderedContainerTransformation<Element, Stereotype, PresentationElement> {

    private static ContainerTransformationFactory INSTANCE;
    
    private ContainerTransformationFactory(AbstractContainerTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
    }
    
    private ContainerTransformationFactory() throws TransformationConfigurationException {
        super(new MagicDrawContainerTransformation());
    }
    
    /**
     * @return An instance of {@link ContainerTransformationFactory}
     */
    public static ContainerTransformationFactory getInstance() {
        if (INSTANCE == null)
            try {
                INSTANCE = new ContainerTransformationFactory();
            } catch (TransformationConfigurationException ex) {
                Logger.getLogger(ContainerTransformationFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        return INSTANCE;
    }

    @Override
    public ElementRenderer<Element, PresentationElement> getElementRenderer() {
        return MagicDrawRenderer.getInstance();
    }
    
    @Override
    protected List<PresentationElement> drawContainedElements(Map<Object, Map<String, 
            Set<Element>>> drawableItems, PresentationElement elementOver, Point location) {
        List<PresentationElement> layout = super.drawContainedElements(drawableItems, elementOver, location);
        elementOver.setSelected(layout);
        if (elementOver instanceof DiagramPresentationElement && !layout.isEmpty())
            ((DiagramPresentationElement) elementOver).layout(false);
        return layout;
    }

}
