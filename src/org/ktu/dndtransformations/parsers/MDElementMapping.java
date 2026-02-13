package org.ktu.dndtransformations.parsers;

import org.ktu.transformations.parsers.ElementMapping;
import org.ktu.transformations.parsers.PropertyStack;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.PatternParser;

/**
 *
 * @author Admin
 */
public class MDElementMapping extends ElementMapping {
    
    /** The {@link Map} of integration type mappings */
    private Map<ConnectableEntity, EnumerationLiteral> integrationLiteralMap;
    
    public Map<PropertyStack, Map<PropertyStack, MDPropertyMapping>> propertyMap;
    
    /** Mappings for CONCAT connections */
    public MDConcatMap concatMap;

    public MDElementMapping(PatternParser owner) {
        super(owner); 
        propertyMap = new HashMap<>();
        concatMap = new MDConcatMap();
        integrationLiteralMap = new HashMap<>();
    }
    
    public MDElementMapping(ElementMapping base) {
        super(base);
        this.concatMap = new MDConcatMap(base.concatMap);
        this.propertyMap = new HashMap<>();
        for (PropertyStack key: base.propertyMap.keySet()) {
            Map<PropertyStack, MDPropertyMapping> pmap = new HashMap<>();
            for (PropertyStack pkey: base.propertyMap.get(key).keySet())
                pmap.put(pkey, new MDPropertyMapping(base.propertyMap.get(key).get(pkey)));
            this.propertyMap.put(key, pmap);
        }    
        this.integrationLiteralMap = new HashMap<>();
    }
    
    
    
    
    public static class MDPropertyMapping extends PropertyMapping {

        /** Integration type for this property */
        private EnumerationLiteral integrationLiteral;
        
        public MDPropertyMapping(PropertyStack sourceStack, PropertyStack targetStack) {
            super(sourceStack, targetStack);
        }
        
        public MDPropertyMapping(PropertyMapping base) {
            super(base);
        }

        /**
         * Return the EnumerationLiteral object, representing integration type for this property mapping
         * @return EnumerationLiteral representing integration type
         */
        public EnumerationLiteral getIntegrationLiteral() {
            return integrationLiteral;
        }

        public void setIntegrationLiteral(EnumerationLiteral integrationLiteral) {
            this.integrationLiteral = integrationLiteral;
        }
        
    }
    
    /**
     * Get the EnumerationLiteral, representing integration type for the {@link MDPropertyMapping} between given source and target property structures
     * @param source	The source property structure (i.e., structure, defined in source partition of the pattern)
     * @param target	The target property structure (i.e., structure, defined in target partition of the pattern)
     * @return EnumerationLiteral representing integration type
     */
    public EnumerationLiteral getIntegrationLiteral(PropertyStack source, PropertyStack target) {
        return propertyMap.get(source).get(target).getIntegrationLiteral();
    }
    
     /**
     * Add an integration type to the integration type mappings
     * @param element   The mapping element which should apply the integration with the given type
     * @param type      EnumerationLiteral representing the integration type
     */
    public void addIntegrationLiteral(ConnectableEntity element, EnumerationLiteral type) {
        integrationLiteralMap.put(element, type);
    }
    
    /**
     * Get the mappings for integration types
     * @return {@link Map} representing mappings with integration types 
     */
    public Map<ConnectableEntity, EnumerationLiteral> getIntegrationLiterals() {
        return Collections.unmodifiableMap(integrationLiteralMap);
    }
    
}
