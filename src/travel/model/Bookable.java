package travel.model;

import travel.util.CurrencyFormat;

public interface Bookable {

    String getId();

    String getName();

    long getPrice();

    default String priceLabel() {
        return CurrencyFormat.rupiah(getPrice());
    }
}
