package ear.life.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * 获取设备信息
 * 
 */
public class DeviceInfoTool {
	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public static int getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "V1.0";
		}
	}

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public static String getCarrierByEMSI(String emsi) {

		if (TextUtils.isEmpty(emsi)) {
			return null;
		}
		if (emsi.startsWith("46000") || emsi.startsWith("46002")) {
			return "中国移动";
		} else if (emsi.startsWith("46001")) {
			return "中国联通";
		} else if (emsi.startsWith("46003")) {
			return "中国电信";
		}

		return null;
	}
}
