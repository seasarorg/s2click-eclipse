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
 * 新規S2Clickプロジェクトを作成するためのウィザードページです。
 * 
 * @author Naoki Takezoe
 */
public class NewS2ClickProjectWizardPage extends WizardPage {

	private Text projectName;
	private Button useS2Jdbc;
	
	public NewS2ClickProjectWizardPage(){
		super("NewS2ClickProjectWizardPage");
		setTitle("新規S2Clickプロジェクト");
		setDescription("新規S2Clickプロジェクトを作成します。");
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		
		new Label(composite, SWT.NULL).setText("プロジェクト名:");
		projectName = new Text(composite, SWT.BORDER);
		projectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectName.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				doValidate();
			}
		});
		
		new Label(composite, SWT.NULL);
		useS2Jdbc = new Button(composite, SWT.CHECK);
		useS2Jdbc.setText("S2JDBCを使う");
		
		setControl(composite);
	}
	
	/**
	 * 入力チェックを行います。
	 */
	private void doValidate(){
		if(projectName.getText().length()==0){
			setErrorMessage("プロジェクト名を入力してください。");
			setPageComplete(false);
			return;
		}
		
		String name = projectName.getText();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		if(project!=null && project.exists()){
			setErrorMessage("プロジェクトはすでに存在します。");
			setPageComplete(false);
			return;
		}
		
		setErrorMessage(null);
		setPageComplete(true);
		return;
	}
	
	/**
	 * プロジェクト名を取得します。
	 * @return プロジェクト名
	 */
	public String getProjectName(){
		return projectName.getText();
	}
	
	/**
	 * S2JDBCを使うかどうかを取得します。
	 * @return S2JDBCを使う場合true、使わない場合false
	 */
	public boolean getUseS2Jdbc(){
		return useS2Jdbc.getSelection();
	}

}
