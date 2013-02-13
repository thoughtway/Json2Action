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
import com.fasterxml.jackson.databind.JsonNode;
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
		String ParentPath = new File(command.class.getResource("/").getPath()).getParent();
		String 	inputdir = ParentPath + "/example/input", 
				outputdir = ParentPath + "/example/output/java";
		File[] list = new File(inputdir).listFiles();
		String packagename = "com.shntec.json2action.demo";
		JCodeModel codeModel = new JCodeModel();
		Generator gen = new Generator(packagename);
		Map<String, String> c2fmap = new HashMap<String, String>();
		
		gen.genBaseClass(codeModel);		
		for (int i = 0; i < list.length; i++)
		{
			if (list[i].isFile() && ! list[i].isHidden())
			{
				String classname = StringUtils.substringBeforeLast(list[i].getName(), ".");
				URL url = list[i].toURI().toURL();
				JDefinedClass jClass = (JDefinedClass)gen.generate(codeModel, classname, url);
				ObjectMapper objmapper = new ObjectMapper();
				JsonNode node = objmapper.readTree(new File(URI.create(url.toString())));

				c2fmap.put(node.get("Action").get("Name").asText(), classname);
			}
		}
		gen.genFactoryClass(codeModel, c2fmap);
		codeModel.build(new File(outputdir));

	};
}
