/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.iesapp.util;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Josep
 */
public final class StringUtils {

    public static final int CASE_INSENSITIVE = 0;
    public static final int CASE_UPPER = 1;

//
// ordena un map pel seus valors i permet valors repetits
//
   public static Map sortByComparator(Map unsortMap) {

        List list = new LinkedList(unsortMap.entrySet());

        //sort list based on comparator
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
	           return ((Comparable) ((Map.Entry) (o1)).getValue())
	           .compareTo(((Map.Entry) (o2)).getValue());
             }
	});

        //put sorted list into map again
	Map sortedMap = new LinkedHashMap();
	for (Iterator it = list.iterator(); it.hasNext();) {
	     Map.Entry entry = (Map.Entry)it.next();
	     sortedMap.put(entry.getKey(), entry.getValue());
	}
	return sortedMap;

    }


//
// Ordena un mapa segons els seus valors (no funciona be per valors repetits)
//
public static HashMap getSortedMap(HashMap hmap)
{
    HashMap map = new LinkedHashMap();
    List mapKeys = new ArrayList(hmap.keySet());
    List mapValues = new ArrayList(hmap.values());
    hmap.clear();
    TreeSet sortedSet = new TreeSet(mapValues);
    Object[] sortedArray = sortedSet.toArray();
    int size = sortedArray.length;
    // a) Ascending sort

    for (int i=0; i<size; i++)
    {

    map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), sortedArray[i]);

    }
    return map;
}

    public static String anyAcademic()
    {
        String curs = "";
        Calendar cal = Calendar.getInstance();
        int mes = cal.get(Calendar.MONTH);
        int any = cal.get(Calendar.YEAR);

        if(mes>=Calendar.SEPTEMBER)
            curs = any + "/" + (any+1);
        else
            curs = (any-1) + "-" + any;

        return curs;
    }

     public static String anyAcademic_primer()
    {
        String curs = "";
        Calendar cal = Calendar.getInstance();
        int mes = cal.get(Calendar.MONTH);
        int any = cal.get(Calendar.YEAR);

        if(mes>=Calendar.SEPTEMBER)
            curs = ""+any;
        else
            curs = ""+(any-1);

        return curs;
    }

      public static int anyAcademic_primer_int()
      {
        int curs = 0;
        Calendar cal = Calendar.getInstance();
        int mes = cal.get(Calendar.MONTH);
        int any = cal.get(Calendar.YEAR);

        if(mes>=Calendar.SEPTEMBER)
            curs = any;
        else
            curs = (any-1);

        return curs;
    }

    public static String AfterLast(String val, String code)
    {
        if(val==null || val.equals("")) return "";

        String txt="";
        int i0 = val.lastIndexOf(code);
        int len = val.length();
      // System.out.println("io es"+i0);
        if(i0>=0 && i0<len) txt = val.substring(i0 + code.length(), len);

        return txt;
    }


    public static String BeforeLast(String val, String code)
    {

        if(val==null || val.equals("")) return "";

        String txt="";
        int i0 = val.lastIndexOf(code);
        if(i0>=0) txt = val.substring(0,i0);

        return txt;
    }


    public static String AfterFirst(String val, String code) {
        if(val==null || val.equals("")) return "";

        String txt="";
        int i0 = val.indexOf(code);
        int len = val.length();

        //System.out.println("io es"+i0);
        if(i0>=0 && i0<len) txt = val.substring(i0 + code.length(), len);

        return txt;
    }

    public static String BeforeFirst(String val, String code) {
       if(val==null || val.equals("")) return "";

        String txt="";
        int i0 = val.indexOf(code);
        if(i0>=0) txt = val.substring(0,i0);

        return txt;
    }


    //converteix un string del tipus yyyy-mm-dd a dd/mm/yyyy
    public static String Sql2EUData(String date, int dbType)
    {
       String any = "";
       String dia = "";
       String mes = "";

       if(dbType == 1)
       {
         any = AfterLast(date, "/");
         String txt = BeforeLast(date, "/");
         mes = AfterLast(txt, "/");
         dia = BeforeLast(txt, "/");
       }
       else if(dbType == 2)
       {
        dia = AfterLast(date, "-");
        String txt = BeforeLast(date, "-");
        mes = AfterLast(txt, "-");
        any = BeforeLast(txt, "-");
       }

       return (dia+ "/" + mes + "/" + any);

    }

    //converteix un string del tipus dd/mm/yyyy a yyyy-mm-dd
    public static String EUData2Sql(String date)
    {
       String any = "";
       String dia = "";
       String mes = "";

       any = AfterLast(date, "/");
       String txt = BeforeLast(date, "/");
       mes = AfterLast(txt, "/");
       dia = BeforeLast(txt, "/");

       return (any+ "-" + mes + "-" + dia);

    }

    public static String noNull(String var)
    {
        if(var == null)
            return "";
        else
            return var.trim();
    }


     public static ArrayList<String> parseStringToArray(String txt, String sep, int mode) {

         String text = "";
         txt = txt==null?"":txt;
        
         if(mode==1) {
             text = txt.toUpperCase();
         }
         else {
             text = txt;
         }

        ArrayList<String> out = new ArrayList<String>();
        if(txt == null) {
             return out;
         }
         
         if(text.length()==0) return out;

         int i0 = text.lastIndexOf(sep);
         if(i0<0)
         {
             out.add(text.trim());
             return out;
         }

         while(i0>-1)
         {
             String tros = text.substring(i0+sep.length(),text.length());
             tros = tros.trim();
             if(!tros.equals("")) out.add(tros);
             text = text.substring(0, i0);
             i0 = text.lastIndexOf(sep);
         }
          String tros = text.trim();
          if(!tros.equals(""))
                out.add(tros);

          //aquest out esta invertit (darrer element al primer)
          ArrayList<String> out2 = new ArrayList<String>();
          for(int i=0; i<out.size(); i++)
          {
              out2.add(out.get(out.size()-i-1));
          }


          return out2;
    }

    public static String AddZeros(int nexp) {
        String codig="";

        if(nexp<10)
        {
            codig = "0000"+nexp;
        }
        else if(nexp>=10 && nexp<100)
        {
            codig = "000"+nexp;
        }
        else if(nexp>=100 && nexp<1000)
        {
            codig = "00"+nexp;
        }
        else if(nexp>=1000 && nexp<10000)
        {
            codig = "0"+nexp;
        }
        else
        {
            codig = ""+nexp;
        }

        return codig;
    }

    public static HashMap StringToHash(String txt, String sep)
    {
        return StringToHash(txt, sep, true);
    }
    
    public static HashMap StringToHash(final String txt, final String sep, final boolean delimiter)
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String close_tag = "";
        String open_tag="";
        if(delimiter)
        {
            close_tag = "}";
            open_tag="{";   
        }
        String texte = txt;
        if(!txt.trim().endsWith(sep))
        {
            texte += sep;
        }
        ArrayList<String> parsed = parseStringToArray(texte, close_tag+sep, 0);
        //System.out.println("parsed::: "+parsed);
        for(int i=0; i<parsed.size(); i++)
        {
            String s = parsed.get(i);
            String key = BeforeLast(s,"="+open_tag).trim();
            String value = AfterLast(s,"="+open_tag).trim();
            map.put(key, value);
        }

        return map;
    }

    
    public static LinkedHashMap StringToLinkedHash(final String txt, final String sep, final boolean delimiter)
    {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        String close_tag = "";
        String open_tag="";
        if(delimiter)
        {
            close_tag = "}";
            open_tag="{";   
        }
        String texte = txt;
        if(!txt.trim().endsWith(sep))
        {
            texte += sep;
        }
        ArrayList<String> parsed = parseStringToArray(texte, close_tag+sep, 0);
        //System.out.println("parsed::: "+parsed);
        for(int i=0; i<parsed.size(); i++)
        {
            String s = parsed.get(i);
            String key = BeforeLast(s,"="+open_tag).trim();
            String value = AfterLast(s,"="+open_tag).trim();
            map.put(key, value);
        }

        return map;
    }
    
    public static String HashToString(HashMap<String,Object> map, String sep)
    {
        String cadena="";

        for(String ky: map.keySet())
        {
            String value = (String) map.get(ky);
            value = value.replaceAll("=","?");
            value = value.replaceAll(sep,"?");
            cadena += ky +"={"+value +"}"+sep;
        }
 
        return cadena;
    }

      public static String formatTime(Time time) {
       String aux = "";
       SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
       aux = formatter.format(time);
       return aux;
    }

    public static String formataNom(String rawNom) {
       String aux = "";

       String nom = AfterLast(rawNom,",").trim();
       String apellidos = BeforeLast(rawNom,",").trim();
      
       aux = nom + " "+apellidos;
       return aux;
    }

    public static DefaultComboBoxModel listAsCombo(ArrayList<String> list) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(int i=0; i<list.size(); i++)
        {
           model.addElement(list.get(i));
        }
        return model;
    }



/**
 * Returns <0 if v1<v2, >0 if v1>v2, 0 if v1=v2
 * @param v1 Version 1
 * @param v2 version 2
 */
    public static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        return cmp;
    }

    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

    


}


