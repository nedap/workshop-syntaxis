-- RECREATE TABLES

DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS composition;

CREATE TABLE IF NOT EXISTS client
(
    id         INTEGER primary key autoincrement,
    first_name TEXT not null,
    last_name  TEXT not null
);

CREATE TABLE IF NOT EXISTS composition
(
    id        INTEGER primary key autoincrement,
    client_id INTEGER not null references client on delete cascade,
    rm_object  TEXT    not null
);

-- SEED CLIENTS

WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
INSERT INTO client (first_name, last_name)
SELECT
    CASE ABS(RANDOM() % 10)
        WHEN 0 THEN 'Jan'
        WHEN 1 THEN 'Piet'
        WHEN 2 THEN 'Klaas'
        WHEN 3 THEN 'Sanne'
        WHEN 4 THEN 'Lisa'
        WHEN 5 THEN 'Mark'
        WHEN 6 THEN 'Eva'
        WHEN 7 THEN 'Tom'
        WHEN 8 THEN 'Nina'
        ELSE 'Lucas'
        END,
    CASE ABS(RANDOM() % 10)
        WHEN 0 THEN 'Jansen'
        WHEN 1 THEN 'De Vries'
        WHEN 2 THEN 'Bakker'
        WHEN 3 THEN 'Visser'
        WHEN 4 THEN 'Smit'
        WHEN 5 THEN 'Meijer'
        WHEN 6 THEN 'Bos'
        WHEN 7 THEN 'Mulder'
        WHEN 8 THEN 'Dekker'
        ELSE 'Peters'
        END
FROM seq;

-- SEED COMPOSITIONS

-- Body Weight

WITH RECURSIVE
    client_measurements AS (
        SELECT
            id AS client_id,
            65 + ABS(RANDOM() % 26) AS base_weight,   -- 65–90 kg
            ABS(RANDOM() % 21)      AS measurement_count,  -- 0–20

            -- Random per client (1x bepaald en hergebruikt)
            ABS(RANDOM() % 30)    AS start_day_offset,
            ABS(RANDOM() % 86400) AS start_second_offset

        FROM client
    ),
    measurements AS (
        SELECT
            client_id,
            base_weight,
            start_day_offset,
            start_second_offset,
            measurement_count,
            1 AS idx,
            0 AS cumulative_minutes
        FROM client_measurements
        WHERE measurement_count > 0

        UNION ALL

        SELECT
            client_id,
            base_weight,
            start_day_offset,
            start_second_offset,
            measurement_count,
            idx + 1,
            cumulative_minutes
                + CASE
                -- ~75%: small gaps (1..360 min => meerdere op 1 dag)
                -- ~25%: large gaps (1..10080 min => tot 7 dagen ertussen)
                      WHEN ABS(RANDOM() % 100) < 50 THEN 1 + ABS(RANDOM() % 360)
                      ELSE 1 + ABS(RANDOM() % 10080)
                END AS cumulative_minutes
        FROM measurements
        WHERE idx < measurement_count
    ),
    prepared_body_weight AS (
        SELECT
            client_id,

            -- gewicht als getal
            base_weight + ((RANDOM() % 200) - 100) / 100.0 AS magnitude_num,

            -- monotone timestamp per client, with random gaps
            (
                replace(
                        datetime(
                                'now',

                            -- Zorg dat alles in het verleden eindigt:
                            -- startpunt = (start_day_offset + tot max 7 dagen per meting) dagen geleden.
                                '-' || (start_day_offset + measurement_count * 7) || ' days',
                                '-' || start_second_offset || ' seconds',

                            -- Opbouwende tijdlijn in minuten (random stappen)
                                '+' || cumulative_minutes || ' minutes'
                        ),
                        ' ',
                        'T'
                ) || '+01:00'
                ) AS timestamp,

            CASE ABS(RANDOM() % 4)
                WHEN 0 THEN 'Naakt'
                WHEN 1 THEN 'Luier'
                WHEN 2 THEN 'Lichte kleding/ondergoed'
                ELSE 'Volledig gekleed, inclusief schoenen'
                END AS clothing

        FROM measurements
    )

INSERT INTO composition (client_id, rm_object)
SELECT
    client_id,

    printf(
            '{"name":{"_type":"DV_TEXT","value":"Lichaamsgewicht","mappings":[]},"links":[],"content":[{"data":{"name":{"_type":"DV_TEXT","value":"","mappings":[]},"links":[],"events":[{"data":{"name":{"_type":"DV_TEXT","value":"","mappings":[]},"_type":"ITEM_TREE","items":[{"name":{"_type":"DV_TEXT","value":"Gewicht","mappings":[]},"_type":"ELEMENT","links":[],"value":{"_type":"DV_QUANTITY","units":"kg","magnitude":%s,"precision":0,"other_reference_ranges":[]},"archetype_node_id":"id5"}],"links":[],"archetype_node_id":"id2"},"name":{"_type":"DV_TEXT","value":"Elke gebeurtenis","mappings":[]},"time":{"value":"%s","other_reference_ranges":[]},"_type":"POINT_EVENT","links":[],"state":{"name":{"_type":"DV_TEXT","value":"","mappings":[]},"_type":"ITEM_TREE","items":[{"name":{"_type":"DV_TEXT","value":"Kleding","mappings":[]},"_type":"ELEMENT","links":[],"value":{"_type":"DV_CODED_TEXT","value":"%s","mappings":[],"defining_code":{"code_string":"at12","terminology_id":{"value":"ac9022.1"}}},"archetype_node_id":"id10"}],"links":[],"archetype_node_id":"id9"},"archetype_node_id":"id4"}],"origin":{"value":"%s","other_reference_ranges":[]},"archetype_node_id":"id3"},"name":{"_type":"DV_TEXT","value":"Lichaamsgewicht","mappings":[]},"_type":"OBSERVATION","links":[],"subject":{"_type":"PARTY_SELF"},"encoding":{"code_string":"UTF-8","terminology_id":{"value":"IANA_character-sets"}},"language":{"code_string":"nl","terminology_id":{"value":"ISO_639-1"}},"archetype_details":{"rm_version":"1.0.4","archetype_id":{"value":"openEHR-EHR-OBSERVATION.body_weight-ovl.v1.0.0"}},"archetype_node_id":"id0.0.2","other_participations":[]}],"category":{"value":"event","mappings":[],"defining_code":{"code_string":"433","terminology_id":{"value":"openehr"}}},"composer":{"name":"employee","_type":"PARTY_IDENTIFIED","identifiers":[]},"language":{"code_string":"nl","terminology_id":{"value":"ISO_639-1"}},"territory":{"code_string":"NL","terminology_id":{"value":"ISO_3166-1"}},"archetype_details":{"rm_version":"1.0.4","archetype_id":{"value":"openEHR-EHR-COMPOSITION.body_weight_report.v1.0.0"}},"archetype_node_id":"id1.1.1"}',

            printf('%.1f', magnitude_num),
            timestamp,
            clothing,
            timestamp
    )
FROM prepared_body_weight;

-- Blood Pressure

WITH RECURSIVE
    client_measurements AS (
        SELECT
            id AS client_id,
            100 + ABS(RANDOM() % 21) AS base_sys,          -- 100–120 mmHg
            65 + ABS(RANDOM() % 16)  AS base_dia,          -- 65–80 mmHg
            ABS(RANDOM() % 21)       AS measurement_count, -- 0–20

            -- Random per client (1x bepaald en hergebruikt)
            ABS(RANDOM() % 30)    AS start_day_offset,
            ABS(RANDOM() % 86400) AS start_second_offset

        FROM client
    ),
    measurements AS (
        SELECT
            client_id,
            base_sys,
            base_dia,
            start_day_offset,
            start_second_offset,
            measurement_count,
            1 AS idx,
            0 AS cumulative_minutes
        FROM client_measurements
        WHERE measurement_count > 0

        UNION ALL

        SELECT
            client_id,
            base_sys,
            base_dia,
            start_day_offset,
            start_second_offset,
            measurement_count,
            idx + 1,
            cumulative_minutes
                + CASE
                -- ~75%: small gaps (1..360 min => meerdere op 1 dag)
                -- ~25%: large gaps (1..10080 min => tot 7 dagen ertussen)
                      WHEN ABS(RANDOM() % 100) < 50 THEN 1 + ABS(RANDOM() % 360)
                      ELSE 1 + ABS(RANDOM() % 10080)
                END AS cumulative_minutes
        FROM measurements
        WHERE idx < measurement_count
    ),
    prepared_blood_pressure AS (
        SELECT
            -- monotone timestamp per client, with random gaps
            (
                replace(
                        datetime(
                                'now',

                            -- Zorg dat alles in het verleden eindigt:
                            -- startpunt = (start_day_offset + tot max 7 dagen per meting) dagen geleden.
                                '-' || (start_day_offset + measurement_count * 7) || ' days',
                                '-' || start_second_offset || ' seconds',

                            -- Opbouwende tijdlijn in minuten (random stappen)
                                '+' || cumulative_minutes || ' minutes'
                        ),
                        ' ',
                        'T'
                ) || '+01:00'
                ) AS timestamp,

            client_id,
            max(90,  min(140, base_sys + ((RANDOM() % 30) - 15))) AS sys, -- 90–140 mmHg
            max(60,  min(90,  base_dia + ((RANDOM() % 20) - 10))) AS dia  -- 60–90 mmHg

        FROM measurements
    )

INSERT INTO composition (client_id, rm_object)
SELECT
    client_id,
    printf(
        '{"name":{"_type":"DV_TEXT","value":"Bloeddruk","mappings":[]},"links":[],"content":[{"data":{"name":{"_type":"DV_TEXT","value":"Geschiedenis","mappings":[]},"links":[],"events":[{"data":{"name":{"_type":"DV_TEXT","value":"","mappings":[]},"_type":"ITEM_TREE","items":[{"name":{"_type":"DV_TEXT","value":"Systolisch","mappings":[]},"_type":"ELEMENT","links":[],"value":{"_type":"DV_QUANTITY","units":"mm[Hg]","magnitude":%s,"precision":0,"other_reference_ranges":[]},"archetype_node_id":"id5"},{"name":{"_type":"DV_TEXT","value":"Diastolisch","mappings":[]},"_type":"ELEMENT","links":[],"value":{"_type":"DV_QUANTITY","units":"mm[Hg]","magnitude":%s,"precision":0,"other_reference_ranges":[]},"archetype_node_id":"id6"}],"links":[],"archetype_node_id":"id4"},"name":{"_type":"DV_TEXT","value":"Elke gebeurtenis","mappings":[]},"time":{"value":"%s","other_reference_ranges":[]},"_type":"POINT_EVENT","links":[],"archetype_node_id":"id7"}],"origin":{"value":"%s","other_reference_ranges":[]},"archetype_node_id":"id2"},"name":{"_type":"DV_TEXT","value":"Bloeddruk","mappings":[]},"_type":"OBSERVATION","links":[],"subject":{"_type":"PARTY_SELF"},"encoding":{"code_string":"UTF-8","terminology_id":{"value":"IANA_character-sets"}},"language":{"code_string":"nl","terminology_id":{"value":"ISO_639-1"}},"archetype_details":{"rm_version":"1.0.4","archetype_id":{"value":"openEHR-EHR-OBSERVATION.blood_pressure-ovl.v1.0.0"}},"archetype_node_id":"id0.0.2","other_participations":[]}],"category":{"value":"event","mappings":[],"defining_code":{"code_string":"433","terminology_id":{"value":"openehr"}}},"composer":{"name":"administrator","_type":"PARTY_IDENTIFIED","identifiers":[],"external_ref":{"id":{"_type":"HIER_OBJECT_ID","value":"com.nedap.NE0001.employee::2"},"type":"PERSON","namespace":"demographic"}},"language":{"code_string":"nl","terminology_id":{"value":"ISO_639-1"}},"territory":{"code_string":"NL","terminology_id":{"value":"ISO_3166-1"}},"archetype_details":{"rm_version":"1.0.4","archetype_id":{"value":"openEHR-EHR-COMPOSITION.blood_pressure_report.v1.0.0"}},"archetype_node_id":"id1.1.1"}',
        sys,
        dia,
        timestamp,
        timestamp
    )
FROM prepared_blood_pressure;

-- Body temperature

WITH RECURSIVE
    client_measurements AS (
        SELECT
            id AS client_id,
            36 + (RANDOM() % 5) / 10.0 AS base_temp,       -- 36.0–40.0 °C
            ABS(RANDOM() % 21)       AS measurement_count, -- 0–20

            -- Random per client (1x bepaald en hergebruikt)
            ABS(RANDOM() % 30)    AS start_day_offset,
            ABS(RANDOM() % 86400) AS start_second_offset

        FROM client
    ),
    measurements AS (
        SELECT
            client_id,
            base_temp,
            start_day_offset,
            start_second_offset,
            measurement_count,
            1 AS idx,
            0 AS cumulative_minutes
        FROM client_measurements
        WHERE measurement_count > 0

        UNION ALL

        SELECT
            client_id,
            base_temp,
            start_day_offset,
            start_second_offset,
            measurement_count,
            idx + 1,
            cumulative_minutes
                + CASE
                -- ~75%: small gaps (1..360 min => meerdere op 1 dag)
                -- ~25%: large gaps (1..10080 min => tot 7 dagen ertussen)
                      WHEN ABS(RANDOM() % 100) < 50 THEN 1 + ABS(RANDOM() % 360)
                      ELSE 1 + ABS(RANDOM() % 10080)
                END AS cumulative_minutes
        FROM measurements
        WHERE idx < measurement_count
    ),
    prepared_body_temperature AS (
        SELECT
            client_id,
            max(35.5, min(41.5, base_temp + ((ABS(RANDOM()) % 11) - 5) / 10.0)) AS body_temperature,

            -- monotone timestamp per client, with random gaps
            (
                replace(
                        datetime(
                                'now',

                            -- Zorg dat alles in het verleden eindigt:
                            -- startpunt = (start_day_offset + tot max 7 dagen per meting) dagen geleden.
                                '-' || (start_day_offset + measurement_count * 7) || ' days',
                                '-' || start_second_offset || ' seconds',

                            -- Opbouwende tijdlijn in minuten (random stappen)
                                '+' || cumulative_minutes || ' minutes'
                        ),
                        ' ',
                        'T'
                ) || '+01:00'
                ) AS timestamp

        FROM measurements
    )

INSERT INTO composition (client_id, rm_object)
SELECT
    client_id,
    printf(
        '{"name":{"_type":"DV_TEXT","value":"Lichaamstemperatuur","mappings":[]},"links":[],"content":[{"data":{"name":{"_type":"DV_TEXT","value":"","mappings":[]},"links":[],"events":[{"data":{"name":{"_type":"DV_TEXT","value":"","mappings":[]},"_type":"ITEM_TREE","items":[{"name":{"_type":"DV_TEXT","value":"Temperatuur","mappings":[]},"_type":"ELEMENT","links":[],"value":{"_type":"DV_QUANTITY","units":"Cel","magnitude":%s,"precision":1,"other_reference_ranges":[]},"archetype_node_id":"id5.1"}],"links":[],"archetype_node_id":"id2"},"name":{"_type":"DV_TEXT","value":"Elke gebeurtenis","mappings":[]},"time":{"value":"%s","other_reference_ranges":[]},"_type":"POINT_EVENT","links":[],"archetype_node_id":"id4"}],"origin":{"value":"%s","other_reference_ranges":[]},"archetype_node_id":"id3"},"name":{"_type":"DV_TEXT","value":"Lichaamstemperatuur","mappings":[]},"_type":"OBSERVATION","links":[],"subject":{"_type":"PARTY_SELF"},"encoding":{"code_string":"UTF-8","terminology_id":{"value":"IANA_character-sets"}},"language":{"code_string":"nl","terminology_id":{"value":"ISO_639-1"}},"archetype_details":{"rm_version":"1.0.4","archetype_id":{"value":"openEHR-EHR-OBSERVATION.body_temperature-ovl.v1.0.0"}},"archetype_node_id":"id0.0.2","other_participations":[]}],"category":{"value":"event","mappings":[],"defining_code":{"code_string":"433","terminology_id":{"value":"openehr"}}},"composer":{"name":"administrator","_type":"PARTY_IDENTIFIED","identifiers":[],"external_ref":{"id":{"_type":"HIER_OBJECT_ID","value":"com.nedap.NE0001.employee::2"},"type":"PERSON","namespace":"demographic"}},"language":{"code_string":"nl","terminology_id":{"value":"ISO_639-1"}},"territory":{"code_string":"NL","terminology_id":{"value":"ISO_3166-1"}},"archetype_details":{"rm_version":"1.0.4","archetype_id":{"value":"openEHR-EHR-COMPOSITION.body_temperature_report.v1.0.0"}},"archetype_node_id":"id1.1.1"}',
        body_temperature,
        timestamp,
        timestamp
    )
FROM prepared_body_temperature;
