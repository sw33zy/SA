import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SendData {
    public static void setMqtt_client(MqttClient mqtt_client) {
        SendData.mqtt_client = mqtt_client;
    }

    private static MqttClient mqtt_client;

    public SendData(String ADAFRUIT_USERNAME, String ADAFRUIT_AIO_KEY){

        String broker = "tcp://io.adafruit.com:1883"; //Adafruit IO broker
        String client_id = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient mqtt_client = new MqttClient(broker, client_id, persistence);
            setMqtt_client(mqtt_client);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(ADAFRUIT_USERNAME);
            connOpts.setPassword(ADAFRUIT_AIO_KEY.toCharArray());
            System.out.println("Connecting to broker: " + broker);
            mqtt_client.connect(connOpts);
        } catch (MqttException me) {
            System.out.println("reason: " + me.getReasonCode());
            System.out.println("msg: " + me.getMessage());
            System.out.println("loc: " + me.getLocalizedMessage());
            System.out.println("cause: " + me.getCause());
            System.out.println("excep: " + me);
            me.printStackTrace();
        }
    }

    public static void sendData(String msg_content, String ADAFRUIT_USERNAME, String feed) {
        String topic = ADAFRUIT_USERNAME + "/feeds/" + feed;

        int qos = 1; //QoS: 0 - at most once, 1 - at least once, 2 - exactly once

        try {
            MqttMessage message = new MqttMessage(msg_content.getBytes());
            message.setQos(qos);
            mqtt_client.publish(topic, message);
            System.out.println("Message published");
            //mqtt_client.disconnect();
            //System.out.println("Disconnected");

        } catch (MqttException me) {
            System.out.println("reason: " + me.getReasonCode());
            System.out.println("msg: " + me.getMessage());
            System.out.println("loc: " + me.getLocalizedMessage());
            System.out.println("cause: " + me.getCause());
            System.out.println("excep: " + me);
            me.printStackTrace();
        }
    }

    public static void closeConnection() throws MqttException {
        mqtt_client.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }
}
