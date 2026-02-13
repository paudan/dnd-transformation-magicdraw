package org.ktu.dndtransformations.tests;

import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;


public class TransformationTestData {
    
    public static class ElementData {
        public Class<?> elementClass;
        public Stereotype elementStereotype;
        public String elementName;
        
        public ElementData() {}

        public ElementData(Class<?> elementClass, Stereotype elementStereotype, String elementName) {
            this.elementClass = elementClass;
            this.elementStereotype = elementStereotype;
            this.elementName = elementName;
        }

    }
    
    public String diagramName;
    private ElementData sourceData;
    public String specPackage;
    public String dndSpecificationName;
    public String customizationName;
    private ElementData targetData;

    public TransformationTestData(String dndSpecificationName, String customizationName) {
        this.dndSpecificationName = dndSpecificationName;
        this.customizationName = customizationName;
        this.sourceData = new ElementData();
        this.targetData = new ElementData();
    }

    
    public TransformationTestData(Class<?> draggedClass, String draggedName, Class<?> targetClass, 
            String targetName, String dndSpecificationName, String customizationName) {
        this.sourceData = new ElementData(draggedClass, null, draggedName);
        this.dndSpecificationName = dndSpecificationName;
        this.customizationName = customizationName;
        this.targetData = new ElementData(targetClass, null, targetName);
    }

    public TransformationTestData(String diagramName, Class<?> draggedClass, Stereotype draggedStereotype, 
            String draggedName, String specPackage, String dndSpecificationName, String customizationName, 
            Class<?> targetClass, Stereotype targetStereotype, String targetName) {
        this.diagramName = diagramName;
        this.sourceData = new ElementData(draggedClass, draggedStereotype, draggedName);
        this.specPackage = specPackage;
        this.dndSpecificationName = dndSpecificationName;
        this.customizationName = customizationName;
        this.targetData = new ElementData(targetClass, targetStereotype, targetName);
    }
    
    public boolean isValid() {
        return sourceData.elementClass != null && targetData.elementClass != null 
                && dndSpecificationName != null & customizationName != null;
    }

    public Class<?> getSourceClass() {
        return sourceData.elementClass;
    }

    public void setSourceClass(Class<?> draggedClass) {
        this.sourceData.elementClass = draggedClass;
    }

    public Stereotype getSourceStereotype() {
        return sourceData.elementStereotype;
    }

    public void setSourceStereotype(Stereotype draggedStereotype) {
        this.sourceData.elementStereotype = draggedStereotype;
    }

    public String getSourceName() {
        return sourceData.elementName;
    }

    public void setSourceName(String draggedName) {
        this.sourceData.elementName = draggedName;
    }

    public Class<?> getTargetClass() {
        return targetData.elementClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetData.elementClass = targetClass;
    }

    public Stereotype getTargetStereotype() {
        return targetData.elementStereotype;
    }

    public void setTargetStereotype(Stereotype targetStereotype) {
        this.targetData.elementStereotype = targetStereotype;
    }

    public String getTargetName() {
        return targetData.elementName;
    }

    public void setTargetName(String targetName) {
        this.targetData.elementName = targetName;
    }
    
}
