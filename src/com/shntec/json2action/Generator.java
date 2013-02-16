package com.shntec.json2action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
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

import com.shntec.json2action.demo.ActionBase;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JForLoop;
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
	private int deep = 0;
	private int count = 0;
	private boolean genInitCode = false;
	private JDefinedClass actionFactory = null;
	private JMethod jM_InitParameter = null;
	
	public Generator(String packagename, String classname, URL jsonfile)
	{
		this.url = jsonfile;
		this.packagename = packagename;
		this.classname = classname;
	};
	
	public Generator(String packagename)
	{
		this.packagename = packagename;
	};
	
	public void genBaseClass(JCodeModel codeModel) throws JClassAlreadyExistsException{
		JPackage jp = codeModel._package(this.packagename);
		
		JDefinedClass respBase = jp._class(JMod.PUBLIC|JMod.ABSTRACT, "ResponseBase");
		createToJSON(respBase);
		
		JDefinedClass handlerInterface = jp._interface("IActionHandler");
		handlerInterface.method(JMod.PUBLIC, respBase, "doAction");
		
		JDefinedClass handlerBase = jp._class(JMod.PUBLIC|JMod.ABSTRACT, "ActionHandler");
		handlerBase.method(JMod.PUBLIC|JMod.ABSTRACT, respBase, "doAction");
		handlerBase.method(JMod.PUBLIC|JMod.ABSTRACT, String.class, "getJsonSchema");
		handlerBase.method(JMod.PUBLIC|JMod.ABSTRACT, codeModel.ref("ActionBase"), "getAction");
		
		JDefinedClass errorResp = jp._class("ErrorResponse");
		errorResp._extends(respBase);
		errorResp.field(JMod.PUBLIC, String.class, "More");
		errorResp.field(JMod.PUBLIC, codeModel.INT, "Code");		
		JMethod errorRespConstructor = errorResp.constructor(JMod.PUBLIC);
		errorRespConstructor.param(String.class, "More");
		errorRespConstructor.param(codeModel.INT, "Code");
		errorRespConstructor.body().directStatement("this.More = More;");
		errorRespConstructor.body().directStatement("this.Code = Code;");
		errorResp.method(JMod.PUBLIC, String.class, "getMore").body()._return(JExpr.refthis("More"));
		errorResp.method(JMod.PUBLIC, codeModel.INT, "getCode").body()._return(JExpr.refthis("Code"));
		JDefinedClass emptyResp = jp._class("EmptyResponse");
		emptyResp._extends(respBase);
		
		JDefinedClass actionBase = jp._class(JMod.ABSTRACT | JMod.PUBLIC, "ActionBase");
		actionBase.method(JMod.ABSTRACT | JMod.PUBLIC, codeModel.VOID, "setName").param(String.class, "Name");
		actionBase.method(JMod.ABSTRACT | JMod.PUBLIC, codeModel.VOID, "setMsg").param(String.class, "Msg");
		actionBase.method(JMod.ABSTRACT | JMod.PUBLIC, codeModel.VOID, "setResult").param(Boolean.class, "Result");
		actionBase.method(JMod.ABSTRACT | JMod.PUBLIC, codeModel.VOID, "setCode").param(Integer.class, "Code");
		createToJSON(actionBase);
		
		//ExceptionAction
		
		JDefinedClass exceptionAction = jp._class(JMod.PUBLIC, "ExceptionAction");
		exceptionAction._extends(codeModel.ref("ActionHandler"));
		exceptionAction.field(JMod.PRIVATE, codeModel.ref("ActionHandler"), "hSource").init(JExpr._null());
		exceptionAction.field(JMod.PRIVATE, codeModel.ref(ValidationReport.class), "vReport").init(JExpr._null());
		exceptionAction.field(JMod.PRIVATE, codeModel.ref("ErrorResponse"), "eResp").init(JExpr._null());
		JMethod eConstructor = exceptionAction.constructor(JMod.PUBLIC);
		eConstructor.param(codeModel.ref(ValidationReport.class), "report");
		eConstructor.param(codeModel.ref("ActionHandler"), "src");
		eConstructor.body().directStatement("hSource = src;");
		eConstructor.body().directStatement("vReport = report;");
		eConstructor.body().directStatement("eResp = new ErrorResponse(vReport.getMessages().get(0), 5001);");
		
		JBlock e_getaction = exceptionAction.method(JMod.PUBLIC, codeModel.ref("ActionBase"), "getAction").body();
		e_getaction.directStatement("hSource.getAction().setResult(false);");
		e_getaction.directStatement("hSource.getAction().setMsg(\"缺少必要的参数。\");");
		e_getaction.directStatement("hSource.getAction().setCode(500);");
		e_getaction._return(JExpr.direct("(ActionBase)hSource.getAction()"));
		
		exceptionAction.method(JMod.PUBLIC, codeModel.ref("ResponseBase"), "doAction").body()._return(JExpr.direct("(ResponseBase)eResp"));
		exceptionAction.method(JMod.PUBLIC, String.class, "getJsonSchema").body()._return(JExpr.direct("\"{}\""));
		
		this.actionFactory = jp._class("ActionFactory");
		
		jM_InitParameter = actionFactory.method(JMod.PUBLIC, codeModel.VOID, "InitParameter");
	};
	
	public void genFactoryClass(JCodeModel codeModel, Map<String, String> PointMap) throws JClassAlreadyExistsException, SecurityException, NoSuchFieldException{
		JPackage jp = codeModel._package(this.packagename);
		
		Iterator<JDefinedClass> it = jp.classes();
		boolean hasUnknownAction = false;
		while(it.hasNext())
		{
			JDefinedClass entry = (JDefinedClass) it.next();
			if ("UnknownAction" == entry.name())
			{
				hasUnknownAction = true;
				break;
			}
		}
		
		if (!hasUnknownAction)
		{
			createUnknownAction(codeModel, jp);
		}
		
		JClass mapclass = codeModel.ref(Map.class).narrow(String.class,Class.class);
		JClass hashmapclass = codeModel.ref(HashMap.class).narrow(String.class,Class.class);
		
		actionFactory.field(JMod.PRIVATE, mapclass, "ActionClassMap").init(JExpr._new(hashmapclass));
		JMethod constructor = actionFactory.constructor(JMod.PUBLIC);
		JBlock jBlock = constructor.body();
		
		Iterator iter = PointMap.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			String val = (String)entry.getValue();
			
			jBlock.directStatement("ActionClassMap.put(\"" + key + "\", " + val + ".class);");
			//createHandlerGetter(codeModel, actionFactory, val);
			createHandlerInit(codeModel, actionFactory, val);
		}
		
		createValidate(actionFactory);
		createInitActionHandler(codeModel, actionFactory);
		JMethod getAction = actionFactory.method(JMod.PUBLIC, codeModel.ref("ActionHandler"), "getAction");
		getAction.param(String.class, "RequestJSON");
		// JsonProcessingException, IOException, IllegalAccessException, IllegalArgumentException, InstantiationException, SecurityException, InvocationTargetException, NoSuchMethodException
		getAction._throws(JsonProcessingException.class);
		getAction._throws(IOException.class);
		getAction._throws(InstantiationException.class);
		getAction._throws(IllegalAccessException.class);
		getAction._throws(IllegalArgumentException.class);
		getAction._throws(InstantiationException.class);
		getAction._throws(SecurityException.class);
		getAction._throws(InvocationTargetException.class);
		getAction._throws(NoSuchMethodException.class);
		getAction.body().decl(codeModel.ref(ObjectMapper.class), "mapper").init(JExpr._new(codeModel.ref(ObjectMapper.class)));
		getAction.body().decl(codeModel.ref(JsonNode.class), "root").init(JExpr._null());
		getAction.body().directStatement("root = mapper.readTree(RequestJSON);");
		JConditional if1block = getAction.body()._if(JExpr.direct("root.has(\"Action\") && root.get(\"Action\").has(\"Name\")"));
		JConditional if2block = if1block._then()._if(JExpr.direct("null != ActionClassMap.get(root.get(\"Action\").get(\"Name\").asText())"));
		if2block._then().decl(codeModel.ref(ValidationReport.class), "report");
		if2block._then().decl(codeModel.ref("ActionHandler"), "handler").init(JExpr.direct("(ActionHandler)(ActionClassMap.get(root.get(\"Action\").get(\"Name\").asText()).newInstance())"));
		if2block._then().decl(codeModel.ref(JsonNode.class), "schemanode").init(JExpr.direct("mapper.readTree(handler.getJsonSchema())"));
		if2block._then().decl(codeModel.ref(ObjectNode.class), "propertiesnode").init(JExpr.direct("(ObjectNode)(schemanode.get(\"properties\"))"));
		if2block._then()._if(JExpr.direct("propertiesnode.has(\"Response\")"))._then().directStatement("propertiesnode.remove(\"Response\");");
		if2block._then().directStatement("report = validate(RequestJSON, schemanode.toString());");
		JConditional if3block = if2block._then()._if(JExpr.direct("report.isSuccess()"));
		if3block._then().directStatement("String methodName = \"init\" + ActionClassMap.get(root.get(\"Action\").get(\"Name\").asText()).getSimpleName();");
		if3block._then().directStatement("this.getClass().getMethod(methodName, ActionHandler.class, String.class).invoke(this, handler, RequestJSON);");
		//JsonNode.class
		//Method m = this.getClass().getMethod(name, parameterTypes)
		//JsonNode.class.getMethod(name, parameterTypes)
		if3block._then()._return(JExpr.ref("handler"));
		if3block._else()._return(JExpr.direct("new ExceptionAction(report, handler)"));
		if2block._else()._return(JExpr._new(codeModel.ref("UnknownAction")));
		
		getAction.body()._return(JExpr._new(codeModel.ref("UnknownAction")));
	}
	
	private void createHandlerInit(JCodeModel codeModel, JDefinedClass jClass, String HandlerName) throws SecurityException, NoSuchFieldException, JClassAlreadyExistsException
	{
		JMethod m = jClass.method(JMod.PUBLIC, codeModel.ref("ActionHandler"), "init" + HandlerName);
		m._throws(JsonProcessingException.class);
		m._throws(IOException.class);
		m._throws(InstantiationException.class);
		m._throws(IllegalAccessException.class);
		JVar param1 = m.param(codeModel.ref("ActionHandler"), "handler");
		JVar param2 = m.param(String.class, "rawdata");
		m.body().decl(codeModel.ref(HandlerName), "phandler", JExpr.direct("(" + HandlerName + ")handler"));
		
		JConditional ifblock = m.body()._if(JExpr.direct("\"UnknownAction\" != handler.getClass().getSimpleName() && \"ExceptionAction\" != handler.getClass().getSimpleName()"));
		ifblock._else()._return(JExpr.direct("handler"));
		
		JBlock jB = ifblock._then();
		jB.decl(codeModel.ref(ObjectMapper.class), "mapper").init(JExpr._new(codeModel.ref(ObjectMapper.class)));
		jB.decl(codeModel.ref(JsonNode.class), "root").init(JExpr._null());
		jB.directStatement("root = mapper.readTree(rawdata);");
		//Action init
		jB.directStatement("phandler.getAction().setSessionID(root.get(\"Action\").get(\"SessionID\").asText());");
		jB.directStatement("phandler.getAction().setFlag(root.get(\"Action\").get(\"Flag\").asInt());");
		//Parameter init
		JDefinedClass handlerClass = null;

		JPackage jp = codeModel._package(packagename);
		Iterator<JDefinedClass> it = jp.classes();
		while(it.hasNext()){
			JDefinedClass c = it.next();
			//System.out.println(c.fullName());
			if ((packagename + "." + HandlerName).equalsIgnoreCase(c.fullName()))
			{
				handlerClass = c;
				break;
			}
		}
		
		if (null != handlerClass)
		{
			JFieldVar jJsonSchema = handlerClass.fields().get("JSONSCHEMA");
			
			//System.out.println(jJsonSchema.decr().toString());
			
			if (null != handlerClass.fields().get("Parameter"))
			{
				//System.out.println(handlerClass.fields().get("Parameter").type().fullName());
				JDefinedClass parameterClass = null;
				it = handlerClass.classes();
				while(it.hasNext())
				{
					JDefinedClass c = it.next();
					if(c.name().equalsIgnoreCase("Parameter"))
					{
						parameterClass = c;
						break;
					}
				}
				
				if (null != parameterClass)
				{
					jB.decl(jClass.owner().ref(ArrayList.class), "list");

					processParameterInit(parameterClass, jB, "phandler.getParameter()", "root.get(\"Parameter\")");
					m.body()._return(JExpr.ref("phandler"));

				}
			}
		}
		else
		{
			System.out.println("handlerClass is null");
		}		

	}
	
	private void createHandlerGetter(JCodeModel codeModel, JDefinedClass jClass, String HandlerName) throws SecurityException, NoSuchFieldException, JClassAlreadyExistsException
	{
		JMethod m = jClass.method(JMod.PUBLIC, codeModel.ref(HandlerName), "get" + HandlerName);
		m._throws(JsonProcessingException.class);
		m._throws(IOException.class);
		m._throws(InstantiationException.class);
		m._throws(IllegalAccessException.class);
		JVar param = m.param(String.class, "rawdata");
		m.body().decl(codeModel.ref(HandlerName), "handler", JExpr.direct("(" + HandlerName + ")getAction(rawdata)"));
		
		JConditional ifblock = m.body()._if(JExpr.direct("\"UnknownAction\" != handler.getClass().getSimpleName() && \"ExceptionAction\" != handler.getClass().getSimpleName()"));
		ifblock._else()._return(JExpr.direct("handler"));
		
		JBlock jB = ifblock._then();
		jB.decl(codeModel.ref(ObjectMapper.class), "mapper").init(JExpr._new(codeModel.ref(ObjectMapper.class)));
		jB.decl(codeModel.ref(JsonNode.class), "root").init(JExpr._null());
		jB.directStatement("root = mapper.readTree(rawdata);");
		//Action init
		jB.directStatement("handler.getAction().setSessionID(root.get(\"Action\").get(\"SessionID\").asText());");
		jB.directStatement("handler.getAction().setFlag(root.get(\"Action\").get(\"Flag\").asInt());");
		//Parameter init
		JDefinedClass handlerClass = null;

		JPackage jp = codeModel._package(packagename);
		Iterator<JDefinedClass> it = jp.classes();
		while(it.hasNext()){
			JDefinedClass c = it.next();
			//System.out.println(c.fullName());
			if ((packagename + "." + HandlerName).equalsIgnoreCase(c.fullName()))
			{
				handlerClass = c;
				break;
			}
		}
		
		if (null != handlerClass)
		{
			JFieldVar jJsonSchema = handlerClass.fields().get("JSONSCHEMA");
			
			//System.out.println(jJsonSchema.decr().toString());
			
			if (null != handlerClass.fields().get("Parameter"))
			{
				//System.out.println(handlerClass.fields().get("Parameter").type().fullName());
				JDefinedClass parameterClass = null;
				it = handlerClass.classes();
				while(it.hasNext())
				{
					JDefinedClass c = it.next();
					if(c.name().equalsIgnoreCase("Parameter"))
					{
						parameterClass = c;
						break;
					}
				}
				
				if (null != parameterClass)
				{
					jB.decl(jClass.owner().ref(ArrayList.class), "list");

					processParameterInit(parameterClass, jB, "handler.getParameter()", "root.get(\"Parameter\")");
					m.body()._return(JExpr.ref("handler"));

				}
			}
		}
		else
		{
			System.out.println("handlerClass is null");
		}		
	}

	private void processParameterInit(JDefinedClass jClass, JBlock jBlock, String codePrefix, String jsonPrefix) throws JClassAlreadyExistsException
	{
		count++;
		Iterator it= jClass.fields().entrySet().iterator();
		
		while(it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String)entry.getKey();
			JFieldVar val = (JFieldVar)entry.getValue();
			String type = val.type().name();
			
			if ("string".equalsIgnoreCase(type))
			{
				jBlock.directStatement(codePrefix + ".set" + key + "(" + jsonPrefix + ".get(\"" + key + "\").asText());");
			}
			else if ( "boolean".equalsIgnoreCase(type))
			{
				jBlock.directStatement(codePrefix + ".set" + key + "(" + jsonPrefix + ".get(\"" + key + "\").asBoolean());");
			}
			else if ("double".equalsIgnoreCase(type))
			{
				jBlock.directStatement(codePrefix + ".set" + key + "(" + jsonPrefix + ".get(\"" + key + "\").asDouble());");
			}
			else if ("integer".equalsIgnoreCase(type))
			{
				jBlock.directStatement(codePrefix + ".set" + key + "(" + jsonPrefix + ".get(\"" + key + "\").asInt());");
			}
			else if (type.startsWith("List"))
			{
				String prefix = codePrefix + ".get" + key + "()";
				
				//jBlock.decl(jClass.owner().ref(List.class).narrow(val.type().boxify().getTypeParameters().get(0)), "arraylist").init(JExpr._new(jClass.owner().ref(ArrayList.class).narrow(val.type().boxify().getTypeParameters().get(0))));
				jBlock.directStatement(codePrefix + ".set" + key + "(new ArrayList<" + val.type().boxify().getTypeParameters().get(0).fullName() + ">());");
				//JsonNode n;
				//n.elements()
				//jBlock._for().init(jClass.owner().INT, "n", e)
				//jBlock.decl(jClass.owner().ref(Iterator.class).narrow(JsonNode.class), "it").init(JExpr.direct(jsonPrefix + ".get(\"" + key + "\").elements()"));
				//jBlock.assign(JExpr.ref("pointer"), JExpr.direct(jsonPrefix + ".get(\"" + key + "\").elements()"));
				
				JForLoop loop = jBlock._for();
				loop.init(jClass.owner().ref(Iterator.class), "it" + count, JExpr.direct(jsonPrefix + ".get(\"" + key + "\").elements()"));
				loop.test(JExpr.direct("it" + count + ".hasNext()"));
				
				JBlock whileblock = loop.body();
				String narrowName = val.type().boxify().getTypeParameters().get(0).fullName();
				System.out.println(narrowName);
				JDefinedClass cC = getSubClass(jClass, narrowName);
				//JDefinedClass cC = jClass._class(narrowName);
				//val.type().boxify().getTypeParameters()
				whileblock.decl(jClass.owner().ref(JsonNode.class), "node" + count).init(JExpr.direct("(JsonNode)it" + count + ".next()"));
				if (cC != null)
				{
					System.out.println(cC.fullName());					
					whileblock.decl(cC, "item").init(JExpr.direct(codePrefix + ".new " + narrowName.substring(narrowName.lastIndexOf(".") + 1) + "()"));
					processParameterInit(cC, whileblock, "item", "node"+count);
					whileblock.directStatement(prefix + ".add(item);");
				}
				else
				{
					String t = val.type().boxify().getTypeParameters().get(0).name();
					if ("string".equalsIgnoreCase(t))
					{
						whileblock.directStatement(prefix + ".add(node" + count + ".asText());");
					}
					else if ("double".equalsIgnoreCase(t))
					{
						whileblock.directStatement(prefix + ".add(node" + count + ".asDouble());");
					}
					else if ("integer".equalsIgnoreCase(t))
					{
						whileblock.directStatement(prefix + ".add(node" + count + ".asInt());");
					}
				}
				//whileblock.decl(type, name)
			}
			else
			{
				String cName = val.type().fullName();
				JDefinedClass cC = getSubClass(jClass, cName);
				//JDefinedClass cC = jClass.owner()._class(val.type().fullName());
				if (cC != null)
				{
					//JVar var = jBlock.decl(cC, "object").init(JExpr._new(cC));
					//jBlock.assign(var, JExpr.direct(codePrefix + ".set" + key + "(object)"));
					jBlock.directStatement(codePrefix + ".set" + key + "(" + codePrefix + ".new " + cC.name().substring(cC.name().lastIndexOf(".") + 1) +"());");
					processParameterInit(cC, jBlock, codePrefix + ".get" + key + "()", jsonPrefix + ".get(\"" + key + "\")");
				}
			}
		}
	}
	
	private JDefinedClass getSubClass(JDefinedClass jContainer, String SubCName)
	{
		Iterator<JDefinedClass> it = jContainer.classes();
		while(it.hasNext())
		{
			JDefinedClass jC = it.next();
			if(jC.fullName().equalsIgnoreCase(SubCName))
				return jC;
		}
		return null;
	}
	
	private void proccessLoop(JDefinedClass jClass, JBlock jBlock, String codePrefix, String jsonPrefix)
	{
		
	}
	
	private void createInitActionHandler(JCodeModel codeModel, JDefinedClass jClass)
	{
		JMethod m = jClass.method(JMod.PRIVATE, codeModel.VOID, "InitActionHandler");
		m.param(codeModel.ref(JsonNode.class), "json");
	}
	
	private void createUnknownAction(JCodeModel codeModel, JPackage jp) throws JClassAlreadyExistsException{
		JDefinedClass jClass = jp._class("UnknownAction");
		
		jClass._extends(codeModel.ref("ActionHandler"));
		//jClass.field(JMod.PRIVATE, codeModel, name)
		JDefinedClass jC = jClass._class(JMod.PUBLIC, "Action");
		jC._extends(codeModel.ref("ActionBase"));
		
		JFieldVar field = jC.field(JMod.PRIVATE, String.class, "Name");
		addGetter(jC, field, "Name");
		addSetter(jC, field, "Name");
		
		field = jC.field(JMod.PRIVATE, Integer.class, "Code");
		addGetter(jC, field, "Code");
		addSetter(jC, field, "Code");
		
		field = jC.field(JMod.PRIVATE, Boolean.class, "Result");
		addGetter(jC, field, "Result");
		addSetter(jC, field, "Result");
		
		field = jC.field(JMod.PRIVATE, String.class, "Msg");
		addGetter(jC, field, "Msg");
		addSetter(jC, field, "Msg");
		
		field = jC.field(JMod.PRIVATE, String.class, "SessionID");
		addGetter(jC, field, "SessionID");
		addSetter(jC, field, "SessionID");
		field.assign(JExpr.direct("\"\""));
		
		field = jC.field(JMod.PRIVATE, Integer.class, "Flag");
		addGetter(jC, field, "Flag");
		addSetter(jC, field, "Flag");
		field.assign(JExpr.direct("0"));

//		JMethod m = null;
//		jC.method(JMod.PUBLIC, String.class, "getName").body()._return(JExpr.refthis("Name"));
//		m = jC.method(JMod.PUBLIC, codeModel.VOID, "setName");
//		m.param(String.class, "Name");
//		m.body().directStatement("this.Name = Name;");
//		
//		jC.method(JMod.PUBLIC, Integer.class, "getCode").body()._return(JExpr.refthis("Code"));
//		m = jC.method(JMod.PUBLIC, codeModel.VOID, "setCode");
//		m.param(Integer.class, "Code");
//		m.body().directStatement("this.Code = Code;");
		
		jClass.field(JMod.PRIVATE, jC, "Action");
		
		jClass.method(JMod.PUBLIC, codeModel.ref("ResponseBase"), "doAction").body()._return(JExpr._new(codeModel.ref("EmptyResponse")));
		jClass.method(JMod.PUBLIC, String.class, "getJsonSchema").body()._return(JExpr.direct("\"{}\""));
		jClass.method(JMod.PUBLIC, codeModel.ref("ActionBase"), "getAction").body()._return(JExpr.refthis("Action"));
		
		JBlock jB = jClass.constructor(JMod.PUBLIC).body();
		jB.assign(JExpr.refthis("Action"), JExpr._new(jC));
		jB.directStatement("this.Action.setName(\"UNKNOWNACTION\");");
		jB.directStatement("this.Action.setCode(5000);");
		jB.directStatement("this.Action.setResult(false);");
		jB.directStatement("this.Action.setMsg(\"未知的Action请求\");");
	}
	
	public JType generate(JCodeModel codeModel, String classname, URL url) throws JClassAlreadyExistsException{
		this.url = url;
		this.classname = classname;
		
		ObjectNode schemaNode = parser.parse(this.url);
		Schema schema = new Schema(null, schemaNode);
		
		JPackage jpackage = codeModel._package(this.packagename);
			
		return _generator(this.classname, schemaNode, jpackage, schema);
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
	
	private void createToJSON(JDefinedClass jClass){
		JMethod a2json = jClass.method(JMod.PUBLIC, String.class, "ToJson");
		//throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JsonGenerationException, JsonMappingException, IOException
		a2json._throws(IllegalArgumentException.class);
		a2json._throws(SecurityException.class);
		a2json._throws(IllegalAccessException.class);
		a2json._throws(InvocationTargetException.class);
		a2json._throws(NoSuchMethodException.class);
		a2json._throws(JsonGenerationException.class);
		a2json._throws(JsonMappingException.class);
		a2json._throws(IOException.class);
		JClass fieldclass = jClass.owner().ref(Field.class);
		JClass mapclass = jClass.owner().ref(Map.class).narrow(String.class,Object.class);
		JClass hashmapclass = jClass.owner().ref(HashMap.class).narrow(String.class,Object.class);
		JClass objectmapclass =jClass.owner().ref(ObjectMapper.class);
		a2json.body().decl(fieldclass, "field[]").init(JExpr.direct("this.getClass().getDeclaredFields()"));
		a2json.body().decl(objectmapclass, "objectmap").init(JExpr._new(objectmapclass));
		a2json.body().decl(mapclass, "map").init(JExpr._new(hashmapclass));
		JBlock jforeachBlock = a2json.body().forEach((JType)fieldclass, "f", JExpr.ref("field")).body();
		jforeachBlock._if(JExpr.direct("\"this$0\" != f.getName()"))._then().directStatement("map.put(f.getName(), this.getClass().getMethod(\"get\" + f.getName()).invoke(this));");
		a2json.body()._return(JExpr.direct("objectmap.writeValueAsString(map)"));
	}
	
	private JType _generator(String nodeName, JsonNode schemaNode, JClassContainer generatableType, Schema schema) throws JClassAlreadyExistsException{
		JType javaType = null;
		if (schemaNode.has("$ref"))
		{
			return null;
		}
		else
		{
			//System.out.println("no ref");
			deep++;
			if (schemaNode.has("enum")) {
				javaType = enumProccess(nodeName, schemaNode, generatableType, schema);
			}
			else
			{
				javaType = typeProccess(nodeName, schemaNode, generatableType, schema);
			}
			schema.setJavaTypeIfEmpty(javaType);
			deep--;
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
        //System.out.println(typename);
        
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
        	if (jClassContainer.isClass())
        	{
        		if ("Parameter" == nodeName)
        		{
        			genInitCode = true;
        		}
        		
        		type = jClassContainer._class(Modifier.PUBLIC, nodeName);
        		propertiesProccess(nodeName, node.get("properties"), (JDefinedClass)type, currentSchema);
        		if ("Response" == nodeName)
        		{
        			((JDefinedClass)type)._extends(jClassContainer.owner().ref("ResponseBase"));
        		}
        		else if ("Action" == nodeName)
        		{
        			((JDefinedClass)type)._extends(jClassContainer.owner().ref("ActionBase"));        			
        		}
        		
        		if ("Parameter" == nodeName)
        		{
        			genInitCode = false;
        		}
        	}
        	else
        	{
        		type = objectProcess(nodeName, node, jClassContainer.getPackage(), currentSchema);
        		JFieldVar schemaField = ((JDefinedClass)type).field(JMod.PRIVATE|JMod.FINAL, jClassContainer.owner().ref(String.class), "JSONSCHEMA");
        		
        		((JDefinedClass)type)._extends(jClassContainer.owner().ref("ActionHandler"));
        		schemaField.init(JExpr.lit(node.toString()));
        		
        		((JDefinedClass)type).method(JMod.PUBLIC, String.class, "getJsonSchema").body()._return(JExpr.refthis("JSONSCHEMA"));
        		
        		createConstructor((JDefinedClass)type, node);
        		createDoAction((JDefinedClass)type);
        	}
        }
        else if (typename.equals("array")){
        	type = arrayProcess(nodeName, node, jClassContainer, currentSchema);
        }
        else
        {
        	type = jClassContainer.owner().ref(Object.class);
        }
		return type;
	};
	
	private void createValidate(JDefinedClass jClass){
		JMethod validatefunc = jClass.method(JMod.PRIVATE, ValidationReport.class, "validate");
		validatefunc._throws(IOException.class);
		
		JBlock jBlock = validatefunc.body();
		validatefunc.param(String.class, "rawdata");
		validatefunc.param(String.class, "schemastr");
		//ObjectMapper objectmap = new ObjectMapper();
		JClass jClass_varomImpl = jClass.owner().ref(ObjectMapper.class);
		JVar jvaromImpl = jBlock.decl(jClass_varomImpl, "objectmap");
		jvaromImpl.init(JExpr._new(jClass_varomImpl));

		//JsonNode schema = objectmap.readTree(JSONSCHEMA);
		JClass jClass_varschemanodeImpl = jClass.owner().ref(JsonNode.class);
		JVar jvarschemanodeImpl = jBlock.decl(jClass_varschemanodeImpl, "schemanode");
		jvarschemanodeImpl.init(JExpr.direct("objectmap.readTree(schemastr)"));

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
		JMethod doFunction = null;
		
		doFunction = jClass.method(JMod.PUBLIC, jClass.owner().ref("ResponseBase"), "doAction");
		doFunction.body()._return(JExpr._new(jClass.owner().ref("EmptyResponse")));
	};
	
	private void createConstructor(JDefinedClass jClass, JsonNode node){
		System.out.println(node);
		JMethod constructor = jClass.constructor(JMod.PUBLIC);
		JBlock jBlock = constructor.body();
		
		if (node.has("properties") &&
			node.get("properties").has("Action"))
		{
			jBlock.directStatement("Action = new Action();");
			//jBlock.directStatement("Parameter = new Parameter();");
			
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
			
			if (root.has("Parameter"))
			{
				jBlock.directStatement("Parameter = new Parameter();");
			}
		}
		//((ObjectNode)root).remove("Response");
		
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
	
	private JClass arrayProcess(String nodeName, JsonNode node, JClassContainer jpackage, Schema schema) throws JClassAlreadyExistsException {

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
		String type = propertyType.name();
		JFieldVar field = jclass.field(JMod.PRIVATE, propertyType, propertyName);
		if (this.genInitCode)
		{
		}
//			
//		if (type.startsWith("List")){
//			System.out.println(propertyType.boxify().getTypeParameters().get(0).fullName());
//			//String narrowName = propertyType.boxify().getTypeParameters().get(0).fullName().substring(this.packagename.length() + 1);
//			field.init(JExpr._new(jclass.owner().ref(ArrayList.class).narrow(propertyType.boxify().getTypeParameters().get(0))));//jclass.owner().ref(narrowName))));
//		}
		
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
