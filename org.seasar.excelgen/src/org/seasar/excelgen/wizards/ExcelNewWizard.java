package org.seasar.excelgen.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.seasar.excelgen.util.DatabaseInfo;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */
public class ExcelNewWizard extends Wizard implements INewWizard {
	
	private ExcelNewWizardPage1 page1;
	private ExcelNewWizardPage2 page2;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public ExcelNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page1 = new ExcelNewWizardPage1(selection);
		addPage(page1);
		
		page2 = new ExcelNewWizardPage2(page1);
		addPage(page2);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page1.getContainerName();
		final String fileName = page1.getFileName();
		final DatabaseInfo dbInfo = page2.getDatabaseInfo();
		final List<String> tableNames = page2.getSelectedTables();
		
		page2.savePreferences();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, dbInfo, tableNames, monitor);
				} catch (CoreException e) {
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
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(String containerName, String fileName, DatabaseInfo dbInfo, 
			List<String> tableNames, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 1);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(dbInfo, tableNames);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
	}
	
	private InputStream openContentStream(
			DatabaseInfo dbInfo, List<String> tableNames) throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Connection conn = null;
		try {
			conn = dbInfo.connect();
			DatabaseMetaData meta = conn.getMetaData();
			List<String> columnNames = new ArrayList<String>();
			
			for(int i = 0; i < tableNames.size(); i++){
				String tableName = tableNames.get(i);
				
				HSSFSheet sheet1 = wb.createSheet();
				wb.setSheetName(i, tableName);

				// ヘッダの作成
				HSSFRow headerRow = sheet1.createRow(0);
			
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + tableName + "\"");
//				ResultSetMetaData rm = rs.getMetaData();
				
				ResultSet columns = meta.getColumns(
						dbInfo.getCatalog(), dbInfo.getSchema(), tableName, "%");
				while(columns.next()){
					headerRow.createCell((short) columnNames.size()).setCellValue(
							new HSSFRichTextString(columns.getString("COLUMN_NAME")));
					columnNames.add(columns.getString("COLUMN_NAME"));
				}
				
				// データの作成
				while(rs.next()){
					int rowCount = 1;
					HSSFRow row = sheet1.createRow(rowCount);
					
					for(int j = 0; j < columnNames.size(); j++){
						row.createCell((short) j).setCellValue(
								new HSSFRichTextString(rs.getString(columnNames.get(j))));
					}
					
					rowCount++;
				}
				
				columns.close();
				stmt.close();
			}
			
		} catch(Exception ex){
			ex.printStackTrace(); // TODO ログに出力する
		} finally {
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		wb.write(out);
		
		return new ByteArrayInputStream(out.toByteArray());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.seasar.excelgen", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}