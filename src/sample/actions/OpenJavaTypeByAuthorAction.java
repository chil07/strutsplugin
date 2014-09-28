package sample.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.ide.IDE;

import sample.dialogs.OpenJavaTypeByAuthorDialog;
import sample.exception.OpenJavaTypeByAuthorLog;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class OpenJavaTypeByAuthorAction extends Action implements
		IWorkbenchWindowActionDelegate, IActionDelegate {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		Shell parent = window.getShell();
		SelectionDialog dialog = new OpenJavaTypeByAuthorDialog(parent);
		dialog.setTitle("Open Java File by Author");
		dialog.setMessage("");

		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;

		Object[] files = dialog.getResult();
		if (files != null && files.length > 0) {
			IFile file = null;
			for (int i = 0; i < files.length; i++) {
				file = (IFile) files[i];
				try {
					IDE.openEditor(window.getActivePage(), file, true);
				} catch (CoreException x) {
					OpenJavaTypeByAuthorLog.log(x);
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	public void init(IAction action) {
		// TODO Auto-generated method stub

	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

}
