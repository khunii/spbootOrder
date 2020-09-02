DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders
(
  id bigserial NOT NULL,
  order_user_id character varying(255),
  ordered_date character varying(255),
  updated_date character varying(255),
  status character varying(255),
  CONSTRAINT order_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS shipping_address CASCADE;
CREATE TABLE shipping_address
(
  id bigserial NOT NULL,
  zip_code character varying(255),
  recipient character varying(255),
  order_id bigint,
  CONSTRAINT shipping_address_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS ordered_product CASCADE;
CREATE TABLE ordered_product
(
  id bigserial NOT NULL,
  product_id bigint,
  price bigint,
  qty int,
  order_id bigint,
  CONSTRAINT ordered_product_pkey PRIMARY KEY (id)
);
