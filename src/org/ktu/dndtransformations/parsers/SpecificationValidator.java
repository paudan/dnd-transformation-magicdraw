package org.ktu.dndtransformations.parsers;

import org.ktu.transformations.parsers.ConcatMap;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.InvalidPatternException;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.StructuredClassifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.ktu.dndtransformations.impl.MagicDrawConfiguration;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.parsers.ConcatMap.ConcatErrorType;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ConnectorEntity;
import org.ktu.transformations.parsers.PatternConfiguration;

/**
 * Performs validation of transformation specification and transformation pattern
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), 
 * Center of Information Systems Design Technologies, Kaunas University of Technology, 2015
 */
public class SpecificationValidator {
    
    private PatternParserImpl parser;
    private MDSpecificationReader reader;
    private Map<String, NotificationType> errorMessages;
    private boolean validatePattern;
    private ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/dndtransformations/parsers/messages");

    public SpecificationValidator(MDSpecificationReader reader, boolean validatePattern) {
        this.reader = reader;
        this.validatePattern = validatePattern;
        errorMessages = new HashMap<>();
    }
    
    public void validate() {
        validateDnDSpecification();
        validateCustomization();
        if (reader.getTransformationPattern() != null && validatePattern)
            validatePattern();
    }

    public Map<String, NotificationType> getErrorMessages() {
        return errorMessages;
    }
    
    private void validateDnDSpecification() {
        if (reader.getSourceClassifier() == null)
            errorMessages.put(String.format(bundle.getString("SpecificationValidator.1"), NotificationType.ERROR.getName(), reader.getSpecificationName()), 
                    NotificationType.ERROR);
        if (reader.getTargetDiagrams().isEmpty())
            errorMessages.put(String.format(bundle.getString("SpecificationValidator.2"), NotificationType.ERROR.getName(), reader.getSpecificationName()), 
                    NotificationType.ERROR);
        if (reader.getRepresentationText() == null)
            errorMessages.put(String.format(bundle.getString("SpecificationValidator.3"), NotificationType.ERROR.getName(), reader.getSpecificationName()), 
                    NotificationType.ERROR);
    }
    
    private void validateCustomization() {
        if (reader.getAllowedTransformationList().isEmpty())
            errorMessages.put(String.format(bundle.getString("SpecificationValidator.4"), 
                    NotificationType.ERROR.getName(), reader.getSpecificationName()), NotificationType.ERROR);
        if (reader.getTargetClassifier() == null && reader.getTransformationPattern() == null)
            errorMessages.put(String.format(bundle.getString("SpecificationValidator.5"), 
                    NotificationType.ERROR.getName(), reader.getCustomizationName()), NotificationType.ERROR);
    }
    
    private void validatePattern() {
        StructuredClassifier pattern = reader.getTransformationPattern();
        if (pattern == null)
            return;
        PatternConfiguration config = MagicDrawConfiguration.getPatternConfiguration();
        try {
            MagicDrawMapper mapper = MagicDrawMapper.getInstance();
            parser = new PatternParserImpl(pattern, reader.getTargetClassifier(), mapper, null, false);
            
            // Check pattern for redundant elements in the root pattern element
            int numSources = 0, numTargets = 0;
            Collection<Property> attributeList = reader.getTransformationPattern().getPart();
            for (Property prop : attributeList) {
                if (StereotypesHelper.hasStereotype(prop.getType(), config.getSourceStereotypeName()))
                    numSources++;
                else if (StereotypesHelper.hasStereotype(prop.getType(), config.getTargetStereotypeName())) 
                    numTargets++;
                else if (!(StereotypesHelper.hasStereotype(prop.getType(), config.getJoinStereotypeName()))) {
                    String name = mapper.getElementName3(prop);
                    errorMessages.put(String.format(bundle.getString("SpecificationValidator.17") + ". " + 
                        bundle.getString("SpecificationValidator.11"), NotificationType.WARNING.getName(), 
                        pattern.getHumanName(), name != null ? name : "<unnamed>"), NotificationType.WARNING);
                }
            }
            if (numSources > 1)
                errorMessages.put(String.format(bundle.getString("SpecificationValidator.18"), 
                        NotificationType.ERROR.getName(), pattern.getHumanName()), NotificationType.ERROR);
            if (numTargets > 1)
                errorMessages.put(String.format(bundle.getString("SpecificationValidator.19"), 
                        NotificationType.ERROR.getName(), pattern.getHumanName()), NotificationType.ERROR);
            
            Element source = parser.getSourceElement();
            Element target = parser.getTargetElement();
            if (source == null)
                errorMessages.put(String.format(bundle.getString("PatternParser.2"), pattern.getHumanName()), NotificationType.ERROR);
            else if (!(source instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class))
                errorMessages.put(String.format(bundle.getString("PatternParser.7"), pattern.getHumanName()), NotificationType.ERROR);
            if (target == null)
                errorMessages.put(String.format(bundle.getString("PatternParser.3"), pattern.getHumanName()), NotificationType.ERROR);
            else if (!(target instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class))
                errorMessages.put(String.format(bundle.getString("PatternParser.8"), pattern.getHumanName()), NotificationType.ERROR);
            
            if (source != null && !parser.allValidConnections(source))
                errorMessages.put(String.format(bundle.getString("PatternParser.4"), pattern.getHumanName()) + 
                        bundle.getString("PatternParser.5"), NotificationType.ERROR);
            if (target != null && !parser.allValidConnections(target))
                errorMessages.put(String.format(bundle.getString("PatternParser.6"), pattern.getHumanName()) + 
                        bundle.getString("PatternParser.5"), NotificationType.ERROR);
            
            Map<ConnectableEntity, ElementMapping> sources = parser.getSourceMappings();
            Map<ConnectableEntity, ElementMapping> targets = parser.getTargetMappings();
            
            checkUnconnectedElements(source, sources.keySet(), bundle.getString("SpecificationValidator.8"));
            checkUnconnectedElements(target, targets.keySet(), bundle.getString("SpecificationValidator.9"));
            
            Set<ConnectableEntity> unmapped = getUnmappedElements();
            boolean mapsTo = false;
            for (ConnectableEntity el: sources.keySet()) {

                if (el.getType() == null) {
                    String name = el.getName3();
                    errorMessages.put(String.format(bundle.getString("SpecificationValidator.15"), 
                            NotificationType.ERROR.getName(), pattern.getHumanName(), name != null ? name : "<unnamed>"), NotificationType.ERROR);
                }
                if (!mapsTo && mapper.mapsToElement(reader.getSourceClassifier(), el) && !sources.get(el).targetList.isEmpty()) 
                    mapsTo = true;
            }
            if (!mapsTo)
                errorMessages.put(String.format(bundle.getString("SpecificationValidator.7"), 
                        NotificationType.ERROR.getName(), pattern.getHumanName(), reader.getSpecificationName()), NotificationType.ERROR);
            boolean mapsToUnmapped  = false, mapsToMapped = false;
            Classifier targetCl = reader.getTargetClassifier();
            for (ConnectableEntity el: targets.keySet()) {
                if (targets.get(el).source == null && parser.getHangingRepresentingTarget(null) == null 
                        && !unmapped.contains(el) && !isInConcat(el, target)) 
                    errorMessages.put(String.format(bundle.getString("SpecificationValidator.9"), 
                            NotificationType.WARNING.getName(), pattern.getHumanName(), el.getPrintableName()), NotificationType.WARNING);
                if (el.getType() == null) {
                    String name = el.getName3();
                    errorMessages.put(String.format(bundle.getString("SpecificationValidator.16"), 
                            NotificationType.ERROR.getName(), pattern.getHumanName(), name != null ? name : "<unnamed>"), NotificationType.ERROR);
                }
                if (targetCl != null && mapper.mapsToElement(targetCl, el)) {
                    if (targets.get(el).source == null)
                        mapsToUnmapped = true;
                    else
                        mapsToMapped = true;
                }
            }
            if (targetCl != null && mapsToMapped)
                errorMessages.put(String.format(bundle.getString("SpecificationValidator.6"), 
                        NotificationType.WARNING.getName(), reader.getCustomizationName()), NotificationType.WARNING);
            else if (targetCl != null && !mapsToUnmapped)
                errorMessages.put(String.format(bundle.getString("SpecificationValidator.10"), 
                        NotificationType.ERROR.getName(), pattern.getHumanName(), reader.getSpecificationName()), NotificationType.ERROR);
            
            checkInvalidElements(pattern, bundle.getString("SpecificationValidator.14"));
            if (source != null)
                checkInvalidElements(source, bundle.getString("SpecificationValidator.12"));
            if (target != null)
                checkInvalidElements(target, bundle.getString("SpecificationValidator.13"));
            
            // Check for DraggedElement misusages
            Map<Type, ArrayDeque<ConnectableEntity>> types = new HashMap<>();
            for (ConnectableEntity key: sources.keySet()) {
                ArrayDeque<ConnectableEntity> set = types.get((Type)key.getType());
                if (set == null) {
                    set = new ArrayDeque<>();
                    types.put((Type) key.getType(), set);
                }
                set.add(key);
            }
            for (Type type: types.keySet()) {
                ArrayDeque<ConnectableEntity> typeElements = types.get(type);
                if (typeElements.size() > 1 && mapper.mapsToElement(reader.getSourceClassifier(), typeElements.peekFirst())) {
                    boolean draggedSet = false;
                    for (ConnectableEntity elem: typeElements)
                        if (sources.get(elem).mapsToDragged)
                            draggedSet = true;
                    if (!draggedSet)
                        errorMessages.put(String.format(bundle.getString("SpecificationValidator.20"), NotificationType.WARNING.getName(), 
                            pattern.getHumanName(), type.getHumanName()), NotificationType.WARNING);
                }
            }
            
            //Validate CONCAT mappings
            for (ConnectableEntity key: targets.keySet()) {
                ConcatMap concats = targets.get(key).concatMap;
                for (ConnectorEntity tgt: concats.keySet())
                    if (concats.getIncomingConnectors(tgt).isEmpty())
                        errorMessages.put(String.format(bundle.getString("SpecificationValidator.21"), NotificationType.ERROR.getName(), 
                            pattern.getHumanName(), concats.getTargetPropertyStack(tgt).metaElement().getPrintableName()), NotificationType.ERROR);
                    else {
                        Map<Object, ConcatErrorType> errors = concats.validateMapping(tgt);
                        for (Object src: errors.keySet()) {
                            String tgtName = concats.getTargetPropertyStack(tgt).metaElement().getPrintableName();
                            if (errors.get(src) == ConcatErrorType.MISSING_CONCAT_RULE) {
                                errorMessages.put(String.format(bundle.getString("SpecificationValidator.22"), NotificationType.ERROR.getName(), 
                                    pattern.getHumanName(), tgtName), NotificationType.ERROR);
                            } else if (errors.get(src) == ConcatErrorType.MISSING_VARIABLE) {
                                String name = src instanceof PropertyStack ? ((PropertyStack)src).metaElement().getPrintableName() : src.toString();
                                errorMessages.put(String.format(bundle.getString("SpecificationValidator.23"), NotificationType.ERROR.getName(), 
                                    pattern.getHumanName(), name, tgtName), NotificationType.ERROR);
                            } else if (errors.get(src) == ConcatErrorType.INVALID_VARIABLE) {
                                String name = src instanceof PropertyStack ? ((PropertyStack)src).metaElement().getPrintableName() : src.toString();
                                errorMessages.put(String.format(bundle.getString("SpecificationValidator.24"), NotificationType.ERROR.getName(), 
                                    pattern.getHumanName(), name, tgtName), NotificationType.ERROR);
                            } else if (errors.get(src) == ConcatErrorType.DUPLICATE_VARIABLE) {
                                errorMessages.put(String.format(bundle.getString("SpecificationValidator.25"), NotificationType.ERROR.getName(), 
                                    pattern.getHumanName(), src.toString(), tgtName), NotificationType.ERROR);
                            } else if (errors.get(src) == ConcatErrorType.INVALID_CONCAT_RULE) {
                                errorMessages.put(String.format(bundle.getString("SpecificationValidator.26"), NotificationType.ERROR.getName(), 
                                    pattern.getHumanName(), tgtName), NotificationType.ERROR);
                            }
                        }         
                    }    
            }
        } catch (InvalidPatternException ex) {
            errorMessages.put(ex.getMessage(), NotificationType.ERROR);
        }
    }
    
    private Set<ConnectableEntity> getUnmappedElements() {
        Set<ConnectableEntity> defPropMap = new HashSet<>();
        Map<ConnectableEntity, ElementMapping> targets = parser.getTargetMappings();
        if (targets.isEmpty())
            return Collections.unmodifiableSet(defPropMap);
        for (ConnectableEntity el : targets.keySet())
            if (targets.get(el).source == null)
                if (targets.get(el).targetPropertyMap == null || targets.get(el).targetPropertyMap.isEmpty())
                    for (ConnectableEntity elc : targets.keySet()) {
                        Map<ConnectableEntity, PropertyStack> targetMap = targets.get(elc).targetPropertyMap;
                        if (el != elc && targetMap != null) {
                            for (ConnectableEntity elckey : targetMap.keySet())
                                if (el == elckey)
                                    defPropMap.add(el);
                        }
                    }
        return Collections.unmodifiableSet(defPropMap);
    }
    
    private void checkUnconnectedElements(Element part, Set<ConnectableEntity> patternElements, String errString) {
        if (!(part instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class))
            return;
        StructuredClassifier pattern = reader.getTransformationPattern();
        MagicDrawMapper mapper = MagicDrawMapper.getInstance();
        Set<Type> sourceTypes = new HashSet<>();
        for (ConnectableEntity key: patternElements)
            if (key.getType() instanceof Type)
                sourceTypes.add((Type) key.getType());
        for (Property prop: ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class)part).getOwnedAttribute())
            if (!sourceTypes.contains(prop.getType())) 
                errorMessages.put(String.format(errString + ". " + bundle.getString("SpecificationValidator.11"), NotificationType.WARNING.getName(), 
                    pattern.getHumanName(), mapper.getPrintableElementName(prop)), NotificationType.WARNING);
    }
    
    private void checkInvalidElements(Element part, String errString) {
        if (!(part instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class))
            return;
        StructuredClassifier pattern = reader.getTransformationPattern();
        for (Property prop: ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class)part).getOwnedAttribute())
            for (Relationship rel: prop.get_relationshipOfRelatedElement())
                errorMessages.put(String.format(errString + ". " + bundle.getString("SpecificationValidator.11"), NotificationType.WARNING.getName(), 
                    pattern.getHumanName(), rel.getHumanName()), NotificationType.WARNING);
    }
    
    private boolean isInConcat(ConnectableEntity el, Element parent) {
        StructuredClassifier pattern = reader.getTransformationPattern();
        Map<ConnectableEntity, ElementMapping> targets = parser.getTargetMappings();
        for (ConnectableEntity key: targets.keySet()) {
            ConcatMap cmap = targets.get(key).concatMap;
            if (!(cmap instanceof MDConcatMap))
                continue;
            MDConcatMap concats = (MDConcatMap) cmap;
            for (Connector conn: pattern.getOwnedConnector()) 
                if (parent == parser.getTargetElement() && el.equals(concats.getTargetPropertyStack(conn).metaElement()))
                    return true;
                else if (parent == parser.getSourceElement()) {
                    for (Connector incoming: concats.getIncomingConnectors(conn))
                        if (el.equals(concats.getSourcePropertyStack(conn, incoming).metaElement()))
                            return true;
                }
        }
        return false;           
    }

}
