package com.shntec.json2action;

import java.lang.reflect.Modifier;
import java.net.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class Generator {
	private final Parser parser = new Parser();
	private URL url = null;
	private String packagename = "";
	private String classname = "";
	
	public Generator(String packagename, String classname, URL jsonfile)
	{
		//ObjectNode schemaNode = parser.parse(jsonfile);
		this.url = jsonfile;
		this.packagename = packagename;
		this.classname = classname;
	};
	
	public void generate(){
		if (null != this.url)
		{
			JCodeModel codeModel = new JCodeModel();
			ObjectNode schemaNode = parser.parse(this.url);
			Schema schema = new Schema(null, schemaNode);
			
			JPackage jpackage = codeModel._package(this.packagename);
			

			_generator(this.classname, schemaNode, jpackage, schema);			
		}
	};
	
	private JType _generator(String nodeName, JsonNode schemaNode, JClassContainer generatableType, Schema schema){
		JType javaType = null;
		if (schemaNode.has("$ref"))
		{
			return null;
		}
		else
		{
			System.out.println("no ref");
			if (schemaNode.has("enum")) {
				
			}
			else
			{
				javaType = typeProccess(nodeName, schemaNode, generatableType, schema);
			}
			schema.setJavaTypeIfEmpty(javaType);
			return javaType;
		}
	};
	
	private JType typeProccess(String nodeName, JsonNode node, JClassContainer jClassContainer, Schema currentSchema){
		String typename = "any";
		JType type;
		
		if (node.has("type") && node.get("type").isArray() && node.get("type").size() > 0) {
            typename = node.get("type").get(0).asText();
        }
		
        if (node.has("type")) {
            typename = node.get("type").asText();
        }

        System.out.println(typename);
        
        if (typename.equals("string")) {
        	type = jClassContainer.owner().ref(String.class);
        }
        else if (typename.equals("number")){
        	type = jClassContainer.owner().ref(Double.class);
        }
        else if (typename.equals("integer")){
        	type = jClassContainer.owner().ref(Boolean.class);
        }
        else if (typename.equals("boolean")){
        	
        }
        else if (typename.equals("object")){
        	type = objectProcess(nodeName, node, jClassContainer.getPackage(), currentSchema);
        }
        else if (typename.equals("array")){
        	
        }
        else
        {
        	type = jClassContainer.owner().ref(Object.class);
        }
		return null;
	};
	
	private JType objectProcess(String nodeName, JsonNode node, JPackage _package, Schema schema){
		JType superType = _package.owner().ref(Object.class);
		boolean beFinal = false;
        if (node.has("extends")) {
            superType = _generator(nodeName + "Parent", node.get("extends"), _package, schema);
        }
        
        if(superType.isPrimitive())
        {
        	return superType;
        }
        
        try
        {
        	Class<?> javaClass = Class.forName(superType.fullName());
        	beFinal = Modifier.isFinal(javaClass.getModifiers());
        }
		catch(ClassNotFoundException e)
		{
			beFinal = false;
		}
        
        if (beFinal)
        	return superType;
        
        JDefinedClass jclass;
	}
	
	private JDefinedClass createClass(String nodeName, JsonNode node, JPackage _package) throws ClassAlreadyExistsException {

        JDefinedClass newType;

        try {
            if (node.has("javaType")) {
                String fqn = node.get("javaType").asText();

                if (isPrimitive(fqn, _package.owner())) {
                    throw new ClassAlreadyExistsException(primitiveType(fqn, _package.owner()));
                }

                try {
                    Class<?> existingClass = Thread.currentThread().getContextClassLoader().loadClass(fqn);
                    throw new ClassAlreadyExistsException(_package.owner().ref(existingClass));
                } catch (ClassNotFoundException e) {
                    newType = _package.owner()._class(fqn);
                }
            } else {
                newType = _package._class(getClassName(nodeName));
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }

//        ruleFactory.getAnnotator().propertyInclusion(newType);

        return newType;

    }
}
