package org.ktu.dndtransformations.parsers;

import org.ktu.transformations.parsers.ConcatMap;
import org.ktu.transformations.parsers.PropertyStack;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.ktu.transformations.parsers.ConnectableEntity;
import org.ktu.transformations.parsers.ConnectorEntity;
import org.ktu.transformations.parsers.SpecificationReader.IntegrationType;

/**
 *
 * @author Admin
 */
public class MDConcatMap extends ConcatMap {
    
    private Map<Connector, Map<Connector, PropertyStack>> connMap;
    private Map<Connector, PropertyStack> targetConnMap;

    public MDConcatMap() {
        super();
        init();
    }
    
    MDConcatMap(ConcatMap base) {
        super(base);
        init(); 
    }
    
    private void init() {
        connMap = new HashMap<>();
        targetConnMap = new HashMap<>();
    }
    
    @Override
    public void addIncomingEntry(ConnectorEntity targetConn, PropertyStack target, ConnectorEntity sourceConn, PropertyStack source) {
        super.addIncomingEntry(targetConn, target, sourceConn, source);
        Map<Connector, PropertyStack> lmap = connMap.get(targetConn);
        if (lmap == null) {
            lmap = new HashMap<>();
            if (targetConn.getConnectorObject() instanceof Connector) {
                connMap.put((Connector) targetConn.getConnectorObject(), lmap);
                targetConnMap.put((Connector) targetConn.getConnectorObject(), target);
            }
        }
        if (sourceConn.getConnectorObject() instanceof Connector)
            lmap.put((Connector) sourceConn.getConnectorObject(), source);
    }
    
    public EnumerationLiteral getIntegrationType(ConnectableEntity target) {
        for (ConnectorEntity conn : targetMap.keySet())
            if (conn.getConnectorObject() instanceof Connector && targetMap.get(conn).lowermostProperty().equals(target))
                return PatternParserImpl.getIntegrationLiteral((Connector) conn.getConnectorObject());
        return null;
    }

    public EnumerationLiteral getIntegrationLiteral(ConnectorEntity sourceConn, ConnectorEntity outConn) {
        SimpleImmutableEntry<IntegrationType, ConnectorEntity> typeRes = super.getIntegrationType(sourceConn, outConn);
        Object connObj = typeRes.getValue().getConnectorObject();
        if (connObj == null || !(connObj instanceof Connector))
            return null;
        if (typeRes.getKey() == typeRes.getValue().getIntegrationType())
            return PatternParserImpl.getIntegrationLiteral((Connector) connObj);
        else {
            Enumeration e = PatternParserImpl.getIntegrationLiteral((Connector) connObj).getEnumeration();
            return getIntegrationTypeElement(typeRes.getKey(), e);
        }
    }

    public EnumerationLiteral getIntegrationLiteral(PropertyStack source, ConnectableElement target) {
        for (ConnectorEntity conn : targetMap.keySet())
            if (targetMap.get(conn).lowermostProperty().equals(target)) {
                Map<ConnectorEntity, PropertyStack> map = concatMap.get(conn);
                for (ConnectorEntity sourceConn : map.keySet())
                    if (map.get(sourceConn).equals(source))
                        return getIntegrationLiteral(sourceConn, conn);
            }
        return null;
    }

    private EnumerationLiteral getIntegrationTypeElement(IntegrationType type, Enumeration enumeration) {
        if (enumeration == null)
            return null;
        for (EnumerationLiteral literal : enumeration.getOwnedLiteral())
            if (literal.getName().equals(type.getName()))
                return literal;
        return null;
    }
    
    public PropertyStack getTargetPropertyStack(Connector conn) {
        return targetConnMap.get(conn);
    }
    
    public PropertyStack getSourcePropertyStack(Connector key, Connector conn) {
        return connMap.get(key).get(conn);
    }

    public Set<Connector> getIncomingConnectors(Connector key) {
        return Collections.unmodifiableSet(connMap.get(key).keySet());
    }
}
