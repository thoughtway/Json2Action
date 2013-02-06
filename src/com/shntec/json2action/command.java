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

import com.sun.codemodel.JCodeModel;

public class command {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub
		String 	inputdir = "/home/netbsd/workspace/Json2Action/example/input", 
				outputdir = "/home/netbsd/workspace/Json2Action/example/output";
		
		File[] list = new File(inputdir).listFiles();

		for (int i = 0; i < list.length; i++)
		{
			if (list[i].isFile() && ! list[i].isHidden())
			{
				String classname = StringUtils.substringBeforeLast(list[i].getName(), ".");
				URL url = list[i].toURI().toURL();
				Generator gen = new Generator("com.shntec", classname, url);
				gen.generate();
			}
		}
	}
}
