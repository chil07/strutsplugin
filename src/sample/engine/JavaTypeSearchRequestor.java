package sample.engine;

import sample.dialogs.JavaTypeContentProvider;
import sample.dialogs.JavaTypeNameMatch;
import sample.util.JavaTypeInfoUtil;

/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class JavaTypeSearchRequestor {

	private JavaTypeItemsFilter pluginSearchFilter;
	private JavaTypeContentProvider provider;
	private JavaTypeInfoUtil pluginInfoUtil;

	public JavaTypeSearchRequestor(JavaTypeItemsFilter pluginSearchFilter,
			JavaTypeContentProvider provider, JavaTypeInfoUtil pluginInfoUtil) {
		super();
		this.pluginSearchFilter = pluginSearchFilter;
		this.provider = provider;
		this.pluginInfoUtil = pluginInfoUtil;
	}

	public JavaTypeItemsFilter getPluginSearchFilter() {
		return pluginSearchFilter;
	}

	public void setPluginSearchFilter(JavaTypeItemsFilter pluginSearchFilter) {
		this.pluginSearchFilter = pluginSearchFilter;
	}

	public void add(JavaTypeNameMatch match) {
		provider.add(match);
	}

	public JavaTypeInfoUtil getPluginInfoUtil() {
		return pluginInfoUtil;
	}

	public void setPluginInfoUtil(JavaTypeInfoUtil pluginInfoUtil) {
		this.pluginInfoUtil = pluginInfoUtil;
	}

}
