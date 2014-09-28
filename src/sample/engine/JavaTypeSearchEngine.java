package sample.engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import sample.dialogs.JavaTypeNameMatch;
import sample.exception.OpenJavaTypeByAuthorException;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class JavaTypeSearchEngine {

	private JavaTypeItemsFilter filter;
	private JavaTypeSearchRequestor requestor;
	private List<String> clsNames = new ArrayList<String>();
	private List<Element> strutsName = new LinkedList<Element>();

	public JavaTypeSearchEngine(JavaTypeSearchRequestor requestor) {
		this.requestor = requestor;
		filter = this.requestor.getPluginSearchFilter();
		initxml();
	}
	private void initxml(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (IProject project : projects) {
			try {
				project.refreshLocal(0, null);
			} catch (CoreException e1) {
//				throw new OpenJavaTypeByAuthorException(e1);
			}
			if (!project.isOpen()) {
				continue;
			}
//			System.out.println("project");
			long start = System.currentTimeMillis();
			
			try{
				searchForXmlFile(project);	
//				System.out.println("[strutsName]"+strutsName.size());
			}catch(Exception e){
				
			}
			
			long end = System.currentTimeMillis();
			System.out.println("[total cost]"+(end-start));
			
		}
	}
	
	private void searchForXmlFile(IContainer parent)
			throws OpenJavaTypeByAuthorException {
		IResource[] members;
		
		try {
			members = parent.members();
		} catch (CoreException e) {
			throw new OpenJavaTypeByAuthorException(e);
		}	
		for (IResource resource : members) {
			if (resource instanceof IContainer) {
				IContainer container = (IContainer) resource;
				searchForXmlFile(container);
			} else if (resource instanceof IFile) {
				//这个地方改成先从xml文件中找到相应的字符串，然后找到相应的java文件
				IFile file = (IFile) resource;
				if("xml".equals(file.getFileExtension())){
//					System.out.println("==========");
					strutsName.addAll(getXmlJavaClassName(file));

				}
			}
		}
	}

public void search() throws OpenJavaTypeByAuthorException {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IProject[] projects = workspace.getRoot().getProjects();
	//清空clsNames
	clsNames.clear();
	for (IProject project : projects) {
		try {
			project.refreshLocal(0, null);
		} catch (CoreException e1) {
			throw new OpenJavaTypeByAuthorException(e1);
		}
		if (!project.isOpen()) {
			continue;
		}
//		System.out.println("project");
		long start = System.currentTimeMillis();
		
		
		searchForJavaFile(project);
		long end = System.currentTimeMillis();
		System.out.println("[total cost]"+(end-start));
		
	}
}

	private void searchForJavaFile(IContainer parent)
			throws OpenJavaTypeByAuthorException {
		IResource[] members;
		
		try {
			members = parent.members();
		} catch (CoreException e) {
			throw new OpenJavaTypeByAuthorException(e);
		}

		long start = System.currentTimeMillis();
		clsNames.addAll(filter.findJavaClass(strutsName));
		
		long end = System.currentTimeMillis();
		if (end - start > 50)
			System.out.println("loop1cost:" + (end-start));
		
		start = System.currentTimeMillis();
		for (IResource resource : members) {
			if (resource instanceof IContainer) {
				IContainer container = (IContainer) resource;
				searchForJavaFile(container);
			} else if (resource instanceof IFile) {

				IFile file = (IFile) resource;
				if("java".equals(file.getFileExtension())){
//					System.out.println("[javaName]"+file.getName());
					for(int i = 0; i < clsNames.size(); i++){
//						System.out.println("XX"+clsNames.get(i).toLowerCase());
						if(file.getName().toLowerCase().contains(clsNames.get(i).toLowerCase())){
							requestor.add(new JavaTypeNameMatch(file,clsNames.get(i)));
						}
					}

				}
			}
		}
		end = System.currentTimeMillis();
		if (end - start > 50)
			System.out.println("loop2cost" + (end-start));
	}
	
	//从xml文件中读搜索的词，并返回
	public List<Element> getXmlJavaClassName(IFile file) throws OpenJavaTypeByAuthorException{
		List<Element> result = new ArrayList<Element>();
		try{
			SAXBuilder saxBuilder = new SAXBuilder(false);
//			System.out.println(file.getFileExtension());
			InputStream inputStream = new FileInputStream(file.getRawLocation().makeAbsolute().toFile());
			Document document = saxBuilder.build(inputStream);
			Element rootElement = document.getRootElement();
			if(rootElement.getName() == "struts"){				
				List<Element> packageList = rootElement.getChildren("package");
				if(null!=packageList){
					for(int i = 0; i < packageList.size(); i++){
						List<Element> eleList = packageList.get(i).getChildren("action");
						result.addAll(eleList);
					}
				}else{
					List<Element> eleList = rootElement.getChildren("action");
					result.addAll(eleList);
				}
			}
			inputStream.close();
			
			return result;
			
		}catch (IOException e) {
			throw new OpenJavaTypeByAuthorException(e);
		}catch(Exception e){
//			System.out.println("+++++++++++");
			e.printStackTrace();
			throw new OpenJavaTypeByAuthorException(e);
		}

	}

	public String getJavaTypeAuthorName(IFile file)
			throws OpenJavaTypeByAuthorException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					file.getContents()));
			String mark = "@author";
			boolean inAnnotation = false;
			String line = reader.readLine();
			while (line != null) {
				if (!inAnnotation) {
					if (line.trim().contains("/**")) {
						inAnnotation = true;
					}
				} else {
					if (line.trim().contains("*/")) {
						inAnnotation = false;
					}
					if (line.trim().contains(mark)) {
						String name = line.substring(line.indexOf(mark)
								+ mark.length());
						name = name.trim();
						return name;
					}
				}
				if (line.trim().startsWith("public class")) {
					return null;
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new OpenJavaTypeByAuthorException(e);
		} catch (CoreException e) {
			throw new OpenJavaTypeByAuthorException(e);
		}
		return null;
	}
}
