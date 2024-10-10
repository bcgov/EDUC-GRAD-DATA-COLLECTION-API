CREATE TABLE GDC_SHEDLOCK
(
    NAME       VARCHAR(64),
    LOCK_UNTIL TIMESTAMP(3) NULL,
    LOCKED_AT  TIMESTAMP(3) NULL,
    LOCKED_BY  VARCHAR(255),
    CONSTRAINT GDC_SHEDLOCK_PK PRIMARY KEY (NAME)
);

COMMENT ON TABLE GDC_SHEDLOCK IS 'This table is used to achieve distributed lock between pods, for schedulers.';