package org.ktu.dndtransformations.impl;

import org.ktu.transformations.helpers.AbstractPropertyManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.util.Collection;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 *
 * @author Admin
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PropertyManager extends AbstractPropertyManager<Element, Stereotype, TypedElement> {
    
    private static PropertyManager INSTANCE;
    
    private PropertyManager() {
        super(MagicDrawMapper.getInstance());
    }

    /**
     * Get an instance of {@link AbstractPropertyManager}; it is created if does not exist
     * @return An instance of {@link AbstractPropertyManager}
     */
    public static PropertyManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PropertyManager();
        return INSTANCE;
    }

    @Override
    public Object getFeatureValue(Element element, String propName) {
        EStructuralFeature feat = element.eClass().getEStructuralFeature(propName);
        if (feat != null)
            return element.eGet(feat);
        return null;
    }

    @Override
    public boolean isFeatureMultiValued(Element element, String name) {
        EStructuralFeature feat = element.eClass().getEStructuralFeature(name);
        if (feat == null)
            return false;
        return feat.isMany();
    }

    @Override
    public Element unsetFeatureValue(Element element, String propName) {
        EStructuralFeature feat = element.eClass().getEStructuralFeature(propName);
        if (feat == null)
            return element;
        element.eUnset(feat);
        return element;
    }

    @Override
    public boolean hasFeature(Element element, String name) {
        return element.eClass().getEStructuralFeature(name) != null;
    }

    @Override
    public Element setPropertyValueList(Element element, String propName, Object... values) {
        EList<Object> list = new BasicEList<>();
        for (Object el: values)
            if (el instanceof Collection)
                list.addAll((Collection)el);
            else
                list.add(el);
        setFeatureValue(element, propName, list);
        return element;
    }

    @Override
    public boolean isFeatureSet(Element element, String name) {
        EStructuralFeature feat = element.eClass().getEStructuralFeature(name);
        if (feat == null)
            return false;
        return element.eIsSet(feat);
    }

    @Override
    public Element setFeatureValue(Element element, String propName, Object value) {
        EStructuralFeature feat = element.eClass().getEStructuralFeature(propName);
        if (feat == null)
            return element;
        element.eSet(feat, value);
        return element;
    }
}
