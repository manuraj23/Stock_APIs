package com.StockApi.StockApi.controller;

import com.StockApi.StockApi.service.StockPriceFetcherService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Controller
public class StockController {

    @Autowired
    private StockPriceFetcherService stockPriceFetcherService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/stock")
    public String getStockData(@RequestParam String symbol, Model model) {
        try {
            Map<String, String> stockData = stockPriceFetcherService.fetchStockData(symbol);
            model.addAttribute("stockDataMap", stockData);
        } catch (IOException | JSONException e) {
            model.addAttribute("error", "Error fetching stock data: " + e.getMessage());
        }
        return "index";
    }
}
