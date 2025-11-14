package com.sos.joc.inventory.dependencies.util;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.common.Dependency;
import com.sos.joc.db.inventory.DBItemInventoryExtendedDependency;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.dependencies.get.AffectedResponseItem;

public class DependencyUtils {
    
    public static List<DBItemInventoryExtendedDependency> getAllDependencies(SOSHibernateSession session) throws SOSHibernateException {
        DBLayerDependencies dbLayer = new DBLayerDependencies(session);
        return dbLayer.getAllExtendedDependencies();
    }
    
    public static Map<Dependency, Set<Dependency>> resolveReferencedBy(List<DBItemInventoryExtendedDependency> dependencies) {
        if(dependencies != null && !dependencies.isEmpty()) {
            Map<Dependency, Set<Dependency>> resolved = new HashMap<Dependency, Set<Dependency>>();
            dependencies.stream().map(DBItemInventoryExtendedDependency::getDependency).forEach(dep -> resolved.putIfAbsent(dep,
                    new HashSet<Dependency>()));
            dependencies.stream().forEach(item -> resolved.get(item.getDependency()).add(item.getReferencedBy()));
            return resolved;
        } else {
            return Collections.emptyMap();
        }
    }
    
    public static Map<Dependency, Set<Dependency>> resolveReferences(List<DBItemInventoryExtendedDependency> dependencies) {
        if(dependencies != null && !dependencies.isEmpty()) {
            Map<Dependency, Set<Dependency>> resolved = new HashMap<Dependency, Set<Dependency>>();
            dependencies.stream().map(DBItemInventoryExtendedDependency::getReferencedBy).forEach(dep -> resolved.putIfAbsent(dep,
                    new HashSet<Dependency>()));
            dependencies.stream().forEach(item -> resolved.get(item.getReferencedBy()).add(item.getDependency()));
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
    
    public static String resolvePath(String folder, String name) {
        if("/".equals(folder)) {
            return name != null ? folder + name : folder;
        } else {
            return Paths.get(folder).resolve(name).toString().replace('\\', '/');
        }
    }
    
    public static Set<ConfigurationObject> convert(Set<Dependency> dependencies) {
        if(dependencies == null) {
            return Collections.emptySet();
        }
        return dependencies.stream().map(DependencyUtils::convert).collect(Collectors.toSet());
    }
    
    public static ConfigurationObject convert(Dependency dependency) {
        ConfigurationObject config = new ConfigurationObject();
        if(dependency.getType() != null) {
            config.setObjectType(dependency.getType());
            config.setName(dependency.getName());
            config.setPath(DependencyUtils.resolvePath(dependency.getFolder(), dependency.getName()));
            config.setValid(dependency.getValid());
            config.setDeployed(dependency.getDeployed());
            config.setReleased(dependency.getReleased());
        }
        return config;
    }
    

}
