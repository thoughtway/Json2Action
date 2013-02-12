package com.shntec.json2action;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

import org.apache.commons.lang.StringUtils;
import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;
//import org.eel.kitchen.jsonschema.main.JsonSchema;
//import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
//import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.codemodel.JVar;

public class command {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JClassAlreadyExistsException 
	 */
	public static void main(String[] args) throws IOException, JClassAlreadyExistsException {
		// TODO Auto-generated method stub
		//JsonSchemaFactory f = JsonSchemaFactory.defaultFactory();
		String ParentPath = new File(command.class.getResource("/").getPath()).getParent();
//		String 	inputdir = "/Users/xiehuajun/Documents/workspace/Json2Action/example/input", 
//				outputdir = "/home/netbsd/workspace/Json2Action/example/output";
		String 	inputdir = ParentPath + "/example/input", 
				outputdir = ParentPath + "/example/output/java";
		File[] list = new File(inputdir).listFiles();
		String packagename = "com.shntec.json2action.demo";
		JCodeModel codeModel = new JCodeModel();
		
		initBaseClass(codeModel, packagename);
		
		for (int i = 0; i < list.length; i++)
		{
			if (list[i].isFile() && ! list[i].isHidden())
			{
				String classname = StringUtils.substringBeforeLast(list[i].getName(), ".");
				URL url = list[i].toURI().toURL();
				Generator gen = new Generator(packagename, classname, url);
				//JCodeModel codeModel = gen.generate();
				gen.generate(codeModel);
//				if (null != codeModel){
					//codeModel.directClass("com.fasterxml.jackson.databind.JsonNode");
					//codeModel.build(new File(outputdir));
					//codeModel.newAnonymousClass(baseType)
//				}
			}
		}
		codeModel.build(new File(outputdir));
		//JsonSchemaFactory factory = JsonSchemaFactory.defaultFactory();
		//JsonSchema schema = factory.

	};
	
	public static void initBaseClass(JCodeModel codeModel, String packagename) throws JClassAlreadyExistsException
	{
		JPackage jp = codeModel._package(packagename);
		
		JDefinedClass respBase = jp._class("ResponseBase");
		JMethod toJson = respBase.method(JMod.PUBLIC|JMod.ABSTRACT, String.class, "ToJson");
		createToJSON(respBase);
		
		JDefinedClass errorResp = jp._class("ErrorRepsonse");
		errorResp._extends(respBase);
		errorResp.field(JMod.PUBLIC, String.class, "More");
		errorResp.field(JMod.PUBLIC, codeModel.INT, "Code");		
		JMethod errorRespConstructor = errorResp.constructor(JMod.PUBLIC);
		errorRespConstructor.param(String.class, "More");
		errorRespConstructor.param(codeModel.INT, "Code");
		errorRespConstructor.body().directStatement("this.More = More");
		errorRespConstructor.body().directStatement("this.Code = Code");
		
		JDefinedClass actionBase = jp._class(JMod.ABSTRACT | JMod.PUBLIC, "ActionBase");
		createToJSON(actionBase);
		//a2json.body()
	};
	
	private static void createToJSON(JDefinedClass jClass){
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
		jforeachBlock.directStatement("map.put(f.getName(), this.getClass().getMethod(\"get\" + f.getName()).invoke(this));");
		a2json.body()._return(JExpr.direct("objectmap.writeValueAsString(map)"));
	}
}
