package file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ConfigMapUtil {
	private static Map<String, String> map = new HashMap<>();
	static {
        try {
            //��ȡ�ļ���
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
            //ת��Ϊ�ַ���
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream,"utf-8");
            //���� Properties ����
            Properties properties = new Properties();
           // prop.load(new InputStreamReader(in, "utf-8"));
            //�����ַ���
            properties.load(inputStreamReader);
            //��ȡ����key
            Enumeration enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                //����key
                String key = (String) enumeration.nextElement();
                //����keyȡֵ
                String value = properties.getProperty(key);
                //����map��
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getValueByKey(String key) {
        return map.get(key);
    }

    public static Map<String, String> getMap() {
        return map;
    }

    public static void setMap(Map<String, String> map) {
        ConfigMapUtil.map = map;
    }
    
    public static String runShell(String command) throws IOException {
		Scanner input = null;
		String result = "";
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			try {
				//�ȴ�����ִ�����
				process.waitFor(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			InputStream is = process.getInputStream();
			input = new Scanner(is);
			while (input.hasNextLine()) {
				result += input.nextLine() + "\n";
			}
			result = command + "\n" + result; //�����������ӡ����
		} finally {
			if (input != null) {
				input.close();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
    
    public static String run(String[] command) throws IOException {
		Scanner input = null;
		String result = "";
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			try {
				//�ȴ�����ִ�����
				process.waitFor(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			InputStream is = process.getInputStream();
			input = new Scanner(is);
			while (input.hasNextLine()) {
				result += input.nextLine() + "\n";
			}
			result = command + "\n" + result; //�����������ӡ����
		} finally {
			if (input != null) {
				input.close();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
}


	


