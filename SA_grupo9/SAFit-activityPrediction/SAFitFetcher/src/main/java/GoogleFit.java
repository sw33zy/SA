import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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

    public static List<String> requestHR(DateTime start, DateTime end, String ACCESS_TOKEN, FileWriter hbFile) throws IOException {

        URL url = new URL("https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        con.setDoOutput(true);
        List<String> data;

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

    public static void requestActivity(String ACCESS_TOKEN, DateTime yesterday, DateTime currentTime, FileWriter activityFile, FileWriter hbFile) throws IOException {
        URL url = new URL("https://www.googleapis.com/fitness/v1/users/me/sessions?startTime=" + removeTimeZone(yesterday) + "&endTime=" + removeTimeZone(currentTime));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        con.setDoOutput(true);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        List<String> data = ParseActivity.parse(content.toString());
        List<String> data2 = new ArrayList<>();

        List<Map.Entry<DateTime, DateTime>> intervals = ParseActivity.intervals(content.toString());
        for (Map.Entry<DateTime, DateTime> i : intervals) {
            if(requestHR(i.getKey(), i.getValue(), ACCESS_TOKEN, hbFile).size() != 0) {
                data2.add(requestHR(i.getKey(), i.getValue(), ACCESS_TOKEN, hbFile).get(0));
            } else {
                data2.add(" null");
            }
        }

        for (String d : data) {
            activityFile.write(d + ";" + data2.get(data.indexOf(d)));
            activityFile.write("\n");
        }


        in.close();
    }

    public static void requestSleep(String ACCESS_TOKEN, DateTime yesterday, DateTime currentTime, FileWriter sleepFile, FileWriter hbFile) throws IOException {
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
                "  \"startTimeMillis\": " + yesterday.getMillis() + ",\n" +
                "  \"endTimeMillis\": " + currentTime.getMillis() + "\n" +
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
            List<String> data = ParseSleep.parse(response.toString());
            List<String> data2 = new ArrayList<>();

            List<Map.Entry<DateTime, DateTime>> intervals = ParseSleep.intervals(response.toString());
            for (Map.Entry<DateTime, DateTime> i : intervals) {
                if(requestHR(i.getKey(), i.getValue(), ACCESS_TOKEN, hbFile).size() != 0) {
                    data2.add(requestHR(i.getKey(), i.getValue(), ACCESS_TOKEN, hbFile).get(0));
                } else {
                    data2.add("null");
                }
            }

            for (String d : data) {
                sleepFile.write(d + "; " + data2.get(data.indexOf(d)));
                sleepFile.write("\n");
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        FileWriter activityFile = new FileWriter("activity2.txt");
        FileWriter sleepFile = new FileWriter("sleep2.txt");
        FileWriter hbFile = new FileWriter("heart_beat2.txt");

        long secondday = 1577923200000L;
        long secondday2 = 1643587200000L;
        //long secondday3 = 1648854000000L;
        long firstday = 1577836800000L;
        long firstday2 = 1643500800000L;
        //long firstday3 = 1648767600000L;


        String ACCESS_TOKEN = "ya29.A0ARrdaM87tDhyvtupdPOcm17c-KqqBGumTti-e6lGRqE2Jke1iER8wMGEn2T1cK4qiHhI0yl9YwqmO_aKmtY_Foq2HlXYF47M1T_VerslmA_LHQw6yjS0bmSf9IPDoewxhSvhl71ZeDcFrbSAkGwMwSslifOkeA";

        DateTime currentTime = new DateTime(secondday2);
        DateTime yesterday = new DateTime(firstday2);

        while (currentTime.getMillis() <= new DateTime().getMillis()) {

            requestActivity(ACCESS_TOKEN, yesterday, currentTime, activityFile, hbFile);
            requestSleep(ACCESS_TOKEN, yesterday, currentTime,sleepFile, hbFile);

            currentTime = currentTime.plusDays(1);
            yesterday = yesterday.plusDays(1);
        }

        activityFile.close();
        sleepFile.close();
        hbFile.close();
    }

}
