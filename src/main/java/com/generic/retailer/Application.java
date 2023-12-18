package com.generic.retailer;

import com.generic.retailer.service.Cli;
import com.generic.retailer.service.OrderService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Application {

  public static void main(String[] args) {

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
    OrderService orderService = new OrderService();
    try(Cli cli = Cli.create(reader, writer, orderService)) {
      cli.run();
    } catch (Exception e) {
      System.exit(1);
    }
  }

}
