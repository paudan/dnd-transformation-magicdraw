package org.ktu.dndtransformations.ui;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListener;
import com.nomagic.magicdraw.ui.notification.Notification;
import com.nomagic.magicdraw.ui.notification.NotificationSeverity;
import com.nomagic.magicdraw.uml.Visitor;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.reflect.VisitorContext;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartEventSupport;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.StructuredClassifier;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.PropertyNames;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.dndtransformations.parsers.SpecificationValidator;
import org.ktu.transformations.notifiers.NotificationType;

/**
 *
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology,
 * 2014-2015
 */
@SuppressWarnings("deprecation")
public class TransformationLibraryEventListener implements ProjectEventListener {
    
    private boolean validated = false;
    private MagicDrawMapper mapper = MagicDrawMapper.getInstance();

    private void registerChangeDraggedListener(final Project prj) {
        SmartListenerConfig cfg = new SmartListenerConfig(PropertyNames.APPLIED_STEREOTYPE_INSTANCE);
        final List<SmartListenerConfig> configs = Collections.singletonList(cfg);
        final SmartEventSupport support = prj.getSmartEventSupport();
        if (support != null)
            support.registerConfig(Property.class, configs, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!(evt instanceof Element))
                        return;
                    Element source = (Element) evt.getSource();
                    if (source == null || evt.getOldValue() == evt.getNewValue())
                        return;
                    Stereotype draggedSt = mapper.getDraggedElementStereotype();
                    Element part = source.getOwner();
                    Stereotype sourceSt = mapper.getSourceStereotype();
                    boolean hadDraggedSt = false;
                    if (evt.getOldValue() != null && evt.getOldValue() instanceof InstanceSpecification) {
                        Element stEl = ((InstanceSpecification) evt.getOldValue()).getStereotypedElement();
                        hadDraggedSt = StereotypesHelper.getStereotypes(stEl).contains(draggedSt);
                    }
                    boolean setDraggedSt = false;
                    if (evt.getNewValue() != null && evt.getNewValue() instanceof InstanceSpecification) {
                        Element stEl = ((InstanceSpecification) evt.getNewValue()).getStereotypedElement();
                        setDraggedSt = StereotypesHelper.getStereotypes(stEl).contains(draggedSt);
                    }
                    if (PropertyNames.APPLIED_STEREOTYPE_INSTANCE.equals(evt.getPropertyName()) && part != null && 
                            StereotypesHelper.hasStereotype(part, sourceSt) && (!hadDraggedSt && setDraggedSt)) {
                        for (Element prop : part.getOwnedElement())
                            if (prop instanceof Property && StereotypesHelper.hasStereotype(prop, draggedSt) && !prop.equals(source))
                                StereotypesHelper.removeStereotype(prop, draggedSt);
                    }
                }
            });
    }
    
    private void performValidation(Project project) {
        final Set<Element> visitedDnds = new HashSet<>();
        final Set<StructuredClassifier> visitedPatterns = new HashSet<>();
        Visitor visitor = new Visitor() {
            
            @Override
            public void visitClass(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class element, VisitorContext context) {
                GUILog log = Application.getInstance().getGUILog();
                if (StereotypesHelper.hasStereotype(element, mapper.getCustomizationStereotype())) {
                    List<Element> dnds = MDSpecificationReader.getDnDSpecifications(element);
                    for (Element dnd: dnds) 
                        if (!visitedDnds.contains(dnd)) {
                        try {
                            MDSpecificationReader reader = new MDSpecificationReader(dnd, element);
                            StructuredClassifier pattern = reader.getTransformationPattern();
                            boolean validate = pattern != null ? !visitedPatterns.contains(pattern) : false;
                            SpecificationValidator validator = new SpecificationValidator(reader, validate);
                            validator.validate();
                            Map<String, NotificationType> errors = validator.getErrorMessages();
                            for (String err: errors.keySet()) {
                                NotificationType type = errors.get(err);
                                Notification notify = new Notification();
                                notify.setText(err);
                                if (type == NotificationType.ERROR)
                                    notify.setSeverity(NotificationSeverity.ERROR);
                                else if (type == NotificationType.WARNING)
                                    notify.setSeverity(NotificationSeverity.WARNING);
                                log.log(notify, false);
                            }
                            if (pattern != null)
                                visitedPatterns.add(pattern);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        visitedDnds.add(dnd);
                        }
                }
            }
        };
        Collection<Element> allElements = project.getModel().getOwnedElement();
        Collection<Element> sharedElements = new HashSet<>();
        Collection<Element> elements = new HashSet<>();
        // Optimize check by skipping projects, attached to this project
        Collection<IAttachedProject> attached = ProjectUtilities.getAllAttachedProjects(project);
        for (IAttachedProject p: attached) 
            sharedElements.addAll(ProjectUtilities.getSharedPackages(p));
        Iterator<Element> it = allElements.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            if (e instanceof NamedElement && !sharedElements.contains(e))
                elements.add(e);
        }
        // Perform recursive check
        while (!elements.isEmpty()) {
            Collection<Element> newElements = new HashSet<>();
            it = elements.iterator();
            while (it.hasNext())
                try {
                    Element e = it.next();
                    if (e instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package)
                        newElements.addAll(e.getOwnedElement());
                    else
                        e.accept(visitor);
                } catch (Exception ex) {
                }
            elements = newElements;
        }     
    }

    @Override
    public void projectOpened(Project project) {
        registerChangeDraggedListener(project);
        if (!validated) {
            performValidation(project);
            validated = true;
        }
    }

    @Override
    public void projectClosed(Project project) {
    }

    @Override
    public void projectSaved(Project arg0, boolean arg1) {
    }

    @Override
    public void projectActivated(Project project) {
    }

    @Override
    public void projectDeActivated(Project project) {
    }

    @Override
    public void projectReplaced(Project arg0, Project arg1) {
    }

    @Override
    public void projectCreated(Project project) {
        registerChangeDraggedListener(project);
    }

    @Override
    public void projectPreClosed(Project project) {
    }

    @Override
    public void projectPreClosedFinal(Project project) {
    }

    @Override
    public void projectPreSaved(Project arg0, boolean arg1) {
    }

    @Override
    public void projectPreActivated(Project project) {
    }

    @Override
    public void projectPreDeActivated(Project project) {
    }

    @Override
    public void projectPreReplaced(Project arg0, Project arg1) {
    }

    @Override
    public void projectOpenedFromGUI(Project project) {
        registerChangeDraggedListener(project);
        if (!validated) {
            performValidation(project);
            validated = true;
        }
    }

    @Override
    public void projectActivatedFromGUI(Project project) {
    }
}
