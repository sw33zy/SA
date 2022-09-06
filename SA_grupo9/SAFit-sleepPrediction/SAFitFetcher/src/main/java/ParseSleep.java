import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

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


    public static List<Map.Entry<String, Map.Entry<DateTime, DateTime>>> intervals(String responseBody) {

        Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "Awake (during sleep cycle)");
        types.put(2, "Sleep");
        types.put(3, "Out-of-bed");
        types.put(4, "Light sleep");
        types.put(5, "Deep sleep");
        types.put(6, "REM");
        List<Map.Entry<String, Map.Entry<DateTime, DateTime>>> intervals = new ArrayList<>();
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
                    JSONArray value = ((JSONObject) obj3).getJSONArray("value");
                    for (Object obj4 : value) {
                        int s = (Integer) ((JSONObject) obj4).getInt("intVal");
                        intervals.add(new AbstractMap.SimpleEntry<>(types.get(s), new AbstractMap.SimpleEntry<>(start,end)) );

                    }
                }
            }
        }

        return intervals;
    }

}

