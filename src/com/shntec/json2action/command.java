package com.shntec.json2action;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.lang.StringUtils;
import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;
import org.hisrc.jscm.codemodel.JSCodeModel;
import org.hisrc.jscm.codemodel.JSFunctionDeclaration;
import org.hisrc.jscm.codemodel.JSProgram;
import org.hisrc.jscm.codemodel.expression.JSAssignmentExpression;
import org.hisrc.jscm.codemodel.expression.JSCallExpression;
import org.hisrc.jscm.codemodel.expression.JSExpression;
import org.hisrc.jscm.codemodel.expression.JSExpressionVisitor;
import org.hisrc.jscm.codemodel.expression.JSFunctionExpression.Function;
import org.hisrc.jscm.codemodel.expression.JSGlobalVariable;
import org.hisrc.jscm.codemodel.expression.JSVariable;
import org.hisrc.jscm.codemodel.expression.JSExpression.Comma;
import org.hisrc.jscm.codemodel.expression.JSPrimaryExpression.Brackets;
import org.hisrc.jscm.codemodel.expression.impl.AssignmentExpressionImpl;
import org.hisrc.jscm.codemodel.expression.impl.AssignmentExpressionImpl.AssignmentImpl;
import org.hisrc.jscm.codemodel.expression.impl.ExpressionImpl;
import org.hisrc.jscm.codemodel.expression.impl.FunctionExpressionImpl;
import org.hisrc.jscm.codemodel.impl.CodeModelImpl;
import org.hisrc.jscm.codemodel.writer.CodeWriter;
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
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws IOException, JClassAlreadyExistsException, SecurityException, NoSuchFieldException {
		// TODO Auto-generated method stub
		String ParentPath = new File(command.class.getResource("/").getPath()).getParent();
		String 	inputdir = ParentPath + "/example/input", 
				outputdir = ParentPath + "/example/output/java",
				jsoutputdir = ParentPath + "/example/output/js",
				jsprefix = "";
		String packagename = "com.shntec.json2action.demo";
		
//		if (true)
//		{
			JSCodeModel c = new CodeModelImpl();
//			JSGlobalVariable a = c.globalVariable("g");
			//a.assign(c.integer(2));
			JSProgram actionsprog = c.program();
			JSProgram clouseprog = c.program();
//			prog.expression(a);
		//	Function jsfunc = c.function();
			//prog.expression(jsfunc);
			//func.getBody().getSourceElements().add(a.assign(c.integer(2))) ;//.expression(a.assign(c.function()));
			
//			CodeWriter f = new CodeWriter(System.out);
			//f.expression(prog.functionDeclaration("").getFunctionExpression().brackets());
//			f.program(prog);
//			return;
//		}
		
		OptionParser parser = new OptionParser( "i:o:p:j:" ){
			{
				accepts("i").withRequiredArg().required()
                .describedAs( "json定义文件的保存路径" );
				accepts("o").withRequiredArg().required()
                .describedAs( "输出的路径" );
				accepts("p").withRequiredArg().required()
                .describedAs( "包的全名" );
				accepts("j").withRequiredArg().required()
                .describedAs( "js对象的前缀" );
			}
		};
		OptionSet options = null;
		try{
			options = parser.parse(args);
		}
		catch (Exception e)
		{
			parser.printHelpOn( System.out );
			return;
		}
		
		
		if (options.has( "i" ) && options.hasArgument("i") &&
			options.has( "o" ) && options.hasArgument("o") &&
			options.has("p") && options.hasArgument("p"))
		{
			inputdir = (String) options.valueOf("i");
			outputdir = (String) options.valueOf("o");
			packagename = (String) options.valueOf("p");
			jsprefix = (String) options.valueOf("j");
			File[] list = new File(inputdir).listFiles();
			
			jsoutputdir = (outputdir.endsWith(File.pathSeparator)? (outputdir + "js"):(outputdir + "/js"));
			outputdir = (outputdir.endsWith(File.pathSeparator)? (outputdir + "java"):(outputdir + "/java"));
			
			File javadir = new File(outputdir);
			File jsdir = new File(jsoutputdir);
			if (!javadir.exists())
			{
				if (!javadir.mkdirs())
				{
					System.out.println("create path: " + outputdir + " error!");
					return;
				}
			}
			
			if (!jsdir.exists())
			{
				if (!jsdir.mkdirs())
				{
					System.out.println("create path: " + jsoutputdir + " error!");
					return;
				}
			}
			
			JCodeModel codeModel = new JCodeModel();
			JSCodeModel jscodeModel = new CodeModelImpl();
			Generator gen = new Generator(packagename);
			
			gen.setJsCodeModel(c);
			gen.setJsActionProg(actionsprog);
			gen.setJsClouseProg(clouseprog);
			
			Map<String, String> c2fmap = new HashMap<String, String>();
			
			gen.genBaseClass(codeModel);		
			for (int i = 0; i < list.length; i++)
			{
				if (list[i].isFile() && ! list[i].isHidden())
				{
					String classname = StringUtils.substringBeforeLast(list[i].getName(), ".");
					URL url = list[i].toURI().toURL();
					gen.generate(codeModel, classname, url);
					gen.jsgenerate(url, jsprefix + ".actions." + classname.toLowerCase());
					ObjectMapper objmapper = new ObjectMapper();
					JsonNode node = objmapper.readTree(new File(URI.create(url.toString())));

					c2fmap.put(node.get("Action").get("Name").asText(), classname);
				}
			}
			gen.genFactoryClass(codeModel, c2fmap);
			codeModel.build(new File(outputdir));
			
			String actionjsfile = jsoutputdir + "/actions.js";
			String clousejsfile = jsoutputdir + "/" + jsprefix + ".js";
			FileWriter fw1 = new FileWriter(actionjsfile),
					fw2 = new FileWriter(clousejsfile);
			
			CodeWriter f1 = new CodeWriter(fw1);
			f1.openRoundBracket();
			f1.program(actionsprog);
			f1.closeRoundBracket();
			f1.openRoundBracket();
			f1.closeRoundBracket();
			f1.semicolon();
			fw1.flush();
			fw1.close();
			
			CodeWriter f2 = new CodeWriter(fw2);
			f2.openRoundBracket();
			f2.program(clouseprog);
			f2.closeRoundBracket();
			f2.openRoundBracket();
			f2.closeRoundBracket();
			f2.semicolon();			
			fw2.flush();
			fw2.close();
		}
		else
		{
			parser.printHelpOn( System.out );
		}
		
	};
}
