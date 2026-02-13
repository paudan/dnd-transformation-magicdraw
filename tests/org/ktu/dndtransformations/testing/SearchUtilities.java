package org.ktu.dndtransformations.testing;

import java.util.Collection;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Vertex;

public class SearchUtilities {

    public static Transition findTransition(Element owner, Vertex source, Vertex target) {
        Collection<? extends Element> transitions = ModelHelper.getElementsOfType(owner, new Class[]{Transition.class}, true);
        for (Element el : transitions) {
            Transition assoc = (Transition) el;
            if (assoc.getSource().equals(source) && assoc.getTarget().equals(target))
                return assoc;
        }
        return null;
    }

}
