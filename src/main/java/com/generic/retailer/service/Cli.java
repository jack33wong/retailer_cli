package com.generic.retailer.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.NonNull;

import static java.util.Objects.requireNonNull;

public final class Cli implements AutoCloseable {

  public static Cli create(@NonNull String prompt, @NonNull BufferedReader reader, @NonNull BufferedWriter writer, @NonNull OrderService orderService, @NonNull LocalDate date) {
    requireNonNull(prompt);
    requireNonNull(reader);
    requireNonNull(writer);
    requireNonNull(orderService);
    return new Cli(prompt, reader, writer, orderService, date);
  }

  public static Cli create(BufferedReader reader, BufferedWriter writer, OrderService orderService) {
    return new Cli(">", reader, writer, orderService, LocalDate.now());
  }

  private static final Predicate<String> WHITESPACE = Pattern.compile("^\\s{0,}$").asPredicate();

  private final String prompt;
  private final BufferedReader reader;
  private final BufferedWriter writer;
  private final LocalDate date;
  private final OrderService orderService;

  private Cli(String prompt, BufferedReader reader, BufferedWriter writer, OrderService orderService, LocalDate date) {
    this.prompt = prompt;
    this.reader = reader;
    this.writer = writer;
    this.date = date;
    this.orderService = orderService;
  }

  private void prompt() throws IOException {
    writeLine(prompt);
  }

  private Optional<String> readLine() throws IOException {
    String line = reader.readLine();
    return line == null || WHITESPACE.test(line) ? Optional.empty() : Optional.of(line);
  }

  private void writeLine(String line) throws IOException {
    writer.write(line);
    writer.newLine();
    writer.flush();
  }

  public void run() throws IOException {
    writeLine("What would you like to buy?");
    prompt();
    Optional<String> line = readLine();
    orderService.addItem(line);
    while (line.isPresent()) {
      writeLine("Would you like anything else?");
      prompt();
      line = readLine();
      orderService.addItem(line);
    }
    writeLine(orderService.checkout(date));
  }

  @Override
  public void close() throws Exception {
      reader.close();
      writer.close();
  }

}
