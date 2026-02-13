package org.ktu.dndtransformations.parsers;

import java.util.Collection;
import java.util.List;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.StructuredClassifier;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.ResourceBundle;
import org.ktu.dndtransformations.impl.MagicDrawConfiguration;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.parsers.SpecificationConfiguration;

/**
 * Finds and reads given transformation specifications
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), 
 * Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
@SuppressWarnings("unchecked")
public class MDSpecificationReader implements SpecificationReader {

    private Element dndSpecElement, custElement;
    private Classifier relation;
    private boolean checkUnique, enabled;
    private Classifier source, target;
    private List<String> targetDiagrams;
    private List<Object> dndList;
    private String representation;
    private EnumerationLiteral integration;
    private Stereotype dndstereotype, custStereotype;
    private static SpecificationConfiguration config;
    private static MagicDrawMapper mapper = MagicDrawMapper.getInstance();
    
    private ResourceBundle bundle = ResourceBundle.getBundle("org/ktu/dndtransformations/parsers/messages");
    
    @Override
    public SpecificationConfiguration getSpecificationConfiguration() {
        if (config == null) 
            config = MagicDrawConfiguration.getSpecificationConfiguration();
        return config;
    }

    /**
     * Create new instance of {@link MDSpecificationReader}
     * @param dndSpecElement	Specification element (must be stereotyped with a 
     * stereotype represented by {@link SpecificationConfiguration.getTransformationStereotypeName()})
     * @throws Exception	The {@code dndSpecElement} is not stereotyped with a 
     * stereotype represented by {@link SpecificationConfiguration.getTransformationStereotypeName()}
     */
    public MDSpecificationReader(Element dndSpecElement) throws Exception {
        dndstereotype = mapper.getDnDExtendedSpecificationStereotype();
        custStereotype = mapper.getCustomizationStereotype();
        config = getSpecificationConfiguration();
        if (!StereotypesHelper.hasStereotype(dndSpecElement, dndstereotype))
            throw new Exception(String.format(bundle.getString("SpecificationReader.0"),
                    getSpecificationName(dndstereotype, dndSpecElement), config.getSpecificationStereotypeName()));
        this.dndSpecElement = dndSpecElement;
        checkUnique = true;
        enabled = true;
        parseSpecification();
    }

    /**
     * Create new instance of {@link MDSpecificationReader}
     * @param dndSpecElement	   Drag and Drop Specification element (must be 
     * stereotyped with a stereotype represented by {@link SpecificationConfiguration.getTransformationStereotypeName()})
     * @param targetCustElement	Specification element (must be stereotyped with a stereotype 
     *  represented by {@link SpecificationConfiguration.getCustomizationStereotypeName()})
     * @throws Exception	The {@code dndSpecElement} is not stereotyped with a stereotype 
     *  represented by {@link SpecificationConfiguration.getTransformationStereotypeName()},
     *  or {@code targetCustElement} is not stereotyped with {@value #CUSTOM_STEREOTYPE}
     */
    public MDSpecificationReader(Element dndSpecElement, Element targetCustElement) throws Exception {
        this(dndSpecElement);
        if (!StereotypesHelper.hasStereotype(targetCustElement, custStereotype))
            throw new Exception(String.format(bundle.getString("SpecificationReader.0"),
                    getSpecificationName(custStereotype, targetCustElement), config.getCustomizationStereotypeName()));
        this.custElement = targetCustElement;
        checkUnique = true;
        parseCustomization();
    }

    private void parseSpecification() {
        relation = (Classifier) StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, 
                dndstereotype, config.getRelationActionResultTagName());
        Boolean chkUnique = (Boolean) StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, 
                dndstereotype, config.getCheckUniquenessTagName());
        if (chkUnique != null)
            checkUnique = chkUnique;
        Boolean chkEnabled = (Boolean) StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, 
                dndstereotype, config.getEnabledTagName());
        if (chkEnabled != null)
            enabled = chkEnabled;
        source = (Classifier) StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, dndstereotype, config.getSourceElementTagName());
        targetDiagrams = StereotypesHelper.getStereotypePropertyValue(dndSpecElement, dndstereotype, config.getTargetDiagramTagName());
        Object repr = StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, dndstereotype, config.getRepresentationTextTagName());
        representation = repr != null ? repr.toString() : null;
        integration = (EnumerationLiteral) StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, 
                dndstereotype, config.getIntegrationActionResultTagName());
    }

    @SuppressWarnings("unchecked")
    private void parseCustomization() {
        dndList = StereotypesHelper.getStereotypePropertyValue(custElement, custStereotype, config.getAllowedTransformationsTagName());
        target = (Classifier) StereotypesHelper.getStereotypePropertyFirst(custElement, custStereotype, config.getCustomizationTargetTagName());
    }

    @Override
    public boolean isSourceStereotype() {
        return source instanceof Stereotype;
    }

    @Override
    public boolean hasTargetDiagram(String diagramName) {
        for (String diag : targetDiagrams)
            if (diag.equalsIgnoreCase(diagramName))
                return true;
        return false;
    }

    /**
     * Checks, if specification has diagram, specified by DiagramPresentationElement, among its target diagrams
     * @param presElem Element to be checked
     * @return {@code true} if {@code presElem} is in the list of target diagrams; {@code false} otherwise
     */
    public boolean hasTargetDiagram(DiagramPresentationElement presElem) {
        return hasTargetDiagram(presElem.getDiagramType().getType());
    }

    /**
     * Checks, if given specification has diagram, specified by DiagramPresentationElement, among its target diagrams
     * @param element	 Specification element (must be stereotyped 
     * with a stereotype represented by {@link SpecificationConfiguration.getTransformationStereotypeName()})
     * @param presElem Element to be checked
     * @return {@code true} if {@code element} is stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getTransformationStereotypeName()} 
     * and {@code presElem} is in the list of its target diagrams; {@code false} otherwise
     */
    public static boolean hasTargetDiagram(Element element, DiagramPresentationElement presElem) {
        Stereotype dnd = mapper.getDnDExtendedSpecificationStereotype();
        if (!StereotypesHelper.hasStereotype(element, dnd))
            return false;
        String targetTag = MagicDrawConfiguration.getSpecificationConfiguration().getTargetDiagramTagName();
        List<String> obj = StereotypesHelper.getStereotypePropertyValue(element, dnd, targetTag);
        if (obj.isEmpty())
            return false;
        String target = presElem.getDiagramType().getType();
        for (String diag : obj)
            if (diag.equalsIgnoreCase(target))
                return true;
        return false;
    }

    /**
     * Checks, if given specification contains tag {@value #SOURCE_ELEMENT_TAG}, whose value corresponds to the type of given element
     * @param element		 The element to be checked
     * @param specification	Specification element (must be stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getTransformationStereotypeName()})
     * @return	{@code true} if {@code specification} is stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getTransformationStereotypeName()} and 
     * type of {@code element} corresponds to the specification; {@code false} otherwise
     */
    @SuppressWarnings("rawtypes")
    public static boolean sourceConditionSatisfied(Element element, Element specification) {
        Stereotype dnd = mapper.getDnDExtendedSpecificationStereotype();
        if (!StereotypesHelper.hasStereotype(specification, dnd))
            return false;
        String sourceTag = MagicDrawConfiguration.getSpecificationConfiguration().getSourceElementTagName();
        Object value = StereotypesHelper.getStereotypePropertyFirst(specification, dnd, sourceTag);
        if (element == null && value == null && !(value instanceof Classifier))
            return false;
        if (value instanceof Stereotype && StereotypesHelper.hasStereotype(element)) {
            if (StereotypesHelper.getStereotypes(element).contains(value))
                return true;
            if (StereotypesHelper.getStereotypesHierarchy(element).contains(value))
                return true;
        } else if (!(value instanceof Stereotype) && !StereotypesHelper.hasStereotype(element)) {
            if (element.getHumanType().equals(((Classifier) value).getName()))
                return true;
            List<Class> types = ClassTypes.getSupertypes(element.getClassType());
            for (Class clazz : types)
                if (element.getClassType().isAssignableFrom(clazz))
                    return true;
        }
        return false;
    }

    /**
     * Checks, if given specification contains tag {@value #CUSTOMIZATION_TARGET_TAG}, which value corresponds to the type of given element
     * @param source		The element to be checked
     * @param customization	Specification element (must be stereotyped with a stereotype 
     *  represented by {@link SpecificationConfiguration.getCustomizationStereotypeName()})
     * @return	{@code true} if {@code customization} is stereotyped with a stereotype 
     *  represented by {@link SpecificationConfiguration.getCustomizationStereotypeName()} and 
     * type of {@code source} corresponds to the specification appropriate value; {@code false}, otherwise
     */
    public static boolean targetConditionSatisfied(Element source, Element customization) {
        Stereotype stereotype = mapper.getCustomizationStereotype();
        if (source == null || stereotype == null)
            return false;
        try {
            if (!StereotypesHelper.hasStereotype(customization, stereotype))
                return false;
        } catch (Exception ex) {
        }
        String tagName = MagicDrawConfiguration.getSpecificationConfiguration().getCustomizationTargetTagName();
        List<Classifier> object = StereotypesHelper.getStereotypePropertyValue(customization, stereotype, tagName);
        if (object.isEmpty())
            return false;
        List<Stereotype> sourceSt = StereotypesHelper.getStereotypes(source);
        if (object instanceof List)
            for (Classifier value : object) {
                boolean satisfies = value instanceof Stereotype ? sourceSt.contains(value) : source.getHumanType().equals(value.getName());
                if (satisfies)
                    return true;
            }
        else
            return object instanceof Classifier 
                && (object instanceof Stereotype ? StereotypesHelper.getStereotypes(source).contains(object)
                            : source.getHumanType().equals(((Classifier) object).getName()));
        return false;
    }

    private static String getSpecificationName(Stereotype dnd, Element dndSpecElement) {
        if (!StereotypesHelper.hasStereotype(dndSpecElement, dnd) || dnd.getName().length() + 1 > dndSpecElement.getHumanName().length())
            return null;
        return dndSpecElement.getHumanName().substring(dnd.getName().length() + 1, dndSpecElement.getHumanName().length());
    }

    @Override
    public String getSpecificationName() {
        return getSpecificationName(dndstereotype, dndSpecElement);
    }
    
    @Override
    public String getCustomizationName() {
        return getSpecificationName(custStereotype, custElement);
    }

    @Override
    public Element getTransformationSpecificationElement() {
        return dndSpecElement;
    }

    /**
     * Get drag and drop specification specification element
     * @param dndSpecElement	Specification element (must be stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getTransformationStereotypeName()})
     * @throws Exception	if {@code dndSpecElement} is not stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getTransformationStereotypeName()}
     */
    public void setDndSpecificationElement(Element dndSpecElement) throws Exception {
        String stName = getSpecificationConfiguration().getSpecificationStereotypeName();
        if (!StereotypesHelper.hasStereotype(dndSpecElement, dndstereotype))
            throw new Exception(String.format(bundle.getString("SpecificationReader.0"),
                    getSpecificationName(dndstereotype, dndSpecElement), stName));
        this.dndSpecElement = dndSpecElement;
        parseSpecification();
    }

    @Override
    public Element getCustomizationElement() {
        return custElement;
    }

    /**
     * Set current customization specification element
     * @param custElement	Specification element (must be stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getCustomizationStereotypeName()})
     * @throws Exception	if {@code custElement} is not stereotyped with a stereotype 
     * represented by {@link SpecificationConfiguration.getCustomizationStereotypeName()}
     */
    public void setCustomizationElement(Element custElement) throws Exception {
        if (custStereotype == null)
            custStereotype = mapper.getCustomizationStereotype();
        if (!StereotypesHelper.hasStereotype(custElement, custStereotype))
            throw new Exception(String.format(bundle.getString("SpecificationReader.0"),
                    getSpecificationName(custStereotype, custElement), 
                    getSpecificationConfiguration().getCustomizationStereotypeName()));
        this.custElement = custElement;
        parseCustomization();
    }

    /**
     * Get transformation pattern classifier
     * @return	A Classifier representing transformation pattern, or {@code null}, if such classifier is not defined in specification
     */
    @Override
    public StructuredClassifier getTransformationPattern() {
        Object pattern = StereotypesHelper.getStereotypePropertyFirst(dndSpecElement, dndstereotype, config.getTransformationPatternTagName());
        if (pattern != null)
            return (StructuredClassifier) PatternParserImpl.getPatternSpecificationElement(getTransformationPatternElements(), (Classifier) pattern);
        return null;
    }

    /**
     * Find matching element with a stereotype represented by {@link SpecificationConfiguration.getCustomizationStereotypeName()} 
     * for given D&amp;D specification element
     * @param specElement	Element representing D&amp;D specification
     * @param elements		The collection of elements to be searched
     * @return	The matching element, if one has been found; {@code null} otherwise
     */
    public static Element getCustomizationBySpecName(Element specElement, Collection<Element> elements) {
        Stereotype custStereotype = mapper.getCustomizationStereotype();
        Stereotype dndStereotype = mapper.getDnDExtendedSpecificationStereotype();
        String name = MDSpecificationReader.getSpecificationName(dndStereotype, specElement);
        String tagName = MagicDrawConfiguration.getSpecificationConfiguration().getAllowedTransformationsTagName();
        for (Element el : elements) {
            List<Classifier> values = StereotypesHelper.getStereotypePropertyValue(el, custStereotype, tagName);
            for (Classifier cf : values)
                if (name.compareTo(cf.getName()) == 0)
                    return el;
        }
        return null;
    }
    
    public static List<Element> getDnDSpecifications(Element custElement) {
        Stereotype custStereotype = mapper.getCustomizationStereotype();
        String tagName = MagicDrawConfiguration.getSpecificationConfiguration().getAllowedTransformationsTagName();
        List<Element> dnd = StereotypesHelper.getStereotypePropertyValue(custElement, custStereotype, tagName);
        return dnd;
    }

    /** Get the collection of transformation pattern elements (the search is performed in the Model of and active MagicDraw project) */
    static Collection<Element> getTransformationPatternElements() {
        return MagicDrawSearch.getInstance().findStereotypedElements(mapper.getPatternStereotype());
    }

    @Override
    public Classifier getRelationClassifier() {
        return relation;
    }

    @Override
    public boolean isCheckUnique() {
        return checkUnique;
    }

    @Override
    public Classifier getSourceClassifier() {
        return source;
    }

    @Override
    public Classifier getTargetClassifier() {
        return target;
    }

    @Override
    public List<String> getTargetDiagrams() {
        return targetDiagrams;
    }

    @Override
    public List<Object> getAllowedTransformationList() {
        return dndList;
    }

    @Override
    public String getRepresentationText() {
        return representation;
    }

    @Override
    public EnumerationLiteral getIntegrationType() {
        return integration;
    }

    @Override
    public int compareTo(SpecificationReader o) {
        return this.getTransformationSpecificationElement().compareTo(o.getTransformationSpecificationElement());
    }

    @Override
    public boolean getTransformationEnabled() {
        return enabled;
    }
    
}
