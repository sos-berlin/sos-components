package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.quicksearch.QuickSearchRequest;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.db.inventory.InventorySearchDBLayer;
import com.sos.joc.db.inventory.items.InventoryQuickSearchItem;
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
            
            if (!in.getQuit()) {
                in = QuickSearchStore.checkToken(in, accessToken);
                answer.setResults(getBasicSearch(in, folderPermissions));
            } else {
                answer.setResults(Collections.emptyList());
            }
            
            Instant now = Instant.now();
            answer.setDeliveryDate(Date.from(now));
            
            if (!in.getQuit()) {
                if (in.getToken() != null) {
                    answer.setToken(in.getToken());
                    QuickSearchStore.updateTimeStamp(in.getToken(), now.toEpochMilli());
                } else {
                    QuickSearchRequest result = new QuickSearchRequest(in.getSearch(), in.getReturnTypes(), answer.getResults());
                    answer.setToken(result.createToken(accessToken));
                    QuickSearchStore.putResult(answer.getToken(), result);
                }
            } else {
                QuickSearchStore.deleteResult(in.getToken());
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Throwable e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private static List<ResponseBaseSearchItem> getBasicSearch(final RequestQuickSearchFilter in, final SOSAuthFolderPermissions folderPermissions)
            throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            
            // TODO only temp. for compatibility
            if (in.getReturnType() != null) {
                if (in.getReturnTypes() == null) {
                    in.setReturnTypes(Collections.singletonList(in.getReturnType()));
                } else {
                    in.getReturnTypes().add(in.getReturnType());
                }
            }
            
            if (in.getToken() != null) {
                List<ResponseBaseSearchItem> result = QuickSearchStore.getResult(in);
                if (result != null) {
                    return result;
                } else {
                    // obsolete token
                    in.setToken(null);
                }
            }
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
            
            List<InventoryQuickSearchItem> items = dbLayer.getQuickSearchInventoryConfigurations(in.getReturnTypes(), in.getSearch());

            if (items != null) {
                Predicate<InventoryQuickSearchItem> isPermitted = item -> folderPermissions.isPermittedForFolder(item.getFolder());
                Comparator<InventoryQuickSearchItem> comp = Comparator.comparing(InventoryQuickSearchItem::getPath);
//                if (in.getReturnType() == null) {
//                    comp = comp.thenComparingInt(i -> i.getObjectType() == null ? 99 : i.getObjectType().intValue());
//                }
                return items.stream().filter(isPermitted).sorted(comp).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } finally {
            Globals.disconnect(session);
        }
    }
}
