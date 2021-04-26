package com.sos.annotator;

import org.jsonschema2pojo.AbstractAnnotator;
import org.jsonschema2pojo.Annotator;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

public class SOSAnnotator  extends AbstractAnnotator implements Annotator {

	@Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
		super.propertyField(field, clazz, propertyName, propertyNode);
		if (propertyNode.hasNonNull("alias")) {
		    JAnnotationArrayMember annotationValue = field.annotate(JsonAlias.class).paramArray("value");
		    for (String alias : propertyNode.get("alias").asText().trim().split("\\s*,\\s*")) {
                annotationValue.param(alias); 
            }
		}
    }

}
