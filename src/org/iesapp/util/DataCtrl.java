/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.iesapp.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Josep
 */
public class DataCtrl {

    public Calendar m_cal;
    protected String avui;
     String strDiaSetmana;
    protected int intDiaSetmana;

    protected static final String[] mesos ={" de Gener de ", " de Febrer de ", " de Març de ",
                                   " d'Abril de ", " de Maig ", " de Juny ",
                                    " de Juliol de ", " d'Agost de ", " de Setembre de ",
                                    " d'Octubre de ", " de Novembre de ", " de Desembre de "};

    public DataCtrl()
    {
        m_cal = Calendar.getInstance();
        m_cal.setFirstDayOfWeek(Calendar.MONDAY);

        inicia();
        
    }

    public DataCtrl(java.sql.Date date)
    {
        if(date==null) {
            date=new java.sql.Date(new java.util.Date().getTime());
        }
        m_cal = Calendar.getInstance();
        m_cal.setTime(date);
        m_cal.setFirstDayOfWeek(Calendar.MONDAY);
        inicia();
    }

    public DataCtrl(java.util.Date date)
    {
        if(date==null) {
            date=new java.util.Date();
        }
        m_cal = Calendar.getInstance();
        m_cal.setTime(date);
        m_cal.setFirstDayOfWeek(Calendar.MONDAY);
        inicia();
    }

    public DataCtrl(Calendar cal)
    {
        if(cal==null) {
            cal = Calendar.getInstance();
        }
        m_cal = cal;
        m_cal.setFirstDayOfWeek(Calendar.MONDAY);
        inicia();
    }

    public DataCtrl(String sdate)
    {
        m_cal = Calendar.getInstance();
        m_cal.setFirstDayOfWeek(Calendar.MONDAY);
        DateFormat formatter = null;
        if(sdate.contains("/")) {
            formatter = new SimpleDateFormat("dd/MM/yyyy");
        }
        else if(sdate.contains("-")) {
            formatter = new SimpleDateFormat("dd-MM-yyyy");
        }
            
        java.util.Date date = null;
        try {
            date = (java.util.Date) formatter.parse(sdate);
        } catch (ParseException ex) {
            Logger.getLogger(DataCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }

        m_cal.setTime(date);
        inicia();
    }
    
     public DataCtrl(String sdate, int SQLFORMAT)
    {
        m_cal = Calendar.getInstance();
        m_cal.setFirstDayOfWeek(Calendar.MONDAY);
        DateFormat formatter = null;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
            
        java.util.Date date = null;
        try {
            date = (java.util.Date) formatter.parse(sdate);
        } catch (ParseException ex) {
            Logger.getLogger(DataCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }

        m_cal.setTime(date);
        inicia();
    }
    
    public String getLongData()
    {
        String txt = ""+ m_cal.get(Calendar.DAY_OF_MONTH);
        txt += mesos[m_cal.get(Calendar.MONTH)];
        txt += m_cal.get(Calendar.YEAR);
        return txt;
    }


    protected void inicia()
    {
            int diaavui=m_cal.get(Calendar.DAY_OF_WEEK);
            if(diaavui == Calendar.MONDAY)
            {
                avui = "Dilluns";
                strDiaSetmana = "L";
                intDiaSetmana = 1;
            }
            if(diaavui == Calendar.TUESDAY)
            {
                avui = "Dimarts";
                strDiaSetmana = "M";
                intDiaSetmana = 2;
            }
            if(diaavui == Calendar.WEDNESDAY)
            {
                avui = "Dimecres";
                strDiaSetmana = "X";
                intDiaSetmana = 3;
            }
            if(diaavui == Calendar.THURSDAY)
            {
                avui = "Dijous";
                strDiaSetmana = "J";
                intDiaSetmana = 4;
            }
            if(diaavui == Calendar.FRIDAY)
            {
                avui = "Divendres";
                strDiaSetmana = "V";
                intDiaSetmana = 5;
            }
            if(diaavui == Calendar.SATURDAY)
            {
                avui = "Dissabte";
                strDiaSetmana = "S";
                intDiaSetmana = 6;
            }
            if(diaavui == Calendar.SUNDAY)
            {
                avui = "Diumenge";
                strDiaSetmana = "D";
                intDiaSetmana = 7;
            }

    }

    public java.util.Date getDate()
    {
        return m_cal.getTime();
    }

    public int getIntDia()
    {
        return intDiaSetmana;
    }

    public String getStringDia()
    {
        return avui;
    }

    public String getDataSQL()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(("yyyy-MM-dd"));
        return sdf.format(m_cal.getTime());
    }
    
    public String getDiaMesComplet()
    {
         SimpleDateFormat sdf = new SimpleDateFormat(("dd/MM/yyyy"));
         return sdf.format(m_cal.getTime()); //avui + ", " +
    }


    public String getHora()
    {
         SimpleDateFormat sdf = new SimpleDateFormat(("HH:mm:ss")); //hh 12 hours HH: 24hours
         Calendar cal = Calendar.getInstance();
         cal.setFirstDayOfWeek(Calendar.MONDAY);
         return sdf.format(cal.getTime());
    }
    
     public String getHoraReduida()
    {
         SimpleDateFormat sdf = new SimpleDateFormat(("HH:mm")); //hh 12 hours HH: 24hours
         Calendar cal = Calendar.getInstance();
         cal.setFirstDayOfWeek(Calendar.MONDAY);
         return sdf.format(cal.getTime());
    }

      public String getHoraPunt()
    {
         SimpleDateFormat sdf = new SimpleDateFormat(("HH.mm.ss")); //hh 12 hours HH: 24hours
         Calendar cal = Calendar.getInstance();
         cal.setFirstDayOfWeek(Calendar.MONDAY);
         return sdf.format(cal.getTime());
    }

 
           
}
