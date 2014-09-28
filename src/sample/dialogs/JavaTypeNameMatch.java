package sample.dialogs;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
import org.eclipse.core.resources.IFile;

public class JavaTypeNameMatch {

	private IFile file;
	private String authorName;

	public String getFullyQualifiedName() {
		return file.toString();
	}

	public String getSimpleTypeName() {
		return file.getName();
	}

	public JavaTypeNameMatch(IFile file, String authorName) {
		super();
		this.file = file;
		this.authorName = authorName;
	}

	public String getPackageName() {
		String path = file.getFullPath().toString();
		String[] segs = path.split("/");
		StringBuffer packageName = new StringBuffer();
		for (int i = 3; i < segs.length - 2; i++) {
			packageName.append(segs[i] + ".");
		}
		if (segs.length - 2 >= 3) {
			packageName.append(segs[segs.length - 2]);
		}
		return packageName.toString();
	}

	public String getContainerName() {
		String path = file.getFullPath().toString();
		String[] segs = path.split("/");
		StringBuffer containerName = new StringBuffer();
		if (segs.length > 1) {
			containerName.append(segs[1]);
		}
		if (segs.length > 2) {
			containerName.append("/" + segs[2]);
		}
		return containerName.toString();
	}

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JavaTypeNameMatch) {
			JavaTypeNameMatch type = (JavaTypeNameMatch) obj;
			if (type.getFile().getFullPath().equals(this.file.getFullPath())) {
				return true;
			}
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return this.file.getFullPath().toString();
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

}
