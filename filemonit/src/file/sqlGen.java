package file;

import java.io.File;

public class sqlGen {

	public static void main(String[] args) {
		File dir = new File("C:\\Users\\0380009079\\Desktop");
		if(dir.exists()) {
			File[] files = dir.listFiles();
			if(files != null && files.length > 0) {
				for(File file:files) {
					String fName = file.getName();
					if(fName.endsWith(".sql")) {
						String subName = fName.substring(0,fName.lastIndexOf(".sql"));
						String sql = "create table "+subName
						+ " select * from `unicorndb`.`"+ subName+"`;";
						System.out.println(sql);
					}
				}
			}
		}
	}

}
