package org.seasar.s2click.clickide.extension;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * �V�KS2Click�v���W�F�N�g���쐬���邽�߂̃E�B�U�[�h�y�[�W�ł��B
 * 
 * @author Naoki Takezoe
 */
public class NewS2ClickProjectWizardPage extends WizardPage {

	private Text projectName;
	private Button useS2Jdbc;
	
	public NewS2ClickProjectWizardPage(){
		super("NewS2ClickProjectWizardPage");
		setTitle("�V�KS2Click�v���W�F�N�g");
		setDescription("�V�KS2Click�v���W�F�N�g���쐬���܂��B");
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		
		new Label(composite, SWT.NULL).setText("�v���W�F�N�g��:");
		projectName = new Text(composite, SWT.BORDER);
		projectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectName.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				doValidate();
			}
		});
		
		new Label(composite, SWT.NULL);
		useS2Jdbc = new Button(composite, SWT.CHECK);
		useS2Jdbc.setText("S2JDBC���g��");
		
		setControl(composite);
	}
	
	/**
	 * ���̓`�F�b�N���s���܂��B
	 */
	private void doValidate(){
		if(projectName.getText().length()==0){
			setErrorMessage("�v���W�F�N�g������͂��Ă��������B");
			setPageComplete(false);
			return;
		}
		
		String name = projectName.getText();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		if(project!=null && project.exists()){
			setErrorMessage("�v���W�F�N�g�͂��łɑ��݂��܂��B");
			setPageComplete(false);
			return;
		}
		
		setErrorMessage(null);
		setPageComplete(true);
		return;
	}
	
	/**
	 * �v���W�F�N�g�����擾���܂��B
	 * @return �v���W�F�N�g��
	 */
	public String getProjectName(){
		return projectName.getText();
	}
	
	/**
	 * S2JDBC���g�����ǂ������擾���܂��B
	 * @return S2JDBC���g���ꍇtrue�A�g��Ȃ��ꍇfalse
	 */
	public boolean getUseS2Jdbc(){
		return useS2Jdbc.getSelection();
	}

}
