package sample.engine;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ui.dialogs.SearchPattern;
import org.jdom.Element;

import sample.dialogs.JavaTypeNameMatch;
/**
 * 
 * @author CuiKun cuikunbj@cn.ibm.com
 *
 */
public class JavaTypeItemsFilter {

	private SearchPattern searchPattern;

	public JavaTypeItemsFilter(String pattern) {
		super();
		searchPattern = new SearchPattern();
		searchPattern.setPattern(pattern);
	}

	public boolean isConsistentItem(Object item) {
		return true;
	}

	public String getPattern() {
		return searchPattern.getPattern();
	}

	public boolean equalsFilter(JavaTypeItemsFilter newFilter) {
		if (newFilter != null) {
			return newFilter.getSearchPattern().equals(this.searchPattern);
		}
		return false;
	}

	public boolean isSubFilter(JavaTypeItemsFilter itemsFilter) {
		if (itemsFilter != null) {
			return this.searchPattern.isSubPattern(itemsFilter
					.getSearchPattern());
		}
		return false;
	}

	public SearchPattern getSearchPattern() {
		return searchPattern;
	}
	
	public boolean matchItem(Object item) {
		if (!(item instanceof JavaTypeNameMatch)) {
			return false;
		}
		JavaTypeNameMatch type = (JavaTypeNameMatch) item;
		return matches(type.getAuthorName());
	}

	public boolean matches(String text) {
		if (text == null || "".equals(text)) {
			return false;
		}
		return searchPattern.matches(text);
	}
	
	public List<String> findJavaClass(List<Element> actions){
		List<String> result = new ArrayList<String>();
		if(null!=actions && actions.size() > 0){
			for(int i = 0; i < actions.size(); i++){
				String name = actions.get(i).getAttributeValue("name");
				String cls = actions.get(i).getAttributeValue("class");
				//如果cls是含有包的结构的话
				if(null != cls && !cls.equals("")){
					String[] clses = cls.split("\\.");
					cls = clses[clses.length-1];
				}
				
				String metd = actions.get(i).getAttributeValue("method");
				
				//考虑通配符
				if(name.contains("*")){
					String input = searchPattern.getPattern();
					
					List<String> aryStr = new ArrayList<String>();
					String testname = name.replaceAll("\\*", "[\\w\\W]*");
					Pattern pattern = Pattern.compile(testname);
					Matcher matcher = pattern.matcher(input);
					if (matcher.find()) {
						int lasti = 0;
						int ti = 0;
						for (; ti < name.length(); ti++) {
							if (name.charAt(ti) == '*') {
								String tmpst = name.substring(lasti, ti);
								if (null != tmpst && !tmpst.equals(""))
									aryStr.add(tmpst);
								aryStr.add("*");
								lasti = ti + 1;
							}
						}
						if (lasti != name.length() - 1) {
							String tmpst = name.substring(lasti, ti);
							if (null != tmpst && !tmpst.equals(""))
								aryStr.add(tmpst);
						}
						
						int lastk = 0;
						boolean flag = false;
						int tj = 0;
						List<String> params = new ArrayList<String>();
						for (; tj < aryStr.size(); tj++) {
							if (aryStr.get(tj) == "*") {
								flag = true;
								continue;
							} else {
								int tmpi = input.indexOf(aryStr.get(tj));
								if (flag) {
									
									String tmpre = input.substring(lastk, tmpi);
									params.add(tmpre);
									
									flag = false;
								}
								lastk = tmpi + aryStr.get(tj).length();
								input = input.substring(lastk);
								lastk = 0;
							}
						}
						StringBuffer sb = new StringBuffer();
						
						for(int j = 0; j < params.size(); j++){
							cls = cls.replace("{"+(j+1)+"}", params.get(j));
							sb.append(params.get(j));
						}
						cls = cls.replace("{0}",sb.toString());
						result.add(cls);
					}	
					
				}else{
					//不考虑通配符
					
					if(searchPattern.matches(name)){
						result.add(cls);
					}
				}
				
			}
		}
		return result;
	}

}
