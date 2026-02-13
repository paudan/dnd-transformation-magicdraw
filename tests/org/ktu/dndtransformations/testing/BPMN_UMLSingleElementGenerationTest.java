package org.ktu.dndtransformations.testing;

import org.junit.Test;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.CentralBufferNode;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.mdusecases.Actor;
import com.nomagic.uml2.ext.magicdraw.mdusecases.UseCase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.tests.DragAndDropGenerationTestCase;
import org.ktu.dndtransformations.tests.TransformationTestData;
import org.ktu.dndtransformations.tests.TestParameterException;

public class BPMN_UMLSingleElementGenerationTest extends DragAndDropGenerationTestCase {

    private static Profile BPMN_profile;
    private static Project project;

    public BPMN_UMLSingleElementGenerationTest() {
        this.filename = "tests\\resources\\Testing2.mdzip";
        project = null;
        BPMN_profile = null;
    }

    private Stereotype getBPMNStereotype(String stName) {
        if (project == null)
            project = Application.getInstance().getProject();
        if (BPMN_profile == null)
            BPMN_profile = StereotypesHelper.getProfile(project, "BPMN2 Profile");
        return StereotypesHelper.getStereotype(project, stName, BPMN_profile);
    }

    /* Tests for UML element generation from BPMN elements */
    @Test
    public void testClass_DataObjectDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Testing class diagram", CentralBufferNode.class, 
                    getBPMNStereotype("DataObject"), "rental manager", "DATAOBJ TO CLASS", "DATAOBJ_TO_CLASS", 
                    "DataObject_Class", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "rental manager"));
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testClassWithCheckUniqueness_DataObjectDragged() {
        Stereotype st = getBPMNStereotype("DataObject");
        try {
            testSingleElementWithCheckUnique(new TransformationTestData("Testing class diagram", CentralBufferNode.class, st, "rental manager",
                    "DATAOBJ TO CLASS", "DATAOBJ_TO_CLASS", "DataObject_Class", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class,
                    null, "rental manager"), true, true);
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testClassWithoutCheckUniqueness_DataObjectDragged() {
        try {
            testSingleElementWithCheckUnique(new TransformationTestData("Testing class diagram", CentralBufferNode.class, getBPMNStereotype("DataObject"),
                    "rental manager", "DATAOBJ TO CLASS", "DATAOBJ_TO_CLASS", "DataObject_Class",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "rental manager"), true, false);
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testClass_BPMNActivityDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Testing class diagram", OpaqueAction.class, 
                    getBPMNStereotype("Task"), "Create another rental contract", "BPMNACTIVITY TO CLASS", 
                    "BPMNACTIVITY TO CLASS specification", "BPMNACTIVITY TO CLASS customization",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "another rental contract"));
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testExistingClass_BPMNActivityDragged() {
        try {
            Stereotype sourceSt = getBPMNStereotype("Task");
            testSingleElementCreation(new TransformationTestData("Testing class diagram", OpaqueAction.class, sourceSt, "Create another rental contract",
                    "BPMNACTIVITY TO CLASS", "BPMNACTIVITY TO CLASS specification", "BPMNACTIVITY TO CLASS customization",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "another rental contract"));
            // Drag the same element in order to generate the same element as previously
            testExistingSingleElementCreation(new TransformationTestData("Testing class diagram", OpaqueAction.class, sourceSt, "Create another rental contract",
                    "BPMNACTIVITY TO CLASS", "BPMNACTIVITY TO CLASS specification", "BPMNACTIVITY TO CLASS customization",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "another rental contract"));
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testUseCase_TaskDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("car rental system UCD", OpaqueAction.class, 
                    getBPMNStereotype("Task"), "Create another rental contract", "TASK TO USECASE", 
                    "TASK TO USECASE specification", "TASK TO USECASE customization", UseCase.class, null, "Create another rental contract"));
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        } 
    }

    /* Tests for BPMN element generation from UML elements */
    @Test
    public void testCreateLane_ActorDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Case 4th", Actor.class, null, 
                    "customer", "ACTOR TO LANE", "ACTOR TO LANE specification",
                    "ACTOR TO LANE customization", ActivityPartition.class, getBPMNStereotype("Lane"), "customer"));
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testCreateResource_ActorDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Case 4th", Actor.class, null, "customer",
                    "ACTOR TO RESOURCE", "ACTOR_TO_RES", "Actor_Resource", Actor.class, getBPMNStereotype("Resource"), "customer"));
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }
}
