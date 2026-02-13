package org.ktu.dndtransformations.testing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.CentralBufferNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.ChangeEvent;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.mdusecases.Actor;
import com.nomagic.uml2.ext.magicdraw.mdusecases.UseCase;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ktu.dndtransformations.tests.TransformationTestData;
import org.ktu.dndtransformations.tests.TestParameterException;

@SuppressWarnings("deprecation")
public class BPMN_UMLGenerationTest extends DragAndDropGenerationTestCase {

    private static Profile BPMN_profile;
    private static Project project;

    public BPMN_UMLGenerationTest() {
        this.filename = "tests\\resources\\Testing2.mdzip";
        project = null;
        BPMN_profile = null;
    }

    private void initProject() {
        if (project == null)
            project = Application.getInstance().getProject();
        if (BPMN_profile == null)
            BPMN_profile = StereotypesHelper.getProfile(project, "BPMN2 Profile");
    }

    private Stereotype getBPMNStereotype(String stName) {
        initProject();
        return StereotypesHelper.getStereotype(project, stName, BPMN_profile);
    }

    /* Tests for UML element generation from BPMN elements */
    @Test
    public void testClass_DataObjectWithStateDragged() {
        try {
            Stereotype st = getBPMNStereotype("DataObject");
            Element el = testSingleElementCreation(new TransformationTestData("Testing class diagram", CentralBufferNode.class, st, "rental contract",
                    "DATAOBJ&STATE TO CLASS", "DATAOBJ&STATE TO CLASS specification", "DATAOBJ&STATE TO CLASS customization",
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "rental contract"));
            boolean found = false;
            for (Property prop : ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) el).getOwnedAttribute())
                if (prop.getName().equals("started"))
                    found = true;
            assertTrue(found);
        } catch (TestParameterException ex) {
            Logger.getLogger(BPMN_UMLGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    private Set<Object> testCreateUseCaseAndActor_LaneDragged(String diagramName, String draggedName,
            String specPackage, String dndSpecName, String customName, boolean findByName) {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), diagramName, Diagram.class, true);
        SymbolElementMap map = project.getSymbolElementMap();
        PresentationElement elementOver = map.getPresentationElement(diagram);
        Element targetPackage = diagram.getOwner();
        ActivityPartition dragged = search.findActivityPartitionElement(draggedName, "LaneSet", false);
        Collection<ActivityNode> tasks = dragged.getContainedNode();
        Iterator<ActivityNode> iter = tasks.iterator();
        while (iter.hasNext()) {
            ActivityNode current = iter.next();
            if (!(current instanceof OpaqueAction))
                tasks.remove(current);
        }
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(specPackage, dndSpecName, customName);
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            Element actor = search.findElement(targetPackage, Actor.class, null, draggedName);
            Set<UseCase> useCases = new HashSet<>();
            for (ActivityNode task : tasks) {
                Element el = ModelHelper.findInParent(targetPackage, task.getName(), UseCase.class, false);
                assertTrue(el != null && el instanceof UseCase);
                assertNotNull(map.getPresentationElement(el));
                useCases.add((UseCase)el);
            }
            assertNotNull(actor);
            assertNotNull(map.getPresentationElement(actor));
            assertTrue(!useCases.isEmpty());
            for (Object el : generated) {
                Association assoc = (Association) el;
                boolean condition = assoc.getEndType().contains(actor);
                boolean cond = false;
                for (UseCase usecase : useCases)
                    cond = cond || assoc.getEndType().contains(usecase);
                assertTrue(condition && cond);
                assertNotNull(map.getPresentationElement(assoc));
            }
            return generated;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    @Test
    public void testUseCaseAndActor_LaneDragged() {
        try {
            Set<Object> generated = testCreateUseCaseAndActor_LaneDragged("car rental system UCD", "rental manager's assistant",
                    "BPD TO UCD1", "BPM_LANE_TO_UCM", "Lane_UseCase", false);
            assertEquals(3, generated.size());
        } finally {
            endTest();
        }
    }

    @Test
    public void testUseCaseAndActorUnassociated_LaneDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "car rental system UCD", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        ActivityPartition dragged = search.findActivityPartitionElement("rental manager's assistant", "LaneSet", false);
        Collection<ActivityNode> tasks = dragged.getContainedNode();
        Iterator<ActivityNode> iter = tasks.iterator();
        while (iter.hasNext()) {
            ActivityNode current = iter.next();
            if (!(current instanceof OpaqueAction))
                tasks.remove(current);
        }
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("BPD TO UCD2", "BPM_LANE_TO_UCM2", "Lane_UseCase");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            for (Object el : generated) {
                if (el instanceof Actor)
                    assertTrue(getProperName(((NamedElement) el).getName()).equals("rental manager's assistant"));
                else {
                    assertTrue(el instanceof UseCase);
                    boolean cond = false;
                    for (ActivityNode task : tasks)
                        cond = cond || ((NamedElement) el).getName().equals(task.getName());
                    assertTrue(cond);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testUseCaseAndActor_LaneWithNameDragged() {
        try {
            Set<Object> generated = testCreateUseCaseAndActor_LaneDragged("car rental system UCD", "rental manager",
                "BPD TO UCD3", "BPM_LANE_TO_UCM_3", "Lane_UseCase_3", true);
            assertEquals(2, generated.size());	// Number of assocations generated
        } finally {
            endTest();
        } 
    }

    @Test
    public void testUseCaseAndActor_LaneWithNameDragged2() {
        try {
            Set<Object> generated = testCreateUseCaseAndActor_LaneDragged("car rental system UCD", "rental manager",
                    "BPD TO UCD5", "BPM_LANE_TO_UCM_5", "Lane_UseCase_5", true);
            assertEquals(3, generated.size());
        } finally {
            endTest();
        }
    }

    @Test
    public void testChangeEvent_TaskDragged() {
        initProject();
        State sourceSt = (State) ModelHelper.findInParent(project.getModel(), "test2", State.class, true);
        State targetSt = (State) ModelHelper.findInParent(project.getModel(), "started", State.class, true);
        Element target = SearchUtilities.findTransition(project.getModel(), sourceSt, targetSt);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(target);
        Element dragged = search.findElementRecursively(project.getModel(), OpaqueAction.class, getBPMNStereotype("Task"), "Create another rental contract", true);
        Element diagram = ModelHelper.findInParent(project.getModel(), "Testing statechart", Diagram.class, true);
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) project.getSymbolElementMap().getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("TASK TO CHANGEEVT", "TASK_CHANGEEVT", "TASK_CHANGEEVT customization");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged, diagramPres);
            assertEquals(1, generated.size());
            ChangeEvent el = (ChangeEvent) generated.toArray(new Element[]{})[0];
            assertEquals(el.getName().compareTo("Cr"), 0);
            assertEquals(((Expression) el.getChangeExpression()).getSymbol().compareTo("rental contract"), 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    /* Tests for BPMN element generation from UML elements */
    @Test
    public void testCreateLaneWithTasks_ActorDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "Case 4th", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        Element targetPackage = diagram.getOwner();
        Element dragged = ModelHelper.findInParent(project.getModel(), "customer", Actor.class, true);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("UCD TO BPD1", "UCD_TO_BPD_2", "Association_Lane");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            assertEquals(1, generated.size());
            testLaneCreation(generated, targetPackage, project, "customer",
                    new String[]{"Make car booking", "Confirm rental contract", "Provide additional personal data"},
                    StereotypesHelper.getStereotype(project, "Task", BPMN_profile), true, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    @Test
    public void testCreateLaneWithTasks_AssociationDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "Case 4th", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        Element targetPackage = diagram.getOwner();
        Element prop1 = ModelHelper.findInParent(project.getModel(), "customer", Actor.class, true);
        Element prop2 = ModelHelper.findInParent(project.getModel(), "Confirm rental contract", UseCase.class, true);
        Element dragged = search.findRelationship(Association.class, prop1, prop2, null, null);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("UCD TO BPD1", "UCD_TO_BPD_1", "Association_Lane");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            assertEquals(1, generated.size());
            testLaneCreation(generated, targetPackage, project, "customer", new String[]{"Confirm rental contract"},
                    StereotypesHelper.getStereotype(project, "Task", BPMN_profile), true, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    @Test
    public void testCreateLaneWithTasks_UseCaseDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "Case 4th", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        Element targetPackage = diagram.getOwner();
        Element dragged = ModelHelper.findInParent(project.getModel(), "Make car booking", UseCase.class, true);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("UCD TO BPD1", "UCD_TO_BPD_3", "Association_Lane");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            assertEquals(1, generated.size());
            testLaneCreation(generated, targetPackage, project, "customer", new String[]{"Make car booking"},
                    StereotypesHelper.getStereotype(project, "Task", BPMN_profile), true, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testCreateLaneWithTasksAndName_ActorDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "Case 4th", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        Element targetPackage = diagram.getOwner();
        Element dragged = ModelHelper.findInParent(project.getModel(), "customer", Actor.class, true);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("UCD TO BPD2", "UCD_TO_BPD_2", "Association_Lane");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            assertEquals(1, generated.size());
            testLaneCreation(generated, targetPackage, project, "customer",
                    new String[]{"Make car booking", "Confirm rental contract", "Provide additional personal data"},
                    StereotypesHelper.getStereotype(project, "Task", BPMN_profile), false, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testCreateLaneWithTasksAndName_AssociationDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "Case 4th", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        Element targetPackage = diagram.getOwner();
        Element prop1 = ModelHelper.findInParent(project.getModel(), "customer", Actor.class, true);
        Element prop2 = ModelHelper.findInParent(project.getModel(), "Confirm rental contract", UseCase.class, true);
        Element dragged = search.findRelationship(Association.class, prop1, prop2, null, null);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("UCD TO BPD2", "UCD_TO_BPD_1", "Association_Lane");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            assertEquals(1, generated.size());
            testLaneCreation(generated, targetPackage, project, "customer", new String[]{"Confirm rental contract"},
                    StereotypesHelper.getStereotype(project, "Task", BPMN_profile), false, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           endTest(); 
        }
    }

    @Test
    public void testCreateResourceAndPool_ActorDragged() {
        initProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), "Case 4th", Diagram.class, true);
        PresentationElement elementOver = project.getSymbolElementMap().getPresentationElement(diagram);
        Element dragged = ModelHelper.findInParent(project.getModel(), "customer", Actor.class, true);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader("ACTOR TO RESOURCE&POOL", "ACTOR_RESOURCE&POOL", "ACTOR TO RESOURCE&POOL customization");
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged);
            assertEquals(1, generated.size());
            Element el = generated.toArray(new Element[]{})[0];
            assertTrue(el instanceof ActivityPartition && StereotypesHelper.hasStereotype(el, StereotypesHelper.getStereotype(project, "Lane", BPMN_profile)));
            ActivityPartition part = (ActivityPartition) el;
            assertTrue(part.getRepresents() != null);
            assertTrue(StereotypesHelper.hasStereotype(part.getRepresents(), StereotypesHelper.getStereotype(project, "Resource", BPMN_profile)));
            PresentationElement symbol = project.getSymbolElementMap().getPresentationElement(el);
            assertNotNull(symbol);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTest();  
        } 
    }
}
