package sample.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

import sample.constants.JavaTypeConstants;
import sample.engine.JavaTypeItemsFilter;
import sample.engine.JavaTypeSearchEngine;
import sample.engine.JavaTypeSearchRequestor;
import sample.exception.OpenJavaTypeByAuthorException;
import sample.exception.OpenJavaTypeByAuthorLog;
import sample.history.JavaTypeSelectionHistory;
import sample.util.JavaTypeInfoUtil;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class OpenJavaTypeByAuthorDialog extends SelectionStatusDialog {

	private static final String EMPTY_STRING = "";

	private final IStatus OK_Status = new Status(IStatus.OK,
			PlatformUI.PLUGIN_ID, IStatus.OK, EMPTY_STRING, null);

	private final IStatus ERROR_Status = new Status(IStatus.ERROR,
			PlatformUI.PLUGIN_ID, IStatus.ERROR, EMPTY_STRING, null);

	private Text text;

	private TableViewer tableViewer;

	private boolean multi;

	private Label progressLabel;

	private Object[] lastSelection;

	private JavaTypeContentProvider contentProvider;

	private JavaTypeItemLabelProvider itemsListLabelProvider;

	private JavaTypeInfoUtil javaTypeUtil;

	private JavaTypeItemsFilter filter;

	private JavaTypeItemsFilter lastCompletedFilter;

	private List lastCompletedResult;

	private FilterJob filterJob;

	public OpenJavaTypeByAuthorDialog(Shell parent) {
		super(parent);
		JavaTypeSelectionHistory history = JavaTypeSelectionHistory
				.getInstance();
		contentProvider = new JavaTypeContentProvider(history, this);
		javaTypeUtil = new JavaTypeInfoUtil();
		itemsListLabelProvider = new JavaTypeItemLabelProvider(this);
		filterJob = new FilterJob();
	}

	@Override
	protected void computeResult() {
		List selectedElements = ((StructuredSelection) tableViewer
				.getSelection()).toList();

		List<IFile> objectsToReturn = new ArrayList<IFile>();

		Object item = null;

		for (Iterator it = selectedElements.iterator(); it.hasNext();) {
			item = it.next();

			if (item instanceof JavaTypeNameMatch) {
				JavaTypeNameMatch type = (JavaTypeNameMatch) item;
				contentProvider.addHistoryElement(item);
				objectsToReturn.add(type.getFile());
			}
		}
		setResult(objectsToReturn);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(dialogArea, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumWidth = 500;
		gd.minimumHeight = 400;
		content.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);

		createHeader(content);

		text = new Text(content, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);

		createLabels(content);

		tableViewer = new TableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE)
				| SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(itemsListLabelProvider);
		tableViewer.setInput(new Object[0]);
		tableViewer.setItemCount(contentProvider.getElements(null).length);
		gd = new GridData(GridData.FILL_BOTH);
		tableViewer.getTable().setLayoutData(gd);

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (tableViewer.getTable().getItemCount() > 0) {
						tableViewer.getTable().setFocus();
					}
				}
			}
		});

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						StructuredSelection selection = (StructuredSelection) event
								.getSelection();
						handleSelected(selection);
					}
				});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick();
			}
		});

		tableViewer.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) != 0
						&& (e.stateMask & SWT.CTRL) != 0) {
					StructuredSelection selection = (StructuredSelection) tableViewer
							.getSelection();

					if (selection.size() == 1) {
						Object element = selection.getFirstElement();
						if (element.equals(tableViewer.getElementAt(0))) {
							text.setFocus();
						}
						if (tableViewer.getElementAt(tableViewer.getTable()
								.getSelectionIndex() - 1) instanceof JavaTypeItemsListSeparator)
							tableViewer.getTable()
									.setSelection(
											tableViewer.getTable()
													.getSelectionIndex() - 1);
						tableViewer.getTable().notifyListeners(SWT.Selection,
								new Event());

					}
				}

				if (e.keyCode == SWT.ARROW_DOWN
						&& (e.stateMask & SWT.SHIFT) != 0
						&& (e.stateMask & SWT.CTRL) != 0) {

					if (tableViewer.getElementAt(tableViewer.getTable()
							.getSelectionIndex() + 1) instanceof JavaTypeItemsListSeparator)
						tableViewer.getTable().setSelection(
								tableViewer.getTable().getSelectionIndex() + 1);
					tableViewer.getTable().notifyListeners(SWT.Selection,
							new Event());
				}

			}
		});

		applyDialogFont(content);
		applyFilter();

		updateStatus(ERROR_Status);
		return dialogArea;
	}

	private void handleDoubleClick() {
		super.okPressed();
	}

	protected void handleSelected(StructuredSelection selection) {
		IStatus status;

		if (selection.size() == 0) {
			status = ERROR_Status;

			if (lastSelection != null) {
				tableViewer.update(lastSelection, null);
			}

			lastSelection = null;

		} else {
			status = OK_Status;
			List items = selection.toList();
			for (Iterator it = items.iterator(); it.hasNext();) {
				Object o = it.next();

				if (o instanceof JavaTypeItemsListSeparator) {
					continue;
				}
			}

			if (lastSelection != null) {
				tableViewer.update(lastSelection, null);
			}
			tableViewer.update(items.toArray(), null);
			lastSelection = items.toArray();
		}

		updateStatus(status);
	}

	private void createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		Label label = new Label(header, SWT.NONE);
		label.setText(JavaTypeConstants.LABEL_SEARCH_PATTERN);
		label.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					text.setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		header.setLayoutData(gd);
	}

	private void createLabels(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		Label listLabel = new Label(labels, SWT.NONE);
		listLabel.setText(JavaTypeConstants.LABEL_TABLE_VIEWER);

		listLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					tableViewer.getTable().setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		listLabel.setLayoutData(gd);

		progressLabel = new Label(labels, SWT.RIGHT);
		progressLabel.setLayoutData(gd);

		labels.setLayoutData(gd);
	}

	protected void applyFilter() {
		JavaTypeItemsFilter newFilter = new JavaTypeItemsFilter(text.getText());
		if (filter != null && filter.equalsFilter(newFilter)) {
			return;
		}
		filterJob.cancel();
		this.filter = newFilter;
		if (this.filter != null) {
			filterJob.schedule();
		}
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private class FilterJob extends Job {

		protected JavaTypeItemsFilter itemsFilter;

		public FilterJob() {
			super(JavaTypeConstants.LABEL_FILTER_JOB);
			setSystem(true);
		}

		protected final IStatus run(IProgressMonitor monitor) {
			return doRun(monitor);
		}

		protected IStatus doRun(IProgressMonitor monitor) {
			try {
				if (monitor.isCanceled())
					return Status.OK_STATUS;

				this.itemsFilter = filter;
				contentProvider.reset();
				contentProvider.addHistoryItems(itemsFilter);
				if (filter.getPattern().length() != 0) {
					filterContent(monitor);
				}

				if (monitor.isCanceled())
					return Status.OK_STATUS;

				contentProvider.refresh();
			} catch (CoreException e) {
				cancel();
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
						IStatus.ERROR, "error", e);
			}
			return Status.OK_STATUS;
		}

		protected void filterContent(IProgressMonitor monitor)
				throws CoreException {

			if (lastCompletedFilter != null
					&& lastCompletedFilter.isSubFilter(this.itemsFilter)) {

				int length = lastCompletedResult.size() / 500;
				monitor.beginTask("Filtering content", length);

				for (int pos = 0; pos < lastCompletedResult.size(); pos++) {

					Object item = lastCompletedResult.get(pos);

					if (monitor.isCanceled())
						break;
					if (itemsFilter.matchItem(item)) {
						contentProvider.add(item);
					}

					if ((pos % 500) == 0) {
						monitor.worked(1);
					}
				}
				contentProvider.rememberResult(itemsFilter);

			} else {

				lastCompletedFilter = null;
				lastCompletedResult = null;

				SubProgressMonitor subMonitor = null;
				if (monitor != null) {
					monitor.beginTask("Filtering content", 100);
					subMonitor = new SubProgressMonitor(monitor, 95);

				}

				fillContentProvider(contentProvider, itemsFilter, subMonitor);

				if (monitor != null && !monitor.isCanceled()) {
					monitor.worked(2);
					contentProvider.rememberResult(itemsFilter);
					monitor.worked(3);
				}
			}

		}

	}

	protected void fillContentProvider(JavaTypeContentProvider provider,
			JavaTypeItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		JavaTypeItemsFilter pluginItemFilter = (JavaTypeItemsFilter) itemsFilter;
		JavaTypeSearchRequestor requestor = new JavaTypeSearchRequestor(
				pluginItemFilter, provider, javaTypeUtil);
		progressMonitor.setTaskName("Search");

		JavaTypeSearchEngine engine = new JavaTypeSearchEngine(requestor);
		try {
			engine.search();
		} catch (OpenJavaTypeByAuthorException e) {
			OpenJavaTypeByAuthorLog.log(e);
		}
	}

	public JavaTypeItemsFilter getFilter() {
		return filter;
	}

	public void setLastCompletedFilter(JavaTypeItemsFilter lastCompletedFilter) {
		this.lastCompletedFilter = lastCompletedFilter;
	}

	public void setLastCompletedResult(List lastCompletedResult) {
		this.lastCompletedResult = lastCompletedResult;
	}

	public JavaTypeContentProvider getContentProvider() {
		return contentProvider;
	}

	public JavaTypeInfoUtil getJavaTypeUtil() {
		return javaTypeUtil;
	}

}
