package org.ktu.dndtransformations.testing;

import org.junit.Test;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.CentralBufferNode;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Signal;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdcollaborations.Collaboration;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Lifeline;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.tests.DragAndDropGenerationTestCase;
import org.ktu.dndtransformations.tests.TransformationTestData;
import org.ktu.dndtransformations.tests.TestParameterException;

public class SOAML_SBVR_BPMNSingleElementGenerationTest extends DragAndDropGenerationTestCase {

    private static Profile BPMN_profile, SBVR_profile, SoaML_profile;
    private static Project project;

    public SOAML_SBVR_BPMNSingleElementGenerationTest() {
        this.filename = "tests\\resources\\SOAML-SBVR-BPMN test.mdzip";
        project = null;
        BPMN_profile = null;
        SBVR_profile = null;
        SoaML_profile = null;
    }

    private Stereotype getBPMNStereotype(String stName) {
        if (project == null)
            project = Application.getInstance().getProject();
        if (BPMN_profile == null)
            BPMN_profile = StereotypesHelper.getProfile(project, "BPMN2 Profile");
        return StereotypesHelper.getStereotype(project, stName, BPMN_profile);
    }

    private Stereotype getSBVRStereotype(String stName) {
        if (project == null)
            project = Application.getInstance().getProject();
        if (SBVR_profile == null)
            SBVR_profile = StereotypesHelper.getProfile(project, "SBVR Profile");
        return StereotypesHelper.getStereotype(project, stName, SBVR_profile);
    }

    private Stereotype getSoaMLStereotype(String stName) {
        if (project == null)
            project = Application.getInstance().getProject();
        if (SoaML_profile == null)
            SoaML_profile = StereotypesHelper.getProfile(project, "SoaML Profile");
        return StereotypesHelper.getStereotype(project, stName, SoaML_profile);
    }

    /* Tests for SoaML element generation from BPMN elements */
    @Test
    public void testMessageType_DataObjectDragged() {
        Stereotype sourceSt = getBPMNStereotype("DataObject");
        Stereotype targetSt = getSoaMLStereotype("MessageType");
        try {
            testSingleElementCreation(new TransformationTestData("message type", CentralBufferNode.class, sourceSt, "contract",
                    null, "Create MessageType from DataObject", "target_MessageType", Signal.class, targetSt, "contract"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testMessageTypeWithCheckUniqueness_DataObjectDragged() {
        Stereotype sourceSt = getBPMNStereotype("DataObject");
        Stereotype targetSt = getSoaMLStereotype("MessageType");
        try {
            testSingleElementWithCheckUnique(new TransformationTestData("message type", CentralBufferNode.class, sourceSt, "contract",
                    null, "Create MessageType from DataObject", "target_MessageType", Signal.class, targetSt, "contract"), true, true);
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testMessageTypeWithoutCheckUniqueness_DataObjectDragged() {
        Stereotype sourceSt = getBPMNStereotype("DataObject");
        Stereotype targetSt = getSoaMLStereotype("MessageType");
        try {
            testSingleElementWithCheckUnique(new TransformationTestData("message type", CentralBufferNode.class, sourceSt, "contract",
                    null, "Create MessageType from DataObject", "target_MessageType", Signal.class, targetSt, "contract"), true, false);
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testParticipant_LaneDragged() {
        Stereotype sourceSt = getBPMNStereotype("Lane");
        Stereotype targetSt = getSoaMLStereotype("Participant");
        try {
            testSingleElementCreation(new TransformationTestData("services architecture", ActivityPartition.class,
                    sourceSt, "customer1", null, "Create Participant from Lane", "target_Participant",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, targetSt, "customer1"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testParticipant_LaneSetDragged() {
        Stereotype sourceSt = getBPMNStereotype("LaneSet");
        Stereotype targetSt = getSoaMLStereotype("Participant");
        try {
            testSingleElementCreation(new TransformationTestData("services architecture", ActivityPartition.class,
                    sourceSt, "customer", null, "Create Participant from LaneSet", "target_Participant",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, targetSt, "customer"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testLifeLine_LaneDragged() {
        Stereotype sourceSt = getBPMNStereotype("Lane");
        try {
            testSingleElementCreation(new TransformationTestData("service choreography", ActivityPartition.class, sourceSt, "customer1",
                    null, "Create LifeLine from Lane", "target_LifeLine", Lifeline.class, null, "customer1"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testLifeLine_LaneSetDragged() {
        Stereotype sourceSt = getBPMNStereotype("LaneSet");
        try {
            testSingleElementCreation(new TransformationTestData("service choreography", ActivityPartition.class, sourceSt, "customer",
                    null, "Create LifeLine from LaneSet", "target_LifeLine", Lifeline.class, null, "customer"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testMessageType_MessageFlowDragged() {
        Stereotype sourceSt = getBPMNStereotype("MessageFlow");
        Stereotype targetSt = getSoaMLStereotype("MessageType");
        try {
            testSingleElementCreation(new TransformationTestData("message type", Dependency.class, sourceSt, "flow",
                    null, "Create MessageType from MessageFlow", "target_MessageType", Signal.class, targetSt, "flow"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        } 
    }

    @Test
    public void testMessageType_MessageDragged() {
        Stereotype sourceSt = getBPMNStereotype("Message");
        Stereotype targetSt = getSoaMLStereotype("MessageType");
        try {
            testSingleElementCreation(new TransformationTestData("message type", CentralBufferNode.class, sourceSt, "Testing message",
                    null, "Create MessageType from Message", "target_MessageType", Signal.class, targetSt, "Testing message"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testServicesArchitecture_TaskDragged() {
        Stereotype sourceSt = getBPMNStereotype("Task");
        Stereotype targetSt = getSoaMLStereotype("ServicesArchitecture");
        try {
            testSingleElementCreation(new TransformationTestData("services architecture", OpaqueAction.class, sourceSt, "Create simple task",
                    null, "Create ServicesArchitecture from Task", "target_ServicesArchitecture", Collaboration.class, targetSt, "Create simple task"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testServiceContract_TaskDragged() {
        Stereotype sourceSt = getBPMNStereotype("Task");
        Stereotype targetSt = getSoaMLStereotype("ServiceContract");
        try {
            testSingleElementCreation(new TransformationTestData("service structure", OpaqueAction.class, sourceSt, "Create simple task",
                    null, "Create ServiceContract from Task", "target_ServiceContract", Collaboration.class, targetSt, "Create simple task"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    /* Tests for SoaML element generation from SBVR elements */
    @Test
    public void testMessageType_GeneralConceptDragged() {
        Stereotype sourceSt = getSBVRStereotype("general concept");
        Stereotype targetSt = getSoaMLStereotype("MessageType");
        try {
            testSingleElementCreation(new TransformationTestData("message type", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, sourceSt, "a",
                    null, "Create MessageType from GeneralConcept", "target_MessageType", Signal.class, targetSt, "a"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testLifeLine_GeneralConceptDragged() {
        Stereotype sourceSt = getSBVRStereotype("general concept");
        try {
            testSingleElementCreation(new TransformationTestData("service choreography", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, sourceSt, "a",
                    null, "Creat LifeLine from GeneralConcept", "target_LifeLine", Lifeline.class, null, "a"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SOAML_SBVR_BPMNSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        } 
    }
}
