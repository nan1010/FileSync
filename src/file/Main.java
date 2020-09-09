/**
 * 
 */
package file;

/**
 * @author 0380009503
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String WORK_TPYE = ConfigMapUtil.getValueByKey("workType");
		if ("file".equals(WORK_TPYE.toLowerCase())) {
			JNotifyFileTool.main(args);
		} else if ("face".equals(WORK_TPYE.toLowerCase())) {
			JNotifyFaceLocal.main(args);
		} else {
			System.out.println("请输入正确的工作模式");
			return;
		}
	}

}
