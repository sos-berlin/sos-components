package com.sos.joc.inventory.dependencies.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.common.Dependency;
import com.sos.joc.db.inventory.DBItemInventoryExtendedDependency;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.model.inventory.dependencies.get.AffectedResponseItem;
import com.sos.joc.model.inventory.dependencies.get.EnforcedConfigurationObject;

public class DependencyUtils {
    
    public static List<DBItemInventoryExtendedDependency> getAllDependencies(SOSHibernateSession session) throws SOSHibernateException {
        DBLayerDependencies dbLayer = new DBLayerDependencies(session);
        return dbLayer.getAllExtendedDependencies();
    }
    
    public static Map<Dependency, Set<Dependency>> resolveReferencedBy(Collection<DBItemInventoryExtendedDependency> dependencies) {
        if (dependencies != null && !dependencies.isEmpty()) {
            Predicate<DBItemInventoryExtendedDependency> isNotSelfReferenced = item -> !item.getInvId().equals(item.getDepId());
            Map<Dependency, Set<Dependency>> resolved = new HashMap<>();
            dependencies.stream().map(DBItemInventoryExtendedDependency::getDependency).forEach(dep -> resolved.putIfAbsent(dep,
                    new HashSet<Dependency>()));
            dependencies.stream().filter(isNotSelfReferenced).forEach(item -> resolved.get(item.getDependency()).add(item.getReferencedBy()));
            return resolved;
        } else {
            return Collections.emptyMap();
        }
    }

    public static Map<Dependency, Set<Dependency>> resolveReferences(Collection<DBItemInventoryExtendedDependency> dependencies) {
        if (dependencies != null && !dependencies.isEmpty()) {
            Predicate<DBItemInventoryExtendedDependency> isNotSelfReferenced = item -> !item.getInvId().equals(item.getDepId());
            Map<Dependency, Set<Dependency>> resolved = new HashMap<>();
            dependencies.stream().map(DBItemInventoryExtendedDependency::getReferencedBy).forEach(dep -> resolved.putIfAbsent(dep,
                    new HashSet<Dependency>()));
            dependencies.stream().filter(isNotSelfReferenced).forEach(item -> resolved.get(item.getReferencedBy()).add(item.getDependency()));
            return resolved;
        } else {
            return Collections.emptyMap();
        }
    }
    
    public static AffectedResponseItem getAffectedResponseItem(Dependency dependency) {
        AffectedResponseItem responseItem = new AffectedResponseItem();
        responseItem.setDraft(!(dependency.getDeployed() || dependency.getReleased()));
        responseItem.setItem(DependencyUtils.convert(dependency));
        return responseItem;
    }
    
    public static Set<EnforcedConfigurationObject> convert(Set<Dependency> dependencies) {
        if(dependencies == null) {
            return Collections.emptySet();
        }
        return dependencies.stream().map(DependencyUtils::convert).collect(Collectors.toSet());
    }
    
    public static EnforcedConfigurationObject convert(Dependency dependency) {
        EnforcedConfigurationObject config = new EnforcedConfigurationObject();
        if(dependency.getType() != null) {
            config.setId(dependency.getId());
            config.setObjectType(dependency.getType());
            config.setName(dependency.getName());
            config.setPath(dependency.getPath());
            config.setValid(dependency.getValid());
            config.setDeployed(dependency.getDeployed());
            config.setReleased(dependency.getReleased());
            config.setEnforce(dependency.getEnforce());
        }
        return config;
    }

}
