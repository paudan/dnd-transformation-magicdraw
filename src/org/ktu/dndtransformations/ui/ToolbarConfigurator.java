package org.ktu.dndtransformations.ui;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.commands.CommandHistory;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.options.OrthogonalLayouterOptionsGroup;
import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.ui.diagrams.CustomDiagramAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilter;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilterImpl;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.layout.OrthogonalDiagramLayouter;
import com.nomagic.magicdraw.uml.symbols.manipulators.drawactions.AdditionalDrawAction;
import com.nomagic.magicdraw.uml.symbols.shapes.ClassView;
import com.nomagic.magicdraw.uml.symbols.shapes.PartView;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.StructuredClassifier;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ktu.dndtransformations.impl.MagicDrawConfiguration;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.transformations.parsers.PatternConfiguration;
import org.ktu.transformations.parsers.SpecificationConfiguration;

/**
 * Toolbar configurator for M2M Transformation diagram, which contains the transformation specification
 *
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 *
 */

@SuppressWarnings("deprecation")
public class ToolbarConfigurator implements AMConfigurator {
    
    private MagicDrawMapper mapper = MagicDrawMapper.getInstance();

    @Override
    public int getPriority() {
        return ConfiguratorWithPriority.MEDIUM_PRIORITY;
    }

    @Override
    public void configure(ActionsManager manager) {
        List<NMAction> allActions = manager.getAllActions();
        final Profile transformProfile = mapper.getTransformationProfile();

        for (final NMAction action : allActions) {
            String actionName = action.getName();
            if (action instanceof CustomDiagramAction && actionName != null && actionName.compareToIgnoreCase("M2M Transformation Pattern") == 0) {
                CustomDiagramAction customAction = (CustomDiagramAction) action;
                customAction.setCustomAdditionalDrawAction(new AdditionalDrawAction() {

                    @Override
                    public boolean execute(PresentationElement symbol, Point mousePoint) {
                        createTransformationPattern(symbol, mousePoint);
                        return true;
                    }

                    @Override
                    public void afterExecute(PresentationElement symbol, Point mousePoint) {
                    }
                });
            } else if (action instanceof CustomDiagramAction && actionName != null && actionName.compareToIgnoreCase("Source") == 0) {
                CustomDiagramAction customAction = (CustomDiagramAction) action;
                customAction.setCustomAdditionalDrawAction(new AdditionalDrawAction() {

                    @Override
                    public boolean execute(PresentationElement symbol, Point mousePoint) {
                        StereotypesHelper.addStereotype(symbol.getElement(), mapper.getSourceStereotype());
                        return true;
                    }

                    @Override
                    public void afterExecute(PresentationElement symbol, Point mousePoint) {
                    }

                });
            } else if (action instanceof CustomDiagramAction && actionName != null && actionName.compareToIgnoreCase("Target") == 0) {
                CustomDiagramAction customAction = (CustomDiagramAction) action;
                customAction.setCustomAdditionalDrawAction(new AdditionalDrawAction() {

                    @Override
                    public boolean execute(PresentationElement symbol, Point mousePoint) {
                        StereotypesHelper.addStereotype(symbol.getElement(), mapper.getTargetStereotype());
                        return true;
                    }

                    @Override
                    public void afterExecute(PresentationElement symbol, Point mousePoint) {
                    }

                });
            } else if (action instanceof CustomDiagramAction && actionName != null && actionName.compareToIgnoreCase("Dragged Element") == 0) {
                final CustomDiagramAction customAction = (CustomDiagramAction) action;
                customAction.setCustomAdditionalDrawAction(new AdditionalDrawAction() {

                    @Override
                    public boolean execute(PresentationElement symbol, Point mousePoint) {
                        return true;
                    }

                    @Override
                    public void afterExecute(PresentationElement symbol, Point mousePoint) {
                        Project prj = Application.getInstance().getProject();
                        Element parent = symbol.getElement().getOwner();
                        Element part = parent.getOwner();
                        if (!(parent instanceof Diagram) && StereotypesHelper.hasStereotype(part, mapper.getSourceStereotype())) {
                            CommandHistory history = prj.getCommandHistory();
                            PresentationElementsManager manager = PresentationElementsManager.getInstance();
                            try {
                                history.startCommand("Setting DraggedElement stereotype");
                                Stereotype draggedSt = mapper.getDraggedElementStereotype();
                                for (Element prop : part.getOwnedElement())
                                    if (prop instanceof Property && StereotypesHelper.hasStereotype(prop, draggedSt))
                                        StereotypesHelper.removeStereotype(prop, draggedSt);
                                StereotypesHelper.addStereotype(parent, draggedSt);
                                Element elem = symbol.getElement();
                                manager.deletePresentationElement(symbol);
                                Element owner = elem.getOwner();
                                Collection<Comment> comments = owner.getOwnedComment();
                                Iterator<Comment> itr = comments.iterator();
                                while (itr.hasNext()) {
                                    Comment comm = itr.next();
                                    if (comm.equals(elem))
                                        itr.remove();
                                }
                                PresentationElement el = prj.getSymbolElementMap().getPresentationElement(owner);
                                if (el != null)
                                    el.getBounds().setSize(el.getPreferredSize().height, el.getPreferredSize().width);
                                history.execute();
                            } catch (ReadOnlyElementException ex) {
                                Logger.getLogger(ToolbarConfigurator.class.getName()).log(Level.ERROR, null, ex);
                            }
                            history.clearHistory();
                        }
                    }

                });
            } else if (action instanceof CustomDiagramAction && actionName != null && actionName.compareToIgnoreCase("M2M Transformation Specification") == 0) {
                CustomDiagramAction customAction = (CustomDiagramAction) action;
                customAction.setCustomAdditionalDrawAction(new AdditionalDrawAction() {

                    @Override
                    public boolean execute(PresentationElement symbol, Point mousePoint) {
                        List<PresentationElement> layout = new ArrayList<>();
                        PresentationElement pattern = createTransformationPattern(symbol, mousePoint);
                        layout.add(pattern);
                        PackageableElement patternEl = ((PackageableElement) pattern.getElement());
                        String name = patternEl.getOwningPackage().getName();
                        patternEl.setName(name);
                        Project proj = Application.getInstance().getProject();
                        if (proj == null)
                            return false;
                        ElementsFactory f = proj.getElementsFactory();
                        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class specEl = f.createClassInstance();
                        specEl.setName(name + " specification");
                        specEl.setOwningPackage(patternEl.getOwningPackage());
                        SpecificationConfiguration config = MagicDrawConfiguration.getSpecificationConfiguration();
                        Stereotype st = mapper.getDnDExtendedSpecificationStereotype();
                        if (st != null) {
                            StereotypesHelper.addStereotype(specEl, st);
                            StereotypesHelper.setStereotypePropertyValue(specEl, st, config.getTransformationPatternTagName(), patternEl);
                        }
                        PresentationElementsManager manager = PresentationElementsManager.getInstance();
                        DiagramPresentationElement diagram = symbol.getDiagramPresentationElement();
                        try {
                            layout.add(manager.createShapeElement(specEl, diagram));
                        } catch (ReadOnlyElementException e) {
                            e.printStackTrace();
                        }
                        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class customEl = f.createClassInstance();
                        st = mapper.getCustomizationStereotype();
                        if (st != null) {
                            StereotypesHelper.addStereotype(customEl, st);
                            StereotypesHelper.setStereotypePropertyValue(customEl, st, config.getAllowedTransformationsTagName(), specEl);
                        }
                        customEl.setOwningPackage(patternEl.getOwningPackage());
                        customEl.setName(name + " customization");
                        try {
                            layout.add(manager.createShapeElement(customEl, diagram));
                        } catch (ReadOnlyElementException e) {
                            e.printStackTrace();
                        }
                        diagram.setSelected(layout);
                        OrthogonalLayouterOptionsGroup opt = new OrthogonalLayouterOptionsGroup();
                        opt.setMoveToFreeSpace(true);
                        opt.setLayoutStyle(OrthogonalLayouterOptionsGroup.LAYOUT_STYLE_NORMAL);
                        diagram.layout(false, new OrthogonalDiagramLayouter(), opt);
                        return true;
                    }

                    @Override
                    public void afterExecute(PresentationElement symbol, Point mousePoint) {
                    }

                });
            } else if (action instanceof CustomDiagramAction && actionName != null && actionName.compareToIgnoreCase("MetaClass") == 0) {
                CustomDiagramAction customAction = (CustomDiagramAction) action;
                customAction.setCustomAdditionalDrawAction(new AdditionalDrawAction() {

                    @Override
                    public void afterExecute(PresentationElement symbol, Point mousePoint) {
                    }

                    @Override
                    public boolean execute(PresentationElement symbol, Point mousePoint) {
                        Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
                        ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent, "Select classifier", null);
                        Profile profile = mapper.getUMLMetamodelProfile();
                        List<?> types = ClassTypes.getClassifiers();
                        SelectElementTypes selectElementTypes = new SelectElementTypes(null, types, null, types);

                        SelectElementInfo selectElementInfo = new SelectElementInfo(false, false);
                            // Available properties are filtered so that only the ones which start with 'p' are selected.
                        //final Collection<Property> candidates = getSelectionCandidates("p");

                        // Gets elements which are initially selected in the dialog.
                            /*List<Property> initialSelection = getInitialSelection(candidates);

                         TypeFilter selectableFilter = new TypeFilterImpl(selectElementTypes.select) {
                         @Override
                         public boolean accept(@Nonnull BaseElement baseElement, boolean b) {
                         return super.accept(baseElement, b) && candidates.contains(baseElement);
                         }
                         };*/
                        TypeFilter displayableFilter = ElementSelectionDlgFactory.createDisplayableForSelectable(types);
                        TypeFilter selectableFilter = new TypeFilterImpl() {
                            @Override
                            public boolean accept(@Nonnull BaseElement baseElement, boolean b) {
                                System.out.println(baseElement.getClassType());
                                return super.accept(baseElement, b)
                                        && //baseElement instanceof Classifier;
                                        !(baseElement.getClass().isInterface() || Modifier.isAbstract(baseElement.getClass().getModifiers()));
                            }
                        };

                        ElementSelectionDlgFactory.initSingle(dlg, selectElementInfo, displayableFilter, selectableFilter, true, null, null);
                        dlg.show();
                        if (dlg.isOkClicked())
                            symbol.setElement((Element) dlg.getSelectedElement());
                        return false;
                    }

                });

            }
        }
    }

    @SuppressWarnings("deprecation")
    private PresentationElement createProperty(String stName, StructuredClassifier owner,
            PresentationElement symbol, Point mousePoint) {
        Project proj = Application.getInstance().getProject();
        if (proj == null)
            return null;
        ElementsFactory f = proj.getElementsFactory();
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class propEl = f.createClassInstance();
        Project project = Application.getInstance().getProject();
        Profile profile = mapper.getTransformationProfile();
        Stereotype st = StereotypesHelper.getStereotype(project, stName, profile);
        if (st != null)
            StereotypesHelper.addStereotype(propEl, st);
        propEl.setPackage(owner.getOwningPackage());
        Property prop = f.createPropertyInstance();
        prop.setType(propEl);
        prop.setOwner(owner);
        prop.setAggregation(AggregationKindEnum.COMPOSITE);
        owner.getAttribute().add(prop);
        ClassView view = (ClassView) symbol;
        PresentationElementsManager manager = PresentationElementsManager.getInstance();
        try {
            PartView el = manager.createPartShape(prop, view.getClassHeaderView().getStructureCompartmentView(), null, false, mousePoint);
            manager.reshapeShapeElement(el, new Rectangle(100, 200));
            return el;
        } catch (ReadOnlyElementException e) {
            e.printStackTrace();
        }
        return null;
    }

    private PresentationElement createTransformationPattern(PresentationElement symbol, Point mousePoint) {
        StructuredClassifier clazz = (StructuredClassifier) symbol.getElement();
        PatternConfiguration config = MagicDrawConfiguration.getPatternConfiguration();
        PresentationElement source = createProperty(config.getSourceStereotypeName(), clazz, symbol, mousePoint);
        PresentationElement target = createProperty(config.getTargetStereotypeName(), clazz, symbol, mousePoint);
        PresentationElementsManager manager = PresentationElementsManager.getInstance();
        try {
            manager.movePresentationElement(source, new Point(5, 5));
            manager.movePresentationElement(target, new Point(170, 5));
            symbol.getBounds().setSize(275, 240);
            manager.movePresentationElement(symbol, mousePoint);
        } catch (ReadOnlyElementException e1) {
            e1.printStackTrace();
        }
        return symbol;

    }

}
