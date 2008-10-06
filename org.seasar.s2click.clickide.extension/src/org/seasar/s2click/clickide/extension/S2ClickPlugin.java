package org.seasar.s2click.clickide.extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import net.sf.clickide.ClickUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class S2ClickPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.seasar.s2click.clickide.extension";

	// The shared instance
	private static S2ClickPlugin plugin;
	
	/**
	 * The constructor
	 */
	public S2ClickPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		removeResources();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static S2ClickPlugin getDefault() {
		return plugin;
	}
	
	/** �u�����N�v���W�F�N�g�̃e���|�����t�@�C�� */
	private File blankZip = null;
	
	/** S2JDBC���g���u�����N�v���W�F�N�g�̃e���|�����t�@�C�� */
	private File blankS2JdbcZip = null;
	
	/**
	 * �v���W�F�N�g�̐����ɕK�v�ȃ��\�[�X���e���|�����t�H���_�ɃR�s�[���܂��B
	 */
	private void copyResources() throws IOException {
		if(blankZip == null){
			{
				blankZip = File.createTempFile("s2click-blank", ".zip");
				URL url = S2ClickPlugin.getDefault().getBundle().getEntry("resources/s2click-blank.zip");
				ClickUtils.copyStream(url.openStream(), new FileOutputStream(blankZip));
			}
			{
				blankS2JdbcZip = File.createTempFile("s2click-s2jdbc-blank", ".zip");
				URL url = S2ClickPlugin.getDefault().getBundle().getEntry("resources/s2click-s2jdbc-blank.zip");
				ClickUtils.copyStream(url.openStream(), new FileOutputStream(blankS2JdbcZip));
			}
		}
	}
	
	/**
	 * �e���|�����t�H���_�ɃR�s�[�������\�[�X���폜���܂��B
	 */
	private void removeResources(){
		if(blankZip != null){
			blankZip.delete();
			blankS2JdbcZip.delete();
		}
	}
	
	/**
	 * S2Click�v���W�F�N�g���쐬���܂��B
	 * 
	 * @param projectName �v���W�F�N�g��
	 * @param useS2Jdbc S2JDBC���g�����ǂ���
	 */
	public void createProject(String projectName, boolean useS2Jdbc,
			IProgressMonitor monitor) throws IOException, CoreException {
		if(monitor == null){
			monitor = new NullProgressMonitor();
		}
		
		monitor.beginTask("S2Click�v���W�F�N�g���쐬��...", 3);
		
		copyResources();
		monitor.worked(1);
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		project.create(monitor);
		
		// TODO ���\�[�X���v���W�F�N�g�ɃR�s�[
		
		project.open(monitor);
		
		monitor.worked(1);
		
		monitor.done();
	}

}
