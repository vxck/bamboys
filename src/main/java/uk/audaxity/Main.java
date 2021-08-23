package uk.audaxity;


import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws UnirestException {
        String unixTime = ""+ (System.currentTimeMillis() / 1000L);
        int CURR_DAY = 0;

        System.out.println("Flights based on check-ins opening an 1h 30m before flights and boarding based on check-ins closing 30 minutes before departure & 45 minutes before aurigny departures");

        JSONObject airportJson = Unirest.get("https://api.flightradar24.com/common/v1/airport.json?code=sou&plugin[]=schedule&plugin-setting[schedule][mode]=departures&plugin-setting[schedule][timestamp]=" + unixTime + "&page=1&limit=100&token=").asJson().getBody().getObject();

        JSONObject departures = airportJson
                .getJSONObject("result")
                .getJSONObject("response")
                .getJSONObject("airport")
                .getJSONObject("pluginData")
                .getJSONObject("schedule")
                .getJSONObject("departures");
        //System.out.println(departures);


        JSONArray flightInformation = departures.getJSONArray("data");
        int flightCount = flightInformation.length();
        Date lastdate = null;

        for (int i=0;i<flightInformation.length();i++) {
            JSONObject flight = flightInformation.getJSONObject(i).getJSONObject("flight");

            String flightNumber = flight.getJSONObject("identification").getJSONObject("number").getString("default");

            String flightName = flight.getJSONObject("airport").getJSONObject("destination").getString("name").split(" ")[0];
            JSONObject flightTimes = flight.getJSONObject("time");

            JSONObject scheduledTimes = flightTimes.getJSONObject("scheduled");
            long departureTime = scheduledTimes.getLong("departure");

            Instant instant = Instant.ofEpochSecond( departureTime );
            Date date = Date.from( instant );

            Instant departureinstant = Instant.ofEpochSecond( departureTime-5400 );
            Date checkindate = Date.from( departureinstant );


            Instant checkinifinishinstant = null;


            //GR being aurigny flights
            if (flightNumber.startsWith("GR")) {
                checkinifinishinstant = Instant.ofEpochSecond( departureTime-2700 );
            } else {
                checkinifinishinstant = Instant.ofEpochSecond( departureTime-1800 );
            }

            Date checkinfinish = Date.from( checkinifinishinstant );


            String abc = checkindate.getHours() + ":" + checkindate.getMinutes();
            String cba = checkinfinish.getHours() + ":" + checkinfinish.getMinutes();





            if (!flightName.contains("dssdkkdsjkskjkds")) {

                if ( CURR_DAY != checkindate.getDate() ) {
                    System.out.println("\u001B[0m"+ "\nDAY " + checkindate.getDate() + "/"+checkindate.getMonth()+"/"+checkindate.getYear());
                    CURR_DAY = checkindate.getDate();
                    lastdate = null;
                }
                System.out.println("\u001B[0m" + "\u001B[34m" + "[" + flightNumber + "] [" + flightName + "] // Check-In @ " + abc + " // Boarding @ " + cba);

                long seconds = 0;
                long hours = 0;
                long minutes = 0;
                if (lastdate != null) {
                    long diff = (date.getTime() - lastdate.getTime());

                    seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                    hours = TimeUnit.MILLISECONDS.toHours(diff);
                    minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

                    if (seconds > 0 || minutes > 0 ) {
                        System.out.println("\u001B[31m" + "[BREAK] Minutes: " + minutes);
                    }
                }

                lastdate = checkinfinish;
            }


        }
    }
}
