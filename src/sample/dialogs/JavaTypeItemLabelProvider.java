package sample.dialogs;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

import sample.exception.OpenJavaTypeByAuthorException;
import sample.exception.OpenJavaTypeByAuthorLog;

/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class JavaTypeItemLabelProvider extends LabelProvider implements
		ILabelDecorator {

	private LocalResourceManager imageManager;
	private OpenJavaTypeByAuthorDialog dialog;

	public JavaTypeItemLabelProvider(OpenJavaTypeByAuthorDialog dialog) {
		imageManager = new LocalResourceManager(JFaceResources.getResources());
		this.dialog = dialog;
	}

	public void dispose() {
		super.dispose();
		imageManager.dispose();
	}

	public Image getImage(Object element) {
		if (!(element instanceof JavaTypeNameMatch)) {
			return super.getImage(element);
		}

		return dialog.getJavaTypeUtil()
				.getContributedImageDescriptor(element);
	}

	public String getText(Object element) {
		if (element instanceof JavaTypeItemsListSeparator) {
			return getSeparatorLabel(((JavaTypeItemsListSeparator) element).getName());
		}

		if (!(element instanceof JavaTypeNameMatch)) {
			return super.getText(element);
		}

		try {
			return dialog.getJavaTypeUtil().getFullyQualifiedText(
					(JavaTypeNameMatch) element);
		} catch (OpenJavaTypeByAuthorException e) {
			OpenJavaTypeByAuthorLog.log(e);
		}
		return "";
	}

	private String getSeparatorLabel(String separatorLabel) {
		Rectangle rect = dialog.getTableViewer().getTable().getBounds();

		int borderWidth = dialog.getTableViewer().getTable().computeTrim(0, 0,
				0, 0).width;

		int imageWidth = WorkbenchImages.getImage(
				IWorkbenchGraphicConstants.IMG_OBJ_SEPARATOR).getBounds().width;

		int width = rect.width - borderWidth - imageWidth;

		GC gc = new GC(dialog.getTableViewer().getTable());
		gc.setFont(dialog.getTableViewer().getTable().getFont());

		int fSeparatorWidth = gc.getAdvanceWidth('-');
		int fMessageLength = gc.textExtent(separatorLabel).x;

		gc.dispose();

		StringBuffer dashes = new StringBuffer();
		int chars = (((width - fMessageLength) / fSeparatorWidth) / 2) - 2;
		for (int i = 0; i < chars; i++) {
			dashes.append('-');
		}

		StringBuffer result = new StringBuffer();
		result.append(dashes);
		result.append(" " + separatorLabel + " ");
		result.append(dashes);
		return result.toString().trim();
	}

	public Image decorateImage(Image image, Object element) {
		return image;
	}

	public String decorateText(String text, Object element) {
		if (!(element instanceof JavaTypeNameMatch)) {
			return null;
		}

		try {
			return dialog.getJavaTypeUtil().getFullyQualifiedText(
					(JavaTypeNameMatch) element);
		} catch (OpenJavaTypeByAuthorException e) {
			OpenJavaTypeByAuthorLog.log(e);
		}
		return "";
	}

}