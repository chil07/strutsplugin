package sample.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String args[]) {
		String a = "{0}{1}Action";
		

		boolean hasPrefix = false;
		List<String> aryStr = new ArrayList<String>();
		String input = "user_add_del_A";
		String testname = "[\\w\\W]*_[\\w\\W]*_[\\w\\W]*_A";
		String name = "*_*_*_A";
		Pattern pattern = Pattern.compile(testname);
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			System.out.println("yes");
		}	
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
		for (int tj = 0; tj < aryStr.size(); tj++) {
			System.out.println("[code]:" + aryStr.get(tj));
		}
		int lastk = 0;
		boolean flag = false;
		int tj = 0;
		List<String> result = new ArrayList<String>();
		for (; tj < aryStr.size(); tj++) {
			if (aryStr.get(tj) == "*") {
				flag = true;
				continue;
			} else {
				int tmpi = input.indexOf(aryStr.get(tj));
				if (flag) {
					System.out.println("lastk:"+lastk+"tmpi:"+tmpi+"len:"+input.length());
					String tmpre = input.substring(lastk, tmpi);
					result.add(tmpre);
					System.out.println(tmpre);
					flag = false;
				}
				lastk = tmpi + aryStr.get(tj).length();
				input = input.substring(lastk);
				lastk = 0;
			}
		}
		StringBuffer sb = new StringBuffer();
		
		for(int j = 0; j < result.size(); j++){
			a = a.replace("{"+(j+1)+"}", result.get(j));
			sb.append(result.get(j));
		}
		a = a.replace("{0}",sb.toString());
		System.out.println(a);

	}

}
