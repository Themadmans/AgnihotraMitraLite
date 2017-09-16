package com.example.tomer.agnihotramitralite;

/**
 * Created by tomer on 8/4/2017.
 */

public class Entrydate {


        String date;
        String sunrise;
        String sunset;

        Entrydate(String d,String r, String set1)
        {
            this.date=d;
            this.sunrise=r;
            this.sunset=set1;
        }

        public String getDate()
        {
            return this.date;
        }


        public String getSunrise()
        {
            return this.sunrise;
        }


        public String getSunset()
        {
            return this.sunset;
        }

       }

