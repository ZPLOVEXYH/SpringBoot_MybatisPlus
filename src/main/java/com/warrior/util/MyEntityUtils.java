package com.warrior.util;

import com.mysql.jdbc.StringUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class MyEntityUtils {

    // 表名称
    private static String tablename = "";
    // 字段名称
    private static String[] colnames;
    // 字段类型
    private static String[] colTypes;
    // 数据库字段描述
    private static List<String> colComment = new ArrayList<>();
    // 列名大小
    private static int[] colSizes;
    // 列名小数精度
    private static int[] colScale;
    private static boolean importUtil = false;
    private static boolean importSql = false;
    private static boolean importMath = false;

    private static String driverClassName = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://localhost:3306/mall?zeroDateTimeBehavior=convertToNull&amp;useUnicode=true&amp;characterEncoding=UTF-8";
	private static String dbUserName = "root";
	private static String dbPassWord = "mysql";


    public static void main(String[] args) {
        try {
            Map<String, Object> tableNameMap = getDatabaseNameToTableNameAndColumnName("mall");
            tableNameMap.forEach((a, b) -> {
                tableNameMap.entrySet().forEach(s -> {
                    try {
                        tableToEntity(s.getValue().toString(), "*");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据数据库名称获取得到表集合
     * @param databaseName
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Map getDatabaseNameToTableNameAndColumnName(String databaseName) throws SQLException, ClassNotFoundException {
        Map<String, Object> tableNameMap = new HashMap<>();
        // 加载驱动
        Class.forName(driverClassName);
        // 获得数据库连接
        Connection connection = DriverManager.getConnection(url, dbUserName, dbPassWord);
        // 获得元数据
        DatabaseMetaData metaData = connection.getMetaData();
        // 获得表信息
        ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
        // 保存字段名
        while (tables.next()) {
            Map<String, String> columnNameMap = new HashMap<>();
            //获得表名
            String table_name = tables.getString("TABLE_NAME");
            //通过表名获得所有字段名
            ResultSet columns = metaData.getColumns(null, null, table_name, "%");
            //获得所有字段名
            while (columns.next()) {
                //获得字段名
                String column_name = columns.getString("COLUMN_NAME");
                //获得字段类型
                String type_name = columns.getString("TYPE_NAME");

                columnNameMap.put(column_name, type_name);

                tableNameMap.put(table_name, table_name);
            }

//            tableNameMap.put(table_name, columnNameMap);

        }

        return tableNameMap;
    }

    /**
     * @param tName
     *            数据库表名
     * @param columnNames
     *            需要生成的字段（以英文逗号分隔）
     *
     */
    public static void tableToEntity(String tName, String columnNames) throws Exception
    {
        System.out.println("====================================javabean开始生成======================================");

//        String driverClassName = environment.getProperty("jdbc.driverClassName");
//        String url = environment.getProperty("jdbc.url");
//        String dbUserName = environment.getProperty("jdbc.username");
//        String dbPassWord = environment.getProperty("jdbc.password");

        tablename = tName.toLowerCase();
        // 数据连Connection获取,自己想办法就行.
        Connection conn = null;
        PreparedStatement pstmt = null;
        Class.forName(driverClassName).newInstance();
        conn = (Connection) DriverManager.getConnection(url, dbUserName, dbPassWord);
        String strsql = "";
        if ("".equals(columnNames))
        {
            strsql = "SELECT * FROM " + tablename;// +" WHERE ROWNUM=1" // 一行记录;
        }
        else
        {
            // columnNames以英文逗号拼接
            strsql = "SELECT " + columnNames + " FROM " + tablename;
        }

        try
        {
            pstmt = conn.prepareStatement(strsql);
            pstmt.executeQuery();

            ResultSetMetaData rsmd = pstmt.getMetaData();
            int size = rsmd.getColumnCount(); // 共有多少列
            colnames = new String[size];
            colTypes = new String[size];
            colSizes = new int[size];
            colScale = new int[size];

            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet resultSet = dbmd.getTables(null, "%", tName, new String[]
                    { "TABLE" });
            while (resultSet.next())
            {
                String tableName = resultSet.getString("TABLE_NAME");
                if (tableName.equals(tName))
                {
                    ResultSet rs = null;
                    // 其他数据库不需要这个方法的，直接传null，这个是oracle和db2这么用
                    if (driverClassName.indexOf("mysql") >= 0)
                    {
                        rs = dbmd.getColumns(null, "%", tableName, "%");
                    }
                    else
                    {
                        rs = dbmd.getColumns(null, getSchema(conn), tableName.toUpperCase(), "%");
                    }

                    while (rs.next())
                    {
                        colComment.add(rs.getString("REMARKS"));
                    }
                }
            }

            for (int i = 0; i < rsmd.getColumnCount(); i++)
            {
                rsmd.getCatalogName(i + 1);
                colnames[i] = rsmd.getColumnName(i + 1).toLowerCase();
                colTypes[i] = rsmd.getColumnTypeName(i + 1).toLowerCase();
                colScale[i] = rsmd.getScale(i + 1);
                if ("datetime".equals(colTypes[i]))
                {
                    importUtil = true;
                }
                if ("image".equals(colTypes[i]) || "text".equals(colTypes[i]))
                {
                    importSql = true;
                }
                if (colScale[i] > 0)
                {
                    importMath = true;
                }
                colSizes[i] = rsmd.getPrecision(i + 1);
            }
            String content = parse(colnames, colTypes, colSizes);
            try
            {
                FileSystemView fsv = FileSystemView.getFileSystemView();
                // 获取当前用户桌面路径
                String path = fsv.getHomeDirectory().toString();
                File directory = new File(path);
                if (directory.exists())
                {

                }
                else
                {
                    directory.createNewFile();
                }

                FileWriter fw = new FileWriter(
                        directory.getAbsolutePath() + "\\" + initcap(processTable(tablename)) + ".java");
                PrintWriter pw = new PrintWriter(fw);
                pw.println(content);
                pw.flush();
                pw.close();

                System.out.println("====================================" + initcap(processTable(tablename))
                        + "生成成功，请查看桌面======================================");
            }
            catch (IOException e)
            {
                System.out.println("====================================" + initcap(processTable(tablename))
                        + "生成失败======================================");
                e.printStackTrace();
            }
        }
        catch (SQLException e)
        {
            System.out.println("====================================" + initcap(processTable(tablename))
                    + "生成失败======================================");
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
                if (pstmt != null)
                {
                    pstmt.close();
                }
            }
            catch (SQLException e)
            {
                System.out.println("====================================" + initcap(processTable(tablename))
                        + "生成失败======================================");
                e.printStackTrace();
            }
        }
    }

    /**
     * 其他数据库不需要这个方法 oracle和db2需要
     */
    private static String getSchema(Connection conn) throws Exception
    {
        String schema;
        schema = conn.getMetaData().getUserName();
        if ((schema == null) || (schema.length() == 0))
        {
            throw new Exception("ORACLE数据库模式不允许为空");
        }

        return schema.toUpperCase().toString();

    }

    /**
     * 解析处理(生成实体类主体代码)
     */
    private static String parse(String[] colNames, String[] colTypes, int[] colSizes)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\nimport java.io.Serializable;\r\n");
        if (importUtil)
        {
            sb.append("import java.util.Date;\r\n");
        }
        if (importSql)
        {
            sb.append("import java.sql.*;\r\n\r\n");
        }
        if (importMath)
        {
            sb.append("import java.math.*;\r\n\r\n");
        }
        sb.append("import io.swagger.annotations.ApiModel;\r\n");
        sb.append("import io.swagger.annotations.ApiModelProperty;\r\n");

        // 表注释
        processColnames(sb);
        sb.append("\r\n");
        sb.append("@ApiModel(" + "\"" + initcap(processTable(tablename)) + "\"" + ")\r\n");
        sb.append("public class " + initcap(processTable(tablename)) + " implements Serializable\r\n{\r\n");
        processAllAttrs(sb);
        processAllMethod(sb);
        sb.append("}\r\n");
        // System.out.println(sb.toString());
        return sb.toString();

    }

    /**
     * 处理列名,把空格下划线'_'去掉,同时把下划线后的首字母大写 要是整个列在3个字符及以内,则去掉'_'后,不把"_"后首字母大写.
     * 同时把数据库列名,列类型写到注释中以便查看,
     *
     * @param sb
     */
    private static void processColnames(StringBuffer sb)
    {
        // sb.append("\r\n/** " + tablename + "\r\n");
        // String colsiz = "";
        // String colsca = "";
        for (int i = 0; i < colnames.length; i++)
        {
            // colsiz = colSizes[i] <= 0 ? "" : (colScale[i] <= 0 ? "(" +
            // colSizes[i] + ")" : "(" + colSizes[i] + "," + colScale[i] + ")");
            // sb.append("\t" + colnames[i].toUpperCase() + " " +
            // colTypes[i].toUpperCase() + colsiz + "\r\n");
            char[] ch = colnames[i].toCharArray();
            char c = 'a';
            if (ch.length > 3)
            {
                for (int j = 0; j < ch.length; j++)
                {
                    c = ch[j];
                    if (c == '_' || c == '-')
                    {
                        if (ch[j + 1] >= 'a' && ch[j + 1] <= 'z')
                        {
                            ch[j + 1] = (char) (ch[j + 1] - 32);
                        }
                    }
                }
            }
            String str = new String(ch);
            colnames[i] = str.replaceAll("_", "").replaceAll("-", "");
        }
        // sb.append("*/\r\n");
    }

    /**
     * 处理表名,把空格下划线'_'和'-'去掉,同时把下划线后的首字母大写 要是整个列在3个字符及以内,则去掉'_'后,不把"_"后首字母大写.
     * 同时把数据库列名,列类型写到注释中以便查看,
     *
     * @param tablename
     */
    private static String processTable(String tablename)
    {

        char[] ch = tablename.toCharArray();
        char c = 'a';
        if (ch.length > 3)
        {
            for (int j = 0; j < ch.length; j++)
            {
                c = ch[j];
                if (c == '_' || c == '-')
                {
                    if (ch[j + 1] >= 'a' && ch[j + 1] <= 'z')
                    {
                        ch[j + 1] = (char) (ch[j + 1] - 32);
                    }
                }
            }
        }
        String str = new String(ch) + "Bean";

        return str.replaceAll("_", "").replaceAll("-", "");
    }

    /**
     * 生成所有的方法
     *
     * @param sb
     */
    private static void processAllMethod(StringBuffer sb)
    {
        for (int i = 0; i < colnames.length; i++)
        {
            sb.append("\tpublic void set" + initcap(colnames[i]) + "("
                    + oracleSqlType2JavaType(colTypes[i], colScale[i], colSizes[i]) + " " + colnames[i] + ")\r\n");
            sb.append("\t{\r\n");
            sb.append("\t\tthis." + colnames[i] + "=" + colnames[i] + ";\r\n");
            sb.append("\t}\r\n");
            sb.append("\r\n");

            sb.append("\tpublic " + oracleSqlType2JavaType(colTypes[i], colScale[i], colSizes[i]) + " get"
                    + initcap(colnames[i]) + "()\r\n");
            sb.append("\t{\r\n");
            sb.append("\t\treturn " + colnames[i] + ";\r\n");
            sb.append("\t}\r\n");
            sb.append("\r\n");
        }
    }

    /**
     * 解析输出属性
     *
     * @return
     */
    private static void processAllAttrs(StringBuffer sb)
    {
        sb.append("\r\n");
        sb.append("\tprivate static final long serialVersionUID = 1L;\r\n");
        sb.append("\r\n");
        for (int i = 0; i < colnames.length; i++)
        {
            String comment = "";
            colComment.removeAll(Collections.singleton(null));
            if (colComment.size() > 0)
            {
                if ("".equals(comment = colComment.get(i)))
                {
                    comment = colnames[i];
                }
            }
            if (!StringUtils.isNullOrEmpty(comment))
            {
                sb.append("\t/**\r\n");
                sb.append("\t* " + comment + "\r\n");
                sb.append("\t*/\r\n");
                sb.append("\t@ApiModelProperty(" + "\"" + comment + "\"" + ")\r\n");
            }

            sb.append("\tprivate " + oracleSqlType2JavaType(colTypes[i], colScale[i], colSizes[i]) + " " + colnames[i]
                    + ";\r\n");
            sb.append("\r\n");
        }
        sb.append("\r\n");
    }

    /**
     * 把输入字符串的首字母改成大写
     *
     * @param str
     * @return
     */
    private static String initcap(String str)
    {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z')
        {
            ch[0] = (char) (ch[0] - 32);
        }

        return new String(ch);
    }

    /**
     * Oracle
     *
     * @param sqlType
     * @param scale
     * @return
     */
    private static String oracleSqlType2JavaType(String sqlType, int scale, int size)
    {
        // if (sqlType.equals("integer") || sqlType.equals("int"))
        // {
        // return "Integer";
        // }
        // else if (sqlType.equals("long"))
        // {
        // return "Long";
        // }
        // else if (sqlType.equals("float") || sqlType.equals("float precision")
        // || sqlType.equals("double")
        // || sqlType.equals("double precision"))
        // {
        // return "BigDecimal";
        // }
        // else if (sqlType.equals("number") || sqlType.equals("decimal") ||
        // sqlType.equals("numeric")
        // || sqlType.equals("real"))
        // {
        // return scale == 0 ? (size < 10 ? "Integer" : "Long") : "BigDecimal";
        // }
        // else if (sqlType.equals("varchar") || sqlType.equals("varchar2") ||
        // sqlType.equals("char")
        // || sqlType.equals("nvarchar") || sqlType.equals("nchar"))
        // {
        // return "String";
        // }
        // else if (sqlType.equals("datetime") || sqlType.equals("date") ||
        // sqlType.equals("timestamp"))
        // {
        // return "Date";
        // }

        return "String";
    }

}
