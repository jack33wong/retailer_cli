package com.generic.retailer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Order {

  @JsonProperty("_id")
  private String id;
  private String receipt;
  private double total;
  private List<Item> discounts;
  private String date;
  private List<Item> trolley;

  public void addItem(Optional<Item> item, long qty){
    if(item.isPresent()){
      Item toUpdateItem = item.get();
      toUpdateItem.setQuantity(qty);
      trolley.add(toUpdateItem);
    }
  }


}
