package org.ktu.dndtransformations.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.ElementFinder;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.Set;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ConnectableEntity;

/**
 * Perform MagicDraw UML element search
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 *
 */
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
public class MagicDrawSearch implements ElementSearch<Element, Stereotype> {
    
    private static MagicDrawSearch INSTANCE;
    private ElementMapper mapper;
    
    private MagicDrawSearch(ElementMapper mapper) {
        this.mapper = mapper;
    }
    
    public static MagicDrawSearch getInstance() {
        if (INSTANCE == null) 
            INSTANCE = new MagicDrawSearch(MagicDrawMapper.getInstance());
        return INSTANCE;
    }
    
    @Override
    public Element find(Element element, Class clazz, String name) {
        return ElementFinder.find(element, clazz, name);
    }

    /**
     * Search for an element by its class, name and stereotype name
     * @param owner		The root Element where search is performed
     * @param base		Base class of the searched Element
     * @param stereotype	Stereotype which is applied to the Element
     * @param elementName	Element name
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    @Override
    public Element findElement(Element owner, Class base, Stereotype stereotype, String elementName) {
        return findElementRecursively(owner, base, stereotype, elementName, false);
    }

    /**
     * Search recursively for an element by its class, name and stereotype name
     * @param owner         The root Element where search is performed
     * @param base          Base class of the searched Element
     * @param stereotype    Stereotype which is applied to the Element
     * @param elementName   Element name
     * @param recursively   Indicates whether search should be performed recursively by searching subpackages or child elements
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    @Override
    public Element findElementRecursively(Element owner, Class base, Stereotype stereotype, String elementName, boolean recursively) {
        if (owner == null)
            owner = Application.getInstance().getProject().getModel();
        Collection<Element> candList = ElementFinder.getChildren(owner, new Class[]{base}, recursively, true);
        if (stereotype != null) {
            for (Element el : candList) {
                String elName = mapper.getElementName(el);
                elName = elName != null ? mapper.getProperName(elName) : "";
                if (StereotypesHelper.hasStereotype(el, stereotype) && elName.compareTo(elementName) == 0)
                    return el;
            }
            return null;
        } else {
            for (Element el : candList) {
                String elName = mapper.getElementName(el);
                elName = elName != null ? mapper.getProperName(elName) : "";
                if (!StereotypesHelper.hasStereotype(el) && elName.compareTo(elementName) == 0)
                    return el;
            }
            return null;
        }
    }

    /**
     * Search for an Relationship element
     * @param owner			    The root Element where search is performed
     * @param base			     Base class of the searched Element
     * @param stereotype	 Stereotype which is applied to the relationship element
     * @param elementName	Element name
     * @param prop1Name		 The name of the first property (corresponding to MagicDraw implementation of UML metamodel), which contains {@code prop1}
     * @param prop1			    The Element representing the object of the first property
     * @param prop2Name		 The name of the second property (corresponding to MagicDraw implementation of UML metamodel), which contains {@code prop2}
     * @param prop2			    The Element representing the object of the second property
     * @param checkNull		 Indicates whether {@code null} values of element names should be taken into account (e.g., elements,
     *                    such as Dependency, Generalization or even Association often do not have names in the model)
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    @Override
    public Element findRelationship(Element owner, Class base, Stereotype stereotype, String elementName,
            String prop1Name, Element prop1, String prop2Name, Element prop2, boolean checkNull) {
        if (prop1Name == null || prop1Name.trim().length() == 0 || prop2Name == null || prop2Name.trim().length() == 0)
            return null;
        Project project = Application.getInstance().getProject();
        if (owner == null)
            owner = project.getModel();
        PropertyManager manager = PropertyManager.getInstance();
        if ((elementName == null || elementName.trim().length() == 0) && checkNull) {
            Collection<? extends Element> elements = ModelHelper.getElementsOfType(owner, new Class[]{base}, true);
            if (elements == null)
                return null;
            for (Element el : elements) {
                boolean stereotypeCond = stereotype != null ? StereotypesHelper.hasStereotype(el, stereotype) : true;
                if (stereotypeCond && manager.hasPropertyValue(el, prop1Name, prop1) && manager.hasPropertyValue(el, prop2Name, prop2))
                    return el;
            }
        } else {
            List<NamedElement> list = ModelHelper.getElementsByClassAndName(base, elementName);
            for (NamedElement el : list) {
                boolean stereotypeCond = stereotype != null ? StereotypesHelper.hasStereotype(el, stereotype) : true;
                if (stereotypeCond && manager.hasPropertyValue(el, prop1Name, prop1) && manager.hasPropertyValue(el, prop2Name, prop2))
                    return el;
            }
        }
        return null;
    }

    /**
     * Search for an ActivityPartition element in the Model of an active project
     * @param name	The name of the element
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    @Override
    public ActivityPartition findActivityPartitionElement(String name) {
        return findActivityPartitionElement(Application.getInstance().getProject().getModel(), name, null, false);
    }

    /**
     * Search for an ActivityPartition element in the Model of an active project
     * @param name	          The name of the element
     * @param stereotypeName	The name of stereotype
     * @param byName		       If the value is set to {@code true}, then search will be performed according to the name of the element,
     *                       otherwise the {@code represents} property will be used
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    @Override
    public ActivityPartition findActivityPartitionElement(String name, String stereotypeName, boolean byName) {
        return findActivityPartitionElement(Application.getInstance().getProject().getModel(), name, stereotypeName, byName);
    }

    /**
     * Search for and ActivityPartition element
     * @param owner			    The root Element where search is performed
     * @param elementName	The name of the element
     * @param stName		    The name of stereotype
     * @param byName		    If the value is set to {@code true}, then search will be performed according to the name of the element,
     *                    otherwise the {@code represents} property will be used
     * @return	The Element if one has been found, or {@code null} if no Element has been found according to given parameters
     */
    @Override
    public ActivityPartition findActivityPartitionElement(Element owner, String elementName, String stName, boolean byName) {
        Project project = Application.getInstance().getProject();
        Stereotype st = stName != null ? StereotypesHelper.getStereotype(project, stName) : null;
        for (Element element : ModelHelper.getElementsOfType(owner, new Class[]{ActivityPartition.class}, true)) {
            if (st != null && !StereotypesHelper.hasStereotype(element, st))
                continue;
            String name = mapper.getElementName2(element);
            boolean condition = false;
            if (byName)
                condition = name != null && elementName.compareTo(name) == 0;
            else {
                String laneName = mapper.getElementName(((ActivityPartition) element).getRepresents());
                laneName = laneName != null ? mapper.getProperName(laneName) : name;
                condition = laneName != null && elementName.compareTo(laneName) == 0;
            }
            if (condition)
                return (ActivityPartition) element;
        }
        return null;
    }

    /**
     * Search for Class elements with particular stereotype in the Model of an active project
     * @param stereotype	The Stereotype object
     * @return	A {@link Collection} of found Elements
     */
    @Override
    public Collection<Element> findStereotypedElements(Stereotype stereotype) {
        Collection<Element> elements = new HashSet<>();
        Project project = Application.getInstance().getProject();
        Collection<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package> packages = project.getModel().getNestedPackage();
        for (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package pack : packages)
            for (Element element : ModelHelper.getElementsOfType(pack, new Class[]{com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class}, true))
                if (StereotypesHelper.hasStereotype(element, stereotype))
                    elements.add(element);
        return elements;
    }

    /**
     * Search for a UML Package element with the given name
     * @param packageName	The name of the Package
     * @return	The UML Package element which was found, or {@code null} if no Package element has been found
     */
    @Override
    public com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package findPackageByName(String packageName) {
        Project project = Application.getInstance().getProject();
        return (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) 
                ModelHelper.findInParent(project.getModel(), packageName, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class, true);
    }

    /**
     * Find given related elements, find UML Relationship
     * @param relClass      The class type of the relationship
     * @param first         The first Element at the end of the Relationship
     * @param second        The second Element at the end of the Relationship
     * @param name          The name of the Relationship element
     * @param stereotype    The stereotype which the searched Association element is stereotyped with
     * @return	The UML Relationship element which was found, or {@code null} if no Relationship element has been found, 
     * or {@code relClass} does not represent a Relationship class type
     */
    @Override
    public Relationship findRelationship(Class relClass, Element first, Element second, String name, Stereotype stereotype) {
        if (!Relationship.class.isAssignableFrom(relClass))
            return null;
        Project project = Application.getInstance().getProject();
        Collection<? extends Element> associations = ModelHelper.getElementsOfType(project.getModel(), new Class[]{relClass}, true);
        for (Element el : associations) {
            Relationship assoc = (Relationship) el;
            if (assoc.getRelatedElement().contains(first) && assoc.getRelatedElement().contains(second)
                    && (assoc instanceof NamedElement && name != null && name.trim().length() > 0 ? 
                        ((NamedElement)assoc).getName().equals(name) : true)
                    && (stereotype != null ? StereotypesHelper.hasStereotype(assoc, stereotype) : true))
                return assoc;
        }
        return null;
    }

    @Override
    public Collection<? extends Element> findChildren(Element root, Class[] types, boolean checkParent) {
        return ElementFinder.getChildren(root, types, checkParent);
    }
    
    /**
     * Finds transformation pattern element for given actual element
     * @param source	The actual element (e.g., that is dragged, etc.)
     * @param sources	The set of transformation pattern elements, where the search is performed
     * @return	The element that has been found, or {@code null} if no such element has been found
     */
    public static ConnectableEntity getPatternElement(Element source, Set<ConnectableEntity> sources) {
        ConnectableEntity res = null;
        MagicDrawMapper mapper = MagicDrawMapper.getInstance();
        if (mapper.hasStereotype(source))
            for (Element st : mapper.getStereotypes(source)) {
                res = getPatternElementByString(mapper.getElementName2(st), sources);
                if (res != null)
                    return res;
            }
        return getPatternElementByString(mapper.getTypeName(source), sources);
    }

    private static ConnectableEntity getPatternElementByString(String elementName, Set<ConnectableEntity> sources) {
        for (ConnectableEntity source : sources)
            if (elementName.compareTo(source.getName3()) == 0)
                return source;
        return null;
    }

    @Override
    public Collection<? extends Element> getElementsOfType(Element root, Class<?>[] types, boolean checkParent) {
        return ModelHelper.getElementsOfType(root, types, checkParent);
    }
    
}
