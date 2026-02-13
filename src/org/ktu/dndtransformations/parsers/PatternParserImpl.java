package org.ktu.dndtransformations.parsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.ConnectorView;
import com.nomagic.magicdraw.uml.symbols.shapes.PartView;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PrimitiveType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.StructuredClassifier;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import org.ktu.dndtransformations.impl.MagicDrawConfiguration;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ConnectorEntity;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.PropertyStack;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.transformations.parsers.PatternConfiguration;


/**
 * Class for transformation pattern parsing and resolving
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class PatternParserImpl extends PatternParser<Connector, ConnectableElement, Element, Stereotype> {

    private Project project;
    
    private PresentationElement rootPres;
    private Collection<PresentationElement> sourcePres, targetPres, sourcePresAll, targetPresAll;
    private Collection<Object> sourceElems, targetElems;
    private Map<PropertyStack, ConnectableEntity> srcProps, tgtProps;
    private boolean hasRead;
    private static PatternConfiguration config;

    /**
     * Initializes new instance of {@link PatternParser}
     *
     * @param rootPattern	The pattern element
     * @param targetCl          The Customization target classifier (effective only if the {@code elementOver} is not a Diagram)
     * @param mapper            The adapter for particular tool implementation
     * @param elementOver	The element, onto which the dragged element was dragged
     * @throws InvalidPatternException	The pattern is invalid or could not be processed
     */
    public PatternParserImpl(StructuredClassifier rootPattern, Classifier targetCl, 
            MagicDrawMapper mapper, Element elementOver) throws InvalidPatternException {
        super(rootPattern, targetCl, mapper, elementOver, true);
        init();
    }
    
    public PatternParserImpl(StructuredClassifier rootPattern, Classifier targetCl, MagicDrawMapper mapper,
            Element elementOver, boolean validate) throws InvalidPatternException {
        super(rootPattern, targetCl, mapper, elementOver, validate);
        init();
        
    }
    
    private void init() throws InvalidPatternException {
        project = Application.getInstance().getProject();
        config = getPatternConfiguration();
        readMainElements();
        hasRead = false;
        mappingFactory = new MagicDrawMappingFactory();
        parse();
    }

    /**
     * Return customization target
     * @return The Classifier which is the target Element
     */
    public Classifier getCustomizationTarget() {
        return (Classifier) targetCl;
    }

    /**
     * Return a {@link Map} of "source-to-target" mappings
     * @return {@link Map} of mappings
     */
    @Override
    public Map<ConnectableEntity, ElementMapping> getSourceMappings() {
        return Collections.unmodifiableMap(sources);
    }

    /**
     * Return a {@link Map} of "target-to-source" mappings
     * @return {@link Map} of mappings
     */
    @Override
    public Map<ConnectableEntity, ElementMapping> getTargetMappings() {
        return Collections.unmodifiableMap(targets);
    }
    
    @Override
    protected Map<Connector, ConnectableElement[]> getPatternConnectorElements() {
        return getOwnedConnectors(((StructuredClassifier)rootPattern).getOwnedConnector());
    }
    
    @Override
    protected Map<Connector, ConnectableElement[]> getSourceConnectorElements() {
        Object source = getSourceElement();
        if (source != null)
            return getOwnedConnectors(((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) source).getOwnedConnector());
        return new HashMap<>();
    }
    
    @Override
    protected Map<Connector, ConnectableElement[]> getTargetConnectorElements() {
        Object target = getTargetElement();
        if (target != null)
            return getOwnedConnectors(((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) target).getOwnedConnector());
        return new HashMap<>();
    }
    
    private Map<Connector, ConnectableElement[]> getOwnedConnectors(Collection<Connector> connList) {
        Map<Connector, ConnectableElement[]> connObjs = new HashMap<>();
        for (Connector conn: connList) {
            List<ConnectorEnd> ends = conn.getEnd();
            ConnectableElement[] endObjs = new ConnectableElement[2];
            endObjs[0] = ends.get(0).getRole();
            endObjs[1] = ends.get(1).getRole();
            connObjs.put(conn, endObjs);
        }
        return connObjs;
    }
    
    @Override
    protected Map<Object, Object[]> getOwnedConnectorViews() {
        List<PresentationElement> els = rootPres.getPresentationElements();
        List<ConnectorView> result = new ArrayList<>();
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof ConnectorView)
                    result.add((ConnectorView) pres);
                newEls.addAll(pres.getPresentationElements());
            }
            els = newEls;
        }
        Iterator<ConnectorView> iter = result.iterator();
        while (iter.hasNext()) {
            ConnectorView conn = iter.next();
            if ((sourcePres.contains(conn.getClient()) && sourcePres.contains(conn.getSupplier()))
                    || (targetPres.contains(conn.getClient()) && targetPres.contains(conn.getSupplier())))
                iter.remove();
        }
        Map<Object, Object[]> connObjs = new HashMap<>();
        for (ConnectorView conn: result) {
            Object[] endObjs = new Object[2];
            endObjs[0] = conn.getClient();
            endObjs[1] = conn.getSupplier();
            connObjs.put(conn, endObjs);
        }
        return connObjs;
    }
    
    @Override
    protected boolean hasJoinStereotype(ConnectableElement connObj) {
        if (connObj == null)
            return false;
        Stereotype joinSt = null;
        if (mapper instanceof MagicDrawMapper) {
            joinSt = (Stereotype) ((MagicDrawMapper)mapper).getJoinStereotype();
            return StereotypesHelper.hasStereotype(connObj, joinSt);
        }
        return false;
    }
    
    @Override
    protected boolean representsElement(ConnectableEntity entity, Object el) {
        if (el instanceof Element)
            return entity.getConnectableObject().equals(el);
        else if (el instanceof PresentationElement)
            return entity.getConnectableObject().equals(((PresentationElement)el).getElement());
        return false;
    }

    @Override
    protected Element[] getPartElements() {
        Element[] root = new Element[2];
        Collection<Property> attributeList = ((StructuredClassifier)rootPattern).getPart();
        for (Property prop : attributeList) {
            if (StereotypesHelper.hasStereotype(prop.getType(), config.getSourceStereotypeName())) {
                if (root[0] == null)
                    root[0] = prop.getType();
                else
                    root = null;
            } else if (StereotypesHelper.hasStereotype(prop.getType(), config.getTargetStereotypeName())) {
                if (root[1] == null)
                    root[1] = prop.getType();
                else
                    root = null;
            }
        }
        return root;
    }

    /**
     * Get DiagramPresentationElement which is mandatory to process transformation pattern
     * @return	The DiagramPresentationElement which has been found; {@code null}, otherwise
     */
    public DiagramPresentationElement getRequiredDiagram() {
        if (project == null)
            return null;
        Collection<DiagramPresentationElement> diagrams = project.getDiagrams();
        for (DiagramPresentationElement diagram : diagrams) 
            for (Element element : diagram.getUsedModelElements(true))
                if (element == rootPattern)
                    return diagram;
        return null;
    }

    /**
     * Given UML Package instance, find DiagramPresentationElement which is mandatory to process transformation pattern
     *
     * @param rootPackage	UML Package element, which should contain the Diagram
     * with the necessary DiagramPresentationElement
     * @return	The DiagramPresentationElement which has been found;
     * {@code null}, otherwise
     */
    public DiagramPresentationElement getRequiredDiagram(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package rootPackage) {
        Collection<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package> packages = rootPackage.getNestedPackage();
        for (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package pack : packages) {
            Collection<Diagram> diagrams = pack.getOwnedDiagram();
            for (Diagram diagram : diagrams) {
                DiagramPresentationElement diagramEl = project.getDiagram(diagram);
                for (Element element : diagramEl.getUsedModelElements(true))
                    if (element == rootPattern)
                        return diagramEl;
            }
            DiagramPresentationElement res = getRequiredDiagram(pack);
            if (res != null)
                return res;
        }
        return null;
    }

    /**
     * Get PresentationElement, representing transformation pattern, by the given DiagramPresentationElement (obtained using {@link #getRequiredDiagram()})
     * @return The PresentationElement that was found
     */
    public PresentationElement getMainPresentationElement() {
        DiagramPresentationElement required = getRequiredDiagram();
        if (required == null)
            return null;
        return getMainPresentationElementByPackage(required);
    }

    /**
     * Get PresentationElement, representing transformation pattern, by the given DiagramPresentationElement
     * @param diagram	DiagramPresentationElement used for search
     */
    private PresentationElement getMainPresentationElementByPackage(DiagramPresentationElement diagram) {
        if (project == null)
            return null;
        diagram.ensureLoaded();
        List<PresentationElement> elements = project.getSymbolElementMap().getAllPresentationElements((Element) rootPattern, diagram);
        return !elements.isEmpty() ? elements.get(0) : null;
    }

    /**
     * Identify main PresentationElement by the given Package
     * @param rootPackage   The UML Package element where the search is performed
     * @return	Given UML Package instance, get main PresentationElement of the
     * DiagramPresentationElement (obtained using {@link #getRequiredDiagram(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package)})
     */
    public PresentationElement getMainPresentationElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package rootPackage) {
        DiagramPresentationElement required = getRequiredDiagram(rootPackage);
        if (required == null) {
            Collection<Diagram> diagrams = rootPackage.getOwnedDiagram();
                for (Diagram diagram : diagrams) {
                    DiagramPresentationElement diagramEl = project.getDiagram(diagram);
                    if (diagram != null) {
                        for (Element element : diagramEl.getUsedModelElements(true))
                            if (element == rootPattern) {
                                required = diagramEl;
                                break;
                            }
                    }
                }
        }
        if (required == null)
            return null;
        return getMainPresentationElementByPackage(required);
    }

    /**
     * Internal procedure to read transformation pattern. Must implemented for each PatternParser processor
     * @param propList      The {@link List} of Objects in one of the transformation pattern parts which should be processed
     * @param elList        The {@link List} of ConnectableElements in the opposite part of the transformation pattern
     * @return  {@link Map} which contains PropertyStack structures together with their corresponding {@link ConnectableEntity} objects
     */
    private Map<PropertyStack, ConnectableEntity> traverseElementStructure(Collection<?> propList, Collection<?> elList) {
        Map<PropertyStack, ConnectableEntity> srcPropMap = new HashMap<>();
        for (Object propObj : propList)
            if (propObj instanceof PresentationElement) {
                PresentationElement prop2 = (PresentationElement) propObj;
                srcPropMap.put(new PropertyStack(), getConnectableEntity((ConnectableElement) prop2.getElement()));
                Set<PropertyStack> struct = new HashSet<>();
                PropertyStack initEl = new PropertyStack();
                initEl.push(getConnectableEntity((ConnectableElement) prop2.getElement()));
                struct.add(initEl);
                List<PresentationElement> els = prop2.getPresentationElements();
                while (!els.isEmpty()) {
                    List<PresentationElement> newEls = new ArrayList<>(), foundAttrs = new ArrayList<>();
                    for (PresentationElement pres : els) {
                        if (pres instanceof PartView)
                            foundAttrs.add(pres);
                        newEls.addAll(pres.getPresentationElements());
                    }
                    if (!foundAttrs.isEmpty()) {
                        Set<PropertyStack> newStruct = new HashSet<>();
                        for (PresentationElement foundAttr : foundAttrs)
                            for (PropertyStack elTree : struct) {
                                PropertyStack newTree = new PropertyStack();
                                newTree.addAll(elTree);
                                ConnectableElement lowestType = (ConnectableElement) elTree.lowermostProperty().getConnectableObject();
                                if (foundAttr.getParent().getElement().equals(lowestType.getType()))
                                    newTree.push(getConnectableEntity((ConnectableElement) foundAttr.getElement()));
                                if (hasConnectionWithOpposite(foundAttr, elList))
                                    newStruct.add(elTree);
                                newStruct.add(newTree);
                            }
                        struct = newStruct;
                    }
                    els = newEls;
                }
                for (PropertyStack elTree : struct)
                    srcPropMap.put(elTree, getConnectableEntity((ConnectableElement) prop2.getElement()));
            }

        Iterator<PropertyStack> iter = srcPropMap.keySet().iterator();
        while (iter.hasNext()) {
            PropertyStack item = iter.next();
            if (item.isEmpty())
                iter.remove();
        }
        return srcPropMap;
    }

    /**
     * Checks, if given transformation pattern element has connection with opposite part 
     * (i.e., if {@code presEl} is in source part, then target part is checked, etc.)
     *
     * @param presEl	The transformation pattern element which is checked
     * @param propList	List of transformation pattern elements which is checked
     * @return	{@code true} if given transformation pattern element has
     * connection with opposite part; {@code false} otherwise
     */
    private boolean hasConnectionWithOpposite(PresentationElement presEl, Collection<?> propList) {
        if (!(presEl.getElement() instanceof ConnectableElement))
            return false;
        ConnectableElement el = (ConnectableElement) presEl.getElement();
        for (ConnectorEnd end : el.getEnd())
            for (ConnectorEnd connEnd : end.get_connectorOfEnd().getEnd())
                if (end != connEnd && !propList.contains(connEnd.getRole()))
                    return true;
        return false;
    }

    /**
     * Finds PresentationElement by the name of the element and root PresentationElement
     * @param name	The name of the element
     * @param rootElem	The root PresentationElement where search is performed
     * @return	Found PresentationElement, or {@code null} if no suitable
     * PresentationElement has been found
     */
    private PresentationElement getPresentationElementByName(String name, PresentationElement rootElem) {
        List<PresentationElement> els = rootElem.getPresentationElements();
        PresentationElement root_ = null;
        while (root_ == null || !els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView
                        && mapper.getElementName3((ConnectableElement) pres.getElement()).compareToIgnoreCase(name) == 0)
                    return pres;
                newEls.addAll(pres.getPresentationElements());
            }
            if (root_ != null)
                return root_;
            els = newEls;
        }
        return null;
    }

    /**
     * Finds PresentationElement by the name of the stereotype, applied to the element, and root PresentationElement
     * @param name	The name of the stereotype, applied to the element
     * @param rootElem	The root PresentationElement where search is performed
     * @return	Found PresentationElement, or {@code null} if no suitable
     * PresentationElement has been found
     */
    private PresentationElement getPresentationElementByStereotype(String stName, PresentationElement rootElem) {
        List<PresentationElement> els = rootElem.getPresentationElements();
        PresentationElement root = null;
        while (root == null || !els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView && pres.getElement() instanceof TypedElement
                        && StereotypesHelper.hasStereotype(((TypedElement) pres.getElement()).getType(), stName))
                    return pres;
                newEls.addAll(pres.getPresentationElements());
            }
            if (root != null)
                return root;
            els = newEls;
        }
        return null;
    }

    
    @Override
    protected Collection<Object> getElementsByStereotype(String stName) {
        List<Object> result = new ArrayList<>();
        if (rootPres == null)
            return result;
        List<PresentationElement> els = rootPres.getPresentationElements();
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView && StereotypesHelper.hasStereotype(pres.getElement(), stName))
                    result.add(pres);
                newEls.addAll(pres.getPresentationElements());
            }
            els = newEls;
        }
        return result;
    }

    @Override
    protected boolean isOwningElement(Object parent, Object child, String partName) {
        if (parent == null && !(parent instanceof ConnectableElement) && !(child instanceof PresentationElement))
            return false;
        ConnectableElement item = (ConnectableElement) parent;
        PresentationElement propEl = (PresentationElement) child;
        if (propEl.getElement().equals(item))
            return true;
        PresentationElement root_ = getPresentationElementByName(partName, rootPres);
        List<PresentationElement> els = root_.getPresentationElements();
        PresentationElement owner = null;
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView && pres.getElement().equals(item)) {
                    owner = pres;
                    break;
                }
                newEls.addAll(pres.getPresentationElements());
            }
            els = newEls;
        }
        if (owner == null)
            return false;
        els = owner.getPresentationElements();
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView && pres.equals(propEl))
                    return true;
                newEls.addAll(pres.getPresentationElements());
            }
            els = newEls;
        }
        return false;
    }

    /**
     * Recursively get all top-level PartView PresentationElements in particular element
     * @param rootElem	The root PresentationElement where the search is performed
     * @return	The {@link Vector} of PresentationElements in {@code root}
     */
    private Vector<PresentationElement> getPresentationElements(PresentationElement rootElem) {
        Vector<PresentationElement> sourcePres = new Vector<>();
        if (rootElem == null)
            return sourcePres;
        List<PresentationElement> els = rootElem.getPresentationElements();
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els)
                if (pres instanceof PartView)
                    sourcePres.add(pres);
                else
                    newEls.addAll(pres.getPresentationElements());
            els = newEls;
        }
        return sourcePres;
    }

    /**
     * Recursively get all PartView PresentationElements in particular element
     * (PartView elements also are searched recursively after they are identified)
     * @param rootElem	The root PresentationElement where the search is performed
     * @return	The {@link Vector} of PresentationElements in {@code root}
     */
    private Vector<PresentationElement> getAllPresentationElements(PresentationElement rootElem) {
        List<PresentationElement> els = rootElem.getPresentationElements();
        Vector<PresentationElement> _sourcePres = new Vector<>();
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView)
                    _sourcePres.add(pres);
                newEls.addAll(pres.getPresentationElements());
            }
            els = newEls;
        }
        return _sourcePres;
    }

    /**
     * Get all ConnectableElements which have particular stereotype
     * @param stName	Stereotype name
     * @param rootElem	The root PresentationElement where the search is performed
     * @return	{@link List} of Object which have particular stereotype
     */
    private List<Object> getConnectableElements(String stName, PresentationElement rootElem) {
        List<PresentationElement> els = rootElem.getPresentationElements();
        PresentationElement root = null;
        while (root == null || !els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView && pres.getElement() instanceof TypedElement
                        && StereotypesHelper.hasStereotype(((TypedElement) pres.getElement()).getType(), stName)) {
                    root = pres;
                    break;
                }
                newEls.addAll(pres.getPresentationElements());
            }
            if (root != null)
                break;
            els = newEls;
        }
        return getConnectableElements(root);
    }

    /**
     * Get all ConnectableElements in particular PresentationElement
     * @param rootElem	The root PresentationElement where the search is performed
     * @return	A {@link List} of ConnectableElements
     */
    private List<Object> getConnectableElements(PresentationElement rootElem) {
        List<Object> cels = new ArrayList<>();
        List<PresentationElement> els = rootElem.getPresentationElements();
        while (!els.isEmpty()) {
            List<PresentationElement> newEls = new ArrayList<>();
            for (PresentationElement pres : els) {
                if (pres instanceof PartView)
                    cels.add((ConnectableElement) pres.getElement());
                newEls.addAll(pres.getPresentationElements());
            }
            els = newEls;
        }
        return cels;
    }
    
    
    private void readPatternElements() throws InvalidPatternException {
        rootPres = null;
        Collection<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package> packages = project.getModel().getNestedPackage();
        for (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package pack : packages) {
            rootPres = getMainPresentationElement(pack);
            if (rootPres != null)
                break;
        }
        if (rootPres == null)
            throw new InvalidPatternException("Main element could not be identified");
        PresentationElement root = getPresentationElementByStereotype(config.getSourceStereotypeName(), rootPres);
        sourcePres = getPresentationElements(root);
        sourcePresAll = getAllPresentationElements(root);
        root = getPresentationElementByStereotype(config.getTargetStereotypeName(), rootPres);
        targetPres = getPresentationElements(root);
        sourceElems = getConnectableElements(config.getSourceStereotypeName(), rootPres);
        targetPresAll = getAllPresentationElements(root);
        targetElems = getConnectableElements(config.getTargetStereotypeName(), rootPres);
        srcProps = traverseElementStructure(sourcePres, sourceElems);
        tgtProps = traverseElementStructure(targetPres, targetElems);
        hasRead = true;
    }

    @Override
    protected Collection<Object> getSourceElements() throws InvalidPatternException {
        if (!hasRead)
            readPatternElements();
        return sourceElems;
    }

    @Override
    protected Collection<Object> getTargetElements() throws InvalidPatternException {
        if (!hasRead)
            readPatternElements();
        return targetElems;
    }

    @Override
    protected Map<PropertyStack, ConnectableEntity> getSourcePropertyMappings() throws InvalidPatternException {
        if (!hasRead)
            readPatternElements();
        return srcProps;
    }

    @Override
    protected Map<PropertyStack, ConnectableEntity> getTargetPropertyMappings() throws InvalidPatternException {
        if (!hasRead)
            readPatternElements();
        return tgtProps;
    }

    /**
     * Find transformation pattern specification element in a set of candidate elements, corresponding to a given classifier
     * @param elements	The collection of candidate elements
     * @param pattern	The classifier which corresponds to the transformation pattern
     * @return	The Element, representing transformation pattern
     */
    public static Element getPatternSpecificationElement(Collection<Element> elements, Classifier pattern) {
        MagicDrawMapper mapper = MagicDrawMapper.getInstance();
        for (Element element : elements)
            if (mapper.getElementName2(element).compareTo(mapper.getElementName2(pattern)) == 0)
                return element;
        return null;
    }

    /**
     * Checks if the parent part of given transformation pattern element is
     * either source part, or target part of the transformation pattern classifier
     *
     * @param el	The mapping element which is checked
     * @return	Depending on the result, returns either a source part element, a target part element, 
     * or {@code null} if {@code el} does not belong to any of these parts
     */
    @Override
    protected Element getRootPart(Object el) {
        if (el == null || (!(el instanceof Element) && !(el instanceof PresentationElement)))
            return null;
        if (el instanceof Element) {
            for (PresentationElement elem : sourcePres)
                if (el.equals(elem.getElement()))
                    return getSourceElement();
            for (PresentationElement elem : targetPres)
                if (el.equals(elem.getElement()))
                    return getTargetElement();
        } else if (el instanceof PresentationElement) {
            if (sourcePresAll.contains(el))
                return getSourceElement();
            else if (targetPresAll.contains(el))
                return getTargetElement();
            else
                return null;
        }
        return null;
    }

    @Override
    protected boolean allValidConnections(Object part) {
        // We cannot have any "element-to-element" relationships in any part - every mapping element must be 
        // associated with an internal property of other element (e.g., connecting element)
        if (part == null || !(part instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class))
            return false;
        Collection<Connector> connectorList = ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) part).getOwnedConnector();
        for (Connector conn : connectorList) {
            ConnectableElement first = conn.getEnd().get(0).getRole();
            ConnectableElement second = conn.getEnd().get(1).getRole();
            if (first.getOwner() == part && second.getOwner() == part)
                return false;
        }
        return true;
    }
    
    private String getConstraintText(Constraint constraint) {
        ValueSpecification spec = constraint.getSpecification();
        if (spec != null && spec instanceof OpaqueExpression) {
            List<String> body = ((OpaqueExpression) spec).getBody();
            if (body != null && !body.isEmpty()) {
                String val = body.get(0).trim();
                if (val.length() > 0)
                    return val;
            }
        } else if (spec != null && spec instanceof Expression) {
            String val = ((Expression) spec).getSymbol();
            if (val != null && val.trim().length() > 0)
                return val.trim();
        }
        return null;
    }

    /**
     * Get text representing UML Constraint for particular Connector element. If several Constraints 
     * are defined for this element, the returned value corresponds to the first Constraint 
     * which is not empty (i.e., has non-empty textual representation)
     * @param conn      Connector element which has the Constraint 
     * @return             {@link String} representing the Constraint which has been found
     */
    @Override
    protected String getRuleText(Connector conn) {
        if (conn == null)
            return null;
        for (Constraint cons : conn.get_constraintOfConstrainedElement()) {
            String text = getConstraintText(cons);
            if (text != null)
                return text;
        }
        return null;
    }
    
    /**
     * Get EnumerationLiteral representing integration type for particular UML mapping 
     * @param conn     Connector element which represents the mapping 
     * @return          EnumerationLiteral which represents integration tag; {@code null}
     * if the Connector element does not have {@value #INTEGRATION_TAG} tag
     */
    public static EnumerationLiteral getIntegrationLiteral(Connector conn) {
        if (conn == null)
            return null;
        Stereotype st = MagicDrawMapper.getInstance().getDragAndDropConnectorStereotype();
        if (StereotypesHelper.hasStereotype(conn, st))
            return (EnumerationLiteral) StereotypesHelper.getStereotypePropertyFirst(conn, st, config.getIntegrationTagName());
        return null;
    }
    
    @Override
    protected IntegrationType getIntegrationType(Connector conn) {
        if (conn == null)
            return null;
        EnumerationLiteral intType = getIntegrationLiteral(conn);
        if (intType == null)
            return IntegrationType.UNDEFINED;
        try {
            return IntegrationType.valueOf(intType.getName().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IntegrationType.UNDEFINED;
        }
    }
    
    @Override
    protected ConnectorEntity getConnectorEntity(Object connObj) {
        if (connObj == null || (!(connObj instanceof Connector) && !(connObj instanceof ConnectorView)))
            return null;
        Connector conn = null;
        if (connObj instanceof Connector)
            conn = (Connector) connObj;
        else if (connObj instanceof ConnectorView)
            conn = ((ConnectorView)connObj).getConnector();
        ConnectorEntity outEntity = new ConnectorEntity(conn);
        outEntity.setRule(getRuleText(conn));
        outEntity.setIntegrationType(getIntegrationType(conn));
        return outEntity;
    }

    @Override
    protected Object getMappedToProperty(Object first, Object second, Object target) {
        if (first == null || !(first instanceof Property) || second == null || !(second instanceof Property))
            return null;
        boolean tfirst = ((Property) first).getUMLClass() == target;
        boolean tsecond = ((Property) second).getUMLClass() == target;
        if ((!tfirst && !tsecond) || (tfirst && tsecond))
            return null;
        return tfirst ? second : first;
    }

    @Override
    protected ConnectableEntity createStringElement(String name) {
        if (project == null)
            return null;
        ElementsFactory f = project.getElementsFactory();
        Property strEl = f.createPropertyInstance();
        PrimitiveType type = f.createPrimitiveTypeInstance();
        f.createPropertyInstance();
        type.setName("String");
        strEl.setType(type);
        if (name != null)
            strEl.setName(name);
        return getConnectableEntity(strEl);
    }

    @Override
    protected String getPatternName() {
        return getPatternRoot().getHumanName();
    }
    
    /**
     * Return the pattern element
     * @return The StructuredClassifier element, representing the transformation pattern
     */
    @Override
    public StructuredClassifier getPatternRoot() {
        return (StructuredClassifier) rootPattern;
    }

    @Override
    public PatternConfiguration getPatternConfiguration() {
        if (config == null) 
            config = MagicDrawConfiguration.getPatternConfiguration();
        return config;
    }
    
    @Override
    protected void performAdditionalUpdate(ElementMapping mapping, ConnectableEntity ce, Connector conn) {
        EnumerationLiteral integration = getIntegrationLiteral(conn);
        if (mapping instanceof MDElementMapping)
            ((MDElementMapping)mapping).addIntegrationLiteral(ce, integration);
    }
    
}
