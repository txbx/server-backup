package backup;

import cn.hutool.core.util.ObjectUtil;

/**
 * @author : txb
 * @date: 2022/11/4 14:53
 * @description :
 */
public class Const {

    public static final String ServerName;

    public static final String Parent;

    public static final String CredentialsJsonPath = "/home/auth/auth.json";

    public static final String TarFilePath = "/home/tar";

    public static final String BackupPackage = "/home/server-backup";

    static {
        Parent = ObjectUtil.isNotEmpty(System.getenv("PARENT")) ? System.getenv("PARENT") : "1tnwC9TV8ZdbnGxZQhFwyTwuBjVdFJVz4";
        ServerName = ObjectUtil.isNotEmpty(System.getenv("SERVER_NAME")) ? System.getenv("SERVER_NAME") : "oracle-arm";
    }
}
