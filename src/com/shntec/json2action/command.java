package com.shntec.json2action;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.net.*;

import org.apache.commons.lang.StringUtils;
import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;
//import org.eel.kitchen.jsonschema.main.JsonSchema;
//import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

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
				if (null != codeModel){
					//codeModel.directClass("com.fasterxml.jackson.databind.JsonNode");
					//codeModel.build(new File(outputdir));
					//codeModel.newAnonymousClass(baseType)
				}
			}
		}
		codeModel.build(new File(outputdir));
		//JsonSchemaFactory factory = JsonSchemaFactory.defaultFactory();
		//JsonSchema schema = factory.

	};
	
	public static void initBaseClass(JCodeModel codeModel, String packagename) throws JClassAlreadyExistsException
	{
		JPackage jp = codeModel._package(packagename);
		
		JDefinedClass respBase = jp._interface("ResponseBase");
		JMethod toJson = respBase.method(JMod.PUBLIC, String.class, "ToJson");
		
		JDefinedClass errorResp = jp._class("ErrorRepsonse");
		errorResp._implements(respBase);
		errorResp.field(JMod.PUBLIC, String.class, "More");
		errorResp.field(JMod.PUBLIC, codeModel.INT, "Code");
		JMethod toJsonImpl = errorResp.method(JMod.PUBLIC, String.class, "ToJson");
		String errorRespRet = "\"{\\\"More\\\":\\\"\" + this.More + \"\\\", \\\"Code\\\":\" + Integer.toString(this.Code) + \"}\"";
		toJsonImpl.body()._return(JExpr.direct(errorRespRet));
		JMethod errorRespConstructor = errorResp.constructor(JMod.PUBLIC);
		errorRespConstructor.param(String.class, "More");
		errorRespConstructor.param(codeModel.INT, "Code");
		errorRespConstructor.body().directStatement("this.More = More");
		errorRespConstructor.body().directStatement("this.Code = Code");		
	}
}
