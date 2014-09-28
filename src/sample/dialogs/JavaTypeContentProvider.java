package sample.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import sample.engine.JavaTypeItemsFilter;
import sample.history.JavaTypeSelectionHistory;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
@SuppressWarnings("unchecked")
public class JavaTypeContentProvider implements IStructuredContentProvider {

	private JavaTypeSelectionHistory javaTypeSelectionHistory;

	private OpenJavaTypeByAuthorDialog dialog;

	private List items;

	private List resultItems;

	private List sortedItems;

	public JavaTypeContentProvider(
			JavaTypeSelectionHistory pluginSelectionHistory,
			OpenJavaTypeByAuthorDialog dialog) {
		this.items = new ArrayList();
		this.resultItems = new ArrayList();
		this.sortedItems = new ArrayList();
		this.dialog = dialog;
		this.javaTypeSelectionHistory = pluginSelectionHistory;
	}

	public void add(Object item) {
		boolean contained = false;
		Iterator iterator = items.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().equals(item)) {
				contained = true;
				break;
			}
		}
		if (!contained) {
			this.items.add(item);
		}
	}

	public void addHistoryItems(JavaTypeItemsFilter itemsFilter) {
		if (this.javaTypeSelectionHistory != null) {
			Object[] items = this.javaTypeSelectionHistory.getHistoryItems();
			for (int i = 0; i < items.length; i++) {
				Object item = items[i];
				if (itemsFilter != null) {
					if (itemsFilter.matchItem(item)) {
						if (itemsFilter.isConsistentItem(item)) {
							if (!this.items.contains(item)) {
								this.items.add(item);
							}
						} else {
							this.javaTypeSelectionHistory.remove(item);
						}
					}
				}
			}
		}
	}

	public void refresh() {
		setResultItems();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TableViewer viewer = dialog.getTableViewer();
				if (viewer != null && !viewer.getTable().isDisposed()) {
					viewer.setItemCount(resultItems.size());
					viewer.refresh();
					if (resultItems.size() > 0) {
						viewer.getTable().setSelection(0);
						viewer.getTable().notifyListeners(SWT.Selection,
								new Event());
					}
				}
			}
		});
	}

	public void reset() {
		this.items.clear();
		this.sortedItems.clear();
	}

	private Comparator getHistoryComparator() {
		return new JavaTypeHistoryComparator();
	}

	public void addHistoryElement(Object item) {
		if (this.javaTypeSelectionHistory != null)
			this.javaTypeSelectionHistory.accessed(item);
		if (dialog.getFilter() == null || !dialog.getFilter().matchItem(item)) {
			this.items.remove(item);
			this.sortedItems.remove(item);
		}
		synchronized (sortedItems) {
			Collections.sort(sortedItems, getHistoryComparator());
		}
		this.refresh();
	}

	public boolean isHistoryElement(Object item) {
		if (!(item instanceof JavaTypeNameMatch)) {
			return false;
		}
		if (this.javaTypeSelectionHistory != null) {
			return this.javaTypeSelectionHistory.contains(item);
		}
		return false;
	}

	public void rememberResult(JavaTypeItemsFilter itemsFilter) {
		if (sortedItems.size() != items.size()) {
			synchronized (sortedItems) {
				sortedItems.clear();
				sortedItems.addAll(items);
				Collections.sort(sortedItems, getHistoryComparator());
			}
		}
		List itemsList = new ArrayList();
		itemsList.addAll(sortedItems);
		if (itemsFilter == dialog.getFilter()) {
			dialog.setLastCompletedFilter(itemsFilter);
			dialog.setLastCompletedResult(itemsList);
		}

	}

	private void setResultItems() {
		if (sortedItems.size() != items.size()) {
			synchronized (sortedItems) {
				sortedItems.clear();
				sortedItems.addAll(items);
				Collections.sort(sortedItems, getHistoryComparator());
			}
		}
		resultItems.clear();
		boolean hasHistory = false;
		if (sortedItems.size() > 0) {
			hasHistory = isHistoryElement(sortedItems.get(0));
		}
		for (Object o : sortedItems) {
			if (hasHistory && !isHistoryElement(o)) {
				resultItems.add(new JavaTypeItemsListSeparator(
						"Workspace Matches"));
				hasHistory = false;
			}
			resultItems.add(o);
		}
	}

	public Object[] getElements(Object inputElement) {
		return resultItems.toArray();
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void updateElement(int index) {

		dialog.getTableViewer().replace(
				(resultItems.size() > index) ? resultItems.get(index) : null,
				index);

	}

	private class JavaTypeHistoryComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			if (!(o1 instanceof JavaTypeNameMatch)
					|| !(o2 instanceof JavaTypeNameMatch)) {
				return 0;
			}
			JavaTypeNameMatch type1 = (JavaTypeNameMatch) o1, type2 = (JavaTypeNameMatch) o2;
			boolean history1 = isHistoryElement(type1), history2 = isHistoryElement(type2);
			if (history1 ^ history2) {
				if (history1) {
					return -1;
				} else {
					return 1;
				}
			} else {
				String name1 = null, name2 = null;
				name1 = type1.getAuthorName();
				name2 = type2.getAuthorName();
				if (name1 == null || name2 == null) {
					return 0;
				}
				int nameCompare = name1.compareTo(name2);
				if (nameCompare == 0) {
					return type1.getFullyQualifiedName().compareTo(
							type2.getFullyQualifiedName());
				} else {
					return nameCompare;
				}
			}
		}
	}

}