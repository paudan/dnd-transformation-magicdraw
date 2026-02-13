package org.ktu.dndtransformations.tests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.tests.MagicDrawTestCase;
import com.nomagic.magicdraw.uml.ElementFinder;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.SymbolElementMap;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.List;
import java.util.Objects;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.apache.log4j.Logger;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.dndtransformations.tests.TransformationTestData.ElementData;
import static org.ktu.dndtransformations.ui.PatternDragAndDropHandler.getTransformerInstance;
import org.ktu.transformations.transforms.rendered.RenderedGenerator;


@SuppressWarnings("deprecation")
public class DragAndDropGenerationTestCase extends MagicDrawTestCase {

    protected static Project project = null;
    protected String filename;
    protected SessionManager sessionManager = SessionManager.getInstance();
    protected MagicDrawSearch search = MagicDrawSearch.getInstance();

    public DragAndDropGenerationTestCase() {
    }

    @Override
    protected void setUpTest() throws Exception {
        super.setUpTest();
        setSkipMemoryTest(true);
        setMemoryTestReady(false);
        if (filename == null)
            throw new IOException("Filename of testing project is not specified!");
        if (filename != null && (project == null || !project.isLoaded()))
            project = openProject(Paths.get(filename).normalize().toUri().getPath());
        if (project == null || !project.isLoaded())
            throw new IOException("File " + filename + " was not opened or could not be found!");
        if (sessionManager.isSessionCreated())
            sessionManager.closeSession();
        sessionManager.createSession("Perform tests");
    }

    protected void endTest() {
        sessionManager.cancelSession();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDownTest() throws Exception {
        super.tearDownTest();
    }
    
    protected String getProperName(String name) {
        if (name == null || name.trim().length() == 0)
            return null;
        return name.replaceAll("\n", " ").replaceAll("  ", " ").trim();
    }

    protected MDSpecificationReader getSpecificationReader(String packageName, String dndName, String customName) throws Exception {
        Element mainPkg = Application.getInstance().getProject().getModel();
        Element patternPkg = packageName != null && packageName.trim().length() > 0 ? search.findPackageByName(packageName) : mainPkg;
        Element dndElement = ElementFinder.find(patternPkg, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, dndName, true);
        if (dndElement == null && patternPkg != mainPkg)
            dndElement = ElementFinder.find(mainPkg, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, dndName, true);
        Element customElement = ElementFinder.find(patternPkg, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, customName, true);
        if (customElement == null && patternPkg != mainPkg)
            customElement = ElementFinder.find(mainPkg, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, customName, true);
        assertNotNull(dndElement);
        assertNotNull(customElement);
        return new MDSpecificationReader(dndElement, customElement);
    }

    protected MDSpecificationReader getSpecificationReader(String packageName, String dndName, String customName,
            boolean forceCheckUnique, Boolean setCheckUnique) throws Exception {
        MDSpecificationReader reader = this.getSpecificationReader(packageName, dndName, customName);
        if (forceCheckUnique && setCheckUnique != null) {
            Field field = MDSpecificationReader.class.getDeclaredField("checkUnique");
            field.setAccessible(true);
            field.set(reader, setCheckUnique);
        }
        return reader;
    }
    
    protected MDSpecificationReader getSpecificationReader(TransformationTestData data) throws Exception {
        return getSpecificationReader(data.specPackage, data.dndSpecificationName, data.customizationName);
    }
    
    protected MDSpecificationReader getSpecificationReader(TransformationTestData data, boolean forceCheckUnique, Boolean setCheckUnique) throws Exception {
        return getSpecificationReader(data.specPackage, data.dndSpecificationName, data.customizationName, forceCheckUnique, setCheckUnique);
    }

    /* Should be used when drag and drop is performed on element, other than Diagram */
    protected Set<Object> performDragAndDropTest(MDSpecificationReader reader, PresentationElement elementOver, Element dragged, DiagramPresentationElement diagram) {
        try {
            RenderedGenerator<Element, Stereotype, ConnectableElement, PresentationElement> transformer = getTransformerInstance();
            transformer.setSpecificationReader(reader);
            return transformer.generate(dragged, diagram.getElement().getOwner(), diagram, elementOver, null);
        } catch (InvalidPatternException | ElementGenerationException e1) {
            Logger.getLogger(getClass()).error(e1);
            System.err.println("Error while loading reader: " + e1.getMessage());
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e);
            System.err.println("Error while loading reader: " + e.getMessage());
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    /* Should be used only when drag and drop is performed on Diagram element */
    protected Set<Object> performDragAndDropTest(MDSpecificationReader reader, PresentationElement elementOver, Element dragged) {
        return this.performDragAndDropTest(reader, elementOver, dragged, (DiagramPresentationElement) elementOver);
    }

    protected void testLaneCreation(Set<Object> generated, Element targetPackage, Project project, String name,
            String[] taskNames, Stereotype taskSt, boolean setResource, boolean setName) {
        if (generated != null)
            for (Object el : generated) {
                if (setName) {
                    assertTrue(el instanceof ActivityPartition);
                    assertNotNull(name);
                    if (!(el instanceof ActivityPartition) && el instanceof NamedElement)
                        assertTrue(getProperName(((NamedElement) el).getName()).equals(name));
                }
                assertEquals(((Element)el).getOwner(), targetPackage);
                PresentationElement symbol = project.getSymbolElementMap().getPresentationElement((Element) el);
                assertNotNull(symbol);
                if (el instanceof ActivityPartition) {
                    ActivityPartition part = (ActivityPartition) el;
                    assertEquals(part.getContainedNode().size(), taskNames.length);
                    if (setResource) {
                        assertNotNull(part.getRepresents());
                        assertTrue(((NamedElement) part.getRepresents()).getName().equals(name));
                    }
                    for (String taskName : taskNames) {
                        Element task1 = ModelHelper.findInParent(targetPackage, taskName, OpaqueAction.class, true);
                        assertNotNull(task1);
                        assertTrue(StereotypesHelper.hasStereotype(task1, taskSt));
                        symbol = project.getSymbolElementMap().getPresentationElement(task1);
                        assertNotNull(symbol);
                    }
                }
            }
    }
    
    private void checkParameterNotEmpty(Object param, String name) throws TestParameterException {
        if (param == null)
            throw new TestParameterException("Parameter " + name + " must be set to proceed with the test");
    }
    
    private Element getDraggedElement(TransformationTestData data) {
        Element dragged;
        if (data.getSourceStereotype() != null)
            dragged = search.findElementRecursively(project.getModel(), 
                    data.getSourceClass(), data.getSourceStereotype(), data.getSourceName(), true);
        else
            dragged = ModelHelper.findInParent(project.getModel(), data.getSourceName(), data.getSourceClass(), true);
        return dragged;
    }
    
    protected Element testSingleElementCreation(TransformationTestData data, boolean forceCheckUnique, Boolean setCheckUnique) throws TestParameterException {
        assertTrue(data.isValid());
        return testSingleElementCreation(getDraggedElement(data), data, forceCheckUnique, setCheckUnique);
    }

    protected Element testSingleElementCreation(Element dragged, TransformationTestData data, 
            boolean forceCheckUnique, Boolean setCheckUnique) throws TestParameterException {
        assertNotNull(dragged);
        assertTrue(data.getTargetClass() != null && data.dndSpecificationName != null & data.customizationName != null);
        checkParameterNotEmpty(data.diagramName, "diagramName");
        checkParameterNotEmpty(data.getSourceName(), "draggedName");
        checkParameterNotEmpty(data.getTargetName(), "targetName");
        if (project == null)
            project = Application.getInstance().getProject();
        assertNotNull(project);
        Element diagram = ModelHelper.findInParent(project.getModel(), data.diagramName, Diagram.class, true);
        SymbolElementMap map = project.getSymbolElementMap();
        DiagramPresentationElement elementOver = (DiagramPresentationElement) map.getPresentationElement(diagram);
        assertNotNull(elementOver);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(data, forceCheckUnique, setCheckUnique);
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged, elementOver);
            assertNotNull(generated);
            assertEquals(1, generated.size());
            Element el = generated.toArray(new Element[]{})[0];
            assertEquals(el.getClassType(), data.getTargetClass());
            System.out.println("Generated el name: " + getProperName(((NamedElement) el).getName()));
            assertTrue(getProperName(((NamedElement) el).getName()).equals(data.getTargetName()));
            if (data.getTargetStereotype() != null)
                assertTrue(StereotypesHelper.hasStereotype(el, data.getTargetStereotype()));
            PresentationElement symbol = project.getSymbolElementMap().getPresentationElement(el);
            assertNotNull(symbol);

            // Make sure that if checkUniqueness is not checked then we must have both Elements, one of with generated name, 
            // and another with the name of dragged element
            if (forceCheckUnique && Objects.equals(setCheckUnique, Boolean.FALSE)) {
                Element targetCandidate;
                if (data.getTargetStereotype() != null)
                    targetCandidate = search.findElement(diagram.getOwner(), data.getTargetClass(), data.getTargetStereotype(), data.getTargetName());
                else
                    targetCandidate = ModelHelper.findInParent(diagram.getOwner(), data.getSourceName(), data.getTargetClass(), true);
                assertNotNull(targetCandidate);
            }
            return el;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Element testSingleElementCreation(TransformationTestData data) throws TestParameterException {
        return testSingleElementCreation(data, false, null);
    }
    
    protected Element testSingleElementCreation(Element dragged, TransformationTestData data) throws TestParameterException {
        return testSingleElementCreation(dragged, data, false, null);
    }

    protected void testSingleElementWithCheckUnique(TransformationTestData data, 
            boolean forceCheckUnique, Boolean setCheckUnique) throws TestParameterException {
        assertTrue(!forceCheckUnique || (forceCheckUnique && setCheckUnique != null));
        if (forceCheckUnique) {
            if (Objects.equals(setCheckUnique, Boolean.TRUE)) {
                Element el = testSingleElementCreation(data, forceCheckUnique, setCheckUnique);
                Element el2 = testSingleElementCreation(data, forceCheckUnique, setCheckUnique);
                assertEquals(el, el2);
            } else {
                testSingleElementCreation(data, forceCheckUnique, setCheckUnique);
                data.setTargetName(data.getTargetName() + "_1");
                testSingleElementCreation(data, forceCheckUnique, setCheckUnique);
            }
        } else
            testSingleElementCreation(data, false, null);
    }

    protected Element testExistingSingleElementCreation(TransformationTestData data) throws TestParameterException {
        assertTrue(data.isValid());
        checkParameterNotEmpty(data.diagramName, "diagramName");
        checkParameterNotEmpty(data.getSourceName(), "draggedName");
        checkParameterNotEmpty(data.getTargetName(), "targetName");
        if (project == null)
            project = Application.getInstance().getProject();
        Element diagram = ModelHelper.findInParent(project.getModel(), data.diagramName, Diagram.class, true);
        assertTrue(diagram != null);
        SymbolElementMap map = project.getSymbolElementMap();
        DiagramPresentationElement elementOver = (DiagramPresentationElement) map.getPresentationElement(diagram);
        assertNotNull(elementOver);
        Element dragged;
        if (data.getSourceStereotype() != null)
            dragged = search.findElementRecursively(project.getModel(), 
                    data.getSourceClass(), data.getSourceStereotype(), data.getSourceName(), true);
        else
            dragged = ModelHelper.findInParent(project.getModel(), data.getSourceName(), data.getSourceClass(), true);
        Element targetCandidate;
        if (data.getTargetStereotype() != null)
            targetCandidate = search.findElement(diagram.getOwner(), data.getTargetClass(), data.getTargetStereotype(), data.getTargetName());
        else
            targetCandidate = ModelHelper.findInParent(diagram.getOwner(), data.getTargetName(), data.getTargetClass(), true);
        assertNotNull(targetCandidate);
        if (data.getTargetStereotype() != null)
            assertTrue(StereotypesHelper.hasStereotype(targetCandidate, data.getTargetStereotype()));
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(data);
            Set<Object> generated = performDragAndDropTest(reader, elementOver, dragged, elementOver);
            Element gen = generated.toArray(new Element[]{})[0];
            assertEquals(targetCandidate, gen);
            PresentationElement symbol = project.getSymbolElementMap().getPresentationElement(gen);
            assertNotNull(symbol);
            return gen;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void testPropertyCreation(TransformationTestData data, Stereotype propertyStereotype) throws TestParameterException {
        assertTrue(data.isValid());
        checkParameterNotEmpty(data.diagramName, "diagramName");
        checkParameterNotEmpty(data.getSourceName(), "draggedName");
        checkParameterNotEmpty(data.getTargetName(), "targetName");
        if (project == null)
            project = Application.getInstance().getProject();
        Element root = project.getModel();
        Element target = search.findElementRecursively(root, data.getTargetClass(), data.getTargetStereotype(), data.getTargetName(), true);
        SymbolElementMap map = project.getSymbolElementMap();
        PresentationElement elementOver = map.getPresentationElement(target);
        Element dragged = search.findElementRecursively(root, data.getSourceClass(), data.getSourceStereotype(), data.getSourceName(), true);
        Element diagram = ModelHelper.findInParent(root, data.diagramName, Diagram.class, true);
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) map.getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(null, data.dndSpecificationName, data.customizationName);
            performDragAndDropTest(reader, elementOver, dragged, diagramPres);
            boolean hasTarget = false;
            for (Element prop : target.getOwnedElement())
                if (prop instanceof NamedElement && ((NamedElement) prop).getName().equals(((NamedElement) dragged).getName())
                        && (propertyStereotype != null ? StereotypesHelper.hasStereotype(prop, propertyStereotype) : true)) {
                    hasTarget = true;
                    break;
                }
            assertTrue(hasTarget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testConstraintCreation(TransformationTestData data, Stereotype constraintStereotype) throws TestParameterException {
        assertTrue(data.isValid());
        checkParameterNotEmpty(data.diagramName, "diagramName");
        checkParameterNotEmpty(data.getSourceName(), "draggedName");
        checkParameterNotEmpty(data.getTargetName(), "targetName");
        if (project == null)
            project = Application.getInstance().getProject();
        Element root = project.getModel();
        Element target = search.findElementRecursively(root, data.getTargetClass(), data.getTargetStereotype(), data.getTargetName(), true);
        SymbolElementMap map = project.getSymbolElementMap();
        PresentationElement elementOver = map.getPresentationElement(target);
        assertNotNull(elementOver);
        Element dragged = search.findElementRecursively(root, Constraint.class, data.getSourceStereotype(), data.getSourceName(), true);
        Element diagram = ModelHelper.findInParent(root, data.diagramName, Diagram.class, true);
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) map.getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(null, data.dndSpecificationName, data.customizationName);
            performDragAndDropTest(reader, elementOver, dragged, diagramPres);
            Classifier cl = (Classifier) target;
            assertEquals(1, cl.getOwnedRule().size());
            Constraint gen = cl.getOwnedRule().toArray(new Constraint[]{})[0];
            assertTrue(gen.getName().equals(((NamedElement) dragged).getName()));
            if (constraintStereotype != null)
                assertTrue(StereotypesHelper.hasStereotype(gen, constraintStereotype));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected Element checkRelationshipExist(Project project, ElementData first, ElementData second, ElementData relation) {
        Element root = project.getModel();
        SymbolElementMap map = project.getSymbolElementMap();
        Element genCl1 = search.findElementRecursively(root, first.elementClass, first.elementStereotype, first.elementName, true);
        assertNotNull(genCl1);
        assertNotNull(map.getPresentationElement(genCl1));
        Element genCl2 = search.findElementRecursively(root, second.elementClass, second.elementStereotype, second.elementName, true);
        assertNotNull(genCl2);
        assertNotNull(map.getPresentationElement(genCl2));
        Element assoc1 = search.findRelationship(relation.elementClass, genCl1, genCl2, relation.elementName, relation.elementStereotype);
        assertNotNull(assoc1);
        assertNotNull(map.getPresentationElement(assoc1));
        return assoc1;
    }
    
    public void testRelationshipCreation(Element dragged, TransformationTestData data, List<ElementData[]> outputs) throws TestParameterException {
        checkParameterNotEmpty(data.diagramName, "diagramName");
        Element root = project.getModel();
        Element diagram = ModelHelper.findInParent(root, data.diagramName, Diagram.class, true);
        SymbolElementMap map = project.getSymbolElementMap();
        DiagramPresentationElement diagramPres = (DiagramPresentationElement) map.getPresentationElement(diagram);
        MDSpecificationReader reader;
        try {
            reader = getSpecificationReader(data.specPackage, data.dndSpecificationName, data.customizationName);  
            Set<Object> generated = performDragAndDropTest(reader, diagramPres, dragged);
            assertEquals(outputs.size(), generated.size());
            for (ElementData [] str: outputs) {
                Element assoc = checkRelationshipExist(project, str[0], str[1], str[2]);
                assertTrue(generated.contains(assoc));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

}
