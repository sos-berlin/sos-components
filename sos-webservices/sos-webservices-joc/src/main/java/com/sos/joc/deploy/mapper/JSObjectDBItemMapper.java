package com.sos.joc.deploy.mapper;

import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.model.deploy.JSObject;

public class JSObjectDBItemMapper {

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
		if (jsObject.getValid() == null) {
			dbItem.setValid(false);
		} else {
			dbItem.setValid(jsObject.getValid());
		}
		dbItem.setVersion(jsObject.getVersion());
		dbItem.setId(jsObject.getId());
		return dbItem;
	}

	public static JSObject mapDBitemToJsObject (final DBItemJSObject dbItem) {
		JSObject jsObject = new JSObject();
		jsObject.setComment(dbItem.getComment());
		jsObject.setContent(dbItem.getContent());
		jsObject.setEditAccount(dbItem.getEditAccount());
		jsObject.setModified(dbItem.getModified());
		jsObject.setObjectType(dbItem.getObjectType());
		jsObject.setParentVersion(dbItem.getParentVersion());
		jsObject.setPath(dbItem.getPath());
		jsObject.setPublishAccount(dbItem.getPublishAccount());
		jsObject.setJobschedulerId(dbItem.getSchedulerId());
		jsObject.setState(dbItem.getState());
		jsObject.setUri(dbItem.getUri());
		jsObject.setValid(dbItem.isValid());
		jsObject.setVersion(dbItem.getVersion());
		jsObject.setId(dbItem.getId());
		return jsObject;
	}

}
