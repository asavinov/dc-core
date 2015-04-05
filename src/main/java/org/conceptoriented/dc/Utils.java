 /*
 * Copyright 2013-2015 Alexandr Savinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.conceptoriented.dc;

import java.math.BigDecimal;
import java.time.Instant;

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


    public static int[] intersect(int[] source, int[] target) { // Restrict the source array by elements from the second array
        int size=0;
        int[] result = new int[Math.min(source.length, target.length)];
        int trgFirst = 0;
        for(int src=0; src<source.length; src++) {

            for(int trg=trgFirst; trg<target.length; trg++) {
                if(source[src] != target[trg]) continue;
                // Found target in source
                result[size] = source[src]; // Store in the result
                size = size + 1;
                trgFirst = trg + 1;
                break;
            }
        }

        return java.util.Arrays.copyOf(result, size);
    }

    public static boolean isInt32(String[] values) {
        if(values == null) return false;

        for (String val : values)
        {
            if(val == null) continue; // assumption: null is supposed to be a valid number
            try {
                int intValue = Integer.parseInt((String) val);
            }
            catch(Exception e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDouble(String[] values) {
        if(values == null) return false;

        for (String val : values)
        {
            if(val == null) continue; // assumption: null is supposed to be a valid number
            try {
                double doubleValue = Double.parseDouble((String) val);
            }
            catch(Exception e) {
                return false;
            }
        }
        return true;
    }

    public static int toInt32(Object val) {
        if(val == null) {
            return 0;
        }
        else if (val instanceof Integer) {
             return ((Integer) val).intValue();
        }
        else if (val instanceof Double) {
            return ((Double) val).intValue();
        }
        else if (val instanceof Boolean) {
            return ((Boolean) val) == true ? 1 : 0;
        }
        else if (val instanceof String) {
             return Integer.parseInt((String) val);
        }
        else {
             String toString = val.toString();
             if (toString.matches("-?\\d+"))
             {
                  return Integer.parseInt(toString);
             }
             throw new IllegalArgumentException("This Object doesn't represent an int");
        }
    }

    public static double toDouble(Object val) {
        if(val == null) {
            return 0.0;
        }
        else if (val instanceof Integer) {
             return ((Integer) val).doubleValue();
        }
        else if (val instanceof Double) {
            return ((Double) val).doubleValue();
        }
        else if (val instanceof Boolean) {
            return ((Boolean) val) == true ? 1.0 : 0.0;
        }
        else if (val instanceof String) {
             return Double.parseDouble((String) val);
        }
        else {
             String toString = val.toString();
             if (toString.matches("-?\\d+"))
             {
                  return Double.parseDouble(toString);
             }
             throw new IllegalArgumentException("This Object doesn't represent a double");
        }
    }

    public static BigDecimal toDecimal(Object val) {
        if(val == null) {
            return null;
        }
        else if (val instanceof BigDecimal) {
             return (BigDecimal)val;
        }
        else {
            return new BigDecimal(val.toString());
        }
    }

    public static boolean toBoolean(Object val) {
        if(val == null) {
            return false;
        }
        if (val instanceof Integer) {
             return ((Integer) val) == 0 ? false : true;
        }
        else if (val instanceof Double) {
            return ((Double) val) == 0.0 ? false : true;
        }
        else if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        }
        else if (val instanceof String) {
             return ((String) val).equals("0") || ((String) val).equals("false") ? false : true;
        }
        else {
             throw new IllegalArgumentException("This Object doesn't represent a boolean");
        }
    }

    public static Instant toDateTime(Object val) {
        if(val == null) {
            return null;
        }
        else if (val instanceof Instant) {
             return ((Instant) val);
        }
        else {
            return Instant.parse(val.toString());
        }
    }

}
