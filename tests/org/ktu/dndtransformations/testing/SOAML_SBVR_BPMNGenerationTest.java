package org.ktu.dndtransformations.testing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.dndtransformations.tests.DragAndDropGenerationTestCase;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.SymbolElementMap;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdcollaborations.Collaboration;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdcollaborations.CollaborationUse;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.tests.TransformationTestData;
import org.ktu.dndtransformations.tests.TestParameterException;

public class SOAML_SBVR_BPMNGenerationTest extends DragAndDropGenerationTestCase {

    private static Profile BPMN_profile, SBVR_profile, SoaML_profile, SOA_profile;
    private static Project project;

    public SOAML_SBVR_BPMNGenerationTest() {
        this.filename = "tests\\resources\\SOAML-SBVR-BPMN test.mdzip";
        project = null;
        BPMN_profile = null;
        SBVR_profile = null;
        SoaML_profile = null;
        SOA_profile = null;
    }

    private void initProject() {
        if (project == null)
            project = Application.getInstance().getProject();
        if (BPMN_profile == null)
            BPMN_profile = StereotypesHelper.getProfile(project, "BPMN2 Profile");
        if (SBVR_profile == null)
            SBVR_profile = StereotypesHelper.getProfile(project, "SBVR Profile");
        if (SoaML_profile == null)
            SoaML_profile = StereotypesHelper.getProfile(project, "SoaML Profile");
        if (SOA_profile == null)
            SOA_profile = StereotypesHelper.getProfile(project, "Cameo SOA+ Profile");
    }

    private Stereotype getBPMNStereotype(String stName) {
        initProject();
        return StereotypesHelper.getStereotype(project, stName, BPMN_profile);
    }

    private Stereotype getSBVRStereotype(String stName) {
        initProject();
        return StereotypesHelper.getStereotype(project, stName, SBVR_profile);
    }

    private Stereotype getSoaMLStereotype(String stName) {
        initProject();
        Stereotype st = StereotypesHelper.getStereotype(project, stName, SoaML_profile);
        if (st == null)
            st = StereotypesHelper.getStereotype(project, stName, SOA_profile);
        return st;
    }

    /* Tests for SoaML element generation from BPMN elements */
    @Test
    public void testServiceContract_ChoreographyTaskDragged() {
        initProject();
        if (project == null)
            project = Application.getInstance().getProject();
        Element dragged = search.findElementRecursively(project.getModel(), OpaqueAction.class, getBPMNStereotype("ChoreographyTask"), "ChorTask", true);
        Element diagram = ModelHelper.findInParent(project.getModel(), "service structure", Diagram.class, true);
        Element targetPackage = diagram.getOwner();
        SymbolElementMap map = project.getSymbolElementMap();
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) map.getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(null, "Create Service Contract with ConsumerPart and ProviderPart", "target_ServiceContract");
            Set<Object> generated = performDragAndDropTest(reader, diagramPres, dragged);
            assertEquals(1, generated.size());
            Element contract = search.findElement(targetPackage, Collaboration.class, getSoaMLStereotype("ServiceContract"), ((NamedElement) dragged).getName());
            assertNotNull(contract);
            Collaboration ce = (Collaboration) contract;
            assertEquals(4, ce.getOwnedAttribute().size());
            Stereotype stCons = getSoaMLStereotype("ConsumerPart");
            Stereotype stProv = getSoaMLStereotype("ProviderPart");
            boolean hasConsumer[] = {false, false}, hasProvider[] = {false, false};
            for (Property prop : ce.getOwnedAttribute())
                if (prop.getName().equals("participant1") && StereotypesHelper.hasStereotype(prop, stCons))
                    hasConsumer[0] = true;
                else if (prop.getName().equals("participant2") && StereotypesHelper.hasStereotype(prop, stCons))
                    hasConsumer[1] = true;
                else if (prop.getName().equals("participant1") && StereotypesHelper.hasStereotype(prop, stProv))
                    hasProvider[0] = true;
                else if (prop.getName().equals("participant2") && StereotypesHelper.hasStereotype(prop, stProv))
                    hasProvider[1] = true;
            assertTrue(hasConsumer[0] == true && hasConsumer[1] == true && Arrays.equals(hasConsumer, hasProvider));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    @Test
    public void testOntoServiceContract_ChoreographyTaskDragged() {
        initProject();
        if (project == null)
            project = Application.getInstance().getProject();
        Element target = search.findElementRecursively(project.getModel(), Collaboration.class, getSoaMLStereotype("ServicesArchitecture"), "provide order", true);
        SymbolElementMap map = project.getSymbolElementMap();
        PresentationElement elementOver = map.getPresentationElement(target);
        Element dragged = search.findElementRecursively(project.getModel(), OpaqueAction.class, getBPMNStereotype("ChoreographyTask"), "ChorTask", true);
        Element diagram = ModelHelper.findInParent(project.getModel(), "service structure", Diagram.class, true);
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) project.getSymbolElementMap().getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(null, "Create Participants and Service Contract", "target_ServicesArchitecture");
            performDragAndDropTest(reader, elementOver, dragged, diagramPres);
            Collaboration ce = (Collaboration) target;
            assertEquals(2, ce.getOwnedAttribute().size());
            Stereotype stParticipant = getSoaMLStereotype("ParticipantPart");
            Stereotype stContract = getSoaMLStereotype("SeviceContract");
            for (Property prop : ce.getOwnedAttribute())
                assertTrue(StereotypesHelper.hasStereotype(prop, stParticipant) && (prop.getName().equals("Participant1") || prop.getName().equals("Participant2")));
            Collection<CollaborationUse> uses = ce.getCollaborationUse();
            assertEquals(1, uses.size());
            boolean hasTarget = false;
            for (CollaborationUse use : uses)
                if (use.getName().equals(((NamedElement) dragged).getName()) && StereotypesHelper.hasStereotype(use, stContract)) {
                    hasTarget = true;
                    break;
                }
            assertTrue(hasTarget);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testOntoServiceContract_LaneDragged() {
        initProject();
        try {
            testPropertyCreation(new TransformationTestData("service structure", ActivityPartition.class,
                    getBPMNStereotype("Lane"), "customer1", null, "Create ConsumerPart from Lane", 
                    "target_ServiceContract", Collaboration.class, getSoaMLStereotype("ServiceContract"), 
                    "provide order"), getSoaMLStereotype("ConsumerPart"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testOntoServiceContract_LaneDragged2() {
        initProject();
        try {
            testPropertyCreation(new TransformationTestData("service structure", ActivityPartition.class, getBPMNStereotype("Lane"), 
                    "customer1", null, "Create ProviderPart from Lane", "target_ServiceContract", Collaboration.class, 
                    getSoaMLStereotype("ServiceContract"), "provide order"), getSoaMLStereotype("ProviderPart"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testOntoServiceContract_LaneSetDragged() {
        initProject();
        try {
            testPropertyCreation(new TransformationTestData("service structure", ActivityPartition.class,
                    getBPMNStereotype("LaneSet"), "customer", null, "Create ConsumerPart from LaneSet", 
                    "target_ServiceContract", Collaboration.class, getSoaMLStereotype("ServiceContract"),
                    "provide order"), getSoaMLStereotype("ConsumerPart"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        } 
    }

    @Test
    public void testOntoServiceContract_LaneSetDragged2() {
        initProject();
        try {
            testPropertyCreation(new TransformationTestData("service structure", ActivityPartition.class,
                    getBPMNStereotype("LaneSet"), "customer", null, "Create ProviderPart from LaneSet", 
                    "target_ServiceContract", Collaboration.class, getSoaMLStereotype("ServiceContract"),
                    "provide order"), getSoaMLStereotype("ProviderPart"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        } 
    }

    @Test
    public void testOntoMessageType_BusinessRuleDragged() {
        initProject();
        TransformationTestData data = new TransformationTestData(Constraint.class, "It is required that the size must be more than 3",
                DataType.class, "DataType Message", "Create Constraint for MessageType from BusinessRule", "target_MessageType");
        data.setSourceStereotype(getSBVRStereotype("structural business rule"));
        data.setTargetStereotype(getSoaMLStereotype("MessageType"));
        data.diagramName = "message type";
        try {
            testConstraintCreation(data, null); 
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testOntoMessageType_BusinessRuleDragged2() {
        initProject();
        TransformationTestData data = new TransformationTestData(Constraint.class, "It is required that the size must be more than 3", 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, "ClassType Message", 
                "Create Constraint for MessageType from BusinessRule", "target_MessageType");
        data.setSourceStereotype(getSBVRStereotype("structural business rule"));
        data.setTargetStereotype(getSoaMLStereotype("MessageType"));
        data.diagramName = "message type";
        try {
            testConstraintCreation(data, null);
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testOntoMessageType_GeneralConceptDragged() {
        initProject();
        try {
            testPropertyCreation(new TransformationTestData("message type", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), 
                    "a", null, "Create Attribute for MessageType fromGeneralConcept", "target_MessageType",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class,
                    getSoaMLStereotype("MessageType"), "ClassType Message"), null);
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }
    
    @Test
    public void testMessageTypes_AssociationDragged() {
        initProject();
        Class targetClass = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class;
        Stereotype messageTypeSt = getSoaMLStereotype("MessageType");
        List<TransformationTestData.ElementData[]> outputs = new ArrayList<>(2);
        outputs.add(new TransformationTestData.ElementData[] {new TransformationTestData.ElementData(targetClass, messageTypeSt, "a"), 
            new TransformationTestData.ElementData(targetClass, messageTypeSt, "b"), 
            new TransformationTestData.ElementData(Association.class, null, "uses")});
        Element root = project.getModel();
        Stereotype genSt = getSBVRStereotype("general concept");
        Stereotype assocSt = getSBVRStereotype("association");
        Element genCl1 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, genSt, "a", true);
        Element genCl2 = search.findElementRecursively(root, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, genSt, "b", true);
        Element dragged = search.findRelationship(Association.class, genCl1, genCl2, "uses", assocSt);
        TransformationTestData data = new TransformationTestData("Create 2 MessageTypes with Association from VerbConcept", "target_MessageTypeDiagram");
        data.diagramName = "message type";
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testMessageTypes_GeneralConceptDragged() {
        initProject();
        Class targetClass = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class;
        Stereotype messageTypeSt = getSoaMLStereotype("MessageType");
        List<TransformationTestData.ElementData[]> outputs = new ArrayList<>(2);
        outputs.add(new TransformationTestData.ElementData[] {new TransformationTestData.ElementData(targetClass, messageTypeSt, "a"), 
            new TransformationTestData.ElementData(targetClass, messageTypeSt, "b"), 
            new TransformationTestData.ElementData(Association.class, null, "uses")});
        outputs.add(new TransformationTestData.ElementData[] {new TransformationTestData.ElementData(targetClass, messageTypeSt, "a"), 
            new TransformationTestData.ElementData(targetClass, messageTypeSt, "c"), 
            new TransformationTestData.ElementData(Association.class, null, "uses")});
        TransformationTestData data = new TransformationTestData("Create MessageTypes with Association(s) from VerbConcept", "target_MessageTypeDiagram");
        data.diagramName = "message type";
        Element dragged = search.findElementRecursively(project.getModel(), 
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "a", true);
        try {
            testRelationshipCreation(dragged, data, outputs);  
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

}
