package org.ktu.dndtransformations.impl;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.reflect.ClassTypes;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.PatternConfiguration;
import org.ktu.transformations.parsers.PatternParser;
import org.ktu.transformations.parsers.SpecificationConfiguration;

/**
 * Class implementing functional mappings for MagicDraw environment
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class MagicDrawMapper implements ElementMapper<Element, ConnectableElement, Stereotype> {
    
    private static MagicDrawMapper INSTANCE;
    
    private static final String DSL_CUSTOMIZATION_PROFILE = "DSL Customization";
    private static final String TRANSFORMATION_PROFILE = "M2M transformations";
    private static final String UML_METAMODEL_WITH_ATTRIBUTES = "UML2 Metamodel with attributes";
    private static final String INTEGRATION_PROFILE = "Integration Profile";

    private static Profile TRANSFORM_PROFILE, CUSTOM_PROFILE, UML_PROFILE, INT_PROFILE;
    private static Stereotype INTEGRATION_STEREOTYPE;
    private static Stereotype DRAGGED_STEREOTYPE, SOURCE_ST, TARGET_ST, JOIN_ST, DND_CONN_ST;
    private static Stereotype dndstereotype, custStereotype, patternStereotype;
    private PatternConfiguration patternConfig;
    private SpecificationConfiguration specConfig;
    
    private MagicDrawMapper() {
        patternConfig = MagicDrawConfiguration.getPatternConfiguration();
        specConfig = MagicDrawConfiguration.getSpecificationConfiguration();
    }
    
    public static MagicDrawMapper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new MagicDrawMapper();
        return INSTANCE;
    }

    @Override
    public String getElementName(Element e) {
        if (e == null)
            return null;
        return e.getHumanName().compareToIgnoreCase(e.getHumanType()) != 0
                ? e.getHumanName().substring(e.getHumanType().length() + 1, e.getHumanName().length()).trim() : null;
    }

    @Override
    public String getProperName(String name) {
        if (name == null || name.trim().length() == 0)
            return null;
        return name.replaceAll("\n", " ").replaceAll("  ", " ").trim();
    }

    @Override
    public boolean mapsToEntity(Element element, ConnectableEntity source) {
        if (element == null)
            return false;
        if (source.getType() == null)
            return false;
        if (source.getType() instanceof Stereotype && element instanceof Stereotype)
            return (source.getType().equals(element)
                    || StereotypesHelper.getStereotypesHierarchy(element).contains((Stereotype) source.getType()));
        else if (source.getType() instanceof Stereotype && StereotypesHelper.hasStereotype(element))
            return (StereotypesHelper.getStereotypes(element).contains((Stereotype) source.getType())
                    || StereotypesHelper.getStereotypesHierarchy(element).contains((Stereotype) source.getType()));
        else if (!(source.getType() instanceof Stereotype) && !StereotypesHelper.hasStereotype(element)) {
            String sourceName2 = getProperName(source.getName());
            if (sourceName2 != null) {
                Project project = Application.getInstance().getProject();
                String sourceName = source.getProcessedName();
                Stereotype st;
                if (element instanceof Stereotype)
                    st = StereotypesHelper.getStereotype(project, sourceName, StereotypesHelper.findProfileForElement(element));
                else
                    st = StereotypesHelper.getStereotype(project, sourceName);
                if (st == null)
                    return false;
                if (element instanceof Stereotype)
                    return element.equals(st) || StereotypesHelper.getStereotypesHierarchy(element).contains(st);
                return StereotypesHelper.getStereotypes(element).contains(st)
                        || StereotypesHelper.getStereotypesHierarchy(element).contains(st);
            } else {
                if (element instanceof Classifier && ((Classifier) element).getName().equals(source.getTypeName()))
                    return true;
                if (element.getHumanType().equals(source.getTypeName()))
                    return true;
                List<Class> types = ClassTypes.getSupertypes(element.getClassType());
                for (Iterator<Class> it = types.iterator(); it.hasNext();) {
                    Class clazz = it.next();
                    if (element.getClassType().isAssignableFrom(clazz))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getElementName2(Element element) {
        if (element == null)
            return null;
        if ((element.getHumanName() == null && element.getHumanType() == null))
            return null;
        if (element.getHumanName() != null && element.getHumanType() != null)
            return getElementName(element) != null ? getElementName(element) : getProperName(element.getHumanName());
        return element.getHumanName() != null ? getProperName(element.getHumanName()) : element.getHumanType();
    }

    @Override
    public String getElementName3(Element element) {
        if (element == null && !(element instanceof TypedElement))
            return null;
        if (element instanceof Property && getElementName(element) != null)
            return getElementName2(element);
        if (element instanceof Property && getElementName(element) == null)
            return getElementName2(((TypedElement) element).getType());
        if (((TypedElement) element).getName() != null)
            return getElementName2(element);
        return null;
    }

    @Override
    public String getPrintableElementName(ConnectableElement element) {
        if (element == null)
            return null;
        String result = null;
        if (element.getType() == null)
            result = element.getHumanName();
        else
            result = element.getType().getHumanName().compareTo(element.getType().getHumanType()) != 0
                    ? (element.getName().length() > 0 ? element.getName() + ": " : "") + element.getType().getName()
                    : element.getHumanName();
        return result != null ? result : "";
    }

    @Override
    public boolean haveIdenticalTypes(Element element1, Element element2) {
        if (element1 == null || element2 == null)
            return false;
        if (element1.getHumanType().equals(getElementName2(element2))
                || element2.getHumanType().equals(getElementName2(element1)))
            return true;
        if (element1 instanceof TypedElement && element2 instanceof TypedElement
                && ((TypedElement) element1).getType() == ((TypedElement) element2).getType())
            return true;
        boolean match = matchWithStereotype(element1, element2);
        if (match)
            return match;
        match = matchWithStereotype(element2, element1);
        if (match)
            return match;
        for (Stereotype st : StereotypesHelper.getStereotypes(element1))
            for (Stereotype st2 : StereotypesHelper.getStereotypes(element2))
                for (Class<?> cl1 : StereotypesHelper.getBaseClassesAsClasses(st))
                    for (Class<?> cl2 : StereotypesHelper.getBaseClassesAsClasses(st2))
                        if (cl1.getSimpleName().compareTo(cl2.getSimpleName()) == 0)
                            return true;
        return false;
    }

    private boolean matchWithStereotype(Element el1, Element el2) {
        if (StereotypesHelper.hasStereotype(el1) && !StereotypesHelper.hasStereotype(el2))
            for (Stereotype st : StereotypesHelper.getStereotypes(el1))
                for (Class<?> cl : StereotypesHelper.getBaseClassesAsClasses(st))
                    if (cl.getSimpleName().compareTo(getElementName2(el2)) == 0)
                        return true;
        return false;
    }

    private List<Class> getBaseClassSt(String name) {
        Stereotype st = StereotypesHelper.getStereotype(Application.getInstance().getProject(), name);
        if (name != null && st != null)
            return StereotypesHelper.getBaseClassesAsClasses(st);
        return new ArrayList<>();
    }

    @Override
    public Class<?> getBaseClass(String name) {
        name = getProperName(name);
        List<Class> classes = getBaseClassSt(name);
        Class<?> clazz = !classes.isEmpty() ? classes.get(0) : null;
        return clazz != null ? clazz : ClassTypes.getClassType(name);
    }

    @Override
    public Class<?> getBaseClass(Element element) {
        if (element == null && !(element instanceof ConnectableElement || element instanceof Type))
            return null;
        if (element instanceof ConnectableElement) {
            ConnectableElement source = (ConnectableElement) element;
            //Initially we check for corresponding stereotype in name, if name is provided with type
            if (source.getName().length() > 0) {
                Stereotype st = getRepresentedStereotype(source);
                List<Class> classes = null;
                if (st != null)
                    classes = StereotypesHelper.getBaseClassesAsClasses(st);
                // Try to find base class of the same type, as the class type of source
                if (classes != null && !classes.isEmpty()) {
                    for (Class clazz : classes)
                        if (clazz.equals(source.getType().getClassType()))
                            return clazz;
                    // If no class of the same type, as the class type of source, was identified, return first class in the list
                    return classes.get(0);
                }
                // In case the name is wrong we check for corresponding stereotype in type
                return getBaseClass(source.getType());
            } else
            // We check for corresponding stereotype in type (possibly name of element in a profile)
                // This is also applied in case the mapping element was incorrectly named
                return getBaseClass(source.getType());
        } else if (element instanceof Type) {
            Type source = (Type) element;
            if (StereotypesHelper.isExtensionElement(source)) {
                List<Class> classes = StereotypesHelper.getBaseClassesAsClasses((Stereotype) source);
                Class base = null;
                if (classes != null && !classes.isEmpty()) {
                    for (Class clazz : classes)
                        if (clazz.equals(source.getClassType()))
                            return base;
                    return classes.get(0);
                }
            }
            return ClassTypes.getClassType(getElementName2(source));
        }
        return null;
    }

    @Override
    public Set<String> getBaseClassNames(ConnectableElement source) {
        if (source == null)
            return null;
        Set<String> classNames = new HashSet<>();
        Stereotype st = getRepresentedStereotype(source);
        if (st != null)
            for (Class<?> cl : StereotypesHelper.getBaseClassesAsClasses(st))
                classNames.add(cl.getSimpleName());
        else
            classNames.add(getElementName3(source));
        return classNames;
    }

    @Override
    public Set<Class> getBaseClasses(ConnectableElement source) {
        if (source == null)
            return null;
        Set<Class> classNames = new HashSet<>();
        Stereotype st = getRepresentedStereotype(source);
        if (st != null)
            classNames.addAll(StereotypesHelper.getBaseClassesAsClasses(st));
        else
            classNames.add(getBaseClass(source));
        return classNames;
    }

    @Override
    public boolean isRelatingClassifier(Class<?> elementClass) {
        return Relationship.class.isAssignableFrom(elementClass) || Transition.class.isAssignableFrom(elementClass)
                || ActivityEdge.class.isAssignableFrom(elementClass);
    }

    @Override
    public boolean isActivityClassifier(Class<?> elementClass) {
        return ActivityPartition.class.isAssignableFrom(elementClass);
    }

    @Override
    public Stereotype getRepresentedStereotype(Element element) {
        if (element == null && !(element instanceof TypedElement))
            return null;
        TypedElement el = (TypedElement) element;
        if (el.getType() instanceof Stereotype)
            return (Stereotype) el.getType();
        if (!(el.getType() instanceof Stereotype) && el.getName() != null && el.getName().trim().length() > 0) {
            String name = getElementName2(el).replaceAll("^[_ ]*|[_ ]*$", "");
            return StereotypesHelper.getStereotype(Application.getInstance().getProject(), name);
        }
        return null;
    }

    @Override
    public String getProperName(Object element) {
        if (element == null && !(element instanceof NamedElement))
            return null;
        return getProperName(((NamedElement) element).getName());
    }
    
        /**
     * Get Transformation profile
     * @return MagicDraw Profile element for D&amp;D transformations
     */
    public Profile getTransformationProfile() {
        if (TRANSFORM_PROFILE == null) {
            Project project = Application.getInstance().getProject();
            TRANSFORM_PROFILE = StereotypesHelper.getProfile(project, TRANSFORMATION_PROFILE);
        }
        return TRANSFORM_PROFILE;
    }

    /**
     * Get DSL Customization profile
     * @return MagicDraw Profile element for DSL customizations
     */
    public Profile getDSLCustomizationProfile() {
        if (CUSTOM_PROFILE == null) {
            Project project = Application.getInstance().getProject();
            CUSTOM_PROFILE = StereotypesHelper.getProfile(project, DSL_CUSTOMIZATION_PROFILE);
        }
        return CUSTOM_PROFILE;
    }

    /**
     * Get UML metamodel with attributes profile
     * @return MagicDraw Profile element, representing UML metamodel with attributes
     */
    public Profile getUMLMetamodelProfile() {
        if (UML_PROFILE == null) {
            Project project = Application.getInstance().getProject();
            UML_PROFILE = StereotypesHelper.getProfile(project, UML_METAMODEL_WITH_ATTRIBUTES);
        }
        return UML_PROFILE;
    }
    
    /**
     * Get integration profile
     * @return MagicDraw Profile element, representing integration profile
     */
    public Profile getIntegrationProfile() {
        if (INT_PROFILE == null) {
            Project project = Application.getInstance().getProject();
            INT_PROFILE = StereotypesHelper.getProfile(project, INTEGRATION_PROFILE);
        }
        return INT_PROFILE;
    }

    /**
     * Get Integration stereotype
     * @return Stereotype Element, used to define integration between source and target elements
     */
    public Stereotype getIntegrationStereotype() {
        if (INTEGRATION_STEREOTYPE == null)
            INTEGRATION_STEREOTYPE = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    specConfig.getIntegrationStereotypeName(), getIntegrationProfile());
        return INTEGRATION_STEREOTYPE;
    }
    
        /**
     * Return an instance of {@value PatternParser#DRAGGED_ELEMENT} Stereotype, representing dragged element
     * @return An instance of Stereotype
     */
    @Override
    public Stereotype getDraggedElementStereotype() {
        if (DRAGGED_STEREOTYPE == null)
            DRAGGED_STEREOTYPE = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    patternConfig.getElementInFocusName(), getTransformationProfile());
        return DRAGGED_STEREOTYPE;
    }
    
     /**
     * Return an instance of {@value PatternParser#SOURCE_TYPE} Stereotype, representing source part of transformation pattern
     * @return An instance of Stereotype
     */
    public Stereotype getSourceStereotype() {
        if (SOURCE_ST == null) 
            SOURCE_ST = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    patternConfig.getSourceStereotypeName(), getTransformationProfile());
        return SOURCE_ST;
    }
    
     /**
     * Return an instance of {@value PatternParser#TARGET_TYPE} Stereotype, representing target part of transformation pattern
     * @return An instance of Stereotype
     */
    public Stereotype getTargetStereotype() {
        if (TARGET_ST == null) {
            Project project = Application.getInstance().getProject();
            TARGET_ST = StereotypesHelper.getStereotype(project, 
                    patternConfig.getTargetStereotypeName(), getTransformationProfile());
        }
        return TARGET_ST;
    }
    
     /**
     * Return an instance of {@value PatternParser#JOIN_TYPE} Stereotype, representing target part of transformation pattern
     * @return An instance of Stereotype
     */
    public Stereotype getJoinStereotype() {
        if (JOIN_ST == null) {
            Project project = Application.getInstance().getProject();
            JOIN_ST = StereotypesHelper.getStereotype(project, 
                    patternConfig.getJoinStereotypeName(), getTransformationProfile());
        }
        return JOIN_ST;
    }
    
         /**
     * Return an instance of {@value PatternParser#INTEGRATION_TAG} Stereotype, representing D&amp;D Connector
     * @return An instance of Stereotype
     */
    public Stereotype getDragAndDropConnectorStereotype() {
        if (DND_CONN_ST == null) 
            DND_CONN_ST = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    patternConfig.getTransformationConnectorName(), getTransformationProfile());
        return DND_CONN_ST;
    }
    
     /**
     * Returns an instance of a Stereotype with a name, returned by 
     * {@link MDSpecificationConfiguration.getTransformationStereotypeName()}
     * @return An instance of Stereotype
     */
    public Stereotype getDnDExtendedSpecificationStereotype() {
        if (dndstereotype == null)
            dndstereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    specConfig.getSpecificationStereotypeName(), getTransformationProfile());
        return dndstereotype;
    }

    /**
     * Returns an instance of a Stereotype with a name, returned by 
     * {@link MDSpecificationConfiguration.getCustomizationStereotypeName()}
     * @return An instance of Stereotype
     */
    public Stereotype getCustomizationStereotype() {
        if (custStereotype == null) 
            custStereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    specConfig.getCustomizationStereotypeName(), getDSLCustomizationProfile());
        return custStereotype;
    }

    /**
     * Returns an instance of a Stereotype with a name, returned by 
     * {@link MDSpecificationConfiguration.getTransformationPatternStereotypeName()}
     * @return An instance of Stereotype
     */
    public Stereotype getPatternStereotype() {
        if (patternStereotype == null) 
            patternStereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), 
                    specConfig.getTransformationPatternStereotypeName(), getTransformationProfile());
        return patternStereotype;
    }

    @Override
    public boolean isTypedElement(Element obj) {
        return obj instanceof TypedElement;
    }

    @Override
    public Element getTypeElement(Element element) {
        if (!(element instanceof TypedElement))
            return null;
        return ((TypedElement)element).getType();
    }

    @Override
    public boolean isElement(Object obj) {
        return obj instanceof Element;
    }

    @Override
    public Class<?> getClassType(Element element) {
        return element.getClassType();
    }

    @Override
    public boolean hasStereotype(Element element) {
        return StereotypesHelper.hasStereotype(element);
    }

    @Override
    public List<Stereotype> getStereotypes(Element element) {
        return StereotypesHelper.getStereotypes(element);
    }

    @Override
    public String getStereotypeName(Stereotype stereotype) {
        return stereotype.getName();
    }

    @Override
    public boolean hasTypeName(Element element, String typeName) {
        if (element instanceof TypedElement)
            return ((TypedElement) element).getType().getHumanType().compareTo(typeName) == 0;
        return false;
    }

    @Override
    public boolean hasStereotype(Element element, Stereotype stereotype) {
        return StereotypesHelper.hasStereotype(element, stereotype);
    }

    @Override
    public boolean isProperty(Element element) {
        return element instanceof Property;
    }

    @Override
    public Element setStereotypePropertyValue(Element element, Stereotype stereotype, String propName, Object propValue) {
        Property prop = StereotypesHelper.getPropertyByName(stereotype, propName);
        if (prop != null)
            StereotypesHelper.setStereotypePropertyValue(element, stereotype, propName, propValue);
        return element;
    }

    @Override
    public String getHumanName(Element element) {
        return element.getHumanName();
    }

    @Override
    public Object getStereotypePropertyValue(Element element, Stereotype stereotype, String propName) {
        Property prop = StereotypesHelper.getPropertyByName(stereotype, propName);
        if (prop != null)
            return StereotypesHelper.getStereotypePropertyValue(element, stereotype, propName);
        return null;
    }

    @Override
    public Element setName(Element element, String name) {
        if (element instanceof NamedElement)
            ((NamedElement)element).setName(name);
        return element;
    }

    @Override
    public String getActualName(Element element) {
        if (element == null || !(element instanceof NamedElement))
            return null;
        return ((NamedElement) element).getName();
    }

    @Override
    public Element getOwner(Element element) {
        return element.getOwner();
    }

    @Override
    public String getTypeName(Element element) {
        return element.getHumanType();
    }

    @Override
    public boolean isClassifier(Element element) {
        return element instanceof Classifier;
    }

    @Override
    public String getQualifiedName(Element element) {
        return element instanceof NamedElement ? ((NamedElement) element).getQualifiedName() : element.getHumanName();
    }

    @Override
    public boolean hasName(Element element) {
        return element instanceof NamedElement &&((NamedElement) element).getName() != null
                && ((NamedElement) element).getName().trim().length() > 0;
    }

    @Override
    public boolean canApplyStereotype(Element element, Stereotype stereotype) {
        return StereotypesHelper.canApplyStereotype(element, stereotype);
    }

    @Override
    public Element addStereotype(Element element, Stereotype stereotype) {
        StereotypesHelper.addStereotype(element, stereotype);
        return element;
    }

    @Override
    public boolean isNamedElement(Element element) {
        return element instanceof NamedElement;
    }

    @Override
    public Element getProjectModel() {
        Project proj = Application.getInstance().getProject();
        if (proj == null)
            return null;
        else
            return proj.getModel();
    }

    @Override
    public boolean isStereotype(Element element) {
        return element instanceof Stereotype;
    }

    @Override
    public Element getStereotypeProfile(Stereotype stereotype) {
        return stereotype.getProfile();
    }

    @Override
    public Stereotype getStereotypeByName(String name) {
        return StereotypesHelper.getStereotype(Application.getInstance().getProject(), name);
    }
    
    @Override
    public boolean mapsToElement(Element element, ConnectableEntity source) {
        if (source.getType() == null)
            return false;
        if (source.getType() instanceof Stereotype && element instanceof Stereotype) 
            return (source.getType().equals(element) || 
                    StereotypesHelper.getStereotypesHierarchy(element).contains((Stereotype) source.getType()));
        else if (source.getType() instanceof Stereotype && StereotypesHelper.hasStereotype(element))
            return (StereotypesHelper.getStereotypes(element).contains((Stereotype) source.getType()) || 
                    StereotypesHelper.getStereotypesHierarchy(element).contains((Stereotype) source.getType()));
        else if (!(source.getType() instanceof Stereotype) && !StereotypesHelper.hasStereotype(element)) {
            MagicDrawMapper mapper = MagicDrawMapper.getInstance();
            String sourceName2 = mapper.getProperName(source.getName());
            if (sourceName2 != null) {
                Project project = Application.getInstance().getProject();
                String sourceName = source.getProcessedName();
                Stereotype st;
                if (element instanceof Stereotype)
                    st = StereotypesHelper.getStereotype(project, sourceName, StereotypesHelper.findProfileForElement(element));
                else
                    st = StereotypesHelper.getStereotype(project, sourceName);
                if (st == null)
                    return false;
                if (element instanceof Stereotype) 
                    return element.equals(st) || StereotypesHelper.getStereotypesHierarchy(element).contains(st);     
                return StereotypesHelper.getStereotypes(element).contains(st) || 
                       StereotypesHelper.getStereotypesHierarchy(element).contains(st);
            } else {
                if (element instanceof Classifier && ((Classifier)element).getName().equals(source.getTypeName()))
                    return true;
                if (element.getHumanType().equals(source.getTypeName()))
                    return true;
                List<Class> types = ClassTypes.getSupertypes(element.getClassType());
                for (Class clazz : types)
                    if (element.getClassType().isAssignableFrom(clazz))
                        return true;
            }
        }
        return false;
    }

    @Override
    public List<Class> getBaseClassesAsClasses(Stereotype stereotype) {
        return StereotypesHelper.getBaseClassesAsClasses(stereotype);
    }

    @Override
    public String getID(Element element) {
        return element.getID();
    }

    @Override
    public Element getModelByElement(Element element) {
        return Project.getProject(element).getModel();
    }

    @Override
    public Class<?> getClassType(String className) {
        return ClassTypes.getClassType(className);
    }

    @Override
    public boolean isAbstractClassifier(Element element) {
        return isClassifier(element) && ((Classifier) element).isAbstract();
    }

    @Override
    public boolean isAssociation(Element element) {
        return element instanceof Association;
    }

    @Override
    public Collection<Element> getAssociationEndTypes(Element association) {
        Collection<Element> ends = new ArrayList<>();
        if (!isAssociation(association))
            return ends;
        for (Type type: ((Association) association).getEndType())
            ends.add(type);
        return ends;
    }

    @Override
    public boolean isElementPresentation(Object element) {
        return element instanceof PresentationElement;
    }

    @Override
    public Class<?> getActivityPartitionClass() {
        return ActivityPartition.class;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isRelationship(Class<?> elementClass) {
        return ModelHelper.isRelationship(elementClass);
    }

    @Override
    public Element getElementFromPresentation(Object presElement) {
        if (presElement instanceof PresentationElement)
            return ((PresentationElement)presElement).getElement();
        if (presElement instanceof DiagramPresentationElement)
            return ((DiagramPresentationElement)presElement).getDiagram();
        return null;
    }

    @Override
    public boolean isDiagram(Element element) {
        return element instanceof Diagram;
    }

    @Override
    public Stereotype getStereotype(String name, Element profile) {
        if (profile instanceof Profile)
            return StereotypesHelper.getStereotype(Application.getInstance().getProject(), name, (Profile)profile);
        return null;
    }

}
