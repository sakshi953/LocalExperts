package com.localexperts.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

// ── City Model ────────────────────────────────────────────────
@Data
@NoArgsConstructor
@Document(collection = "cities")
public class City {

    @Id
    private String id;

    @Indexed(unique = true)
    private String key;

    private String name;

    private String state = "Maharashtra";

    private String pin;

    private String icon = "🏙️";

    private String mapLabel;

    private Double lat;
    private Double lng;

    private boolean isActive = true;
}
