package com.sos.annotator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

public class SOSAnnotator extends Jackson2Annotator {
	
	/*
	 * With this annotator you can 
	 * 
	 * 1. specify an property order for serialization
	 * The object in the JSON schema needs:
	 * "propertyOrder": [ "TYPE", ... ]
	 * 
	 * 2. specify, that a field(1), a getter(2) and/or setter(4) will be ignore
	 * with @JsonIgnore annotation
	 * The property in the JSON schema needs:
	 * "ignore": integer (similar to unix permissions)
	 * 
	 * 3. Annotate Custom-JsonTypeInfo (not working)
	 * 
	 */
	
	private Map<String, Integer> ignoreValues = new HashMap<String, Integer>();

	public SOSAnnotator(GenerationConfig generationConfig) {
		super(generationConfig);
	}
	
	@Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
		super.propertyInclusion(clazz, schema);
//		if (schema.has("annotation")) {
//			switch (schema.get("annotation").asText()) {
//			case "instruction": 
//				clazz.annotate(InstructionJsonTypeInfo.class);
//				break;
//			case "command":
//				clazz.annotate(CommandJsonTypeInfo.class);
//				break;
//			case "deployObject":
//				clazz.annotate(DeployObjectJsonTypeInfo.class);
//				break;
//			case "deleteObject":
//				clazz.annotate(DeleteObjectJsonTypeInfo.class);
//				break;
//			case "executable":
//				clazz.annotate(ExecutableJsonTypeInfo.class);
//				break;
//			}
//		}
		if (schema.has("properties")) {
			propertyOrder(clazz, schema.get("properties"), schema.get("propertyOrder"), schema.get("extends") != null);
        }
    }

	@Override
	public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
		//
	}
	
	public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode, JsonNode propertyOrder, boolean hasExtends) {
		JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");
		List<String> propertyOrderList = new ArrayList<String>();
		
		if (propertyOrder != null && propertyOrder.isArray()) {
			for (Iterator<JsonNode> iterator = propertyOrder.elements(); iterator.hasNext();) {
	            String propertyName = iterator.next().asText();
	            annotationValue.param(propertyName);
	            propertyOrderList.add(propertyName);
	        }
		} else if(hasExtends) {
			annotationValue.param("TYPE");
            propertyOrderList.add("TYPE");
		}
		
		for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
			String fieldName = properties.next();
			if (!propertyOrderList.contains(fieldName)) {
				annotationValue.param(fieldName);
			}
		}
	}
	
	@Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
		super.propertyField(field, clazz, propertyName, propertyNode);
		ignoreValues.put(propertyName, 0);
		if (propertyNode.hasNonNull("ignore")) {
			ignoreValues.put(propertyName, propertyNode.get("ignore").asInt(0));
			if (propertyNode.get("ignore").asInt(0) % 2 == 1) {
				field.annotate(JsonIgnore.class);
			}
		}
    }

    @Override
    public void propertyGetter(JMethod getter, String propertyName) {
    	super.propertyGetter(getter, propertyName);
    	if (ignoreValues.get(propertyName) == 2 || ignoreValues.get(propertyName) == 3 || ignoreValues.get(propertyName) == 6) {
    		getter.annotate(JsonIgnore.class);
    	}
    }

    @Override
    public void propertySetter(JMethod setter, String propertyName) {
    	super.propertySetter(setter, propertyName);
    	if (ignoreValues.get(propertyName) > 3) {
    		setter.annotate(JsonIgnore.class);
    	}
    }

}
