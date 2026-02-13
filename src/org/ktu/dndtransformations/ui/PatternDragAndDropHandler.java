package org.ktu.dndtransformations.ui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.annotation.CheckForNull;
import org.apache.log4j.Logger;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dnd.CustomDragAndDropHandler;
import com.nomagic.magicdraw.ui.notification.Notification;
import com.nomagic.magicdraw.ui.notification.NotificationSeverity;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.ktu.dndtransformations.impl.ElementProducer;
import org.ktu.dndtransformations.impl.MagicDrawMapper;
import org.ktu.dndtransformations.impl.MagicDrawRenderer;
import org.ktu.dndtransformations.impl.MagicDrawSearch;
import org.ktu.dndtransformations.impl.PropertyManager;
import org.ktu.dndtransformations.parsers.MDSpecificationReader;
import org.ktu.dndtransformations.parsers.MagicDrawPatternFactory;
import org.ktu.dndtransformations.transforms.ContainerTransformationFactory;
import org.ktu.dndtransformations.transforms.MultipleTransformationFactory;
import org.ktu.dndtransformations.transforms.RelationTransformationFactory;
import org.ktu.dndtransformations.transforms.SingleTransformationFactory;
import org.ktu.transformations.elements.AbstractElementProducer;
import org.ktu.transformations.elements.ElementGenerationException;
import org.ktu.transformations.helpers.AbstractPropertyManager;
import org.ktu.transformations.helpers.ElementSearch;
import org.ktu.transformations.mappers.ElementMapper;
import org.ktu.transformations.notifiers.NotificationObserver;
import org.ktu.transformations.notifiers.NotificationType;
import org.ktu.transformations.parsers.InvalidPatternException;
import org.ktu.transformations.renderers.ElementRenderer;
import org.ktu.transformations.transforms.TransformationConfigurationException;
import org.ktu.transformations.transforms.rendered.RenderedGenerator;

/**
 * Plugin Drag and Drop handler class
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class PatternDragAndDropHandler extends CustomDragAndDropHandler implements NotificationObserver {

    private int index;
    private ArrayList<MDSpecificationReader> specList;

    public PatternDragAndDropHandler(int index) {
        super();
        this.index = index;
        specList = new ArrayList<>();
    }
    
    public static RenderedGenerator<Element, Stereotype, ConnectableElement, PresentationElement> getTransformerInstance() {
        ElementMapper<Element, ConnectableElement, Stereotype> mapper = MagicDrawMapper.getInstance();
        AbstractPropertyManager<Element, Stereotype, TypedElement> manager = PropertyManager.getInstance();
        ElementSearch<Element, Stereotype> search = MagicDrawSearch.getInstance();
        AbstractElementProducer<Element, Stereotype> producer = ElementProducer.getInstance();
        final ElementRenderer<Element, PresentationElement> renderer = MagicDrawRenderer.getInstance();
        RenderedGenerator<Element, Stereotype, ConnectableElement, PresentationElement> transformer = 
                new RenderedGenerator<>(mapper, manager, producer, search, renderer, MagicDrawPatternFactory.getInstance());
        transformer.setContainerTransformation(ContainerTransformationFactory.getInstance());
        transformer.setMultipleTransformation(MultipleTransformationFactory.getInstance());
        transformer.setRelationTransformation(RelationTransformationFactory.getInstance());
        transformer.setSingleElementTransformation(SingleTransformationFactory.getInstance());
        return transformer;
    }

    @Override
    public boolean drop(@CheckForNull Point location, @CheckForNull PresentationElement elementOver,
            @CheckForNull List<Element> draggedElements, @CheckForNull DiagramPresentationElement diagram) {
        SessionManager sessionManager = SessionManager.getInstance();
        try {
            if (sessionManager.isSessionCreated())
                sessionManager.closeSession();
            sessionManager.createSession("Performing drag and drop operations");
            RenderedGenerator<Element, Stereotype, ConnectableElement, PresentationElement> transformer = getTransformerInstance();
            transformer.setSpecificationReader(specList.get(index));
            transformer.setLocation(location);
            Collection<NotificationObserver> observers = new HashSet<>();
            observers.add(this);
            transformer.generate(draggedElements.get(0), diagram.getElement().getOwner(), diagram, elementOver, observers);
        } catch (InvalidPatternException | ElementGenerationException | TransformationConfigurationException e1) {
            Application.getInstance().getGUILog().log(e1.getMessage());
            Logger.getLogger(getClass()).error(e1);
            e1.printStackTrace();
            return false;
        } catch (Exception e) {
            Application.getInstance().getGUILog().log(e.getMessage());
            Logger.getLogger(getClass()).error(e);
            e.printStackTrace();
        } finally {
            if (sessionManager.isSessionCreated())
                sessionManager.closeSession();
            specList.clear();
        }
        return false;
    }

    @Override
    public String getDescription() {
        if (specList == null || specList.isEmpty() || index >= specList.size())
            return null;
        return specList.get(index).getRepresentationText();
    }

    @Override
    public boolean willAcceptDrop(@CheckForNull Point location, @CheckForNull PresentationElement elementOver,
            @CheckForNull List<Element> draggedElements, @CheckForNull DiagramPresentationElement diagram) {
        if (elementOver == null || diagram == null || draggedElements == null)
            return false;
        if (!draggedElements.isEmpty() && draggedElements.get(0) != null) {
            specList = getTransformationSpecifications(draggedElements.get(0),
                    elementOver.getElement() == diagram.getElement() ? diagram : elementOver);
            return specList != null && !specList.isEmpty() && index < specList.size();
        }
        return false;
    }

    private ArrayList<MDSpecificationReader> getTransformationSpecifications(Element source, PresentationElement presElem) {
        MagicDrawMapper mapper = MagicDrawMapper.getInstance();
        Stereotype stereotype = mapper.getCustomizationStereotype();
        Collection<Element> elements = MagicDrawSearch.getInstance().findStereotypedElements(stereotype);
        ArrayList<MDSpecificationReader> specReaders = new ArrayList<>();
        for (Element custom : elements) {
            List<Element> dndElements = MDSpecificationReader.getDnDSpecifications(custom);
            for (Element element: dndElements) {
                boolean presElCond = false;
                if (presElem instanceof DiagramPresentationElement)
                    presElCond = MDSpecificationReader.hasTargetDiagram(element, (DiagramPresentationElement) presElem);
                else 
                    presElCond = MDSpecificationReader.targetConditionSatisfied(presElem.getElement(), custom);
                if (presElCond && MDSpecificationReader.sourceConditionSatisfied(source, element)) {
                    MDSpecificationReader reader = null;
                    try {
                        reader = new MDSpecificationReader(element, custom);
                    } catch (Exception e) {
                        Application.getInstance().getGUILog().log(e.getMessage());
                        Logger.getLogger(getClass()).error(e.getMessage());
                    }
                    if (reader != null)
                        specReaders.add(reader);
                }   
            }
        }
        Collections.sort(specReaders);
        return specReaders;
    }

    @Override
    public void update(Object[] generated, String text, NotificationType type) {
        GUILog log = Application.getInstance().getGUILog();
        Notification notify = new Notification();
        notify.setText(text);
        if (type == NotificationType.ERROR)
            notify.setSeverity(NotificationSeverity.ERROR);
        else if (type == NotificationType.WARNING)
            notify.setSeverity(NotificationSeverity.WARNING);
        else
            notify.setSeverity(NotificationSeverity.INFO);
        log.log(notify, false);
    }
}
