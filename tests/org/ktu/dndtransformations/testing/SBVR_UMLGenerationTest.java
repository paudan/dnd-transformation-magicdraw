package org.ktu.dndtransformations.testing;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.SymbolElementMap;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.mdusecases.Actor;
import com.nomagic.uml2.ext.magicdraw.mdusecases.UseCase;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.dndtransformations.tests.DragAndDropGenerationTestCase;
import org.ktu.dndtransformations.tests.TestParameterException;
import org.ktu.dndtransformations.tests.TransformationTestData;
import org.ktu.dndtransformations.tests.TransformationTestData.ElementData;

/**
 *
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class SBVR_UMLGenerationTest extends DragAndDropGenerationTestCase {
    
    private static Profile SBVR_profile;
    private static Project project;
    
    public SBVR_UMLGenerationTest() {
        this.filename = "tests\\resources\\Testing.mdzip";
        project = null;
        SBVR_profile = null;
    }

    private void initProject() {
        if (project == null)
            project = Application.getInstance().getProject();
        if (SBVR_profile == null)
            SBVR_profile = StereotypesHelper.getProfile(project, "SBVR Profile");
    }

    private Stereotype getSBVRStereotype(String stName) {
        initProject();
        return StereotypesHelper.getStereotype(project, stName, SBVR_profile);
    }
    
    @Test
    public void testState_CharacteristicDragged() {
        initProject();
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class draggedClass = 
                (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) search.findElement(project.getModel(), 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "rental");
        NamedElement dragged = null;
        for (Property prop: draggedClass.getAttribute())
            if (prop.getName() != null && prop.getName().equals("started")) {
                dragged = prop;
                break;
            }
        assertNotNull(dragged);
        TransformationTestData data = new TransformationTestData("CHAR_TO_STATE", "CHAR_STATE");
        data.specPackage = "CHARACTERISTIC TO STATE";
        data.setTargetClass(State.class);
        data.setTargetName(dragged.getName());
        try {
            testSingleElementCreation(dragged, data);
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    public void testUseCase_ElementDragged(String dndName, String custName, Element dragged, String[] genNames) {
        Element pkg = search.findPackageByName("Use Case example");
        Element diagram = ModelHelper.findInParent(pkg, "car rental system UCD", Diagram.class, true);
        SymbolElementMap map = project.getSymbolElementMap();
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) map.getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(null, dndName, custName);  
            Set<Object> generated = performDragAndDropTest(reader, diagramPres, dragged);
            assertEquals(genNames.length, generated.size());
            for (String name: genNames) {
                Element uc1 = search.findElement(pkg, UseCase.class, null, name);
                assertTrue(generated.contains(uc1));
                assertNotNull(map.getPresentationElement(uc1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testUseCase_GeneralConceptDragged() {
        initProject();
        Element dragged = search.findElementRecursively(project.getModel(), 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "a", true);
        testUseCase_ElementDragged("GENCONCEPT TO USECASE specification", 
                "GENCONCEPT TO USECASE customization", dragged, new String[] {"uses a", "uses1 a"});
    }
    
    @Test
    public void testUseCase_AssociationDragged() {
        initProject();
        Element root = project.getModel();
        Stereotype genSt = getSBVRStereotype("general concept");
        Element genCl1 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, genSt, "a", true);
        Element genCl2 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, genSt, "b", true);
        Element dragged = search.findRelationship(Association.class, genCl1, genCl2, "uses", getSBVRStereotype("association"));
        testUseCase_ElementDragged("VERBCONCEPT TO USECASE specification", 
                "VERBCONCEPT TO USECASE customization", dragged, new String[] {"uses a", "uses b"});
    }
    
    @Test
    public void testActorAndUseCase_GeneralConceptDragged() {
        initProject();
        List<ElementData[]> outputs = new ArrayList<>(2);
        outputs.add(new ElementData[] {new ElementData(Actor.class, null, "a"), 
            new ElementData(UseCase.class, null, "uses b"), new ElementData(Association.class, null, null)});
        outputs.add(new ElementData[] {new ElementData(Actor.class, null, "a"), 
            new ElementData(UseCase.class, null, "uses1 c"), new ElementData(Association.class, null, null)});
        TransformationTestData data = new TransformationTestData("GC TO USECASE specification", "VERBCONCEPT TO USECASE2 customization");
        data.specPackage = "Use Case example";
        data.diagramName = "car rental system UCD";
        Element dragged = search.findElementRecursively(project.getModel(), 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "a", true);
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testActorAndUseCase_AssociationDragged() {
        initProject();
        List<ElementData[]> outputs = new ArrayList<>(4);
        outputs.add(new ElementData[] {new ElementData(Actor.class, null, "a"), 
            new ElementData(UseCase.class, null, "uses a"), new ElementData(Association.class, null, null)});
        outputs.add(new ElementData[] {new ElementData(Actor.class, null, "a"), 
            new ElementData(UseCase.class, null, "uses b"), new ElementData(Association.class, null, null)});
        outputs.add(new ElementData[] {new ElementData(Actor.class, null, "b"), 
            new ElementData(UseCase.class, null, "uses a"), new ElementData(Association.class, null, null)});
        outputs.add(new ElementData[] {new ElementData(Actor.class, null, "b"), 
            new ElementData(UseCase.class, null, "uses b"), new ElementData(Association.class, null, null)});
        TransformationTestData data = new TransformationTestData("VERBCONCEPT TO USECASE2 specification", "VERBCONCEPT TO USECASE2 customization");
        data.specPackage = "Use Case example";
        data.diagramName = "car rental system UCD";
        Element root = project.getModel();
        Stereotype genSt = getSBVRStereotype("general concept");
        Element genCl1 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, genSt, "a", true);
        Element genCl2 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, genSt, "b", true);
        Element dragged = search.findRelationship(Association.class, genCl1, genCl2, "uses", getSBVRStereotype("association"));
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    public void testAssociation_AssociationDragged(String source1, String source2, String sourceAssocName, 
            Stereotype sourceAssocSt, Stereotype sourceClassSt,  String sourceDiagram, String specPkg, 
            String dndName, String customName, Stereotype targetAssocSt, Stereotype targetClassSt) {
        initProject();
        Element root = project.getModel();
        Class targetClass = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class;
        List<ElementData[]> outputs = new ArrayList<>(1);
        outputs.add(new ElementData[] {new ElementData(targetClass, targetClassSt, source1), 
            new ElementData(targetClass, targetClassSt, source2), new ElementData(Association.class, targetAssocSt, sourceAssocName)});
        Element genCl1 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, sourceClassSt, source1, true);
        Element genCl2 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, sourceClassSt, source2, true);
        Element dragged = search.findRelationship(Association.class, genCl1, genCl2, sourceAssocName, sourceAssocSt);
        TransformationTestData data = new TransformationTestData(dndName, customName);
        data.diagramName = sourceDiagram;
        data.specPackage = specPkg;
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testUMLAssociation_SBVRAssociationDragged() {
        testAssociation_AssociationDragged("a", "b", "uses", getSBVRStereotype("association"),
                getSBVRStereotype("general concept"), "Test vocabulary", "VERB TO ASSOCIATION", 
                "VC_TO_ASSOCIATION", "Association_VC", null, null);
    }
    
    @Test
    public void testSBVRAssociation_UMLAssociationDragged() {
        testAssociation_AssociationDragged("rental charge", "currency", "is converted to", null, null, 
                "Rental entity classes", "ASSOCIATION TO VERB", "ASSOCIATION_TO_VC", "Association_VC", 
                getSBVRStereotype("association"), getSBVRStereotype("general concept"));
    }
    
    @Test
    public void testUMLAssociation_GeneralConceptDragged() {
        initProject();
        Class targetClass = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class;
        List<ElementData[]> outputs = new ArrayList<>(2);
        outputs.add(new ElementData[] {new ElementData(targetClass, null, "a"), 
            new ElementData(targetClass, null, "b"), new ElementData(Association.class, null, "uses")});
        outputs.add(new ElementData[] {new ElementData(targetClass, null, "a"), 
            new ElementData(targetClass, null, "c"), new ElementData(Association.class, null, "uses1")});
        TransformationTestData data = new TransformationTestData("GEN_CONCEPT_TO_ASSOCIATION", "GeneralConcept_Association");
        data.diagramName = "Test vocabulary";
        Element dragged = search.findElementRecursively(project.getModel(), 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "a", true);
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testGeneralConceptAndIndividualConcept_ActorDragged() {
        initProject();
        Element root = project.getModel();
        Element dragged = search.findElementRecursively(root, Actor.class, null, "customer", true);
        Element diagram = ModelHelper.findInParent(root, "car rental system UCD", Diagram.class, true);
        SymbolElementMap map = project.getSymbolElementMap();
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) map.getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(null, "Actor to GC&IC", "Actor_general_ind_concepts");  
            Set<Object> generated = performDragAndDropTest(reader, diagramPres, dragged);
            assertEquals(2, generated.size());
            Stereotype genSt = getSBVRStereotype("general concept"); 
            Stereotype indSt = getSBVRStereotype("individual concept"); 
            boolean hasGen = false, hasInd = false;
            for (Object ee: generated) 
                if (ee instanceof Element) {
                    Element el = (Element) ee;
                    assertTrue(getProperName(((NamedElement) el).getName()).equals("customer"));
                    if (el.getClassType() == com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class) 
                        hasGen = StereotypesHelper.hasStereotype(el, genSt);
                    else if (el.getClassType() == InstanceSpecification.class) 
                        hasInd = StereotypesHelper.hasStereotype(el, indSt);
                    else
                        assertTrue(false);
                    assertNotNull(map.getPresentationElement(el));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testSBVRAssociation_UMLClassDragged() {
        initProject();
        Class targetClass = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class;
        Stereotype genSt = getSBVRStereotype("general concept");
        Stereotype assocSt = getSBVRStereotype("association");
        List<ElementData[]> outputs = new ArrayList<>(14);
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "branch_imported"),
            new ElementData(Association.class, assocSt, "has")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "bad experience"),
            new ElementData(Association.class, assocSt, "occurs during rental")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "renter_imported"),
            new ElementData(Association.class, assocSt, "is responsible for")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "rental price"),
            new ElementData(Association.class, assocSt, "honored_by")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "is open"),
            new ElementData(Association.class, assocSt, null)});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "car movement"),
            new ElementData(Association.class, assocSt, "includes")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "rental period"),
            new ElementData(Association.class, assocSt, "includes_imported")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "is returned"),
            new ElementData(Association.class, assocSt, null)});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "car group"),
            new ElementData(Association.class, assocSt, "has_imported5")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "incident"),
            new ElementData(Association.class, assocSt, "occurs during rental")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "car booking request"),
            new ElementData(Association.class, assocSt, "matches")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "date time"),
            new ElementData(Association.class, assocSt, "has_imported")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "date time"),
            new ElementData(Association.class, assocSt, "has_imported2")});
        outputs.add(new ElementData[] {new ElementData(targetClass, genSt, "rental"), 
            new ElementData(targetClass, genSt, "date time"),
            new ElementData(Association.class, assocSt, "has_imported3")});
        TransformationTestData data = new TransformationTestData("CLASS_TO_VC", "Class_VC");
        data.specPackage = "ASSOCIATION TO VERB";
        data.diagramName = "Rental entity classes";
        Element dragged = search.findElementRecursively(project.getModel(), 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "rental", true);
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    
}
