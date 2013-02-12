package com.shntec.json2action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import static java.lang.Character.*;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.*;
import static javax.lang.model.SourceVersion.*;

import org.apache.commons.lang.WordUtils;
import org.eel.kitchen.jsonschema.report.ValidationReport;

public class Generator {
	public class ClassAlreadyExistsException extends Exception {

	    private final JType existingClass;

	    /**
	     * Creates a new exception where the given existing class was found to
	     * conflict with an attempt to create a new class.
	     * 
	     * @param existingClass
	     *            the class already present on the classpath (or in the map of
	     *            classes to be generated) when attempt to create a new class
	     *            was made.
	     */
	    public ClassAlreadyExistsException(JType existingClass) {
	        this.existingClass = existingClass;
	    }

	    /**
	     * Gets the corresponding existing class that caused this exception.
	     * 
	     * @return the class already present on the classpath (or in the map of
	     *         classes to be generated) when attempt to create a new class was
	     *         made.
	     */
	    public JType getExistingClass() {
	        return existingClass;
	    }

	};
	
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
	
	public void generate(JCodeModel codeModel) throws JClassAlreadyExistsException{
		if (null != this.url)
		{
			ObjectNode schemaNode = parser.parse(this.url);
			Schema schema = new Schema(null, schemaNode);
			
			JPackage jpackage = codeModel._package(this.packagename);
			
			_generator(this.classname, schemaNode, jpackage, schema);
		}
	};
	
	public JCodeModel generate() throws JClassAlreadyExistsException{
		if (null != this.url)
		{
			JCodeModel codeModel = new JCodeModel();
			ObjectNode schemaNode = parser.parse(this.url);
			Schema schema = new Schema(null, schemaNode);
			
			JPackage jpackage = codeModel._package(this.packagename);

			_generator(this.classname, schemaNode, jpackage, schema);
			
			return codeModel;
		}
		else
			return null;
	};
	
	private JType _generator(String nodeName, JsonNode schemaNode, JClassContainer generatableType, Schema schema) throws JClassAlreadyExistsException{
		JType javaType = null;
		if (schemaNode.has("$ref"))
		{
			return null;
		}
		else
		{
			System.out.println("no ref");
			if (schemaNode.has("enum")) {
				javaType = enumProccess(nodeName, schemaNode, generatableType, schema);
			}
			else
			{
				javaType = typeProccess(nodeName, schemaNode, generatableType, schema);
			}
			schema.setJavaTypeIfEmpty(javaType);
			return javaType;
		}
	};
	
	private JType typeProccess(String nodeName, JsonNode node, JClassContainer jClassContainer, Schema currentSchema) throws JClassAlreadyExistsException{
		String typename = "any";
		JType type = null;
		
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
        	type = jClassContainer.owner().ref(Integer.class);
        }
        else if (typename.equals("boolean")){
        	type = jClassContainer.owner().ref(Boolean.class);
        }
        else if (typename.equals("object")){
        	//type = objectProcess(nodeName, node, jClassContainer.getPackage(), currentSchema);
        	if (jClassContainer.isClass())
        	{
        		//type = jClassContainer._class(Modifier.PUBLIC, nodeName);
        		type = jClassContainer._class(Modifier.PUBLIC, nodeName);
        		propertiesProccess(nodeName, node.get("properties"), (JDefinedClass)type, currentSchema);
        		if ("Response" == nodeName)
        		{
        			((JDefinedClass)type)._implements(jClassContainer.owner().ref("ResponseBase"));
        			//createToJson((JDefinedClass)type);
        		}
        		else if ("Action" == nodeName)
        		{
        			((JDefinedClass)type)._extends(jClassContainer.owner().ref("ActionBase"));        			
        		}
        		//type = objectProcess(nodeName, node, jClassContainer.getPackage(), currentSchema);
        	}
        	else
        	{
        		type = objectProcess(nodeName, node, jClassContainer.getPackage(), currentSchema);
        		JFieldVar schemaField = ((JDefinedClass)type).field(JMod.PRIVATE|JMod.STATIC, jClassContainer.owner().ref(String.class), "JSONSCHEMA");
        		schemaField.init(JExpr.lit(node.toString()));
        		createConstructor((JDefinedClass)type, node);
        		createDoAction((JDefinedClass)type);
        		createValidate((JDefinedClass)type);
        	}
        }
        else if (typename.equals("array")){
        	type = arrayProcess(nodeName, node, jClassContainer.getPackage(), currentSchema);
        }
        else
        {
        	type = jClassContainer.owner().ref(Object.class);
        }
		return type;
	};
	
	private void createToJson(JDefinedClass jClass){
		//Map m = jClass.fields();
		//Iterator it = m.entrySet().iterator();
		//ObjectMapper mapper = new ObjectMapper();
		JMethod toJsonFunc = jClass.method(JMod.PUBLIC, String.class, "ToJson");
		toJsonFunc.body().directStatement("");
	};
	
	private void createValidate(JDefinedClass jClass){
		JMethod validatefunc = jClass.method(JMod.PRIVATE, ValidationReport.class, "validate");
		validatefunc._throws(IOException.class);
		
		JBlock jBlock = validatefunc.body();
		validatefunc.param(String.class, "rawdata");
		//ObjectMapper objectmap = new ObjectMapper();
		JClass jClass_varomImpl = jClass.owner().ref(ObjectMapper.class);
		JVar jvaromImpl = jBlock.decl(jClass_varomImpl, "objectmap");
		jvaromImpl.init(JExpr._new(jClass_varomImpl));

		//JsonNode schema = objectmap.readTree(JSONSCHEMA);
		JClass jClass_varschemanodeImpl = jClass.owner().ref(JsonNode.class);
		JVar jvarschemanodeImpl = jBlock.decl(jClass_varschemanodeImpl, "schemanode");
		jvarschemanodeImpl.init(JExpr.direct("objectmap.readTree(JSONSCHEMA)"));

		//JsonSchemaFactory factory = JsonSchemaFactory.defaultFactory();
		JClass jClass_varfactoryImpl = jClass.owner().ref("org.eel.kitchen.jsonschema.main.JsonSchemaFactory");
		JVar jvarfactoryImpl = jBlock.decl(jClass_varfactoryImpl, "factory");
		jvarfactoryImpl.init(JExpr.direct("JsonSchemaFactory.defaultFactory()"));
		
		//JsonSchema schema = factory.fromSchema(fstabSchema);
		JClass jClass_varschemaImpl = jClass.owner().ref("org.eel.kitchen.jsonschema.main.JsonSchema");
		JVar jvarschemaImpl = jBlock.decl(jClass_varschemaImpl, "schema");
		jvarschemaImpl.init(JExpr.direct("factory.fromSchema(schemanode)"));
		//JsonNode request = objectmap.readTree(rawdata)
		JClass jClass_varrequestImpl = jClass.owner().ref(JsonNode.class);
		JVar jvarrequestImpl = jBlock.decl(jClass_varrequestImpl, "request");
		jvarrequestImpl.init(JExpr.direct("objectmap.readTree(rawdata)"));
		//return schema.validate(request).isSucces();
		jBlock._return(JExpr.direct("schema.validate(request)"));
	};
	
	private void createDoAction(JDefinedClass jClass){
		Map m = jClass.fields();
		Iterator it = m.entrySet().iterator();
		boolean hasResponse = false;
		
		while(it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == "Response")
            {
            	hasResponse = true;
            	break;
            }
		};
		JMethod doFunction = null;
		if (!hasResponse)
			doFunction = jClass.method(JMod.PUBLIC, jClass.owner().VOID, "doAction");
		else
		{
			doFunction = jClass.method(JMod.PUBLIC, jClass.owner().ref("ResponseBase"), "doAction");
			doFunction.body()._return(JExpr._null());
		}
	};
	
	private void createConstructor(JDefinedClass jClass, JsonNode node){
		JMethod constructor = jClass.constructor(JMod.PUBLIC);
		JBlock jBlock = constructor.body();
		jBlock.directStatement("Action = new Action();");
		jBlock.directStatement("Parameter = new Parameter();");
		
		ObjectMapper O_MAPPER = new ObjectMapper();
		JsonNode root = null;
		try {
			root = O_MAPPER.readTree(new File(this.url.toURI()));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if (root.has("Action"))
		{
			JsonNode anode = root.get("Action");
			System.out.println(node);
			//jBlock.directStatement("Action.")
			Iterator it = anode.fields();
			
			while(it.hasNext()){
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String)entry.getKey();
		        JsonNode value = (JsonNode)entry.getValue();
		        if ("string".equalsIgnoreCase(node.get("properties").get("Action").get("properties").get(key).get("type").asText()))
		        {
		        	jBlock.directStatement("Action.set" + key + "(\"" + value.asText() + "\");");
		        }
		        else
		        {
		        	jBlock.directStatement("Action.set" + key + "(" + value.asText() + ");");
		        }
		        
			}
		}
		//constructor.param(jClass.owner().ref("String"), "RequestJson");
	}
	
	private JDefinedClass enumProccess(String nodeName, JsonNode node, JClassContainer jContainer, Schema schema){
		JDefinedClass _enum = this.createEnum(nodeName, jContainer);
		schema.setJavaTypeIfEmpty(_enum);
		
		JFieldVar valueField = addValueField(_enum);
		addToString(_enum, valueField);
        addEnumConstants(node, _enum);
        addFactoryMethod(node, _enum);
		return _enum;
	};
	
	private JClass arrayProcess(String nodeName, JsonNode node, JPackage jpackage, Schema schema) throws JClassAlreadyExistsException {

        boolean uniqueItems = node.has("uniqueItems") && node.get("uniqueItems").asBoolean();
        boolean rootSchemaIsArray = !schema.isGenerated();

        JType itemType;
        if (node.has("items")) {
            itemType = _generator(makeSingular(nodeName), node.get("items"), jpackage, schema);
        } else {
            itemType = jpackage.owner().ref(Object.class);
        }

        JClass arrayType;
        if (uniqueItems) {
            arrayType = jpackage.owner().ref(Set.class).narrow(itemType);
        } else {
            arrayType = jpackage.owner().ref(List.class).narrow(itemType);
        }

        if (rootSchemaIsArray) {
            schema.setJavaType(arrayType);
        }

        return arrayType;
    };
    
	private JType objectProcess(String nodeName, JsonNode node, JPackage _package, Schema schema) throws JClassAlreadyExistsException{
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
        try
        {
        	jclass = createClass(nodeName, node, _package);
        }
        catch (ClassAlreadyExistsException e) 
        {
            return e.getExistingClass();
        }
        
        jclass._extends((JClass) superType);
        schema.setJavaTypeIfEmpty(jclass);
        
        if (node.has("title")) {

        }

        if (node.has("description")) {

        }

        if (node.has("properties")) {
            propertiesProccess(nodeName, node.get("properties"), jclass, schema);
        }

        
        return jclass;
	}
	
	private JDefinedClass propertiesProccess(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) throws JClassAlreadyExistsException
	{
		for (Iterator<String> properties = node.fieldNames(); properties.hasNext();) {
            String property = properties.next();

            propertyProcess(property, node.get(property), jclass, schema);
        }
		return jclass;
	};
	
	private JDefinedClass propertyProcess(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) throws JClassAlreadyExistsException{		
		String propertyName = nodeName;
		
		JType propertyType = _generator(nodeName, node, jclass, schema);
		
		JFieldVar field = jclass.field(JMod.PRIVATE, propertyType, propertyName);
		
		JMethod getter = addGetter(jclass, field, nodeName);
        JMethod setter = addSetter(jclass, field, nodeName);
        
		return jclass;
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
                newType = _package._class(nodeName);
            }
        } catch (JClassAlreadyExistsException e) {
            throw new ClassAlreadyExistsException(e.getExistingClass());
        }


        return newType;

    };
    
    private boolean isPrimitive(String name, JCodeModel owner) {
        try {
            return JType.parse(owner, name) != owner.VOID;
        } catch (IllegalArgumentException e) {
            return false;
        }
    };
    
    
    private static JPrimitiveType primitiveType(String name, JCodeModel owner) {
        try {
            return (JPrimitiveType) owner.parseType(name);
        } catch (ClassNotFoundException e) {
        	throw new GenerationException(
                    "Given name does not refer to a primitive type, this type can't be found: "
                            + name, e);
        }
    }
    
    private JMethod addGetter(JDefinedClass c, JFieldVar field, String jsonPropertyName) {
        JMethod getter = c.method(JMod.PUBLIC, field.type(), ((field.type().equals(field.type().owner()._ref(boolean.class))) ? "is" : "get") + jsonPropertyName);

        JBlock body = getter.body();
        body._return(field);

        return getter;
    }

    private JMethod addSetter(JDefinedClass c, JFieldVar field, String jsonPropertyName) {
        JMethod setter = c.method(JMod.PUBLIC, void.class, "set" + jsonPropertyName);

        JVar param = setter.param(field.type(), field.name());
        JBlock body = setter.body();
        body.assign(JExpr._this().ref(field), param);

        return setter;
    }
    
    private String makeSingular(String nodeName) {

        if (endsWith(nodeName, "ies")) {
            return removeEnd(nodeName, "ies") + "y";
        } else {
            return removeEnd(removeEnd(nodeName, "s"), "S");
        }

    }
    
    private JDefinedClass createEnum(String nodeName, JClassContainer container){
    	int modifiers = container.isPackage() ? JMod.PUBLIC : JMod.PUBLIC | JMod.STATIC;

        String name = nodeName;
        
        try {
            return container._class(modifiers, name, ClassType.ENUM);
        } catch (JClassAlreadyExistsException e) {
            throw new GenerationException(e);
        }
    }
   
    private JFieldVar addValueField(JDefinedClass _enum){
    	JFieldVar valueField = _enum.field(JMod.PRIVATE | JMod.FINAL, String.class, "value");

        JMethod constructor = _enum.constructor(JMod.PRIVATE);
        JVar valueParam = constructor.param(String.class, "value");
        JBlock body = constructor.body();
        body.assign(JExpr._this().ref(valueField), valueParam);
    	return valueField;
    }
    
    private void addToString(JDefinedClass _enum, JFieldVar valueField){
    	JMethod toString = _enum.method(JMod.PUBLIC, String.class, "toString");
        JBlock body = toString.body();

        body._return(JExpr._this().ref(valueField));
    };
    
    private void addEnumConstants(JsonNode node, JDefinedClass _enum) {
        for (Iterator<JsonNode> values = node.elements(); values.hasNext();) {
            JsonNode value = values.next();

            if (!value.isNull()) {
                JEnumConstant constant = _enum.enumConstant(getConstantName(value.asText()));
                constant.arg(JExpr.lit(value.asText()));
            }
        }
    };
    
    private String getConstantName(String nodeName) {
    	List<String> enumNameGroups = new ArrayList<String>(asList(splitByCharacterTypeCamelCase(nodeName)));

        String enumName = "";

        enumName = upperCase(join(enumNameGroups, "_"));

        if (isEmpty(enumName)) {
            enumName = "__EMPTY__";
        } else if (Character.isDigit(enumName.charAt(0))) {
            enumName = "_" + enumName;
        }
        
        return enumName;
    };
    
    private void addFactoryMethod(JsonNode node, JDefinedClass _enum) {
        JMethod fromValue = _enum.method(JMod.PUBLIC | JMod.STATIC, _enum, "fromValue");
        JVar valueParam = fromValue.param(String.class, "value");
        JBlock body = fromValue.body();

        JForEach forEach = body.forEach(_enum, "c", _enum.staticInvoke("values"));

        JInvocation invokeEquals = forEach.var().ref("value").invoke("equals");
        invokeEquals.arg(valueParam);

        forEach.body()._if(invokeEquals)._then()._return(forEach.var());

        JInvocation illegalArgumentException = JExpr._new(_enum.owner().ref(IllegalArgumentException.class));
        illegalArgumentException.arg(valueParam);
        body._throw(illegalArgumentException);
    }
}
