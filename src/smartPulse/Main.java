package smartPulse;
import org.w3c.dom.*;
import org.w3c.dom.Element;

import javax.xml.parsers.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            String urlString = "https://seffaflik.epias.com.tr/transparency/service/market/intra-day-trade-history?endDate=2023-05-31&startDate=2023-05-31";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(conn.getInputStream());
            
            NodeList nodeList = document.getElementsByTagName("intraDayTradeHistoryList");

            Map<String, Map<String, Object>> contracts = new HashMap<>();
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String contract = element.getElementsByTagName("conract").item(0).getTextContent();
                String date = element.getElementsByTagName("date").item(0).getTextContent();
                
                if (contract.startsWith("PH")) {
                    if (!contracts.containsKey(contract)) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("totalPriceQuantity", 0.0);
                        data.put("totalQuantity", 0.0);
                        data.put("date", date);
                        contracts.put(contract, data);
                    }

                    double price = Double.parseDouble(element.getElementsByTagName("price").item(0).getTextContent())/10;
                    double quantity = Double.parseDouble(element.getElementsByTagName("quantity").item(0).getTextContent())/10;
                    contracts.get(contract).put("totalPriceQuantity", (Double)contracts.get(contract).get("totalPriceQuantity") + price * quantity);
                    contracts.get(contract).put("totalQuantity", (Double)contracts.get(contract).get("totalQuantity") + quantity);
                }
            }

            for (String contract : contracts.keySet()) {
                double totalPriceQuantity = (Double)contracts.get(contract).get("totalPriceQuantity");
                double totalQuantity = (Double)contracts.get(contract).get("totalQuantity");
                String date = (String)contracts.get(contract).get("date");
                double averageWeightedPrice = totalPriceQuantity / totalQuantity;
                System.out.println("Contract: " + contract);
                System.out.println("Date: " + date);
                System.out.println("Total Transaction Amount: " + totalPriceQuantity);
                System.out.println("Total Transaction Quantity: " + totalQuantity);
                System.out.println("Weighted Average Price: " + averageWeightedPrice);
                System.out.println("---------------------------------------------------");
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
