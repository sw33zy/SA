import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParseSleep {
    public static List<String> parse(String responseBody) {
        Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "Awake (during sleep cycle)");
        types.put(2, "Sleep");
        types.put(3, "Out-of-bed");
        types.put(4, "Light sleep");
        types.put(5, "Deep sleep");
        types.put(6, "REM");

        List<String> data = new ArrayList<String>();

        JSONObject json = new JSONObject(responseBody);
        JSONArray buckets = json.getJSONArray("bucket");
        for (Object obj : buckets) {
            JSONArray dataset = ((JSONObject) obj).getJSONArray("dataset");
            for (Object obj2 : dataset) {
                JSONArray point = ((JSONObject) obj2).getJSONArray("point");
                for (Object obj3 : point) {
                    StringBuilder sb = new StringBuilder();
                    JSONObject fields = (JSONObject) obj3;
                    String startime = (String) fields.getString("startTimeNanos");
                    sb.append(new DateTime(TimeUnit.MILLISECONDS.convert(Long.parseLong(startime), TimeUnit.NANOSECONDS)));
                    sb.append(",");
                    String endtime = (String) fields.getString("endTimeNanos");
                    sb.append(new DateTime(TimeUnit.MILLISECONDS.convert(Long.parseLong(endtime), TimeUnit.NANOSECONDS)));
                    sb.append(",");
                    JSONArray value = ((JSONObject) obj3).getJSONArray("value");
                    for (Object obj4 : value) {
                        int s = (Integer) ((JSONObject) obj4).getInt("intVal");
                        sb.append( types.get(s));
                        data.add(sb.toString());
                    }
                }
            }
        }
        //System.out.println(sb);
        return data;
    }

    public static List<Map.Entry<DateTime, DateTime>> intervals(String responseBody) {

        List<Map.Entry<DateTime, DateTime>> intervals = new ArrayList<>();
        DateTime start,end;

        JSONObject json = new JSONObject(responseBody);
        JSONArray buckets = json.getJSONArray("bucket");
        for (Object obj : buckets) {
            JSONArray dataset = ((JSONObject) obj).getJSONArray("dataset");
            for (Object obj2 : dataset) {
                JSONArray point = ((JSONObject) obj2).getJSONArray("point");
                for (Object obj3 : point) {
                    JSONObject fields = (JSONObject) obj3;
                    String startime = (String) fields.getString("startTimeNanos");
                    start = new DateTime(TimeUnit.MILLISECONDS.convert(Long.parseLong(startime), TimeUnit.NANOSECONDS));
                    String endtime = (String) fields.getString("endTimeNanos");
                    end = new DateTime(TimeUnit.MILLISECONDS.convert(Long.parseLong(endtime), TimeUnit.NANOSECONDS));
                    intervals.add(new AbstractMap.SimpleEntry<>(start,end));
                }
            }
        }

        return intervals;
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
            for (String d : data) {
                sleepFile.write(d);
                sleepFile.write("\n");
            }
        }
    }
}

