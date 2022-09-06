import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GoogleFit {


    public static void main(String[] args) throws IOException, InterruptedException {

        FileWriter dataset = new FileWriter("dataset.txt");

        long secondday = 1577923200000L; // HH:MIN 00:00
        long secondday2 = 1643587200000L; // HH:MIN 00:00

        long firstday = 1577836800000L; // HH:MIN 00:00
        long firstday2 = 1643500800000L; // HH:MIN 00:00

        long seccondDay1 = 1577923200000L;  // HH:MIN 16:00
        long seccondDay  = 1645027200000L; // HH:MIN 16:00
        long firstDay1   = 1577836800000L; // HH:MIN 16:00
        long firstDay    = 1644940800000L; // HH:MIN 16:00

        long firstDay3 = 1583452800000L;
        long secondDay3 = 1583539200000L;



        String ACCESS_TOKEN = "ya29.A0ARrdaM9r1yhPTnbZQIxk0fFqrqC0XdELnAIwUb9Y17VtLVvZF3HgRgzPhcr8GCcTNduwntg8bg3InwoM26vjWeRVsFutJFlXzDk-fAqssNo_XHmB3RmIHnFeiiESiVeqBZZj7AqrQQILEg-uh850o-jqUYVk1g";

        DateTime currentTime = new DateTime(secondDay3);
        DateTime yesterday = new DateTime(firstDay3);

        while (currentTime.getMillis() <= new DateTime().getMillis()) {

            List<String> data1 = ParseHeartBeat.requestHR(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data2 = ParseActiveMinutes.requestAM(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data3 = ParseCalories.requestPC(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data4 = ParseSteps.requestS(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data5 = ParseDistance.requestD(yesterday, currentTime, ACCESS_TOKEN);

            if(data1.size()!=0 && data2.size()!=0 && data3.size()!=0 && data4.size()!=0 && data5.size()!=0) {
                dataset.write(data1.get(0) + data2.get(0) + data3.get(0) + data4.get(0) + data5.get(0));
                dataset.write("\n");
            }

            currentTime = currentTime.plusDays(1);
            yesterday = yesterday.plusDays(1);
        }

        dataset.close();
    }

}
