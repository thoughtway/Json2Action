package com.shntec.json2action;

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
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

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
			Schema node = new Schema(null, schemaNode);
			
			JPackage jpackage = codeModel._package(this.packagename);
			System.out.println(node.getContent());
		}
	}
}
