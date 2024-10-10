CREATE TABLE INCOMING_FILESET
(
    INCOMING_FILESET_ID         UUID                                NOT NULL,
    SCHOOL_ID                   UUID                                NOT NULL,
    DEM_FILE_NAME               VARCHAR(255),
    DEM_FILE_DATE_UPLOADED      TIMESTAMP,
    DEM_FILE_STATUS_CODE        VARCHAR(10)                         NOT NULL,
    XAM_FILE_NAME               VARCHAR(255),
    XAM_FILE_DATE_UPLOADED      TIMESTAMP,
    XAM_FILE_STATUS_CODE        VARCHAR(10)                         NOT NULL,
    CRS_FILE_NAME               VARCHAR(255),
    CRS_FILE_DATE_UPLOADED      TIMESTAMP,
    CRS_FILE_STATUS_CODE        VARCHAR(10)                         NOT NULL,
    FILESET_STATUS_CODE         VARCHAR(10)                         NOT NULL,
    CREATE_USER                 VARCHAR(100)                         NOT NULL,
    CREATE_DATE                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                 VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT INCOMING_FILESET_ID_PK PRIMARY KEY (INCOMING_FILESET_ID)
);

CREATE TABLE DEMOGRAPHIC_STUDENT
(
    DEMOGRAPHIC_STUDENT_ID               UUID                                NOT NULL,
    INCOMING_FILESET_ID                  UUID                                NOT NULL,
    STUDENT_STATUS_CODE                  VARCHAR(10)                         NOT NULL,
    LOCAL_ID                             VARCHAR(12),
    PEN                                  VARCHAR(10),
    LAST_NAME                            VARCHAR(255),
    FIRST_NAME                           VARCHAR(255),
    ADDRESS1                             VARCHAR(255),
    ADDRESS2                             VARCHAR(255),
    CITY                                 VARCHAR(255),
    PROVINCIAL_CODE                      VARCHAR(2),
    COUNTRY_CODE                         VARCHAR(3),
    POSTAL_CODE                          VARCHAR(6),
    BIRTHDATE                            VARCHAR(8),
    GENDER                               VARCHAR(1),
    CITIZENSHIP                          VARCHAR(1),
    GRADE                                VARCHAR(10),
    PROGRAM_CODE_1                       VARCHAR(4),
    PROGRAM_CODE_2                       VARCHAR(4),
    PROGRAM_CODE_3                       VARCHAR(4),
    PROGRAM_CODE_4                       VARCHAR(4),
    PROGRAM_CODE_5                       VARCHAR(4),
    PROGRAM_CADRE_FLAG                   VARCHAR(1),
    GRAD_REQUIREMENT_YEAR                VARCHAR(4),
    SCHOOL_CERTIFICATE_COMPLETION_DATE   VARCHAR(8),
    CREATE_USER                          VARCHAR(100)                         NOT NULL,
    CREATE_DATE                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                          VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT DEMOGRAPHIC_STUDENT_ID_PK PRIMARY KEY (DEMOGRAPHIC_STUDENT_ID)
);

CREATE TABLE COURSE_STUDENT
(
    COURSE_STUDENT_ID                    UUID                                NOT NULL,
    INCOMING_FILESET_ID                  UUID                                NOT NULL,
    STUDENT_STATUS_CODE                  VARCHAR(10)                         NOT NULL,
    LOCAL_ID                             VARCHAR(12),
    PEN                                  VARCHAR(10),
    COURSE_CODE                          VARCHAR(5),
    COURSE_LEVEL                         VARCHAR(3),
    COURSE_YEAR                          VARCHAR(4),
    COURSE_MONTH                         VARCHAR(2),
    INTERIM_PERCENTAGE                   VARCHAR(3),
    INTERIM_GRADE                        VARCHAR(2),
    FINAL_PERCENTAGE                     VARCHAR(3),
    FINAL_GRADE                          VARCHAR(2),
    COURSE_STATUS                        VARCHAR(1),
    LAST_NAME                            VARCHAR(255),
    NUMBER_OF_CREDITS                    VARCHAR(2),
    RELATED_COURSE                       VARCHAR(5),
    RELATED_LEVEL                        VARCHAR(3),
    COURSE_DESCRIPTION                   VARCHAR(255),
    COURSE_TYPE                          VARCHAR(1),
    COURSE_GRADUATION_REQUIREMENT        VARCHAR(1),
    CREATE_USER                          VARCHAR(100)                         NOT NULL,
    CREATE_DATE                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                          VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT COURSE_STUDENT_ID_PK PRIMARY KEY (COURSE_STUDENT_ID)
);

CREATE TABLE ASSESSMENT_STUDENT
(
    ASSESSMENT_STUDENT_ID                UUID                                NOT NULL,
    INCOMING_FILESET_ID                  UUID                                NOT NULL,
    STUDENT_STATUS_CODE                  VARCHAR(10)                         NOT NULL,
    LOCAL_ID                             VARCHAR(12),
    PEN                                  VARCHAR(10),
    COURSE_CODE                          VARCHAR(5),
    COURSE_YEAR                          VARCHAR(4),
    COURSE_MONTH                         VARCHAR(2),
    IS_ELECTRONIC_EXAM                   VARCHAR(1),
    PROVINCIAL_SPECIAL_CASE              VARCHAR(255),
    LOCAL_COURSE_ID                      VARCHAR(255),
    COURSE_STATUS                        VARCHAR(255),
    LAST_NAME                            VARCHAR(255),
    CREATE_USER                          VARCHAR(100)                         NOT NULL,
    CREATE_DATE                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                          VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT COURSE_STUDENT_ID_PK PRIMARY KEY (ASSESSMENT_STUDENT_ID)
);

CREATE TABLE STUDENT_STATUS_CODE
(
    STUDENT_STATUS_CODE         VARCHAR(10)                         NOT NULL,
    LABEL                       VARCHAR(30)                         NOT NULL,
    DESCRIPTION                 VARCHAR(255)                        NOT NULL,
    DISPLAY_ORDER               NUMERIC   DEFAULT 1                 NOT NULL,
    CREATE_USER                 VARCHAR(100)                         NOT NULL,
    CREATE_DATE                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                 VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT STUDENT_STATUS_CODE_PK PRIMARY KEY (STUDENT_STATUS_CODE)
);

CREATE TABLE FILE_STATUS_CODE
(
    FILE_STATUS_CODE            VARCHAR(10)                         NOT NULL,
    LABEL                       VARCHAR(30)                         NOT NULL,
    DESCRIPTION                 VARCHAR(255)                        NOT NULL,
    DISPLAY_ORDER               NUMERIC   DEFAULT 1                 NOT NULL,
    CREATE_USER                 VARCHAR(100)                         NOT NULL,
    CREATE_DATE                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                 VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT FILE_STATUS_CODE_PK PRIMARY KEY (FILE_STATUS_CODE)
);

CREATE TABLE DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE
(
    DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE_ID UUID                                NOT NULL,
    DEMOGRAPHIC_STUDENT_ID                  UUID                                NOT NULL,
    VALIDATION_ISSUE_SEVERITY_CODE          VARCHAR(10)                         NOT NULL,
    VALIDATION_ISSUE_CODE                   VARCHAR(100)                        NOT NULL,
    VALIDATION_ISSUE_FIELD_CODE             VARCHAR(100)                        NOT NULL,
    CREATE_USER                             VARCHAR(100)                         NOT NULL,
    CREATE_DATE                             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                             VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE_ID_PK PRIMARY KEY (DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE_ID)
);

CREATE TABLE COURSE_STUDENT_VALIDATION_ISSUE
(
    COURSE_STUDENT_VALIDATION_ISSUE_ID      UUID                                NOT NULL,
    COURSE_STUDENT_ID                       UUID                                NOT NULL,
    VALIDATION_ISSUE_SEVERITY_CODE          VARCHAR(10)                         NOT NULL,
    VALIDATION_ISSUE_CODE                   VARCHAR(100)                        NOT NULL,
    VALIDATION_ISSUE_FIELD_CODE             VARCHAR(100)                        NOT NULL,
    CREATE_USER                             VARCHAR(100)                         NOT NULL,
    CREATE_DATE                             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                             VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT COURSE_STUDENT_VALIDATION_ISSUE_ID_PK PRIMARY KEY (COURSE_STUDENT_VALIDATION_ISSUE_ID)
);

CREATE TABLE ASSESSMENT_STUDENT_VALIDATION_ISSUE
(
    ASSESSMENT_STUDENT_VALIDATION_ISSUE_ID UUID                                 NOT NULL,
    ASSESSMENT_STUDENT_ID                  UUID                                 NOT NULL,
    VALIDATION_ISSUE_SEVERITY_CODE          VARCHAR(10)                         NOT NULL,
    VALIDATION_ISSUE_CODE                   VARCHAR(100)                        NOT NULL,
    VALIDATION_ISSUE_FIELD_CODE             VARCHAR(100)                        NOT NULL,
    CREATE_USER                             VARCHAR(100)                         NOT NULL,
    CREATE_DATE                             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER                             VARCHAR(100)                         NOT NULL,
    UPDATE_DATE                             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ASSESSMENT_STUDENT_VALIDATION_ISSUE_ID_PK PRIMARY KEY (ASSESSMENT_STUDENT_VALIDATION_ISSUE_ID)
);

ALTER TABLE INCOMING_FILESET
    ADD CONSTRAINT FK_INCOMING_FILESET_FILESET_STATUS_CODE FOREIGN KEY (FILESET_STATUS_CODE)
        REFERENCES FILE_STATUS_CODE (FILE_STATUS_CODE);

ALTER TABLE INCOMING_FILESET
    ADD CONSTRAINT FK_INCOMING_FILESET_DEM_FILE_STATUS_CODE FOREIGN KEY (DEM_FILE_STATUS_CODE)
        REFERENCES FILE_STATUS_CODE (FILE_STATUS_CODE);

ALTER TABLE INCOMING_FILESET
    ADD CONSTRAINT FK_INCOMING_FILESET_XAM_FILE_STATUS_CODE FOREIGN KEY (XAM_FILE_STATUS_CODE)
        REFERENCES FILE_STATUS_CODE (FILE_STATUS_CODE);

ALTER TABLE INCOMING_FILESET
    ADD CONSTRAINT FK_INCOMING_FILESET_CRS_FILE_STATUS_CODE FOREIGN KEY (CRS_FILE_STATUS_CODE)
        REFERENCES FILE_STATUS_CODE (FILE_STATUS_CODE);

ALTER TABLE DEMOGRAPHIC_STUDENT
    ADD CONSTRAINT FK_DEMOGRAPHIC_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID)
        REFERENCES INCOMING_FILESET (INCOMING_FILESET_ID);

ALTER TABLE DEMOGRAPHIC_STUDENT
    ADD CONSTRAINT FK_DEMOGRAPHIC_STUDENT_CRS_FILE_STATUS_CODE FOREIGN KEY (STUDENT_STATUS_CODE)
        REFERENCES STUDENT_STATUS_CODE (STUDENT_STATUS_CODE);

ALTER TABLE COURSE_STUDENT
    ADD CONSTRAINT FK_COURSE_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID)
        REFERENCES INCOMING_FILESET (INCOMING_FILESET_ID);

ALTER TABLE COURSE_STUDENT
    ADD CONSTRAINT FK_COURSE_STUDENT_CRS_FILE_STATUS_CODE FOREIGN KEY (STUDENT_STATUS_CODE)
        REFERENCES STUDENT_STATUS_CODE (STUDENT_STATUS_CODE);

ALTER TABLE ASSESSMENT_STUDENT
    ADD CONSTRAINT FK_ASSESSMENT_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID)
        REFERENCES INCOMING_FILESET (INCOMING_FILESET_ID);

ALTER TABLE ASSESSMENT_STUDENT
    ADD CONSTRAINT FK_ASSESSMENT_STUDENT_CRS_FILE_STATUS_CODE FOREIGN KEY (STUDENT_STATUS_CODE)
        REFERENCES STUDENT_STATUS_CODE (STUDENT_STATUS_CODE);

ALTER TABLE DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE
    ADD CONSTRAINT FK_DEMOGRAPHIC_STUDENT_VALIDATION_CRS_FILE_STATUS_CODE FOREIGN KEY (DEMOGRAPHIC_STUDENT_ID)
        REFERENCES DEMOGRAPHIC_STUDENT (DEMOGRAPHIC_STUDENT_ID);

ALTER TABLE COURSE_STUDENT_VALIDATION_ISSUE
    ADD CONSTRAINT FK_COURSE_STUDENT_VALIDATION_CRS_FILE_STATUS_CODE FOREIGN KEY (COURSE_STUDENT_ID)
        REFERENCES COURSE_STUDENT (COURSE_STUDENT_ID);

ALTER TABLE ASSESSMENT_STUDENT_VALIDATION_ISSUE
    ADD CONSTRAINT FK_ASSESSMENT_STUDENT_VALIDATION_CRS_FILE_STATUS_CODE FOREIGN KEY (ASSESSMENT_STUDENT_ID)
        REFERENCES ASSESSMENT_STUDENT (ASSESSMENT_STUDENT_ID);

CREATE INDEX FILESET_STATUS_CODE_IDX ON INCOMING_FILESET (FILESET_STATUS_CODE);
CREATE INDEX DEM_INCOMING_FILESET_ID_IDX ON DEMOGRAPHIC_STUDENT (INCOMING_FILESET_ID);
CREATE INDEX CRS_INCOMING_FILESET_ID_IDX ON COURSE_STUDENT (INCOMING_FILESET_ID);
CREATE INDEX ASSESSMENT_INCOMING_FILESET_ID_IDX ON ASSESSMENT_STUDENT (INCOMING_FILESET_ID);
CREATE INDEX DEMOGRAPHIC_STUDENT_ID_IDX ON DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE (DEMOGRAPHIC_STUDENT_ID);
CREATE INDEX COURSE_STUDENT_ID_IDX ON COURSE_STUDENT_VALIDATION_ISSUE (COURSE_STUDENT_ID);
CREATE INDEX ASSESSMENT_STUDENT_ID_IDX ON ASSESSMENT_STUDENT_VALIDATION_ISSUE (ASSESSMENT_STUDENT_ID);