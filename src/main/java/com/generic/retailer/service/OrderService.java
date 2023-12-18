package com.generic.retailer.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generic.retailer.model.Order;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class OrderService {

  private static String checkout_endpoint = "http://localhost:8080/retailer/v0/order/checkout?receipt=true";
  private Order order;
  private ProductService productService;

  public OrderService(){
    order = Order.builder().trolley(new ArrayList<>()).build();
    productService = new ProductService();
    productService.loadItems();
  }

  public void addItem(Optional<String> name){
    if(name.isPresent())
      order.addItem(productService.findItem(name.get()), 1);
  }

  public String checkout(LocalDate date) throws IOException {
    order.setDate(date.toString());
    return processCheckoutRequest();
  }

  private String processCheckoutRequest() throws IOException {

    StringBuilder content = new StringBuilder();

    URL url = new URL(checkout_endpoint);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setDoOutput(true);
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json");

    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    String jsonInputString = mapper.writeValueAsString(order);

    try (OutputStream os = con.getOutputStream()) {
      byte[] input = jsonInputString.getBytes("utf-8");
      os.write(input, 0, input.length);
    }

    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine).append(System.lineSeparator());
      }
    }

    Order order = mapper.readValue(content.toString(), new TypeReference<Order>(){});

    return order.getReceipt();
  }

}
