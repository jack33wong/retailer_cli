package com.generic.retailer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generic.retailer.model.Item;
import com.generic.retailer.model.Order;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

public class ProductService {

  private static String products_endpoint = "http://localhost:8080/retailer/v0/products";
  HashMap<String,Item> itemMap = new HashMap<>();

  public void loadItems(){

    StringBuffer content = new StringBuffer();
    try {
      URL url = new URL(products_endpoint);
      HttpURLConnection con = null;
      con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setRequestMethod("GET");
      ObjectMapper mapper = new ObjectMapper();

      try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          content.append(inputLine).append(System.lineSeparator());
        }
      }
      Order order = mapper.readValue(content.toString(), new TypeReference<Order>(){});
      order.getTrolley().stream().forEach(i -> itemMap.put(i.getName(), Item.builder().name(i.getName()).price(i.getPrice()).build()));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public Optional<Item> findItem(String name){
    name = name.toUpperCase();
    return itemMap.containsKey(name) ? Optional.of(itemMap.get(name)) : Optional.empty();
  }

}
