import org.json.JSONArray;
import org.json.JSONObject;
import org.joda.time.DateTime;

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

}
