
import com.google.auth.oauth2.GoogleCredentials;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GoogleFit {


    public static DateTime removeTimeZone(DateTime time){
        DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        Date date = time.toDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int offset = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
        calendar.add(Calendar.MILLISECOND, offset);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateTime dateTime = new DateTime(calendar);
        return dateTime;
    }

    public static List<String> requestHR(DateTime start, DateTime end, String ACCESS_TOKEN) throws IOException {
        List<String> data;
        //Request ao sleep
        URL url = new URL("https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        con.setDoOutput(true);
        String jsonInputString = "{\n" +
                "  \"aggregateBy\": [{\n" +
                "    \"dataTypeName\": \"com.google.heart_rate.bpm\",\n" +
                "  }],\n" +
                "\"bucketByTime\": { \"durationMillis\":" + (end.getMillis()-start.getMillis()) + "},\n" +
                "  \"startTimeMillis\": " + start.getMillis() + ",\n" +
                "  \"endTimeMillis\": " + end.getMillis() + "\n" +
                "}";

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            data = ParseHeartBeat.parsePartial(response.toString());
        }
        return data;
    }


    public static void requestSleep(String ACCESS_TOKEN, DateTime yesterday, DateTime currentTime, FileWriter sleepFile) throws IOException {
        URL url = new URL("https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        con.setDoOutput(true);


        String jsonInputString = "{\n" +
                "  \"aggregateBy\": [{\n" +
                "    \"dataTypeName\": \"com.google.sleep.segment\",\n" +
                "  }],\n" +
                "  \"startTimeMillis\": " + yesterday.getMillis() +  ",\n" +
                "  \"endTimeMillis\": " + currentTime.getMillis() +  "\n" +
                "}";
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            List<Map.Entry<String, Map.Entry<DateTime, DateTime>>> intervals = ParseSleep.intervals(response.toString());
            if(intervals.size() != 0) {
                DateTime firstLightSleep = intervals.get(0).getValue().getKey();
                DateTime halfHourBefore = firstLightSleep.minusMinutes(30);
                DateTime firstDeepSleep = null;
                int lightSleep = 0, deepSleep = 0;
                int j = 0;
                for (Map.Entry<String, Map.Entry<DateTime, DateTime>> i : intervals) {
                    if (i.getKey().equals("Light sleep")) {
                        lightSleep += Minutes.minutesBetween(i.getValue().getKey(), i.getValue().getValue()).getMinutes();
                    }
                    if (i.getKey().equals("Deep sleep")) {
                        if(j==0){
                            firstDeepSleep = i.getValue().getKey();
                        }
                        deepSleep += Minutes.minutesBetween(i.getValue().getKey(), i.getValue().getValue()).getMinutes();
                        j++;
                    }
                }

                if(requestHR(halfHourBefore, firstLightSleep, ACCESS_TOKEN).size() != 0) {
                    System.out.println(requestHR(halfHourBefore, firstLightSleep, ACCESS_TOKEN).get(0) + halfHourBefore + "," + firstDeepSleep + "," + lightSleep + "," + deepSleep);
                    sleepFile.write(requestHR(halfHourBefore, firstLightSleep, ACCESS_TOKEN).get(0) + halfHourBefore + "," + firstDeepSleep + "," + lightSleep + "," + deepSleep);
                    sleepFile.write("\n");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        FileWriter sleepFile = new FileWriter("sleep.txt");

        long secondday = 1577923200000L; // HH:MIN 00:00
        long secondday2 = 1643587200000L; // HH:MIN 00:00

        long firstday = 1577836800000L; // HH:MIN 00:00
        long firstday2 = 1643500800000L; // HH:MIN 00:00

        long seccondDay1 = 1577923200000L;  // HH:MIN 16:00
        long seccondDay  = 1645027200000L; // HH:MIN 16:00
        long firstDay1   = 1577836800000L; // HH:MIN 16:00
        long firstDay    = 1644940800000L; // HH:MIN 16:00



        String ACCESS_TOKEN = "ya29.A0ARrdaM-VNO_E2byp6HrCHgibJ8hJJdRlDitfE2iRKNxtYkDcyujF_oeZIXIVievHA5BjDD6VxDm2P_MfnEBtk_VkGek2BgyNPHiBQPqqMqBT7qlZn9A4-MOzhc2HzxUDsnqB3ab1rLUchyrqG2Y8dw4hWYKiBw";

        DateTime currentTime = new DateTime(seccondDay1);
        DateTime yesterday = new DateTime(firstDay1);

        while (currentTime.getMillis() <= new DateTime().getMillis()) {

            requestSleep(ACCESS_TOKEN, yesterday, currentTime,sleepFile);

            currentTime = currentTime.plusDays(1);
            yesterday = yesterday.plusDays(1);
        }

        sleepFile.close();
    }

}
