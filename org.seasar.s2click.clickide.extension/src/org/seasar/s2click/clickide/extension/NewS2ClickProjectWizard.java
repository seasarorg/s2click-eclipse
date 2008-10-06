package org.seasar.s2click.clickide.extension;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * S2Clickプロジェクトを生成するためのウィザードです。
 * 
 * @author Naoki Takezoe
 * @see NewS2ClickProjectWizardPage
 */
public class NewS2ClickProjectWizard extends Wizard implements INewWizard {

	private NewS2ClickProjectWizardPage page;
	
	public NewS2ClickProjectWizard() {
		setWindowTitle("New S2Click Project");
	}

	@Override public void addPages() {
		page = new NewS2ClickProjectWizardPage();
		addPage(page);
	}

	@Override public boolean performFinish() {
		final String projectName = page.getProjectName();
		final boolean useS2Jdbc = page.getUseS2Jdbc();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					S2ClickPlugin.getDefault().createProject(projectName, useS2Jdbc, monitor);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			realException.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}
