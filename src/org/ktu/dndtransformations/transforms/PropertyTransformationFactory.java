package org.ktu.dndtransformations.transforms;

import com.nomagic.magicdraw.uml.symbols.PresentationElement;
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
import org.ktu.transformations.transforms.AbstractPropertyTransformation;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.rendered.RenderedPropertyTransformation;

class MagicDrawPropertyTransformation extends AbstractPropertyTransformation<Element, Stereotype> {

    public MagicDrawPropertyTransformation() throws TransformationConfigurationException {
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
 * A factory class which performs transformation to properties of particular element
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class PropertyTransformationFactory extends RenderedPropertyTransformation<Element, Stereotype, PresentationElement> {

    private static PropertyTransformationFactory INSTANCE;

    public PropertyTransformationFactory(AbstractPropertyTransformation<Element, Stereotype> transform) throws TransformationConfigurationException {
        super(transform);
    }

    public PropertyTransformationFactory() throws TransformationConfigurationException {
        super(new MagicDrawPropertyTransformation());
    }

    /**
     * @return An instance of {@link PropertyTransformationFactory}
     */
    public static PropertyTransformationFactory getInstance() {
        if (INSTANCE == null)
            try {
                INSTANCE = new PropertyTransformationFactory();
            } catch (TransformationConfigurationException ex) {
                Logger.getLogger(PropertyTransformationFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        return INSTANCE;
    }

    @Override
    public ElementRenderer<Element, PresentationElement> getElementRenderer() {
        return MagicDrawRenderer.getInstance();
    }

}
