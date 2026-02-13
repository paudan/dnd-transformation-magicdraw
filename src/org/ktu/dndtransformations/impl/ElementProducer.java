package org.ktu.dndtransformations.impl;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.uml.ModelElementWrapper;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.AbstractMap.SimpleImmutableEntry;
import org.ktu.dndtransformations.parsers.MDElementMapping;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.dndtransformations.parsers.PatternParserImpl;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.notifiers.NotificationObservable;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ConnectorEntity;
import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.SpecificationConfiguration;
import org.ktu.transformations.parsers.SpecificationReader;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;
import org.ktu.transformations.transforms.TransformationManager;

/**
 *
 * @author Admin
 */
@SuppressWarnings({"deprecation"})
public class ElementProducer extends AbstractElementProducer<Element, Stereotype> implements NotificationObservable {
    
    private static ElementProducer INSTANCE;

    private ElementProducer(MagicDrawMapper mapper, AbstractPropertyManager<Element, Stereotype, ?> propManager) {
        super(mapper, propManager);
        Project proj = Application.getInstance().getProject();
        if (proj == null)
            this.targetPackage = null;
        else
            this.targetPackage = proj.getModel();
    }
    
    public static ElementProducer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ElementProducer(MagicDrawMapper.getInstance(), PropertyManager.getInstance());
        return INSTANCE;
    }
    
    @Override
    protected Element getTargetPackage() {
        if (targetPackage == null) 
            setTargetPackage(mapper.getProjectModel());
        return targetPackage;
    }
    
    /**
     * Create integration relationship between source and target elements
     * @param client	The client element of the relationship (the target Element)
     * @param supplier	The supplier element of the relationship (the source Element)
     * @param type	Instance of EnumerationLiteral which is required to create an integration between {@code client} and {@code supplier}
     * It should correspond to integration type, retrieved from specification (usually obtained by calling {@link SpecificationReader#getIntegrationType}
     */
    public void createIntegration(Element client, Element supplier, EnumerationLiteral type) {
        if (type == null)
            return;
        if (type.getName().equals(SpecificationReader.IntegrationType.NONE.getName()))
            return;
        Project project = Application.getInstance().getProject();
        if (project == null)
            return;
        Abstraction abstraction = project.getElementsFactory().createAbstractionInstance();
        Stereotype st = ((MagicDrawMapper)mapper).getIntegrationStereotype();
        StereotypesHelper.addStereotype(abstraction, st);
        StereotypesHelper.createDefaultValues(abstraction, st, true);
        abstraction.setOwner(project.getModel());
        ModelHelper.setClientElement(abstraction, client);
        ModelHelper.setSupplierElement(abstraction, supplier);
        SpecificationConfiguration config = MagicDrawConfiguration.getSpecificationConfiguration();
        StereotypesHelper.setStereotypePropertyValue(abstraction, st, config.getIntegrationSourceTagName(), mapper.getElementName2(client));
        StereotypesHelper.setStereotypePropertyValue(abstraction, st, config.getIntegrationTargetTagName(), mapper.getElementName2(supplier));
        StereotypesHelper.setStereotypePropertyValue(abstraction, st, config.getIntegrationTypeTagName(), type);
        sendNotification(new Object[] {abstraction}, String.format("Generated integration between %s and %s of type %s",
                client.getHumanName(), supplier.getHumanName(), type.getName()), NotificationType.INFO);
    }
    
    private static EnumerationLiteral getIntegrationTypeElement(IntegrationType type, Enumeration enumeration) {
        if (enumeration == null)
            return null;
        for (EnumerationLiteral literal : enumeration.getOwnedLiteral())
            if (literal.getName().equals(type.getName()))
                return literal;
        return null;
    }

    /**
     * Get integration type, which should be applied for integration relationship between source and target elements
     * @param specReader {@link MDSpecificationReader} object, which performs processing of transformation specification
     * @param mapping    {@link MDElementMapping} object which contains the mapping information
     * @param targetEl   Mapping element, which is the target element, which would be obtained after transformation using {@code mapping}
     * @return UML EnumerationLiteral object, representing the integration type
     */
    public EnumerationLiteral getIntegrationType(MDSpecificationReader specReader, MDElementMapping mapping, ConnectableEntity targetEl) {
        EnumerationLiteral defaultType = specReader.getIntegrationType();
        EnumerationLiteral connIntType = mapping.getIntegrationLiterals().get(targetEl);
        MDElementMapping targetMapping = (MDElementMapping) mapping.getParser().getTargetMappings().get(targetEl);
        boolean hasConcat = targetMapping != null ? !targetMapping.concatMap.isEmpty() : false;
        if (connIntType == null) {
            if (hasConcat) 
                return targetMapping.concatMap.getIntegrationType(targetEl);
            else if (defaultType == null || defaultType.getName().equals(IntegrationType.NONE.getName()))
                return null;
            else
                return defaultType;
        } else {
            if (connIntType.getName().equals(IntegrationType.NONE.getName()))
                return null;
            else if (connIntType.getName().equals(IntegrationType.DEFAULT.getName())) {
                if (mapping.getNamingRules().get(targetEl) == null && !hasConcat)
                    return getIntegrationTypeElement(IntegrationType.FULL, connIntType.getEnumeration());
                else if (hasConcat)
                    return targetMapping.concatMap.getIntegrationType(targetEl);
                else
                    return getIntegrationTypeElement(IntegrationType.PARTIAL, connIntType.getEnumeration());
            } else
                return connIntType;
        }
    }
    
    @Override
    protected void createIntegration(Element srcObj, Element newel, SimpleImmutableEntry<IntegrationType, ConnectorEntity> typeRes, 
            ElementMapping ms, ConnectableEntity targetEl) {
        MDSpecificationReader reader = (MDSpecificationReader) TransformationManager.getInstance().getCurrentReader();
        if (typeRes == null) {
            if (ms instanceof MDElementMapping)
                createIntegration(newel, srcObj, getIntegrationType(reader, (MDElementMapping) ms, targetEl));
            return;
        }
        EnumerationLiteral type = getIntegrationType(reader, (MDElementMapping) ms, targetEl);
        EnumerationLiteral literal = PatternParserImpl.getIntegrationLiteral((Connector) typeRes.getValue().getConnectorObject());
        if (literal != null) {
            EnumerationLiteral calculatedType = getIntegrationTypeElement(typeRes.getKey(), literal.getEnumeration());
            if (type != null && calculatedType == null && newel != null) {
                createIntegration(newel, srcObj, type);
            } else if (newel != null && calculatedType != null) 
                createIntegration(newel, srcObj, calculatedType);
        }
    }
    
    @Override
    protected Element createElementInstance(Class<?> classType, Element owner) {
        ModelElementWrapper wrapper = new ModelElementWrapper(classType);
        return wrapper.createInstance(owner);
    }

    @Override
    public Element createElementCopy(Element element, Element parent) {
        return CopyPasting.copyPasteElement(element, parent);
    }

    @Override
    public void createIntegration(Element client, Element supplier, IntegrationType type) {
    }

    @Override
    public void removeElement(Element element) {
        try {
            ModelElementsManager.getInstance().removeElement(element);
        } catch (ReadOnlyElementException e1) {
        }
    }

    @Override
    public void setAssociationNavigable(Element element) {
        if (!(element instanceof Association))
            return;
        Association assoc = (Association) element;
        for (Property end: assoc.getMemberEnd())
            ModelHelper.setNavigable(end, true);
    }

    @Override
    public void setClientElement(Element relation, Element client) {
        ModelHelper.setClientElement(relation, client);
    }

    @Override
    public void setSupplierElement(Element relation, Element supplier) {
        ModelHelper.setClientElement(relation, supplier);
    }
    
}
