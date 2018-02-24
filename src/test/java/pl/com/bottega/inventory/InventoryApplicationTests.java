package pl.com.bottega.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.data.MapEntry.entry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InventoryApplicationTests {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private DbCleaner dbCleaner;

  @Autowired
  private ObjectMapper objectMapper;

  @Before
  public void cleanDb() {
    dbCleaner.clean();
  }

  @Test
  public void successfulInflate() throws Exception {
    inflate("Shoes", 20).andExpect(status().isOk());
  }

  @Test
  public void missingSkuCodeToInflate() throws Exception {
    inflate(null, 20).andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.skuCode").value("can't be blank"));
    inflate("", 20).andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.skuCode").value("can't be blank"));
  }

  @Test
  public void missingAmountToInflate() throws Exception {
    inflate("Shoes", null).andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.amount").value("can't be blank"));
  }

  @Test
  public void invalidAmountsToInflate() throws Exception {
    inflate("Shoes", -1).andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.amount").value("must be between 1 and 999"));
    inflate("Shoes", 1000).andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.amount").value("must be between 1 and 999"));
  }

  @Test
  public void succesfulPurchase() throws Exception {
    inflate("Socks", 10);
    inflate("Shoes", 20);
    inflate("Trousers", 50);

    purchase(
        entry("Socks", 2),
        entry("Shoes", 2),
        entry("Trousers", 14)
    ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.success").value(true)).
        andExpect(jsonPath("$.purchasedProducts.Socks").value(2)).
        andExpect(jsonPath("$.purchasedProducts.Shoes").value(2)).
        andExpect(jsonPath("$.purchasedProducts.Trousers").value(14));
  }

  @Test
  public void invalidSkuCodesToPurchase() throws Exception {
    inflate("Pants", 10);

    purchase(
        entry("Pants", 10),
        entry("Socks", 2),
        entry("Trousers", 2)
    ).
        andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.Socks").value("no such sku")).
        andExpect(jsonPath("$.errors.Trousers").value("no such sku")).
        andExpect(jsonPath("$.errors.Pants").doesNotExist());
  }

  @Test
  public void notEnoughStock() throws Exception {
    inflate("Pants", 10);
    inflate("Trousers", 10);

    purchase(
        entry("Pants", 5)
    ).
        andExpect(status().isOk());

    purchase(
        entry("Pants", 6),
        entry("Trousers", 10)
    ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.success").value(false)).
        andExpect(jsonPath("$.missingProducts.Pants").value(6)).
        andExpect(jsonPath("$.missingProducts.Trousers").doesNotExist()).
        andExpect(jsonPath("$.purchasedProducts.Pants").doesNotExist()).
        andExpect(jsonPath("$.purchasedProducts.Trousers").doesNotExist())
    ;
  }

  @Test
  public void noProductsToPurchase() throws Exception {
    purchase().andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.skus").value("are required"));
  }

  @Test
  public void eitherAllPurchasedOrNone() throws Exception {
    inflate("Pants", 10);
    inflate("Trousers", 10);

    purchase(
        entry("Pants", 5),
        entry("Trousers", 11)
    ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.success").value(false));

    purchase(
        entry("Pants", 6),
        entry("Trousers", 10)
    ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.success").value(true));
  }

  @Test
  public void inflateSameProductMultipleTimes() throws Exception {
    inflate("Pants", 10);
    inflate("Pants", 10);

    purchase(
        entry("Pants", 20)
    ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.success").value(true));
  }

  @Test
  public void invalidPurchase() throws Exception {
    purchase(entry("Pants", -1), entry("Shoes", 1001)).
        andExpect(status().isUnprocessableEntity()).
        andExpect(jsonPath("$.errors.Pants").value("must be between 1 and 999")).
        andExpect(jsonPath("$.errors.Shoes").value("must be between 1 and 999"))
        ;
  }

  private String toBuy(MapEntry<String, Integer>... items) throws Exception {
    Map<String, Integer> map = new HashMap<>();
    for (MapEntry<String, Integer> item : items)
      map.put(item.getKey(), item.getValue());
    return objectMapper.writeValueAsString(map);
  }


  private ResultActions inflate(String skuCode, Integer amount) throws Exception {
    Map<String, Object> content = new HashMap<>();
    content.put("skuCode", skuCode);
    content.put("amount", amount);
    return mvc.perform(post("/inventory").
        content(objectMapper.writeValueAsString(content)).
        contentType(MediaType.APPLICATION_JSON));
  }

  private ResultActions purchase(MapEntry<String, Integer>... skus) throws Exception {
    return mvc.perform(post("/purchase").
        content(toBuy(skus)).
        contentType(MediaType.APPLICATION_JSON));
  }
}