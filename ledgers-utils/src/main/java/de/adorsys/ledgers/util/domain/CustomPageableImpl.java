package de.adorsys.ledgers.util.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPageableImpl {
    private int page;
    private int size;
}
