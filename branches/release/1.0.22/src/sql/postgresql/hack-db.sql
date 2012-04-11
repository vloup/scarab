---
--- This is a hack specific to PostgreSQL: implicit conversions from
--- bool to smallint are no longer supported, and this causes problems
--- with columns that are defined as BOLLEANINT in a Torque schema.
--- These lines add a cast to the Scarab database that resolves the
--- problem, although it is not particularly elegant!
---
CREATE FUNCTION bool2smallint(boolean) RETURNS smallint AS $$
   SELECT CASE WHEN $1 THEN INT2(1) ELSE INT2(0) END;
$$ LANGUAGE SQL;

CREATE CAST (boolean AS smallint) WITH FUNCTION bool2smallint(boolean) AS IMPLICIT;
