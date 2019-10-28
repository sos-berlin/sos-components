package com.sos.joc.deploy.mapper;

import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.model.deploy.JSObject;

public class JSObjectToDBItemMapper {

	public static DBItemJSObject mapJsObjectToDBitem (final JSObject jsObject) {
		DBItemJSObject dbItem = new DBItemJSObject();
		dbItem.setComment(jsObject.getComment());
		dbItem.setContent(jsObject.getContent());
		dbItem.setEditAccount(jsObject.getEditAccount());
		dbItem.setModified(jsObject.getModified());
		dbItem.setObjectType(jsObject.getObjectType());
		dbItem.setParentVersion(jsObject.getParentVersion());
		dbItem.setPath(jsObject.getPath());
		dbItem.setPublishAccount(jsObject.getPublishAccount());
		dbItem.setSchedulerId(jsObject.getJobschedulerId());
		dbItem.setState(jsObject.getState());
		dbItem.setUri(jsObject.getUri());
		dbItem.setValid(jsObject.getValid());
		dbItem.setVersion(jsObject.getVersion());
		dbItem.setId(jsObject.getId());
		return dbItem;
	}

}
