package org.ktu.dndtransformations.testing;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.mdusecases.Actor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.ktu.dndtransformations.tests.DragAndDropGenerationTestCase;
import org.ktu.dndtransformations.tests.TestParameterException;
import org.ktu.dndtransformations.tests.TransformationTestData;

/**
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class SBVR_UMLSingleElementGenerationTest extends DragAndDropGenerationTestCase {
    
    private static Profile SBVR_profile;
    private static Project project;
    
    public SBVR_UMLSingleElementGenerationTest() {
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
    public void testClass_GeneralConceptDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Rental entity classes", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, 
                    getSBVRStereotype("general concept"), "a", "GC TO CLASS", "GC_TO_CLASS",  
                    "General Concept_Class", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, null, "a"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testDatatype_GeneralConceptDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Rental entity classes", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, 
                    getSBVRStereotype("general concept"), "a", "GC TO CLASS", "GC_TO_DATATYPE",  
                    "General Concept_DataType", DataType.class, null, "a"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testGeneralConcept_ClassDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Test vocabulary", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, 
                    null, "rental contract", "CLASS TO GC", "CLASS_TO_GC",  
                    "General Concept_Class", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, 
                    getSBVRStereotype("general concept"), "rental contract"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testGeneralConcept_DataTypeDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Test vocabulary", DataType.class, null, 
                    "datetime", "CLASS TO GC", "DATATYPE_TO_GC",  
                    "General Concept_Class", com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, 
                    getSBVRStereotype("general concept"), "datetime"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testGeneralConcept_BoundaryDragged1() {
        if (project == null)
            project = Application.getInstance().getProject();
        Stereotype st = StereotypesHelper.getStereotype(project, "UseCaseModel", StereotypesHelper.getProfile(project, "MagicDraw Profile"));
        try {
            testSingleElementCreation(new TransformationTestData("Test vocabulary", Model.class, st, 
                    "car rental system", "BOUNDARY TO GC", "BOUNDARY_TO_GC1",  "Package_General Concept", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "car rental system"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testGeneralConcept_BoundaryDragged2() {
        if (project == null)
            project = Application.getInstance().getProject();
        Stereotype st = StereotypesHelper.getStereotype(project, "Subsystem", StereotypesHelper.getProfile(project, "StandardProfile"));
        try {
            testSingleElementCreation(new TransformationTestData("Test vocabulary", Component.class, st, 
                    "Test", "BOUNDARY TO GC", "BOUNDARY_TO_GC2",  "Package_General Concept", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "Test"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
    
    @Test
    public void testGeneralConcept_BoundaryDragged3() {
        try {
            testSingleElementCreation(new TransformationTestData("Test vocabulary", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class, null, 
                    "Test2", "BOUNDARY TO GC", "BOUNDARY_TO_GC",  "Package_General Concept", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "Test2"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }

    @Test
    public void testGeneralConcept_ActorDragged() {
        try {
            testSingleElementCreation(new TransformationTestData("Test vocabulary", 
                    Actor.class, null, "customer", "ACTOR TO GC&IC", "ACTOR_TO_GC", "Actor_General Concept", 
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, getSBVRStereotype("general concept"), "customer"));
        } catch (TestParameterException ex) {
            Logger.getLogger(SBVR_UMLSingleElementGenerationTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            endTest();
        }
    }
}
