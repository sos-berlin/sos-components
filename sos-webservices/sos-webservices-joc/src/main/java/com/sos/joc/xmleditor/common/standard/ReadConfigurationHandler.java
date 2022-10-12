package com.sos.joc.xmleditor.common.standard;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLNotMatchSchemaException;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.xmleditor.common.Utils;
import com.sos.joc.xmleditor.common.Xml2JsonConverter;

public class ReadConfigurationHandler {

    private ObjectType type;
    private ReadStandardConfigurationAnswer answer;

    public ReadConfigurationHandler(ObjectType objectType) {
        type = objectType;
        answer = new ReadStandardConfigurationAnswer();
    }

    public void readCurrent(DBItemXmlEditorConfiguration item, boolean forceRelease) throws Exception {
        answer.setSchema(JocXmlEditor.getFileContent(JocXmlEditor.getStandardAbsoluteSchemaLocation(type)));
        answer.setSchemaIdentifier(JocXmlEditor.getStandardSchemaIdentifier(type));
        answer.setReleased(false);
        answer.setHasReleases(false);
        answer.setState(ItemStateEnum.NO_CONFIGURATION_EXIST);
        answer.setConfiguration(null);
        answer.setConfigurationJson(null);
        answer.setRecreateJson(false);

        if (item == null || (item.getConfigurationDraft() == null && item.getConfigurationReleased() == null)) {
            return;
        }

        if (item.getConfigurationReleased() != null) {
            answer.setHasReleases(true);

            if (forceRelease || item.getConfigurationDraft() == null) {
                String xml = item.getConfigurationReleased();
                String json = item.getConfigurationReleasedJson();

                answer.setConfigurationDate(item.getReleased());
                answer.setConfiguration(xml);
                if (SOSString.isEmpty(json)) {
                    recreateJson(xml);
                } else {
                    deserializeJson(json, xml);
                }

                if (item.getConfigurationDraft() == null) {
                    answer.setReleased(true);
                    answer.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                } else {
                    if (item.getReleased() != null && item.getReleased().after(item.getModified())) {
                        answer.setState(ItemStateEnum.RELEASE_IS_NEWER);
                    } else {
                        answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
                    }
                }
                return;
            }
        }

        if (item.getConfigurationDraft() != null) {
            String xml = item.getConfigurationDraft();
            String json = item.getConfigurationDraftJson();

            answer.setConfigurationDate(item.getModified());
            answer.setConfiguration(xml);
            if (SOSString.isEmpty(json)) {
                recreateJson(xml);
            } else {
                deserializeJson(json, xml);
            }

            if (answer.getHasReleases()) {
                if (item.getReleased() != null && item.getReleased().after(item.getModified())) {
                    answer.setState(ItemStateEnum.RELEASE_IS_NEWER);
                } else {
                    answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
                }
            } else {
                answer.setState(ItemStateEnum.RELEASE_NOT_EXIST);
            }
        }
    }

    private void recreateJson(String xml) throws Exception {
        answer.setRecreateJson(true);
        try {
            answer.setConfigurationJson(convert(type, xml));
        } catch (SOSXMLNotMatchSchemaException e) {
            answer.setRecreateJson(false);
        }
    }

    private void deserializeJson(String json, String xml) throws Exception {
        try {
            answer.setConfigurationJson(Utils.deserializeJson(json));
        } catch (Throwable e) {
            recreateJson(xml);
        }
    }

    public ReadStandardConfigurationAnswer getAnswer() {
        return answer;
    }

    private String convert(ObjectType type, String xmlConfiguration) throws Exception {
        Xml2JsonConverter converter = new Xml2JsonConverter();
        return converter.convert(type, JocXmlEditor.getStandardAbsoluteSchemaLocation(type), xmlConfiguration);
    }

}
