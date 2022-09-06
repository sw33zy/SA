import org.json.JSONArray;
import org.json.JSONObject;
import org.joda.time.DateTime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ParseHeartBeat {
    public static List<String> parse(String responseBody) {

        JSONObject json  = new JSONObject(responseBody);
        List<String> data = new ArrayList<String>();
        JSONArray bucket = json.getJSONArray("bucket");
        for(Object obj : bucket){
            JSONArray dataset = ((JSONObject) obj).getJSONArray("dataset");
            for(Object ob : dataset){
                JSONArray point = ((JSONObject) ob).getJSONArray("point");
                for(Object o : point){
                    StringBuilder sb = new StringBuilder();
                    String startime = (String) ((JSONObject) o).getString("startTimeNanos");
                    sb.append(new DateTime(TimeUnit.MILLISECONDS.convert(Long.parseLong(startime), TimeUnit.NANOSECONDS)));
                    sb.append(",");
                    String endtime = (String) ((JSONObject) o).getString("endTimeNanos");
                    sb.append(new DateTime(TimeUnit.MILLISECONDS.convert(Long.parseLong(endtime), TimeUnit.NANOSECONDS)));
                    sb.append(",");
                    JSONArray value = ((JSONObject) o).getJSONArray("value");
                    for(Object v : value) {
                        int hb = (Integer) ((JSONObject) v).getInt("fpVal");
                        sb.append(hb);
                        sb.append(",");
                    }
                    data.add(sb.toString());
                }
            }
        }
        //System.out.println(sb);
        return data;
    }


    public static List<String> parsePartial(String responseBody) {

        JSONObject json  = new JSONObject(responseBody);
        List<String> data = new ArrayList<String>();
        JSONArray bucket = json.getJSONArray("bucket");
        for(Object obj : bucket){
            JSONArray dataset = ((JSONObject) obj).getJSONArray("dataset");
            for(Object ob : dataset){
                JSONArray point = ((JSONObject) ob).getJSONArray("point");
                for(Object o : point){
                    StringBuilder sb = new StringBuilder();
                    JSONArray value = ((JSONObject) o).getJSONArray("value");
                    for(Object v : value) {
                        int hb = (Integer) ((JSONObject) v).getInt("fpVal");
                        sb.append(hb);
                        sb.append(",");
                    }
                    data.add(sb.toString());
                }
            }
        }
        //System.out.println(sb);
        return data;
    }

    public static void requestHR(String ACCESS_TOKEN, DateTime yesterday, DateTime currentTime, FileWriter hbFile) throws IOException {
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
                "  \"bucketByTime\": { \"durationMillis\": 1800000},\n" +
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
            List<String> data = ParseHeartBeat.parse(response.toString());
            for (String d : data) {
                hbFile.write(d);
                hbFile.write("\n");
            }
        }
    }
}
