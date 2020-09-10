package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author
 *
 *         读取配置文件工具类
 */
public class ConfigMapUtil {
	private static Map<String, String> map = new HashMap<>();
	static {
		try {
			// 读取文件流
			InputStream resourceAsStream = null;
			File outerFile = new File("config.properties");
			if (outerFile.exists()) {
				System.out.println("外部配置文件存在：" + outerFile.getAbsolutePath());
				resourceAsStream = new FileInputStream(outerFile);
			} else {
				System.out.println("使用默认配置文件：");
				resourceAsStream = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("config.properties");
			}
			// 转变为字符流
			InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, "utf-8");
			// 创建 Properties 对象
			Properties properties = new Properties();
			// prop.load(new InputStreamReader(in, "utf-8"));
			// 加载字符流
			properties.load(inputStreamReader);
			// 关闭字节流、字符流
			resourceAsStream.close();
			inputStreamReader.close();
			// 获取所有key
			Enumeration<?> enumeration = properties.propertyNames();
			while (enumeration.hasMoreElements()) {
				// 遍历key
				String key = (String) enumeration.nextElement();
				// 根据key取值
				String value = properties.getProperty(key);
				// 放入map中
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
}
