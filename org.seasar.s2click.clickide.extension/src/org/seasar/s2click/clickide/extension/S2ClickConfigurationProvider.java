package org.seasar.s2click.clickide.extension;

import org.apache.click.eclipse.ClickPlugin;
import org.apache.click.eclipse.ClickUtils;
import org.apache.click.eclipse.core.config.DefaultClickConfigurationProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * S2Click用の<code>IClickConfigurationProvider</code>実装クラスです。
 * <p>
 * <tt>s2click.dicon</tt>から設定情報を読み込みます。
 * 
 * @author Naoki Takezoe
 */
public class S2ClickConfigurationProvider extends DefaultClickConfigurationProvider {

	private static final String S2CLICK_DICON = "s2click.dicon";
	private static final String CONVENTION_DICON = "convention.dicon";
	
	/**
	 * S2Clickでは常に自動マッピングが有効なため、このメソッドは常に<code>true</code>を返します。
	 * 
	 * @return true
	 */
	public boolean getAutoMapping(IProject project) {
		return true;
	}

	/**
	 * Returns the charset which is specified in <tt>s2click.dicon</tt>.
	 * If <tt>s2click.dicon</tt> doesn't has the charset, returns <code>null</code>.
	 * 
	 * @return the charset
	 */
	public String getCharset(IProject project) {
		String property = getProperty(project, "charset");
		if(property != null){
			return property.replaceAll("(^\"|\"$)", "");
		}
		return null;
	}

	/**
	 * Returns <code>IType</code> of the format object which is specified in <tt>s2click.dicon</tt>.
	 * If format element is not defined, this method returns <code>net.sf.click.util.Format</code>.
	 * 
	 * @param project the project
	 * @return IType of the format object
	 */
	public IType getFormat(IProject project) {
		String property = getProperty(project, "formatClass");
		String className = null;
		
		if(property != null){
			className = property.replaceAll("(^@|@class$)", "");
		}
		
		if(className==null){
			className = ClickUtils.DEFAULT_FORMAT_CLASS;
		}
		try {
			IJavaProject javaProject = JavaCore.create(project);
			return javaProject.findType(className);
		} catch(Exception ex){
			return null;
		}
	}

	/**
	 * Returns the package name of page classes which is specified in <tt>convention.dicon</tt>.
	 * If the package name is not specified, thie method returns <code>null</code>.
	 * 
	 * @param project the project
	 * @return the package name of page classes or <code>null</code>
	 */
	public String getPagePackageName(IProject project) {
		IStructuredModel model = getDiconModel(project, CONVENTION_DICON);
		try {
			if(model == null){
				return null;
			}
			NodeList list = (((IDOMModel)model).getDocument()).getElementsByTagName("initMethod");
			for(int i=0;i<list.getLength();i++){
				Element element = (Element) list.item(i);
				if("addRootPackageName".equals(element.getAttribute("name"))){
					NodeList args = element.getElementsByTagName("arg");
					for(int j=0;j<args.getLength();j++){
						Element arg = (Element) args.item(j);
						String packageName = getText(arg);
						return packageName.replaceAll("(^\"|\"$)", "") + ".page";
					}
				}
			}
		} catch(Exception ex){
		} finally {
			if(model!=null){
				model.releaseFromRead();
			}
		}
		return null;
	}

	private static String getProperty(IProject project, String name){
		IStructuredModel model = getDiconModel(project, S2CLICK_DICON);
		try {
			if(model == null){
				return null;
			}
			NodeList list = (((IDOMModel)model).getDocument()).getElementsByTagName("property");
			for(int i=0;i<list.getLength();i++){
				Element element = (Element) list.item(i);
				if(name.equals(element.getAttribute("name"))){
					return getText(element);
				}
			}
		} catch(Exception ex){
		} finally {
			if(model!=null){
				model.releaseFromRead();
			}
		}
		return null;
	}
	
	private static IFile getDiconFile(IProject project, String fileName){
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
			for(int i=0;i<roots.length;i++){
				IResource resource = roots[i].getResource();
				if(resource != null && resource instanceof IContainer){
					IContainer container = (IContainer) resource;
					IResource file = container.findMember(fileName);
					if(file != null && file.exists() && file instanceof IFile){
						return (IFile) file;
					}
				}
			}
		} catch(Exception ex){
		}
		
		return null;
	}
	
	private static IStructuredModel getDiconModel(IProject project, String fileName){
		IStructuredModel model = null;
		try {
			IFile file = getDiconFile(project, fileName);
			if(file==null){
				return null;
			}
			model = StructuredModelManager.getModelManager().getModelForRead(file);
		} catch(Exception ex){
			ClickPlugin.log(ex);
		}
		return model;
	}
	
	private static String getText(Element element){
		StringBuffer sb = new StringBuffer();
		NodeList list = element.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Node node = list.item(i);
			if(node instanceof Text){
				sb.append(node.getNodeValue());
			}
		}
		return sb.toString().trim();
	}

	/**
	 * If the given project has <tt>s2click.dicon</tt>, 
	 * this method returns <code>true</code>.
	 */
	public boolean isSupportedProject(IProject project) {
		return getDiconFile(project, S2CLICK_DICON) != null;
	}

}
