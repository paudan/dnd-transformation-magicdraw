package org.ktu.dndtransformations.parsers;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.StructuredClassifier;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.PatternParserFactory;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.parsers.PatternParser;

/**
 *
 * @author Admin
 */
public class MagicDrawPatternFactory implements PatternParserFactory<Connector, ConnectableElement, Element, Stereotype> {
    
    private static MagicDrawPatternFactory INSTANCE;
    
    private MagicDrawPatternFactory() {
        super();
    }
    
    public static MagicDrawPatternFactory getInstance() {
        if (INSTANCE == null)
            INSTANCE = new MagicDrawPatternFactory();
        return INSTANCE;
    }

    @Override
    public <Mapper extends ElementMapper<Element, ConnectableElement, Stereotype>> 
        PatternParser<Connector, ConnectableElement, Element, Stereotype> getParserInstance(Element rootPattern, 
                Element targetCl, Mapper mapper, Element elementOver) throws InvalidPatternException {
        if (rootPattern instanceof StructuredClassifier && mapper instanceof MagicDrawMapper)
            return new PatternParserImpl((StructuredClassifier)rootPattern, targetCl != null ? (Classifier)targetCl : null, (MagicDrawMapper) mapper, elementOver);
        return null;
    }

    @Override
    public <Mapper extends ElementMapper<Element, ConnectableElement, Stereotype>> 
        PatternParser<Connector, ConnectableElement, Element, Stereotype> getParserInstance(Element rootPattern, 
                Element targetCl, Mapper mapper) throws InvalidPatternException {
        if (rootPattern instanceof StructuredClassifier)
            return new PatternParserImpl((StructuredClassifier)rootPattern, targetCl != null ? (Classifier)targetCl : null, (MagicDrawMapper) mapper, null);
        return null;
    }

    
}
