package com.StockApi.StockApi.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class StockPriceFetcherService {

    private static final String API_KEY = "E1OAIVLF2YOMYW5V";
    private static final String INTERVAL = "1min";
    private static final DecimalFormat df = new DecimalFormat("#.######");

    public Map<String, String> fetchStockData(String symbols) throws IOException, JSONException {
        Map<String, String> stockResults = new LinkedHashMap<>();
        String[] symbolArray = symbols.split(",");

        for (String symbol : symbolArray) {
            symbol = symbol.trim(); // Trim any whitespace
            String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol="
                    + symbol + "&interval=" + INTERVAL + "&apikey=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuffer content;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }

                JSONObject jsonResponse = new JSONObject(content.toString());
                if (!jsonResponse.has("Time Series (1min)")) {
                    stockResults.put(symbol, "No data available for this symbol.");
                    continue;
                }

                JSONObject timeSeries = jsonResponse.getJSONObject("Time Series (1min)");

                Iterator<String> iterator = timeSeries.keys();
                ArrayList<String> allTimestamps = new ArrayList<>();
                while (iterator.hasNext()) {
                    allTimestamps.add(iterator.next());
                }

                Collections.sort(allTimestamps, Collections.reverseOrder());
                String latestTime = allTimestamps.get(0);
                JSONObject latestData = timeSeries.getJSONObject(latestTime);

                String open = latestData.getString("1. open");
                double openPrice = Double.parseDouble(open);
                String close = latestData.getString("4. close");
                double closePrice = Double.parseDouble(close);
                double change = closePrice - openPrice;
                double percentChange = (change / openPrice) * 100;

                StringBuilder result = new StringBuilder();
                result.append("Change is: ").append(df.format(change)).append("\n");
                result.append("Percentage Change: ").append(df.format(percentChange)).append("%\n\n");
                result.append("Latest data for ").append(symbol).append(" at ").append(latestTime).append(":\n");
                result.append("Open: ").append(latestData.getString("1. open")).append("\n");
                result.append("High: ").append(latestData.getString("2. high")).append("\n");
                result.append("Low: ").append(latestData.getString("3. low")).append("\n");
                result.append("Close: ").append(latestData.getString("4. close")).append("\n");
                result.append("Volume: ").append(latestData.getString("5. volume")).append("\n");

                stockResults.put(symbol, result.toString());
            } else {
                stockResults.put(symbol, "Failed to fetch data. Response Code: " + responseCode);
            }
        }
        return stockResults;
    }
}
