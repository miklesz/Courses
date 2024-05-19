BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "Order_product" (
	"order_id"	SMALLINT UNSIGNED NOT NULL,
	"product_id"	SMALLINT UNSIGNED NOT NULL,
	"amount"	TINYINT(2) NOT NULL
);
CREATE TABLE IF NOT EXISTS "Customer" (
	"id"	SMALLINT UNSIGNED IDENTITY(1 , 1) NOT NULL,
	"name"	VARCHAR(50) NOT NULL,
	"city"	VARCHAR(50) NOT NULL,
	"date"	DATE NOT NULL,
	PRIMARY KEY("id")
);
CREATE TABLE IF NOT EXISTS "Product" (
	"id"	SMALLINT UNSIGNED IDENTITY(1 , 1) NOT NULL,
	"name"	VARCHAR(50) NOT NULL,
	"price"	FLOAT NOT NULL,
	"amount"	TINYINT(2) NOT NULL,
	"date"	DATE NOT NULL,
	PRIMARY KEY("id")
);
INSERT INTO "Order_product" VALUES (1,2,2);
INSERT INTO "Order_product" VALUES (1,4,1);
INSERT INTO "Order_product" VALUES (2,6,1);
INSERT INTO "Order_product" VALUES (2,8,1);
INSERT INTO "Order_product" VALUES (2,5,2);
INSERT INTO "Order_product" VALUES (3,5,1);
INSERT INTO "Order_product" VALUES (3,7,2);
INSERT INTO "Order_product" VALUES (4,5,1);
INSERT INTO "Order_product" VALUES (4,2,1);
INSERT INTO "Order_product" VALUES (4,7,2);
INSERT INTO "Order_product" VALUES (5,4,1);
INSERT INTO "Order_product" VALUES (6,6,1);
INSERT INTO "Customer" VALUES (1,'Adam','Lublin','2011-02-05');
INSERT INTO "Customer" VALUES (2,'Monika','Gdynia','2011-02-19');
INSERT INTO "Customer" VALUES (3,'Natalia','Zakopane','2011-02-23');
INSERT INTO "Customer" VALUES (4,'Katarzyna','Lublin','2011-03-08');
INSERT INTO "Customer" VALUES (5,'Marcin','Warszawa','2011-03-21');
INSERT INTO "Product" VALUES (5,'Spodnie',100.0,5,'2011-02-01');
INSERT INTO "Product" VALUES (6,'Bluza',60.0,2,'2011-02-12');
COMMIT;
