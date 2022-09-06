import org.joda.time.DateTime;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;


public class GoogleFit {

    public static void main(String[] args) throws IOException, InterruptedException {

        FileWriter activityFile = new FileWriter("activity2.txt");
        FileWriter sleepFile = new FileWriter("sleepNEW.txt");
        FileWriter hbFile = new FileWriter("heart_beat.txt");
        FileWriter activeMinutes = new FileWriter("active_minutes.txt");
        FileWriter calories = new FileWriter("calories.txt");
        FileWriter distance = new FileWriter("distance.txt");
        FileWriter steps = new FileWriter("steps.txt");

        long secondday = 1577923200000L;
        long firstday = 1577836800000L;

        String ACCESS_TOKEN = "ya29.A0ARrdaM9tVmOgMkaXHNhA2o3wsv8TRcbGL_WA0wCyc6A32JQdIWkazSOvPt3b5ldRsBC01PGtpQml9TtQ-6spnJgUMBmbtaD3Xfx6houeDrOx3uJA6MO6JS5Djv-_fz45a2PrbMUMftfncS5Tm2e9tZY57dqHOQ";

        DateTime currentTime = new DateTime(secondday);
        DateTime yesterday = new DateTime(firstday);
        int counter = 0;

        while (currentTime.getMillis() <= new DateTime().getMillis()) {

            ParseActivity.requestActivity(ACCESS_TOKEN, yesterday, currentTime, activityFile);
            ParseSleep.requestSleep(ACCESS_TOKEN, yesterday, currentTime, sleepFile);
            ParseHeartBeat.requestHR(ACCESS_TOKEN, yesterday, currentTime, hbFile);

            List<String> data2 = ParseActiveMinutes.requestAM(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data3 = ParseCalories.requestPC(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data4 = ParseSteps.requestS(yesterday, currentTime, ACCESS_TOKEN);
            List<String> data5 = ParseDistance.requestD(yesterday, currentTime, ACCESS_TOKEN);

            if (data2.size() != 0) {
                activeMinutes.write(data2.get(0) + "\n");
            }

            if (data3.size() != 0) {
                calories.write(data3.get(0) + "\n");
            }

            if (data4.size() != 0) {
                steps.write(data4.get(0) + "\n");
            }

            if (data5.size() != 0) {
                distance.write(data5.get(0) + "\n");
            }
            counter++;
            currentTime = currentTime.plusDays(1);
            yesterday = yesterday.plusDays(1);
            if (counter == 100) {
                sleep(1000);
                counter = 0;
            }
        }

        activityFile.close();
        sleepFile.close();
        hbFile.close();
        activeMinutes.close();
        calories.close();
        distance.close();
        steps.close();
    }

}
