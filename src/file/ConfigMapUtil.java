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
            //读取文件流
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
            //转变为字符流
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream,"utf-8");
            //创建 Properties 对象
            Properties properties = new Properties();
           // prop.load(new InputStreamReader(in, "utf-8"));
            //加载字符流
            properties.load(inputStreamReader);
            //关闭字节流、字符流
            resourceAsStream.close();
            inputStreamReader.close();
            //获取所有key
            Enumeration enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                //遍历key
                String key = (String) enumeration.nextElement();
                //根据key取值
                String value = properties.getProperty(key);
                //放入map中
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
				//等待命令执行完成
				process.waitFor(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			InputStream is = process.getInputStream();
			input = new Scanner(is);
			while (input.hasNextLine()) {
				result += input.nextLine() + "\n";
			}
			result = command + "\n" + result; //加上命令本身，打印出来
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
				//等待命令执行完成
				process.waitFor(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			InputStream is = process.getInputStream();
			input = new Scanner(is);
			while (input.hasNextLine()) {
				result += input.nextLine() + "\n";
			}
			result = command + "\n" + result; //加上命令本身，打印出来
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


	


