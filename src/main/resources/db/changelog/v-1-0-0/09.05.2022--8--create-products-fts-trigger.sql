CREATE OR REPLACE FUNCTION  product_fulltext_trigger() RETURNS trigger AS
'
BEGIN
    new.fulltext :=
            ((setweight(to_tsvector(''pg_catalog.russian'', new.title), ''A'')) ||
             (setweight(to_tsvector(''pg_catalog.russian'', new.description), ''B'')));
    RETURN new;
END;
'
    LANGUAGE plpgsql;

CREATE TRIGGER products_fts_trigger
    BEFORE INSERT OR UPDATE
    ON products
    FOR EACH ROW
EXECUTE FUNCTION product_fulltext_trigger();
