import com.sun.javaws.exceptions.InvalidArgumentException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns an array (modified to dictionary for Java) with the 'count', 'min', 'max', 'average' and 'stddev' for the device and
 * weights obtained from the data document described by the schema.
 *
 * param String $url the URL of an XML document containing the device-list
 *
 * @return array with keys which in Java is similar to a Map with 'count' which has an integer value equal to the
 *         number of individual devices in the device list (number of device element times the quantity of each)
 *         and 'min', 'max', 'average' and 'stddev' are the minimum, maximum, arithmetic mean (average) and standard deviation
 *         of the weights of all devices. All weight statistics should be returned in US ounces.
 *
 * @throws InvalidArgumentException if the URL is invalid
 * @throws RuntimeException on networking or document processing error
 */

public class DeviceSummary {

    // This function is vital to run the code
    public static void main(String args[]) {

        //the url for the XML file of data
        String url = "https://main.g2planet.com/codetest/example.xml";

        try {
            deviceListInfoSummary(url);
        } catch (Exception e) {
            System.out.println("InvalidArgumentException: "+ e);
        }
    }

    /**
     * deviceListInfoSummary() prints the average, min, max, and stddev of device weights, and then a count of devices
     * @param url The url where the XML data comes from
     * @throws RuntimeException
     */

    public static void deviceListInfoSummary(String url) throws RuntimeException {

        //parse the XML into a dom
        Document dom = getXMLData(url);

        //Get the nodeLists needed from the dom
        NodeList deviceNodeList = dom.getElementsByTagName("f:device");

        NodeList weightNodeList = dom.getElementsByTagName("f:weight");

        //get the count
        int count = getCount(deviceNodeList);

        //get the minimum in ounces
        double minWeight = getWeightMin(weightNodeList);

        //get the maximum in ounces
        double maxWeight = getWeightMax(weightNodeList);

        //get the mean/average in ounces
        double meanWeight = getAverage(weightNodeList);

        //get the stdDev in ounces
        double stdDevWeight = getStandardDev(weightNodeList);


        Map<String, Double> myMap = new HashMap<>();
        //Add to the map the values wanted
        myMap.put("count", Double.valueOf(count));
        myMap.put("min", Double.valueOf(minWeight));
        myMap.put("max", Double.valueOf(maxWeight));
        myMap.put("average", Double.valueOf(meanWeight));
        myMap.put("stddev", Double.valueOf(stdDevWeight));

        //spit out the result
        System.out.println(myMap);


    }

    /**
     * getXMLData()
     * @param url The url where the XML data comes from
     * @return Document - the Document Object Model that holds all the XML data obtained from the url
     */

    public static Document getXMLData(String url) {

        String inputLine = "";
        StringBuffer stringBuffer = new StringBuffer();

        // This first pulls the XML data into a String and then from a String to a Document Object Model (DOM)
        try {
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

            int response = connection.getResponseCode();

            //for debugging purposes
            System.out.println("Response Code: " + response);

            BufferedReader bfReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while((inputLine = bfReader.readLine()) != null){
                    stringBuffer.append(inputLine);
            }
            bfReader.close();

            String xmlString = stringBuffer.toString();


            //convert the string to a dom
            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));

            return dom;


        } catch (Exception e){
            System.out.println(e);
            return null;

        }

    }

    /**
     * getCount()
     * @param nodeList The devicelist information
     * @return count The number of total devices
     */

    public static int getCount(NodeList nodeList) {
        int count = 0;

        for(int i=0; i<nodeList.getLength(); i++){
            Element element = (Element) nodeList.item(i);
            Integer item = Integer.parseInt(element.getAttribute("quantity"));
            int quantityValue = item.intValue();
            count += quantityValue;
        }
        return count;

    }

    /**
     * getAverage()
     * @param nodeList The weight attributes of the devices
     * @return average The average weight of the devices in US ounces
     */
    public static double getAverage(NodeList nodeList) {

        double total = 0;
        int counter = 0;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            counter++;

            //check if the units are pounds, ounces
            switch (element.getAttribute("units")) {

                case "pounds":
                    //Convert the String to a Double
                    Double weight = Double.parseDouble(element.getTextContent()) * 16;

                    //Add that amount to total
                    total += weight.doubleValue();

                    break;

                case "ounces":
                    // Converts the String to a Double
                    weight = Double.parseDouble(element.getTextContent());

                    total += weight.doubleValue();

                    break;

                default:
                    return -1.0;
            }
        }

        double average = total / counter;
        return average;
    }

    /**
     * getStandardDev()
     * To calculate standard deviation- get the average/mean of the data. Then for each number in the data list
     * subtract the average from each number and then square the result. Then take the average of those
     * squared differences. Then take the square root of that mean.
     * @param nodeList The weight attributes of the devices
     * @return stdDeviation The standard deviation of the devices' weights
     */


    public static double getStandardDev(NodeList nodeList) {

        double mean = getAverage(nodeList);
        double stdTotal = 0;
        int counter = 0;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            counter++;

            //check if the units are pounds, ounces
            switch (element.getAttribute("units")) {

                case "pounds":
                    //Convert the String to a Double
                    Double weight = Double.parseDouble(element.getTextContent()) * 16;

                    //Subtract the mean from it and square the result
                    double difference = (weight.doubleValue() - mean);
                    double result = Math.pow(difference, 2);

                    //Add that to the total
                    stdTotal += result;


                    break;

                case "ounces":
                    // Converts the String to a Double
                    weight = Double.parseDouble(element.getTextContent());

                    //Subtract the mean from it and square the result
                    difference = (weight.doubleValue() - mean);
                    result = Math.pow(difference, 2);

                    //Add that to the total
                    stdTotal += result;

                    break;

                default:
                    return -1.0;
            }
        }

        // Get the average of the total squared differences
        double meanStdTotal = stdTotal / counter;

        double stdDeviation = Math.sqrt(meanStdTotal);
        return stdDeviation;

    }

    /**
     * getWeightMin()
     * @param nodeList The weight attributes of the devices
     * @return min The minimum weight of the devices' weights in US ounces
     */

    public static double getWeightMin(NodeList nodeList) {
        Element firstElement = (Element) nodeList.item(0);

        //Because most of the entry types are in pounds. Assume it's in pounds
        Double firstElementNumber = Double.parseDouble(firstElement.getTextContent()) * 16;
        double min = firstElementNumber.doubleValue();

        if (firstElement.getAttribute("units").equalsIgnoreCase("ounces")) {
            // Have to undo the pound conversion
            Double firstWeight = firstElementNumber / 16;
            min = firstWeight.doubleValue();
        }

        for (int i = 1; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);

            //check if the units are pounds, ounces, or if something else not caught make an absurd number to signal an issue
            switch (element.getAttribute("units")) {

                case "pounds":
                    //Convert the String to a Double
                    Double weight = Double.parseDouble(element.getTextContent()) * 16;

                    if (min > weight.doubleValue()) {
                        min = weight.doubleValue();
                    }

                    break;

                case "ounces":
                    // Converts the String to a Double
                    weight = Double.parseDouble(element.getTextContent());

                    if (min > weight.doubleValue()) {
                        min = weight.doubleValue();
                    }

                    break;

                default:
                    return -1.0; //something is wrong and this should signal we need to look into it
            }

        }

        return min;
    }

    /**
     * getWeightMax()
     * @param nodeList The weight attributes of the devices
     * @return max The maximum weight of the devices' weights in US ounces
     */

    public static double getWeightMax(NodeList nodeList) {
        Element firstElement = (Element) nodeList.item(0);

        //Because most of the entry types are in pounds. Assume it's in pounds
        Double firstElementNumber = Double.parseDouble(firstElement.getTextContent()) * 16;
        double max = firstElementNumber.doubleValue();

        if (firstElement.getAttribute("units").equalsIgnoreCase("ounces")) {
            // Have to undo the pound conversion
            Double firstWeight = firstElementNumber / 16;
            max = firstWeight.doubleValue();
        }

        for (int i = 1; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);

            //check if the units are pounds, ounces, or if something else not caught make an absurd number to signal an issue
            switch (element.getAttribute("units")) {

                case "pounds":
                    //Convert the String to a Double
                    Double weight = Double.parseDouble(element.getTextContent()) * 16;

                    if (max < weight.doubleValue()) {
                        max = weight.doubleValue();
                    }

                    break;

                case "ounces":
                    // Converts the String to a Double
                    weight = Double.parseDouble(element.getTextContent());

                    if (max < weight.doubleValue()) {
                        max = weight.doubleValue();
                    }

                    break;

                default:
                    return -1.0; //something is wrong and this should signal we need to look into it
            }

        }

        return max;
    }

}
