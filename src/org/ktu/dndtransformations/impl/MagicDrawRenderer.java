package org.ktu.dndtransformations.impl;

import org.ktu.transformations.renderers.ElementRenderer;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.ViewHelper;
import com.nomagic.magicdraw.uml.symbols.shapes.SwimlaneCellView;
import com.nomagic.magicdraw.uml.symbols.shapes.SwimlaneView;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.Action;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.transformations.mappers.ElementMapper;

/**
 * A factory class which contains the functionality to create and render MagicDraw PresentationElement representations of actual Elements
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2014-2015
 */
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
public class MagicDrawRenderer implements ElementRenderer<Element, PresentationElement> {

    private static MagicDrawRenderer INSTANCE;
    private static ElementMapper mapper;
    
    private MagicDrawRenderer(ElementMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Return an instance of this factory (if there is no existing instance then one is created)
     * @return An instance of {@link MagicDrawRenderer}
     */
    public static MagicDrawRenderer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new MagicDrawRenderer(MagicDrawMapper.getInstance());
        return INSTANCE;
    }

    /**
     * Creates a generic PresentationElement for a given Element
     * @param element	 Element which PresentationElement must be created for
     * @param location	Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	  The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The generated PresentationElement
     */
    @Override
    public PresentationElement renderSingleElement(Element element, Point location, PresentationElement parent) {
        if (element == null || element instanceof Constraint || !ViewHelper.canElementHaveSymbolInDiagram(element))
            return null;
        if (mapper.isActivityClassifier(element.getClassType()) && parent instanceof DiagramPresentationElement)
            return renderSingleSwimlane(element, location, (DiagramPresentationElement) parent);
        PresentationElement presel = findPresentationElement(parent, element);
        if (presel == null) {
            try {
                PresentationElementsManager manager = PresentationElementsManager.getInstance();
                presel = manager.createShapeElement(element, parent);
                if (location != null)
                    presel.setLocation(location);
                return presel;
            } catch (ReadOnlyElementException e) {
                Logger.getLogger(getClass()).log(Level.ERROR, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Creates a PresentationElement for a given single swimlane element
     * @param element	Element which PresentationElement must be created for
     * @param location	Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return      The generated PresentationElement
     */
    @Override
    public PresentationElement renderSingleSwimlane(Element element, Point location, PresentationElement parent) {
        if (!mapper.isActivityClassifier(element.getClassType()))
            return null;
        PresentationElement presel = containsLaneWithName(parent, mapper.getElementName2(element));
        if (presel == null && parent instanceof DiagramPresentationElement) {
            PresentationElementsManager manager = PresentationElementsManager.getInstance();
            List<ActivityPartition> newparts = new ArrayList<>();
            if (element instanceof ActivityPartition)
                newparts.add((ActivityPartition) element);
            try {
                presel = manager.createSwimlane(newparts, new ArrayList(), (DiagramPresentationElement) parent);
                if (location != null)
                    presel.setLocation(location);
                return presel;
            } catch (ReadOnlyElementException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Creates PresentationElements for a tuple of Elements, consisting of relationship-type of connecting element and its properties
     * @param mainel        The main (connecting) Element
     * @param properties    The set of Elements which are connected by {@code mainel}
     * @param location      Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent        The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The {@linkplain List} of generated PresentationElement
     */
    @Override
    public List<PresentationElement> renderRelatedElements(Element mainel, List<Element> properties, Point location, PresentationElement parent) {
        ArrayList<PresentationElement> visuals = new ArrayList<>();
        PresentationElementsManager manager = PresentationElementsManager.getInstance();
        List<PresentationElement> layout = new ArrayList<>();
        PresentationElement presel = null;
        try {
            for (Element newel : properties) {
                presel = findPresentationElement(parent, newel);
                if (presel == null) {
                    presel = manager.createShapeElement(newel, parent);
                    layout.add(presel);
                }
                visuals.add(presel);
            }
            PresentationElement client = ModelHelper.getClientElement(mainel) == visuals.get(0).getElement() ? visuals.get(0) : visuals.get(1);
            PresentationElement supplier = ModelHelper.getSupplierElement(mainel) == visuals.get(1).getElement() ? visuals.get(1) : visuals.get(0);
            presel = manager.createPathElement(mainel, client, supplier);
            layout.add(presel);
            if (location != null)
                presel.setLocation(location);
        } catch (ReadOnlyElementException e) {
            Logger.getLogger(getClass()).log(Level.ERROR, e.getMessage());
        }
        return layout;
    }

    /**
     * Creates PresentationElements for a tuple of Elements, consisting of container-type of connecting element and elements, contained by this element
     * @param mainel	 The main (connecting) Element
     * @param properties The {@linkplain Map} describing the structure of Elements which must be represented together with the connecting element. The keys of
     *                   {@code properties} define the name of the property (corresponding to MagicDraw implementation of UML metamodel), 
     *                   and the value is {@linkplain Set} of Elements, which are set as these properties
     * @param location	 Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	 The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The {@linkplain List} of generated PresentationElement
     */
    @Override
    public List<PresentationElement> renderContainedElements(Element mainel, Map<String, Set<Element>> properties, Point location, PresentationElement parent) {
        PresentationElementsManager manager = PresentationElementsManager.getInstance();
        List<PresentationElement> layout = new ArrayList<>();
        try {
            PresentationElement presel = findPresentationElement(parent, mainel);
            if (presel == null)
                presel = manager.createShapeElement(mainel, parent);
            if (location != null)
                presel.setLocation(location);
            layout.add(presel);
            if (!properties.isEmpty()) {
                for (String fname : properties.keySet())
                    for (Element item : properties.get(fname)) {
                        boolean drawable = false;
                        try {
                            drawable = ViewHelper.canElementHaveSymbolInDiagram(item);
                        } catch (Exception ex) {
                            drawable = false;
                        }
                        if (drawable)
                            try {
                                PresentationElement nodeel = findPresentationElement(presel, item);
                                if (nodeel == null)
                                    nodeel = manager.createShapeElement(item, presel);
                                layout.add(nodeel);
                            } catch (ReadOnlyElementException e) {
                                Logger.getLogger(getClass()).log(Level.ERROR, e.getMessage());
                            }
                    }
            }
        } catch (ReadOnlyElementException e) {
            Logger.getLogger(getClass()).log(Level.ERROR, e);
        }
        return layout;
    }

    /**
     * Creates a PresentationElement for a swimlane element, together with Element contained by this element
     * @param drawableItems	The items which PresentationElement must be created for. The structure is defined as a {@linkplain Map}, where keys represent swimlane
     *                      elements, and values are of type {@linkplain Map}, with keys of defining the name of the property (corresponding to MagicDraw implementation of UML metamodel),
     *                      and the value is {@linkplain Set} of Elements, which are set as these properties
     * @param checkUnique	  Indicates if check for existing swimlane PresentationElements should also be performed
     * @param location	     Actual location on the {@code parent} where the generated PresentationElement will be placed on
     * @param parent	       The PresentationElement (e.g., a DiagramPresentationElement or other) which the resulting PresentationElement will be placed on
     * @return	The {@linkplain List} of generated PresentationElement
     */
    @Override
    public List<PresentationElement> renderSwimlane(Map<Object, Map<String, Set<Element>>> drawableItems, boolean checkUnique,
            Point location, PresentationElement parent) {
        PresentationElementsManager manager = PresentationElementsManager.getInstance();
        List<PresentationElement> layout = new ArrayList<>();
        List<ActivityPartition> newparts = new ArrayList<>();
        Map<ActivityPartition, SwimlaneView> existparts = new HashMap<>();
        for (Object el : drawableItems.keySet()) 
            if (el instanceof ActivityPartition) {
                ActivityPartition partition = (ActivityPartition) el;
                Element item = (Element) partition.eGet(partition.eClass().getEStructuralFeature("represents"));
                String name = mapper.getElementName2(item);
                partition.setName(name);
                if (checkUnique) {
                    PresentationElement presel = containsLaneWithName(parent, name);
                    if (presel != null)
                        existparts.put(partition, (SwimlaneView) presel);
                    else
                        newparts.add(partition);
                } else
                    newparts.add(partition);
        }
        try {
            if (existparts.isEmpty()) {
                SwimlaneView presel = manager.createSwimlane(newparts, new ArrayList(), (DiagramPresentationElement) parent);
                for (ActivityPartition part : newparts)
                    existparts.put(part, presel);
                layout.add(presel);
            }
        } catch (ReadOnlyElementException e) {
            Logger.getLogger(getClass()).log(Level.ERROR, e.getMessage());
        }
        for (Object mainel : drawableItems.keySet()) {
            Map<String, Set<Element>> map = drawableItems.get(mainel);
            if (map.get("node") == null)
                break;
            for (Element item : map.get("node"))
                if (item instanceof Action)
                    try {
                        ActivityPartition partition = ((Action) item).getInPartition().iterator().next();
                        if (partition != null && existparts.get(partition) != null) {
                            List<SwimlaneCellView> cells = existparts.get(partition).getHeaderToCellsMap().get(partition);
                            if (cells != null && !cells.isEmpty()) {
                                PresentationElement nodeel = findPresentationElement(cells.get(0), item);
                                if (nodeel == null)
                                    nodeel = manager.createShapeElement(item, cells.get(0));
                                layout.add(nodeel);
                            }
                        }
                    } catch (ReadOnlyElementException e) {
                        Logger.getLogger(getClass()).log(Level.ERROR, e.getMessage());
                    }
        }
        return layout;
    }

    private PresentationElement containsLaneWithName(PresentationElement owner, String name) {
        if (name == null)
            return null;
        for (PresentationElement el : owner.getPresentationElements())
            if (el instanceof SwimlaneView) {
                Set<ActivityPartition> cells = ((SwimlaneView) el).getHeaderToCellsMap().keySet();
                for (ActivityPartition cell : cells) {
                    String laneName = mapper.getElementName(cell.getRepresents());
                    laneName = laneName != null ? mapper.getProperName(laneName) : mapper.getElementName2(cell);
                    if (laneName != null && name.compareTo(laneName) == 0)
                        return el;
                }
            }
        return null;
    }

    private PresentationElement findPresentationElement(PresentationElement parentEl, Element element) {
        for (PresentationElement el : parentEl.getPresentationElements()) {
            String presName = mapper.getElementName2(el.getElement());
            String elName = mapper.getElementName2(element);
            if (presName != null && elName != null && presName.compareTo(elName) == 0
                    && el.getElement().getHumanType().compareTo(element.getHumanType()) == 0)
                return el;
        }
        return null;
    }

}
