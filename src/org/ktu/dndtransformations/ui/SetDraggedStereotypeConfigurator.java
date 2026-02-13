package org.ktu.dndtransformations.ui;

import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.ui.actions.DefaultDiagramAction;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.awt.event.ActionEvent;
import org.ktu.dndtransformations.impl.MagicDrawMapper;

/**
 *
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of
 * Information Systems Design Technologies, Kaunas University of Technology,
 * 2014-2015
 */
public class SetDraggedStereotypeConfigurator implements DiagramContextAMConfigurator {
    
    private MagicDrawMapper mapper = MagicDrawMapper.getInstance();
    
    protected class SetDraggedStereotypeAction extends DefaultDiagramAction {
        
        private PresentationElement requester;

        public SetDraggedStereotypeAction(String id, String name, PresentationElement requester) {
            super(id, name, null, null);
            this.requester = requester;
        }
        
        @Override
	public void actionPerformed(ActionEvent e) {
            Element source = requester.getElement();
            Stereotype sourceSt = mapper.getSourceStereotype();
            Stereotype draggedSt = mapper.getDraggedElementStereotype();
            Element part = source.getOwner();
            if (StereotypesHelper.hasStereotype(part, sourceSt)) {
                for (Element prop : part.getOwnedElement())
                    if (prop instanceof Property && StereotypesHelper.hasStereotype(prop, draggedSt))
                        StereotypesHelper.removeStereotype(prop, draggedSt);
                StereotypesHelper.addStereotype(source, draggedSt);
            }
	}
  
    }

    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement arg1, PresentationElement[] arg2, PresentationElement requester) {
        if (requester == null)
            return;
        Element source = requester.getElement();
        Stereotype sourceSt = mapper.getSourceStereotype();
        if (StereotypesHelper.hasStereotype(source.getOwner(), sourceSt))
            manager.addActionNearTheGiven("", true, new SetDraggedStereotypeAction("", "Set as dragged element", requester));
    }

    @Override
    public int getPriority() {
        return ConfiguratorWithPriority.MEDIUM_PRIORITY;
    }
    
}
