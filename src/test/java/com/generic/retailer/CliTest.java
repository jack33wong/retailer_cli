package com.generic.retailer;

import com.generic.retailer.service.Cli;
import com.generic.retailer.service.OrderService;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CliTest {

  private static BufferedReader reader(String... lines) {
    StringBuilder builder = new StringBuilder();
    Arrays
        .stream(lines)
        .forEach(line -> builder.append(line).append(lineSep()));
    return new BufferedReader(new StringReader(builder.toString()));
  }

  private static String lineSep() {
    return System.lineSeparator();
  }

   /*
    * The receipt format should be as per below:
    *
    *    "===== RECEIPT ======",
    *    "DVD           £15.00",
    *    "CD            £10.00",
    *    "BOOK           £5.00",
    *    "THURS         -£6.00",
    *    "====================",
    *    "TOTAL         £24.00"
   */
  private static void assertReceipt(StringWriter writer, String... expected) {
    String[] obtained = writer.toString().split(System.lineSeparator());
    int expectedNumItems = expected.length - 3;
    String[] items = new String[expectedNumItems];
    int numItems = 0;
    boolean receiptHeader = false;
    boolean receiptEnd = false;
    String total = "";
    for (int i = 0; i < obtained.length; i++) {
      if (!receiptHeader) {
        if ("===== RECEIPT ======".equals(obtained[i])) {
          receiptHeader = true;
        }
        // Everything before receipt header is ignored
      } else if (!receiptEnd) {
          if ("====================".equals(obtained[i])) {
            receiptEnd = true;
          } else {
            if (numItems == expectedNumItems) {
              fail("Too many items");
            }
            items[numItems] = obtained[i];
            numItems++;
          }
      } else {
        total = obtained[i];
        break;
      }
    }
    assertThat(receiptHeader).isTrue();
    assertThat(receiptEnd).isTrue();
    assertThat(items).containsExactlyInAnyOrder(Arrays.copyOfRange(expected, 1, expectedNumItems + 1));
    assertThat(total).isEqualTo(expected[expected.length - 1]);
  }

  @Test
  public void testReceipt() throws IOException {

    BufferedReader reader = reader(
        "cd",
        "dvd",
        "book"
    );
    StringWriter writer = new StringWriter();
    LocalDate notThursday = LocalDate.now();
    if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
      notThursday.plusDays(1);
    }
    OrderService orderService = new OrderService();
    Cli cli = Cli.create(">", reader, new BufferedWriter(writer), orderService, notThursday);
    cli.run();
    assertReceipt(
        writer,
        "===== RECEIPT ======",
        "CD            £10.00",
        "DVD           £15.00",
        "BOOK           £5.00",
        "====================",
        "TOTAL         £30.00"
    );
  }

  @Test
  public void testAggregatedReceipt() throws IOException {
    BufferedReader reader = reader(
        "cd",
        "cd",
        "book"
    );

    StringWriter writer = new StringWriter();
    LocalDate notThursday = LocalDate.now();
    if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
      notThursday.plusDays(1);
    }
    OrderService orderService = new OrderService();
    Cli cli = Cli.create(">", reader, new BufferedWriter(writer), orderService, notThursday);
    cli.run();
    assertReceipt(
        writer,
        "===== RECEIPT ======",
        "CD (x2)       £20.00",
        "BOOK           £5.00",
        "====================",
        "TOTAL         £25.00"
    );
  }

  @Test
  public void testDiscountTwoForOne() throws IOException {
    BufferedReader reader = reader(
        "dvd",
        "dvd",
        "book"
    );

    StringWriter writer = new StringWriter();
    LocalDate notThursday = LocalDate.now();
    if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
      notThursday.plusDays(1);
    }
    OrderService orderService = new OrderService();
    Cli cli = Cli.create(">", reader, new BufferedWriter(writer), orderService, notThursday);
    cli.run();
    assertReceipt(
        writer,
        "===== RECEIPT ======",
        "DVD (x2)      £30.00",
        "BOOK           £5.00",
        "2 FOR 1      -£15.00",
        "====================",
        "TOTAL         £20.00"
    );
  }

  @Test
  public void testDiscountThursdays() throws IOException {
    BufferedReader reader = reader(
            "dvd",
            "cd",
            "book"
    );

    StringWriter writer = new StringWriter();
    LocalDate thursday = LocalDate.now();
    while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
      thursday = thursday.plusDays(1);
    }
    OrderService orderService = new OrderService();
    Cli cli = Cli.create(">", reader, new BufferedWriter(writer), orderService, thursday);
    cli.run();
    assertReceipt(
        writer,
        "===== RECEIPT ======",
        "DVD           £15.00",
        "CD            £10.00",
        "BOOK           £5.00",
        "THURS         -£6.00",
        "====================",
        "TOTAL         £24.00"
    );
  }

  @Test
  public void testDiscount2For1OnThursdays() throws IOException {
    BufferedReader reader = reader(
        "dvd",
        "dvd",
        "book"
    );

    StringWriter writer = new StringWriter();
    LocalDate thursday = LocalDate.now();
    while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
      thursday = thursday.plusDays(1);
    }
    OrderService orderService = new OrderService();
    Cli cli = Cli.create(">", reader, new BufferedWriter(writer), orderService, thursday);
    cli.run();
    assertReceipt(
        writer,
        "===== RECEIPT ======",
        "DVD (x2)      £30.00",
        "BOOK           £5.00",
        "2 FOR 1      -£15.00",
        "THURS         -£1.00",
        "====================",
        "TOTAL         £19.00"
    );
  }

  @Test
  public void testThreeDVDDiscount2For1OnThursdays() throws IOException {
    BufferedReader reader = reader(
        "dvd",
        "dvd",
        "dvd"
    );

    StringWriter writer = new StringWriter();
    LocalDate thursday = LocalDate.now();
    while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
      thursday = thursday.plusDays(1);
    }
    OrderService orderService = new OrderService();
    Cli cli = Cli.create(">", reader, new BufferedWriter(writer), orderService, thursday);
    cli.run();
    assertReceipt(
        writer,
        "===== RECEIPT ======",
        "DVD (x3)      £45.00",
        "2 FOR 1      -£15.00",
        "THURS         -£3.00",
        "====================",
        "TOTAL         £27.00"
    );
  }
}
