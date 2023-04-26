package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventorySearchDBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IQuickSearchResource;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class QuickSearchResourceImpl extends JOCResourceImpl implements IQuickSearchResource {

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestQuickSearchFilter.class);
            RequestQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, RequestQuickSearchFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response != null) {
                return response;
            }

            ResponseQuickSearch answer = new ResponseQuickSearch();
            answer.setResults(getBasicSearch(in, folderPermissions));
            Instant now = Instant.now();
            answer.setDeliveryDate(Date.from(now));
            answer.setToken(createToken(in, now)); 
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static List<ResponseBaseSearchItem> getBasicSearch(final RequestQuickSearchFilter in, SOSAuthFolderPermissions folderPermissions) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);

            List<InventorySearchItem> items = dbLayer.getQuickSearchInventoryConfigurations(in.getReturnType(), in.getSearch());
            
            List<ResponseBaseSearchItem> r = Collections.emptyList();
            if (items != null) {
                Function<InventorySearchItem, ResponseBaseSearchItem> toResponseSearchItem = item -> toResponseSearchItem(item);
                Predicate<InventorySearchItem> isPermitted = item -> folderPermissions.isPermittedForFolder(item.getFolder());

                r = items.stream().filter(isPermitted).map(toResponseSearchItem).sorted(Comparator.comparing(ResponseBaseSearchItem::getPath))
                        .collect(Collectors.toList());
            }
            return r;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static String createToken(final RequestQuickSearchFilter in, Instant now) {
        if (!SOSString.isEmpty(in.getToken())) {
            return in.getToken(); 
        } else {
            return SOSString.hash256(now.toString());
        }
    }
    
    private static ResponseBaseSearchItem toResponseSearchItem(InventorySearchItem item) {
        ResponseBaseSearchItem ri = new ResponseBaseSearchItem();
        ri.setPath(item.getPath());
        ri.setName(item.getName());
        ri.setObjectType(item.getTypeAsEnum());
        return ri;
    }
}