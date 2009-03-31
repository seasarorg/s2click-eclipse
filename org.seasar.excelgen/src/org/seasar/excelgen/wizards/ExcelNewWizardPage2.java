package org.seasar.excelgen.wizards;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.excelgen.ExcelGenPlugIn;
import org.seasar.excelgen.util.DatabaseInfo;
import org.seasar.excelgen.util.JarClassLoader;
import org.seasar.excelgen.util.UIUtils;

public class ExcelNewWizardPage2 extends WizardPage {

	private JarClassLoader classLoader;
	private DatabaseInfo dbinfo;
	private Button view;
	private Text jarFile;
	private Combo driver;
	private TableViewer tableViewer;
	private List<String> tableModel = new ArrayList<String>();
	private Text catalog;
	private Text schema;
	private Text password;
	private Text user;
	private Text databaseURI;
	private URL[] classpathes = new URL[0];
	private ResourceBundle url = ResourceBundle.getBundle("org.seasar.excelgen.wizards.databaseURI");
	private Text filter;
	
	private ExcelNewWizardPage1 page1;
	private ScopedPreferenceStore store;
	
	public static final String JAR_FILE = "jarFile";
	public static final String DRIVER = "driver";
	public static final String URI = "uri";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String SCHEMA = "schema";
	public static final String CATALOG = "catalog";
	
	
	public ExcelNewWizardPage2(ExcelNewWizardPage1 page1){
		super("ExcelNewWizardPage2");
		setTitle("エクスポートするテーブルの選択");
		setMessage("選択したテーブルの情報をExcelにエクスポートします。");
		this.page1 = page1;
	}
	
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		UIUtils.createLabel(container, "JARファイル：");

		jarFile = new Text(container, SWT.BORDER | SWT.SINGLE);
		jarFile.setEditable(false);
		jarFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(container, SWT.PUSH);
		button.setText("参照...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		UIUtils.createLabel(container, "ドライバ：");

		driver = new Combo(container, SWT.READ_ONLY);
		driver.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		driver.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(Collections.list(url.getKeys()).contains(driver.getText())){
					String template = url.getString(driver.getText());
					databaseURI.setText(template);
				}
			}
		});
		driver.add("sun.jdbc.odbc.JdbcOdbc");
		driver.select(0);

		new Label(container, SWT.NULL);
		//-------------
		UIUtils.createLabel(container, "URI：");

		databaseURI = new Text(container, SWT.BORDER | SWT.SINGLE);
		databaseURI.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(container, SWT.NULL);
		//-------------
		UIUtils.createLabel(container, "ユーザ：");

		user = new Text(container, SWT.BORDER | SWT.SINGLE);
		user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(container, SWT.NULL);
		//-------------
		UIUtils.createLabel(container, "パスワード：");

		password = new Text(container, SWT.BORDER | SWT.PASSWORD);
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(container, SWT.NULL);
		//-------------
		UIUtils.createLabel(container, "スキーマ：");

		schema = new Text(container, SWT.BORDER | SWT.SINGLE);
		schema.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(container, SWT.NULL);
		//-------------
		UIUtils.createLabel(container, "カタログ：");

		catalog = new Text(container, SWT.BORDER | SWT.SINGLE);
		catalog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(container, SWT.NULL);
		//-------------
		UIUtils.createLabel(container, "ビューを含む");
		view = new Button(container, SWT.CHECK);
		new Label(container, SWT.NULL);
		//-------------
		new Label(container, SWT.NULL);
		Button load = new Button(container, SWT.PUSH);
		load.setText("テーブル読み込み");
		load.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					loadTables();
				} catch (Exception ex) {
					ex.printStackTrace();
					MessageBox msg = new MessageBox(getShell());
					msg.setMessage(ex.getMessage());
					msg.open();
				}
			}
		});

		new Label(container, SWT.NULL);
		//----------------
//		UIUtils.createLabel(container, "フィルタ：");
//		filter = new Text(container, SWT.BORDER);
//		filter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		filter.addModifyListener(new ModifyListener(){
//			public void modifyText(ModifyEvent e) {
//				tableViewer.refresh();
//			}
//		});
//		new Label(container, SWT.NULL);
		//----------------
		UIUtils.createLabel(container, "テーブル：");
		tableViewer = new TableViewer(container, SWT.CHECK|SWT.BORDER);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider(){
			
		});
		tableViewer.setFilters(new ViewerFilter[]{new TableFilter()});
		tableViewer.setInput(tableModel);
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(page1.getContainerName()));
		store = new ScopedPreferenceStore(
				new ProjectScope(resource.getProject()), ExcelGenPlugIn.PLUGIN_ID);
		
		jarFile.setText(store.getString(JAR_FILE));
		
		String jdbcDriver = store.getString(DRIVER);
		if(jarFile.getText().length() > 0 && jdbcDriver != null && jdbcDriver.length() != 0){
			loadJdbcDriver();
			driver.setText(jdbcDriver);
		}
		
		databaseURI.setText(store.getString(URI));
		user.setText(store.getString(USER));
		password.setText(store.getString(PASSWORD));
		schema.setText(store.getString(SCHEMA));
		catalog.setText(store.getString(CATALOG));
		
		setControl(container);
	}
	
	public void savePreferences(){
		store.setValue(JAR_FILE, jarFile.getText());
		store.setValue(DRIVER, driver.getText());
		store.setValue(URI, databaseURI.getText());
		store.setValue(USER, user.getText());
		store.setValue(PASSWORD, password.getText());
		store.setValue(SCHEMA, schema.getText());
		store.setValue(CATALOG, catalog.getText());
		try {
			store.save();
		} catch(IOException ex){
			ex.printStackTrace(); // TODO ログに出す
		}
	}
	
	private class TableFilter extends ViewerFilter {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
//			String tableName = (String) element;
//			if(tableName.indexOf(filter.getText()) >= 0){
//				return true;
//			}
//			return false;
			return true;
		}
		
	}
	
	private void loadTables() throws Exception {
		if(classLoader!=null){
			Class<?> driverClass = classLoader.loadClass(driver.getText());
			dbinfo = new DatabaseInfo(driverClass);
			dbinfo.setURI(databaseURI.getText());
			dbinfo.setUser(user.getText());
			dbinfo.setPassword(password.getText());
			dbinfo.setCatalog(catalog.getText());
			dbinfo.setSchema(schema.getText());
			dbinfo.setEnableView(view.getSelection());
			
			tableModel.clear();
			//filter.setText("");
			
			for(String tableName: dbinfo.loadTables()){
				tableModel.add(tableName);
			}
			
			tableViewer.refresh();
		}
	}
	
	public DatabaseInfo getDatabaseInfo(){
		return dbinfo;
	}
	
	public List<String> getSelectedTables(){
		List<String> tableNames = new ArrayList<String>();
		for(TableItem item: tableViewer.getTable().getItems()){
			if(item.getChecked()){
				tableNames.add(item.getData().toString());
			}
		}
		return tableNames;
	}
	
	private void loadJdbcDriver(){
		try {
			URL jarURL = new URL("file:///" + jarFile.getText());
			URL[] clspath = new URL[classpathes.length + 1];
			clspath[0] = jarURL;
			for (int i = 0; i < classpathes.length; i++) {
				clspath[i + 1] = classpathes[i];
			}
			classLoader = new JarClassLoader(clspath);
			java.util.List<Class<?>> list = classLoader.getJDBCDriverClass(jarFile.getText());
			driver.removeAll();
			for(Class<?> item: list){
				if(Arrays.binarySearch(driver.getItems(),item.getName())<0){
					driver.add(item.getName());
				}
			}
			driver.add("sun.jdbc.odbc.JdbcOdbc");
			driver.select(0);
		} catch (Exception e1) {
			//DBPlugin.logException(e1);
		}			
	}
	
	/**
	 * Choose a jar file which contains the JDBC driver.
	 */
	private void handleBrowse(){
		FileDialog dialog = new FileDialog(getShell());
		if(dialog.open()==null){
			return;
		}
		jarFile.setText(dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName());
		loadJdbcDriver();
	}

}