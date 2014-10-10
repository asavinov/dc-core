package com.conceptoriented.com;

public class Utils {

	public static boolean isNullOrEmpty(String param) { 
	    return param == null || param.trim().length() == 0;
	}
	
    public static boolean sameSchemaName(String n1, String n2)
    {
        return sameTableName(n1, n2);
    }

    public static boolean sameTableName(String n1, String n2)
    {
        if (n1 == null || n2 == null) return false;
        if (Utils.isNullOrEmpty(n1) || Utils.isNullOrEmpty(n2)) return false;
        return n1.equalsIgnoreCase(n2);
    }

    public static boolean sameColumnName(String n1, String n2)
    {
        return sameTableName(n1, n2);
    }

}
