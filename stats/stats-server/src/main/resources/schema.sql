CREATE TABLE IF NOT EXISTS "stats" (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	app varchar(50) NOT NULL,
	uri varchar(50) NOT NULL,
	ip varchar(50) NOT NULL,
	"timestamp" timestamp NOT NULL,
	CONSTRAINT stats_pk PRIMARY KEY (id)
);