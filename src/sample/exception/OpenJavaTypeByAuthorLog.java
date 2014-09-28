package sample.exception;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sample.Activator;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class OpenJavaTypeByAuthorLog {
	public static void log(Exception e) {
		String message = "Plugin Search Error";
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, message, e));
	}
}
